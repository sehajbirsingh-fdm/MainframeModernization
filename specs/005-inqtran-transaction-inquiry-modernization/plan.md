# Implementation Plan: INQTRAN Transaction Inquiry Modernization

Branch: 005-inqtran-transaction-inquiry-modernization  
Date: 2026-07-31  
Spec: specs/005-inqtran-transaction-inquiry-modernization/spec.md

## 1. Plan Overview

This plan defines the technical implementation approach for Feature 005, delivering the approved INQTRANL read-only transaction inquiry capability inside the existing backend and frontend applications.

Scope is limited to integrating the inquiry flow into the current repository architecture while preserving validated legacy-observable behavior and applying approved modernization behavior at the API boundary.

Date-handling distinction retained by design:
- Verified legacy behavior: omitted dates flow through sentinel normalization, converted host variables, always-present SQL date predicates, no evidenced pre-SQL calendar-validity guard, and SQL technical-failure routing to the legacy abend path.
- Approved modernization behavior: the modern API supports optional omitted date boundaries as a target-system contract decision.

This plan does not treat approved modernization date behavior as proven legacy equivalence.

## 2. Technical Context

- Backend runtime: Java 21 and Spring Boot 3.
- Backend application: existing backend/api project.
- Frontend application: existing frontend/app React + TypeScript + Vite project.
- Persistence for proof of concept: JDBC with H2, using current repository conventions.
- Current proof-of-concept boundary: no live DB2, CICS, or mainframe dependency.
- Feature contract source: specs/005-inqtran-transaction-inquiry-modernization/contracts/openapi.yaml.
- Runtime OpenAPI publication target: backend/api/src/main/resources/openapi.yaml.
- Test expectations: backend unit, backend repository integration, backend controller, contract conformance, frontend unit, and end-to-end coverage.
- Delivery constraint: no second backend or frontend application.

## 3. Current Legacy Architecture

Legacy flow is represented at a technical-plan level as:

request or transaction entry -> INQTRANL processing -> input and date normalization -> SQL host-variable population -> always-present DB2 predicates -> count and row processing -> normal response or technical abend path

Observed legacy characteristics relevant to implementation planning:
- INQTRANL performs both filtered total counting and ordered row retrieval.
- Date predicates remain present in both query paths.
- Technical SQL failures route to abend handling rather than partial successful output.

## 4. Current Repository Architecture

Feature 005 is integrated into the existing repository layout and conventions:
- Use the existing backend application in backend/api.
- Use the existing frontend application in frontend/app.
- Do not create an additional backend service.
- Do not create an additional frontend project.
- Reuse existing package, routing, API-client, resource, test, and configuration patterns.
- Keep unrelated feature behavior unchanged.

## 5. Target Architecture

Primary dependency flow:

React inquiry page -> frontend API client -> Spring controller -> service -> repository abstraction -> JDBC/H2 implementation

Future substitution seam:

service or repository boundary -> future DB2 or approved mainframe integration adapter

Future mainframe connectivity is not part of the current proof of concept.

## 6. Technology Stack

- Java 21
- Spring Boot 3
- Spring MVC
- JDBC
- H2
- Maven
- React
- TypeScript
- Vite
- Existing backend test framework in backend/api
- Existing frontend test framework in frontend/app
- Playwright or existing repository E2E framework under frontend/app/e2e
- OpenAPI

## 7. Repository Integration Strategy

Feature 005 implementation strategy in current applications:
- Extend existing backend/api and frontend/app projects only.
- Integrate inquiry UI behavior into existing route and navigation patterns.
- Reuse current exception-handling and configuration conventions.
- Reuse existing resource initialization patterns for H2 schema and seed data.
- Keep Feature 005 logic isolated from unrelated feature paths.
- Run regression tests to prevent cross-feature behavioral drift.

### Repository-First Implementation Rule

Before selecting exact package names, folders, components, route files, API-client files, test locations, configuration files, shared utilities, or class names, inspect the current backend/api and frontend/app implementations and follow the conventions already established by implemented repository features.

The current repository implementation takes precedence over illustrative paths or proposed names in this plan.

Feature 005 must not introduce:
- a parallel backend structure,
- a parallel frontend structure,
- a second application architecture,
- a new feature-folder convention where an existing convention already applies,
- duplicate routing, API-client, error-handling, configuration, styling, or testing patterns.

Any exact file or package placement should be derived from repository evidence during task generation and implementation.

Source precedence for implementation decisions:
1. Feature 005 spec.md and supporting/requirements.md for approved behavior and obligations.
2. Feature 005 contracts/openapi.yaml for the transport contract.
3. The current backend/api and frontend/app repository implementation for existing structure and conventions.
4. Feature 005 plan.md for the approved implementation approach.
5. Relevant shared shell guidance from frontend-modernization, only where consistent with the current repository and Feature 005 artifacts.

If sources conflict, report the conflict for resolution rather than inventing a new structure or behavior.

## 8. Project and Package Structure

Verified existing repository locations:
- Backend source root: backend/api/src/main/java/com/bankofz/mainframemodernization
- Backend test root: backend/api/src/test/java/com/bankofz/mainframemodernization
- Backend resource root: backend/api/src/main/resources
- H2 schema initialization: backend/api/src/main/resources/schema.sql
- H2 seed data initialization: backend/api/src/main/resources/data.sql
- Runtime OpenAPI publication file: backend/api/src/main/resources/openapi.yaml
- Frontend source root: frontend/app/src
- Frontend feature area: frontend/app/src/features
- Frontend API-client area: frontend/app/src/api
- Frontend unit test locations: frontend/app/src/features/*/*.test.tsx and frontend/app/src/**/**/*.test.ts
- Frontend E2E location: frontend/app/e2e
- Feature 005 documentation root: specs/005-inqtran-transaction-inquiry-modernization

Intended placement approach:
- Place backend feature implementation under existing backend package conventions.
- Place backend tests under matching test package conventions.
- Place frontend inquiry UI/state under frontend feature conventions.
- Place frontend API access under existing frontend API-client conventions.

## 9. Backend Component Design

### Controller Design

Controller responsibilities:
- Bind HTTP path and query parameters.
- Perform structural boundary validation.
- Delegate orchestration to the service layer.
- Convert approved outcomes into HTTP responses.
- Reuse existing backend exception-handling conventions.

Controller non-responsibilities:
- No persistence logic.
- No duplication of service orchestration behavior.

### Service Design

Service responsibilities:
- Orchestrate request processing order.
- Apply approved date-boundary handling at API boundary while preserving legacy-compatible backend behavior.
- Normalize limit and offset behavior.
- Coordinate filtered count and ordered row retrieval.
- Enforce no-partial-success behavior for technical retrieval failures.
- Build approved response model including coherent metadata.

Service non-responsibilities:
- No invention of unsupported domain behavior.

### Repository Design

Repository responsibilities:
- Provide filtered total-count retrieval.
- Provide ordered row retrieval.
- Use equivalent filters for count and row paths.
- Preserve read-only persistence access for this feature.
- Remain isolated from transport concerns.

### Adapter Design

Adapter strategy:
- JDBC/H2 is the current proof-of-concept implementation behind the repository boundary.
- The boundary supports future DB2 or approved mainframe adapter substitution.
- Substitution should not require changes to controller contracts or public API behavior.

Current scope boundary:
- No live production adapter is required in this feature implementation.

### Mapper Design

Mapper responsibilities:
- Transform persistence values into approved domain and response fields.
- Preserve leading-zero semantics for identifiers.
- Compose transaction identifiers in approved component order.
- Apply approved date and time transformations.
- Preserve approved reference and amount mapping behavior.
- Exclude unapproved response fields.

### Domain and DTO Design

Domain and transport model boundaries:
- Internal domain types represent inquiry controls, normalized controls, row values, and result metadata.
- Persistence row types are internal to repository and adapter boundaries.
- API response DTOs expose only approved spec and OpenAPI fields.
- Pagination metadata and transaction rows remain explicit in response DTOs.
- Transport models remain separated from persistence implementation details.

## 10. Frontend Design

Frontend integration in existing React application includes:
- Route and navigation integration using current app patterns.
- Inquiry form for account identity and supported controls.
- API request construction for approved endpoint and query parameters.
- Loading-state rendering.
- Successful result rendering for populated pages.
- Empty-success rendering for valid zero-row outcomes.
- Validation-error rendering for approved boundary failures.
- Technical-error rendering for server-side retrieval failures.
- Pagination controls aligned to approved limit and offset behavior.
- Metadata and transaction-row rendering aligned to approved response fields.

Feature-specific frontend behavior is governed by the Feature 005 specification and OpenAPI contract. The existing frontend/app implementation governs repository structure and implementation conventions, including folder structure, routing, API-client organization, shared components, styling, tests, and related patterns. Relevant shared shell guidance from frontend-modernization may be reused where it remains consistent with the current repository, but feature-specific or outdated material in that package must not override Feature 005 artifacts or cause a parallel frontend structure to be introduced.

No unrelated UI redesign or non-feature UI expansion is included.

## 11. End-to-End Data Flow

1. The user submits account identity and inquiry controls.
2. The frontend validates basic input shape and calls the approved endpoint.
3. The controller binds and structurally validates the request.
4. The service normalizes limit and offset.
5. The service applies approved modern omitted-date behavior.
6. The repository obtains the filtered total count.
7. The repository obtains ordered result rows using equivalent filters.
8. The mapper converts persistence rows into approved response fields.
9. The service assembles coherent totalCount and returnedCount metadata.
10. The controller returns success, empty success, validation failure, or technical failure.
11. The frontend renders the corresponding UI state.

Failure invariant:
- If count or row retrieval fails technically, no partial successful page is returned.

## 12. Persistence Strategy

Persistence strategy for the current proof of concept:
- Use H2 as the local proof-of-concept store.
- Use JDBC as the persistence implementation.
- Reuse existing schema and seed-data initialization conventions.
- Preserve read-only inquiry behavior.
- Keep persistence behind repository abstraction.
- Ensure equivalent filters between count and row retrieval paths.
- Preserve exact account identity filtering.
- Preserve inclusive supplied-date filtering.
- Preserve approved ordering and pagination behavior.
- Preserve leading-zero semantics in mapped identifiers.
- Enforce approved page-size ceiling.
- Map approved transaction fields only.

Evidence boundary:
- The plan does not assume deployed DB2 schema/type/nullability has been fully proven from repository evidence.

## 13. Legacy-to-Modern Transformation Strategy

### Verified Legacy Behavior To Preserve

- Exact account filtering by sort code and account number.
- Inclusive supplied date filtering.
- Read-only retrieval behavior.
- Ordering by transaction date descending then transaction time descending.
- Separate count and row retrieval semantics.
- Technical-failure behavior with no partial successful result.

Ordering precision statement:
- Transactions are ordered by transaction date descending and transaction time descending. Relative ordering is not guaranteed for rows tied on both values unless a future approved tie-breaker is introduced.

### Approved Modernization Behavior

- Optional omitted date boundaries at the modern API boundary.
- HTTP response semantics defined by the approved feature contract.
- JSON response model aligned to the approved contract.
- Explicit pagination controls via limit and offset.
- Frontend UI states for loading, success, empty success, validation failure, and technical failure.
- H2/JDBC proof-of-concept persistence implementation.

Modernization decisions are not represented as proven legacy runtime facts.

## 14. Validation Strategy

Approved validation scope:
- Sort code shape validation.
- Account number shape validation.
- Date format validation for supplied dates.
- Approved date-boundary relationship validation.
- Limit defaulting and clamping behavior.
- Non-negative offset handling.
- Leading-zero preservation for identity values.

Validation ownership split:
- Frontend convenience validation: basic user-input shape feedback.
- Controller structural validation: boundary binding and request shape enforcement.
- Service normalization: effective controls and preserved behavior orchestration.

Validation exclusions:
- No invented account-existence validation.
- No invented transaction-existence validation.
- No unsupported rejection rules.

## 15. Error-Handling Strategy

Approved response outcomes:
- HTTP 200 for successful results.
- HTTP 200 for valid inquiries with no matching transactions.
- HTTP 400 for approved structural or boundary-validation failures.
- HTTP 500 for technical retrieval failures.

Error invariants:
- No partial successful response if count or row retrieval fails.
- No HTTP 404 for empty transaction results.
- No SQL internals, stack traces, or persistence implementation details exposed in API error responses.

Implementation approach:
- Reuse existing backend exception-handling patterns.
- Ensure frontend technical-error rendering is safe and non-sensitive.

## 16. Security Strategy

Security approach for Feature 005:
- Follow existing application and repository security policy.
- Do not invent new feature-specific authorization requirements.
- Avoid unnecessary exposure of account or transaction information.
- Keep request and error handling free of sensitive-value leakage.
- Treat non-approved security behavior as out of Feature 005 scope.

## 17. Logging and Observability

Logging and observability approach:
- Reuse existing Spring Boot logging conventions.
- Reuse existing correlation or request-identification conventions where available.
- Log technical retrieval failures at appropriate application boundaries.
- Distinguish empty-success outcomes from technical failures in operational logs.
- Avoid logging full account identifiers or full transaction payloads at normal levels.
- Preserve useful diagnostic context without exposing sensitive values.
- Do not introduce a new logging or observability platform for this feature.

## 18. Testing Strategy

### Backend Unit Tests

- Service normalization behavior.
- Supplied date-boundary behavior.
- Approved omitted-date behavior at API boundary.
- Pagination behavior.
- Count metadata behavior.
- Empty-success behavior.
- Technical-failure behavior.
- No-partial-success enforcement.
- Mapping behavior and leading-zero preservation.

### Repository Integration Tests

- Exact account filters.
- Inclusive supplied-date filters.
- Count and row filter parity.
- Ordering behavior.
- Limit and offset behavior.
- H2 query behavior.
- Failure propagation where practical.

### Controller Tests

- Request binding.
- Structural validation.
- HTTP 200, 400, and 500 behaviors.
- Empty-success behavior.
- Response structure behavior.

### Contract Tests

- Endpoint alignment.
- Parameter alignment.
- Status-code alignment.
- Response-schema alignment.
- Reconciliation between feature OpenAPI and runtime publication file.

### Frontend Unit Tests

- Request construction.
- Loading state.
- Result state.
- Empty state.
- Validation-error state.
- Technical-error state.
- Pagination behavior.

### End-to-End Tests

- Normal result retrieval.
- No-match empty success.
- Omitted-date request path.
- Pagination behavior.
- Offset beyond result set.
- Safe technical-failure rendering.

### Regression Tests

- Confirm existing backend behavior outside Feature 005 remains unchanged.
- Confirm existing frontend behavior outside Feature 005 remains unchanged.

Artifact sequencing note:
- test-spec.md and traceability-matrix.md are subsequent verification artifacts and are not treated as finalized plan inputs.

## 19. API Contract Strategy

Artifact responsibilities:
- spec.md defines approved system behavior.
- supporting/requirements.md defines required implementation obligations.
- contracts/openapi.yaml defines the Feature 005 transport contract.
- backend/api/src/main/resources/openapi.yaml is the runtime publication artifact that must be reconciled with the feature contract.

Merge gate:
- Contract conformance and runtime publication reconciliation are required before merge.

## 20. Configuration and Deployment Strategy

Configuration and deployment boundaries:
- Keep implementation inside the existing Spring Boot deployment unit.
- Keep frontend implementation inside the existing frontend application.
- Do not create an additional backend service.
- Do not create an additional frontend project.
- Reuse existing H2 schema and seed-data initialization conventions.
- Reuse existing Spring profiles and properties where possible.
- Do not require live DB2, CICS, or mainframe connectivity for the proof of concept.
- Treat future production adapter configuration as out of current scope.
- Avoid unrelated deployment changes.

## 21. Future Mainframe Adapter Options

Future production persistence options are architectural only in this plan:
- Direct approved DB2 connectivity, or
- An approved mainframe service or adapter boundary.

Current scope boundary:
- Production connectivity mechanism selection, detailed design, and implementation are out of scope for this proof of concept.

Compatibility objective:
- Replacing JDBC/H2 should not require changes to public API behavior, controller behavior, frontend behavior, or approved service orchestration behavior.

## 22. Risks and Mitigations

1. Deployed DB2 schema and nullability uncertainty  
Mitigation: preserve repository abstraction; validate behavior against approved H2 proof-of-concept model; record production adapter validation requirements.

2. Nullable source fields without evidenced indicator-variable handling  
Mitigation: add characterization tests and conservative mapping/error handling; defer unresolved null behavior to production adapter decisions.

3. Unresolved legacy sentinel runtime behavior  
Mitigation: preserve evidence-based distinction in implementation and tests; keep approved omitted-date API behavior explicitly contract-driven.

4. Rows tied on both transaction date and transaction time  
Mitigation: test and document the approved two-key ordering only; do not assume or assert tertiary tie-break behavior.

5. Legacy-to-modern reference type differences  
Mitigation: preserve contract field semantics; add mapping tests for format and leading-zero-sensitive values.

6. Runtime OpenAPI drift from feature contract  
Mitigation: add contract reconciliation checks between contracts/openapi.yaml and backend runtime publication artifact before merge.

7. Regression risk in shared backend or frontend applications  
Mitigation: isolate Feature 005 components and execute backend/frontend regression tests.

8. Future production adapter ownership and configuration uncertainty  
Mitigation: keep adapter boundary explicit and defer production connectivity ownership decisions to subsequent delivery planning.

## 23. Assumptions and Open Technical Decisions

### Assumptions

- Existing repository architecture and conventions remain applicable.
- JDBC with H2 remains the approved proof-of-concept persistence approach.
- Feature 005 implementation remains inside existing backend and frontend applications.
- No new feature-specific authorization behavior is introduced.
- Finalized Feature 005 upstream artifacts remain authoritative for implementation.

### Open Technical Decisions

- Exact package naming and final class placement within existing conventions.
- Reuse versus extension of existing global exception-handling implementation.
- Exact implementation mechanism for shared count and row filter construction.
- Final ownership and configuration model for production DB2 or mainframe adapter.

## 24. Implementation Phases

1. Persistence preparation  
Objective: confirm H2 schema/data compatibility with approved fields and query paths; define repository count/row boundary.

2. Backend domain and repository boundary  
Objective: implement domain mapping and repository abstraction wiring aligned with approved behavior.

3. Service orchestration and normalization  
Objective: implement normalized control handling, omitted-date contract handling, count/row coordination, and no-partial-success enforcement.

4. Controller and error handling  
Objective: implement request binding, boundary validation, and response/error mapping to approved HTTP outcomes.

5. OpenAPI reconciliation  
Objective: align runtime OpenAPI publication with feature contract artifact.

6. Frontend integration  
Objective: integrate inquiry route, form, API client, pagination controls, and UI states in existing frontend patterns.

7. Automated test completion  
Objective: complete backend, frontend, contract, and E2E coverage plus regression checks.

8. Documentation and final verification  
Objective: verify downstream plan-to-task continuity and produce subsequent verification artifacts.

Phase dependency summary:
- Complete backend persistence/service/controller path before full frontend completion.
- Complete OpenAPI reconciliation and automated tests before final verification.
- Complete regression checks before merge.

## 25. Complexity Tracking

Primary complexity drivers:
- Count and row query filter parity.
- Pagination and ordering interaction.
- Fixed-width and leading-zero-sensitive mapping.
- Verified-legacy versus approved-modern date-boundary distinction.
- No-partial-success enforcement across dual retrieval stages.
- Integration into shared backend and frontend applications without regressions.

Complexity exceptions:
- None identified.

## 26. Implementation Readiness

This plan is derived from finalized Feature 005 upstream artifacts and repository conventions. It does not introduce new requirements, business rules, user stories, acceptance criteria, or API behavior.

Implementation should proceed through the subsequent tasks.md workflow. Remaining verification artifacts, including test-spec.md and traceability-matrix.md, are expected after this plan stage.

Future production DB2 or mainframe connectivity remains outside the current proof-of-concept scope.