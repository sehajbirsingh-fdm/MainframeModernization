# CRECUST Customer Create Modernization - Implementation Tasks

**Document ID:** `tasks.md`  
**Feature:** `004-crecust-customer-create-modernization`  
**Behavior Authority:** `spec.md`  
**Contract Authority:** `contracts/openapi.yaml`  
**Plan Authority:** `plan.md`

## 1. Setup Tasks

### T001 - Confirm SDD artifact freeze
- Description: Confirm `spec.md`, `plan.md`, `data-model.md`, `mapping-matrix.md`, and `contracts/openapi.yaml` are aligned before code implementation.
- Dependencies: None.
- Done Criteria:
  - No unresolved spec/contract conflicts.
  - Traceability skeleton established.

### T002 - Create backend package skeleton
- Description: Create package structure for controller, service, repository, mapper, validation, errors, and tests.
- Dependencies: T001.
- Done Criteria:
  - Thin controller boundary established.
  - Service/repository boundaries separated.

### T003 - Prepare mock customer and control-state sources
- Description: Configure mock data using `mock-data/customer-records.json` and in-memory control-state allocator.
- Dependencies: T001.
- Done Criteria:
  - Customer records load successfully.
  - Customer number allocator supports monotonic increment by sortcode.

## 2. Domain and Contract Tasks

### T004 - Implement create-customer request DTO
- Description: Implement request DTO constrained to CRECUST copybook fields.
- Dependencies: T002.
- Done Criteria:
  - No non-copybook fields added.
  - Field-level validation annotations applied.

### T005 - Implement response DTO and legacy status DTO
- Description: Implement success response model including `legacyStatus`.
- Dependencies: T004.
- Done Criteria:
  - Contains `commSuccess` and `commFailCode`.
  - Contains generated identity and mapped customer fields.

### T006 - Implement canonical error envelope
- Description: Implement error payload including `legacyFailCode`, `correlationId`, timestamp.
- Dependencies: T004.
- Done Criteria:
  - Matches `openapi.yaml` schema.

## 3. Business Rule Tasks

### T007 - Implement title validation rule
- Description: Enforce allowed title set and fail code `T` mapping.
- Dependencies: T004.
- Done Criteria:
  - Accepts only legacy-supported titles.
  - Invalid title path returns mapped failure.

### T008 - Implement DOB validation rule set
- Description: Implement DOB checks for lower bound, future date, max age, and calendar validity.
- Dependencies: T004.
- Done Criteria:
  - Fail codes `O`, `Z`, `Y` are correctly mapped.

### T009 - Implement credit-check orchestration abstraction
- Description: Implement `CreditCheckGateway` + mock adapter to model average/fallback behavior.
- Dependencies: T002.
- Done Criteria:
  - Average score logic and fallback semantics implemented.
  - Legacy credit fail-code branches mapped.

### T010 - Implement customer-number allocation flow
- Description: Implement control-state read/increment equivalent to `GET-LAST-CUSTOMER-DB2` semantics.
- Dependencies: T003.
- Done Criteria:
  - Monotonic increment verified per sortcode.
  - Failure path maps to fail code `4`.

### T011 - Implement customer write flow
- Description: Persist customer in repository with date integer conversion and generated number.
- Dependencies: T003, T010.
- Done Criteria:
  - Persistence failure maps to fail code `1`.

### T012 - Implement service orchestration sequence
- Description: Implement full ordered flow matching CRECUST observable behavior.
- Dependencies: T007, T008, T009, T010, T011.
- Done Criteria:
  - Rule order preserved.
  - Success path sets `commSuccess=Y` and blank fail code.

## 4. API Tasks

### T013 - Implement create endpoint
- Description: Implement `POST /v1/customers` controller delegating to service.
- Dependencies: T012.
- Done Criteria:
  - Returns 201 on success.
  - Controller remains thin.

### T014 - Implement centralized error mapping
- Description: Map rule and system exceptions to canonical envelope + HTTP statuses.
- Dependencies: T006, T013.
- Done Criteria:
  - Mappings implemented for 400/422/500/503.
  - `legacyFailCode` populated when applicable.

### T015 - Implement correlation ID propagation
- Description: Generate/propagate correlation ID for success and failure responses.
- Dependencies: T013.
- Done Criteria:
  - Header and payload correlation trace is present.

## 5. Testing Tasks

### T016 - Unit tests for title rule
- Description: Test all allowed titles and invalid title cases.
- Dependencies: T007.

### T017 - Unit tests for DOB rules
- Description: Test lower-bound, future, over-age, and invalid-date cases.
- Dependencies: T008.

### T018 - Unit tests for credit orchestration
- Description: Test average score calculation and zero-score fallback.
- Dependencies: T009.

### T019 - Unit tests for number allocation
- Description: Test monotonic sequence generation and failure mapping.
- Dependencies: T010.

### T020 - Unit tests for persistence and mapping
- Description: Test date conversion and record write semantics.
- Dependencies: T011.

### T021 - Service orchestration tests
- Description: Test full happy path and fail-code branches from CRECUST.
- Dependencies: T012.

### T022 - Controller/API tests
- Description: Test endpoint binding and status mapping for success and failures.
- Dependencies: T013, T014.

### T023 - Contract conformance tests
- Description: Validate request and response schema compliance against OpenAPI.
- Dependencies: T022.

### T024 - Traceability verification tests
- Description: Ensure every requirement/rule has a test reference.
- Dependencies: T016-T023.

## 6. Documentation Tasks

### T025 - Update implementation notes
- Description: Record key implementation decisions and deviations (if any).
- Dependencies: T024.

### T026 - Update runbook and quickstart
- Description: Ensure local run/test instructions match implemented behavior.
- Dependencies: T025.

### T027 - Final review artifacts
- Description: Produce review report and close SDD verification checklist.
- Dependencies: T026.

## 7. Completion Checklist
- [ ] Spec/plan/contract alignment complete.
- [ ] All business rules implemented.
- [ ] All legacy fail codes mapped and tested.
- [ ] Endpoint returns expected success/error schemas.
- [ ] Correlation ID behavior verified.
- [ ] No out-of-scope integration introduced.
- [ ] Documentation and review artifacts finalized.
