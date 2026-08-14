# Data Model — 00B INQTRAND Transaction Detail Inquiry

## Purpose
Define conceptual/query/result structures for zero-or-one detail lookup.

## Conceptual Domain Model
```text
TransactionKey 1 ---- identifies ---- 0..1 TransactionDetail
                                      |
                                      `-- derived TransactionId
```

## Entities and Value Objects
### TransactionKey
| Field | Format | Mapping |
|---|---|---|
| `sortCode` | 6 digits | MM-001 |
| `accountNumber` | 8 digits | MM-002 |
| `date` | 8 digits | MM-003 |
| `time` | 6 digits | MM-004 |
| `reference` | 12 digits | MM-005 |

Representation constraints do not imply calendar/time semantic validity.

### TransactionDetail
| Field | Type | Mapping |
|---|---|---|
| `transactionId` | string | MM-015 |
| `sortCode` | string | MM-007 |
| `accountNumber` | string | MM-008 |
| `date` | string | MM-009 |
| `time` | string | MM-010 |
| `reference` | string | MM-011 |
| `type` | string | MM-012 |
| `description` | string | MM-013 |
| `amount` | exact decimal | MM-014 |

### TransactionDetailResult
- `found: boolean`
- `transaction: TransactionDetail | null`

Invariant: found=true → one transaction; found=false → transaction=null.

## Relationships and Cardinality
One exact key returns zero or one transaction. No collection belongs to 00B.

## Query and Result Models
Only the five key fields are query inputs. Range dates, counts, sort order, limit and offset belong to INQTRANL and are excluded.

## Derived Fields
`transactionId = sortCode + "-" + accountNumber + "-" + date + "-" + time + "-" + reference` (44 material chars).

## Validation Constraints
External digit widths only; type remains at legacy width 3 when returned; no closed enum.

## Type and Format Rules
Keys are strings; date is 8-digit legacy-compatible; amount exact decimal; fixed CHAR padding may be stripped only as representation.

Semantic null/default constraint:
- transaction-detail data must not silently convert an absent/null source value into an invented business value (for example zero) unless separately justified and approved;
- INQTRANL list-specific null-to-zero behavior is not automatically inherited;
- this is a target semantic constraint and does not require reproducing legacy SQL indicator-variable mechanics.

## Persistence Representation
Repository discovery reports H2 `PROCTRAN` composite PK:
`PROCTRAN_SORTCODE`, `PROCTRAN_NUMBER`, `PROCTRAN_DATE`, `PROCTRAN_TIME`, `PROCTRAN_REF`.
Exact schema/nullability must be inspected before coding.

## DTO Representation
Use `TransactionDetailResult` envelope. Reuse existing transaction records/types when semantics align; do not duplicate by default.

## Legacy-to-Modern Mapping References
`supporting/mapping-matrix.md` is authoritative for legacy-to-modern mapping semantics and evidence boundaries.
Approved target representation decisions are consumed from `supporting/intended-system.md`, `research.md`, and `supporting/architecture.md` where Mapping Matrix items were intentionally deferred.

Examples:
- MM-016 preserves legacy found/not-found semantic state; Intended System approves the explicit modern `found` result-envelope representation.
- MM-017 preserves legacy success/technical-failure semantics; Intended System approves modern transport behavior for successful absence and technical-failure separation.

No external field is invented for DB/COMMAREA eyecatchers.

## Model Diagram
```text
TransactionKey(5 fields)
        |
     lookup
        v
TransactionDetailResult
  found=false -> transaction=null
  found=true  -> TransactionDetail(9 fields)
```

## Open Questions
Is existing `TransactionRecord` semantically compatible with the approved detail model while preserving detail-specific semantics and avoiding inherited list-only null/default behavior, or is the minimum necessary detail-specific representation required? Exact H2 nullability and seed behavior need inspection.


## Artifact Relationships

- **Upstream Inputs:** `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `supporting/architecture.md`, `research.md`.
- **Downstream Consumers:** `supporting/requirements.md`, `spec.md`, `plan.md`, `contracts/openapi.yaml`, `tasks.md`.
- **Authority Boundary:** Authoritative for approved conceptual/query/result model and format constraints.
- **Conflict Handling:** Mapping Matrix and approved behavior win over implementation class convenience.
