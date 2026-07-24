# MainframeModernization Project Onboarding Guide

## 1. Project Purpose

This project modernizes legacy mainframe banking behaviors into API-first and UI-assisted capabilities while preserving legacy business meaning, fail-code semantics, and data mapping discipline.

The modernization is not just a code rewrite. It is a controlled translation from legacy logic to modern services, with verifiable parity and clear traceability from requirement to implementation to test evidence.

## 2. What Has Been Implemented

At a high level, three business capabilities are now implemented:

1. Customer inquiry modernization.
2. Account inquiry modernization.
3. Customer create modernization (CRECUST), including backend and frontend.

For the create-customer feature specifically, the implemented core behavior is:

1. Accept a customer creation request through a modern API contract.
2. Apply legacy-aligned validation and business rules in sequence.
3. Execute credit-assessment behavior through an adapter abstraction.
4. Generate a new customer number with monotonic increment semantics.
5. Persist the created customer through a repository abstraction.
6. Return modern HTTP responses while preserving legacy status observability.

In the current POC mode, data persistence is mock-first and process-memory based. This enables deterministic local development and testing without requiring live enterprise dependencies.

## 3. The Modernization Approach We Used

We built the solution from scratch using a specification-driven lifecycle. The key principle was:

"Understand legacy behavior first, then implement only what is supported by source authority, and prove it with tests."

This prevented accidental behavior drift and ensured modernization decisions remained traceable and auditable.

## 4. SDD Basics For New Readers

SDD in this project means a structured, phase-based way to turn business behavior into working software with evidence.

SDD here is composed of these practical artifacts:

1. Specification artifact.
2. Planning artifact.
3. Data model artifact.
4. Mapping matrix.
5. API contract.
6. Task breakdown.
7. Traceability and review checklists.
8. Test specification.

What each artifact does:

1. Specification: Defines feature scope, rules, acceptance criteria, and expected outcomes.
2. Plan: Defines implementation strategy, boundaries, and architecture choices.
3. Data model: Defines request, response, and persistence structures.
4. Mapping matrix: Defines authoritative field-by-field mapping between legacy and modern representations.
5. API contract: Defines endpoint shape, payload schema, status codes, and error envelope.
6. Tasks: Converts requirements into executable engineering work.
7. Traceability/checklists: Ensures each requirement is implemented and test-covered.
8. Test spec: Defines scenario-level verification criteria.

## 5. How SDD Drove Implementation

The execution flow followed this pattern:

1. Interpret legacy behavior and copybook constraints.
2. Freeze requirements and contract boundaries.
3. Design clear layers: controller, service, repository, and adapters.
4. Implement business rules in service orchestration order.
5. Keep transport concerns in controllers and error handlers.
6. Keep persistence behind a repository abstraction.
7. Build automated tests for both success and failure semantics.
8. Verify consistency between implementation and SDD artifacts.

This approach ensured that implementation remained disciplined and explainable to both engineering and domain stakeholders.

## 6. How the Create-Customer Feature Works End-to-End

From a business-flow point of view:

1. Input arrives with customer identity, contact, address, status, and date components.
2. Validation executes for title, date-of-birth correctness, and date constraints.
3. Credit-assessment path is executed through a dedicated gateway abstraction.
4. A new customer number is allocated by controlled increment logic.
5. Customer data is normalized, mapped, and saved through repository abstraction.
6. Success response returns created identity plus legacy success indicators.
7. Failure response returns standardized error payload plus legacy fail-code visibility.

From an architecture point of view:

1. Controller remains thin.
2. Service holds business behavior.
3. Repository abstracts data persistence and future integration changes.
4. Gateway abstractions isolate external-style dependencies.
5. Error mapping is centralized for consistent API behavior.

## 7. What "Legacy Parity" Means In This Project

Legacy parity means preserving observable business outcomes, not replicating old technology.

Parity is enforced by:

1. Rule-level fail-code mapping.
2. Field-level mapping discipline.
3. Legacy status semantics in modern responses.
4. Equivalent sequence of decision points in service orchestration.
5. Acceptance criteria tied to source behavior, not assumptions.

## 8. Testing and Verification Philosophy

Verification is multi-layered:

1. Unit tests validate rule correctness and fail-code behavior.
2. API tests validate contract-level status and payload behavior.
3. Frontend tests validate user flow, validation feedback, and success rendering.
4. Regression checks ensure new functionality does not break existing capabilities.

What this gives new contributors:

1. Fast confidence in behavior changes.
2. Clear evidence when discussing parity with stakeholders.
3. Safer iteration when extending adapters or persistence strategy.

## 9. Current State and Known Boundaries

Current strengths:

1. End-to-end modernized flow exists for inquiry and create use cases.
2. Backend and frontend are operational for create-customer scenarios.
3. SDD artifacts and implementation have been aligned and versioned.

Current POC boundaries:

1. Mock-first runtime mode is used for local and demonstration environments.
2. Live enterprise integration is intentionally abstracted behind interfaces.
3. Some advanced parity branches are intentionally planned for continued hardening.

## 10. How a New Engineer Should Contribute

When adding or changing behavior, follow this order:

1. Start from requirement and acceptance criteria.
2. Update SDD artifacts first when behavior changes.
3. Implement through existing architectural boundaries.
4. Add tests for both success and failure paths.
5. Re-run verification and confirm traceability stays intact.

If you follow this sequence, your changes will remain consistent with the project's modernization standards and will be easier to review, validate, and maintain.

## 11. Quick SDD Mental Model (One-Minute Version)

Use this compact model when onboarding:

1. Spec says what the business behavior must be.
2. Plan says how we will build it.
3. Mapping says how legacy and modern fields align.
4. Contract says how external consumers interact.
5. Tasks convert intent to build steps.
6. Tests prove we built what we said we would build.
7. Reviews confirm nothing important was missed.

That is the foundation used to build this modernization effort from scratch.
