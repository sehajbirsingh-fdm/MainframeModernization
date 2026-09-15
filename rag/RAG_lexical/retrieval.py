"""Run isolated semantic, BM25, or RRF-hybrid code retrieval."""

from __future__ import annotations

import argparse
import json
import os
from functools import lru_cache
from pathlib import Path
from typing import Any, Literal

import lancedb
from sentence_transformers import SentenceTransformer

from rag.embedding_model import MODEL_NAME, MODEL_REVISION
from rag.sync_embedding_table import DATABASE_PATH, TABLE_NAME

from .build_fts_index import FTS_COLUMN


QUERY_PREFIX = "Represent this query for searching relevant code: "
RESULTS_PATH = Path(__file__).resolve().parent / "hybrid_results.json"
RetrievalMode = Literal["semantic", "lexical", "hybrid"]

RESULT_COLUMNS = [
    "chunk_id",
    "file_id",
    "filename",
    "source_kind",
    "feature",
    "start_line",
    "end_line",
    "file_summary",
    "node_summary_texts",
    "chunk_text",
]


def _device() -> str:
    configured = os.getenv("RAG_EMBEDDING_DEVICE")
    if configured:
        return configured
    try:
        import torch

        if torch.backends.mps.is_available():
            return "mps"
        if torch.cuda.is_available():
            return "cuda"
    except ImportError:
        pass
    return "cpu"


@lru_cache(maxsize=1)
def _model() -> SentenceTransformer:
    return SentenceTransformer(
        MODEL_NAME,
        revision=MODEL_REVISION,
        trust_remote_code=True,
        device=_device(),
        local_files_only=True,
    )


@lru_cache(maxsize=1)
def _table() -> Any:
    database = lancedb.connect(DATABASE_PATH)
    if TABLE_NAME not in set(database.list_tables().tables):
        raise FileNotFoundError(
            f"LanceDB table '{TABLE_NAME}' does not exist at {DATABASE_PATH}"
        )
    return database.open_table(TABLE_NAME)


def semantic_search(question: str, limit: int = 30) -> list[dict[str, Any]]:
    """Preserve the existing CodeRankEmbed cosine retrieval path."""
    query_vector = _model().encode(
        QUERY_PREFIX + question,
        normalize_embeddings=True,
        convert_to_numpy=True,
    ).astype("float32")

    rows = (
        _table()
        .search(query_vector.tolist(), vector_column_name="embedding")
        .where("embedding_status = 'ready'")
        .distance_type("cosine")
        .select([*RESULT_COLUMNS, "_distance"])
        .limit(limit)
        .to_list()
    )
    for rank, row in enumerate(rows, start=1):
        row["semantic_rank"] = rank
        row["cosine_similarity"] = round(1.0 - row.pop("_distance"), 6)
    return rows


def lexical_search(question: str, limit: int = 30) -> list[dict[str, Any]]:
    """Retrieve exact lexical matches with LanceDB BM25 full-text search."""
    try:
        rows = (
            _table()
            .search(question, query_type="fts", fts_columns=FTS_COLUMN)
            .where("embedding_status = 'ready'")
            .select([*RESULT_COLUMNS, "_score"])
            .limit(limit)
            .to_list()
        )
    except Exception as exception:
        raise RuntimeError(
            "Lexical search failed. Run "
            "`python -m rag.RAG_lexical.build_fts_index` first."
        ) from exception

    for rank, row in enumerate(rows, start=1):
        row["lexical_rank"] = rank
        row["bm25_score"] = round(float(row.pop("_score")), 6)
    return rows


def _rrf_fuse(
    semantic: list[dict[str, Any]],
    lexical: list[dict[str, Any]],
    semantic_weight: float,
    lexical_weight: float,
    rrf_k: int,
) -> list[dict[str, Any]]:
    combined: dict[str, dict[str, Any]] = {}

    for row in semantic:
        item = dict(row)
        item["rrf_score"] = semantic_weight / (rrf_k + row["semantic_rank"])
        combined[row["chunk_id"]] = item

    for row in lexical:
        existing = combined.get(row["chunk_id"])
        lexical_score = lexical_weight / (rrf_k + row["lexical_rank"])
        if existing is None:
            item = dict(row)
            item["rrf_score"] = lexical_score
            combined[row["chunk_id"]] = item
            continue
        existing["lexical_rank"] = row["lexical_rank"]
        existing["bm25_score"] = row["bm25_score"]
        existing["rrf_score"] += lexical_score

    return sorted(
        combined.values(),
        key=lambda item: (
            -item["rrf_score"],
            item.get("semantic_rank", 10**9),
            item.get("lexical_rank", 10**9),
            item["chunk_id"],
        ),
    )


def hybrid_search(
    question: str,
    semantic_limit: int = 30,
    lexical_limit: int = 30,
    final_limit: int = 10,
    semantic_weight: float = 1.0,
    lexical_weight: float = 1.0,
    rrf_k: int = 60,
) -> list[dict[str, Any]]:
    """Fuse independent semantic and BM25 rankings and return the top results."""
    semantic = semantic_search(question, semantic_limit)
    lexical = lexical_search(question, lexical_limit)
    fused = _rrf_fuse(
        semantic,
        lexical,
        semantic_weight=semantic_weight,
        lexical_weight=lexical_weight,
        rrf_k=rrf_k,
    )
    rows = fused[:final_limit]
    for rank, row in enumerate(rows, start=1):
        row["rank"] = rank
        row["rrf_score"] = round(row["rrf_score"], 8)
    return rows


def retrieve(
    question: str,
    mode: RetrievalMode = "hybrid",
    semantic_limit: int = 30,
    lexical_limit: int = 30,
    final_limit: int = 10,
    semantic_weight: float = 1.0,
    lexical_weight: float = 1.0,
    rrf_k: int = 60,
) -> list[dict[str, Any]]:
    """Return a stable context shape for any experimental retrieval mode."""
    if mode == "semantic":
        rows = semantic_search(question, final_limit)
    elif mode == "lexical":
        rows = lexical_search(question, final_limit)
    elif mode == "hybrid":
        rows = hybrid_search(
            question,
            semantic_limit=semantic_limit,
            lexical_limit=lexical_limit,
            final_limit=final_limit,
            semantic_weight=semantic_weight,
            lexical_weight=lexical_weight,
            rrf_k=rrf_k,
        )
    else:
        raise ValueError(f"Unsupported retrieval mode: {mode}")

    contexts: list[dict[str, Any]] = []
    for rank, row in enumerate(rows, start=1):
        contexts.append(
            {
                "rank": rank,
                "chunk_id": row["chunk_id"],
                "file_id": row["file_id"],
                "filename": row["filename"],
                "source_kind": row["source_kind"],
                "feature": row["feature"],
                "start_line": row["start_line"],
                "end_line": row["end_line"],
                "file_summary": row["file_summary"],
                "node_summary_texts": row["node_summary_texts"],
                "text": row["chunk_text"],
                "cosine_similarity": row.get("cosine_similarity"),
                "bm25_score": row.get("bm25_score"),
                "semantic_rank": row.get("semantic_rank"),
                "lexical_rank": row.get("lexical_rank"),
                "rrf_score": row.get("rrf_score"),
            }
        )
    return contexts


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--query", help="Question to search; prompts interactively when omitted")
    parser.add_argument("--mode", choices=("semantic", "lexical", "hybrid"), default="hybrid")
    parser.add_argument("--semantic-candidates", type=int, default=30)
    parser.add_argument("--lexical-candidates", type=int, default=30)
    parser.add_argument("--limit", type=int, default=10)
    parser.add_argument("--semantic-weight", type=float, default=1.0)
    parser.add_argument("--lexical-weight", type=float, default=1.0)
    args = parser.parse_args()

    question = (args.query or input("Enter your codebase question: ")).strip()
    if not question:
        raise SystemExit("Query cannot be empty.")
    if min(args.semantic_candidates, args.lexical_candidates, args.limit) < 1:
        parser.error("candidate counts and limit must be positive")

    results = retrieve(
        question,
        mode=args.mode,
        semantic_limit=args.semantic_candidates,
        lexical_limit=args.lexical_candidates,
        final_limit=args.limit,
        semantic_weight=args.semantic_weight,
        lexical_weight=args.lexical_weight,
    )
    payload = {
        "query": question,
        "mode": args.mode,
        "semantic_candidates": args.semantic_candidates,
        "lexical_candidates": args.lexical_candidates,
        "final_limit": args.limit,
        "semantic_weight": args.semantic_weight,
        "lexical_weight": args.lexical_weight,
        "results": results,
    }
    RESULTS_PATH.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")

    print(f"\nTop {len(results)} {args.mode} results:")
    for result in results:
        signals = []
        if result["semantic_rank"] is not None:
            signals.append(f"semantic={result['semantic_rank']}")
        if result["lexical_rank"] is not None:
            signals.append(f"lexical={result['lexical_rank']}")
        if result["rrf_score"] is not None:
            signals.append(f"rrf={result['rrf_score']:.6f}")
        print(
            f"{result['rank']:>2}. {result['file_id']}:"
            f"{result['start_line']}-{result['end_line']} "
            f"({' | '.join(signals)})"
        )
    print(f"\nFull results written to {RESULTS_PATH}")


if __name__ == "__main__":
    main()
