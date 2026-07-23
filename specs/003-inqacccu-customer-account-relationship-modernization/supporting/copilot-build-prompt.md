# INQACCCU Copilot Build Prompt

## Purpose
Implement the INQACCCU Customer-Account Relationship Modernization feature in a controlled, task-driven manner using the finalized artifacts and existing repository architecture.

This prompt governs implementation execution only. It does not redefine requirements, business behavior, architecture, API behavior, or contract design.

## Authoritative Artifacts
Use the following files as frozen authorities:

- supporting/intended-system.md
- supporting/architecture.md
- supporting/business-rules.md
- supporting/data-model.md
- supporting/program-analysis.md
- supporting/research.md
- requirements.md
- spec.md
- plan.md
- tasks.md
- contracts/openapi.yaml
- supporting/test-spec.md

Use artifact responsibilities exactly as defined:

- requirements.md defines business needs.
- spec.md defines externally observable feature and API behavior.
- plan.md defines implementation architecture and strategy.
- tasks.md defines implementation sequence and completion criteria.
- contracts/openapi.yaml defines the machine-readable API contract.
- supporting/test-spec.md defines required verification coverage.
- supporting artifacts provide legacy, data, mapping, and architectural context.

If any frozen artifacts conflict, do not guess and do not edit frozen files. Stop only the affected task and report the exact conflict with file names and relevant sections.

## Repository-First Implementation
Before making code changes:

1. Inspect the current repository structure.
2. Identify established backend and frontend modules.
3. Identify existing naming, packaging, configuration, API, testing, error-handling, logging, and security conventions.
4. Reuse existing conventions and patterns where they do not conflict with frozen artifacts.
5. Do not create a second backend or frontend application.
6. Do not introduce new frameworks, libraries, infrastructure, or architectural layers unless explicitly required by plan.md and absent from the repository.

Implementation must follow the finalized feature architecture in plan.md and supporting/architecture.md while staying consistent with repository conventions.

## Task-Driven Execution Rules
Execute tasks.md in dependency order.

For each task:

1. Read task description, dependencies, and acceptance criteria.
2. Confirm all dependency tasks are complete.
3. Implement only the current task plus minimum supporting changes.
4. Do not implement future tasks early.
5. Add or update tests required for current task verification.
6. Run relevant build/test command(s) for impacted modules.
7. Verify all acceptance criteria are satisfied.
8. Mark/report task complete only after implementation and verification succeed.
9. Continue to next unblocked task.

Do not create separate TODO files. Do not replace implementation work with planning notes.

## Scope Control
Implement only finalized INQACCCU scope.

Do not introduce:

- new inquiry modes
- new reserved-value behavior
- write operations
- new endpoints
- pagination/filtering not required by frozen artifacts
- additional customer/account capabilities outside feature scope
- dedicated Repeat Inquiry feature
- unrelated navigation redesign
- unrelated repository cleanup or refactoring

Frontend may support updating inquiry input and submitting another request through normal inquiry flow only.

## Backend Implementation Expectations
Implement backend according to plan.md, tasks.md, contracts/openapi.yaml, and established repository structure.

Required implementation concerns (as assigned by tasks):

- request validation
- customer validation before account retrieval
- internally derived sort-code behavior
- read-only account retrieval
- bounded maximum account return count
- normal end-of-data handling
- customer-not-found outcomes
- zero-account outcomes
- successful account-list outcomes
- retrieval-stage legacy failure mappings
- separation of business outcomes from infrastructure failures
- identifier and leading-zero preservation
- date transformation
- numberOfAccounts alignment with returned account collection
- contract-conformant validation and infrastructure error payloads
- correlation and safe logging
- existing project security behavior

Do not expose internal repository/domain records directly through API payloads.
Do not change contracts/openapi.yaml to fit implementation drift.
Runtime behavior must conform to frozen OpenAPI contract.

## Frontend Implementation Expectations
Implement in the existing frontend application and follow repository-established React, TypeScript, Vite, routing, API-client, state, and testing conventions already present in src/frontend-react.

Required frontend behavior (as assigned by tasks):

- inquiry-page route and rendering
- customer-number input
- client-side validation and field-level feedback
- prevention of invalid backend requests
- valid API request construction
- loading presentation
- successful account-result rendering
- zero-account presentation
- customer-not-found presentation
- business failure presentation
- validation-error presentation
- infrastructure and network error presentation
- preservation of leading zeroes
- update input and submit another request through normal flow
- replace previous display with newly completed inquiry outcome
- prevent stale result/loading/error leakage between inquiries

Do not add user-entered bearer-token fields unless explicitly required by frozen artifacts and existing application architecture.

## Testing Expectations
Implement tests according to supporting/test-spec.md and task acceptance criteria.

Use only frameworks/tooling already established in repository or explicitly named in plan.md.

Required verification (as applicable by task):

- backend unit tests
- mapper/transformation tests
- service orchestration tests
- repository/adapter tests
- controller and exception-handler tests
- Spring integration tests
- frontend unit/component tests
- frontend validation tests
- frontend API-client tests
- frontend integration tests
- browser-level end-to-end tests
- automated OpenAPI contract-conformance tests

Tests must use deterministic data and controlled failure simulation.
Do not fabricate results. Do not mark tests complete without running applicable commands.

## OpenAPI Conformance
Treat contracts/openapi.yaml as frozen and authoritative.

Verify that implementation matches:

- path and parameter definitions
- HTTP 200 business response schema
- HTTP 400 validation-error schema
- HTTP 500 infrastructure-error schema
- required fields, types, formats, and enums

If mismatch exists, fix implementation unless frozen-artifact conflict is identified.

## Configuration and Data Modes
Follow configuration strategy in plan.md.

- Keep configuration externalized where required.
- Preserve intended mode separation where plan/tasks define mock and adapter-backed behavior.
- Do not invent production credentials, connection settings, or unsupported infrastructure.
- Avoid starting unnecessary infrastructure in mock mode.
- If non-mock mode is selected without required configuration, fail clearly.
- Keep repository/service contracts independent of specific data source implementation.

## Quality Rules
Maintain:

- existing formatting and naming conventions
- clear separation of concerns
- read-only feature behavior
- safe error messages
- safe structured logging
- correlation propagation where required
- no sensitive values in logs
- deterministic tests
- no dead code or temporary implementations
- no unexplained suppressions or disabled tests
- no unrelated dependency upgrades

## Blocker Handling
A task is blocked only when:

- required dependency task is incomplete
- required repository content is missing
- frozen artifacts contain a material conflict
- external dependency required by frozen scope is unavailable
- task acceptance criteria cannot be satisfied without changing approved behavior

When blocked:

1. Stop only the affected task.
2. Do not invent behavior.
3. Report:
   - blocked task ID
   - exact blocker
   - affected artifact or repository path
   - what was verified
   - smallest decision/correction required

Continue other tasks only when dependencies and behavior are unaffected.

## Completion Standard
Feature implementation is complete only when:

- all tasks are implemented in dependency order
- all task acceptance criteria are satisfied
- backend and frontend builds succeed
- required automated tests pass
- integrated inquiry flow is verified
- runtime responses conform to frozen OpenAPI contract
- no unresolved critical defects remain
- no frozen artifacts were changed to hide implementation drift
- no unsupported behavior was introduced

## Final Implementation Report
After implementation, provide a concise report with:

1. Tasks completed
2. Files created/modified
3. Backend behavior implemented
4. Frontend behavior implemented
5. Tests added and commands run
6. Build and test results
7. OpenAPI conformance status
8. Any blockers, deviations, or remaining manual verification

Do not claim command success unless command execution occurred.
Do not include speculative future work unless tied to an actual documented blocker.
