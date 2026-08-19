# Test Specification - 006 Bank Statement Retrieval

## Test Cases
- TC-001 Valid account and period returns statement summary and entries.
- TC-002 Valid account with no period entries returns empty entries and summary counts.
- TC-003 Invalid period format returns 400.
- TC-004 Invalid account format returns 400.
- TC-004a Invalid statement month (for example YYYY00 or YYYY13) returns 400.
- TC-004b Leap-year February request includes Feb-29 transactions when present.
- TC-005 Unauthenticated request returns 401.
- TC-006 Unauthorized request returns 403.
- TC-007 Missing account returns 404.
- TC-008 Retrieval failure returns 500.
- TC-008a Endpoint returns only requested account's rows (no same-sortcode cross-account leakage).
- TC-008b Missing CUSTOMER row with valid account/statement data still returns 200 statement response.
- TC-008c Null transaction description is returned as `N/A`.
- TC-009 Contract conformance with openapi.yaml.
