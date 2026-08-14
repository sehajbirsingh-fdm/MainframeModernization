# Mapping Matrix — 00B INQTRAND Transaction Detail Inquiry

## Purpose
Trace INQTRAND input → host/DB2 → output → approved modern representation without skipping intermediate transformations.

## Mapping Conventions
Legacy paths are evidence. Modern target names are **Approved Modernization Decisions**. External key values remain strings to preserve fixed width/leading zeros.

MM-001 through MM-005 describe modern lookup/input key representations.
MM-007 through MM-014 describe modern found-detail output representations for a successfully fetched row.

## Field Mappings
| ID | Legacy source | Intermediate / predicate | Legacy output | Approved target | Notes |
|---|---|---|---|---|---|
| MM-001 | sort code `9(6)` | host → `PROCTRAN_SORTCODE =` | sort code | `sortCode` string | 6 digits |
| MM-002 | account `9(8)` | host → `PROCTRAN_NUMBER =` | account | `accountNumber` string | 8 digits |
| MM-003 | date `9(8)` | reshape → DB2 date equality | reshape back | `date` string | 8 digits; no calendar rule |
| MM-004 | time `9(6)` | host → `PROCTRAN_TIME =` | time | `time` string | 6 digits |
| MM-005 | reference `9(12)` | host → `PROCTRAN_REF =` | reference | `reference` string | 12 digits |
| MM-006 | selected DB eyecatcher | fetch host | not transaction output | not exposed | no invented field |
| MM-007 | selected sort code | fetch | detail | `transaction.sortCode` | key component; found-row path only (Confirmed Legacy Evidence) |
| MM-008 | selected number | fetch | detail | `transaction.accountNumber` | key component; found-row path only (Confirmed Legacy Evidence) |
| MM-009 | selected date | DB date → YYYYMMDD | detail | `transaction.date` | 8 digits; found-row path only (Confirmed Legacy Evidence) |
| MM-010 | selected time | fetch | detail | `transaction.time` | 6 digits; found-row path only (Confirmed Legacy Evidence) |
| MM-011 | selected ref | fetch | detail | `transaction.reference` | 12 digits; found-row path only (Confirmed Legacy Evidence) |
| MM-012 | type CHAR(3) | fetch | detail type | `transaction.type` | opaque 3-char string; found-row path only (Confirmed Legacy Evidence) |
| MM-013 | desc CHAR(40) | fetch | detail desc | `transaction.description` | found-row path only; any trailing-padding trim is an Approved Modernization Decision, not a legacy transformation |
| MM-014 | amount DECIMAL(12,2) | fetch | `S9(10)V99` | exact decimal / BigDecimal | found-row path only; no null→0 default (Confirmed Legacy Evidence) |
| MM-015 | five key parts | hyphen construction | ID X(50) | `transaction.transactionId` | 44 material chars; `sortCode-accountNumber-date-time-reference` |
| MM-016 | FOUND | SQLCODE branch | Y/N | found/not-found semantic state | Confirmed Legacy Evidence is semantic state only; explicit wire boolean is a separate representation decision |
| MM-017 | SUCCESS | Y for found/no-row | Y/N | success/technical-failure semantic state | Confirmed Legacy Evidence is control semantics only; exact HTTP/transport representation is deferred to API modernization authority |

## Control Field Mappings
COMMAREA eyecatcher is protocol state and is not exposed. FOUND and SUCCESS carry legacy semantic/control state; any explicit modern wire fields for those states are representation decisions owned by authoritative modernization/API artifacts.

## Repeating Structures and Cardinality
Zero-or-one detail. No list array.

## Type and Format Mismatches
- COBOL display-numeric identity → strings externally.
- DB2 DATE → 8-digit legacy-compatible string externally.
- fixed CHAR padding may be removed only as representation.
- amount uses exact decimal, never floating-point approximation.

## Nullability and Indicator Handling
`PROCDB2.cpy` allows null on several selected columns; INQTRAND supplies no null indicators. Modern detail handling must not inherit INQTRANL's repository `null amount -> BigDecimal.ZERO` behavior. If a matched row cannot be read due to null-without-indicator semantics, preserve technical failure unless new authority explicitly changes that behavior.

## Provisional / Approved Modern Target Mappings
The target names in MM-001..MM-017 are approved for this feature package, not legacy claims.

## Uncertainty Register
- Exact current H2 nullability must be re-inspected during implementation.
- Fixed CHAR trailing-space semantics beyond representation are not proven.


## Artifact Relationships

- **Upstream Inputs:** `supporting/program-analysis.md`, `supporting/business-rules.md`, `INQTRAND.cpy`, `PROCDB2.cpy`, `PROCTRAN.cpy`.
- **Downstream Consumers:** `supporting/intended-system.md`, `data-model.md`, `supporting/requirements.md`, `spec.md`, `contracts/openapi.yaml`, `supporting/traceability-matrix.md`.
- **Authority Boundary:** Authoritative for approved legacy-to-modern data mapping and preserved uncertainties.
- **Conflict Handling:** Legacy source owns legacy layout/transformation truth; downstream artifacts may not invent/default fields silently.
