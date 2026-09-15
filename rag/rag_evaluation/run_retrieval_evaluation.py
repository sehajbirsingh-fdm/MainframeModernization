"""Run the manual retrieval evaluation and write human-readable analytics."""

from __future__ import annotations

import argparse
import json
import time
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path
from statistics import mean
from typing import Any

import yaml

try:
    from .rag_provider import retrieve
    from .retrieval_assertions import score_retrieval
except ImportError:
    from rag_provider import retrieve
    from retrieval_assertions import score_retrieval

ROOT = Path(__file__).resolve().parent
CASES_PATH = ROOT / "eval_cases.yaml"
RESULTS_DIR = ROOT / "evaluation-results"
ANALYSIS_PATH = RESULTS_DIR / "analysis.txt"
HISTORY_PATH = RESULTS_DIR / "retrieval_history.jsonl"


def _timestamp() -> tuple[datetime, str, str]:
    value = datetime.now(timezone.utc)
    return value, value.isoformat(timespec="seconds"), value.strftime("%Y%m%d_%H%M%S")


def _load_cases(limit: int | None) -> list[dict[str, Any]]:
    cases = yaml.safe_load(CASES_PATH.read_text(encoding="utf-8"))
    if not isinstance(cases, list):
        raise ValueError(f"Expected a list of cases in {CASES_PATH}")
    return cases[:limit] if limit else cases


def _gold_details(
    gold: list[dict[str, Any]], score: dict[str, Any], contexts: list[dict[str, Any]]
) -> list[dict[str, Any]]:
    details: list[dict[str, Any]] = []
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
    details: list[dict[str, Any]] = []
    for item, matches in zip(contexts, score["retrieved_matches"]):
        details.append(
            {
                "rank": item["rank"],
                "chunk_id": item["chunk_id"],
                "file_id": item["file_id"],
                "start_line": item["start_line"],
                "end_line": item["end_line"],
                "cosine_similarity": item["cosine_similarity"],
                "is_labeled_relevant": bool(matches),
                "matched_gold_indices": matches,
            }
        )
    return details


def _averages(results: list[dict[str, Any]]) -> dict[str, float]:
    names = ("hit_at_k", "precision_at_k", "recall_at_k", "mrr", "ndcg_at_k")
    return {
        name: mean(item["metrics"][name] for item in results) if results else 0.0
        for name in names
    }


def _feature_rows(results: list[dict[str, Any]]) -> list[dict[str, Any]]:
    groups: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for result in results:
        groups[result["metadata"].get("feature", "unknown")].append(result)

    rows = []
    for feature, items in sorted(groups.items()):
        passed = sum(item["passed"] for item in items)
        rows.append(
            {
                "feature": feature,
                "cases": len(items),
                "passed": passed,
                "failed": len(items) - passed,
                "pass_rate": passed / len(items),
                **_averages(items),
            }
        )
    return rows


def _history() -> list[dict[str, Any]]:
    if not HISTORY_PATH.exists():
        return []
    entries = []
    for line in HISTORY_PATH.read_text(encoding="utf-8").splitlines():
        if line.strip():
            entries.append(json.loads(line))
    return entries


def _write_analysis(summary: dict[str, Any], details_path: Path) -> None:
    history = _history()
    first_run = history[0]["timestamp_utc"] if history else summary["timestamp_utc"]
    lines = [
        "Bank of Z RAG Retrieval Evaluation",
        "==================================",
        f"First run (UTC): {first_run}",
        f"Latest run (UTC): {summary['timestamp_utc']}",
        f"Completed runs: {len(history)}",
        "",
        "Evaluation scope",
        "----------------",
        f"Questions: {summary['questions']}",
        f"Labeled gold sources: {summary['gold_sources']}",
        f"Chunk retrieval size: {summary['chunk_size']}",
        "Matching rule: same file_id plus optional line overlap and every must_contain marker",
        "",
        "Overall results",
        "---------------",
        f"Passed questions: {summary['passed']}",
        f"Failed questions: {summary['failed']}",
        f"Query pass rate: {summary['pass_rate']:.2%}",
        f"Retrieval accuracy (mean Hit@{summary['chunk_size']}): {summary['averages']['hit_at_k']:.4f}",
        f"Labeled Precision@{summary['chunk_size']}: {summary['averages']['precision_at_k']:.4f}",
        f"Recall@{summary['chunk_size']}: {summary['averages']['recall_at_k']:.4f}",
        f"MRR: {summary['averages']['mrr']:.4f}",
        f"nDCG@{summary['chunk_size']}: {summary['averages']['ndcg_at_k']:.4f}",
        f"Pass gate: Hit=1, Precision>=1/{summary['chunk_size']}, Recall>=0.5, "
        f"MRR>=1/{summary['chunk_size']}, nDCG>=0.2",
        "",
        "Failed question IDs",
        "-------------------",
    ]
    failed = [item for item in summary["results"] if not item["passed"]]
    lines.append(", ".join(f"{item['case_number']:02d}" for item in failed) if failed else "None")

    lines.extend(["", "Recent run history", "------------------", "Timestamp (UTC)              C  Cases  Pass%  Hit@K  Prec@K  Recall@K"])
    for run in history[-10:]:
        averages = run["averages"]
        chunk_size = run.get("chunk_size", run.get("top_k", 10))
        lines.append(
            f"{run['timestamp_utc']:<28} {chunk_size:>2} {run['questions']:>5} {run['pass_rate']:>6.1%} "
            f"{averages['hit_at_k']:>6.3f} {averages['precision_at_k']:>7.3f} "
            f"{averages['recall_at_k']:>8.3f}"
        )

    lines.extend(
        [
            "",
            "Artifacts",
            "---------",
            f"Question-level details: {details_path}",
            f"Append-only run history: {HISTORY_PATH}",
            "",
            "Note: labeled precision only counts chunks matching the curated gold sources.",
        ]
    )
    ANALYSIS_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "-c",
        "--chunk-size",
        "--top-k",
        dest="chunk_size",
        type=int,
        default=10,
        help="Number of chunks retrieved for each question (default: 10)",
    )
    parser.add_argument("--limit", type=int, default=None)
    args = parser.parse_args()
    if args.chunk_size < 1:
        parser.error("--chunk-size must be at least 1")

    cases = _load_cases(args.limit)
    _, timestamp, filename_timestamp = _timestamp()
    started = time.perf_counter()
    results: list[dict[str, Any]] = []

    for case_number, case in enumerate(cases, start=1):
        variables = case.get("vars", {})
        question = str(variables.get("question", "")).strip()
        contexts = retrieve(question, args.chunk_size)
        output = {"question": question, "contexts": contexts}
        scored = score_retrieval(output, {"vars": variables})
        gold = variables.get("gold_sources", [])
        result = {
            "case_number": case_number,
            "description": case.get("description", f"Case {case_number}"),
            "question": question,
            "metadata": case.get("metadata", {}),
            "passed": scored["passed"],
            "metrics": scored["metrics"],
            "metric_passes": scored["metric_passes"],
            "first_relevant_rank": scored["first_relevant_rank"],
            "gold_sources": _gold_details(gold, scored, contexts),
            "retrieved_chunks": _retrieved_details(contexts, scored),
        }
        results.append(result)
        status = "PASS" if result["passed"] else "FAIL"
        print(f"[{case_number:02d}/{len(cases):02d}] {status}  {result['description']}")

    passed = sum(item["passed"] for item in results)
    summary = {
        "timestamp_utc": timestamp,
        "chunk_size": args.chunk_size,
        "questions": len(results),
        "gold_sources": sum(len(item["gold_sources"]) for item in results),
        "passed": passed,
        "failed": len(results) - passed,
        "pass_rate": passed / len(results) if results else 0.0,
        "averages": _averages(results),
        "features": _feature_rows(results),
        "elapsed_seconds": round(time.perf_counter() - started, 3),
        "results": results,
    }

    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    details_path = RESULTS_DIR / (
        f"retrieval_details_chunk_size_{args.chunk_size}_{filename_timestamp}.json"
    )
    details_path.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")

    history_entry = {key: value for key, value in summary.items() if key != "results"}
    with HISTORY_PATH.open("a", encoding="utf-8") as history_file:
        history_file.write(json.dumps(history_entry, separators=(",", ":")) + "\n")
    _write_analysis(summary, details_path)

    print(
        f"\nRetrieval evaluation: {passed}/{len(results)} passed "
        f"({summary['pass_rate']:.1%})"
    )
    print(f"Analytics: {ANALYSIS_PATH}")
    print(f"Question details: {details_path}")


if __name__ == "__main__":
    main()
