# Lexical and Hybrid Retrieval Experiment

This folder is an isolated A/B test for adding exact-term retrieval to the existing
CodeRankEmbed search. It does not modify the chatbot, `rag/search_code.py`, the API,
the embedding pipeline, or the current evaluation folder.

The experiment reads the existing `code_chunks` LanceDB table:

- **Semantic:** the current CodeRankEmbed cosine search.
- **Lexical:** LanceDB full-text search (BM25) over raw `chunk_text`.
- **Hybrid:** both candidate lists combined with Reciprocal Rank Fusion (RRF), then
  the highest-ranked K chunks are returned directly.

LanceDB already provides FTS support. The optional reranker uses the separately
pinned `onnxruntime` dependency in `rag/requirements.txt`.

## Run Order

Run commands from the repository root. The commands below use the existing RAG
virtual environment and do not call an answer-generating LLM.

### 1. Build the FTS index

```bash
rag/.venv/bin/python -m rag.RAG_lexical.build_fts_index
```

After an ingestion batch changes, adds, or removes chunks, rebuild the experiment's
index:

```bash
rag/.venv/bin/python -m rag.RAG_lexical.build_fts_index --replace
```

### 2. Try one query

```bash
RAG_EMBEDDING_DEVICE=mps rag/.venv/bin/python -m rag.RAG_lexical.retrieval --mode hybrid
```

The default experiment fetches 30 semantic and 30 lexical candidates, fuses them,
and writes the top 10 to `hybrid_results.json`.

Use `--mode semantic` or `--mode lexical` to inspect either branch independently.

### 3. Rerank 30 candidates to the final 8

```bash
RAG_EMBEDDING_DEVICE=mps rag/.venv/bin/python -m rag.RAG_lexical.rerank
```

The reranking defaults are 60 semantic and 60 lexical candidates, 90/10 first-stage
RRF fusion, the best 30 hybrid candidates, and 8 final results. It uses
`faxenoff/code-daemon-reranker-v1`, a multilingual code-search cross-encoder trained
with hard negatives mined by CodeRankEmbed. Long chunks are scored in overlapping
token windows instead of being silently truncated. Final ranking conservatively
fuses the original candidate rank (50%) with the code-reranker rank (50%). No
generative LLM is called and no new embeddings are stored. The model is distributed
as ONNX, so this experiment uses `onnxruntime`; on macOS, `mps` accelerates
CodeRankEmbed through PyTorch while the reranker uses CPU. Core ML can be
tested explicitly with `RAG_RERANK_PROVIDER=CoreMLExecutionProvider`, but it was
slower than CPU for this model's partially supported dynamic graph.

### 4. Establish comparable baselines

All modes reuse the existing 100 manually grounded questions and deterministic gold
source checks from `rag/rag_evaluation`.

```bash
RAG_EMBEDDING_DEVICE=mps rag/.venv/bin/python -m rag.RAG_lexical.evaluate_retrieval --mode semantic -c 10

RAG_EMBEDDING_DEVICE=mps rag/.venv/bin/python -m rag.RAG_lexical.evaluate_retrieval --mode lexical -c 10

RAG_EMBEDDING_DEVICE=mps rag/.venv/bin/python -m rag.RAG_lexical.evaluate_retrieval --mode hybrid -c 10 --semantic-candidates 30 --lexical-candidates 30

RAG_EMBEDDING_DEVICE=mps rag/.venv/bin/python -m rag.RAG_lexical.evaluate_retrieval --mode rerank -c 8 --semantic-candidates 60 --lexical-candidates 60 --semantic-weight 0.9 --lexical-weight 0.1 --candidate-limit 30 --retrieval-rank-weight 0.5 --reranker-rank-weight 0.5
```

The isolated evaluator writes:

- `evaluation-results/analysis.txt`: concise latest metrics and recent run history.
- `evaluation-results/retrieval_details_<mode>_k<K>_<timestamp>.json`: every
  question, expected source, retrieved chunk, rank, and score.
- `evaluation-results/retrieval_history.jsonl`: append-only summaries for comparing
  experiments.

## Why This Is Safe to Test

The semantic and BM25 searches run independently. RRF combines their **ranks**, not
their incomparable raw cosine and BM25 scores. A correct semantic result therefore
remains a candidate; lexical retrieval can promote chunks containing exact endpoint
paths, class names, constants, fail codes, and other identifiers that embeddings may
underweight.

Do not connect this experiment to the chatbot until its hybrid Hit@10, Recall@10,
MRR, and nDCG are better than the semantic baseline on the same test set. The detailed
JSON should also be reviewed for regressions, especially where tests or generated
contracts crowd out production implementation files.
