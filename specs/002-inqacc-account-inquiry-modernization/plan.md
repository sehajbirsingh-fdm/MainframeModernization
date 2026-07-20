# Implementation Plan: INQACC Account Inquiry Modernization

**Branch**: `002-inqacc-account-inquiry-modernization`  
**Date**: 2026-07-19  
**Spec Authority**: `spec.md`  
**Contract Authority**: `contracts/openapi.yaml`

## Summary

This plan defines how the approved INQACC account inquiry feature will be built as a specification-driven modernization. The feature modernizes legacy read-only account inquiry behavior behind a REST API and a lightweight React UI used for exercising and demonstrating the flow in POC mode.

The implementation preserves legacy-observable behavior defined in the specification, including:
- canonical endpoint `GET /v1/accounts/{sortcode}/{accountNumber}`
- composite-key lookup using `ACCOUNT_SORTCODE` and `ACCOUNT_NUMBER`
- reserved account-number behavior when `accountNumber = 99999999`
- standardized error semantics and safe observability

This plan does not redefine business rules or API behavior. Behavioral authority remains in `spec.md`, and contract authority remains in `contracts/openapi.yaml`.

Database-readiness posture: DB2-ready JDBC architecture with configuration-driven database activation, not verified live DB2 integration.

## Technical Context

- **Modernization target**: Legacy INQACC inquiry capability exposed through HTTP API and demo-oriented web UI.
- **Feature boundary**: Read-only inquiry only. No create, update, delete, or batch workflows.
- **Execution mode**: Default POC runtime is mock mode. A relational database mode is implemented but inactive unless explicitly enabled by configuration.
- **Contract model**: Contract-first implementation under the hierarchy: Specification -> OpenAPI -> Implementation.
- **Security baseline**: Bearer-token authentication and role-based authorization at API boundary as defined by specification and contract.
- **Observability baseline**: Correlation-ID-aware structured logging with safe diagnostic content only.
- **Performance posture**: No new latency or availability commitments are introduced in this plan beyond approved scope.
- **Database-readiness boundary**: No live DB2 connection is required for POC acceptance. Activation against a specific DB2 environment requires compatible driver availability, valid credentials, confirmed schema/table mappings, and environment/network/security access.

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.x, Spring Web, Spring Validation, Spring Security, Spring JDBC, Jackson.
- **Frontend (POC demo UI)**: Existing `src/frontend-react` React + TypeScript + Vite module, using repository-configured versions and conventions.
- **Testing**: JUnit 5, Spring test tooling, and frontend component/flow testing tooling aligned to the selected React setup; H2 may be used only as a test database for JDBC-mode tests.
- **Data modes**:
  - **Mock mode (default)**: controlled mock data loaded in memory; no `DataSource` creation.
  - **Database mode (inactive until configured)**: JDBC adapter with Hikari-backed `DataSource` created only when `app.data.mode=db`.

Technology choices are retained to stay consistent with project direction while keeping POC acceptance independent from any live database.

Repository reconciliation note: earlier planning drafts assumed JavaScript-oriented wording and fixed React versions. Repository inspection confirmed the existing `src/frontend-react` application already used React, Vite, and TypeScript prior to INQACC implementation. INQACC therefore reuses that module in-place, avoids duplicate frontend creation, and does not introduce JavaScript/TypeScript migration solely for this feature.

Runtime configuration model (equivalent behavior):

```properties
app.data.mode=mock

app.db.url=${APP_DB_URL:}
app.db.username=${APP_DB_USERNAME:}
app.db.password=${APP_DB_PASSWORD:}
app.db.schema=${APP_DB_SCHEMA:}
app.db.table-name=${APP_DB_TABLE_NAME:ACCOUNT}

app.db.pool.max-size=10
app.db.pool.min-idle=2
```

Property names may follow existing repository conventions if behavior remains equivalent.
Database credentials and sensitive settings remain external to source control.

## Architecture Overview

The solution uses a layered architecture with clear boundaries:

1. **API Boundary**
Receives `GET /v1/accounts/{sortcode}/{accountNumber}` requests, enforces security boundary integration, and delegates to the application layer.

2. **Application Service Boundary**
Orchestrates one shared inquiry flow: validation coordination, reserved-number branch decision, repository lookup invocation, and response/error mapping coordination.

3. **Repository Abstraction Boundary**
Defines inquiry data access through `AccountRepository`, with two separate implementations selected by configuration:
- `MockAccountRepository` for default `app.data.mode=mock`
- `JdbcAccountRepository` for `app.data.mode=db`

`AccountInquiryService` depends only on `AccountRepository` and remains unchanged across data modes.
Repository adapters are data-access only and do not redefine business rules, validation, error handling, or API behavior.

4. **Data Mode Configuration Boundary**
Controls conditional bean activation for repository and database configuration.
- In mock mode, no `DataSource` is created and no database connection is attempted.
- In db mode, database configuration is activated, a Hikari-backed `DataSource` is created, and startup validation enforces required DB properties.

5. **Mapping and Conversion Boundary**
Applies authoritative field mapping and transformations (trim fixed-width CHAR content, numeric conversion, date conversion) before response serialization.

6. **Error Translation Boundary**
Converts validation, not-found, authorization/authentication, and unexpected technical failures into the canonical API error contract.

7. **Observability Boundary**
Ensures structured, correlation-aware logs that support traceability without exposing sensitive account or credential data.

## Project Structure

This feature plan aligns with the existing repository structure and does not introduce a separate application topology.

- **Feature documents**: `specs/002-inqacc-account-inquiry-modernization/`
- **API contract**: `specs/002-inqacc-account-inquiry-modernization/contracts/openapi.yaml`
- **Supporting analysis and mapping**:
  - `supporting/program-analysis.md`
  - `supporting/mapping-matrix.md`
  - `supporting/test-spec.md`
  - `supporting/traceability-matrix.md`
- **Backend and frontend source roots** remain under the established project source layout.

The plan references mapping and traceability artifacts rather than duplicating their content.

## Component Responsibilities

At planning level, major component responsibilities are:

- **HTTP/API adapter**: Bind canonical route, parse path inputs, apply boundary-level concerns, return success/error responses.
- **Inquiry application service**: Execute shared inquiry orchestration, coordinate validation, make the reserved-number branch decision once, invoke repository operations, and preserve shared business behavior across data modes.
- **Validation boundary**: Enforce sortcode and account-number format constraints prior to business lookup processing.
- **Repository interface**: Define lookup operations for standard and reserved-number flows.
- **Mock repository adapter**: Load controlled account data and serve in-memory lookups in default mock mode.
- **JDBC repository adapter**: Execute parameterized SQL lookups and map rows to the same canonical account domain model in db mode.
- **Database configuration boundary**: Activate JDBC/Hikari configuration only in db mode, read externalized DB properties, and fail startup clearly when required DB-mode configuration is missing or invalid.
- **Mapper/converter**: Provide one shared mapping/conversion boundary for canonical API response output, including authoritative field mapping and required data-format conversions, independent of active repository adapter.
- **Exception and error mapping mechanism**: Standardize all failure outcomes to canonical error envelope and HTTP semantics.
- **Logging/correlation mechanism**: Generate or propagate correlation IDs and produce safe structured logs.

## Data Flow

High-level request and response flow:

1. Request enters API boundary at `GET /v1/accounts/{sortcode}/{accountNumber}`.
2. Authentication and authorization checks run at API/security boundary.
3. Path parameter validation enforces six-digit sortcode and eight-digit account-number rules.
4. Application service determines lookup path:
   - standard composite-key lookup, or
   - reserved-number branch for `accountNumber = 99999999` selecting highest account number for the sortcode.
5. Repository abstraction executes lookup through the active repository implementation:
  - mock mode: in-memory lookup from controlled mock data
  - db mode: JDBC lookup through parameterized SQL
6. Retrieved legacy-shaped data is mapped and converted using approved mapping rules.
7. Success response returns canonical account payload fields.
8. Failure paths are translated to canonical error payload and status semantics.
9. Correlation ID is propagated in response and logs for traceability.

Runtime mode behavior:
- `app.data.mode=mock` (default): activates `MockAccountRepository`, loads controlled mock records into memory, and does not create a `DataSource`.
- `app.data.mode=db`: activates `JdbcAccountRepository` and database configuration, creates Hikari-backed `DataSource`, and requires valid externally provided DB connection settings.

Architecture intent is to minimize code changes for future DB2 activation while avoiding claims of verified connectivity to unknown DB2 environments.

## Implementation Strategy

Implementation is organized as a contract-first, layered modernization flow:

1. **Contract alignment first**
Confirm technical design alignment to frozen specification and OpenAPI before coding behavior.

2. **Layered backend construction**
Implement API boundary, service orchestration, repository abstraction, and mapping/conversion boundaries in alignment with authoritative artifacts.

3. **Dual repository realization with default mock runtime**
Implement both repository adapters in this feature while keeping mock mode as default POC runtime.
- Mock mode remains the acceptance path and requires no live database.
- Database mode remains inactive until explicitly configured.

4. **Conditional data-mode configuration**
Implement configuration-based repository selection and DB-mode startup validation.
- Support `app.data.mode` with default `mock` behavior when unset (unless existing project conventions explicitly require property declaration).
- Support environment-backed DB settings, safe table-name configuration, and pool settings for db mode.

5. **Frontend integration for feature exercise**
Connect the React-based inquiry experience to the canonical API contract for validation and demonstration flows.

6. **Conformance verification**
Validate implemented behavior against specification, OpenAPI, mapping matrix, and test-spec artifacts.

The strategy remains technical and high-level; detailed execution tasks are intentionally deferred to `tasks.md` after plan approval.

## Validation Strategy

Validation is enforced at the API/application boundary prior to lookup execution.

- `sortcode` must be numeric and exactly 6 digits.
- `accountNumber` must be numeric and exactly 8 digits.
- Invalid path inputs are treated as validation errors and mapped to canonical bad-request outcomes.

Validation rules are implemented from the specification and OpenAPI contract; this plan does not redefine those rules.

Configuration validation is also enforced for db mode startup:
- When `app.data.mode=db`, required DB configuration (URL, username, password, and applicable table configuration) must be present and valid.
- Invalid or missing required db-mode configuration causes clear startup failure.

## Error Handling Strategy

Error handling follows canonical API semantics and envelope structure defined by specification and OpenAPI.

- **Validation failures** map to bad-request error outcomes.
- **Authentication/authorization failures** map to unauthorized/forbidden outcomes.
- **No-match results** map to not-found outcomes.
- **Unexpected technical failures** map to internal-error outcomes.
- **Transient repository/service unavailability** maps to service-unavailable outcomes.

Mode-specific handling:
- Mock mode avoids database connectivity paths entirely.
- DB-mode configuration failures are treated as startup-time failures, not runtime fallback to silent mock behavior.

All error responses preserve the canonical nested error payload with `code`, `message`, `correlationId`, and `timestamp` fields, as defined by authoritative artifacts.

## Logging and Observability Strategy

Observability implementation is structured, correlation-aware, and safe by default.

- Correlation ID is generated or propagated at request entry and carried across logs and response metadata.
- Logs capture operational metadata needed for diagnostics and traceability.
- Sensitive data is excluded from logs, including bearer tokens, account numbers, customer numbers, balances, and full account payloads.
- POC observability remains lightweight and avoids introducing unnecessary production telemetry platforms.

## Security Strategy

Security planning is limited to approved scope:

- Enforce bearer-token authentication at API boundary.
- Enforce role/permission-based authorization for inquiry access.
- Preserve required HTTP distinction between authentication and authorization failures.
- Avoid introducing new security domains or unrelated authentication workflows beyond the frozen feature scope.
- Keep database credentials external to source control and supplied through environment-backed configuration.

## Testing Strategy

Testing is organized by technical boundaries and conformance goals:

- **Unit testing** for service orchestration, validation behavior, mapping/conversion, and error translation logic.
- **API/controller testing** for contract-conformant request/response and status semantics.
- **Repository adapter testing** for composite-key and reserved-number lookup behavior under mock data.
- **Data-mode testing** for conditional repository activation and mode-specific lifecycle behavior.
- **JDBC adapter testing** for parameterized-query execution and row mapping using a test-only relational database.
- **Frontend behavior testing** for inquiry flow exercise, validation feedback, and error display behavior.
- **Integration/conformance testing** across API, service, mapping, and mock repository boundaries.

Testing verifies that service and API behavior remain consistent across mock and db modes.

Test design references `supporting/test-spec.md` and `supporting/traceability-matrix.md` rather than duplicating those artifacts.

## Technical Risks and Assumptions

### Key Technical Risks

- **Legacy conversion fidelity risk**: Incorrect trimming, numeric conversion, or date conversion can break parity.
- **Reserved-branch lookup risk**: Reserved account-number flow may regress without explicit verification.
- **Mock-data representativeness risk**: Incomplete mock records can hide mapping or formatting defects.
- **Data-mode parity risk**: Behavioral drift between mock and jdbc adapters can cause inconsistent outcomes.
- **DB configuration risk**: Incomplete db-mode configuration can cause startup failure when db mode is enabled.
- **Environment compatibility risk**: Actual DB2 activation still depends on driver, schema alignment, credentials, network access, and security/TLS settings in the target environment.
- **Contract drift risk**: Implementation behavior can diverge if contract-first discipline is not maintained.

### Assumptions

- `spec.md` remains the frozen behavior authority for this feature.
- `contracts/openapi.yaml` remains the machine-readable contract authority.
- Supporting mapping and traceability artifacts are maintained and available during implementation.
- POC execution and acceptance run in default mock mode with no live DB2/CICS dependency.
- Database mode is implemented in this feature but remains inactive until `app.data.mode=db` and required DB settings are provided.
- No claim is made that live DB2 connectivity has been tested in this feature.

## Broad Implementation Phases

The technical build will progress through broad phases:

1. **Design and contract alignment**
Confirm architecture boundaries and contract-first conformance approach.

2. **Core backend boundary implementation**
Establish API, service orchestration, repository abstraction, dual adapters (mock and jdbc), conditional data-mode configuration, and mapping/error boundaries.

3. **Frontend integration and end-to-end behavior alignment**
Connect UI inquiry flow to canonical API behavior for exercise and demonstration.

4. **Conformance and readiness verification**
Verify alignment with specification, OpenAPI, mapping, and test artifacts, including parity across mock and db modes, prior to final implementation completion and POC acceptance.

This plan is the approved technical authority for implementation task execution and conformance review.