# Implementation Plan: INQACCCU Customer Account Relationship Modernization

## Architecture Overview
This feature is implemented as a read-only inquiry capability that preserves legacy-observable behavior while exposing the frozen external contract. The implementation follows the architecture boundaries in supporting/architecture.md:

- Frontend interaction boundary provides the user-facing inquiry channel and backend API integration path.
- Interface boundary receives inquiry input and returns mapped outcomes.
- Orchestration boundary enforces sequence: validate customer first, retrieve accounts second, then map outcomes.
- Customer-validation boundary encapsulates INQCUST-equivalent validation behavior.
- Account-data access boundary performs retrieval using validated customer context and internally derived sort code.
- Transformation boundary preserves identifier semantics and maps legacy date/data meanings into the external representation required by the frozen contract.
- Outcome mapping boundary preserves legacy status distinctions while keeping infrastructure failures separate from business outcomes.

The implementation plan does not redefine API contract details; those remain authoritative in spec.md and contracts/openapi.yaml.

## Technology Stack
Technology choices are derived from repository reality in src/api and src/frontend-react, constrained by the frozen artifacts:

- Java 21 runtime.
- Spring Boot 3.x application framework.
- Spring Web for REST interface implementation.
- Spring Validation (Bean Validation) for request parameter validation.
- Spring Security (already present in project dependencies) integrated according to current service policy.
- Spring JDBC for repository-oriented data access abstractions.
- Jackson JSON processing through Spring Boot web stack.
- Spring Boot Actuator for operational health/observability endpoints.
- Maven as the primary build/test runtime path in this repository module.
- JUnit 5 and Spring Boot Test for unit and web-layer testing.
- H2 test dependency for isolated repository/adapter tests where needed.
- Existing frontend module at src/frontend-react using React + TypeScript + Vite (repository-configured versions).
- React Router for frontend route composition in the existing UI module.
- Frontend API communication and input-validation libraries already present in the frontend module (React Query and Zod).
- Frontend test/build toolchain already present in the module (Vitest, Testing Library, Playwright, npm scripts).

No additional technology is introduced by this plan.

## Project Structure
Implementation aligns with the existing backend and frontend repository layout:

- src/api: backend inquiry API implementation module.
- src/frontend-react: existing frontend demonstration application module.

Backend layout:

- src/main/java/.../controller: request handling and response dispatch.
- src/main/java/.../service: inquiry orchestration and flow control.
- src/main/java/.../repository: customer-validation and account-retrieval abstractions/adapters.
- src/main/java/.../domain: internal domain models and status representations.
- src/main/java/.../mapper: legacy-to-contract transformation and outcome mapping.
- src/main/java/.../support: configuration and cross-cutting wiring.
- src/main/resources: application configuration and runtime contract artifacts.
- src/test/java/...: unit, slice, and integration-style tests.

Frontend layout:

- src/frontend-react/src/features: feature-oriented UI flows and screens.
- src/frontend-react/src/api: backend communication clients and request wiring.
- src/frontend-react/src/domain: frontend-side domain typing aligned to backend contract usage.
- src/frontend-react/src/config: runtime endpoint and environment configuration.
- src/frontend-react/src/test: frontend test utilities and shared test setup.

Feature-specific implementation should follow this structure and avoid cross-layer leakage.

## Component Responsibilities
### Frontend Inquiry Module
- Implements inquiry page behavior within the existing frontend application structure.
- Handles input capture, frontend-side validation feedback, loading state transitions, and result/error rendering.
- Invokes backend inquiry API and presents inquiry outcomes.

### Inquiry Controller
- Accepts validated route input and delegates to service orchestration.
- Remains thin and free of business decision logic.
- Emits responses in shapes defined by frozen OpenAPI contract.

### Inquiry Orchestration Service
- Executes deterministic flow order: validation, retrieval, transformation, outcome mapping.
- Applies record-cap logic aligned to preserved legacy maximum.
- Ensures read-only behavior and no write-side effects.

### Customer Validation Adapter
- Encapsulates customer-existence checks as a dedicated capability.
- Distinguishes invalid input handling from customer-not-found business outcomes in cooperation with validation and mapping layers.

### Account Retrieval Adapter
- Retrieves account records for validated customers.
- Uses internally derived fixed sort code behavior required by frozen artifacts.
- Preserves non-deterministic ordering and normal end-of-data completion semantics.

### Mapping and Transformation
- Preserves fixed-width identifiers and leading zeroes as strings across boundaries.
- Maps legacy status indicators into the frozen contract fields.
- Converts account date values to the externally required format while preserving legacy date meaning.

### Error Mapping
- Maps validation failures to contract-specific validation-error payloads.
- Maps non-business infrastructure failures to contract-specific infrastructure-error payloads.
- Prevents leakage of business response payload fields into non-business error responses.

## Layered Architecture
The feature will follow strict layering:

- Frontend interaction layer: inquiry page input handling, API invocation, and outcome rendering.
- Presentation layer: controller and exception translation.
- Application layer: orchestration service coordinating use-case flow.
- Domain/mapping layer: status, account, and response mapping logic.
- Data access layer: repository interfaces and adapters for validation/retrieval.
- Cross-cutting layer: security configuration, logging, correlation, and observability.

Dependencies flow inward only: frontend -> API boundary -> controller -> service -> repository/mapper. Repository adapters do not depend on controller concerns.

## Request and Data Flow
High-level runtime flow:

1. User enters inquiry input in the existing frontend application inquiry page.
2. Frontend applies client-side input checks and, when valid, submits request to the backend API.
3. Backend request enters controller and contract validation is applied at the API boundary.
4. Service orchestrator invokes customer-validation capability.
5. Service invokes account retrieval through repository abstraction when validation path allows retrieval.
6. Retrieval results are transformed and mapped to contract-facing response payload.
7. Controller/exception handlers return mapped business or error response schema.
8. Frontend resolves loading state and renders the returned result or error presentation.
9. For a subsequent inquiry, frontend reuses the same interaction flow with updated input.

This flow preserves architecture constraints without re-specifying endpoint contract definitions.

## Data Access Strategy
- Define clear repository interfaces for customer validation and account retrieval responsibilities.
- Keep retrieval read-only and side-effect free.
- Enforce the preserved max-return constraint at query or post-query boundary so external response never exceeds baseline.
- Preserve abstraction boundaries so mock/backing implementations can be substituted without service-layer changes.
- Treat end-of-data as normal completion path, not as an exception path.

## Validation Strategy
- Use Bean Validation on inbound route parameters to enforce contract-level shape rules.
- Centralize validation exception translation to the frozen validation error schema.
- Keep business validation logic (customer existence and retrieval-stage outcomes) in service/repository orchestration rather than framework parameter validation.
- Ensure syntactically valid reserved values follow business outcome mapping rather than validation rejection.
- Apply frontend input validation to improve user feedback before request submission while preserving backend contract validation as the authority.

## Error Handling Strategy
- Use a centralized exception handling mechanism for non-business HTTP 400 and HTTP 500 responses.
- Keep business outcomes in the normal service response path with legacy status mapping.
- Map retrieval-stage failure categories through business response mapping, not infrastructure error schema.
- Reserve infrastructure schema for non-business processing failures.
- Ensure response payload separation is strict between business and error contracts.
- Map backend error responses in the frontend to clear validation/system error presentation states without changing backend semantics.

## Mapping Strategy
- Isolate transformation code in mapper components to avoid contract logic duplication.
- Map legacy status fields to success/failCode/customerFound consistently.
- Derive numberOfAccounts from transformed account collection and enforce alignment with returned list size.
- Preserve all supported account fields and avoid introducing unsupported portfolio or relationship metadata.
- Normalize date output at mapping boundary to the frozen external representation.

## Security Strategy
Security requirements are not newly defined by the frozen business requirements; therefore the implementation will:

- Integrate with the existing application security framework already present in the module.
- Apply authentication/authorization policy through configuration and security components, not business logic classes.
- Keep endpoint behavior contract-consistent regardless of security backend implementation.
- Avoid introducing new security products or protocol assumptions outside current project architecture.

## Logging and Observability
- Use structured, level-appropriate logging at controller/service/repository boundaries.
- Include correlation identifiers across request lifecycle for diagnosability.
- Log outcome categories and failure paths without exposing sensitive payload details.
- Use Actuator-based operational visibility for health and runtime diagnostics.
- Record infrastructure exceptions with sufficient technical context for troubleshooting while keeping external responses contract-safe.

## Configuration Strategy
- Externalize runtime behavior via Spring configuration properties and profiles.
- Keep integration mode and adapter wiring configurable through support/configuration classes.
- Maintain clear defaults suitable for local and test execution.
- Keep contract artifacts and validation settings version-aligned with frozen feature documents.
- Use existing frontend runtime configuration for backend base URL, proxy behavior, and request timeout in src/frontend-react.

## Testing Strategy
Testing will be layered to validate behavior preservation and contract fidelity:

- Unit tests for orchestration logic, status mapping, and transformation rules.
- Unit tests for validation/error mapping components.
- Web MVC/controller tests for response status and payload-shape guarantees per contract classes.
- Repository adapter tests for validation and retrieval semantics, including max-record and end-of-data handling.
- Integration-style tests that exercise end-to-end request flow within Spring context using controlled test data.
- Frontend component tests for inquiry page rendering, input handling, loading states, and result/error presentation.
- Frontend validation tests for input-check behavior and backend-validation response rendering.
- Frontend API integration tests for request construction, response handling, and inquiry outcome rendering.
- Frontend end-to-end tests for browser-level inquiry flow against the backend API in local integration mode.

Test implementation traces to frozen requirements/spec outcomes without duplicating acceptance-criteria text inside this plan.

## Risks and Assumptions
### Risks
- Customer-validation integration behavior may vary by backing adapter and can affect outcome mapping consistency.
- Legacy date-semantics preservation can be misapplied if transformation responsibilities are duplicated.
- Retrieval adapters may accidentally introduce ordering or record-count behavior drift.
- Security policy changes may impact endpoint accessibility if not isolated from business logic.
- Frontend and backend response-handling drift can create inconsistent user presentation if shared contract assumptions diverge.

### Assumptions
- The frozen Spec and OpenAPI represent the external contract baseline for this feature.
- Existing repository/module structure remains the implementation host for this feature.
- Inquiry remains read-only with no persistence-side modifications.

### Mitigations
- Centralize status/date/identifier mapping in dedicated mapper components.
- Enforce contract-focused controller tests and mapper unit tests.
- Keep adapter interfaces narrow and validate behavior through focused repository tests.
- Isolate security decisions in configuration/security layers.
- Enforce frontend contract-alignment tests and integration checks against backend responses.

## Implementation Phases
### Phase 1: Foundation and Boundaries
Establish backend and frontend module boundaries, interfaces, and configuration wiring aligned with the approved architecture.

### Phase 2: Core Inquiry Orchestration
Implement backend customer-validation-first orchestration and frontend inquiry request/response handling flow.

### Phase 3: Contract-Fidelity Mapping and Error Translation
Complete backend response/error mapping and frontend result/error presentation aligned to the frozen contract.

### Phase 4: Observability and Security Integration
Apply backend structured logging/correlation/security wiring and frontend runtime integration configuration for stable API communication.

### Phase 5: Verification and Stabilization
Complete multi-level backend/frontend automated testing, resolve integration drift against frozen artifacts, and harden delivery readiness.
