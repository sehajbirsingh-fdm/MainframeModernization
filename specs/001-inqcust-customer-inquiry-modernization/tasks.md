# Tasks: INQCUST Customer Inquiry Modernization

**Input**: Design documents from `/specs/001-inqcust-customer-inquiry-modernization/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/openapi.yaml`

## Format: `[ID] [P?] Description`

- `[P]` means task can be performed in parallel.
- Tasks reference functional requirements and business rules from `spec.md`.

## Phase 1: Setup

- [ ] T001 Create Java 21 Spring Boot 3 Maven project using package `com.fdm.bankofz.customerinquiry`.
- [ ] T002 Add dependencies: Spring Web, Validation, Actuator, springdoc-openapi, Spring Boot Test.
- [ ] T003 Add `.github/copilot-instructions.md` to repository root.
- [ ] T004 Copy `mock-data/customer-records.json` to `src/main/resources/mock-data/customer-records.json`.
- [ ] T005 Create application configuration with `inquiry.random.max-retries=1000`.

## Phase 2: Domain and DTOs

- [ ] T006 [P] Create `CustomerRecord` model from `data-model.md`.
- [ ] T007 [P] Create `AddressResponse` DTO.
- [ ] T008 [P] Create `CustomerResponse` DTO.
- [ ] T009 [P] Create `LegacyInquiryStatus` DTO.
- [ ] T010 [P] Create `RiskAssessmentResponse` DTO.
- [ ] T011 [P] Create `CustomerInquiryResponse` DTO.
- [ ] T012 [P] Create `ErrorResponse` and `FieldErrorResponse` DTOs.
- [ ] T013 [P] Create enums `CustomerStatus`, `LookupMode`, and `RiskRating`.

## Phase 3: Repository Layer

- [ ] T014 Create `CustomerRepository` interface with exact methods from `plan.md`.
- [ ] T015 Implement `MockCustomerRepository` loading mock JSON records.
- [ ] T016 Implement exact lookup by sort code and customer number. Covers FR-004.
- [ ] T017 Implement latest lookup by sort code and highest customer number. Covers FR-006 and BR-002.
- [ ] T018 Implement list-by-sort-code helper for random lookup. Covers FR-005.

## Phase 4: Services and Mapping

- [ ] T019 Create `LookupModeResolver`. Covers BR-001 and BR-002.
- [ ] T020 Create `LegacyDateConverter` for integer `YYYYMMDD` to `LocalDate`. Covers FR-009.
- [ ] T021 Create `CustomerMapper` using mapping matrix. Covers FR-008 and FR-010.
- [ ] T022 Create `LegacyStatusFactory`. Covers FR-007.
- [ ] T023 Create `RandomCustomerSelector` with deterministic testability. Covers BR-006 and BR-007.
- [ ] T024 Create `RiskAssessmentService`. Covers FR-012 and BR-008 to BR-011.
- [ ] T025 Create `CustomerInquiryService` orchestrating SPECIFIC, RANDOM, and LATEST lookup modes.

## Phase 5: API and Error Handling

- [ ] T026 Create `CustomerInquiryController` exposing `GET /api/v1/customers/{sortCode}/{customerNumber}`. Covers FR-001.
- [ ] T027 Add validation for `sortCode`. Covers FR-002.
- [ ] T028 Add validation for `customerNumber`. Covers FR-003.
- [ ] T029 Create `GlobalExceptionHandler` for 400, 404, and 500 responses.
- [ ] T030 Add structured logging for sort code, customer number, lookup mode, and outcome.

## Phase 6: Tests

- [ ] T031 [P] Test specific customer found. Covers Scenario 1.
- [ ] T032 [P] Test specific customer not found. Covers Scenario 2.
- [ ] T033 [P] Test latest customer lookup. Covers Scenario 3.
- [ ] T034 [P] Test latest customer not found. Covers BR-005.
- [ ] T035 [P] Test random customer lookup success. Covers Scenario 4.
- [ ] T036 [P] Test random lookup retry failure. Covers BR-006 and BR-007.
- [ ] T037 [P] Test invalid sort code and invalid customer number. Covers Scenario 5.
- [ ] T038 [P] Test date conversion success and invalid date failure.
- [ ] T039 [P] Test risk LOW, MEDIUM, HIGH, and reviewRequired results. Covers Scenario 6.
- [ ] T040 Create MockMvc controller tests for 200, 400, and 404 responses.

## Phase 7: Documentation and Demo

- [ ] T041 Create README with run commands, curl examples, Swagger URL, and demo talk track.
- [ ] T042 Verify OpenAPI renders in Swagger UI.
- [ ] T043 Run `mvn clean test` and confirm all tests pass.
- [ ] T044 Run service locally and execute specific, latest, random, not-found, and validation demo calls.
- [ ] T045 Review implementation against `supporting/copilot-quality-checklist.md`.

## Dependencies

- T006-T013 before T014-T025.
- T014-T018 before T025.
- T019-T024 before T025.
- T025 before T026-T030.
- T026-T030 before controller tests.
- Unit tests can start after corresponding service/model tasks are complete.
