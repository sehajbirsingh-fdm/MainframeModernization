# Supporting Requirements — 00B INQTRAND Transaction Detail Inquiry

## Executive Summary
Modernize INQTRAND as an exact read-only five-part transaction detail inquiry within the existing application. Preserve successful absence and exclude list semantics.

## Business Context
A permitted user needs the details of one transaction identified by the same components used by legacy INQTRAND.

## Scope
Backend detail lookup, API, modern result, minimal frontend integration, security/observability reuse, tests, runtime OpenAPI reconciliation.

## Out of Scope
INQTRANL reimplementation, transaction mutation, list filtering/pagination/count/order, real mainframe connectivity, unrelated transaction management.

## Functional Requirements
- **FR-001:** require sortCode, accountNumber, date, time, reference.
- **FR-002:** use all five for one exact read-only lookup; no list semantics.
- **FR-003:** matching retrievable row → `found=true` + one transaction.
- **FR-004:** no matching row → successful `found=false` + no transaction object.
- **FR-005:** found detail fields: transactionId, sortCode, accountNumber, date, time, reference, type, description, amount.
- **FR-006:** ID = `sortCode-accountNumber-date-time-reference`.
- **FR-007:** external date uses 8-digit legacy-compatible representation; no new calendar-validity business rule.
- **FR-008:** no create/update/delete.
- **FR-009:** do not add record-eyecatcher/logical-delete/type predicates without new authority.
- **FR-010:** do not silently convert nullable detail data to invented zero/blank values.
- **FR-011:** retain fixed widths and leading zeros.
- **FR-012:** expose `GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`.
- **FR-013:** existing transaction UI can request and present found/not-found detail.

## Non-Functional Requirements
- **NFR-001:** repository-first integration; no parallel app/hierarchy.
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
- **MOD-004:** reuse existing `inqtran` backend/frontend foundations when compatible.
- **MOD-005:** reuse security/correlation.
- **MOD-006:** feature contract drives 00B runtime OpenAPI; historical broad detail endpoint is not automatically adopted.

## Assumptions
Existing H2 PROCTRAN remains POC store; repository paths remain available; list rows contain decomposed identity for optional navigation.

## Risks
OpenAPI ambiguity, list null-default incompatibility, frontend auth gap, seed sufficiency, missing production DDL.

## Requirement Dependencies
| Group | Upstream |
|---|---|
| FR-001..FR-011 | BR-002..BR-012, MM-001..MM-017 |
| FR-012..FR-013 | Intended System, Research, repository evidence |
| NFR-* | Architecture/repository conventions |
| SEC-* | repository security |
| OPS-* | repository error/OpenAPI/H2 conventions |
| COMP-* | confirmed legacy behavior |
| MOD-* | approved modernization decisions |


## Artifact Relationships

- **Upstream Inputs:** `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `supporting/architecture.md`, `research.md`, `data-model.md`.
- **Downstream Consumers:** `spec.md`, `plan.md`, `tasks.md`, `contracts/openapi.yaml`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`, review checklists.
- **Authority Boundary:** Authoritative for uniquely identified implementation obligations.
- **Conflict Handling:** Confirmed legacy behavior/upstream scope wins; assumptions remain non-mandatory until approved.
