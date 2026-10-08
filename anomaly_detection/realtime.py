#!/usr/bin/env python3
"""Generate live transactions through the Bank of Z API."""

from __future__ import annotations

import argparse
import random
import time
from dataclasses import dataclass
from decimal import Decimal

from bank_api import BankApi, DEFAULT_API_URL, DEFAULT_TOKEN
from detect import load_dataset


NORMAL_TRANSACTIONS = (
    ("DBT", "Grocery store", 20, 180, 20),
    ("DBT", "Cafe purchase", 3, 25, 10),
    ("DBT", "Restaurant", 15, 120, 12),
    ("DBT", "Pharmacy", 8, 90, 7),
    ("DBT", "Transit fare", 3, 160, 8),
    ("DBT", "Fuel purchase", 35, 140, 8),
    ("DBT", "Online purchase", 12, 300, 10),
    ("DBT", "Utilities payment", 60, 250, 5),
    ("DBT", "Rent payment", 900, 2300, 3),
    ("DBT", "ATM withdrawal", 20, 300, 7),
    ("CRD", "Payroll deposit", 2400, 5200, 5),
    ("CRD", "E-transfer received", 20, 650, 4),
    ("CRD", "Refund", 10, 250, 3),
)


@dataclass(frozen=True, slots=True)
class GeneratedTransaction:
    transaction_type: str
    description: str
    amount: Decimal

    def payload(self) -> dict[str, object]:
        return {
            "type": self.transaction_type,
            "description": self.description,
            "amount": float(self.amount),
        }


def generate_transaction(
    rng: random.Random,
    anomaly_rate: float,
    anomaly_audience: str = "mixed",
) -> GeneratedTransaction:
    if rng.random() < anomaly_rate:
        customer_alert = anomaly_audience == "customer" or (
            anomaly_audience == "mixed" and rng.random() < 0.6
        )
        if customer_alert:
            amount = -Decimal(f"{rng.uniform(7000, 18000):.2f}")
            return GeneratedTransaction("DBT", "Large online purchase", amount)
        amount = Decimal(f"{rng.uniform(10000, 30000):.2f}")
        return GeneratedTransaction("CRD", "Large incoming transfer", amount)

    category = rng.choices(
        NORMAL_TRANSACTIONS,
        weights=[item[4] for item in NORMAL_TRANSACTIONS],
        k=1,
    )[0]
    transaction_type, description, minimum, maximum, _ = category
    amount = Decimal(f"{rng.uniform(minimum, maximum):.2f}")
    if transaction_type == "DBT":
        amount = -amount
    return GeneratedTransaction(transaction_type, description, amount)


def print_created(transaction: dict[str, object], customer_name: str) -> None:
    occurred_at = f"{transaction['date']} {transaction['time']}"
    account = f"{transaction['sortCode']}/{transaction['accountNumber']}"
    amount = Decimal(str(transaction["amount"]))
    print(
        f"CREATED  {occurred_at}  {account:<15}  {customer_name:<20}  "
        f"{transaction['reference']}  {amount:>11,.2f}  {transaction['description']}"
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--api-url", default=DEFAULT_API_URL)
    parser.add_argument("--token", default=DEFAULT_TOKEN)
    parser.add_argument("--count", type=int, default=100, help="Transactions to create; zero runs until stopped")
    parser.add_argument("--min-seconds", type=float, default=5.0)
    parser.add_argument("--max-seconds", type=float, default=12.0)
    parser.add_argument("--anomaly-rate", type=float, default=0.02)
    parser.add_argument(
        "--anomaly-audience",
        choices=("mixed", "customer", "bank"),
        default="mixed",
        help="Anomaly type to generate; mixed preserves the normal simulator behavior",
    )
    parser.add_argument("--seed", type=int)
    args = parser.parse_args()
    if args.count < 0:
        parser.error("--count must be zero or greater")
    if args.min_seconds < 0 or args.max_seconds < args.min_seconds:
        parser.error("interval must satisfy 0 <= min-seconds <= max-seconds")
    if not 0 <= args.anomaly_rate <= 1:
        parser.error("--anomaly-rate must be between 0 and 1")
    return args


def main() -> int:
    args = parse_args()
    rng = random.Random(args.seed)
    api = BankApi(args.api_url, args.token)
    seed = load_dataset()

    eligible_accounts = tuple(
        account
        for account in seed.accounts.values()
        if (customer := seed.customers.get((account.sort_code, account.customer_number)))
        and customer.status == "ACTIVE"
    )
    if not eligible_accounts:
        raise RuntimeError("No active customer accounts are available")

    print(f"Live transaction simulation running across {len(eligible_accounts)} active accounts.")

    created_count = 0
    try:
        while args.count == 0 or created_count < args.count:
            account = rng.choice(eligible_accounts)
            generated = generate_transaction(
                rng,
                args.anomaly_rate,
                args.anomaly_audience,
            )
            created = api.create(account.sort_code, account.number, generated.payload())
            customer = seed.customers[(account.sort_code, account.customer_number)]
            print_created(created, customer.name)

            created_count += 1
            if args.count == 0 or created_count < args.count:
                time.sleep(rng.uniform(args.min_seconds, args.max_seconds))
    except KeyboardInterrupt:
        print(f"\nStopped after {created_count} transactions.")
        return 0
    except RuntimeError as error:
        print(f"\nStopped: {error}")
        return 1

    print(f"Finished after {created_count} transactions.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
