# Business Rules — 00B INQTRAND Transaction Detail Inquiry

## Purpose and Ownership
Record observable legacy-derived behavior without target implementation or HTTP semantics.

## Rule Classification
BR-001 through BR-012 are **Confirmed Legacy Evidence**.

## Confirmed Business Rules
### BR-001 — COMMAREA eyecatcher normalization
If inbound eyecatcher is not `ITRD`, overwrite only `INQTRAND-EYE` with `ITRD`; no business rejection is exposed. The source does not establish full INQTRAND structure initialization or clearing at this point.

### BR-002 — Exact five-part transaction lookup
Match sort code, account number, date, time, and reference using equality on all five.

### BR-003 — Date reshaping only
Rearrange `YYYYMMDD` ↔ `YYYY-MM-DD` by character position. No pre-query calendar-validity rule exists.

### BR-004 — Found transaction is successful
A matching fetch sets SUCCESS=`Y`, FOUND=`Y`, and returns transaction detail.

### BR-005 — Missing transaction is successful
SQLCODE 100 sets SUCCESS=`Y` while FOUND remains `N`; absence is not a technical error.

Before lookup, INQTRAND explicitly initializes only SUCCESS and FOUND. On SQLCODE 100, transaction-detail output fields are not explicitly cleared or repopulated. Therefore, legacy evidence does not establish those detail fields as blank, zero, null, or otherwise reset for not-found outcomes.

### BR-006 — Composite transaction identifier
For a found row, ID = `sortCode-accountNumber-date-time-reference`.

### BR-007 — Returned detail fields
For a successfully fetched row, return transactionId, sortCode, accountNumber, date, time, reference, type, description, and amount.

### BR-008 — DB2 technical failure path
Cursor open/close non-zero SQLCODE and fetch SQLCODE other than 0/100 route to technical abend handling.

### BR-009 — Read-only behavior
The cursor is `FOR FETCH ONLY`; no transaction mutation occurs.

### BR-010 — No list semantics
No date-range filtering, pagination, total/returned counts, ordering, limit/offset, or repeating transaction array belongs to INQTRAND.

### BR-011 — No record-validity/type filter
Lookup does not filter on PROCTRAN eyecatcher, logical delete representation, or transaction type.

### BR-012 — No SQL NULL defaulting
Fetch uses no null indicators for nullable selected columns; the source establishes no default of null amount to zero or null text to blank.

## Inferred Business Intent
- **BI-001 (Reasonable Inference):** the five-part key is intended as one transaction identity.
- **BI-002 (Reasonable Inference):** type is opaque data because INQTRAND returns it without validating a closed set.

## Remaining Uncertainties
- External malformed-input/user-facing behavior is not defined by the COMMAREA program.
- Caller display behavior for successful absence is not supplied.
- Production nullable data conditions are unknown.

## Rule-to-Evidence Matrix
| Rule | Evidence |
|---|---|
| BR-001 | `INQTRAND.cbl`, `INQTRAND.cpy` |
| BR-002 | cursor equality predicates |
| BR-003 | date conversion paragraphs |
| BR-004 | SQLCODE 0 branch |
| BR-005 | output-flag initialization + SQLCODE 100 branch |
| BR-006 | ID construction |
| BR-007 | SQLCODE 0 branch + SELECT list + COMMAREA outputs |
| BR-008 | SQLCODE handling + abend routine |
| BR-009 | `FOR FETCH ONLY` |
| BR-010 | complete INQTRAND flow; INQTRANL contrast |
| BR-011 | INQTRAND predicates + related `PROCTRAN.cpy` |
| BR-012 | `PROCDB2.cpy` + FETCH without indicators |


## Artifact Relationships

- **Upstream Inputs:** `supporting/program-analysis.md`, `supporting/dependency-map.md`, supplied legacy source.
- **Downstream Consumers:** `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `supporting/requirements.md`, `spec.md`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`.
- **Authority Boundary:** Authoritative for observable legacy-derived rules and classifications.
- **Conflict Handling:** Primary source wins; modernization/API choices may not be retroactively labeled legacy business rules.
