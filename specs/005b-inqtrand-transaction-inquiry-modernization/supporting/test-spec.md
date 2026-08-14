# Test Specification — 00B INQTRAND Transaction Detail Inquiry

## Purpose
Define pre-implementation verification scenarios for approved behavior. No scenario is marked passed until executed evidence exists.

## Test Scope
Backend domain/service/repository/controller/security, OpenAPI, frontend client/UI, E2E, and INQTRANL regression.

## Test Levels
Unit, repository integration with H2, controller/MockMvc, security slice, contract conformance, frontend unit/component, Playwright E2E, regression.

## Test Data Strategy
Use deterministic current H2 records after T004 inspection. Add only minimal seed rows needed for:
- known found detail;
- not-found key;
- leading-zero identity if not already present;
- nullable/detail technical behavior where schema permits.

Do not invent production DB2 data assumptions.

## Functional Scenarios

| ID | Scenario | Expected | Coverage |
|---|---|---|---|
| TS-001 | exact known five-part key | 200, found=true, one detail | FR-001..FR-005, BR-002, BR-004 |
| TS-002 | complete key with no row | 200, found=false, transaction=null | FR-004, BR-005 |
| TS-003 | verify transactionId | exact 46-char five-part hyphenation | FR-006, BR-006 |
| TS-004 | verify all detail fields | nine approved fields, exact mappings | FR-005, BR-007, MM-007..MM-015 |
| TS-005 | read-only request | no mutation SQL/state change | FR-008, BR-009 |

## Validation Scenarios
| ID | Scenario | Expected |
|---|---|---|
| TS-006 | sortCode not 6 digits | 400 ERR-001 |
| TS-007 | accountNumber not 8 digits | 400 ERR-001 |
| TS-008 | date not 8 digits | 400 ERR-001 |
| TS-009 | time not 6 digits | 400 ERR-001 |
| TS-010 | reference not 12 digits | 400 ERR-001 |
| TS-011 | non-digit path component | 400 ERR-001 |
| TS-012 | width-valid unusual date/time | not rejected by new semantic calendar/clock validation |

## Boundary Scenarios
- **TS-013:** leading-zero key round-trips through URL, repository parameters, response and transactionId.
- **TS-014:** max legacy description/type widths map without extra enum filtering.

## Empty-Result Scenarios
- **TS-002** is mandatory at repository, service, controller and E2E/API levels where practical.
- Assert absence is not 404 or 500.

## Technical Failure Scenarios
- **TS-015:** repository SQL exception → 500 ERR-500 with correlationId.
- **TS-016:** matched row with unsupported nullable selected data, where reproducible → technical path, not fabricated zero/blank result.

## Persistence Scenarios
- **TS-017:** inspect/assert generated SQL semantics use all five equality predicates and no count/order/pagination/date-range clauses.
- **TS-018:** assert no eyecatcher/logical-delete/type predicate is introduced.

## API Contract Scenarios
- **TS-019:** feature and runtime OpenAPI include exact approved path, five required regex-constrained path parameters, 200/400/401/403/500, and correct found envelope.

## Frontend Scenarios
- **TS-020:** client creates exact detail URL while preserving leading zeros and maps found/not-found/error.
- **TS-021:** component renders loading, found detail, not-found and technical error states.

## E2E Scenarios
- **TS-022:** authorized user exercises detail end-to-end for found and not-found; if frontend auth configuration blocks execution, record BLOCKED with exact evidence rather than pass.

## Security and Operational Scenarios
- unauthenticated request → 401;
- authenticated without role → 403;
- ACCOUNT_INQUIRER → permitted;
- response/error correlation ID behavior follows existing conventions;
- no hard-coded feature credential.

## Regression Scenarios
Existing INQTRANL list tests and E2E behavior must remain unchanged, including list date-range/pagination behavior. 00B must not alter list semantics.

## Traceability

| Tests | Requirements / rules |
|---|---|
| TS-001..TS-005 | FR-001..FR-008, BR-002..BR-009 |
| TS-006..TS-014 | FR-007, FR-011, OPS-001 |
| TS-015..TS-016 | FR-010, OPS-002, BR-008, BR-012 |
| TS-017..TS-018 | FR-002, FR-009, BR-010, BR-011 |
| TS-019 | FR-012, OPS-003, MOD-006 |
| TS-020..TS-022 | FR-013, SEC-*, MOD-004..MOD-005 |

## Evidence Requirements
For each executed scenario record:
- test command/suite;
- test case name;
- pass/fail/blocked;
- relevant logs/screenshots/reports where appropriate;
- defect reference for failure;
- commit/build reference.

Planned scenarios are not execution evidence.


## Artifact Relationships

- **Upstream Inputs:** `supporting/requirements.md`, `spec.md`, `supporting/business-rules.md`, `contracts/openapi.yaml`, `data-model.md`, `tasks.md`.
- **Downstream Consumers:** `supporting/traceability-matrix.md`, `checklists/qa-review-checklist.md`, `supporting/copilot-build-prompt.md`.
- **Authority Boundary:** Authoritative for planned verification coverage and expected results, not executed status.
- **Conflict Handling:** Specification/Requirements define behavior; test results must report mismatches rather than changing expectations.
