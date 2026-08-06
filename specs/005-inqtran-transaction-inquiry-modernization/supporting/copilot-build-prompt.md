# Copilot Build Prompt - Feature 005 INQTRAN Transaction Inquiry Modernization

## 1. Mission

Implement Feature 005 inside the existing backend and frontend applications in this repository, using the finalized Feature 005 artifacts as the controlling source.

Build only the read-only INQTRANL transaction inquiry capability.

INQTRAND transaction-detail inquiry is out of scope.

## 2. Authoritative Artifact Order

Use this precedence order when implementing. If sources conflict, report the contradiction and stop. Do not invent behavior or structure.

1. supporting/requirements.md and spec.md for approved obligations and behavior.
2. contracts/openapi.yaml for the transport contract.
3. supporting/business-rules.md, supporting/program-analysis.md, and supporting/mapping-matrix.md for verified legacy behavior and transformations.
4. supporting/intended-system.md, supporting/architecture.md, research.md, and data-model.md for approved technical boundaries and decisions.
5. plan.md for implementation approach.
6. tasks.md for execution order.
7. supporting/test-spec.md for verification scenarios.
8. supporting/traceability-matrix.md for cross-artifact linkage.
9. Current repository implementation for exact file, package, route, client, test, and configuration conventions.
10. checklists/code-review-checklist.md, checklists/qa-review-checklist.md, and quickstart.md for completion evidence and gate expectations.

Treat these as authoritative Feature 005 references:
- supporting/program-analysis.md
- supporting/dependency-map.md
- supporting/business-rules.md
- supporting/mapping-matrix.md
- supporting/intended-system.md
- supporting/architecture.md
- research.md
- data-model.md
- supporting/requirements.md
- checklists/requirements.md
- spec.md
- plan.md
- contracts/openapi.yaml
- supporting/test-spec.md
- supporting/traceability-matrix.md
- tasks.md
- checklists/code-review-checklist.md
- checklists/qa-review-checklist.md
- quickstart.md

## 2.1 Artifact Mutation Rules

During implementation, treat artifacts by category.

Frozen behavioral and design artifacts (read-only during implementation):
- supporting/program-analysis.md
- supporting/dependency-map.md
- supporting/business-rules.md
- supporting/mapping-matrix.md
- supporting/intended-system.md
- supporting/architecture.md
- research.md
- data-model.md
- supporting/requirements.md
- checklists/requirements.md
- spec.md
- plan.md
- contracts/openapi.yaml
- supporting/test-spec.md

Rules for frozen artifacts:
- Do not rewrite, reinterpret, or silently reconcile these artifacts during implementation.
- If implementation cannot satisfy one of these artifacts, stop and report the contradiction.

Execution-state artifacts (update only after evidence exists):
- tasks.md
- supporting/traceability-matrix.md
- checklists/code-review-checklist.md
- checklists/qa-review-checklist.md

Rules for execution-state artifacts:
- A task checkbox may be checked only after that task dependencies, done criteria, and verification evidence are satisfied.
- Traceability status may change from planned to executed only after implementation and test evidence exists.
- Code-review and QA checklist items may be checked only after the corresponding review is actually performed.
- Do not pre-check future work.

Operational and runtime documentation (may be updated to reflect verified implementation):
- quickstart.md
- Backend usage or README documentation
- Frontend usage or README documentation
- backend/api/src/main/resources/openapi.yaml
- Other existing runtime documentation identified by repository conventions

Rules for operational and runtime documentation:
- Updates must remain aligned with the frozen Feature 005 contract.
- Documentation updates must not introduce new behavior.

## 3. Repository-First Implementation Rule

Before choosing exact names and locations, inspect the current backend/api and frontend/app code.

You must inspect and reuse existing conventions for:
- Backend package organization.
- Controller, service, repository, and mapper patterns.
- Exception-handling and error-envelope patterns.
- H2 schema and data initialization patterns.
- Runtime OpenAPI publication patterns.
- Frontend routing and navigation patterns.
- Frontend API-client organization patterns.
- Frontend component/state patterns.
- Backend and frontend test placement and structure.
- Build, run, and test scripts.

Prohibited structural divergence:
- No second backend.
- No second frontend.
- No parallel package hierarchy.
- No duplicate shared utilities.
- No new parallel test hierarchy.
- No new framework without repository evidence.
- No invented folder structure.

Repository evidence takes precedence over illustrative names or paths.

### Change-Scope Discipline

- Inspect repository status before implementation.
- Identify and report pre-existing uncommitted changes.
- Do not modify unrelated files.
- Inspect repository status after each major phase.
- Report every changed file in the final report.
- Avoid cleanup or refactoring outside approved Feature 005 integration points.
- Do not overwrite or revert pre-existing user changes.

## 4. Scope

In scope:
- Exact account transaction inquiry.
- Optional date boundaries.
- Limit and offset handling.
- Ordering behavior.
- totalCount and returnedCount behavior.
- Approved transaction field mapping.
- Empty-success behavior.
- Technical-failure behavior.
- Frontend inquiry UI integration.
- Automated verification.
- Runtime OpenAPI reconciliation.
- Documentation and demo readiness.

Out of scope:
- INQTRAND.
- Live DB2, CICS, or mainframe connectivity.
- Production adapter implementation.
- Account-existence validation.
- New authorization behavior.
- Mock JSON persistence.
- Unrelated frontend redesign.
- Arbitrary coverage targets.
- Arbitrary performance targets.

## 5. Critical Legacy vs Modernization Distinction

Keep these categories explicitly separate in implementation comments, tests, documentation, and final reporting.

Repository evidence confirms:
- Omitted legacy dates trigger sentinel normalization.
- Sentinel values are converted into SQL host variables.
- Both SQL date predicates remain present.
- No pre-SQL calendar-validity guard is evidenced.
- SQL technical failures route to the legacy abend path.

Repository evidence does not prove:
- Omitted dates become unconstrained deployed SQL predicates.
- Omitted dates always produce a specific SQLCODE.

Approved modernization behavior:
- Optional omitted date boundaries at the API.
- HTTP and JSON transport.
- Explicit limit and offset controls.
- H2 and JDBC proof-of-concept persistence.
- React frontend states.

Do not collapse these evidence categories into one claim.

## 6. Required Technical Context

Implement within this approved context:
- Java 21.
- Spring Boot 3.
- Existing backend/api project.
- Existing frontend/app project.
- React.
- TypeScript.
- Vite.
- JDBC.
- H2.
- Maven.
- Vitest or current repository frontend test tooling.
- Playwright or current repository E2E tooling.
- OpenAPI.

Do not introduce another framework unless an existing repository requirement makes it necessary.

## 7. Implementation Sequence

Execute tasks.md in dependency order. Do not reorder tasks in ways that violate dependencies.

Expected sequence:
1. Inspect repository conventions.
2. Confirm schema and fixture strategy.
3. Implement domain and DTO models.
4. Implement repository abstraction and JDBC/H2 queries.
5. Implement mapping.
6. Implement service orchestration.
7. Implement controller and error handling.
8. Reconcile runtime OpenAPI.
9. Implement frontend API client and UI.
10. Add backend, contract, frontend, and E2E tests.
11. Run regressions.
12. Update documentation.
13. Complete traceability, QA, code review, and demo evidence.

Mark a task complete only when all of the following are true:
- All dependencies are complete.
- The implementation described by the task exists.
- The task done criteria are satisfied.
- Verification evidence or command output has been produced.
- No unresolved contradiction affects the task.

A task must remain unchecked if evidence is missing, even when partial code exists.

## 8. Backend Implementation Rules

You must:
- Keep the controller thin.
- Use constructor injection.
- Keep orchestration in service classes.
- Keep persistence behavior in repository classes.
- Keep field transformation in mappers.
- Expose only approved DTO fields.
- Reuse existing exception-handling conventions.
- Preserve no-partial-success behavior for technical failures.

You must not:
- Add account-existence validation.
- Add unsupported HTTP behavior.
- Return partial success on technical failure.

## 9. Persistence and Query Rules

You must implement:
- JDBC and H2 proof-of-concept persistence.
- Parameterized SQL queries.
- Read-only query behavior.
- Exact sort code and account number filtering.
- Inclusive supplied-date filtering.
- Approved omitted-boundary behavior.
- Equivalent filters between count and row paths.
- Count before pagination semantics.
- Ordering by transaction date descending, then transaction time descending.
- Ordering before applying offset and limit.
- Leading-zero preservation.

You must not implement:
- Tertiary ordering key behavior.
- Mock JSON persistence fallback.

Future DB2/mainframe connectivity must remain only behind the repository or adapter seam, without implementing live connectivity now.

## 10. Service and Controller Rules

Service must own:
- Limit defaulting.
- Zero-limit normalization.
- Maximum-limit clamping.
- Offset handling.
- Optional date interpretation.
- Count and list coordination.
- Metadata construction.
- Empty-result handling.
- No-partial-success enforcement.

Controller must own:
- Path and query binding.
- Structural validation.
- Approved HTTP 200, 400, and 500 behavior.
- Safe response mapping.

Do not return HTTP 404 for empty transaction results.

## 11. Frontend Implementation Rules

You must:
- Integrate into current route and navigation structure.
- Reuse current API-client patterns.
- Provide inquiry controls for approved inputs.
- Omit optional query parameters when not supplied.
- Implement loading state.
- Implement populated state.
- Implement empty-success state.
- Implement validation-error state.
- Implement technical-error state.
- Implement pagination behavior.
- Render metadata and transaction rows per approved contract.
- Replace prior completed results on subsequent completed inquiry.
- Preserve shared shell behavior and unrelated routes.

Behavior authority for Feature 005 is spec.md plus contracts/openapi.yaml. Structural authority is the current frontend/app implementation.

Do not treat historical INQCUST-specific frontend-modernization material as authority for Feature 005 behavior or structure.

## 12. API Contract Rules

You must align implementation to contracts/openapi.yaml for:
- Exact path and method.
- Parameter optionality, bounds, and format rules.
- Approved response schemas.
- HTTP 200 populated responses.
- HTTP 200 empty responses.
- HTTP 400 validation responses.
- HTTP 500 technical-failure responses.
- No undocumented response fields.
- Runtime OpenAPI reconciliation.

Treat contracts/openapi.yaml as frozen during implementation. If repository reality or implementation constraints contradict the feature contract, stop and report the contradiction before making any contract change. Do not silently edit the feature contract to match an implementation.

Runtime OpenAPI publication artifacts may be updated to match the frozen Feature 005 contract.

## 13. Testing and Verification Rules

Implement and execute verification according to supporting/test-spec.md.

Required coverage layers:
- Backend unit tests.
- Repository integration tests.
- Controller or API tests.
- Contract tests.
- Frontend component tests.
- End-to-end tests.
- Regression tests.
- SR-003 parameterized-query verification.
- Logging and correlation verification where supported.

Keep requirement, task, and test traceability synchronized with supporting/traceability-matrix.md.

Do not claim a test passed without actual command output evidence.

Do not add new tests except where required to verify an existing approved requirement.

### Incremental Verification

Run focused verification after each applicable implementation layer:
- Schema or persistence changes: run relevant initialization and repository tests.
- Domain or mapper changes: run relevant unit tests.
- Service changes: run service unit tests.
- Controller changes: run controller or API tests.
- Runtime OpenAPI changes: run contract reconciliation tests.
- Frontend changes: run relevant frontend component tests.
- Integrated feature completion: run E2E and regression suites.

Do not wait until end of implementation to discover basic layer failures. Do not run full regression after every small edit unless the change impact requires it.

### Traceability Status Update Rules

Update traceability status only when all of the following are true:
- The linked implementation exists.
- The linked task is complete.
- The linked test or review evidence has been executed.
- The evidence location is recorded.

Keep planned mappings labeled planned until execution evidence exists.

## 14. Documentation and Operational Rules

Update and verify:
- Runtime OpenAPI publication artifacts.
- Backend usage documentation.
- Frontend usage documentation.
- quickstart.md steps and outcomes.
- Proof-of-concept limitation statements.
- Run and test command documentation.
- Traceability status after execution.

Re-run documented commands from the actual repository checkout.

## 15. Security, Logging, and Error-Handling Rules

You must preserve:
- Existing route-security behavior.
- No new feature-specific authorization behavior.
- Parameterized SQL usage.
- No SQL, stack, or internal implementation details in API errors.
- No internal implementation details in frontend errors.
- No full transaction payload logging at normal levels.
- No unnecessary full account identifier logging.
- Distinguishable empty-success vs technical-failure outcomes.
- Correlation or request identification where current platform conventions support it.

## 16. Prohibited Work

Do not perform any of the following:
- INQTRAND implementation.
- Transaction-detail endpoint.
- Account-existence validation.
- Tertiary ordering behavior.
- Mock JSON persistence.
- Live DB2, CICS, or mainframe connection.
- Production adapter design or implementation.
- Second backend project.
- Second frontend project.
- Parallel folder structures.
- New authorization rules.
- Unsupported validation behavior.
- Undocumented API fields.
- Unrelated refactors.
- Generator changes.
- Arbitrary coverage targets.
- Arbitrary performance targets.

## 17. Stop Conditions

Stop and report before proceeding if any of the following is encountered:
- Repository structure contradicts the approved implementation approach.
- contracts/openapi.yaml contradicts spec.md or supporting/requirements.md.
- supporting/requirements.md contradicts supporting/business-rules.md.
- A referenced requirement, acceptance, task, or test identifier does not exist.
- A required dependency or test framework is absent in repository reality.
- Existing schema cannot support approved behavior without broader-impact changes.
- Implementation would require unsupported behavior.
- A task cannot be completed without changing an authoritative artifact.

Do not silently choose new behavior or structure.

## 18. Required Final Report

At completion, provide a concrete report with evidence-oriented summaries for:
1. Files changed.
2. Tasks completed.
3. Tasks not completed and why.
4. Repository conventions reused.
5. Backend implementation summary.
6. Frontend implementation summary.
7. Schema and fixture changes.
8. OpenAPI reconciliation result.
9. Commands executed.
10. Test results by layer.
11. Regression results.
12. Traceability updates.
13. Code review findings.
14. QA findings.
15. Demo verification result.
16. Confirmation that no mock JSON, live mainframe connection, second project, INQTRAND, tertiary ordering, or unsupported behavior was introduced.
17. Remaining risks or contradictions.
18. Pre-existing repository changes identified before implementation.
19. Exact final repository change list.
20. Tasks intentionally left unchecked due to missing evidence.
21. Authoritative-artifact contradictions encountered.
22. Confirmation that frozen behavioral and design artifacts were not modified.

Distinguish implemented, tested, reviewed, documented, deferred, and blocked status explicitly. Do not use complete to mean only that code was written.

Do not use vague statements such as all tests passed. Name exact commands, suites, and outcome summaries.
