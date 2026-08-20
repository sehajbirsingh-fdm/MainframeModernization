# Supporting Requirements — 005B INQTRAND Transaction Detail Inquiry

## Executive Summary
Modernize INQTRAND as an exact read-only five-part transaction detail inquiry within the existing application. Preserve successful absence and exclude list semantics.

## Business Context
A permitted user needs the details of one transaction identified by the same components used by legacy INQTRAND.

## Scope
Backend detail lookup, API, modern result, minimal frontend integration, security/observability reuse, tests, runtime OpenAPI reconciliation.

## Out of Scope
INQTRANL reimplementation, transaction mutation, list filtering/pagination/count/order, real mainframe connectivity, unrelated transaction management.

## Functional Requirements
- **FR-001:** require all five transaction identity components with exact transport shapes: `sortCode` exactly 6 digits, `accountNumber` exactly 8 digits, `date` exactly 8 digits, `time` exactly 6 digits, `reference` exactly 12 digits.
- **FR-002:** use all five for one exact read-only lookup; no list semantics.
- **FR-003:** matching retrievable row → `found=true` + one transaction.
- **FR-004:** no matching row → successful absence with HTTP `200`, `found=false`, and `transaction=null` (not `404` and not technical failure).
- **FR-005:** found detail fields: transactionId, sortCode, accountNumber, date, time, reference, type, description, amount.
- **FR-006:** ID = `sortCode-accountNumber-date-time-reference`.
- **FR-007:** external identity-component validation is structural only; do not add calendar-date semantic validation, HHMMSS semantic time-range validation, account-existence validation, or transaction-reference business validation.
- **FR-008:** no create/update/delete.
- **FR-009:** do not add record-eyecatcher/logical-delete/type predicates without new authority.
- **FR-010:** do not silently convert nullable detail data to invented zero/blank values.
- **FR-011:** preserve exact identity-component digit widths and leading zeros for `sortCode`, `accountNumber`, `date`, `time`, and `reference`; do not broaden this into a requirement to preserve COBOL fixed-CHAR padding for all modern JSON detail strings.
- **FR-012:** expose `GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`.
- **FR-013:** existing transaction UI must support list-row to detail integration using the five transaction identity components, calling the existing transaction API client and the INQTRAND detail endpoint, and presenting found-detail or successful-absence states.

## Non-Functional Requirements
- **NFR-001:** remain in the existing `com.bankofz.mainframemodernization.inqtran` vertical slice; extend existing `TransactionRepository` and `JdbcTransactionRepository`; reuse existing H2/`PROCTRAN`/JDBC foundations; do not introduce a parallel `inqtrand` repository hierarchy unless later architectural evidence requires it.
- **NFR-002:** exact decimal precision.
- **NFR-003:** prepared SQL and safe configured identifiers.
- **NFR-004:** tests at touched backend/frontend/contract/E2E layers.
- **NFR-005:** existing correlation-ID/logging convention.

## Security Requirements
- **SEC-001:** stay within secured `/api/v1/accounts/**`.
- **SEC-002:** require existing `ACCOUNT_INQUIRER` role.
- **SEC-003:** no separate auth or hard-coded frontend credential.

## Operational Requirements
- **OPS-001:** syntax/shape validation uses existing 400 `ERR-001`.
- **OPS-002:** technical failure uses existing 500 `ERR-500` with correlation ID.
- **OPS-003:** reconcile runtime OpenAPI with feature contract.
- **OPS-004:** continue H2 DB2-mode POC; production DB2 connection is out of scope.

## Compatibility and Legacy-Preservation Requirements
- **COMP-001:** preserve exact five-part identity.
- **COMP-002:** preserve successful not-found.
- **COMP-003:** preserve composite transaction ID.
- **COMP-004:** exclude list semantics.
- **COMP-005:** avoid unproven validity filters/defaulting.

## Approved Modernization Requirements
- **MOD-001:** COMMAREA operation becomes REST/JSON.
- **MOD-002:** five decomposed path segments are authoritative input.
- **MOD-003:** response envelope has `found` + nullable `transaction`.
- **MOD-004:** approved integration direction is to extend existing `inqtran` backend/frontend foundations, including the existing transaction repository boundary, rather than creating a parallel detail repository subsystem.
- **MOD-005:** reuse security/correlation.
- **MOD-006:** feature contract drives 005B runtime OpenAPI; historical broad detail endpoint is not automatically adopted.

## Assumptions
Existing H2 PROCTRAN remains POC store; repository paths remain available; list rows contain (or can supply) decomposed five-part identity to support required detail navigation.

## Risks
OpenAPI ambiguity, list null-default incompatibility, frontend auth gap, seed sufficiency, missing production DDL.

## Requirement Dependencies
| Group | Upstream |
|---|---|
| Confirmed legacy-behavior obligations (read-only exact five-part lookup, found vs successful absence semantics, at-most-one detail, no list semantics, no unapproved predicates/defaulting) | supporting/program-analysis.md, supporting/business-rules.md, supporting/mapping-matrix.md |
| Approved modern behavior obligations (exact transport shapes, explicit successful-absence transport, endpoint contract, mandatory list-to-detail integration) | supporting/intended-system.md, research.md |
| Architecture/integration obligations (existing slice/repository extension, H2/PROCTRAN/JDBC reuse, no parallel hierarchy without demonstrated need) | supporting/architecture.md, repository evidence |
| Model/representation obligations (detail/result structure, transactionId composition, mapping semantics and resolved representation decisions) | supporting/mapping-matrix.md, data-model.md, supporting/intended-system.md, research.md, supporting/architecture.md |
| NFR-* (except where explicitly tightened above) | Architecture/repository conventions |
| SEC-* | repository security |
| OPS-* | repository error/OpenAPI/H2 conventions |
| COMP-* | confirmed legacy behavior |
| MOD-* | approved modernization decisions |


## Artifact Relationships

- **Upstream Inputs:** `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `supporting/architecture.md`, `research.md`, `data-model.md`.
- **Downstream Consumers:** `spec.md`, `plan.md`, `tasks.md`, `contracts/openapi.yaml`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`, review checklists.
- **Authority Boundary:** Authoritative for uniquely identified implementation obligations.
- **Conflict Handling:** Confirmed legacy behavior/upstream scope wins; assumptions remain non-mandatory until approved.
