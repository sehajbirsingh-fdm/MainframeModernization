# Implementation Plan: UPDCUST Customer Update Modernization

Branch: 004a-updcust-customer-update-modernization
Date: 2026-07-30
Spec: specs/004a-updcust-customer-update-modernization/spec.md

## Summary
Implement UPDCUST as a legacy-parity customer update capability with strict copybook-constrained mapping, complete fail-code coverage, and professional UI entry from inquiry results.

## Scope
In scope:
- Backend update endpoint and service flow for UPDCUST behavior.
- Copybook-constrained request/response mapping.
- Fail-code-preserving business logic.
- UI design placement requirements for update button and edit page flow (spec only).
- Automated test requirements for all parity paths.

Out of scope:
- Actual code implementation in this document set.
- Any non-UPDCUST feature expansion.

## Technical Approach
1. Define API contract and DTO fields from UPDCUST copybook.
2. Implement service logic in parity order:
   - title validation
   - minimum meaningful update gate
   - read existing customer
   - conditional field updates
   - persist update
   - map success/failure legacy status
3. Keep repository abstraction for persistence.
4. Reuse inquiry workflow as source for update navigation.

## Validation Strategy
- Unit tests for each business rule and fail code (T, 1, 2, 3, 4).
- Service tests for selective field update gating.
- Controller tests for HTTP mapping and error envelope.
- Contract tests for copybook field length constraints and date conversion.
- UI tests for update-button placement and prefill behavior.

## Risks And Mitigations
- Risk: Drift from legacy gating semantics.
  Mitigation: Rule-by-rule traceability matrix tied to COBOL evidence.

- Risk: Unexpected interpretation of blank/space values.
  Mitigation: Explicit trim and blank-handling rules in mapping matrix and tests.

- Risk: Updating title without first name in modern UI expectations.
  Mitigation: Preserve legacy parity and document clearly in API behavior.

## Deliverables
- Full feature specification set under specs/004a-updcust-customer-update-modernization.
- Supporting rule, mapping, test, and traceability artifacts.
- Implementation-ready tasks list.
