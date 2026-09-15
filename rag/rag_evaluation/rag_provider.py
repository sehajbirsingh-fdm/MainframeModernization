"""Promptfoo providers that exercise the local RAG without the frontend or API."""

from __future__ import annotations

import os
import sys
from functools import lru_cache
from pathlib import Path
from typing import Any

import lancedb

os.environ.setdefault("HF_HUB_OFFLINE", "1")
os.environ.setdefault("TRANSFORMERS_OFFLINE", "1")

from sentence_transformers import SentenceTransformer

PROJECT_ROOT = Path(__file__).resolve().parents[2]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

try:
    from rag.embedding_model import MODEL_NAME, MODEL_REVISION
    from rag.rag_evaluation.answer_context_experiment import (
        MAX_CONTEXT_CHARACTERS,
        build_answer_context,
        generate_answer,
        unique_file_summaries,
    )
    from rag.sync_embedding_table import DATABASE_PATH, TABLE_NAME
except ModuleNotFoundError:
    from answer_context_experiment import (
        MAX_CONTEXT_CHARACTERS,
        build_answer_context,
        generate_answer,
        unique_file_summaries,
    )
    from embedding_model import MODEL_NAME, MODEL_REVISION
    from sync_embedding_table import DATABASE_PATH, TABLE_NAME


TOP_K = 20
QUERY_PREFIX = "Represent this query for searching relevant code: "
GROQ_MODEL = "openai/gpt-oss-120b"


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
    return lancedb.connect(DATABASE_PATH).open_table(TABLE_NAME)


def retrieve(question: str, top_k: int = TOP_K) -> list[dict[str, Any]]:
    """Return deterministic top-k metadata and source text from LanceDB."""
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
        .select(
            [
                "chunk_id",
                "file_id",
                "filename",
                "start_line",
                "end_line",
                "file_summary",
                "node_summary_texts",
                "chunk_text",
                "_distance",
            ]
        )
        .limit(top_k)
        .to_list()
    )

    contexts: list[dict[str, Any]] = []
    for rank, row in enumerate(rows, start=1):
        contexts.append(
            {
                "rank": rank,
                "chunk_id": row["chunk_id"],
                "file_id": row["file_id"],
                "filename": row["filename"],
                "start_line": row["start_line"],
                "end_line": row["end_line"],
                "cosine_similarity": round(1.0 - row.pop("_distance"), 6),
                "file_summary": row["file_summary"],
                "node_summary_texts": row["node_summary_texts"],
                "text": row["chunk_text"],
                "chunk_text": row["chunk_text"],
            }
        )
    return contexts


def _question(prompt: str, context: dict[str, Any]) -> str:
    value = context.get("vars", {}).get("question", prompt)
    question = str(value).strip()
    if not question:
        raise ValueError("Evaluation question cannot be empty")
    return question


def retrieve_only(prompt: str, options: dict, context: dict) -> dict:
    """Promptfoo provider for retrieval-only evaluation; it makes no LLM call."""
    question = _question(prompt, context)
    top_k = int(
        os.getenv("RAG_EVAL_CHUNK_SIZE", options.get("config", {}).get("top_k", TOP_K))
    )
    contexts = retrieve(question, top_k)
    return {
        "output": {
            "question": question,
            "answer": None,
            "contexts": contexts,
        },
        "metadata": {"top_k": top_k, "retrieved_count": len(contexts)},
    }


def answer_with_context(prompt: str, options: dict, context: dict) -> dict:
    """Generate an answer using the exact top-20 context-packing experiment."""
    question = _question(prompt, context)
    config = options.get("config", {})
    top_k = int(os.getenv("RAG_EVAL_CHUNK_SIZE", config.get("top_k", TOP_K)))
    contexts = retrieve(question, top_k)
    summaries = unique_file_summaries(contexts)
    context_budget = int(config.get("max_context_characters", MAX_CONTEXT_CHARACTERS))
    llm_context, context_stats = build_answer_context(
        contexts,
        summaries,
        max_context_characters=context_budget,
    )
    answer_model = str(config.get("answer_model", GROQ_MODEL))
    answer, token_usage = generate_answer(question, llm_context, answer_model)
    return {
        "output": {
            "question": question,
            "answer": answer,
            "llm_context": llm_context,
            "contexts": contexts,
            "unique_file_summaries": summaries,
            "context_stats": context_stats,
            "token_usage": token_usage,
            "answer_model": answer_model,
        },
        "metadata": {
            "top_k": top_k,
            "retrieved_count": len(contexts),
            "unique_file_count": len(summaries),
            **context_stats,
        },
    }
