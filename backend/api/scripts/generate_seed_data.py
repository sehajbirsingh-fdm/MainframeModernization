#!/usr/bin/env python3
"""Generate deterministic H2 seed data for the modern banking application.

The generated SQL deliberately preserves the original contract-test records while
adding a realistic synthetic population. Transaction rows keep the existing
PROCTRAN shape; anomaly labels live in a separate QA-only CSV manifest.
"""

from __future__ import annotations

import csv
import random
from dataclasses import dataclass
from datetime import date, datetime, timedelta
from pathlib import Path


SEED = 20260916
TARGET_TRANSACTION_COUNT = 3_000
CONTROLLED_ANOMALY_COUNT = 30
OUTPUT_DIR = Path(__file__).resolve().parents[1] / "src" / "main" / "resources"
SQL_PATH = OUTPUT_DIR / "data.sql"
ANOMALY_PATH = OUTPUT_DIR / "synthetic-anomalies.csv"


@dataclass(frozen=True)
class Customer:
    sort_code: str
    number: str
    title: str
    first_name: str
    last_name: str
    birth_date: int
    phone: str
    address_1: str
    address_2: str
    city: str
    postcode: str
    status: str
    created_date: int
    credit_score: int
    review_date: int


@dataclass(frozen=True)
class Account:
    customer_number: str
    sort_code: str
    number: str
    account_type: str
    interest_rate: float
    opened: int
    overdraft_limit: int
    available_balance: float
    actual_balance: float
    last_statement: int = 20260831
    next_statement: int = 20260930


@dataclass(frozen=True)
class Transaction:
    sort_code: str
    account_number: str
    booked_date: str
    booked_time: str
    reference: str
    transaction_type: str
    description: str
    amount: float


BASE_CUSTOMERS = [
    Customer("123456", "0000000001", "Mr", "John", "Smith", 19750101, "4165550101", "1 Main Street", "Suite 100", "Toronto", "M5H2N2", "ACTIVE", 20100615, 742, 20260115),
    Customer("123456", "0000000002", "Ms", "Asha", "Patel", 19880322, "4165550102", "2 King Street", "Suite 100", "Toronto", "M5V1A1", "ACTIVE", 20190510, 650, 20250510),
    Customer("123456", "0000000003", "Mr", "Sam", "Brown", 19900120, "4165550103", "3 Bay Street", "", "Toronto", "M5J2N1", "SUSPENDED", 20200110, 742, 20260115),
    Customer("123456", "0000000004", "Mrs", "Mina", "Das", 19801105, "4165550104", "4 Front Street", "", "Toronto", "M5V2T6", "ACTIVE", 20180101, 580, 20260115),
    Customer("123456", "0000000005", "Dr", "Nora", "Lee", 19951215, "4165550105", "5 Queen Street", "", "Toronto", "M5H2M9", "ACTIVE", 20210121, 710, 20260201),
    Customer("654321", "0000000002", "Ms", "Priya", "Patel", 19940404, "6135550202", "22 Elm Street", "", "Ottawa", "K1A0B1", "ACTIVE", 20220301, 700, 20260131),
]

# These are deliberate failure-path fixtures used by INQACCCU tests, not bad data.
TEST_CUSTOMERS = [
    Customer("987654", "0000000200", "Test", "Open", "Failure Simulation", 19900101, "4165550900", "90 Test Street", "", "Toronto", "M5A1A1", "TEST", 20200101, 0, 20200101),
    Customer("987654", "0000000300", "Test", "Fetch", "Failure Simulation", 19900101, "4165550901", "91 Test Street", "", "Toronto", "M5A1A2", "TEST", 20200101, 0, 20200101),
    Customer("987654", "0000000400", "Test", "Close", "Failure Simulation", 19900101, "4165550902", "92 Test Street", "", "Toronto", "M5A1A3", "TEST", 20200101, 0, 20200101),
]

BASE_ACCOUNTS = [
    Account("0000000001", "123456", "00000001", "CHK", 0.50, 20200115, 500, 1520.45, 1498.12, 20251231, 20260131),
    Account("0000000001", "123456", "00000099", "SAV", 2.15, 20190520, 0, 9200.00, 9200.00, 20251231, 20260131),
    Account("0000000002", "654321", "20000001", "SAV", 1.75, 20220301, 0, 500.00, 500.00, 20251130, 20251231),
    Account("0000000003", "123456", "00000050", "CHK", 1.10, 20200110, 1000, 2500.00, 2490.00, 20251231, 20260131),
    # Deliberate standalone fixture used by statement and relationship tests.
    Account("9999999999", "123456", "00000077", "CHK", 0.25, 20210101, 0, 150.00, 150.00, 20251231, 20260131),
]

BASE_TRANSACTIONS = [
    Transaction("123456", "00000001", "20260728", "143015", "000000000123", "CRD", "Payroll deposit", 125.50),
    Transaction("123456", "00000001", "20260728", "101500", "000000000124", "DBT", "Grocery store", -45.75),
    Transaction("123456", "00000001", "20260727", "090000", "000000000125", "DBT", "Utilities payment", -80.00),
    Transaction("123456", "00000001", "20260726", "223000", "000000000126", "CRD", "Refund", 15.25),
    Transaction("123456", "00000001", "20260726", "223000", "000000000127", "DBT", "Cafe purchase", -10.00),
    Transaction("123456", "00000099", "20260720", "121500", "000000000128", "CRD", "Interest credit", 5.00),
    Transaction("654321", "20000001", "20260719", "111000", "000000000129", "DBT", "ATM withdrawal", -20.00),
    Transaction("123456", "00000050", "20260718", "081500", "000000000130", "DBT", "Transit pass", -12.75),
    Transaction("123456", "00000077", "20260708", "091500", "000000000132", "CRD", "Standalone account credit", 100.00),
    Transaction("123456", "00000077", "20280229", "120000", "000000000133", "CRD", "Leap day credit", 29.00),
]

PEOPLE = [
    ("Mr", "Liam", "Wilson"), ("Ms", "Sofia", "Chen"),
    ("Mr", "Noah", "Williams"), ("Ms", "Maya", "Singh"),
    ("Mr", "Ethan", "Martin"), ("Ms", "Olivia", "Thompson"),
    ("Mr", "Lucas", "Roy"), ("Ms", "Emma", "Garcia"),
    ("Mr", "Benjamin", "Taylor"), ("Ms", "Chloe", "Nguyen"),
    ("Mr", "Daniel", "Kim"), ("Ms", "Amelia", "Johnson"),
    ("Mr", "Henry", "Campbell"), ("Ms", "Zoe", "Anderson"),
    ("Mr", "Adam", "Hassan"), ("Ms", "Grace", "Wong"),
    ("Mr", "Nathan", "Clarke"), ("Ms", "Layla", "Ahmed"),
    ("Mr", "Ryan", "Murphy"), ("Ms", "Ava", "Robinson"),
    ("Mr", "Jacob", "Tremblay"), ("Ms", "Isabella", "Rossi"),
    ("Mr", "Marcus", "Green"), ("Ms", "Emily", "Baker"),
]

LOCATIONS = [
    ("654321", "Ottawa", "613", "K1P5G4", "Wellington Street"),
    ("234567", "Vancouver", "604", "V6B2W9", "Granville Street"),
    ("345678", "Calgary", "403", "T2P1J9", "Stephen Avenue"),
    ("456789", "Montreal", "514", "H2Y1C6", "Saint-Paul Street"),
    ("567890", "Halifax", "902", "B3J1S9", "Barrington Street"),
]

DEBIT_CATEGORIES = [
    ("Grocery store", 20, 180, 20),
    ("Cafe purchase", 3, 25, 10),
    ("Restaurant", 15, 120, 12),
    ("Pharmacy", 8, 90, 7),
    ("Transit fare", 3, 160, 8),
    ("Fuel purchase", 35, 140, 8),
    ("Online purchase", 12, 300, 10),
    ("Mobile bill", 45, 140, 5),
    ("Utilities payment", 60, 250, 5),
    ("Rent payment", 900, 2300, 3),
    ("Insurance premium", 80, 300, 4),
    ("Gym membership", 25, 100, 3),
    ("Streaming subscription", 8, 30, 3),
    ("ATM withdrawal", 20, 300, 7),
]

CREDIT_CATEGORIES = [
    ("Payroll deposit", 2400, 5200, 45),
    ("E-transfer received", 20, 650, 30),
    ("Refund", 10, 250, 20),
    ("Interest credit", 1, 35, 5),
]


def sql_string(value: object) -> str:
    return "'" + str(value).replace("'", "''") + "'"


def sql_decimal(value: float) -> str:
    return f"{value:.2f}"


def random_date(rng: random.Random, start: date, end: date) -> date:
    return start + timedelta(days=rng.randint(0, (end - start).days))


def as_yyyymmdd(value: date) -> int:
    return int(value.strftime("%Y%m%d"))


def build_customers(rng: random.Random) -> list[Customer]:
    customers: list[Customer] = []
    for index, (title, first_name, last_name) in enumerate(PEOPLE, start=1):
        sort_code, city, area_code, postcode, street = LOCATIONS[(index - 1) % len(LOCATIONS)]
        birth_date = random_date(rng, date(1962, 1, 1), date(2002, 12, 31))
        created_date = random_date(rng, date(2015, 1, 1), date(2024, 9, 30))
        customers.append(
            Customer(
                sort_code=sort_code,
                number=f"{100 + index:010d}",
                title=title,
                first_name=first_name,
                last_name=last_name,
                birth_date=as_yyyymmdd(birth_date),
                phone=f"{area_code}555{1000 + index:04d}",
                address_1=f"{100 + index} {street}",
                address_2="" if index % 4 else f"Unit {index}",
                city=city,
                postcode=postcode,
                status="ACTIVE" if index != 19 else "INACTIVE",
                created_date=as_yyyymmdd(created_date),
                credit_score=rng.randint(560, 835),
                review_date=20261000 + ((index % 28) + 1),
            )
        )
    return customers


def build_accounts(rng: random.Random, customers: list[Customer]) -> list[Account]:
    accounts = [
        Account("0000000004", "123456", "00000002", "CHK", 0.35, 20180315, 750, 3840.20, 3801.55),
        Account("0000000005", "123456", "00000003", "SAV", 2.40, 20210201, 0, 12680.75, 12680.75),
    ]
    next_account = 10_000_001
    for index, customer in enumerate(customers):
        account_count = 2 if index < 14 else 1
        for account_index in range(account_count):
            account_type = "CHK" if account_index == 0 else "SAV"
            actual_balance = round(rng.uniform(400, 14_000) if account_type == "CHK" else rng.uniform(2_000, 55_000), 2)
            pending = round(rng.uniform(0, 220), 2) if account_type == "CHK" else 0.0
            opened_year = str(customer.created_date)[:4]
            opened = int(f"{opened_year}{(index % 12) + 1:02d}{(index % 27) + 1:02d}")
            accounts.append(
                Account(
                    customer_number=customer.number,
                    sort_code=customer.sort_code,
                    number=f"{next_account:08d}",
                    account_type=account_type,
                    interest_rate=round(rng.uniform(0.20, 0.85), 2) if account_type == "CHK" else round(rng.uniform(1.50, 3.75), 2),
                    opened=opened,
                    overdraft_limit=rng.choice([0, 500, 1_000, 1_500]) if account_type == "CHK" else 0,
                    available_balance=round(actual_balance - pending, 2),
                    actual_balance=actual_balance,
                )
            )
            next_account += 1
    assert len(accounts) == 40
    return accounts


def weighted_choice(rng: random.Random, categories: list[tuple[str, int, int, int]]) -> tuple[str, int, int, int]:
    return rng.choices(categories, weights=[item[3] for item in categories], k=1)[0]


def next_reference(counter: int) -> str:
    return f"{100_000_000_000 + counter:012d}"


def build_transactions(
    rng: random.Random,
    accounts: list[Account],
) -> tuple[list[Transaction], list[dict[str, str]]]:
    transactions: list[Transaction] = []
    anomalies: list[dict[str, str]] = []
    reference_counter = 1
    period_start = date(2026, 1, 1)
    period_end = date(2026, 8, 31)

    # Exactly 74 ordinary transactions per generated account: 2,960 rows.
    for account_index, account in enumerate(accounts):
        spending_factor = rng.uniform(0.72, 1.38)
        for _ in range(74):
            booked_date = random_date(rng, period_start, period_end)
            hour = rng.choices(range(6, 23), weights=[2, 3, 5, 7, 8, 8, 8, 8, 8, 8, 7, 7, 6, 6, 5, 4, 3], k=1)[0]
            booked_time = f"{hour:02d}{rng.randint(0, 59):02d}{rng.randint(0, 59):02d}"
            if rng.random() < 0.14:
                description, low, high, _weight = weighted_choice(rng, CREDIT_CATEGORIES)
                amount = round(rng.uniform(low, high) * (0.92 + spending_factor * 0.08), 2)
                transaction_type = "CRD"
            else:
                description, low, high, _weight = weighted_choice(rng, DEBIT_CATEGORIES)
                amount = -round(rng.uniform(low, high) * spending_factor, 2)
                transaction_type = "DBT"
            transactions.append(
                Transaction(
                    account.sort_code,
                    account.number,
                    booked_date.strftime("%Y%m%d"),
                    booked_time,
                    next_reference(reference_counter),
                    transaction_type,
                    description,
                    amount,
                )
            )
            reference_counter += 1

    # Fifteen unusually large amounts: debit alerts go to customers; credits to bank review.
    for index, account in enumerate(accounts[:15]):
        is_credit = index >= 8
        booked_date = date(2026, 8, 4) + timedelta(days=index)
        transaction = Transaction(
            account.sort_code,
            account.number,
            booked_date.strftime("%Y%m%d"),
            f"{10 + (index % 8):02d}1500",
            next_reference(reference_counter),
            "CRD" if is_credit else "DBT",
            "Large incoming transfer" if is_credit else "Large online purchase",
            round(rng.uniform(15_000, 28_000), 2) if is_credit else -round(rng.uniform(7_500, 16_000), 2),
        )
        transactions.append(transaction)
        anomalies.append(anomaly_row(transaction, "UNUSUALLY_LARGE_CREDIT" if is_credit else "UNUSUALLY_LARGE_DEBIT", "BANK_REVIEW" if is_credit else "CUSTOMER_ALERT"))
        reference_counter += 1

    # Nine transactions at hours absent from the normal generator.
    for index, account in enumerate(accounts[15:24]):
        transaction = Transaction(
            account.sort_code,
            account.number,
            (date(2026, 8, 12) + timedelta(days=index)).strftime("%Y%m%d"),
            f"0{1 + (index % 3)}{10 + index:02d}30",
            next_reference(reference_counter),
            "DBT",
            "Unusual late-night purchase",
            -round(rng.uniform(80, 620), 2),
        )
        transactions.append(transaction)
        anomalies.append(anomaly_row(transaction, "UNUSUAL_TIME", "CUSTOMER_ALERT"))
        reference_counter += 1

    # Two three-transaction bursts occurring within 90 seconds.
    for group_index, account in enumerate(accounts[24:26]):
        burst_start = datetime(2026, 8, 25 + group_index, 14 + group_index, 20, 0)
        for burst_index in range(3):
            instant = burst_start + timedelta(seconds=burst_index * 45)
            transaction = Transaction(
                account.sort_code,
                account.number,
                instant.strftime("%Y%m%d"),
                instant.strftime("%H%M%S"),
                next_reference(reference_counter),
                "DBT",
                "Rapid online purchase",
                -round(rng.uniform(220, 780), 2),
            )
            transactions.append(transaction)
            anomalies.append(anomaly_row(transaction, "RAPID_TRANSACTION_BURST", "CUSTOMER_ALERT"))
            reference_counter += 1

    assert len(transactions) == TARGET_TRANSACTION_COUNT - len(BASE_TRANSACTIONS)
    assert len(anomalies) == CONTROLLED_ANOMALY_COUNT
    return transactions, anomalies


def anomaly_row(transaction: Transaction, reason: str, audience: str) -> dict[str, str]:
    return {
        "reference": transaction.reference,
        "sort_code": transaction.sort_code,
        "account_number": transaction.account_number,
        "date": transaction.booked_date,
        "time": transaction.booked_time,
        "expected_reason": reason,
        "expected_audience": audience,
    }


def customer_sql(customer: Customer) -> str:
    values = [
        "CUST", customer.sort_code, customer.number, customer.title,
        customer.first_name, customer.last_name, customer.birth_date,
        customer.phone, customer.address_1, customer.address_2, customer.city,
        customer.postcode, "Canada", customer.status, customer.created_date,
        customer.credit_score, customer.review_date,
    ]
    return "(" + ", ".join(str(value) if isinstance(value, int) else sql_string(value) for value in values) + ")"


def account_sql(account: Account) -> str:
    values = [
        sql_string("ACCOUNT"), sql_string(account.customer_number),
        sql_string(account.sort_code), sql_string(account.number),
        sql_string(account.account_type), sql_decimal(account.interest_rate),
        str(account.opened), str(account.overdraft_limit), str(account.last_statement), str(account.next_statement),
        sql_decimal(account.available_balance), sql_decimal(account.actual_balance),
    ]
    return "(" + ", ".join(values) + ")"


def customer_email(customer: Customer) -> str:
    first_name = "".join(character for character in customer.first_name.lower() if character.isalnum())
    last_name = "".join(character for character in customer.last_name.lower() if character.isalnum())
    return f"{first_name}.{last_name}@exp.com"


def customer_contact_sql(customer: Customer) -> str:
    values = [
        sql_string(customer.sort_code),
        sql_string(customer.number),
        sql_string(customer_email(customer)),
        "FALSE" if customer.status == "TEST" else "TRUE",
    ]
    return "(" + ", ".join(values) + ")"


def transaction_sql(transaction: Transaction) -> str:
    values = [
        sql_string("TRN "), sql_string(transaction.sort_code),
        sql_string(transaction.account_number), sql_string(transaction.booked_date),
        sql_string(transaction.booked_time), sql_string(transaction.reference),
        sql_string(transaction.transaction_type), sql_string(transaction.description),
        sql_decimal(transaction.amount),
    ]
    return "(" + ", ".join(values) + ")"


def merge_statement(table: str, key: str, rows: list[str]) -> str:
    return f"MERGE INTO {table} KEY ({key}) VALUES\n" + ",\n".join(rows) + ";"


def validate(
    realistic_customers: list[Customer],
    generated_accounts: list[Account],
    transactions: list[Transaction],
    anomalies: list[dict[str, str]],
) -> None:
    all_customers = BASE_CUSTOMERS + realistic_customers
    all_accounts = BASE_ACCOUNTS + generated_accounts
    all_transactions = BASE_TRANSACTIONS + transactions
    customer_numbers = {customer.number for customer in all_customers}
    account_keys = {(account.sort_code, account.number) for account in all_accounts}
    transaction_keys = {
        (row.sort_code, row.account_number, row.booked_date, row.booked_time, row.reference)
        for row in all_transactions
    }
    references = {row.reference for row in all_transactions}

    assert len(all_customers) == 30
    assert len(TEST_CUSTOMERS) == 3
    contact_emails = {customer_email(customer) for customer in all_customers + TEST_CUSTOMERS}
    assert len(contact_emails) == len(all_customers) + len(TEST_CUSTOMERS)
    assert len(all_accounts) == 45
    assert len(all_transactions) == TARGET_TRANSACTION_COUNT
    assert len(account_keys) == len(all_accounts)
    assert len(transaction_keys) == len(all_transactions)
    assert len(references) == len(all_transactions)
    assert len(anomalies) == CONTROLLED_ANOMALY_COUNT
    assert all(account.customer_number in customer_numbers or account.customer_number == "9999999999" for account in all_accounts)
    assert all((row.sort_code, row.account_number) in account_keys for row in all_transactions)
    assert all(len(row.description) <= 40 for row in all_transactions)


def render_sql(
    realistic_customers: list[Customer],
    generated_accounts: list[Account],
    transactions: list[Transaction],
) -> str:
    all_customers = BASE_CUSTOMERS + realistic_customers + TEST_CUSTOMERS
    all_accounts = BASE_ACCOUNTS + generated_accounts
    all_transactions = BASE_TRANSACTIONS + sorted(
        transactions,
        key=lambda row: (row.sort_code, row.account_number, row.booked_date, row.booked_time, row.reference),
    )

    sections = [
        """-- GENERATED FILE: run backend/api/scripts/generate_seed_data.py to rebuild.\n-- Dataset: 30 realistic customers + 3 failure-test fixtures, 45 accounts,\n-- 3,000 transactions, including 30 controlled synthetic anomalies (1%).\n-- The legacy CUSTOMER and PROCTRAN column formats remain unchanged.""",
        merge_statement(
            "CUSTOMER",
            "CUSTOMER_SORTCODE, CUSTOMER_NUMBER",
            [customer_sql(row) for row in all_customers],
        ),
        merge_statement(
            "CUSTOMER_CONTACT",
            "CUSTOMER_CONTACT_SORTCODE, CUSTOMER_CONTACT_NUMBER",
            [customer_contact_sql(row) for row in all_customers],
        ),
        merge_statement(
            "ACCOUNT",
            "ACCOUNT_SORTCODE, ACCOUNT_NUMBER",
            [account_sql(row) for row in all_accounts],
        ),
        merge_statement(
            "PROCTRAN",
            "PROCTRAN_SORTCODE, PROCTRAN_NUMBER, PROCTRAN_DATE, PROCTRAN_TIME, PROCTRAN_REF",
            [transaction_sql(row) for row in all_transactions],
        ),
        """-- Reserved customer numbers below intentionally simulate legacy retrieval failures.\nMERGE INTO RELATIONSHIP_SIMULATION KEY (CUSTOMER_NUMBER) VALUES\n('0000000200', 'OPEN_FAILURE'),\n('0000000300', 'FETCH_FAILURE'),\n('0000000400', 'CLOSE_FAILURE');""",
    ]
    return "\n\n".join(sections) + "\n"


def write_anomaly_manifest(anomalies: list[dict[str, str]]) -> None:
    with ANOMALY_PATH.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(anomalies[0]))
        writer.writeheader()
        writer.writerows(anomalies)


def main() -> None:
    rng = random.Random(SEED)
    realistic_customers = build_customers(rng)
    generated_accounts = build_accounts(rng, realistic_customers)
    transactions, anomalies = build_transactions(rng, generated_accounts)
    validate(realistic_customers, generated_accounts, transactions, anomalies)

    SQL_PATH.write_text(render_sql(realistic_customers, generated_accounts, transactions), encoding="utf-8")
    write_anomaly_manifest(anomalies)
    print(
        f"Generated {SQL_PATH} with {TARGET_TRANSACTION_COUNT} transactions; "
        f"wrote {CONTROLLED_ANOMALY_COUNT} QA labels to {ANOMALY_PATH}."
    )


if __name__ == "__main__":
    main()
