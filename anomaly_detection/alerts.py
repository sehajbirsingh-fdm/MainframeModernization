#!/usr/bin/env python3
"""Persist detected transaction anomalies in a local SQLite registry."""

from __future__ import annotations

import argparse
import json
import sqlite3
import sys
from contextlib import closing
from pathlib import Path
from typing import Iterable, Sequence

from bank_api import BankApi, DEFAULT_API_URL, DEFAULT_TOKEN
from detect import (
    DEFAULT_DATA,
    Finding,
    detect,
    load_live_dataset,
    print_finding_table,
    read_merge_rows,
)


DEFAULT_DB = Path(__file__).resolve().parent / "data" / "alerts.db"
CONTACT_COLUMNS = ("sort_code", "customer_number", "email", "email_enabled")

SCHEMA = """
CREATE TABLE IF NOT EXISTS alert (
    transaction_id TEXT PRIMARY KEY,
    sort_code TEXT NOT NULL,
    account_number TEXT NOT NULL,
    transaction_date TEXT NOT NULL,
    transaction_time TEXT NOT NULL,
    reference TEXT NOT NULL,
    transaction_type TEXT NOT NULL,
    description TEXT NOT NULL,
    amount TEXT NOT NULL,
    customer_number TEXT,
    customer_name TEXT,
    customer_email TEXT,
    reasons TEXT NOT NULL,
    priority INTEGER NOT NULL CHECK (priority BETWEEN 1 AND 3),
    audience TEXT NOT NULL CHECK (audience IN ('CUSTOMER_ALERT', 'BANK_REVIEW')),
    status TEXT NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'SENT', 'FAILED')),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notified_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_alert_status_created
ON alert (status, created_at);
"""


def transaction_id(finding: Finding) -> str:
    transaction = finding.transaction
    return "-".join(
        (
            transaction.sort_code,
            transaction.account_number,
            transaction.occurred_at.strftime("%Y%m%d"),
            transaction.occurred_at.strftime("%H%M%S"),
            transaction.reference,
        )
    )


def load_contact_emails(path: Path = DEFAULT_DATA) -> dict[tuple[str, str], str]:
    rows = read_merge_rows(path, "CUSTOMER_CONTACT", CONTACT_COLUMNS)
    return {
        (row["sort_code"], row["customer_number"]): row["email"]
        for row in rows
        if row["email_enabled"].upper() == "TRUE"
    }


def open_registry(path: Path) -> sqlite3.Connection:
    path.parent.mkdir(parents=True, exist_ok=True)
    connection = sqlite3.connect(path)
    connection.row_factory = sqlite3.Row
    connection.executescript(SCHEMA)
    return connection


def store_findings(
    connection: sqlite3.Connection,
    findings: Iterable[Finding],
    contacts: dict[tuple[str, str], str],
) -> tuple[list[Finding], int]:
    inserted: list[Finding] = []
    existing = 0
    sql = """
        INSERT OR IGNORE INTO alert (
            transaction_id,
            sort_code,
            account_number,
            transaction_date,
            transaction_time,
            reference,
            transaction_type,
            description,
            amount,
            customer_number,
            customer_name,
            customer_email,
            reasons,
            priority,
            audience
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    with connection:
        for finding in findings:
            transaction = finding.transaction
            customer = finding.customer
            customer_key = (
                (customer.sort_code, customer.number)
                if customer is not None
                else None
            )
            cursor = connection.execute(
                sql,
                (
                    transaction_id(finding),
                    transaction.sort_code,
                    transaction.account_number,
                    transaction.occurred_at.strftime("%Y%m%d"),
                    transaction.occurred_at.strftime("%H%M%S"),
                    transaction.reference,
                    transaction.transaction_type,
                    transaction.description,
                    str(transaction.amount),
                    customer.number if customer else None,
                    customer.name if customer else None,
                    contacts.get(customer_key) if customer_key else None,
                    json.dumps(finding.reasons),
                    finding.priority,
                    finding.audience,
                ),
            )
            if cursor.rowcount == 1:
                inserted.append(finding)
            else:
                connection.execute(
                    """
                    UPDATE alert
                    SET
                        customer_number = COALESCE(customer_number, ?),
                        customer_name = COALESCE(customer_name, ?),
                        customer_email = COALESCE(customer_email, ?)
                    WHERE transaction_id = ?
                    """,
                    (
                        customer.number if customer else None,
                        customer.name if customer else None,
                        contacts.get(customer_key) if customer_key else None,
                        transaction_id(finding),
                    ),
                )
                existing += 1

    return inserted, existing


def read_alerts(
    connection: sqlite3.Connection,
    status: str | None,
    limit: int,
    transaction_ids: Sequence[str] = (),
) -> Sequence[sqlite3.Row]:
    conditions: list[str] = []
    parameters: list[object] = []
    if status:
        conditions.append("status = ?")
        parameters.append(status)
    if transaction_ids:
        placeholders = ", ".join("?" for _ in transaction_ids)
        conditions.append(f"transaction_id IN ({placeholders})")
        parameters.extend(transaction_ids)
    where = f"WHERE {' AND '.join(conditions)}" if conditions else ""
    limit_clause = "LIMIT ?" if limit > 0 else ""
    if limit > 0:
        parameters.append(limit)
    return connection.execute(
        f"""
        SELECT *
        FROM alert
        {where}
        ORDER BY created_at DESC, transaction_date DESC, transaction_time DESC
        {limit_clause}
        """,
        parameters,
    ).fetchall()


def print_alerts(rows: Sequence[sqlite3.Row]) -> None:
    print(f"Stored alerts: {len(rows)}")
    if not rows:
        return

    print()
    print(
        f"{'STATUS':<7} {'PRIORITY':>8}  {'AUDIENCE':<14} {'CUSTOMER':<10} "
        f"{'ACCOUNT':<15} {'REFERENCE':<12} {'WHEN':<19} REASON"
    )
    print("-" * 125)
    for row in rows:
        reasons = ", ".join(json.loads(row["reasons"]))
        account = f"{row['sort_code']}/{row['account_number']}"
        occurred_at = (
            f"{row['transaction_date'][0:4]}-{row['transaction_date'][4:6]}-"
            f"{row['transaction_date'][6:8]} {row['transaction_time'][0:2]}:"
            f"{row['transaction_time'][2:4]}:{row['transaction_time'][4:6]}"
        )
        print(
            f"{row['status']:<7} {row['priority']:>8}  {row['audience']:<14} "
            f"{(row['customer_number'] or 'unlinked'):<10} {account:<15} "
            f"{row['reference']:<12} {occurred_at:<19} {reasons}"
        )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--api-url", default=DEFAULT_API_URL)
    parser.add_argument("--token", default=DEFAULT_TOKEN)
    parser.add_argument("--db", type=Path, default=DEFAULT_DB)
    parser.add_argument("--list", action="store_true", help="List stored alerts without scanning")
    parser.add_argument("--status", choices=("NEW", "SENT", "FAILED"))
    parser.add_argument("--transaction-id", action="append", default=[])
    parser.add_argument("--limit", type=int, default=0, help="Limit displayed rows; zero shows all")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        with closing(open_registry(args.db)) as registry:
            if args.list:
                print_alerts(
                    read_alerts(
                        registry,
                        args.status,
                        args.limit,
                        args.transaction_id,
                    )
                )
                return 0

            api = BankApi(args.api_url, args.token)
            dataset = load_live_dataset(api)
            findings = detect(dataset)
            contacts = load_contact_emails()
            inserted, existing = store_findings(registry, findings, contacts)
    except (OSError, RuntimeError, sqlite3.Error, ValueError) as error:
        print(f"Error: {error}", file=sys.stderr)
        return 2

    print(f"Detected anomalies: {len(findings)} of {len(dataset.transactions)} transactions")
    print(f"SQLite registry: {len(inserted)} new, {existing} already stored")
    if inserted:
        print()
        print_finding_table(inserted, args.limit)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
