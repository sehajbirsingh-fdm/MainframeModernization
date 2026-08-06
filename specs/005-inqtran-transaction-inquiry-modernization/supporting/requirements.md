# Requirements — INQTRANL Transaction List Inquiry

## Executive Summary
Provide a read-only account transaction inquiry capability that returns transaction history for a requested account identity while preserving validated legacy filtering, defaults, ordering, pagination, counts, field mappings, empty-result behavior, and technical-failure semantics.

## Business Context
This feature provides a modernized account transaction-list inquiry capability that lets users retrieve read-only transaction history for a specific account identity with optional date constraints and paging controls. The modernization objective is to preserve proven legacy business outcomes while delivering them through the current application's established delivery model.

## Scope
- One transaction-list query operation for sort code and account number.
- Optional date filtering, limit, and offset.
- Total count, returned count, ordered transactions, and composite IDs.
- Existing-application backend/frontend integration and tests.

## Out of Scope
- Single transaction detail retrieval (`INQTRAND`).
- Account existence validation.
- Transaction creation/update/deletion.
- Statements, balances, categorization, search text, exports, or live mainframe connectivity.
- Reproduction of `ABNDFILE`; use existing modern error-handling/logging conventions.

## Functional Requirements

- **FR-001 [BR-001]:** The system shall retrieve transactions using both sort code and account number as exact identity filters.
- **FR-002 [BR-002, BR-003, BR-004]:** The system shall support inclusive from/to date filtering for supplied boundaries; when a boundary is omitted, legacy processing shall preserve sentinel normalization behavior, and final target omitted-boundary semantics shall require an approved modernization decision after runtime/SME verification.
- **FR-003 [BR-005, BR-006, BR-007]:** The system shall default limit to 50 when omitted or zero, clamp values above 100 to 100, and never return more than 100 rows in one successful inquiry.
- **FR-004 [BR-008]:** The system shall apply offset after filtering and ordering and before selecting returned rows.
- **FR-005 [BR-009]:** The system shall provide consistent and deterministic ordering using transaction date descending followed by transaction time descending, without introducing additional ordering semantics beyond validated legacy behavior.
- **FR-006 [BR-010, BR-011, BR-017]:** The response shall include total count before pagination and returned count for the current page; returned count shall equal the number of returned transaction rows, and both counts shall align with the same filtered population used for row retrieval.
- **FR-007 [BR-012, BR-013]:** The system shall return a successful empty result when no transaction matches, with zero returned rows and an empty transaction collection in the success response.
- **FR-008 [BR-015, BR-019]:** Each transaction shall include the deterministic composite transaction ID defined by legacy-evidenced component ordering.
- **FR-009 [BR-016]:** Each transaction shall expose only legacy-evidenced output fields and transformations.
- **FR-010 [BR-001-BR-019]:** The operation shall be read-only and shall not mutate transaction data.
- **FR-011 [BR-018]:** A retrieval failure shall return a technical failure without partial page data.
- **FR-012 [BR-001-BR-019]:** The existing React application shall provide a minimal page for entering supported query inputs and rendering metadata and rows consistent with preserved backend response behavior.
- **FR-013 [BR-001-BR-019]:** All evidenced legacy behavior for this capability shall be preserved unless an approved modernization enhancement explicitly supersedes that behavior.
- **FR-014 [BR-001-BR-019]:** The capability shall integrate into the existing application without altering unrelated functionality or violating established architectural conventions.

## Non-Functional Requirements

- **NFR-001:** Use Java 21 and the repository's Spring Boot 3 conventions.
- **NFR-002:** Preserve identifiers as strings to retain leading zeros.
- **NFR-003:** Enforce separation of concerns across controller, service, repository, mapper, and DTO layers.
- **NFR-004:** Keep business logic ownership in the service layer; controllers and repositories shall not implement core business-rule orchestration.
- **NFR-005:** Keep persistence behind a repository abstraction suitable for future DB2/mainframe adapter substitution without changing business orchestration behavior.
- **NFR-006:** Validation ownership shall be explicit: boundary-level structural validation at transport boundaries and business normalization/behavior enforcement at the service layer.
- **NFR-007:** Do not log full transaction descriptions, references, or account identifiers at normal levels unless repository policy explicitly allows it.
- **NFR-008:** Do not return stack traces or database details to clients.
- **NFR-009:** Use existing application observability/correlation conventions where available.
- **NFR-010:** Maintain feature-level traceability such that every requirement is intended to map through Specification, Plan, Tasks, Test Specification, and Traceability Matrix artifacts.
- **NFR-011:** Add unit, repository integration, controller, OpenAPI conformance, frontend unit, and E2E tests.

## Security Requirements

- **SR-001:** The legacy evidence does not define authorization behavior for this capability; therefore, no new feature-specific authorization rule shall be invented in this refinement.
- **SR-002:** Validate syntactic input at the HTTP boundary without inventing domain rejection rules.
- **SR-003:** Prevent SQL injection through parameterized JDBC queries.
- **SR-004:** Avoid sensitive-data leakage in errors and logs.

## Operational Requirements

- **OR-001:** Extend the current backend on its existing runtime and port.
- **OR-002:** Extend the current frontend route/navigation and reuse the Vite proxy.
- **OR-003:** Use existing H2 and SQL initialization conventions.
- **OR-004:** Do not connect to production DB2/CICS in the POC.
- **OR-005:** Reconcile the feature contract with the runtime OpenAPI publication path before merge.
- **OR-006:** Remain compatible with existing deployment and environment-profile conventions used by the current application.
- **OR-007:** Follow existing configuration management conventions for feature flags, datasource binding, and externalized settings.
- **OR-008:** Follow established logging, monitoring, metrics, and correlation conventions defined by Architecture.
- **OR-009:** Preserve existing operational behavior for unrelated endpoints and services.

## Modernization Enhancements Requiring Approval

The following items are approved modernization decisions identified during Research and are intentionally separated from mandatory legacy-preservation requirements.

- Use nullable date query parameters rather than invalid legacy DB2 sentinel dates.
- Treating omitted date parameters as unconstrained effective bounds (instead of legacy sentinel-normalization path) requires explicit modernization approval and runtime/SME verification.
- Use a standard JSON technical-error envelope and HTTP 500.
- Validate date syntax/calendar order at the API boundary.
- Use database-native pagination if proven behaviorally equivalent.

## Assumptions

- The current application can add a `PROCTRAN`-equivalent H2 table or approved existing transaction table.
- Existing repository patterns are the preferred implementation baseline.
- Existing repository conventions remain the preferred architectural baseline unless superseded by approved architectural decisions.

## Risks

### Critical
- Missing DB2 DDL/nullability constraints.
- Security policy is not established by the supplied evidence.

### Major
- Ambiguous runtime OpenAPI authority.
- Numeric COMMAREA reference conflicts with DB2 `CHAR(12)`.
- API date representation compatibility remains unresolved across legacy declarations and host conversion evidence.
- Runtime behavior of converted sentinel date values in always-present SQL date predicates remains unresolved.

### Minor
- No evidenced account-not-found distinction.
- Unstable ordering for rows tied on date and time.
