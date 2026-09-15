# RAG Evaluation

This folder evaluates the existing CodeRankEmbed and LanceDB pipeline directly. It
does not start or modify the React frontend, FastAPI chatbot endpoint, ingestion, or
embedding jobs.

## Dataset

`eval_cases.yaml` contains 100 manually grounded questions across account inquiry,
customer inquiry, customer-account relationships, customer creation and update,
transactions, statements, routing/configuration, and z/OS contracts. Gold retrieval
targets use repository-relative `file_id` values plus optional `must_contain` terms,
so the test verifies the relevant chunk rather than merely any chunk from the file.

The questions range from specific functions and validation rules to conceptual,
end-to-end workflows. Each question was written from inspected production code first;
the expected files and code markers were recorded before phrasing the user question.

## Run retrieval only

This is the current evaluation phase. It is local and makes no Groq calls. `-c` means
the number of chunks retrieved per question; it does not change ingestion chunk size:

```bash
cd /Users/temp/Documents/pod/MainframeModernization
RAG_EMBEDDING_DEVICE=mps \
  rag/.venv/bin/python rag/rag_evaluation/run_retrieval_evaluation.py -c 10
```

For a top-50 comparison, use `-c 50`. `--chunk-size` and the older `--top-k` name are
aliases for the same parameter.

The runner writes:

- `evaluation-results/analysis.txt`: latest overall and per-feature analytics.
- `evaluation-results/retrieval_details_chunk_size_<c>_<timestamp>.json`: every
  question, ranked chunk, gold match, metric, and pass/fail result.
- `evaluation-results/retrieval_history.jsonl`: one append-only summary per run, used
  to retain the first-run timestamp and compare later runs.

## Promptfoo's role

Promptfoo is the optional evaluation test harness. Our Python provider still performs
CodeRankEmbed and LanceDB retrieval, and our Python assertions still calculate the
metrics. Promptfoo reads the cases, invokes those functions, displays assertion-level
pass/fail results in the terminal, stores its own runs, and provides a web viewer.

Run the same top-10 dataset through Promptfoo with:

```bash
cd /Users/temp/Documents/pod/MainframeModernization
PROMPTFOO_PYTHON="$PWD/rag/.venv/bin/python" \
  RAG_EVAL_CHUNK_SIZE=10 \
  npm --prefix rag/rag_evaluation run eval:retrieval
```

Change `RAG_EVAL_CHUNK_SIZE` to compare another retrieval depth. Promptfoo reports
Hit@K, labeled Precision@K, Recall@K, MRR, and nDCG@K. Mean Hit@K
is the retrieval-accuracy figure; query pass rate requires every configured metric
threshold to pass. Precision is explicitly labeled because useful chunks outside the
manually curated gold set are otherwise counted as false positives.

The minimum passing Precision@K and MRR scale as `1/K`, meaning at least one labeled
chunk must appear within the requested depth. Recall remains at 0.5 and nDCG at 0.2,
so increasing K does not create an impossible fixed-precision requirement.

## Final-answer evaluation

The answer suite runs the same 100 grounded cases through the top-20 answer strategy:

1. CodeRankEmbed retrieves 20 semantic chunks from LanceDB.
2. Each unique file summary is added once.
3. Raw chunk code is packed into a 26,000-character context budget.
4. `openai/gpt-oss-120b` generates the answer.
5. `gemini-3.5-flash-lite` independently grades the answer in one structured call.

The Gemini judge reports required-fact coverage, faithfulness, factual correctness,
answer relevance, context support, source accuracy, code-snippet fidelity, forbidden
claim compliance, and abstention quality. It also classifies failures as retrieval,
generation, grounding, citation, or snippet gaps.

For a five-case smoke test:

```bash
cd /Users/temp/Documents/pod/MainframeModernization
set -a; source rag/.env; set +a
PROMPTFOO_PYTHON="$PWD/rag/.venv/bin/python" \
RAG_EMBEDDING_DEVICE=mps \
RAG_EVAL_CHUNK_SIZE=20 \
RAG_EVAL_JUDGE_MODEL=gemini-3.5-flash-lite \
  npm --prefix rag/rag_evaluation run eval:answer -- \
  --filter-first-n 5 --no-cache
```

For the complete timestamped 100-case evaluation and concise analysis:

```bash
cd /Users/temp/Documents/pod/MainframeModernization
set -a; source rag/.env; set +a
STAMP=$(date -u +%Y%m%d_%H%M%S)
RESULT="$PWD/rag/rag_evaluation/evaluation-results/promptfoo_answer_gemini_3_5_flash_lite_top20_${STAMP}.json"

PROMPTFOO_PYTHON="$PWD/rag/.venv/bin/python" \
RAG_EMBEDDING_DEVICE=mps \
RAG_EVAL_CHUNK_SIZE=20 \
RAG_EVAL_JUDGE_MODEL=gemini-3.5-flash-lite \
  npm --prefix rag/rag_evaluation run eval:answer -- \
  --no-cache --output "$RESULT"

rag/.venv/bin/python -m rag.rag_evaluation.answer_evaluation "$RESULT"
```

Promptfoo writes every question, exact model context, generated answer, Gemini scores,
reasons, retrieved chunks, latency, and token usage to the timestamped JSON. The
report command writes the latest concise summary to `evaluation-results/answer_analysis.txt`
and appends one run record to `answer_evaluation_history.jsonl`.

## Review results

```bash
npm --prefix rag/rag_evaluation run view
```

The provider loads the embedding model once in Promptfoo's persistent Python worker,
then queries the current `rag/vector_store/code_chunks` LanceDB table for each case.
The standalone Python runner creates `analysis.txt`; Promptfoo does not write that
custom report.
