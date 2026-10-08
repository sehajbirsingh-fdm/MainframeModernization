#!/usr/bin/env python3
"""Scan Bank of Z transactions for rule-based anomalies."""

from __future__ import annotations

import argparse
import csv
import json
import sys
from collections import defaultdict, deque
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from pathlib import Path
from statistics import median
from typing import Iterable, Sequence

from bank_api import BankApi, DEFAULT_API_URL, DEFAULT_TOKEN


REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DATA = REPO_ROOT / "backend/api/src/main/resources/data.sql"
DEFAULT_LABELS = REPO_ROOT / "backend/api/src/main/resources/synthetic-anomalies.csv"

CUSTOMER_COLUMNS = (
    "eyecatcher", "sort_code", "number", "title", "first_name", "last_name",
    "date_of_birth", "phone", "address_1", "address_2", "city", "postcode",
    "country", "status", "created_date", "credit_score", "review_date",
)
ACCOUNT_COLUMNS = (
    "eyecatcher", "customer_number", "sort_code", "number", "account_type",
    "interest_rate", "opened", "overdraft_limit", "last_statement",
    "next_statement", "available_balance", "actual_balance",
)
TRANSACTION_COLUMNS = (
    "eyecatcher", "sort_code", "account_number", "date", "time", "reference",
    "transaction_type", "description", "amount",
)

REASON_PRIORITIES = {
    "UNUSUALLY_LARGE_DEBIT": 2,
    "UNUSUALLY_LARGE_CREDIT": 3,
    "UNUSUAL_TIME": 1,
    "RAPID_TRANSACTION_BURST": 2,
}


@dataclass(frozen=True, slots=True)
class Customer:
    sort_code: str
    number: str
    name: str
    status: str


@dataclass(frozen=True, slots=True)
class Account:
    sort_code: str
    number: str
    customer_number: str
    account_type: str


@dataclass(frozen=True, slots=True)
class Transaction:
    sort_code: str
    account_number: str
    occurred_at: datetime
    reference: str
    transaction_type: str
    description: str
    amount: Decimal

    @property
    def account_key(self) -> tuple[str, str]:
        return self.sort_code, self.account_number


@dataclass(frozen=True, slots=True)
class Dataset:
    customers: dict[tuple[str, str], Customer]
    accounts: dict[tuple[str, str], Account]
    transactions: tuple[Transaction, ...]


@dataclass(frozen=True, slots=True)
class DetectionConfig:
    large_debit_floor: Decimal = Decimal("5000")
    large_credit_floor: Decimal = Decimal("8000")
    amount_median_multiplier: Decimal = Decimal("4")
    unusual_time_before_hour: int = 4
    rapid_window_seconds: int = 120
    rapid_transaction_count: int = 3


@dataclass(frozen=True, slots=True)
class Finding:
    transaction: Transaction
    account: Account
    customer: Customer | None
    reasons: tuple[str, ...]

    @property
    def priority(self) -> int:
        return max(REASON_PRIORITIES[reason] for reason in self.reasons)

    @property
    def audience(self) -> str:
        return "BANK_REVIEW" if "UNUSUALLY_LARGE_CREDIT" in self.reasons else "CUSTOMER_ALERT"

    def to_dict(self) -> dict[str, object]:
        transaction = self.transaction
        return {
            "reference": transaction.reference,
            "customer_number": self.customer.number if self.customer else None,
            "customer_name": self.customer.name if self.customer else None,
            "sort_code": transaction.sort_code,
            "account_number": transaction.account_number,
            "occurred_at": transaction.occurred_at.isoformat(),
            "type": transaction.transaction_type,
            "description": transaction.description,
            "amount": str(transaction.amount),
            "priority": self.priority,
            "audience": self.audience,
            "reasons": list(self.reasons),
        }

def _parse_tuple(line: str) -> list[str]:
    value = line.strip()
    if not value.startswith("("):
        raise ValueError(f"Expected SQL tuple, got: {line!r}")

    value = value[1:]
    if value.endswith(",") or value.endswith(";"):
        value = value[:-1].rstrip()
    if not value.endswith(")"):
        raise ValueError(f"Incomplete SQL tuple: {line!r}")

    reader = csv.reader([value[:-1]], quotechar="'", doublequote=True, skipinitialspace=True)
    return [field.strip() for field in next(reader)]


def read_merge_rows(path: Path, table: str, columns: Sequence[str]) -> list[dict[str, str]]:
    marker = f"MERGE INTO {table} "
    rows: list[dict[str, str]] = []
    reading = False

    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if stripped.startswith(marker):
            reading = True
            continue
        if not reading or not stripped.startswith("("):
            continue

        values = _parse_tuple(stripped)
        if len(values) != len(columns):
            raise ValueError(f"{table} row has {len(values)} values; expected {len(columns)}")
        rows.append(dict(zip(columns, values, strict=True)))

        if stripped.endswith(";"):
            return rows

    if not rows:
        raise ValueError(f"No {table} rows found in {path}")
    return rows


def load_dataset(path: Path = DEFAULT_DATA) -> Dataset:
    customer_rows = read_merge_rows(path, "CUSTOMER", CUSTOMER_COLUMNS)
    account_rows = read_merge_rows(path, "ACCOUNT", ACCOUNT_COLUMNS)
    transaction_rows = read_merge_rows(path, "PROCTRAN", TRANSACTION_COLUMNS)

    customers = {
        (row["sort_code"], row["number"]): Customer(
            sort_code=row["sort_code"],
            number=row["number"],
            name=" ".join(part for part in (row["first_name"], row["last_name"]) if part),
            status=row["status"],
        )
        for row in customer_rows
    }
    accounts = {
        (row["sort_code"], row["number"]): Account(
            sort_code=row["sort_code"],
            number=row["number"],
            customer_number=row["customer_number"],
            account_type=row["account_type"],
        )
        for row in account_rows
    }
    transactions = tuple(
        Transaction(
            sort_code=row["sort_code"],
            account_number=row["account_number"],
            occurred_at=datetime.strptime(row["date"] + row["time"], "%Y%m%d%H%M%S"),
            reference=row["reference"],
            transaction_type=row["transaction_type"],
            description=row["description"],
            amount=Decimal(row["amount"]),
        )
        for row in transaction_rows
    )

    missing_accounts = sorted({transaction.account_key for transaction in transactions} - accounts.keys())
    if missing_accounts:
        raise ValueError(f"Transactions reference unknown accounts: {missing_accounts}")

    return Dataset(customers=customers, accounts=accounts, transactions=transactions)


def load_live_dataset(api: BankApi, identities_path: Path = DEFAULT_DATA) -> Dataset:
    identities = load_dataset(identities_path)
    transactions = tuple(
        _transaction_from_api(row)
        for account in identities.accounts.values()
        for row in api.transactions(account.sort_code, account.number)
    )
    return Dataset(
        customers=identities.customers,
        accounts=identities.accounts,
        transactions=transactions,
    )


def _transaction_from_api(row: dict[str, object]) -> Transaction:
    try:
        return Transaction(
            sort_code=str(row["sortCode"]),
            account_number=str(row["accountNumber"]),
            occurred_at=datetime.strptime(str(row["date"]) + str(row["time"]), "%Y%m%d%H%M%S"),
            reference=str(row["reference"]),
            transaction_type=str(row["type"]),
            description=str(row["description"]),
            amount=Decimal(str(row["amount"])),
        )
    except (KeyError, TypeError, ValueError) as error:
        raise RuntimeError("Bank API returned an invalid transaction row") from error


def amount_threshold(values: Iterable[Decimal], floor: Decimal, multiplier: Decimal) -> Decimal:
    observations = tuple(values)
    if not observations:
        return floor
    centre = median(observations)
    return max(floor, centre * multiplier)


def _rapid_references(
    transactions: Iterable[Transaction],
    window_seconds: int,
    minimum_count: int,
) -> set[str]:
    rapid: set[str] = set()
    by_account: dict[tuple[str, str], list[Transaction]] = defaultdict(list)
    for transaction in transactions:
        by_account[transaction.account_key].append(transaction)

    for account_transactions in by_account.values():
        window: deque[Transaction] = deque()
        for transaction in sorted(account_transactions, key=lambda item: item.occurred_at):
            window.append(transaction)
            while (transaction.occurred_at - window[0].occurred_at).total_seconds() > window_seconds:
                window.popleft()
            if len(window) >= minimum_count:
                rapid.update(item.reference for item in window)
    return rapid


def detect(dataset: Dataset, config: DetectionConfig = DetectionConfig()) -> list[Finding]:
    debit_amounts: dict[tuple[str, str], list[Decimal]] = defaultdict(list)
    credit_amounts: dict[tuple[str, str], list[Decimal]] = defaultdict(list)
    for transaction in dataset.transactions:
        if transaction.amount < 0:
            debit_amounts[transaction.account_key].append(abs(transaction.amount))
        else:
            credit_amounts[transaction.account_key].append(transaction.amount)

    debit_thresholds = {
        key: amount_threshold(values, config.large_debit_floor, config.amount_median_multiplier)
        for key, values in debit_amounts.items()
    }
    credit_thresholds = {
        key: amount_threshold(values, config.large_credit_floor, config.amount_median_multiplier)
        for key, values in credit_amounts.items()
    }
    rapid_references = _rapid_references(
        dataset.transactions,
        config.rapid_window_seconds,
        config.rapid_transaction_count,
    )

    findings: list[Finding] = []
    for transaction in dataset.transactions:
        reasons: list[str] = []
        if transaction.amount < 0 and abs(transaction.amount) > debit_thresholds[transaction.account_key]:
            reasons.append("UNUSUALLY_LARGE_DEBIT")
        if transaction.amount > 0 and transaction.amount > credit_thresholds[transaction.account_key]:
            reasons.append("UNUSUALLY_LARGE_CREDIT")
        if transaction.occurred_at.hour < config.unusual_time_before_hour:
            reasons.append("UNUSUAL_TIME")
        if transaction.reference in rapid_references:
            reasons.append("RAPID_TRANSACTION_BURST")
        if not reasons:
            continue

        account = dataset.accounts[transaction.account_key]
        customer = dataset.customers.get((account.sort_code, account.customer_number))
        findings.append(
            Finding(
                transaction=transaction,
                account=account,
                customer=customer,
                reasons=tuple(reasons),
            )
        )

    return sorted(findings, key=lambda item: (item.transaction.occurred_at, item.transaction.reference))


def print_finding_table(findings: Sequence[Finding], limit: int = 0) -> None:
    shown = findings[:limit] if limit > 0 else findings
    if not shown:
        return
    print(
        f"{'PRIORITY':>8}  {'AUDIENCE':<14} {'CUSTOMER':<10} {'NAME':<20} "
        f"{'ACCOUNT':<15} {'REFERENCE':<12} {'WHEN':<19} {'AMOUNT':>11}  REASON"
    )
    print("-" * 140)
    for finding in shown:
        transaction = finding.transaction
        customer_number = finding.customer.number if finding.customer else "unlinked"
        customer_name = finding.customer.name if finding.customer else "Unknown customer"
        account = f"{transaction.sort_code}/{transaction.account_number}"
        reasons = ", ".join(finding.reasons)
        print(
            f"{finding.priority:>8}  {finding.audience:<14} {customer_number:<10} "
            f"{customer_name[:20]:<20} {account:<15} "
            f"{transaction.reference:<12} "
            f"{transaction.occurred_at:%Y-%m-%d %H:%M:%S} "
            f"{transaction.amount:>11,.2f}  {reasons}"
        )
    if limit > 0 and len(findings) > limit:
        print(f"\nShowing {limit} of {len(findings)} findings.")


def print_findings(findings: Sequence[Finding], transaction_count: int, limit: int = 0) -> None:
    print(f"Suspicious transactions: {len(findings)} of {transaction_count}")
    if findings:
        print()
        print_finding_table(findings, limit)


def evaluate(findings: Sequence[Finding], labels_path: Path = DEFAULT_LABELS) -> dict[str, object]:
    with labels_path.open(newline="", encoding="utf-8") as handle:
        expected_rows = list(csv.DictReader(handle))

    expected = {(row["reference"], row["expected_reason"]) for row in expected_rows}
    predicted = {
        (finding.transaction.reference, reason)
        for finding in findings
        for reason in finding.reasons
    }
    matched = expected & predicted
    missed = expected - predicted
    unexpected = predicted - expected
    precision = len(matched) / len(predicted) if predicted else 0.0
    recall = len(matched) / len(expected) if expected else 0.0
    return {
        "expected": len(expected),
        "matched": len(matched),
        "missed": len(missed),
        "unexpected": len(unexpected),
        "precision": precision,
        "recall": recall,
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--api-url", default=DEFAULT_API_URL)
    parser.add_argument("--token", default=DEFAULT_TOKEN)
    parser.add_argument("--data", type=Path, help="Read a data.sql file instead of the live API")
    parser.add_argument("--limit", type=int, default=0, help="Limit displayed findings; zero shows all")
    parser.add_argument("--json", action="store_true", help="Write findings as JSON")
    parser.add_argument("--evaluate", action="store_true", help="Compare results with the synthetic QA labels")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    api = BankApi(args.api_url, args.token)
    try:
        data_path = args.data or (DEFAULT_DATA if args.evaluate else None)
        dataset = (
            load_dataset(data_path)
            if data_path is not None
            else load_live_dataset(api)
        )
    except (OSError, RuntimeError, ValueError) as error:
        print(f"Error: {error}", file=sys.stderr)
        return 2
    findings = detect(dataset)

    evaluation = evaluate(findings) if args.evaluate else None
    if args.json:
        payload: dict[str, object] = {"findings": [finding.to_dict() for finding in findings]}
        if evaluation is not None:
            payload["evaluation"] = evaluation
        print(json.dumps(payload, indent=2))
    else:
        print_findings(findings, len(dataset.transactions), args.limit)

    if evaluation is not None and not args.json:
        print(
            "\nQA evaluation: "
            f"{evaluation['matched']}/{evaluation['expected']} matched, "
            f"{evaluation['missed']} missed, {evaluation['unexpected']} unexpected, "
            f"precision {evaluation['precision']:.1%}, recall {evaluation['recall']:.1%}"
        )
    if evaluation is not None and (evaluation["missed"] or evaluation["unexpected"]):
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
