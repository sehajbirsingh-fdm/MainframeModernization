# Modernization Report — 005B INQTRAND Transaction Detail Inquiry

## Executive Summary

Feature 005B has a well-supported legacy behavior baseline and a repository-aligned modernization design. INQTRAND is an exact, read-only, five-part transaction-detail inquiry. It performs one fetch, treats SQLCODE 100 as a successful not-found result, constructs a deterministic composite transaction ID, and separates technical DB2 failure from normal absence.

The existing application already implements INQTRANL transaction list inquiry. 005B should extend those modern transaction foundations without importing INQTRANL's list-only range, count, ordering, pagination, or modern null-default behavior.

This package is **ready for implementation** under the frozen Plan/Tasks/contract/test/traceability set. Runtime implementation is not completed, code review is not executed, QA is not executed, and executable Quickstart commands/runtime details remain pending direct verification.

## Capability Overview

- **Legacy program:** INQTRAND.
- **Business outcome:** expose a zero-or-one detail result abstraction by exact identity: legacy behavior performs one fetch with all five equality predicates, while supplied production DB2 DDL/index evidence does not prove physical uniqueness of that five-part key.
- **Identity:** sort code + account number + date + time + reference.
- **Mode:** read-only.
- **Normal outcomes:** found success or successful absence.
- **Technical outcome:** DB2/CICS error path with ITRD/ABNDPROC evidence.

## Evidence Reviewed

### Primary/direct legacy evidence
Repository-relative locations established by repository discovery:
- `legacy-bankofz/base/cics/cobol/INQTRAND.cbl`
- `legacy-bankofz/base/cics/copy/INQTRAND.cpy`
- `legacy-bankofz/base/cics/copy/PROCDB2.cpy`

Additional supplied evidence:
- `ABNDINFO.cpy`
- `SORTCODE.cpy`
- system `SQLCA` reference in source.

### Runtime error-path evidence
- supplied `ABNDPROC.cbl`.

### Related legacy evidence
- `legacy-bankofz/base/cics/cobol/INQTRANL.cbl`
- `legacy-bankofz/base/cics/copy/INQTRANL.cpy`
- supplied `PROCTRAN.cpy`

### Modern repository evidence
`repository-discovery-report(7).md`, including current backend/frontend architecture, H2/JDBC, INQTRANL implementation, security, testing and OpenAPI state.

## Current State

The modern repository already contains:
- Java 21 / Spring Boot 3.5.3 backend;
- feature-based vertical slices;
- existing `inqtran` controller/service/repository/mapper/domain code for INQTRANL;
- H2 DB2-mode `PROCTRAN`;
- account-path security with `ACCOUNT_INQUIRER`;
- correlation-ID infrastructure;
- React/TypeScript/Vite transaction UI foundations;
- backend/frontend/E2E and OpenAPI-conformance test patterns.

No runtime INQTRAND detail implementation is confirmed by repository discovery.

## Problems and Constraints

1. Runtime OpenAPI has list inquiry but no confirmed detail implementation, while a broader OpenAPI file contains a historical detail shape.
2. INQTRANL's modern repository defaults null amount to zero; INQTRAND legacy evidence does not support that detail behavior.
3. Frontend transaction client authorization wiring is unresolved against the secured backend.
4. Exact H2 nullability/seed coverage for detail scenarios needs direct implementation-time inspection.
5. Production DB2 DDL/index/CICS routing evidence was not supplied.
6. Exact current startup/test commands and ports were not captured by the read-only discovery report.

## Future State

An authorized user can call:

`GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`

The existing application returns:
- 200 / `found=true` / one transaction when retrievable;
- 200 / `found=false` / `transaction=null` when absent;
- existing structured validation/security/technical errors otherwise.

The feature remains read-only and shares the current transaction subsystem.

## Preserved Behavior

- exact five-part equality identity;
- zero-or-one result;
- successful not-found;
- exact composite ID;
- 8-digit external date representation;
- no list semantics;
- no record-eyecatcher/logical-delete/type filter;
- no unsupported null default;
- technical failures remain distinct.

## Approved Modernization Changes

- CICS COMMAREA → REST/JSON.
- Decomposed five-part path identity.
- `found` + nullable `transaction` response envelope.
- 400 ERR-001 transport-shape validation.
- existing 401/403 security.
- 500 ERR-500 modern technical-error convention with correlation ID.
- structural validation boundary is exact and string-based: `sortCode` 6 digits, `accountNumber` 8 digits, `date` 8 digits, `time` 6 digits, `reference` 12 digits.
- preserve identity strings and leading zeroes with no numeric coercion.
- malformed structural input returns 400 ERR-001.
- no invented Gregorian/calendar date validation, HHMMSS semantic clock-range validation, account-existence validation, or transaction-reference business validation.
- reuse current `inqtran`, JDBC/H2, frontend and testing structures.
- remain within `com.bankofz.mainframemodernization.inqtran`; extend existing `TransactionRepository` and `JdbcTransactionRepository`; reuse existing H2 DB2-mode `PROCTRAN`; reuse existing transaction frontend/client structure; avoid an unnecessary parallel top-level `inqtrand` backend/repository hierarchy.
- feature contract drives runtime OpenAPI reconciliation.

These are modernization choices, not claims about the legacy interface.

## Architecture Summary

```text
Existing React transaction feature
        |
        v
existing/extended transaction client
        |
        v
secured account detail GET
        |
        v
existing inqtran controller/service boundary
        |
        v
extended transaction repository
        |
        v
JDBC -> H2 PROCTRAN
```

No second backend, frontend or transaction database is planned.

Implementation guardrail: modern zero-or-one behavior must not be presented as proof of production DB2 physical uniqueness. Do not add arbitrary ordering, silently select a first duplicate, or invent duplicate-resolution semantics; if duplicate physical matches are discovered during implementation/testing, record a data/integration issue requiring explicit resolution.

## Business Value

No quantified savings or business impact were supplied, so none are invented. The evidence-supported value of the design is structural:
- preserves a verified legacy inquiry outcome;
- reuses an already-established modern transaction foundation;
- limits implementation scope to one detail capability;
- gives SMEs explicit traceability for preserved behavior and modernization decisions;
- makes normal absence, technical failure and list/detail behavior separately testable.

## Risk Assessment

| Risk | Severity | Mitigation |
|---|---|---|
| Historical vs runtime OpenAPI ambiguity | Medium | feature contract + T012/T013 runtime reconciliation |
| Detail null behavior accidentally copied from list | High | BR-012, FR-010, T003/T006, TS-016 |
| Frontend auth gap | Medium | T001/T014; no hard-coded credential |
| H2 seed/nullability uncertainty | Medium | T004 before persistence implementation |
| INQTRANL regression | Medium | T019 regression gate |
| Missing production DDL/CICS definitions | Low for POC / future risk | keep adapter boundary; record uncertainty |

## Complexity Assessment

**Moderate.** The core lookup is simple, but parity-sensitive behavior spans persistence, transport semantics, security, frontend integration, OpenAPI, and regression. Complexity comes primarily from careful reuse and avoiding behavioral drift, not from algorithmic complexity.

## Delivery Approach

Implement incrementally in the frozen task order:
1. re-inspect repository/runtime setup;
2. define/reuse detail models;
3. implement exact repository lookup and tests;
4. implement service/controller/security and tests;
5. reconcile OpenAPI;
6. integrate frontend and tests;
7. execute E2E/regression;
8. complete code review, QA, traceability and verified Quickstart.

## Roadmap

- **Milestone 1:** T001-T006 — repository/persistence foundation.
- **Milestone 2:** T007-T013 — backend behavior, security, contract.
- **Milestone 3:** T014-T018 — frontend detail integration.
- **Milestone 4:** T019-T020 — regression, evidence, review and QA.

No duration estimate is asserted because no delivery estimate evidence was supplied.

## Implementation Readiness

| Area | Status |
|---|---|
| Legacy behavior baseline | Ready |
| Dependency analysis | Ready |
| Business rules/mappings | Ready |
| Target architecture/research | Ready |
| Requirements/specification | Ready |
| Plan/tasks | Ready |
| Feature OpenAPI | Ready |
| Test plan/traceability | Ready |
| Runtime implementation | Not started by this package |
| Code review | Pending implementation |
| QA execution | Pending implementation |
| Executable Quickstart commands | Pending direct runtime verification |

## Assumptions

- H2 PROCTRAN remains the POC persistence store.
- Existing repository conventions remain the integration target.
- Existing INQTRANL list rows expose/provide the five identity components in usable form for required list-row -> detail navigation -> existing transaction client -> approved INQTRAND endpoint -> found-detail or successful-absence presentation; existing INQTRANL list behavior remains unchanged.
- No production mainframe connection is required for 005B POC.

## Open Questions

1. What is the approved frontend authentication/token source for current secured APIs?
2. What are the exact current H2 nullability constraints and seed records?
3. What exact run/test commands and ports are current?
4. What production DB2 constraints/indexes exist beyond supplied declaration evidence?
5. What inbound CICS transaction/caller definition invokes legacy INQTRAND?

## SME Validation

A mainframe/legacy SME should validate at minimum:
- five-part identity and exact lookup;
- successful SQLCODE 100 semantics;
- date rearrangement/no calendar pre-validation;
- composite transaction ID;
- technical failure path;
- absence of list semantics;
- absence of eyecatcher/delete/type filtering;
- null-indicator interpretation.

An application/Java SME should validate repository integration, especially reuse boundaries, runtime OpenAPI, auth wiring, H2 schema behavior and regression impact.

## Scope Confirmation

INQTRANL has not been re-modernized. No list semantics have been attributed to INQTRAND. No application implementation code was generated or modified by this package.


## Artifact Relationships

- **Upstream Inputs:** `supporting/program-analysis.md`, `supporting/dependency-map.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `supporting/architecture.md`, `research.md`, `data-model.md`, `supporting/requirements.md`, `spec.md`, `plan.md`, `tasks.md`, `contracts/openapi.yaml`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`, `checklists/code-review-checklist.md`, `checklists/qa-review-checklist.md`, `quickstart.md`, repository evidence.
- **Downstream Consumers:** Client/SME review, implementation readiness decision, `supporting/copilot-build-prompt.md`.
- **Authority Boundary:** Authoritative as a client-facing synthesis only; it is not a new requirements source.
- **Conflict Handling:** Detailed frozen source artifacts win on conflict and this report must be corrected; implementation status or local runtime behavior cannot be used to silently redefine approved Requirements/Specification/OpenAPI behavior.
