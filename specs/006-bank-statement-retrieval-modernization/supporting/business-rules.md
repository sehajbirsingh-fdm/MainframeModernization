# Business Rules - 006 Bank Statement Retrieval

- BR-001: Statement is scoped by exact account identity and statement period; no multi-account loop semantics are permitted.
- BR-002: Statement period boundaries follow BNKSTMT intent with standard leap-year calendar handling.
- BR-003: Summary values include periodFrom/periodTo and opening/closing plus credit/debit totals.
- BR-003a: openingBalance is historical period-start balance and must not mirror legacy reverse-from-current formula.
- BR-004: Entries included are only those in the statement period.
- BR-005: Statement retrieval is read-only.
- BR-006: Technical failures are surfaced as technical-error outcomes.
- BR-007: Missing CUSTOMER row does not fail statement retrieval when account and statement data exist.
- BR-008: Null transaction descriptions are returned as `N/A`.
