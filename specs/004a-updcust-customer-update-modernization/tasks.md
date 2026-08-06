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
- [ ] T009a Explicitly clear legacy fail code on every success response path.

## Phase 3: Repository And Persistence
- [ ] T010 Add repository query by sortCode + customerNumber.
- [ ] T011 Add repository update operation with copybook-constrained field lengths.
- [ ] T012 Preserve immutable fields (createdDate, creditScore, creditScoreReviewDate) during update.
- [ ] T012a Correct creditScoreReviewDate response mapping to numeric yyyymmdd decomposition (do not preserve legacy raw MOVE defect).

## Phase 4: API And Error Handling
- [x] T013 Add update endpoint controller with thin orchestration only.
- [x] T014 Add standardized error envelope with correlation ID and legacy fail code.
- [x] T015 Add response mapping for trimmed output fields and ISO date conversion.
- [ ] T015a Synchronize runtime OpenAPI to include PUT /api/v1/customers/{customerNumber} with 400/401/403/404/422/500 responses.

## Phase 5: UI Integration Requirements
- [x] T016 Place Update Customer button on inquiry success view.
- [x] T017 Add edit route and pre-populate update form from selected customer.
- [ ] T018 Ensure update workflow preserves existing inquiry/create behavior.

## Phase 6: Testing
- [x] T019 Unit tests for each business rule and fail code.
- [x] T020 Service tests for conditional field updates.
- [x] T021 Controller tests for HTTP + error mappings.
- [ ] T021a Add controller security tests for unauthenticated (401) and unauthorized (403) update requests.
- [ ] T022 Integration tests for successful and failed update scenarios.
- [x] T023 Frontend tests for update-button visibility and edit-form prefill.
- [ ] T023b Add tests for explicit success fail-code clearing and creditScoreReviewDate integrity mapping.

## Phase 6b: Domain Governance
- [ ] T023c Capture SME decision on status-domain policy for parity mode and document final stance.

## Phase 6a: Security Alignment
- [ ] T023a Align security matcher/rules so `/api/v1/customers/**` update route is protected by authentication and authorization policy.

## Phase 7: Documentation
- [ ] T024 Keep mapping matrix synchronized with copybook fields.
- [ ] T025 Keep traceability matrix synchronized with COBOL rules and tests.
- [ ] T026 Add quickstart steps for manual verification of update flow.

## Exit Criteria
- [ ] QG-001 All UPDCUST fail codes implemented and test-covered.
- [ ] QG-002 No non-copybook fields added to API contract.
- [ ] QG-003 Update workflow integrated professionally from inquiry result.
- [ ] QG-004 Existing feature behavior unchanged.
- [ ] QG-005 Runtime OpenAPI and feature contract are synchronized for UPDCUST update endpoint.
- [ ] QG-006 Security negative-path coverage (401/403) is present and passing.
- [ ] QG-007 creditScoreReviewDate returns valid ISO mapping from numeric storage semantics on success.
- [ ] QG-008 Success responses always return blank fail code.
- [ ] QG-009 Status-domain policy decision is documented and approved by SME.
