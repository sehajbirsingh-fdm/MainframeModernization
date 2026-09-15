"""Create or replace the experimental LanceDB full-text index."""

from __future__ import annotations

import argparse
from datetime import timedelta
from typing import Any

import lancedb
from lancedb.index import FTS

from rag.sync_embedding_table import DATABASE_PATH, TABLE_NAME


FTS_COLUMN = "chunk_text"
FTS_INDEX_NAME = "chunk_text_fts"


def _index_columns(index: Any) -> list[str]:
    columns = getattr(index, "columns", [])
    return list(columns) if columns is not None else []


def build_fts_index(replace: bool = False) -> str:
    """Build a BM25 index over existing raw chunk text."""
    database = lancedb.connect(DATABASE_PATH)
    if TABLE_NAME not in set(database.list_tables().tables):
        raise FileNotFoundError(
            f"LanceDB table '{TABLE_NAME}' does not exist at {DATABASE_PATH}. "
            "Run sync_embedding_table.py first."
        )

    table = database.open_table(TABLE_NAME)
    if FTS_COLUMN not in table.schema.names:
        raise ValueError(f"LanceDB table is missing the '{FTS_COLUMN}' column")

    existing = [
        index
        for index in table.list_indices()
        if FTS_COLUMN in _index_columns(index)
    ]
    if existing and not replace:
        return (
            f"FTS index already exists on '{FTS_COLUMN}'. "
            "Use --replace after an ingestion batch to rebuild it."
        )

    table.create_index(
        FTS_COLUMN,
        config=FTS(
            base_tokenizer="simple",
            lower_case=True,
            stem=False,
            remove_stop_words=False,
            max_token_length=100,
            with_position=False,
        ),
        name=FTS_INDEX_NAME,
        replace=replace,
        wait_timeout=timedelta(minutes=5),
    )
    return (
        f"Built BM25 FTS index '{FTS_INDEX_NAME}' on "
        f"{TABLE_NAME}.{FTS_COLUMN} at {DATABASE_PATH}"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--replace",
        action="store_true",
        help="Replace the current FTS index after chunk additions, edits, or deletions.",
    )
    args = parser.parse_args()
    print(build_fts_index(replace=args.replace))


if __name__ == "__main__":
    main()
