"""Evaluate isolated semantic, lexical, and hybrid retrieval experiments."""

from __future__ import annotations

import argparse
import json
import time
from datetime import datetime, timezone
from pathlib import Path
from statistics import mean
from typing import Any

import yaml

from rag.rag_evaluation.retrieval_assertions import score_retrieval

from .retrieval import retrieve


ROOT = Path(__file__).resolve().parent
CASES_PATH = ROOT.parent / "rag_evaluation" / "eval_cases.yaml"
RESULTS_DIR = ROOT / "evaluation-results"
ANALYSIS_PATH = RESULTS_DIR / "analysis.txt"
HISTORY_PATH = RESULTS_DIR / "retrieval_history.jsonl"


def _timestamp() -> tuple[str, str]:
    value = datetime.now(timezone.utc)
    return value.isoformat(timespec="seconds"), value.strftime("%Y%m%d_%H%M%S")


def _load_cases(case_limit: int | None) -> list[dict[str, Any]]:
    cases = yaml.safe_load(CASES_PATH.read_text(encoding="utf-8"))
    if not isinstance(cases, list):
        raise ValueError(f"Expected a list of cases in {CASES_PATH}")
    return cases[:case_limit] if case_limit else cases


def _gold_details(
    gold: list[dict[str, Any]], score: dict[str, Any]
) -> list[dict[str, Any]]:
    details = []
    for gold_index, source in enumerate(gold):
        ranks = [
            rank
            for rank, matches in enumerate(score["retrieved_matches"], start=1)
            if gold_index in matches
        ]
        details.append({**source, "matched": bool(ranks), "matched_ranks": ranks})
    return details


def _retrieved_details(
    contexts: list[dict[str, Any]], score: dict[str, Any]
) -> list[dict[str, Any]]:
    details = []
    for item, matches in zip(contexts, score["retrieved_matches"]):
        details.append(
            {
                "rank": item["rank"],
                "chunk_id": item["chunk_id"],
                "file_id": item["file_id"],
                "start_line": item["start_line"],
                "end_line": item["end_line"],
                "cosine_similarity": item["cosine_similarity"],
                "bm25_score": item["bm25_score"],
                "semantic_rank": item["semantic_rank"],
                "lexical_rank": item["lexical_rank"],
                "rrf_score": item["rrf_score"],
                "candidate_rank": item.get("candidate_rank"),
                "rerank_score": item.get("rerank_score"),
                "rerank_rank": item.get("rerank_rank"),
                "rerank_fusion_score": item.get("rerank_fusion_score"),
                "rerank_window_count": item.get("rerank_window_count"),
                "rerank_cache_hit": item.get("rerank_cache_hit"),
                "is_labeled_relevant": bool(matches),
                "matched_gold_indices": matches,
            }
        )
    return details


def _averages(results: list[dict[str, Any]]) -> dict[str, float]:
    metric_names = ("hit_at_k", "precision_at_k", "recall_at_k", "mrr", "ndcg_at_k")
    return {
        name: mean(result["metrics"][name] for result in results) if results else 0.0
        for name in metric_names
    }


def _read_history() -> list[dict[str, Any]]:
    if not HISTORY_PATH.exists():
        return []
    return [
        json.loads(line)
        for line in HISTORY_PATH.read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]


def _write_analysis(summary: dict[str, Any], details_path: Path) -> None:
    history = _read_history()
    failed_ids = [
        str(result["case_number"]) for result in summary["results"] if not result["passed"]
    ]
    metrics = summary["averages"]
    config = summary["configuration"]
    lines = [
        "Bank of Z Hybrid Retrieval Experiment",
        "====================================",
        f"Latest run (UTC): {summary['timestamp_utc']}",
        f"Mode: {summary['mode']}",
        f"Questions: {summary['questions']}",
        f"Final chunks (K): {summary['chunk_size']}",
        f"Candidates: semantic={config['semantic_candidates']}, lexical={config['lexical_candidates']}",
        f"RRF weights: semantic={config['semantic_weight']}, lexical={config['lexical_weight']}",
        "",
        "Results",
        "-------",
        f"Passed: {summary['passed']}",
        f"Failed: {summary['failed']}",
        f"Pass rate: {summary['pass_rate']:.2%}",
        f"Hit@{summary['chunk_size']}: {metrics['hit_at_k']:.4f}",
        f"Precision@{summary['chunk_size']}: {metrics['precision_at_k']:.4f}",
        f"Recall@{summary['chunk_size']}: {metrics['recall_at_k']:.4f}",
        f"MRR: {metrics['mrr']:.4f}",
        f"nDCG@{summary['chunk_size']}: {metrics['ndcg_at_k']:.4f}",
        f"Failed question IDs: {', '.join(failed_ids) if failed_ids else 'None'}",
        f"Elapsed seconds: {summary['elapsed_seconds']}",
        "",
        "Recent runs",
        "-----------",
        "Timestamp (UTC)              Mode      K  Pass%   Hit@K  Recall@K  MRR",
    ]
    if summary["mode"] == "rerank":
        lines.insert(8, f"Candidates sent to reranker: {config['candidate_limit']}")
        lines.insert(9, f"Reranker: {config['rerank_model']}")
        lines.insert(
            10,
            f"ONNX provider: {config['rerank_provider']}",
        )
        lines.insert(
            11,
            "Final rank fusion: "
            f"retrieval={config['retrieval_rank_weight']}, "
            f"reranker={config['reranker_rank_weight']}",
        )
    for run in history[-10:]:
        averages = run["averages"]
        lines.append(
            f"{run['timestamp_utc']:<28} {run['mode']:<9} {run['chunk_size']:>2} "
            f"{run['pass_rate']:>6.1%} {averages['hit_at_k']:>7.3f} "
            f"{averages['recall_at_k']:>8.3f} {averages['mrr']:>6.3f}"
        )
    lines.extend(
        [
            "",
            "Artifacts",
            "---------",
            f"Question details: {details_path}",
            f"Run history: {HISTORY_PATH}",
            "Gold questions: " + str(CASES_PATH),
        ]
    )
    ANALYSIS_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--mode",
        choices=("semantic", "lexical", "hybrid", "rerank"),
        default="hybrid",
    )
    parser.add_argument("-c", "--chunk-size", type=int, default=10)
    parser.add_argument("--semantic-candidates", type=int, default=30)
    parser.add_argument("--lexical-candidates", type=int, default=30)
    parser.add_argument("--semantic-weight", type=float, default=1.0)
    parser.add_argument("--lexical-weight", type=float, default=1.0)
    parser.add_argument("--candidate-limit", type=int, default=30)
    parser.add_argument("--retrieval-rank-weight", type=float, default=0.5)
    parser.add_argument("--reranker-rank-weight", type=float, default=0.5)
    parser.add_argument("--rerank-rrf-k", type=int, default=60)
    parser.add_argument("--batch-size", type=int, default=64)
    parser.add_argument("--case-limit", type=int, default=None)
    args = parser.parse_args()

    positive_counts = (
        args.chunk_size,
        args.semantic_candidates,
        args.lexical_candidates,
        args.candidate_limit,
        args.rerank_rrf_k,
        args.batch_size,
    )
    if min(positive_counts) < 1:
        parser.error("chunk and candidate counts must be positive")
    if args.semantic_weight < 0 or args.lexical_weight < 0:
        parser.error("retrieval weights cannot be negative")
    if args.mode in {"hybrid", "rerank"} and args.semantic_weight + args.lexical_weight == 0:
        parser.error("hybrid retrieval needs at least one positive weight")
    if args.mode == "rerank" and args.chunk_size > args.candidate_limit:
        parser.error("reranked --chunk-size cannot exceed --candidate-limit")
    if args.retrieval_rank_weight < 0 or args.reranker_rank_weight < 0:
        parser.error("reranking rank-fusion weights cannot be negative")
    if args.retrieval_rank_weight + args.reranker_rank_weight == 0:
        parser.error("at least one reranking rank-fusion weight must be positive")

    cases = _load_cases(args.case_limit)
    timestamp, filename_timestamp = _timestamp()
    started = time.perf_counter()
    results: list[dict[str, Any]] = []

    for case_number, case in enumerate(cases, start=1):
        variables = case.get("vars", {})
        question = str(variables.get("question", "")).strip()
        if args.mode == "rerank":
            from .rerank import rerank

            contexts = rerank(
                question,
                semantic_limit=args.semantic_candidates,
                lexical_limit=args.lexical_candidates,
                candidate_limit=args.candidate_limit,
                final_limit=args.chunk_size,
                semantic_weight=args.semantic_weight,
                lexical_weight=args.lexical_weight,
                retrieval_rank_weight=args.retrieval_rank_weight,
                reranker_rank_weight=args.reranker_rank_weight,
                rerank_rrf_k=args.rerank_rrf_k,
                batch_size=args.batch_size,
            )
        else:
            contexts = retrieve(
                question,
                mode=args.mode,
                semantic_limit=args.semantic_candidates,
                lexical_limit=args.lexical_candidates,
                final_limit=args.chunk_size,
                semantic_weight=args.semantic_weight,
                lexical_weight=args.lexical_weight,
            )
        scored = score_retrieval(
            {"question": question, "contexts": contexts}, {"vars": variables}
        )
        result = {
            "case_number": case_number,
            "description": case.get("description", f"Case {case_number}"),
            "question": question,
            "metadata": case.get("metadata", {}),
            "passed": scored["passed"],
            "metrics": scored["metrics"],
            "metric_passes": scored["metric_passes"],
            "first_relevant_rank": scored["first_relevant_rank"],
            "gold_sources": _gold_details(variables.get("gold_sources", []), scored),
            "retrieved_chunks": _retrieved_details(contexts, scored),
        }
        results.append(result)
        status = "PASS" if result["passed"] else "FAIL"
        print(
            f"[{case_number:03d}/{len(cases):03d}] {status}  {result['description']}",
            flush=True,
        )

    passed = sum(result["passed"] for result in results)
    rerank_model = None
    rerank_provider = None
    if args.mode == "rerank":
        from .rerank import RERANK_MODEL, active_provider

        rerank_model = RERANK_MODEL
        rerank_provider = active_provider()

    summary = {
        "timestamp_utc": timestamp,
        "mode": args.mode,
        "chunk_size": args.chunk_size,
        "questions": len(results),
        "gold_sources": sum(len(result["gold_sources"]) for result in results),
        "passed": passed,
        "failed": len(results) - passed,
        "pass_rate": passed / len(results) if results else 0.0,
        "averages": _averages(results),
        "configuration": {
            "semantic_candidates": args.semantic_candidates,
            "lexical_candidates": args.lexical_candidates,
            "semantic_weight": args.semantic_weight,
            "lexical_weight": args.lexical_weight,
            "rrf_k": 60,
            "candidate_limit": args.candidate_limit,
            "retrieval_rank_weight": args.retrieval_rank_weight,
            "reranker_rank_weight": args.reranker_rank_weight,
            "rerank_rrf_k": args.rerank_rrf_k,
            "batch_size": args.batch_size,
            "rerank_model": rerank_model,
            "rerank_provider": rerank_provider,
        },
        "elapsed_seconds": round(time.perf_counter() - started, 3),
        "results": results,
    }

    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    details_path = RESULTS_DIR / (
        f"retrieval_details_{args.mode}_k{args.chunk_size}_{filename_timestamp}.json"
    )
    details_path.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    history_entry = {key: value for key, value in summary.items() if key != "results"}
    with HISTORY_PATH.open("a", encoding="utf-8") as history_file:
        history_file.write(json.dumps(history_entry, separators=(",", ":")) + "\n")
    _write_analysis(summary, details_path)

    print(
        f"\n{args.mode.title()} retrieval: {passed}/{len(results)} passed "
        f"({summary['pass_rate']:.1%})"
    )
    print(f"Analytics: {ANALYSIS_PATH}")
    print(f"Question details: {details_path}")


if __name__ == "__main__":
    main()
