# Business Rules — INQTRANL

All rules below have direct legacy evidence. Modern HTTP validation decisions not present in the legacy source are excluded or marked for validation.

## BR-001 — Account identity filters transactions
- **Source evidence:** `WHERE PROCTRAN_SORTCODE = :HV-QUERY-SORTCODE AND PROCTRAN_NUMBER = :HV-QUERY-ACCNO`.
- **Rule:** Return only transactions whose sort code and account number exactly match the request.
- **Modern interpretation:** Repository query uses both fixed-width string identifiers.
- **Priority:** High
- **Risk:** High
- **Testability:** Positive and cross-account exclusion tests.

## BR-002 — Date range is inclusive
- **Source evidence:** `PROCTRAN_DATE >= from` and `PROCTRAN_DATE <= to` in count and list cursors.
- **Rule:** Include transactions occurring on either range boundary.
- **Modern interpretation:** Inclusive date predicates.
- **Priority:** High
- **Risk:** Medium
- **Testability:** Lower/upper boundary tests.

## BR-003 — Missing from-date uses legacy minimum sentinel
- **Source evidence:** condition name value `0`; program moves `0`, then converts it to `0000-00-00`.
- **Rule:** Legacy calls with no from-date do not intentionally exclude older transactions.
- **Modern interpretation:** Omitted `fromDate` means no lower bound; this preserves intent without sending an invalid SQL date literal.
- **Priority:** High
- **Risk:** High
- **Testability:** Omitted lower-bound test.

## BR-004 — Missing to-date uses legacy maximum sentinel
- **Source evidence:** condition name value `99999999`; converted to `9999-99-99`.
- **Rule:** Legacy calls with no to-date do not intentionally exclude newer transactions.
- **Modern interpretation:** Omitted `toDate` means no upper bound.
- **Priority:** High
- **Risk:** High
- **Testability:** Omitted upper-bound test.

## BR-005 — Default page limit is 50
- **Source evidence:** `IF INQTRANL-LIMIT = 0 MOVE 50`.
- **Rule:** An unspecified/zero legacy limit returns at most 50 rows.
- **Modern interpretation:** Omitted `limit` defaults to 50. Whether explicit HTTP `limit=0` defaults or is rejected requires approval; proposed contract preserves default behavior by treating 0 as default.
- **Priority:** High
- **Risk:** Medium
- **Testability:** Default and zero-limit tests.

## BR-006 — Maximum page limit is 100
- **Source evidence:** values over 100 are moved to 100; OCCURS 100.
- **Rule:** A single response contains no more than 100 transactions.
- **Modern interpretation:** Clamp values above 100 to 100 to preserve observable behavior.
- **Priority:** High
- **Risk:** Medium
- **Testability:** 100 and >100 tests.

## BR-007 — Offset skips ordered rows
- **Source evidence:** fetch loop increments `WS-SKIP-COUNT` until it equals offset before mapping rows.
- **Rule:** Offset is applied after filtering and ordering, before returned rows are collected.
- **Modern interpretation:** SQL/JDBC pagination may implement the equivalent behavior, provided results match.
- **Priority:** High
- **Risk:** High
- **Testability:** Offset 0, middle page, and beyond-end tests.

## BR-008 — Ordering is newest date/time first
- **Source evidence:** `ORDER BY PROCTRAN_DATE DESC, PROCTRAN_TIME DESC`.
- **Rule:** Transactions are returned by descending date, then descending time.
- **Modern interpretation:** Preserve the two-key ordering. Do not invent a tertiary order in the behavioral contract.
- **Priority:** High
- **Risk:** High
- **Testability:** Multi-date and same-date ordering tests.

## BR-009 — Total count is pre-pagination
- **Source evidence:** separate `COUNT(*)` cursor uses filters but not offset/limit.
- **Rule:** Total count reports all filtered rows; returned count reports only page rows.
- **Modern interpretation:** Page metadata distinguishes total and returned counts.
- **Priority:** High
- **Risk:** Medium
- **Testability:** Dataset larger than page test.

## BR-010 — Empty list is successful
- **Source evidence:** SQLCODE `+100` terminates normally; success is subsequently set to `Y`.
- **Rule:** No matching transactions produces success with zero counts and an empty list.
- **Modern interpretation:** HTTP 200 with empty `transactions`.
- **Priority:** High
- **Risk:** Medium
- **Testability:** No-match test.

## BR-011 — Composite transaction ID is deterministic
- **Source evidence:** concatenated sort code, account, `YYYYMMDD`, time, and reference separated by hyphens.
- **Rule:** Each returned row includes that 5-part ID representation.
- **Modern interpretation:** Return the exact formatted identifier without using it as proof of database uniqueness.
- **Priority:** High
- **Risk:** Medium
- **Testability:** Exact-format mapping test.

## BR-012 — Dates are exposed as YYYYMMDD in the legacy response
- **Source evidence:** DB2 `YYYY-MM-DD` is converted to the 8-digit COMMAREA field.
- **Rule:** The observable legacy transaction date representation is `YYYYMMDD`.
- **Modern interpretation:** API uses 8-character digit strings to avoid changing the evidenced representation.
- **Priority:** Medium
- **Risk:** Medium
- **Testability:** Mapping test.

## BR-013 — Transaction fields mirror PROCTRAN row values
- **Source evidence:** direct moves for sort code, number, time, reference, type, description, amount; date-only conversion.
- **Rule:** Returned values are sourced from the selected database row without business calculations.
- **Modern interpretation:** Mapper trims/preserves database character padding only according to repository convention; padding behavior needs validation.
- **Priority:** High
- **Risk:** Medium
- **Testability:** Field-by-field mapping test.

## BR-014 — Read-only processing
- **Source evidence:** both cursors are `FOR FETCH ONLY`; no write SQL.
- **Rule:** Inquiry does not alter transaction data.
- **Modern interpretation:** GET operation and read-only repository/service transaction.
- **Priority:** High
- **Risk:** Low
- **Testability:** Repository interaction test/no write path.

## BR-015 — DB2 failures are technical failures
- **Source evidence:** nonzero SQLCODE paths link `ABNDPROC` and abend with `ITRL`.
- **Rule:** Database retrieval failures do not return partial success.
- **Modern interpretation:** Return a standardized 500 technical error and no partial transaction page.
- **Priority:** High
- **Risk:** High
- **Testability:** Count/list repository failure tests.
