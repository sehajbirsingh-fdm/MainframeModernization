#!/usr/bin/env python3
"""Run the Bank of Z anomaly-alert demonstration end to end."""

from __future__ import annotations

import argparse
import os
import socket
import sqlite3
import subprocess
import sys
from contextlib import closing
from pathlib import Path
from urllib.parse import urlparse

from bank_api import DEFAULT_API_URL, DEFAULT_TOKEN
from alerts import DEFAULT_DB, open_registry
from notification import DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT


HERE = Path(__file__).resolve().parent
REPOSITORY = HERE.parent


def require_service(name: str, host: str, port: int, start_hint: str) -> None:
    try:
        with socket.create_connection((host, port), timeout=1):
            return
    except OSError as error:
        raise RuntimeError(
            f"{name} is not reachable at {host}:{port}. {start_hint}"
        ) from error


def run_stage(number: int, title: str, script: str, *arguments: str) -> None:
    border = "=" * 72
    print(f"\n{border}\n{number}/3  {title}\n{border}", flush=True)
    result = subprocess.run(
        [sys.executable, str(HERE / script), *arguments],
        cwd=REPOSITORY,
        check=False,
    )
    if result.returncode:
        raise RuntimeError(f"{script} stopped with exit code {result.returncode}")


def print_stage(number: int, title: str, message: str) -> None:
    border = "=" * 72
    print(f"\n{border}\n{number}/3  {title}\n{border}")
    print(message)


def alert_ids(
    path: Path,
    *,
    audience: str | None = None,
) -> set[str]:
    with closing(open_registry(path)) as connection:
        if audience:
            rows = connection.execute(
                "SELECT transaction_id FROM alert WHERE audience = ?",
                (audience,),
            ).fetchall()
        else:
            rows = connection.execute("SELECT transaction_id FROM alert").fetchall()
    return {row["transaction_id"] for row in rows}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--api-url", default=DEFAULT_API_URL)
    parser.add_argument("--token", default=DEFAULT_TOKEN)
    parser.add_argument("--db", type=Path, default=DEFAULT_DB)
    parser.add_argument("--emails", type=int, default=1)
    parser.add_argument("--findings", type=int, default=10)
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Draft the email without sending it or changing alert status",
    )
    args = parser.parse_args()
    if args.emails < 1:
        parser.error("--emails must be at least 1")
    if args.findings < 1:
        parser.error("--findings must be at least 1")
    return args


def backend_address(api_url: str) -> tuple[str, int]:
    parsed = urlparse(api_url)
    if not parsed.hostname:
        raise RuntimeError(f"Invalid API URL: {api_url}")
    default_port = 443 if parsed.scheme == "https" else 80
    return parsed.hostname, parsed.port or default_port


def main() -> int:
    args = parse_args()
    try:
        if not os.getenv("GROQ_API_KEY"):
            raise RuntimeError(
                "GROQ_API_KEY is missing from anomaly_detection/.env."
            )
        api_host, api_port = backend_address(args.api_url)
        require_service(
            "Bank backend",
            api_host,
            api_port,
            "Start the Spring Boot API first.",
        )
        if not args.dry_run:
            require_service(
                "Mailpit SMTP",
                DEFAULT_SMTP_HOST,
                DEFAULT_SMTP_PORT,
                "Start Mailpit before running this demo.",
            )

        common_api = ("--api-url", args.api_url, "--token", args.token)
        known_alerts = alert_ids(args.db)
        run_stage(
            1,
            "DETECT AND REGISTER NEW ALERTS",
            "alerts.py",
            *common_api,
            "--db",
            str(args.db),
            "--limit",
            str(args.findings),
        )
        new_alerts = sorted(alert_ids(args.db) - known_alerts)
        customer_alerts = sorted(
            set(new_alerts) & alert_ids(args.db, audience="CUSTOMER_ALERT")
        )

        if not customer_alerts:
            print_stage(
                2,
                "DRAFT AND SEND CUSTOMER NOTIFICATION",
                "No new customer alerts were registered in this scan. No email sent.",
            )
        else:
            notification_args = [
                "--db",
                str(args.db),
                "--limit",
                str(args.emails),
                "--newest",
            ]
            for transaction_id in customer_alerts:
                notification_args.extend(("--transaction-id", transaction_id))
            if args.dry_run:
                notification_args.append("--dry-run")
            run_stage(
                2,
                "DRAFT AND SEND CUSTOMER NOTIFICATION",
                "notification.py",
                *notification_args,
            )

        status = "NEW" if args.dry_run else "SENT"
        if not customer_alerts:
            print_stage(
                3,
                "VERIFY PERSISTED ALERT STATUS",
                "No notification was created or sent in this run.",
            )
        else:
            verification_args = [
                "--db",
                str(args.db),
                "--list",
                "--status",
                status,
                "--limit",
                str(args.emails),
            ]
            for transaction_id in customer_alerts:
                verification_args.extend(("--transaction-id", transaction_id))
            run_stage(
                3,
                "VERIFY PERSISTED ALERT STATUS",
                "alerts.py",
                *verification_args,
            )
    except (RuntimeError, sqlite3.Error) as error:
        print(f"\nDemo stopped: {error}", file=sys.stderr)
        return 1

    if not customer_alerts:
        print("\nScan complete. No customer notification sent.")
    elif args.dry_run:
        print("\nThe email was previewed only; no message was sent.")
    else:
        print("\nOpen Mailpit at http://localhost:8025")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
