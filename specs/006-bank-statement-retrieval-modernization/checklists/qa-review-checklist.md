# QA Review Checklist - 006 Bank Statement Retrieval

- [x] Valid account and period returns statement summary and entries.
- [x] Valid account with no period transactions returns empty statement entries.
- [x] Invalid period format returns 400.
- [x] Invalid statement month returns 400.
- [x] Unauthenticated request returns 401.
- [x] Unauthorized request returns 403.
- [x] Missing account returns 404 contract outcome.
- [x] Technical failures return 500 contract outcome.
- [x] Leap-year February period supports Feb 29 entries.
- [x] Endpoint returns only requested account rows (no cross-account leakage).
- [x] Missing CUSTOMER row still returns 200 when account/statement data exists.
- [x] Null transaction descriptions are normalized to N/A.
- [x] OpenAPI contract conformance for statement endpoint and schemas is validated.
