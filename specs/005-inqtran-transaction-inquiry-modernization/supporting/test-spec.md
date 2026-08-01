# Test Specification - Feature 005 INQTRAN Transaction Inquiry Modernization

## 1. Purpose

This document defines how approved Feature 005 behavior and obligations are verified across backend, persistence, API contract, frontend, and end-to-end test layers.

It is a verification artifact only. It does not define new requirements, change approved behavior, or modify any transport contract.

## 2. Scope

In scope:
- Exact account transaction inquiry by sort code and account number.
- Supplied and omitted date-boundary behavior.
- Pagination and ordering behavior.
- totalCount and returnedCount semantics.
- Transaction field mapping and transformation behavior.
- Boundary validation behavior.
- Empty success outcomes.
- Technical-failure outcomes and no-partial-success rule.
- Frontend request, rendering, and state behavior for Feature 005.
- OpenAPI conformance, including runtime publication reconciliation.
- Regression checks for shared backend and frontend applications.

Out of scope:
- INQTRAND transaction-detail behavior.
- Live DB2, CICS, or production mainframe adapter testing.
- Production connectivity mechanism validation.
- Arbitrary performance benchmarking.
- Arbitrary coverage percentage targets.
- New authorization behavior design.
- Unrelated frontend redesign.

## 3. Test Objectives

- Verify implementation satisfies finalized Feature 005 requirements and contract behavior.
- Verify approved modernization behavior is kept distinct from unresolved legacy runtime behavior.
- Verify count and row retrieval paths apply equivalent filters.
- Verify ordering is applied before offset and limit.
- Verify metadata remains coherent across populated and empty pages.
- Verify technical failures never return partial successful pages.
- Verify frontend behavior aligns to approved Feature 005 contract behavior.
- Verify no regressions are introduced in existing repository features.

## 4. Authoritative Test Basis

Test-basis responsibilities:
- supporting/business-rules.md: verified and approved business behavior constraints (BR-001 through BR-019).
- supporting/requirements.md: implementation obligations (FR, NFR, SR, OR identifiers).
- spec.md: user-visible behavior, acceptance criteria, invariants, and boundary semantics.
- contracts/openapi.yaml: endpoint, parameter, status, and schema transport contract.
- supporting/mapping-matrix.md: field flow and transformation evidence.
- data-model.md: conceptual structures and invariants used for scenario design.
- plan.md: test-layer coverage responsibilities and architecture-level placement.
- Current repository tests and scripts: implementation conventions for backend and frontend test organization.

Additional evidence consulted for context alignment:
- supporting/program-analysis.md
- supporting/dependency-map.md
- supporting/intended-system.md
- supporting/architecture.md
- research.md
- checklists/requirements.md

Note:
- A file named supporting/dual-mode-analysis.md was not found in the current Feature 005 package. This does not block this test-spec rewrite because finalized behavior references above are present.

## 5. Test Levels and Responsibilities

### Backend Unit Tests

Own verification of:
- Service orchestration sequence.
- Limit and offset normalization.
- Date-control interpretation for supplied and omitted controls.
- Count/list coordination and metadata construction.
- No-partial-success technical-failure behavior.
- Isolated mapping logic where unit-level seams exist.

### Repository Integration Tests

Own verification of:
- H2 SQL behavior in repository integration context.
- Exact account filtering.
- Inclusive supplied date filtering.
- Approved omitted-boundary implementation behavior.
- Count/list filter parity.
- Ordering, offset, and limit behavior.
- Row mapping from persisted values.
- Persistence-failure propagation where practical.

### Controller or API Tests

Own verification of:
- Path and query parameter binding.
- Structural validation outcomes.
- HTTP 200, 400, and 500 behavior.
- Empty-success behavior.
- Response structure and safe error exposure.

### Contract Tests

Own verification of:
- OpenAPI operation path and method.
- Path and query parameter definitions.
- Optionality, type, format, defaults, and bounds.
- Status-code set and schema alignment.
- Runtime OpenAPI publication reconciliation.

### Frontend Unit and Component Tests

Own verification of:
- Request composition and form behavior.
- Loading, populated, empty, validation, and technical-error states.
- Metadata and row rendering.
- Pagination controls.
- Subsequent inquiry behavior replacing prior completed result state.

### End-to-End Tests

Own representative integrated journeys across current frontend and backend runtime behavior.

### Regression Tests

Own verification that INQCUST, INQACC, INQACCCU, CRECUST, shared shell/routing, and applicable shared security behavior remain unaffected.

### Characterization and Modernization Verification

Characterization tests verify legacy-observable behavior that is supported by repository evidence and intentionally preserved. This includes exact account filtering, inclusive supplied date boundaries, read-only behavior, two-key ordering, count and row retrieval semantics, technical-failure behavior, and no-partial-success behavior.

Characterization tests must not claim to prove deployed legacy runtime behavior that repository evidence does not establish.

Modernization verification tests confirm approved target-system behavior. This includes optional omitted date boundaries at the modern API, HTTP status semantics, JSON response structure, explicit limit and offset controls, frontend loading/success/empty/validation/technical-error states, H2/JDBC proof-of-concept behavior, and OpenAPI conformance.

Omitted-date tests in this document are modernization verification tests. They do not prove that deployed legacy SQL treated omitted dates as unconstrained, and they do not prove a specific deployed DB2 SQLCODE outcome.

## 6. Test Environment and Tooling

Verified repository tooling:
- Backend: Maven with spring-boot-starter-test and Spring Boot test support.
- Backend persistence integration: H2-backed test paths consistent with repository conventions.
- Frontend unit/component: Vitest with jsdom and Testing Library packages.
- Frontend E2E: Playwright invoked through existing npm script.
- Build and execution scripts: mvn test/verify and npm test/npm run test:e2e under current repository workflow.

Environment constraints:
- Proof-of-concept tests do not require live DB2, CICS, or mainframe access.
- No new test framework is introduced by this specification.

### Repository-First Testing Rule

Before selecting exact test folders, package names, class names, fixture locations, helper structures, or execution scripts, inspect the current backend/api and frontend/app test implementations and follow the conventions already established by implemented features.

Repository evidence takes precedence over illustrative organization in this document. Feature 005 must not introduce a parallel test hierarchy or a new testing convention where an existing repository convention already applies.

Exact test file placement, package naming, test class naming, fixture placement, test helper placement, npm script usage, Maven test organization, frontend component-test organization, Playwright organization, and H2 test setup must be derived from the current repository.

No new test framework may be introduced.

## 7. Test Data and Fixture Strategy

Fixture principles:
- Use deterministic test-scope fixtures.
- Use H2-backed integration datasets consistent with approved schema conventions.
- Preserve leading-zero identifiers in sort code, account number, and reference fields.
- Include enough rows to verify limit default, cap, and pagination behavior.
- Include rows across multiple dates/times and tied ordering conditions.
- Include matching and nonmatching account identities.
- Include positive, negative, and decimal amount values.
- Include empty-result scenarios.
- Include technical-failure injection points at count and row retrieval stages.

Data-shape requirements:
- Include rows tied on date only.
- Include rows tied on both date and time.
- Include offset values within, equal to, and greater than filtered totals.

Data-boundary constraints:
- No mock JSON persistence path is introduced for Feature 005 verification.
- Demo H2 seed/fixture data remains test-scope data and is not authoritative production data.
- Unsupported null behavior is not invented solely to manufacture fixture scenarios.

## 8. Functional Test Scenarios

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-001 | Repository Integration | Exact sort code/account match with mixed account dataset | Only rows for exact requested sortCode and accountNumber are returned | supporting/business-rules.md - BR-001; supporting/requirements.md - FR-001; spec.md - AC-001 |
| TC-002 | Repository Integration | Row exactly on supplied fromDate boundary | Boundary row is included | supporting/business-rules.md - BR-002; supporting/requirements.md - FR-002; spec.md - AC-002 |
| TC-003 | Repository Integration | Row exactly on supplied toDate boundary | Boundary row is included | supporting/business-rules.md - BR-002; supporting/requirements.md - FR-002; spec.md - AC-002 |
| TC-004 | Backend Unit | Only fromDate supplied | Request is processed with approved modern omitted-boundary behavior for toDate; behavior is validated as contract behavior, not legacy runtime equivalence proof | supporting/requirements.md - FR-002, FR-013; spec.md - AC-002, AC-014; contracts/openapi.yaml - fromDate/toDate optional query parameters |
| TC-005 | Backend Unit | Only toDate supplied | Request is processed with approved modern omitted-boundary behavior for fromDate; behavior is validated as contract behavior, not legacy runtime equivalence proof | supporting/requirements.md - FR-002, FR-013; spec.md - AC-002, AC-014; contracts/openapi.yaml - fromDate/toDate optional query parameters |
| TC-006 | Backend Unit | Both dates omitted | Request is accepted and processed per approved modern omitted-boundary contract behavior with coherent metadata | supporting/requirements.md - FR-002, FR-013; spec.md - Validation Rules, Error Handling, and AC-014; contracts/openapi.yaml - optional fromDate/toDate |
| TC-007 | Backend Unit | Omitted limit | Effective limit is 50 | supporting/business-rules.md - BR-005; supporting/requirements.md - FR-003; spec.md - AC-003 |
| TC-008 | Backend Unit | limit=0 | Effective limit is normalized to 50 | supporting/business-rules.md - BR-005; supporting/requirements.md - FR-003; spec.md - AC-003 |
| TC-009 | Repository Integration | limit=100 | No more than 100 rows are returned | supporting/business-rules.md - BR-007; supporting/requirements.md - FR-003; spec.md - AC-003 |
| TC-010 | Backend Unit | limit above 100 | Effective limit is normalized to 100 | supporting/business-rules.md - BR-006; supporting/requirements.md - FR-003; spec.md - AC-003 |
| TC-011 | Repository Integration | offset=0 | First row of ordered filtered set is returned | supporting/business-rules.md - BR-008; supporting/requirements.md - FR-004; spec.md - AC-004 |
| TC-012 | Repository Integration | offset within filtered result set | Correct number of ordered rows are skipped before return | supporting/business-rules.md - BR-008; supporting/requirements.md - FR-004; spec.md - AC-004 |
| TC-013 | Controller/API | offset equals filtered total | HTTP 200 with preserved totalCount and returnedCount 0 | spec.md - AC-008; supporting/requirements.md - FR-006, FR-007; contracts/openapi.yaml - 200 emptyOffsetBeyondTotal example |
| TC-014 | Controller/API | offset greater than filtered total | HTTP 200 with preserved totalCount and empty transactions | spec.md - AC-008; contracts/openapi.yaml - 200 response semantics |
| TC-015 | Repository Integration | Large valid offset value | Response remains valid empty page when offset is beyond total | supporting/requirements.md - FR-004, FR-007; spec.md - Edge Cases offset beyond filtered set |
| TC-016 | Controller/API | Empty match set (no matching rows) | HTTP 200 with totalCount 0, returnedCount 0, transactions [] | supporting/business-rules.md - BR-012; supporting/requirements.md - FR-007; spec.md - AC-007; contracts/openapi.yaml - emptyNoMatch example |
| TC-017 | Backend Unit | Final partial page | returnedCount equals actual page size while totalCount remains full filtered population | supporting/business-rules.md - BR-010, BR-011, BR-017; supporting/requirements.md - FR-006, NFR-004; spec.md - AC-006 |
| TC-018 | Backend Unit | Read-only inquiry execution | No write operation is performed | supporting/requirements.md - FR-010; spec.md - Feature Invariants and AC-011 |
| TC-019 | Controller/API | Count-stage technical failure | HTTP 500 and no successful page returned | supporting/business-rules.md - BR-018; supporting/requirements.md - FR-011; spec.md - AC-012 and Error Handling |
| TC-020 | Controller/API | Row-stage technical failure | HTTP 500 and no partial successful page returned | supporting/business-rules.md - BR-018; supporting/requirements.md - FR-011; spec.md - AC-012 and Error Handling |
| TC-021 | Backend Unit | Complete filtered totalCount construction | totalCount reflects filtered set before pagination | supporting/business-rules.md - BR-010; supporting/requirements.md - FR-006, NFR-004; spec.md - AC-006 |
| TC-022 | Backend Unit | returnedCount construction | returnedCount equals transactions array size for page | supporting/business-rules.md - BR-011; supporting/requirements.md - FR-006; spec.md - AC-006 |

## 9. Validation and Boundary Test Scenarios

Validation baseline:
- This section verifies approved structural validation from supporting/requirements.md, spec.md, and contracts/openapi.yaml.
- Calendar-date rejection and fromDate>toDate rejection are tested only if those validations are finalized in implementation and contract policy; they are not assumed as mandatory solely from legacy evidence.

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-023 | Controller/API | sortCode too short | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - sortCode pattern |
| TC-024 | Controller/API | sortCode too long | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - sortCode pattern |
| TC-025 | Controller/API | sortCode contains nondigits | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - sortCode pattern |
| TC-026 | Controller/API | accountNumber too short | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - accountNumber pattern |
| TC-027 | Controller/API | accountNumber too long | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - accountNumber pattern |
| TC-028 | Controller/API | accountNumber contains nondigits | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - accountNumber pattern |
| TC-029 | Controller/API | Leading-zero identity values | Request is accepted and leading zeros are preserved in response context | supporting/requirements.md - NFR-002; spec.md - Feature Invariants |
| TC-030 | Controller/API | fromDate invalid format | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - fromDate pattern |
| TC-031 | Controller/API | toDate invalid format | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules; contracts/openapi.yaml - toDate pattern |
| TC-032 | Controller/API | negative limit | HTTP 400 | spec.md - Validation Rules; contracts/openapi.yaml - limit minimum 0 |
| TC-033 | Controller/API | negative offset | HTTP 400 | spec.md - Validation Rules; contracts/openapi.yaml - offset minimum 0 |
| TC-034 | Controller/API | nonnumeric limit or offset | HTTP 400 | supporting/requirements.md - SR-002; spec.md - Validation Rules |
| TC-035 | Backend Unit | limit=0 normalization path | Effective limit is normalized to 50 before retrieval orchestration | supporting/business-rules.md - BR-005; supporting/requirements.md - FR-003; spec.md - AC-003 |
| TC-036 | Backend Unit | limit above maximum normalization path | Effective limit is normalized to 100 before retrieval orchestration | supporting/business-rules.md - BR-006; supporting/requirements.md - FR-003; spec.md - AC-003 |
| TC-037 | Controller/API (conditional) | fromDate later than toDate if implemented as approved contract validation | HTTP 400 only when this validation is finalized by contract and runtime behavior | spec.md - Validation Rules (Proposed modernization validations requiring approval); supporting/requirements.md - Modernization Enhancements Requiring Approval |
| TC-038 | Controller/API (conditional) | invalid calendar date if implemented as approved contract validation | HTTP 400 only when this validation is finalized by contract and runtime behavior | spec.md - Validation Rules (Proposed modernization validations requiring approval); supporting/requirements.md - Modernization Enhancements Requiring Approval |

## 10. Persistence and Query Test Scenarios

Ordering and pagination principle:

Transactions are ordered by transaction date descending and transaction time descending. Tests must not assert relative ordering for rows tied on both values because no approved tertiary tie-breaker exists.

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-039 | Repository Integration | Count and row paths use same account filters | Population used for count/list parity reflects same account identity constraints | supporting/business-rules.md - BR-001, BR-017; supporting/requirements.md - FR-006 |
| TC-040 | Repository Integration | Count and row paths use same supplied date filters | Filter parity preserved for supplied boundaries | supporting/business-rules.md - BR-002, BR-017; supporting/requirements.md - FR-002, FR-006 |
| TC-041 | Repository Integration | Omitted-boundary behavior applied consistently to both paths | Count/list parity remains coherent for omitted-boundary modern contract behavior | supporting/requirements.md - FR-002, FR-006, FR-013; contracts/openapi.yaml - optional date controls |
| TC-042 | Repository Integration | Different transaction dates ordering | Descending date order verified | supporting/business-rules.md - BR-009; supporting/requirements.md - FR-005; spec.md - AC-005 |
| TC-043 | Repository Integration | Same date, different time ordering | Descending time order verified within same date | supporting/business-rules.md - BR-009; supporting/requirements.md - FR-005; spec.md - AC-005 |
| TC-044 | Repository Integration | Rows tied on both date/time | Test asserts membership and page counts, not relative order between tied rows | supporting/business-rules.md - BR-009; spec.md - AC-005 and Edge Cases |
| TC-045 | Repository Integration | Ordering occurs before offset | Selected page reflects ordered set then skipped rows | supporting/business-rules.md - BR-008, BR-009; supporting/requirements.md - FR-004, FR-005 |
| TC-046 | Repository Integration | Ordering occurs before limit | Returned subset reflects ordered set then capped rows | supporting/business-rules.md - BR-009; supporting/requirements.md - FR-003, FR-005 |
| TC-047 | Repository Integration | Adjacent-page behavior where stable assertions are possible | No row duplication/loss across adjacent offsets for non-tied ordering keys | supporting/requirements.md - FR-004, FR-006; spec.md - AC-004, AC-006 |
| TC-048 | Repository Integration | Read-only repository enforcement | No insert/update/delete behavior introduced in query path | supporting/requirements.md - FR-010; spec.md - Feature Invariants and AC-011 |
| TC-049 | Repository Integration | Count query failure propagation | Failure prevents successful page construction | supporting/business-rules.md - BR-018; supporting/requirements.md - FR-011 |
| TC-050 | Repository Integration | Row query failure propagation | Failure prevents partial page construction | supporting/business-rules.md - BR-018; supporting/requirements.md - FR-011 |

## 11. Mapping and Transformation Test Scenarios

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-051 | Backend Unit | sortCode mapping | Response field matches approved mapped value and preserves leading zeros | supporting/mapping-matrix.md - Core Field and Flow Mapping (INQTRANL-SORTCODE row); supporting/requirements.md - FR-009, NFR-002 |
| TC-052 | Backend Unit | accountNumber mapping | Response field matches approved mapped value and preserves leading zeros | supporting/mapping-matrix.md - Core Field and Flow Mapping (INQTRANL-ACCNO row); supporting/requirements.md - FR-009, NFR-002 |
| TC-053 | Backend Unit | transaction date mapping | Response date format is YYYYMMDD | supporting/business-rules.md - BR-016; supporting/requirements.md - FR-009; spec.md - AC-010 |
| TC-054 | Backend Unit | transaction time mapping | Response time format is HHMMSS | spec.md - Response Field Definitions (Transaction row properties); supporting/requirements.md - FR-009 |
| TC-055 | Backend Unit | transaction reference mapping | Reference maps to approved response field shape with leading-zero preservation | supporting/mapping-matrix.md - Core Field and Flow Mapping (INQTRANL-TRAN-REF row); spec.md - Response Field Definitions (Transaction row properties) |
| TC-056 | Backend Unit | transaction type/description mapping | Only approved fields are mapped; no extra fields introduced | supporting/business-rules.md - BR-016; supporting/requirements.md - FR-009; spec.md - AC-010 |
| TC-057 | Backend Unit | amount mapping positive/negative/decimal | Decimal precision and sign are preserved for approved amounts | supporting/mapping-matrix.md - Core Field and Flow Mapping (INQTRANL-TRAN-AMOUNT row); spec.md - AC-010 |
| TC-058 | Backend Unit | composite transactionId composition | ID is sortCode-accountNumber-date-time-reference in exact component order | supporting/business-rules.md - BR-015; supporting/requirements.md - FR-008; spec.md - AC-009 |
| TC-059 | Backend Unit | fixed-width trimming/preservation where approved | Output behavior follows approved mapping/contract behavior without unevidenced transformations | supporting/mapping-matrix.md - Type Compatibility, Nullability, and Uncertainty Register; spec.md - Response Field Definitions (Transaction row properties) |

Nullability handling note:
- Unresolved deployed DB2 nullability behavior is treated as a risk and future adapter concern, not as invented Feature 005 mapping behavior.

## 12. API and OpenAPI Conformance Test Scenarios

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-060 | Contract | Method and path conformance | Runtime endpoint matches GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions | contracts/openapi.yaml - path and operationId getAccountTransactions |
| TC-061 | Contract | Path parameter conformance | sortCode/accountNumber constraints match contract patterns and required status | contracts/openapi.yaml - path parameters; spec.md - Validation Rules |
| TC-062 | Contract | Query parameter conformance | fromDate/toDate/limit/offset optionality, type, and bounds match contract | contracts/openapi.yaml - query parameter schemas |
| TC-063 | Contract | Default and normalization alignment | Contract default/min/max metadata aligns with API behavior assertions | contracts/openapi.yaml - limit/offset schema defaults and bounds; supporting/requirements.md - FR-003 |
| TC-064 | Contract | HTTP 200 populated response schema | Populated success shape conforms to TransactionListResponse and Transaction schema | contracts/openapi.yaml - 200 and components/schemas |
| TC-065 | Contract | HTTP 200 empty response schema | Empty success shape conforms with totalCount/returnedCount/transactions semantics | contracts/openapi.yaml - 200 examples emptyNoMatch and emptyOffsetBeyondTotal; spec.md - AC-007, AC-008 |
| TC-066 | Contract | HTTP 400 schema and behavior | Validation failure status and schema align with ErrorResponse | contracts/openapi.yaml - 400; spec.md - Error Handling |
| TC-067 | Contract | HTTP 500 schema and behavior | Technical-failure status and schema align with ErrorResponse and no-partial-success rule | contracts/openapi.yaml - 500; supporting/requirements.md - FR-011 |
| TC-068 | Contract | No HTTP 404 for empty transactions | Empty transaction outcomes remain HTTP 200 | contracts/openapi.yaml - 200 description; spec.md - Error Handling and AC-007 |
| TC-069 | Contract | Runtime publication reconciliation | backend/api/src/main/resources/openapi.yaml reconciles with contracts/openapi.yaml for Feature 005 path/operation/schemas | supporting/requirements.md - OR-005; plan.md - API Contract Strategy |

## 13. Frontend Component Test Scenarios

Frontend behavior authority note:
- Feature 005 frontend behavior is governed by Feature 005 spec.md and contracts/openapi.yaml.
- frontend/app implementation governs test structure and conventions.
- INQCUST-specific material in historical frontend-modernization artifacts is not authoritative for Feature 005 behavior.

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-070 | Frontend Unit/Component | Route and navigation integration for Feature 005 view | Feature 005 inquiry UI is reachable through existing frontend/app routing patterns | supporting/requirements.md - FR-012, FR-014; plan.md - Frontend Design |
| TC-071 | Frontend Unit/Component | Input entry and leading-zero preservation | Entered sortCode/accountNumber retain leading zeros in request path construction | supporting/requirements.md - FR-001, NFR-002; spec.md - API Behaviour (Path parameters) |
| TC-072 | Frontend Unit/Component | Request path construction | Request targets approved Feature 005 endpoint path | spec.md - API Behaviour; contracts/openapi.yaml - path |
| TC-073 | Frontend Unit/Component | Query composition with supplied parameters | Supplied fromDate/toDate/limit/offset are included correctly | contracts/openapi.yaml - query parameter definitions |
| TC-074 | Frontend Unit/Component | Omission of unsupplied optional dates | Unsupplied date controls are omitted from request and handled by API contract behavior | contracts/openapi.yaml - optional fromDate/toDate; supporting/requirements.md - FR-002 |
| TC-075 | Frontend Unit/Component | Loading state | Loading state is shown during in-flight inquiry | supporting/requirements.md - FR-012; spec.md - AC-013 |
| TC-076 | Frontend Unit/Component | Populated success state | Metadata and transaction rows render from 200 populated response | supporting/requirements.md - FR-012; spec.md - AC-013 |
| TC-077 | Frontend Unit/Component | Empty-success state | Empty non-error state renders for valid zero-result response | supporting/requirements.md - FR-007, FR-012; spec.md - AC-007, AC-013 |
| TC-078 | Frontend Unit/Component | Validation feedback state | User receives boundary validation feedback for invalid input | supporting/requirements.md - SR-002; spec.md - Validation Rules |
| TC-079 | Frontend Unit/Component | Technical-error state | Safe technical-error state renders for API technical failure | supporting/requirements.md - FR-011, FR-012, NFR-008; spec.md - Error Handling |
| TC-080 | Frontend Unit/Component | Safe error messaging | UI does not display SQL/stack/internal detail | supporting/requirements.md - NFR-008, SR-004 |
| TC-081 | Frontend Unit/Component | Pagination control behavior | UI controls pass approved limit/offset behavior and render coherent metadata | supporting/requirements.md - FR-003, FR-004, FR-006; spec.md - AC-003, AC-004, AC-006 |
| TC-082 | Frontend Unit/Component | Subsequent inquiry replaces prior completed result | New completed inquiry replaces prior rendered result state coherently | supporting/requirements.md - FR-012, FR-014; spec.md - AC-013, AC-015 |
| TC-083 | Frontend Unit/Component | No unrelated shell or route regression | Existing shared shell/navigation behavior remains unaffected by Feature 005 integration | supporting/requirements.md - FR-014; plan.md - Repository Integration Strategy |

## 14. End-to-End Test Scenarios

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-084 | E2E | Normal successful retrieval | User obtains populated 200 response with rendered metadata and rows | supporting/requirements.md - FR-001, FR-006, FR-012; spec.md - AC-001, AC-006, AC-013 |
| TC-085 | E2E | Supplied inclusive date boundaries | Boundary rows are included and UI reflects applied controls | supporting/requirements.md - FR-002, FR-012; spec.md - AC-002, AC-013 |
| TC-086 | E2E | Omitted date boundaries | Omitted date requests execute as approved modern API contract behavior and render coherent results | supporting/requirements.md - FR-002, FR-013; contracts/openapi.yaml - optional date controls |
| TC-087 | E2E | Empty result journey | HTTP 200 empty success renders non-error empty state | supporting/requirements.md - FR-007, FR-012; spec.md - AC-007, AC-013 |
| TC-088 | E2E | Pagination journey | Limit/offset controls produce expected page transitions and metadata | supporting/requirements.md - FR-003, FR-004, FR-006, FR-012 |
| TC-089 | E2E | Offset beyond total | HTTP 200 with preserved totalCount and empty returned page | spec.md - AC-008; contracts/openapi.yaml - emptyOffsetBeyondTotal example |
| TC-090 | E2E | Validation error journey | Invalid input produces 400-path UX feedback | supporting/requirements.md - SR-002; spec.md - Validation Rules and Error Handling |
| TC-091 | E2E | Safe technical failure journey | Technical failure yields safe error UI and no partial successful data state | supporting/requirements.md - FR-011, FR-012; spec.md - AC-012 |
| TC-092 | E2E | Subsequent inquiry replacement journey | Second inquiry replaces prior results and metadata coherently | supporting/requirements.md - FR-012, FR-014; spec.md - AC-013, AC-015 |
| TC-093 | E2E | Existing route behavior preservation | Existing implemented routes remain functional after Feature 005 integration | supporting/requirements.md - FR-014; supporting/requirements.md - OR-002, OR-009 |

## 15. Security, Logging, and Error-Exposure Tests

| Test ID | Test level | Scenario | Expected result | Authoritative trace |
|---|---|---|---|---|
| TC-094 | Controller/API | Route-security behavior alignment | New endpoint follows applicable existing repository route-security behavior | supporting/requirements.md - SR-001, OR-009 |
| TC-095 | Controller/API | No invented authorization semantics | Feature 005 behavior introduces no feature-specific authorization rule beyond approved policy | supporting/requirements.md - SR-001 |
| TC-096 | Controller/API | API error exposure safety | Error payload excludes SQL, stack traces, DB internals, and implementation details | supporting/requirements.md - NFR-008, SR-004; spec.md - Error Handling |
| TC-097 | Frontend Unit/Component | Frontend error exposure safety | UI does not display internal failure details | supporting/requirements.md - SR-004, NFR-008 |
| TC-098 | Backend Unit/Integration | Logging distinguishes empty success vs technical failure | Logs separate successful empty outcomes from technical failure outcomes | supporting/requirements.md - NFR-009; plan.md - Logging and Observability |
| TC-099 | Backend Unit/Integration | Logging data minimization | Logs avoid unnecessary exposure of full identifiers or full transaction payloads | supporting/requirements.md - NFR-007, SR-004; plan.md - Logging and Observability |
| TC-100 | Backend Unit/Integration | Correlation preservation where supported | Request identification/correlation behavior remains consistent with existing conventions | supporting/requirements.md - NFR-009, OR-008 |
| TC-101 | Repository Integration | Parameterized query construction for transaction inquiry filters | Repository count and row-retrieval queries use parameter binding for filter inputs and do not introduce dynamic string-concatenated SQL from request values | supporting/requirements.md - SR-003; plan.md - Repository Design and Security Strategy |

## 16. Regression Test Scope

Regression scope verifies that Feature 005 integration does not break existing implemented capabilities:
- INQCUST flows and tests.
- INQACC flows and tests.
- INQACCCU flows and tests.
- CRECUST flows and tests.
- Shared frontend shell and route/navigation behavior.
- Applicable shared security behavior.
- Runtime OpenAPI publication for existing endpoints and Feature 005 path addition.

Regression execution uses repository evidence and existing suites/scripts only. No nonexistent suites are invented.

## 17. Traceability Approach

Traceability model for this test specification:
- Each test case includes test ID, test level, explicit scenario, expected result, and authoritative trace.
- Trace references use verified artifact identifiers or explicit section names.
- If a behavior lacks a stable ID, reference the exact section heading in the source artifact.

Required trace format examples:
- supporting/business-rules.md - BR-###
- supporting/requirements.md - FR-### / NFR-### / SR-### / OR-###
- spec.md - US-### / AC-### / Feature Invariants / Validation Rules / Error Handling
- contracts/openapi.yaml - operation/path/schema element
- supporting/mapping-matrix.md - mapping row description

Test ID rules:
- IDs are stable sequential identifiers (TC-001 onward).
- Legacy scenario continuity is preserved by keeping existing TC-001 through TC-028 lineage and expanding with precise decomposition.
- Each test case has one primary behavior, one expected result, one assigned level, and authoritative trace grounding.
- Unrelated invalid inputs are split into separate scenarios to avoid ambiguous assertions.

Note:
- A future supporting/traceability-matrix.md may provide broader artifact-wide traceability, but this test specification is independently test-to-source grounded.

## 18. Entry Criteria

- Finalized Feature 005 upstream behavior and contract artifacts are available.
- Feature 005 plan is reconciled with current artifact set.
- Applicable implementation layer exists for the test phase being executed.
- Deterministic fixtures and H2 test setup are available for repository and integration scenarios.
- Any unresolved contradictions affecting expected behavior are documented before execution.

## 19. Exit Criteria

- All applicable automated Feature 005 tests pass.
- Required backend unit, repository integration, controller/API, contract, frontend, and E2E scenarios pass.
- OpenAPI conformance and runtime publication reconciliation checks pass.
- Applicable regression suites for existing capabilities pass.
- No unresolved critical behavior remains unverified.
- No technical-failure path returns partial successful data.
- Test evidence is available for downstream traceability and checklist review.

## 20. Assumptions, Risks, and Exclusions

Assumptions:
- H2-based verification is the approved proof-of-concept persistence verification basis.
- Existing backend and frontend test conventions remain applicable for Feature 005.
- Runtime OpenAPI publication remains reconcilable with Feature 005 contract artifact.

Risks:
- Deployed DB2 schema and nullability details remain unresolved by repository evidence.
- Legacy omitted-date runtime behavior remains unresolved in deployed DB2 context.
- Relative row order is not guaranteed for ties on both transaction date and transaction time.
- Production mainframe adapter testing remains future-scope work.

Exclusions and evidence limits:
- H2 tests verify approved proof-of-concept behavior but do not prove deployed DB2 equivalence.
- Omitted-date tests verify approved modern API contract behavior and do not prove legacy sentinel runtime outcomes.
- No live DB2, CICS, or production mainframe adapter dependency is required by this test specification.
