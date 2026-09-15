"""Rerank hybrid candidates with a code-search cross-encoder."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import sqlite3
from functools import lru_cache
from pathlib import Path
from typing import Any

import numpy as np
import onnxruntime as ort
from huggingface_hub import hf_hub_download
from transformers import PreTrainedTokenizerFast

from .retrieval import retrieve


# This model was trained on multilingual NL-to-code pairs with hard negatives
# mined by nomic-ai/CodeRankEmbed, which is also this project's retriever.
RERANK_MODEL = "faxenoff/code-daemon-reranker-v1"
RERANK_MODEL_REVISION = "f56371723d562efa497302027b595139f070b228"
RERANK_MAX_LENGTH = 256
RERANK_QUERY_MAX_TOKENS = 96
RERANK_PATH_MAX_TOKENS = 48
RERANK_WINDOW_OVERLAP = 32
RERANK_RRF_K = 60
DEFAULT_RETRIEVAL_RANK_WEIGHT = 0.5
DEFAULT_RERANKER_RANK_WEIGHT = 0.5
RESULTS_PATH = Path(__file__).resolve().parent / "rerank_results.json"
CACHE_PATH = Path(__file__).resolve().parent / "rerank_scores.sqlite3"


def _providers() -> list[str]:
    available = set(ort.get_available_providers())
    configured = os.getenv("RAG_RERANK_PROVIDER")
    if configured:
        if configured not in available:
            raise ValueError(
                f"RAG_RERANK_PROVIDER={configured!r} is unavailable; "
                f"choose from {sorted(available)}"
            )
        if configured == "CPUExecutionProvider":
            return [configured]
        return [configured, "CPUExecutionProvider"]

    # Core ML is substantially slower for this partially supported dynamic graph.
    # RAG_EMBEDDING_DEVICE controls CodeRankEmbed only; reranking defaults to CPU.
    return ["CPUExecutionProvider"]


@lru_cache(maxsize=1)
def _tokenizer() -> PreTrainedTokenizerFast:
    tokenizer_path = hf_hub_download(
        RERANK_MODEL,
        "tokenizer.json",
        revision=RERANK_MODEL_REVISION,
    )
    return PreTrainedTokenizerFast(
        tokenizer_file=tokenizer_path,
        bos_token="<s>",
        cls_token="<s>",
        eos_token="</s>",
        sep_token="</s>",
        unk_token="<unk>",
        pad_token="<pad>",
        mask_token="<mask>",
        # Long chunks are intentionally tokenized first and windowed below.
        model_max_length=1_000_000,
    )


@lru_cache(maxsize=1)
def _session() -> ort.InferenceSession:
    model_path = hf_hub_download(
        RERANK_MODEL,
        "model.onnx",
        revision=RERANK_MODEL_REVISION,
    )
    return ort.InferenceSession(model_path, providers=_providers())


def active_provider() -> str:
    """Return the configured primary ONNX Runtime provider."""
    return _providers()[0]


def _predict(pairs: list[tuple[str, str]], batch_size: int) -> np.ndarray:
    tokenizer = _tokenizer()
    session = _session()
    input_names = {item.name for item in session.get_inputs()}
    scores: list[float] = []
    for start in range(0, len(pairs), batch_size):
        batch = pairs[start : start + batch_size]
        encoded = tokenizer(
            [query for query, _ in batch],
            [document for _, document in batch],
            padding=True,
            truncation=True,
            max_length=RERANK_MAX_LENGTH,
            return_tensors="np",
            return_token_type_ids=False,
        )
        inputs = {
            name: np.asarray(value, dtype=np.int64)
            for name, value in encoded.items()
            if name in input_names
        }
        logits = session.run(None, inputs)[0]
        scores.extend(float(value) for value in np.asarray(logits).reshape(-1))
    return np.asarray(scores, dtype=np.float32)


def _score_key(question: str, candidate: dict[str, Any]) -> str:
    payload = {
        "model": RERANK_MODEL,
        "revision": RERANK_MODEL_REVISION,
        "max_length": RERANK_MAX_LENGTH,
        "query_max_tokens": RERANK_QUERY_MAX_TOKENS,
        "window_overlap": RERANK_WINDOW_OVERLAP,
        "question": question,
        "chunk_id": candidate["chunk_id"],
        "file_id": candidate["file_id"],
        "text": candidate.get("text") or "",
    }
    encoded = json.dumps(payload, sort_keys=True, separators=(",", ":")).encode()
    return hashlib.sha256(encoded).hexdigest()


def _cached_scores(keys: list[str]) -> dict[str, float]:
    if not CACHE_PATH.exists() or not keys:
        return {}
    with sqlite3.connect(CACHE_PATH) as connection:
        connection.execute(
            "CREATE TABLE IF NOT EXISTS scores "
            "(cache_key TEXT PRIMARY KEY, score REAL NOT NULL)"
        )
        placeholders = ",".join("?" for _ in keys)
        rows = connection.execute(
            f"SELECT cache_key, score FROM scores WHERE cache_key IN ({placeholders})",
            keys,
        )
        return {str(key): float(score) for key, score in rows}


def _store_scores(values: dict[str, float]) -> None:
    if not values:
        return
    with sqlite3.connect(CACHE_PATH) as connection:
        connection.execute(
            "CREATE TABLE IF NOT EXISTS scores "
            "(cache_key TEXT PRIMARY KEY, score REAL NOT NULL)"
        )
        connection.executemany(
            "INSERT OR REPLACE INTO scores(cache_key, score) VALUES (?, ?)",
            values.items(),
        )


def _truncate_query(question: str) -> str:
    tokenizer = _tokenizer()
    token_ids = tokenizer.encode(question, add_special_tokens=False)
    if len(token_ids) <= RERANK_QUERY_MAX_TOKENS:
        return question
    return tokenizer.decode(
        token_ids[:RERANK_QUERY_MAX_TOKENS], skip_special_tokens=True
    )


def _candidate_windows(question: str, candidate: dict[str, Any]) -> list[str]:
    """Keep every part of a code chunk visible to the short-context reranker."""
    tokenizer = _tokenizer()
    path_tokens = tokenizer.encode(candidate["file_id"], add_special_tokens=False)
    display_path = tokenizer.decode(
        path_tokens[-RERANK_PATH_MAX_TOKENS:], skip_special_tokens=True
    )
    prefix = f"File: {display_path}\nCode:\n"
    query_tokens = tokenizer.encode(question, add_special_tokens=False)
    prefix_tokens = tokenizer.encode(prefix, add_special_tokens=False)
    special_tokens = tokenizer.num_special_tokens_to_add(pair=True)
    code_budget = (
        RERANK_MAX_LENGTH
        - len(query_tokens)
        - len(prefix_tokens)
        - special_tokens
    )
    if code_budget < 32:
        code_budget = 32

    code_tokens = tokenizer.encode(
        str(candidate.get("text") or ""), add_special_tokens=False
    )
    if not code_tokens:
        return [prefix]

    overlap = min(RERANK_WINDOW_OVERLAP, code_budget // 4)
    step = max(1, code_budget - overlap)
    windows = []
    for start in range(0, len(code_tokens), step):
        token_window = code_tokens[start : start + code_budget]
        windows.append(
            prefix + tokenizer.decode(token_window, skip_special_tokens=True)
        )
        if start + code_budget >= len(code_tokens):
            break
    return windows


def _validate_rank_weights(retrieval_weight: float, reranker_weight: float) -> None:
    if retrieval_weight < 0 or reranker_weight < 0:
        raise ValueError("rank-fusion weights cannot be negative")
    if retrieval_weight + reranker_weight == 0:
        raise ValueError("at least one rank-fusion weight must be positive")


def rerank(
    question: str,
    semantic_limit: int = 60,
    lexical_limit: int = 60,
    candidate_limit: int = 30,
    final_limit: int = 8,
    semantic_weight: float = 0.9,
    lexical_weight: float = 0.1,
    retrieval_rank_weight: float = DEFAULT_RETRIEVAL_RANK_WEIGHT,
    reranker_rank_weight: float = DEFAULT_RERANKER_RANK_WEIGHT,
    rerank_rrf_k: int = RERANK_RRF_K,
    batch_size: int = 64,
) -> list[dict[str, Any]]:
    """Retrieve candidates, score all code windows, and conservatively rerank."""
    if final_limit > candidate_limit:
        raise ValueError("final_limit cannot exceed candidate_limit")
    _validate_rank_weights(retrieval_rank_weight, reranker_rank_weight)

    candidates = retrieve(
        question,
        mode="hybrid",
        semantic_limit=semantic_limit,
        lexical_limit=lexical_limit,
        final_limit=candidate_limit,
        semantic_weight=semantic_weight,
        lexical_weight=lexical_weight,
    )
    if not candidates:
        return []

    model_question = _truncate_query(question)
    score_keys = [_score_key(model_question, candidate) for candidate in candidates]
    cached = _cached_scores(score_keys)
    pairs: list[tuple[str, str]] = []
    pair_candidate_indexes: list[int] = []
    window_counts = [0] * len(candidates)
    for candidate_index, candidate in enumerate(candidates):
        if score_keys[candidate_index] in cached:
            continue
        windows = _candidate_windows(model_question, candidate)
        window_counts[candidate_index] = len(windows)
        pairs.extend((model_question, window) for window in windows)
        pair_candidate_indexes.extend([candidate_index] * len(windows))

    candidate_scores = [cached.get(key, float("-inf")) for key in score_keys]
    if pairs:
        raw_scores = _predict(pairs, batch_size=batch_size)
        for candidate_index, score in zip(pair_candidate_indexes, raw_scores):
            candidate_scores[candidate_index] = max(
                candidate_scores[candidate_index], float(score)
            )
        _store_scores(
            {
                score_keys[index]: candidate_scores[index]
                for index in set(pair_candidate_indexes)
            }
        )

    reranked: list[dict[str, Any]] = []
    for index, (candidate, score, window_count) in enumerate(
        zip(candidates, candidate_scores, window_counts)
    ):
        item = dict(candidate)
        item["candidate_rank"] = candidate["rank"]
        item["rerank_score"] = round(score, 8)
        item["rerank_window_count"] = window_count
        item["rerank_cache_hit"] = score_keys[index] in cached
        reranked.append(item)

    model_order = sorted(
        reranked,
        key=lambda item: (-item["rerank_score"], item["candidate_rank"]),
    )
    for model_rank, item in enumerate(model_order, start=1):
        item["rerank_rank"] = model_rank
        item["rerank_fusion_score"] = (
            retrieval_rank_weight / (rerank_rrf_k + item["candidate_rank"])
            + reranker_rank_weight / (rerank_rrf_k + model_rank)
        )

    reranked.sort(
        key=lambda item: (
            -item["rerank_fusion_score"],
            item["candidate_rank"],
            item["rerank_rank"],
        )
    )
    results = reranked[:final_limit]
    for rank, result in enumerate(results, start=1):
        result["rank"] = rank
        result["rerank_fusion_score"] = round(
            result["rerank_fusion_score"], 8
        )
    return results


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--query", help="Question to rerank; prompts when omitted")
    parser.add_argument("--semantic-candidates", type=int, default=60)
    parser.add_argument("--lexical-candidates", type=int, default=60)
    parser.add_argument("--candidate-limit", type=int, default=30)
    parser.add_argument("--final-limit", type=int, default=8)
    parser.add_argument("--semantic-weight", type=float, default=0.9)
    parser.add_argument("--lexical-weight", type=float, default=0.1)
    parser.add_argument(
        "--retrieval-rank-weight",
        type=float,
        default=DEFAULT_RETRIEVAL_RANK_WEIGHT,
    )
    parser.add_argument(
        "--reranker-rank-weight",
        type=float,
        default=DEFAULT_RERANKER_RANK_WEIGHT,
    )
    parser.add_argument("--rerank-rrf-k", type=int, default=RERANK_RRF_K)
    parser.add_argument("--batch-size", type=int, default=64)
    args = parser.parse_args()

    question = (args.query or input("Enter your codebase question: ")).strip()
    if not question:
        raise SystemExit("Query cannot be empty.")
    counts = (
        args.semantic_candidates,
        args.lexical_candidates,
        args.candidate_limit,
        args.final_limit,
        args.rerank_rrf_k,
        args.batch_size,
    )
    if min(counts) < 1:
        parser.error("candidate counts, limits, RRF K, and batch size must be positive")
    if args.final_limit > args.candidate_limit:
        parser.error("--final-limit cannot exceed --candidate-limit")
    if args.semantic_weight < 0 or args.lexical_weight < 0:
        parser.error("retrieval weights cannot be negative")
    if args.semantic_weight + args.lexical_weight == 0:
        parser.error("at least one retrieval weight must be positive")
    try:
        _validate_rank_weights(
            args.retrieval_rank_weight, args.reranker_rank_weight
        )
    except ValueError as exception:
        parser.error(str(exception))

    results = rerank(
        question,
        semantic_limit=args.semantic_candidates,
        lexical_limit=args.lexical_candidates,
        candidate_limit=args.candidate_limit,
        final_limit=args.final_limit,
        semantic_weight=args.semantic_weight,
        lexical_weight=args.lexical_weight,
        retrieval_rank_weight=args.retrieval_rank_weight,
        reranker_rank_weight=args.reranker_rank_weight,
        rerank_rrf_k=args.rerank_rrf_k,
        batch_size=args.batch_size,
    )
    payload = {
        "query": question,
        "model": RERANK_MODEL,
        "model_revision": RERANK_MODEL_REVISION,
        "semantic_candidates": args.semantic_candidates,
        "lexical_candidates": args.lexical_candidates,
        "candidate_limit": args.candidate_limit,
        "final_limit": args.final_limit,
        "semantic_weight": args.semantic_weight,
        "lexical_weight": args.lexical_weight,
        "retrieval_rank_weight": args.retrieval_rank_weight,
        "reranker_rank_weight": args.reranker_rank_weight,
        "rerank_rrf_k": args.rerank_rrf_k,
        "onnx_provider": active_provider(),
        "results": results,
    }
    RESULTS_PATH.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")

    print(f"\nTop {len(results)} reranked results:")
    for result in results:
        print(
            f"{result['rank']:>2}. {result['rerank_fusion_score']:.6f} "
            f"{result['file_id']}:{result['start_line']}-{result['end_line']} "
            f"(candidate={result['candidate_rank']} | "
            f"code-rerank={result['rerank_rank']} | "
            f"windows={result['rerank_window_count']})"
        )
    print(f"\nFull results written to {RESULTS_PATH}")


if __name__ == "__main__":
    main()
