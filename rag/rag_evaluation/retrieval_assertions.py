"""Deterministic Promptfoo assertions for code-chunk retrieval quality."""

from __future__ import annotations

import json
import math
from typing import Any


def _payload(output: Any) -> dict[str, Any]:
    if isinstance(output, dict):
        return output
    if isinstance(output, str):
        parsed = json.loads(output)
        if isinstance(parsed, dict):
            return parsed
    raise ValueError("Expected the RAG provider to return a structured object")


def _gold(context: dict[str, Any]) -> list[dict[str, Any]]:
    value = context.get("vars", {}).get("gold_sources", [])
    if not isinstance(value, list):
        raise ValueError("gold_sources must be a list")
    return [item for item in value if isinstance(item, dict)]


def _overlaps(retrieved: dict[str, Any], expected: dict[str, Any]) -> bool:
    if retrieved.get("file_id") != expected.get("file_id"):
        return False

    expected_start = expected.get("start_line")
    expected_end = expected.get("end_line")
    if expected_start is not None and expected_end is not None:
        if retrieved.get("end_line", 0) < expected_start:
            return False
        if retrieved.get("start_line", 0) > expected_end:
            return False

    text = str(retrieved.get("text", "")).lower()
    terms = expected.get("must_contain", [])
    return all(str(term).lower() in text for term in terms)


def _matches(output: Any, context: dict[str, Any]) -> tuple[list[dict], list[dict], set[int]]:
    payload = _payload(output)
    retrieved = payload.get("contexts", [])
    gold = _gold(context)
    matched_gold = {
        gold_index
        for gold_index, expected in enumerate(gold)
        if any(_overlaps(item, expected) for item in retrieved)
    }
    return retrieved, gold, matched_gold


def _result(score: float, threshold: float, reason: str) -> dict[str, Any]:
    return {"pass": score >= threshold, "score": score, "reason": reason}


def score_retrieval(
    output: Any,
    context: dict[str, Any],
    thresholds: dict[str, float] | None = None,
) -> dict[str, Any]:
    """Score one ranked result set and retain its gold/chunk match matrix."""
    payload = _payload(output)
    retrieved = payload.get("contexts", [])
    gold = _gold(context)
    minimum_rank_score = 1.0 / len(retrieved) if retrieved else 1.0
    configured = {
        "hit_at_k": 1.0,
        "precision_at_k": minimum_rank_score,
        "recall_at_k": 0.5,
        "mrr": minimum_rank_score,
        "ndcg_at_k": 0.2,
    }
    if thresholds:
        configured.update(thresholds)

    retrieved_matches = [
        [index for index, expected in enumerate(gold) if _overlaps(item, expected)]
        for item in retrieved
    ]
    matched_gold = {index for indexes in retrieved_matches for index in indexes}
    relevant_retrieved = sum(bool(indexes) for indexes in retrieved_matches)

    if not gold:
        metrics = {
            "hit_at_k": 1.0 if not retrieved else 0.0,
            "precision_at_k": 1.0 if not retrieved else 0.0,
            "recall_at_k": 1.0,
            "mrr": 1.0,
            "ndcg_at_k": 1.0,
        }
        first_relevant_rank = None
    else:
        first_relevant_rank = next(
            (rank for rank, indexes in enumerate(retrieved_matches, start=1) if indexes),
            None,
        )
        metrics = {
            "hit_at_k": 1.0 if matched_gold else 0.0,
            "precision_at_k": relevant_retrieved / len(retrieved) if retrieved else 0.0,
            "recall_at_k": len(matched_gold) / len(gold),
            "mrr": 0.0 if first_relevant_rank is None else 1.0 / first_relevant_rank,
            "ndcg_at_k": _ndcg(retrieved, gold),
        }

    metric_passes = {
        name: metrics[name] >= threshold for name, threshold in configured.items()
    }
    return {
        "passed": all(metric_passes.values()),
        "metrics": metrics,
        "thresholds": configured,
        "metric_passes": metric_passes,
        "matched_gold_indices": sorted(matched_gold),
        "retrieved_matches": retrieved_matches,
        "first_relevant_rank": first_relevant_rank,
    }


def _ndcg(retrieved: list[dict], gold: list[dict]) -> float:
    remaining = set(range(len(gold)))
    gains: list[float] = []
    for item in retrieved:
        candidates = [index for index in remaining if _overlaps(item, gold[index])]
        if not candidates:
            gains.append(0.0)
            continue
        best = max(candidates, key=lambda index: float(gold[index].get("relevance", 1)))
        remaining.remove(best)
        gains.append(float(gold[best].get("relevance", 1)))

    dcg = sum((2**gain - 1) / math.log2(rank + 1) for rank, gain in enumerate(gains, start=1))
    ideal = sorted((float(item.get("relevance", 1)) for item in gold), reverse=True)
    idcg = sum((2**gain - 1) / math.log2(rank + 1) for rank, gain in enumerate(ideal, start=1))
    return dcg / idcg if idcg else 1.0


def hit_at_k(output: Any, context: dict[str, Any]) -> dict[str, Any]:
    _, gold, matched = _matches(output, context)
    if not gold:
        return _result(1.0, 1.0, "No gold source is expected for this unanswerable case")
    score = 1.0 if matched else 0.0
    return _result(score, 1.0, f"Matched {len(matched)} of {len(gold)} gold sources")


def recall_at_k(output: Any, context: dict[str, Any]) -> dict[str, Any]:
    _, gold, matched = _matches(output, context)
    if not gold:
        return _result(1.0, 1.0, "Retrieval recall is not applicable to this case")
    score = len(matched) / len(gold)
    threshold = float(context.get("config", {}).get("threshold", 0.5))
    return _result(score, threshold, f"Retrieved {len(matched)} of {len(gold)} gold sources")


def precision_at_k(output: Any, context: dict[str, Any]) -> dict[str, Any]:
    retrieved, gold, _ = _matches(output, context)
    if not gold:
        score = 1.0 if not retrieved else 0.0
        return _result(score, 1.0, "Expected no retrieved chunks for this unanswerable case")
    relevant = sum(
        any(_overlaps(item, expected) for expected in gold) for item in retrieved
    )
    score = relevant / len(retrieved) if retrieved else 0.0
    default_threshold = 1.0 / len(retrieved) if retrieved else 1.0
    threshold = float(context.get("config", {}).get("threshold", default_threshold))
    return _result(
        score,
        threshold,
        f"{relevant} of {len(retrieved)} retrieved chunks match a labeled gold source",
    )


def reciprocal_rank(output: Any, context: dict[str, Any]) -> dict[str, Any]:
    payload = _payload(output)
    retrieved = payload.get("contexts", [])
    gold = _gold(context)
    if not gold:
        return _result(1.0, 1.0, "Reciprocal rank is not applicable to this case")
    rank = next(
        (
            index
            for index, item in enumerate(retrieved, start=1)
            if any(_overlaps(item, expected) for expected in gold)
        ),
        None,
    )
    score = 0.0 if rank is None else 1.0 / rank
    default_threshold = 1.0 / len(retrieved) if retrieved else 1.0
    threshold = float(context.get("config", {}).get("threshold", default_threshold))
    return _result(score, threshold, f"First relevant result rank: {rank or 'not retrieved'}")


def ndcg_at_k(output: Any, context: dict[str, Any]) -> dict[str, Any]:
    payload = _payload(output)
    retrieved = payload.get("contexts", [])
    gold = _gold(context)
    if not gold:
        return _result(1.0, 1.0, "nDCG is not applicable to this case")

    score = _ndcg(retrieved, gold)
    threshold = float(context.get("config", {}).get("threshold", 0.2))
    return _result(score, threshold, f"nDCG@{len(retrieved)}={score:.3f}")
