"""Test Groq answers with unique file summaries and top-20 raw code chunks."""

from __future__ import annotations

import argparse
import json
import os
from datetime import datetime, timezone
from pathlib import Path

import lancedb
from dotenv import load_dotenv
from groq import Groq

os.environ.setdefault("HF_HUB_OFFLINE", "1")
os.environ.setdefault("TRANSFORMERS_OFFLINE", "1")

from sentence_transformers import SentenceTransformer

from rag.embedding_model import MODEL_NAME, MODEL_REVISION
from rag.rag_evaluation.answer_evaluation import RAG_SYSTEM_PROMPT
from rag.search_code import GROQ_MODEL, PROJECT_CONTEXT, QUERY_PREFIX
from rag.sync_embedding_table import DATABASE_PATH, TABLE_NAME


TOP_K = 20
MAX_CONTEXT_CHARACTERS = 26_000
RESULTS_DIR = Path(__file__).resolve().parent / "evaluation-results"


def _search(query: str, device: str) -> list[dict]:
    model = SentenceTransformer(
        MODEL_NAME,
        revision=MODEL_REVISION,
        trust_remote_code=True,
        device=device,
        local_files_only=True,
    )
    query_vector = model.encode(
        QUERY_PREFIX + query,
        normalize_embeddings=True,
        convert_to_numpy=True,
    ).astype("float32")

    table = lancedb.connect(DATABASE_PATH).open_table(TABLE_NAME)
    results = (
        table.search(query_vector.tolist(), vector_column_name="embedding")
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
                "chunk_text",
                "_distance",
            ]
        )
        .limit(TOP_K)
        .to_list()
    )

    for rank, result in enumerate(results, start=1):
        result["rank"] = rank
        result["cosine_similarity"] = round(1.0 - result.pop("_distance"), 6)
    return results


def unique_file_summaries(results: list[dict]) -> list[dict[str, str]]:
    summaries: list[dict[str, str]] = []
    seen: set[str] = set()
    for result in results:
        file_id = result["file_id"]
        if file_id in seen:
            continue
        seen.add(file_id)
        summaries.append(
            {
                "file_id": file_id,
                "filename": result["filename"],
                "file_summary": result["file_summary"],
            }
        )
    return summaries


def _truncate_code(text: str, limit: int) -> tuple[str, bool]:
    if len(text) <= limit:
        return text, False
    marker = "\n... [middle of chunk omitted to fit the LLM context budget] ...\n"
    available = max(limit - len(marker), 120)
    head_size = int(available * 0.72)
    return text[:head_size] + marker + text[-(available - head_size) :], True


def build_answer_context(
    results: list[dict],
    file_summaries: list[dict],
    max_context_characters: int = MAX_CONTEXT_CHARACTERS,
) -> tuple[str, dict[str, int]]:
    summary_blocks = [
        "\n".join(
            [
                f"[File summary {index}]",
                f"File: {item['file_id']}",
                f"Summary: {item['file_summary']}",
            ]
        )
        for index, item in enumerate(file_summaries, start=1)
    ]

    summary_section = "FILES\n" + "\n\n".join(summary_blocks)
    chunk_headers = [
        f"[Chunk {result['rank']}: {result['file_id']} "
        f"lines {result['start_line']}-{result['end_line']}]\n"
        for result in results
    ]
    fixed_size = len(summary_section) + len("\n\nCODE CHUNKS\n") + sum(
        len(header) + 2 for header in chunk_headers
    )
    available_code_chars = max(max_context_characters - fixed_size, 10_000)

    # Preserve all 20 results while giving higher-ranked chunks more room.
    weights = [1.4 if index < 5 else 1.1 if index < 10 else 0.8 for index in range(len(results))]
    weight_total = sum(weights)
    chunk_blocks: list[str] = []
    sent_code_chars = 0
    truncated_chunks = 0
    for result, header, weight in zip(results, chunk_headers, weights):
        allocation = max(400, int(available_code_chars * weight / weight_total))
        code, truncated = _truncate_code(result["chunk_text"], allocation)
        sent_code_chars += len(code)
        truncated_chunks += int(truncated)
        chunk_blocks.append(header + code)

    context = summary_section + "\n\nCODE CHUNKS\n" + "\n\n".join(chunk_blocks)
    stats = {
        "context_characters": len(context),
        "original_chunk_characters": sum(len(result["chunk_text"]) for result in results),
        "sent_chunk_characters": sent_code_chars,
        "truncated_chunk_count": truncated_chunks,
        "context_character_limit": max_context_characters,
    }
    return context, stats


def generate_answer(
    query: str,
    context: str,
    model_name: str = GROQ_MODEL,
) -> tuple[str, dict[str, int]]:
    load_dotenv(Path(__file__).resolve().parents[1] / ".env")
    api_key = os.getenv("GROQ_API_KEY")
    if not api_key:
        raise SystemExit("GROQ_API_KEY is missing. Add it to rag/.env and try again.")

    completion = Groq(api_key=api_key, max_retries=4).chat.completions.create(
        model=model_name,
        messages=[
            {
                "role": "system",
                "content": (
                    f"{RAG_SYSTEM_PROMPT}\n\n"
                    f"Project description:\n{PROJECT_CONTEXT}"
                ),
            },
            {
                "role": "user",
                "content": (
                    f"Question:\n{query}\n\n"
                    "Use the unique file summaries for orientation and the raw code "
                    "chunks as the primary evidence. Do not repeat a file summary for "
                    "every chunk.\n\n"
                    f"Retrieved context:\n{context}"
                ),
            },
        ],
        temperature=0.2,
        max_completion_tokens=1800,
    )
    answer = completion.choices[0].message.content
    if not answer:
        raise RuntimeError("Groq returned an empty answer.")

    usage = completion.usage
    token_usage = {
        "prompt_tokens": getattr(usage, "prompt_tokens", 0),
        "completion_tokens": getattr(usage, "completion_tokens", 0),
        "total_tokens": getattr(usage, "total_tokens", 0),
    }
    return answer.strip(), token_usage


def _output_path() -> Path:
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")
    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    return RESULTS_DIR / f"answer_context_top20_{timestamp}.json"


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Answer one query using top-20 semantic chunks and unique file summaries."
    )
    parser.add_argument("--query", help="Question to evaluate; prompts when omitted.")
    parser.add_argument(
        "--device",
        default=os.getenv("RAG_EMBEDDING_DEVICE", "mps"),
        help="Sentence Transformers device (default: RAG_EMBEDDING_DEVICE or mps).",
    )
    args = parser.parse_args()

    query = (args.query or input("Enter your codebase question: ")).strip()
    if not query:
        raise SystemExit("Query cannot be empty.")

    results = _search(query, args.device)
    file_summaries = unique_file_summaries(results)
    context, context_stats = build_answer_context(results, file_summaries)

    print(
        f"Retrieved {len(results)} raw chunks from "
        f"{len(file_summaries)} unique files."
    )
    print(
        f"LLM context: {context_stats['context_characters']} characters; "
        f"{context_stats['truncated_chunk_count']} chunk bodies compacted."
    )
    for result in results:
        print(
            f"{result['rank']:>2}. {result['cosine_similarity']:.4f}  "
            f"{result['file_id']}:{result['start_line']}-{result['end_line']}"
        )

    print(f"\nGenerating final answer with {GROQ_MODEL}...")
    answer, token_usage = generate_answer(query, context)
    output_path = _output_path()
    payload = {
        "timestamp_utc": datetime.now(timezone.utc).isoformat(),
        "query": query,
        "retrieval": {
            "method": "CodeRankEmbed semantic cosine search",
            "top_k": TOP_K,
            "unique_file_count": len(file_summaries),
            **context_stats,
        },
        "answer_model": GROQ_MODEL,
        "token_usage": token_usage,
        "unique_file_summaries": file_summaries,
        "llm_context": context,
        "results": results,
        "answer": answer,
    }
    output_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    print(f"\nFinal answer:\n\n{answer}")
    print(f"\nExperiment written to {output_path}")


if __name__ == "__main__":
    main()
