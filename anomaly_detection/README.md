# Transaction anomaly detection

Rule-based detection against the live Bank of Z transaction API.

```bash
python3 anomaly_detection/detect.py
python3 anomaly_detection/detect.py --json
```

Current rules flag unusually large debits, unusually large credits, activity before
04:00, and three or more transactions within two minutes. Each run reads the current
`PROCTRAN` records through the backend API and only prints its findings. It does not
write alert state.

Priorities are `1` (low), `2` (medium), and `3` (high).

## Alert registry

Run a live scan and save new findings to the local SQLite registry:

```bash
python3 anomaly_detection/alerts.py
```

The registry is stored at `anomaly_detection/data/alerts.db`. Its unique transaction
ID prevents the same transaction from being inserted twice, and the database remains
available when Python and Spring Boot are stopped.

List stored alerts without requiring the backend:

```bash
python3 anomaly_detection/alerts.py --list
python3 anomaly_detection/alerts.py --list --status NEW
```

## Customer notifications

Customer email wording is generated through Groq using `openai/gpt-oss-20b` by
default. Install the dependencies, copy the local environment template, and add your
API key before running the notifier:

```bash
python3 -m pip install -r anomaly_detection/requirements.txt
cp anomaly_detection/.env.example anomaly_detection/.env
```

Then set `GROQ_API_KEY` inside `anomaly_detection/.env`. The real `.env` is ignored
by Git and environment variables still take precedence over values from the file.

Preview one drafted email without sending or changing alert statuses:

```bash
python3 anomaly_detection/notification.py --dry-run --limit 1
```

With Mailpit running on its default SMTP port, send all pending customer alerts:

```bash
python3 anomaly_detection/notification.py
```

Successful notifications are marked `SENT` and are not sent again. `BANK_REVIEW`
alerts are intentionally excluded for future handling in the bank UI. Retry failed
drafting or SMTP deliveries explicitly with `--retry-failed`. Notification delivery
stops if Groq is unavailable; no non-LLM fallback email is sent.

## End-to-end presentation demo

With the already-configured Spring Boot backend and Mailpit process running, launch
the complete demo from the repository root:

```bash
python3 anomaly_detection/main.py
```

Run `realtime.py` separately to simulate incoming transactions. The runner scans the
current API data, saves new findings in SQLite, asks Groq to draft the newest pending
customer email from that scan, sends it to Mailpit, and prints the persisted `SENT`
record. It does not start or stop the transaction simulator, and it does not display
or resend historical alerts when the current scan adds nothing.

Preview the complete flow without SMTP delivery or a status change:

```bash
python3 anomaly_detection/main.py --dry-run
```

The original seed-data QA check remains available separately:

```bash
python3 anomaly_detection/detect.py --evaluate
```

## Real-time simulation

With the Spring Boot backend running, start the simulator from the repository root:

```bash
python3 anomaly_detection/realtime.py
```

It creates 100 transactions through the backend API at random 5–12 second intervals.
Each transaction uses an existing active account, the current Toronto timestamp, and
the next 12-digit database reference. The existing Transaction Inquiry page shows
the new row when that account is queried or refreshed. This script generates data
only; run `detect.py` separately whenever you want a current anomaly scan.

Useful options:

```bash
python3 anomaly_detection/realtime.py --count 10
python3 anomaly_detection/realtime.py --count 0
python3 anomaly_detection/realtime.py --anomaly-rate 0.10
```
