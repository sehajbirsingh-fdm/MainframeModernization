# INQACCCU Pre-Implementation Test Specification

## 1. Test Objectives

- Define required verification for INQACCCU backend and frontend implementation before execution begins.
- Ensure planned tests cover frozen requirements, frozen spec outcomes, and frozen OpenAPI contract obligations.
- Define scenario coverage for normal, boundary, and failure paths without changing approved behavior.
- Establish traceability expectations from frozen artifacts to implementation tasks and test evidence.

## 2. Scope

In scope:

- Backend inquiry endpoint, orchestration, validation, retrieval, mapping, and error handling for INQACCCU.
- Frontend inquiry experience in existing src/frontend-react module for input, submission, loading, and result/error presentation.
- Backend/frontend integration flow and response-contract conformance to contracts/openapi.yaml.
- Cross-cutting checks required by plan.md: safe logging/correlation and existing project security behavior at integration boundary.

Out of scope:

- New business capabilities, new inquiry modes, and non-approved endpoint behavior.
- OpenAPI contract changes.
- Production performance certification and production operations sign-off.

## 3. Test Levels

Required test levels:

- Backend unit tests.
- Mapper/transformation tests.
- Service orchestration tests.
- Repository/adapter tests.
- Controller and exception-handler tests.
- Spring integration tests.
- Frontend unit/component tests.
- Frontend input-validation tests.
- Frontend API-client tests.
- Frontend integration tests.
- Browser-level end-to-end tests.
- OpenAPI contract-conformance tests.

## 4. Test Environment and Configuration

Backend environment:

- Java 21 + Spring Boot 3.x module in src/api.
- Maven-based test execution.
- Spring test support and module-configured test dependencies from repository baseline.
- Runtime/config profile values required for feature endpoint and adapter wiring.

Frontend environment:

- Existing src/frontend-react module.
- Repository-configured React + TypeScript + Vite toolchain.
- Module test tooling already present in repository baseline.
- Frontend runtime configuration for backend base URL/proxy and request timeout.

Integrated execution environment:

- Local backend and frontend runtime started with feature configuration.
- Controlled mock data and adapter behavior for deterministic scenario coverage.

## 5. Test Data Strategy

Data sets and fixtures must cover:

- Valid customer with one or more accounts.
- Valid customer with zero accounts.
- Nonexistent customer.
- Reserved customer numbers 0000000000 and 9999999999 as syntactically valid customer-not-found business outcomes.
- Maximum account-return boundary (20 returned entries).
- Identifier values with leading zeroes in customer and account identifiers.
- Representative account dates that verify transformation to external date format.
- Retrieval-stage failure path simulation for failCode 2, 3, and 4.
- Infrastructure failure simulation distinct from business-outcome response flow.
- Invalid frontend input set for client-side validation feedback.

Data constraints:

- Deterministic data/mocks are required for repeatable automated verification.
- No unsupported business cases are introduced.

## 6. Backend Test Scenarios

| Scenario ID | Scenario | Primary Level | Verification Criteria | Traceability |
|---|---|---|---|---|
| BE-001 | customerNumber validation rejects missing/blank/non-digit/incorrect length | Controller + exception-handler | HTTP 400 payload matches validation-error schema contract | FR-001, AC-011, T005, T024 |
| BE-002 | reserved values remain in business flow, not validation failure | Service orchestration + controller | 0000000000/9999999999 produce business outcome response and not HTTP 400 | FR-003, AC-002, AC-003, T010, T024 |
| BE-003 | customer validation occurs before account retrieval | Service orchestration | Retrieval adapter is not invoked when customer validation path fails | FR-002, BR-004, T006, T010, T022 |
| BE-004 | account retrieval uses internally derived sort code behavior | Service + repository | Retrieval call path uses derived sort code behavior with validated customer number | FR-004, BR-002, BR-011, T007, T010 |
| BE-005 | retrieval path is read-only | Repository/adapter | Retrieval operation executes read-only inquiry behavior without write side effects | FR-005, BR-001, TC-001, T007, T023 |
| BE-006 | bounded maximum returned accounts | Service + repository | numberOfAccounts and accounts collection are capped to maximum 20 | FR-007, AC-009, TC-003, T007, T010, T023 |
| BE-007 | end-of-data treated as normal completion | Repository + service | End-of-data path yields non-error business completion | FR-008, BR-016, T007, T010, T023 |
| BE-008 | customer-not-found business outcome mapping | Service + mapper | legacyStatus/failCode/customerFound and empty accounts mapped correctly | FR-009, FR-010, AC-002, AC-003, T009, T022 |
| BE-009 | valid customer zero-account outcome mapping | Service + mapper | success outcome with numberOfAccounts=0 and empty accounts | FR-006, AC-008, T009, T022 |
| BE-010 | valid customer with accounts outcome mapping | Service + mapper | account list response includes required fields and aligned count | FR-011, AC-001, T008, T009, T022 |
| BE-011 | retrieval open-stage failure mapping | Service + mapper | failCode 2 business response mapping verified | FR-010, AC-005, BR-015, T009, T010, T022 |
| BE-012 | retrieval fetch-stage failure mapping | Service + mapper | failCode 3 business response mapping verified | FR-010, AC-006, BR-015, T009, T010, T022 |
| BE-013 | retrieval close-stage failure mapping | Service + mapper | failCode 4 business response mapping verified | FR-010, AC-007, BR-015, T009, T010, T022 |
| BE-014 | infrastructure failures remain separate from business outcomes | Controller + exception-handler | HTTP 500 uses infrastructure-error schema and does not include business payload fields | Spec infrastructure error contract, T024 |
| BE-015 | identifier and leading-zero preservation | Mapper/transformation | customerNumber/accountNumber and other identifiers preserve leading zeroes in output | FR-012, FR-021, AC-013, T008, T022 |
| BE-016 | date transformation to external representation | Mapper/transformation | account date fields are transformed to external format required by frozen contract | FR-013, AC-010, T008, T022 |
| BE-017 | numberOfAccounts aligns with returned collection | Service + mapper | response count equals actual returned account collection size | FR-009, T009, T010, T022 |
| BE-018 | business response payload schema conformance | Controller + contract conformance | HTTP 200 payloads conform to CustomerAccountInquiryResponse schema | OpenAPI 200 schema, T012, T028 |
| BE-019 | validation-error payload schema conformance | Controller + contract conformance | HTTP 400 payloads conform to ValidationErrorResponse schema | OpenAPI 400 schema, T012, T028 |
| BE-020 | infrastructure-error payload schema conformance | Controller + contract conformance | HTTP 500 payloads conform to InfrastructureErrorResponse schema | OpenAPI 500 schema, T012, T028 |
| BE-021 | security behavior under existing project policy | Spring integration | Endpoint behavior follows existing configured security policy in module baseline | Plan security strategy, T024, T027 |
| BE-022 | correlation and safe logging behavior | Spring integration + observability checks | Request correlation and safe logging behavior match plan cross-cutting expectations | Plan logging/observability, T027 |

## 7. Frontend Test Scenarios

| Scenario ID | Scenario | Primary Level | Verification Criteria | Traceability |
|---|---|---|---|---|
| FE-001 | inquiry page renders in existing app route | Frontend component/integration | Inquiry page is accessible and visible through configured route | FR-015, T016, T019, T025 |
| FE-002 | customer-number input accepts expected inquiry values | Frontend component | Input field supports expected customer-number entry interactions | FR-015, FR-021, T016, T025 |
| FE-003 | client-side validation and field-level feedback | Frontend validation/component | Invalid customer-number inputs show field feedback before request dispatch | FR-019, T015, T025 |
| FE-004 | invalid submission does not invoke backend | Frontend validation/API-client test | No API call is issued for client-side invalid input | FR-019, T015, T026 |
| FE-005 | valid request construction for inquiry submit | API-client + component | Request path/parameter construction uses entered customer number and configured base path | FR-015, Spec endpoint, T014, T026 |
| FE-006 | loading presentation during active request | Frontend component/integration | UI presents loading state while request is in progress | Spec frontend loading behavior, T016, T025 |
| FE-007 | successful account-result presentation | Frontend component | UI renders returned account fields for customer-found-with-accounts outcome | FR-016, T017, T025 |
| FE-008 | identifier preservation in display | Frontend component | Display preserves leading zeroes in shown identifiers | FR-021, AC-013, T017, T025 |
| FE-009 | zero-account outcome presentation | Frontend component | UI presents distinct zero-account outcome state | FR-017, T016, T025 |
| FE-010 | customer-not-found outcome presentation | Frontend component | UI presents distinct customer-not-found outcome state | FR-018, T016, T025 |
| FE-011 | business retrieval-failure presentation | Frontend component | UI presents business failure outcomes returned via legacy status payload | Spec retrieval failure behavior, T016, T025 |
| FE-012 | validation-error response presentation | Frontend integration | HTTP 400 payload is presented as validation feedback state | FR-019, OpenAPI 400 schema, T016, T025 |
| FE-013 | infrastructure and network error presentation | Frontend integration/API-client | UI presents distinct system/network error outcomes for non-business failures | FR-020, OpenAPI 500 schema, T016, T026 |
| FE-014 | subsequent inquiry by updating input and resubmitting | Frontend integration | User can update input and submit another request in same flow | FR-022, Spec subsequent inquiry behavior, T018, T025 |
| FE-015 | newly completed inquiry result replaces prior view | Frontend integration | UI shows newly completed inquiry result after resubmission | FR-022, T018, T025 |
| FE-016 | stale result/error isolation across inquiries | Frontend integration | Stale results/errors from prior inquiry do not leak into subsequent inquiry outcome | Spec frontend outcome transition behavior, T018, T025 |
| FE-017 | frontend/backend contract alignment for consumed fields | Frontend API-client + component | Consumed and rendered fields align with frozen business and error response schemas | OpenAPI schemas, T014, T017, T026 |

## 8. Integration and End-to-End Scenarios

| Scenario ID | Scenario | Primary Level | Verification Criteria | Traceability |
|---|---|---|---|---|
| E2E-001 | full happy path with accounts | Browser-level E2E | user input -> frontend validation -> API -> orchestration -> retrieval -> mapping -> frontend result presentation completes successfully | AC-001, T027 |
| E2E-002 | full valid zero-account path | Browser-level E2E | full flow completes with zero-account outcome presentation | AC-008, T027 |
| E2E-003 | full customer-not-found path | Browser-level E2E | full flow completes with customer-not-found outcome presentation | AC-002, AC-003, T027 |
| E2E-004 | full retrieval failure path | Browser-level E2E | full flow returns business failure payload and frontend presents failure outcome | AC-005, AC-006, AC-007, T027 |
| E2E-005 | full validation failure path | Browser-level E2E | invalid input path produces validation feedback and contract-conformant response handling | AC-011, T027 |
| E2E-006 | full infrastructure failure path | Browser-level E2E | infrastructure error response is handled and presented distinctly from business outcomes | Spec infrastructure error contract, T027 |
| E2E-007 | full subsequent inquiry flow | Browser-level E2E | second inquiry after first completion renders newly completed result without stale leakage | FR-022, T027 |

## 9. Contract-Conformance Testing

Contract-conformance verification requirements:

- Frozen contracts/openapi.yaml is the sole schema authority for API conformance checks.
- Automated conformance checks must validate:
	- path and parameter conformance for GET /api/v1/customers/{customerNumber}/accounts
	- HTTP 200 business payload schema conformance
	- HTTP 400 validation-error payload schema conformance
	- HTTP 500 infrastructure-error payload schema conformance
	- required fields and enum constraints in legacyStatus and error types
- Example payload consistency should be verified as representative conformance fixtures.
- Contract-conformance tests must not modify or regenerate the frozen OpenAPI contract.

## 10. Non-Functional and Cross-Cutting Verification

- Logging safety: verify no sensitive payload leakage and expected operational log behavior.
- Correlation: verify request correlation behavior required by implementation plan.
- Stability: verify deterministic behavior of bounded account returns and repeated inquiry interactions.
- Security boundary behavior: verify behavior under existing project security policy only; no new security semantics are introduced.

## 11. Entry and Exit Criteria

Entry criteria:

- Frozen artifacts are available and unchanged: requirements.md, spec.md, plan.md, tasks.md, contracts/openapi.yaml, and required supporting artifacts.
- Backend and frontend repository modules are available and buildable in local development environment.
- Controlled test data/mocks are prepared for required scenario matrix.
- Local/integration configuration is available for backend and frontend test execution.

Exit criteria:

- Required scenario set in Sections 6-10 is implemented at designated test levels.
- Applicable backend and frontend suites pass in configured execution environments.
- Integrated inquiry flow scenarios are verified.
- OpenAPI conformance verification is completed for required response families.
- No unresolved critical defects remain for frozen-scope behavior.
- Traceability links from artifact identifiers to implemented test evidence are completed.

## 12. Traceability Expectations

Expected traceability chain:

- Frozen requirement/spec/openapi identifier -> implementation task ID -> test scenario ID -> implemented test evidence reference.

Identifier sources used in this document:

- Requirement IDs: FR-001 through FR-022 from requirements.md.
- Spec acceptance criteria IDs: AC-001 through AC-013 from spec.md.
- Business rule IDs: BR-001 through BR-016 and TC-001 through TC-005 from business-rules.md where behavior mapping is needed.
- Task IDs: T001 through T031 from tasks.md, with test implementation concentration in T022-T028.

Implementation evidence expectations:

- Traceability artifacts include scenario ID to implemented test evidence mapping.
- Implemented test evidence includes concrete test file references.

## 13. Risks and Assumptions

Assumptions:

- Frozen artifacts remain authoritative for all test design decisions.
- Existing module test toolchains and local runtime configuration are available for planned test levels.
- Controlled data/mocks can represent all required business and failure scenarios without changing approved behavior.

Risks to test execution readiness:

- Runtime/front-end integration drift may obscure whether failures are contract or integration defects.
- Incomplete failure-path simulation data may block verification of retrieval-stage failCode scenarios.
- Contract drift between runtime implementation and frozen OpenAPI can invalidate integration assumptions.

Risk controls in test design:

- Separate unit, integration, and contract-conformance layers for precise defect localization.
- Deterministic scenario fixtures for each required behavior and failure path.
- Explicit scenario-level traceability to frozen requirements/spec/openapi/task identifiers.
