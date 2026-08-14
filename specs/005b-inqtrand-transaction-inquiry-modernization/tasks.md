# Tasks — 00B INQTRAND Transaction Detail Inquiry

## Execution Rules
- Execute in dependency order.
- Re-open current repository files before modifying them.
- Do not create a parallel backend/frontend.
- Do not change frozen behavior to fit existing implementation.
- Record verification evidence for every task.
- Stop and report if authoritative artifacts conflict.

## Dependency Overview
```text
T001
 -> T002 -> T003 -> T004
 -> T005 -> T006
 -> T007 -> T008 -> T009
 -> T010 -> T011
 -> T012 -> T013 -> T014
 -> T015 -> T016 -> T017
 -> T018 -> T019 -> T020
```

## Repository Inspection

### T001 — Reconfirm exact extension points
- **Dependencies:** none.
- **References:** Plan repository summary; Architecture.
- **Requirements:** NFR-001, OPS-003, SEC-003.
- **Work:** inspect current inqtran Java classes/tests, schema/data, both OpenAPI files, frontend transaction files, package scripts/README, and security setup.
- **Done:** exact files/classes/commands/ports and auth mechanism are recorded; no contradictions ignored.
- **Evidence:** inspection notes/commit diff references.

## Backend Domain and Mapping

### T002 — Define/reuse detail query and result model
- **Dependencies:** T001.
- **References:** Data Model; MM-001..MM-017.
- **Requirements:** FR-001, FR-003..FR-007, FR-011.
- **Done:** model supports exact key, found envelope, nine fields, exact ID without list-only fields.

### T003 — Protect detail mapping semantics
- **Dependencies:** T002.
- **References:** BR-006, BR-012; MM-012..MM-015.
- **Requirements:** FR-005, FR-006, FR-010, NFR-002.
- **Done:** exact decimal/ID/type behavior; no null→zero/blank default for detail.

## Persistence

### T004 — Verify H2 PROCTRAN schema and seeds
- **Dependencies:** T001.
- **Requirements:** OPS-004, FR-010.
- **Done:** current column types/nullability/PK and deterministic detail cases documented; seed changes identified only if required.

### T005 — Add exact repository lookup
- **Dependencies:** T002, T004.
- **References:** BR-002, BR-009..BR-012.
- **Requirements:** FR-002, FR-008..FR-010, NFR-003.
- **Done:** prepared SQL constrains all five keys; no count/order/pagination/range/eyecatcher/delete/type filter.

### T006 — Repository tests
- **Dependencies:** T005.
- **Requirements:** FR-002..FR-004, FR-009..FR-011, NFR-004.
- **Done:** found, absent, leading-zero, exact-key, and relevant null/error scenarios tested.

## Service Layer

### T007 — Add detail service behavior
- **Dependencies:** T003, T005.
- **Requirements:** FR-003, FR-004, FR-006, FR-010.
- **Done:** optional row → found envelope; absence stays success; repo failures become technical failure.

### T008 — Service tests
- **Dependencies:** T007.
- **Done:** found/not-found/technical and ID behavior verified.

## Controller and API

### T009 — Add detail GET controller operation
- **Dependencies:** T007.
- **Requirements:** FR-012, FR-011, OPS-001, OPS-002.
- **Done:** approved path and width validation; no list query params.

### T010 — Controller/error tests
- **Dependencies:** T009.
- **Done:** 200 found, 200 absence, 400 validation, 500 technical and correlation response verified.

### T011 — Security tests
- **Dependencies:** T009.
- **Requirements:** SEC-001, SEC-002.
- **Done:** unauthenticated 401, unauthorized 403, authorized ACCOUNT_INQUIRER success.

## Runtime OpenAPI

### T012 — Reconcile runtime OpenAPI
- **Dependencies:** T009.
- **References:** `contracts/openapi.yaml`.
- **Requirements:** OPS-003, MOD-006.
- **Done:** runtime resources OpenAPI contains contract-equivalent path/schemas; historical broad path is not substituted.

### T013 — Extend OpenAPI conformance test
- **Dependencies:** T012.
- **Done:** test checks path, parameters, response codes, required schemas/constraints.

## Frontend Client

### T014 — Add transaction-detail client/type support
- **Dependencies:** T001, T012.
- **Requirements:** FR-013, SEC-003.
- **Done:** exact five-part URL encoding; found envelope types; approved auth mechanism reused or blocker recorded.

### T015 — Frontend client tests
- **Dependencies:** T014.
- **Done:** URL, leading zeros, found/not-found/error parsing verified.

## Frontend UI

### T016 — Add detail route/view
- **Dependencies:** T014.
- **Requirements:** FR-013.
- **Done:** found detail, not-found, loading and error states rendered in existing transaction feature.

### T017 — Add optional list-to-detail navigation
- **Dependencies:** T016.
- **Requirements:** MOD-004.
- **Done:** if implemented, uses decomposed key and does not alter INQTRANL inquiry behavior.

### T018 — Frontend component tests
- **Dependencies:** T016, T017.
- **Done:** key user states and navigation covered.

## E2E / Regression / Documentation

### T019 — Execute backend, frontend, E2E and INQTRANL regression
- **Dependencies:** T006, T008, T010, T011, T013, T015, T018.
- **Requirements:** NFR-004.
- **Done:** approved suites pass or blockers are recorded with evidence; INQTRANL behavior remains unchanged.

### T020 — Final review, QA, traceability and Quickstart evidence
- **Dependencies:** T019.
- **References:** Traceability Matrix, Code Review Checklist, QA Review Checklist, Quickstart.
- **Done:** matrices/evidence updated, code review complete, QA status explicit, runtime commands/expected results finalized from actual repository.

## Completion Checklist
- [ ] Exact five-key lookup implemented.
- [ ] No INQTRANL list semantics added.
- [ ] Successful absence is 200 found=false.
- [ ] No hidden validity/type/delete filters.
- [ ] No detail null defaulting.
- [ ] Security tests pass.
- [ ] Runtime OpenAPI aligned.
- [ ] Frontend uses existing architecture/auth mechanism.
- [ ] Regression passes or blockers recorded.
- [ ] Review/QA evidence complete.


## Artifact Relationships

- **Upstream Inputs:** `plan.md`, `spec.md`, `supporting/requirements.md`, `contracts/openapi.yaml` expectations, `supporting/test-spec.md` expectations.
- **Downstream Consumers:** Implementation agent, `supporting/traceability-matrix.md`, review checklists, `supporting/copilot-build-prompt.md`.
- **Authority Boundary:** Authoritative for executable work order/done criteria, not requirements.
- **Conflict Handling:** If a task would require changing Specification/Requirements, stop and reconcile upstream rather than expanding the task.
