# Tasks: UPDCUST Customer Update Modernization

Input: specs/004a-updcust-customer-update-modernization/spec.md

Format: [ID] [P?] Description

## Phase 1: Analysis And Contracts
- [ ] T001 Extract UPDCUST behavior from COBOL and copybooks.
- [ ] T002 Define authoritative request/response DTO fields from UPDCUST.cpy.
- [ ] T003 Define API endpoint contract with optional sortCode fallback behavior.
- [ ] T004 Document fail-code mappings (T,1,2,3,4) and HTTP status mapping.

## Phase 2: Domain And Service Rules
- [ ] T005 Implement title allow-list validation.
- [ ] T006 Implement minimum meaningful update gate.
- [ ] T007 Implement selective update rules for name/title, phone, address, status, DOB.
- [ ] T008 Implement sortCode resolution with fallback to configured default.
- [ ] T009 Implement legacy status mapping in success and failure responses.

## Phase 3: Repository And Persistence
- [ ] T010 Add repository query by sortCode + customerNumber.
- [ ] T011 Add repository update operation with copybook-constrained field lengths.
- [ ] T012 Preserve immutable fields (createdDate, creditScore, creditScoreReviewDate) during update.

## Phase 4: API And Error Handling
- [ ] T013 Add update endpoint controller with thin orchestration only.
- [ ] T014 Add standardized error envelope with correlation ID and legacy fail code.
- [ ] T015 Add response mapping for trimmed output fields and ISO date conversion.

## Phase 5: UI Integration Requirements
- [ ] T016 Place Update Customer button on inquiry success view.
- [ ] T017 Add edit route and pre-populate update form from selected customer.
- [ ] T018 Ensure update workflow preserves existing inquiry/create behavior.

## Phase 6: Testing
- [ ] T019 Unit tests for each business rule and fail code.
- [ ] T020 Service tests for conditional field updates.
- [ ] T021 Controller tests for HTTP + error mappings.
- [ ] T022 Integration tests for successful and failed update scenarios.
- [ ] T023 Frontend tests for update-button visibility and edit-form prefill.

## Phase 7: Documentation
- [ ] T024 Keep mapping matrix synchronized with copybook fields.
- [ ] T025 Keep traceability matrix synchronized with COBOL rules and tests.
- [ ] T026 Add quickstart steps for manual verification of update flow.

## Exit Criteria
- [ ] QG-001 All UPDCUST fail codes implemented and test-covered.
- [ ] QG-002 No non-copybook fields added to API contract.
- [ ] QG-003 Update workflow integrated professionally from inquiry result.
- [ ] QG-004 Existing feature behavior unchanged.
