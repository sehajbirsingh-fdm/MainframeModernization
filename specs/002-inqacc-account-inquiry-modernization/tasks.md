# INQACC Account Inquiry Modernization - Implementation Tasks

**Document ID:** `tasks.md`  
**Feature:** `002-inqacc-account-inquiry-modernization`  
**Behavior Authority:** `spec.md`  
**Contract Authority:** `contracts/openapi.yaml`  
**Technical Plan Authority:** `plan.md`

## 1. Setup Tasks

### T001 - Align module baseline to approved stack
- Description: Ensure the INQACC implementation module uses the approved stack only: Java 21, Spring Boot 3.x, Spring Web, Spring Validation, Spring Security, Spring JDBC, Jackson, React 18, and Vite.
- Dependencies: None.
- Acceptance / Done Criteria:
  - Backend build configuration is present and runnable with Java 21 and Spring Boot 3.x.
  - Frontend build configuration is present and runnable with React 18 and Vite.
  - No disallowed technology is introduced (including TypeScript, Spring Data JPA, Hibernate, and OpenFeign).
  - No live DB2 or CICS connectivity is required for POC completion. No CICS adapter is introduced.
  - JDBC database mode is permitted only through the approved `JdbcAccountRepository` boundary and remains inactive by default.
  - H2 usage, if present, is limited to automated JDBC tests only.

### T002 - Create INQACC package and folder skeleton
- Description: Create the backend and frontend folder/package structure for controller, service, repository, mapper/conversion, validation, error handling, logging/correlation, and tests.
- Dependencies: T001.
- Acceptance / Done Criteria:
  - Structure is present and consistent with repository patterns.
  - Structure separates controller, service, repository, mapping/conversion, validation, error-handling, and test responsibilities.

### T003 - Prepare mock-data source for account inquiry
- Description: Establish default mock-mode account data source for POC execution and wire it to the mock repository adapter.
- Dependencies: T001.
- Acceptance / Done Criteria:
  - Mock repository data source is available locally.
  - Mock mode is the default runtime when `app.data.mode` is absent unless project conventions require explicit declaration.
  - Data shape supports all required response fields and reserved lookup scenarios.
  - Mock mode activates only the mock repository path.
  - Mock mode does not create a `DataSource` and does not attempt a database connection.
  - Mock mode requires no database credentials.
  - No live DB2 or CICS connectivity is used.

### T004 - Configure correlation ID propagation utilities
- Description: Configure reusable correlation ID handling for request intake, response headers, and logging context.
- Dependencies: T002.
- Acceptance / Done Criteria:
  - Correlation ID handling is implemented for backend request lifecycle.
  - `X-Correlation-ID` is available for response handling and diagnostics.

## 2. Backend Tasks

### T005 - Implement account inquiry response model
- Description: Create backend response model aligned to the OpenAPI `AccountResponse` schema and required fields.
- Dependencies: T002.
- Acceptance / Done Criteria:
  - Response model includes all 12 required fields from contract.
  - Field names and formats align with `contracts/openapi.yaml`.

### T006 - Implement canonical error envelope model
- Description: Create backend error response model aligned to OpenAPI `ErrorResponse` envelope.
- Dependencies: T002.
- Acceptance / Done Criteria:
  - Error envelope includes `error.code`, `error.message`, `error.correlationId`, and `error.timestamp`.
  - Error model serialization aligns with contract.

### T007 - Implement repository interface for account inquiry
- Description: Define repository abstraction for standard composite-key lookup and reserved lookup behavior.
- Dependencies: T002.
- Acceptance / Done Criteria:
  - Interface supports lookup by sortcode + accountNumber.
  - Interface supports technical query operations required for both standard lookup and highest-account-number lookup by sortcode.
  - `AccountInquiryService` depends only on this interface.

### T008 - Implement mock repository adapter
- Description: Implement `MockAccountRepository` backed by controlled mock data only.
- Dependencies: T003, T007.
- Acceptance / Done Criteria:
  - Adapter is activated only when mock mode is selected.
  - Standard lookup path returns single account or no match.
  - Adapter supports technical lookup operations defined by `AccountRepository`.
  - Adapter is read-only and has no write operations.

### T052 - Implement JDBC repository adapter
- Description: Implement `JdbcAccountRepository` using parameterized SQL for standard and reserved lookup paths.
- Dependencies: T007.
- Acceptance / Done Criteria:
  - Adapter activates only when `app.data.mode=db`.
  - Standard lookup executes parameterized SQL using composite key fields.
  - Highest-account-number query by sortcode is implemented as repository technical lookup support.
  - Returned rows map to the same canonical domain model used by mock mode.
  - SQL statements are documented as DB2-ready where practical, but not validated against a live DB2 environment in this feature.
  - Adapter is read-only and has no write operations.

### T053 - Implement conditional data-mode configuration
- Description: Implement configuration-based repository bean selection and database-mode configuration activation.
- Dependencies: T008, T052.
- Acceptance / Done Criteria:
  - `app.data.mode=mock` selects `MockAccountRepository`.
  - `app.data.mode=db` selects `JdbcAccountRepository`.
  - No `DataSource` bean is created in mock mode.

### T054 - Implement database-mode DataSource and startup validation
- Description: Implement db-mode configuration for a Hikari-backed `DataSource` with environment-backed properties.
- Dependencies: T053.
- Acceptance / Done Criteria:
  - DB connection properties are read from environment-backed settings (`url`, `username`, `password`).
  - Optional schema and table-name configuration are supported safely.
  - Pool settings for max size and min idle are configurable for db mode.
  - Startup fails clearly when db mode is selected without valid required configuration.
  - Required DB2 JDBC driver and target schema/column assumptions are documented for future environment activation.
  - No claim is made that SQL has been verified against a live DB2 environment.
  - Database credentials are externalized and not committed to source control.

### T009 - Implement mapping and conversion boundary
- Description: Implement mapping from mock/legacy-shaped records to API response model.
- Dependencies: T005, T007.
- Acceptance / Done Criteria:
  - Both adapters return the same canonical account model used by one shared mapping boundary.
  - API response mapping is implemented once and reused across data modes.
  - Trailing spaces are trimmed for fixed-width character fields.
  - Date values are converted to ISO `yyyy-MM-dd`.
  - Numeric values are converted with correct decimal behavior.
  - Transport-only legacy fields are not emitted.

### T010 - Implement input validation boundary
- Description: Enforce path parameter validation for sortcode and accountNumber.
- Dependencies: T002.
- Acceptance / Done Criteria:
  - Sortcode validation enforces exactly 6 numeric digits.
  - Account-number validation enforces exactly 8 numeric digits.
  - Validation failures are routed to canonical 400 error handling.

### T011 - Implement inquiry application service orchestration
- Description: Implement service orchestration for standard and reserved lookup paths.
- Dependencies: T008, T009, T010, T052, T053.
- Acceptance / Done Criteria:
  - Service executes standard composite-key lookup for non-reserved account numbers.
  - Service makes the reserved `99999999` branch decision once in shared service flow.
  - Reserved branch uses repository technical lookup operation for highest-account-number resolution.
  - Service preserves read-only behavior.

### T012 - Implement controller for canonical endpoint
- Description: Implement API adapter for `GET /v1/accounts/{sortcode}/{accountNumber}`.
- Dependencies: T010, T011.
- Acceptance / Done Criteria:
  - Canonical endpoint is implemented exactly once.
  - Controller delegates business orchestration to service layer.
  - Response body and headers align with OpenAPI contract.

### T013 - Implement authentication boundary
- Description: Enforce bearer token authentication at API boundary.
- Dependencies: T001, T012.
- Acceptance / Done Criteria:
  - Missing/invalid/expired bearer token maps to 401.
  - Authenticated principal context is available to endpoint processing.

### T014 - Implement authorization boundary
- Description: Enforce inquiry permission/role checks for account inquiry access.
- Dependencies: T013.
- Acceptance / Done Criteria:
  - Authenticated but unauthorized requests map to 403.
  - Authorized requests proceed to inquiry flow.

### T015 - Implement exception-to-error mapping
- Description: Implement centralized error translation to canonical error envelope.
- Dependencies: T006, T010, T011, T012.
- Acceptance / Done Criteria:
  - Error mapping supports 400, 401, 403, 404, 500, and 503 outcomes.
  - Error responses include canonical envelope fields and correlation ID.

### T016 - Implement not-found behavior mapping
- Description: Map no-match repository outcome to canonical 404 response behavior.
- Dependencies: T011, T015.
- Acceptance / Done Criteria:
  - Valid but unmatched lookup returns 404 with canonical error envelope.
  - No-match path is distinct from validation and auth failures.
  - Not-found behavior is identical regardless of active repository mode.

### T017 - Implement service-unavailable mapping
- Description: Map transient repository/service unavailability to 503 behavior.
- Dependencies: T008, T015, T052, T054.
- Acceptance / Done Criteria:
  - Transient dependency failures produce 503 with canonical envelope.
  - Unexpected internal failures remain mapped to 500.

### T018 - Implement correlation ID propagation
- Description: Ensure correlation ID is generated or propagated and returned in responses.
- Dependencies: T004, T012, T015.
- Acceptance / Done Criteria:
  - Each response includes correlation ID traceability.
  - Correlation ID is available in logs and error envelope.

### T019 - Implement safe structured logging
- Description: Implement structured logging with safe diagnostic content only.
- Dependencies: T018.
- Acceptance / Done Criteria:
  - Logs contain operational metadata needed for traceability.
  - Logs exclude bearer tokens, account numbers, customer numbers, balances, and full account payloads.

### T020 - Enforce read-only operation boundaries
- Description: Verify backend implementation does not introduce write operations or batch workflows.
- Dependencies: T011, T012.
- Acceptance / Done Criteria:
  - No create/update/delete behavior exists in API or repository paths.
  - No additional endpoints or batch workflows are introduced.

## 3. Frontend Tasks

### T021 - Prepare React + Vite inquiry frontend baseline
- Description: Set up frontend application baseline for INQACC inquiry flow using repository-proven patterns.
- Dependencies: T001.
- Acceptance / Done Criteria:
  - React/Vite frontend runs locally.
  - Frontend structure is consistent with repository organization patterns.
  - Frontend structure keeps form input, request/state handling, and result/error rendering maintainably separated.

### T022 - Implement inquiry form inputs
- Description: Implement inquiry form with sortcode and account number inputs.
- Dependencies: T021.
- Acceptance / Done Criteria:
  - Form captures sortcode and account number values.
  - Inputs support repeat inquiry flow without page reload.

### T023 - Implement client-side validation behavior
- Description: Add client-side validation aligned to API input rules.
- Dependencies: T022.
- Acceptance / Done Criteria:
  - Sortcode validation enforces 6-digit numeric format.
  - Account-number validation enforces 8-digit numeric format.
  - Validation feedback updates correctly as input changes.

### T024 - Implement API client for account inquiry endpoint
- Description: Implement frontend API client integration for canonical endpoint.
- Dependencies: T022, T012.
- Acceptance / Done Criteria:
  - Client calls `GET /v1/accounts/{sortcode}/{accountNumber}`.
  - Client handles correlation ID exposure for support diagnostics where appropriate.
  - No additional or out-of-scope endpoints are used.

### T025 - Implement explicit UI state model
- Description: Implement explicit, testable UI states for inquiry lifecycle.
- Dependencies: T024.
- Acceptance / Done Criteria:
  - States include equivalent behavior to IDLE, LOADING, SUCCESS, NOT_FOUND, and ERROR.
  - State transitions are deterministic and testable.

### T026 - Implement loading and submission guards
- Description: Prevent duplicate submissions and unstable loading interactions.
- Dependencies: T025.
- Acceptance / Done Criteria:
  - Duplicate requests cannot be submitted while loading.
  - Disabled controls prevent click-through.
  - Loading transitions avoid visible layout jumps.

### T027 - Implement success rendering for all account fields
- Description: Render all required response fields on successful inquiry.
- Dependencies: T024, T025.
- Acceptance / Done Criteria:
  - UI displays all 12 required response fields from success payload.
  - Previous content does not flicker unnecessarily during successful refreshes.

### T028 - Implement error state rendering
- Description: Render error outcomes for validation failures, not-found, auth/authz errors, and general failures.
- Dependencies: T025.
- Acceptance / Done Criteria:
  - Not-found state is distinct from generic error state.
  - Authentication and authorization failure responses are surfaced clearly.
  - General 500/503 failures are surfaced with stable UI behavior.

### T029 - Implement stale-response protection
- Description: Prevent older in-flight responses from overwriting newer user requests.
- Dependencies: T024, T025.
- Acceptance / Done Criteria:
  - Stale responses cannot overwrite current result state.
  - Repeated rapid inquiries preserve most-recent-request correctness.

### T030 - Implement responsive and accessible inquiry view
- Description: Ensure inquiry UI remains responsive and accessible across expected viewport sizes.
- Dependencies: T022, T027, T028.
- Acceptance / Done Criteria:
  - Layout is usable on desktop and narrow/mobile widths.
  - Form and status/result regions are keyboard-usable and screen-reader-friendly.
  - Visual hierarchy remains clean and easy to use.

## 4. Integration Tasks

### T031 - Integrate frontend and backend inquiry flow
- Description: Validate end-to-end interaction between frontend inquiry UI and backend endpoint.
- Dependencies: T012, T024, T025.
- Acceptance / Done Criteria:
  - Frontend requests and backend responses interoperate for success and error paths.
  - Correlation ID behavior is observable end-to-end.

### T032 - Integrate security behavior across UI and API
- Description: Validate handling of authenticated, unauthorized, and unauthenticated paths in integrated flow.
- Dependencies: T013, T014, T028, T031.
- Acceptance / Done Criteria:
  - 401 and 403 outcomes are distinguishable and handled in UI.
  - Authorized request flow reaches success path when data exists.

### T033 - Integrate reserved lookup behavior end-to-end
- Description: Validate reserved account-number lookup behavior through full stack.
- Dependencies: T011, T024, T031.
- Acceptance / Done Criteria:
  - `accountNumber = 99999999` triggers highest-account-number behavior for the sortcode.
  - End-to-end response format matches success contract.

## 5. Testing Tasks

### T034 - Implement service-layer tests
- Description: Add tests for service orchestration including standard lookup, reserved lookup, and read-only behavior.
- Dependencies: T011.
- Acceptance / Done Criteria:
  - Service tests cover positive and negative paths.
  - Reserved branch logic is explicitly tested.

### T035 - Implement controller/API tests
- Description: Add tests for endpoint binding, status mappings, and response contract alignment.
- Dependencies: T012, T015.
- Acceptance / Done Criteria:
  - Tests cover 200, 400, 401, 403, 404, 500, and 503 outcomes.
  - Endpoint path and parameter behavior align with contract.

### T036 - Implement repository adapter tests
- Description: Add tests for mock repository standard and reserved lookups.
- Dependencies: T008.
- Acceptance / Done Criteria:
  - Standard composite-key lookup behavior is verified.
  - Highest-account-number repository technical lookup behavior is verified.
  - Mock-mode repository activation is verified.
  - Mock mode confirms no `DataSource` creation.

### T055 - Implement JDBC repository tests with test-only relational database
- Description: Add JDBC repository tests using a test-only relational database (H2) for query and mapping verification.
- Dependencies: T052, T054.
- Acceptance / Done Criteria:
  - H2 is used only for automated JDBC tests.
  - Parameterized SQL query behavior is verified.
  - Row mapping to canonical domain model is verified.
  - Highest-account-number repository technical lookup behavior is verified.
  - Test results are not represented as live DB2 connectivity verification.

### T056 - Implement data-mode configuration and startup-failure tests
- Description: Add tests for repository bean selection and startup validation across mock and db modes.
- Dependencies: T053, T054.
- Acceptance / Done Criteria:
  - Mock mode selects `MockAccountRepository` and no `DataSource`.
  - DB mode selects `JdbcAccountRepository` with `DataSource`.
  - Missing required db-mode configuration fails startup clearly.

### T057 - Implement service parity tests across repository implementations
- Description: Add service tests proving the same service behavior with mock and jdbc repositories.
- Dependencies: T011, T055.
- Acceptance / Done Criteria:
  - Service behavior for success, not-found, and reserved lookup is equivalent across both modes.
  - Read-only behavior is preserved across both modes.

### T037 - Implement mapper and conversion tests
- Description: Add tests for trimming, date conversion, and decimal conversion behavior.
- Dependencies: T009.
- Acceptance / Done Criteria:
  - Fixed-width character trimming is verified.
  - ISO date conversion is verified.
  - Decimal conversion behavior is verified.

### T038 - Implement validation tests
- Description: Add tests for sortcode and account-number input validation rules.
- Dependencies: T010.
- Acceptance / Done Criteria:
  - Valid and invalid boundary cases are covered for both fields.
  - Validation failures map to canonical 400 behavior.

### T039 - Implement error-handling tests
- Description: Add tests for centralized exception mapping to canonical error envelope.
- Dependencies: T015, T016, T017.
- Acceptance / Done Criteria:
  - Envelope fields are present for all error outcomes.
  - Status-to-error mapping is correct for 400/401/403/404/500/503.

### T040 - Implement authentication and authorization tests
- Description: Add tests for bearer authentication and role-based authorization behavior.
- Dependencies: T013, T014.
- Acceptance / Done Criteria:
  - Missing/invalid/expired token paths are verified as 401.
  - Authenticated-but-unauthorized path is verified as 403.

### T041 - Implement correlation ID and safe logging tests
- Description: Add tests for correlation ID propagation and safe logging behavior where practical.
- Dependencies: T018, T019.
- Acceptance / Done Criteria:
  - Correlation ID is present in response and error paths.
  - Logs do not include prohibited sensitive fields.

### T042 - Implement frontend validation and state tests
- Description: Add frontend tests for validation updates, explicit UI states, and state transitions.
- Dependencies: T023, T025.
- Acceptance / Done Criteria:
  - Validation feedback updates correctly while editing.
  - State transitions for idle/loading/success/not-found/error are covered.

### T043 - Implement frontend loading and duplicate-submission tests
- Description: Add frontend tests ensuring stable loading behavior and submission guards.
- Dependencies: T026.
- Acceptance / Done Criteria:
  - Duplicate submission prevention is verified.
  - Disabled controls do not permit click-through.
  - Loading does not cause visible layout jumps.

### T044 - Implement frontend rendering and repeat-inquiry tests
- Description: Add frontend tests for success rendering, error rendering, and repeat inquiry behavior.
- Dependencies: T027, T028.
- Acceptance / Done Criteria:
  - All 12 fields render correctly on success.
  - Not-found and general error rendering are verified.
  - Repeat inquiries are verified without stale-state regressions.

### T045 - Implement stale-response protection tests
- Description: Add frontend tests ensuring stale responses cannot overwrite newer requests.
- Dependencies: T029.
- Acceptance / Done Criteria:
  - Out-of-order async responses are handled safely.
  - Most recent inquiry result remains authoritative in UI state.

### T046 - Implement backend/frontend integration tests
- Description: Add tests validating integrated request lifecycle across frontend and backend boundaries.
- Dependencies: T031, T032, T033, T056.
- Acceptance / Done Criteria:
  - Integration tests cover success, not-found, and auth error flows.
  - Correlation behavior and envelope consistency are verified.
  - Where practical, API-visible behavior is confirmed unchanged between mock and db modes.

### T047 - Implement OpenAPI conformance tests
- Description: Add automated checks that implementation conforms to frozen OpenAPI contract.
- Dependencies: T035, T046.
- Acceptance / Done Criteria:
  - Implemented endpoint, parameters, statuses, and schemas conform to `contracts/openapi.yaml`.
  - Contract conformance checks run in local verification workflow.

## 6. Documentation Tasks

### T048 - Verify specification and OpenAPI alignment
- Description: Verify frozen `spec.md` and `contracts/openapi.yaml` remain aligned for endpoint, status semantics, and response/error schema obligations.
- Dependencies: None.
- Acceptance / Done Criteria:
  - Any genuine mismatch is documented as a blocker for human review.
  - If an approved correction is required, only contract-alignment updates are applied without behavioral redesign.

### T049 - Verify implementation-to-contract conformance notes
- Description: Document how implemented backend/frontend behavior maps to frozen contract and plan boundaries.
- Dependencies: T047.
- Acceptance / Done Criteria:
  - Conformance notes exist for endpoint, statuses, security behavior, correlation ID, and both repository modes.
  - Notes reference supporting artifacts without duplicating them.

### T050 - Update run and verification documentation
- Description: Update README/run instructions for backend and frontend startup, test execution, and mock-data usage.
- Dependencies: T031, T046.
- Acceptance / Done Criteria:
  - Local run instructions for backend and frontend are accurate.
  - Test execution instructions are accurate.
  - Mock mode execution and limitations are documented.
  - DB mode enablement is documented, including required environment variables, driver requirement, and expected schema/table assumptions.
  - Documentation states that live DB2 connectivity is not required for POC completion.
  - Documentation states that database mode is designed for future DB2 activation but live DB2 connectivity is not verified in this POC.
  - Documentation states that deployment credentials remain external.

## 7. Deployment / Readiness Tasks

### T051 - Perform POC readiness verification
- Description: Perform final POC readiness pass for build/run/test/documentation completeness within approved scope.
- Dependencies: T047, T050.
- Acceptance / Done Criteria:
  - Backend build succeeds locally.
  - Frontend build succeeds locally.
  - Required automated tests pass.
  - No out-of-scope deployment or production-infrastructure assumptions are introduced.

## 8. Completion Checklist

- [ ] All implementation tasks in this document are complete.
- [ ] Backend builds successfully.
- [ ] Frontend builds successfully.
- [ ] Test suites pass.
- [ ] Canonical endpoint is implemented: `GET /v1/accounts/{sortcode}/{accountNumber}`.
- [ ] Standard composite-key lookup works.
- [ ] Reserved `99999999` lookup works with highest-account-number behavior.
- [ ] Required response fields are returned.
- [ ] Required HTTP statuses are implemented: 400, 401, 403, 404, 500, 503.
- [ ] Security behavior is verified (bearer auth + authorization boundary).
- [ ] Correlation ID behavior is implemented and verified.
- [ ] Safe structured logging behavior is verified.
- [ ] Mock repository is the default POC runtime. JDBC database mode is implemented but inactive by default. No live DB2 or CICS connection is required or used for POC acceptance.
- [ ] One shared controller and one shared service flow are used across both data modes.
- [ ] Both adapters implement the same repository contract and return the same canonical account model.
- [ ] Reserved-number branch decision exists only in shared service flow.
- [ ] No `DataSource` is created in mock mode.
- [ ] Database mode is configuration-driven and uses JDBC with Hikari-backed `DataSource` and parameterized SQL.
- [ ] DB2 driver requirement and schema/table assumptions are documented for future activation.
- [ ] No claim of live DB2 connectivity verification is made.
- [ ] No duplicate business logic exists across repository implementations.
- [ ] Frontend is stable, responsive, and visually usable.
- [ ] Repeated submissions behave correctly.
- [ ] Loading transitions do not flicker or allow stale results to overwrite newer responses.
- [ ] Implementation conforms to `contracts/openapi.yaml`.
- [ ] Documentation is updated.
- [ ] No out-of-scope functionality was introduced.