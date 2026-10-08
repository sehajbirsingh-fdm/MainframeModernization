#!/usr/bin/env python3
"""Send pending customer alerts through a local SMTP server."""

from __future__ import annotations

import argparse
import json
import os
import smtplib
import sqlite3
import sys
from contextlib import closing
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from email.message import EmailMessage
from email.utils import formatdate, make_msgid
from pathlib import Path
from typing import Any, Sequence

from dotenv import load_dotenv

from alerts import DEFAULT_DB, open_registry


load_dotenv(Path(__file__).resolve().with_name(".env"))

DEFAULT_SMTP_HOST = os.getenv("MAILPIT_SMTP_HOST", "localhost")
DEFAULT_SMTP_PORT = int(os.getenv("MAILPIT_SMTP_PORT", "1025"))
DEFAULT_GROQ_MODEL = os.getenv("GROQ_MODEL", "openai/gpt-oss-20b")
DEFAULT_SENDER = os.getenv(
    "BANK_ALERT_SENDER",
    "Bank of Z Alerts <admin@bankz.test>",
)
REASON_LABELS = {
    "UNUSUALLY_LARGE_DEBIT": "an unusually large debit",
    "UNUSUAL_TIME": "activity at an unusual time",
    "RAPID_TRANSACTION_BURST": "multiple transactions in a short period",
}


@dataclass(frozen=True, slots=True)
class CustomerAlert:
    transaction_id: str
    reference: str
    account_number: str
    occurred_at: datetime
    description: str
    amount: Decimal
    customer_name: str
    customer_email: str
    reasons: str


@dataclass(frozen=True, slots=True)
class EmailDraft:
    subject: str
    body: str


def pending_customer_alerts(
    connection: sqlite3.Connection,
    retry_failed: bool,
    limit: int,
    newest: bool = False,
    transaction_ids: Sequence[str] = (),
) -> list[CustomerAlert]:
    statuses = ("NEW", "FAILED") if retry_failed else ("NEW",)
    placeholders = ", ".join("?" for _ in statuses)
    parameters: list[object] = list(statuses)
    transaction_filter = ""
    if transaction_ids:
        transaction_placeholders = ", ".join("?" for _ in transaction_ids)
        transaction_filter = f"AND transaction_id IN ({transaction_placeholders})"
        parameters.extend(transaction_ids)
    limit_clause = "LIMIT ?" if limit > 0 else ""
    if limit > 0:
        parameters.append(limit)
    order = "DESC" if newest else "ASC"

    rows = connection.execute(
        f"""
        SELECT
            transaction_id,
            reference,
            account_number,
            transaction_date,
            transaction_time,
            description,
            amount,
            customer_name,
            customer_email,
            reasons
        FROM alert
        WHERE audience = 'CUSTOMER_ALERT'
          AND status IN ({placeholders})
          {transaction_filter}
        ORDER BY created_at {order}, transaction_date {order}, transaction_time {order}
        {limit_clause}
        """,
        parameters,
    ).fetchall()

    alerts: list[CustomerAlert] = []
    for row in rows:
        alerts.append(
            CustomerAlert(
                transaction_id=row["transaction_id"],
                reference=row["reference"],
                account_number=row["account_number"],
                occurred_at=datetime.strptime(
                    row["transaction_date"] + row["transaction_time"],
                    "%Y%m%d%H%M%S",
                ),
                description=row["description"],
                amount=Decimal(row["amount"]),
                customer_name=row["customer_name"] or "Customer",
                customer_email=row["customer_email"] or "",
                reasons=row["reasons"],
            )
        )
    return alerts


def create_groq_client() -> Any:
    api_key = os.getenv("GROQ_API_KEY")
    if not api_key:
        raise RuntimeError("GROQ_API_KEY is not set")
    try:
        from groq import Groq
    except ImportError as error:
        raise RuntimeError("Groq SDK is not installed; run: python3 -m pip install groq") from error
    return Groq(api_key=api_key)


def draft_email(client: Any, alert: CustomerAlert, model: str) -> EmailDraft:
    amount = abs(alert.amount)
    direction = "debit" if alert.amount < 0 else "credit"
    first_name = alert.customer_name.split(maxsplit=1)[0]
    account_suffix = alert.account_number[-4:]
    reason_labels = [
        REASON_LABELS.get(reason, reason.lower().replace("_", " "))
        for reason in json.loads(alert.reasons)
    ]
    reason_summary = ", ".join(reason_labels)

    facts = {
        "first_name": first_name,
        "account_ending": account_suffix,
        "transaction_type": direction,
        "amount": f"${amount:,.2f}",
        "date_time": f"{alert.occurred_at:%Y-%m-%d %H:%M:%S}",
        "description": alert.description,
        "reference": alert.reference,
        "reason": reason_summary,
    }
    try:
        response = client.chat.completions.create(
            model=model,
            reasoning_effort="low",
            temperature=0.2,
            max_completion_tokens=400,
            messages=[
                {
                    "role": "system",
                    "content": (
                        "Write a concise, calm transaction-security email for the fictional Bank of Z. "
                        "Use only the provided facts. The body must begin exactly with 'Hi <first_name>,' "
                        "and explain that an unusual transaction was noticed. Copy the account ending, "
                        "amount, date_time, description, and reference exactly as supplied. State that no action is "
                        "required if the customer recognizes it; otherwise ask them to contact Bank of Z "
                        "immediately through an official support channel. Do not invent phone numbers, "
                        "links, policies, consequences, or additional transaction details. Do not ask for "
                        "passwords or banking credentials. End with 'Bank of Z Security Team'."
                    ),
                },
                {
                    "role": "user",
                    "content": json.dumps(facts),
                },
            ],
            response_format={
                "type": "json_schema",
                "json_schema": {
                    "name": "bank_alert_email",
                    "strict": True,
                    "schema": {
                        "type": "object",
                        "properties": {
                            "subject": {"type": "string"},
                            "body": {"type": "string"},
                        },
                        "required": ["subject", "body"],
                        "additionalProperties": False,
                    },
                },
            },
        )
        content = response.choices[0].message.content
        payload = json.loads(content or "")
    except Exception as error:
        raise RuntimeError(f"Groq drafting failed: {error}") from error

    subject = payload.get("subject")
    body = payload.get("body")
    if not isinstance(subject, str) or not subject.strip():
        raise RuntimeError("Groq returned an empty email subject")
    if not isinstance(body, str) or not body.strip():
        raise RuntimeError("Groq returned an empty email body")

    required_text = (
        f"Hi {first_name},",
        facts["amount"],
        facts["date_time"],
        facts["description"],
        facts["reference"],
        account_suffix,
        "Bank of Z Security Team",
    )
    if any(value not in body for value in required_text):
        raise RuntimeError("Groq draft omitted required transaction details")
    normalized_body = body.lower()
    if "no action" not in normalized_body or "contact" not in normalized_body:
        raise RuntimeError("Groq draft omitted the required customer guidance")
    if len(subject) > 140 or len(body) > 3_000:
        raise RuntimeError("Groq draft exceeded the allowed email length")
    if "http://" in body.lower() or "https://" in body.lower():
        raise RuntimeError("Groq draft unexpectedly included a link")

    return EmailDraft(subject=subject.strip(), body=body.strip())


def build_message(
    alert: CustomerAlert,
    sender: str,
    draft: EmailDraft,
) -> EmailMessage:
    message = EmailMessage()
    message["From"] = sender
    message["To"] = alert.customer_email
    message["Subject"] = draft.subject
    message["Date"] = formatdate(localtime=True)
    message["Message-ID"] = make_msgid(domain="bankz.test")
    message["X-Bank-Alert-ID"] = alert.transaction_id
    message.set_content(draft.body)
    return message


def update_status(
    connection: sqlite3.Connection,
    transaction_id: str,
    status: str,
) -> None:
    with connection:
        connection.execute(
            """
            UPDATE alert
            SET
                status = ?,
                notified_at = CASE WHEN ? = 'SENT' THEN CURRENT_TIMESTAMP ELSE NULL END
            WHERE transaction_id = ?
            """,
            (status, status, transaction_id),
        )


def send_notifications(
    connection: sqlite3.Connection,
    alerts: Sequence[CustomerAlert],
    host: str,
    port: int,
    sender: str,
    groq_client: Any,
    groq_model: str,
) -> tuple[int, int]:
    sent = 0
    failed = 0
    with smtplib.SMTP(host, port, timeout=10) as smtp:
        for alert in alerts:
            if not alert.customer_email:
                update_status(connection, alert.transaction_id, "FAILED")
                failed += 1
                print(f"FAILED {alert.reference} -> customer email is missing")
                continue
            try:
                draft = draft_email(groq_client, alert, groq_model)
                smtp.send_message(build_message(alert, sender, draft))
            except (OSError, RuntimeError, smtplib.SMTPException) as error:
                update_status(connection, alert.transaction_id, "FAILED")
                failed += 1
                print(f"FAILED {alert.reference} -> {alert.customer_email}: {error}")
                continue

            update_status(connection, alert.transaction_id, "SENT")
            sent += 1
            print(f"SENT   {alert.reference} -> {alert.customer_email}")
    return sent, failed


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--db", type=Path, default=DEFAULT_DB)
    parser.add_argument("--smtp-host", default=DEFAULT_SMTP_HOST)
    parser.add_argument("--smtp-port", type=int, default=DEFAULT_SMTP_PORT)
    parser.add_argument("--sender", default=DEFAULT_SENDER)
    parser.add_argument("--groq-model", default=DEFAULT_GROQ_MODEL)
    parser.add_argument("--limit", type=int, default=0, help="Maximum emails to process; zero sends all")
    parser.add_argument(
        "--newest",
        action="store_true",
        help="Process the newest pending customer alerts first",
    )
    parser.add_argument(
        "--transaction-id",
        action="append",
        default=[],
        help="Only process the specified alert transaction ID; may be repeated",
    )
    parser.add_argument("--dry-run", action="store_true", help="Preview recipients without sending")
    parser.add_argument("--retry-failed", action="store_true", help="Retry alerts marked FAILED")
    args = parser.parse_args()
    if args.limit < 0:
        parser.error("--limit must be zero or greater")
    return args


def main() -> int:
    args = parse_args()
    try:
        with closing(open_registry(args.db)) as registry:
            alerts = pending_customer_alerts(
                registry,
                args.retry_failed,
                args.limit,
                args.newest,
                args.transaction_id,
            )
            if not alerts:
                print("No customer alerts awaiting notification.")
                return 0

            groq_client = create_groq_client()

            if args.dry_run:
                print(f"Customer alerts ready: {len(alerts)}")
                for alert in alerts:
                    recipient = alert.customer_email or "[missing customer email]"
                    if not alert.customer_email:
                        print(f"CANNOT DRAFT {alert.reference} -> {recipient}")
                        continue
                    draft = draft_email(groq_client, alert, args.groq_model)
                    print(f"\nTO: {recipient}")
                    print(f"SUBJECT: {draft.subject}\n")
                    print(draft.body)
                print("No emails sent and no statuses changed.")
                return 0

            sent, failed = send_notifications(
                registry,
                alerts,
                args.smtp_host,
                args.smtp_port,
                args.sender,
                groq_client,
                args.groq_model,
            )
    except (OSError, RuntimeError, smtplib.SMTPException, sqlite3.Error, ValueError) as error:
        print(f"Error: {error}", file=sys.stderr)
        return 2

    print(f"Notification summary: {sent} sent, {failed} failed")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
