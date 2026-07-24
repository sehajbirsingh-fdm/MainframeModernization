# Implementation Plan: CRECUST Customer Create Modernization

**Branch**: `004-crecust-customer-create-modernization`  
**Date**: 2026-07-22  
**Spec Authority**: `spec.md`  
**Contract Authority**: `contracts/openapi.yaml`

## Summary
This plan defines how CRECUST customer creation is modernized using an SDD lifecycle flow: discovery -> specification -> design -> tasking -> implementation -> verification. The plan preserves legacy-observable behavior from COBOL while delivering a clean API-first implementation path in POC mode.

Key preserved semantics:
- Title whitelist validation with explicit failure.
- DOB validation with legacy-equivalent boundaries.
- Sortcode-scoped customer number allocation.
- Credit score orchestration semantics and fallbacks.
- Success/fail status continuity via `legacyStatus` metadata.

## SDD Lifecycle Gates
1. **Gate 1: Analysis Complete**
- Program and copybook analysis signed off.
- Data mapping matrix and business rules complete.

2. **Gate 2: Spec Complete**
- Functional and non-functional requirements frozen in `spec.md`.
- Contract aligned in `contracts/openapi.yaml`.

3. **Gate 3: Design Complete**
- Data model, intended architecture, and testing strategy finalized.

4. **Gate 4: Implementation Ready**
- `tasks.md` complete with dependency ordering.
- Traceability matrix links every rule to tasks and tests.

5. **Gate 5: Verification Complete**
- Unit/integration/controller tests pass.
- Legacy parity checks pass for all fail-code outcomes.

## Technical Context
- **Target feature**: Create customer record from copybook-constrained payload.
- **Runtime mode**: POC mock mode only by default.
- **Legacy sources**: `CRECUST.cbl`, `CRECUST.cpy`, `CUSTOMER.cpy`, `CUSTDB2.cpy`, `CUSTCTRL.cpy`, `NEWCUSNO.cpy`.
- **Persistence**: `CustomerRepository` over mock JSON-backed data + control-state abstraction.
- **Credit check**: adapter abstraction with deterministic mock behavior.
- **Security posture**: not introduced unless explicitly requested.

## Architecture Overview
1. **Controller boundary**
- `POST /v1/customers` accepts validated request payload and delegates business logic.

2. **Service boundary**
- Executes ordered business flow:
  - title validation
  - credit score orchestration
  - DOB validation
  - customer number allocation
  - persistence
  - response mapping

3. **Repository boundary**
- `CustomerRepository` abstraction for customer writes and lookups.
- `CustomerControlRepository` abstraction for next-customer-number behavior.

4. **Credit adapter boundary**
- `CreditCheckGateway` abstraction to mimic multi-agency behavior.

5. **Mapping boundary**
- COMMAREA-compatible input mapping.
- ISO date response conversion.
- Legacy status mapping.

6. **Error mapping boundary**
- Fail codes mapped to stable API error envelope and HTTP status.

## Data Flow
1. Client submits `POST /v1/customers`.
2. Input and title validations execute.
3. Credit score module runs and returns average/fallback output.
4. DOB validation executes with legacy rules.
5. Customer-number allocator increments control state.
6. Customer record is persisted.
7. Success response returns created resource and legacy status metadata.
8. On failures, response returns mapped error with `legacyFailCode`.

## Implementation Strategy
1. Freeze contract and spec first.
2. Implement domain and DTOs from mapping matrix.
3. Implement repositories and control-number allocator in mock mode.
4. Implement service orchestration preserving order from COBOL flow.
5. Implement error and status mapping.
6. Implement comprehensive tests by fail code and rule.

## Validation Strategy
- Input validation at API boundary.
- Business-rule validation in service.
- DOB and title checks must be deterministic and unit-tested.
- All legacy fail codes in CRECUST must have explicit verification tests.

## Error Handling Strategy
- Business-rule failures map to standardized errors with `legacyFailCode`.
- Persistence/control-state unavailability maps to `503`.
- Unexpected faults map to `500`.
- Include `correlationId` in all error responses.

## Testing Strategy
- Unit tests for every business rule and fail-code branch.
- Repository tests for customer-number generation and write behavior.
- Controller tests for success and each mapped HTTP status.
- Contract tests validating request/response schema.
- Legacy parity tests for status and fail-code continuity.

## Risks and Mitigations
- **Risk**: Incorrect title normalization compared with padded COBOL values.  
  **Mitigation**: Normalize input while preserving canonical allowed set and test legacy equivalence.

- **Risk**: Customer-number race behavior not mirrored in mock mode.  
  **Mitigation**: Use atomic allocator abstraction and test monotonic sequencing.

- **Risk**: Credit-check fallback divergence from COBOL semantics.  
  **Mitigation**: Isolate gateway and preserve fallback rules in service with dedicated tests.

## Assumptions
- `SORTCODE` remains system-owned.
- Mock data file is source-of-truth for POC persistence seed.
- No front-end work is required for this feature package.

## Exit Criteria
- All tasks in `tasks.md` complete.
- All acceptance criteria in `spec.md` pass.
- Traceability matrix shows no gaps.
- Review report status is PASS or PASS WITH ACTIONS and all critical actions closed.
