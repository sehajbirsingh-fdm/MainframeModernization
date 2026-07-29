# Requirements — INQTRANL Transaction List Inquiry

## Executive Summary
Modernize the read-only account transaction list inquiry in the existing application while preserving its filters, defaults, ordering, pagination, counts, field mappings, empty-result behavior, and technical-failure semantics.

## Business Context
The current CICS program queries DB2 `PROCTRAN` through a COMMAREA. The repository already provides Spring Boot, JDBC/H2, React/Vite, testing, and feature-layer conventions.

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
- **FR-002 [BR-002, BR-003, BR-004]:** The system shall support inclusive optional from/to date filters; omitted boundaries shall not constrain that side of the date range.
- **FR-003 [BR-005, BR-006]:** The system shall default limit to 50 when omitted or zero and clamp values above 100 to 100.
- **FR-004 [BR-007]:** The system shall apply offset after filtering and ordering and before selecting returned rows.
- **FR-005 [BR-008]:** The system shall order results by transaction date descending and transaction time descending.
- **FR-006 [BR-009]:** The response shall include total count before pagination and returned count for the current page.
- **FR-007 [BR-010]:** The system shall return successful empty results when no transaction matches.
- **FR-008 [BR-011]:** Each transaction shall include the composite transaction ID defined in the specification.
- **FR-009 [BR-012, BR-013]:** Each transaction shall expose only the legacy-evidenced fields and transformations.
- **FR-010 [BR-014]:** The operation shall be read-only.
- **FR-011 [BR-015]:** A retrieval failure shall return a technical failure without partial page data.
- **FR-012 [BR-001–BR-015]:** The existing React application shall provide a minimal page for entering supported query inputs and rendering metadata and rows.

## Non-Functional Requirements

- **NFR-001:** Use Java 21 and the repository's Spring Boot 3 conventions.
- **NFR-002:** Preserve identifiers as strings to retain leading zeros.
- **NFR-003:** Keep controller, service, repository, mapper, and DTO responsibilities separated.
- **NFR-004:** Keep persistence behind a repository interface suitable for a future DB2/mainframe adapter.
- **NFR-005:** Maintain feature-level source-to-rule-to-requirement-to-test traceability.
- **NFR-006:** Add unit, repository integration, controller, OpenAPI conformance, frontend unit, and E2E tests.
- **NFR-007:** Do not log full transaction descriptions, references, or account identifiers at normal levels unless repository policy explicitly allows it.
- **NFR-008:** Do not return stack traces or database details to clients.
- **NFR-009:** Use existing application observability/correlation conventions where available.

## Security Requirements

- **SR-001:** Apply the repository's approved route-security policy after its owner confirms whether transaction inquiry is public, authenticated, or account-authorized.
- **SR-002:** Validate syntactic input at the HTTP boundary without inventing domain rejection rules.
- **SR-003:** Prevent SQL injection through parameterized JDBC queries.
- **SR-004:** Avoid sensitive-data leakage in errors and logs.

## Operational Requirements

- **OR-001:** Extend the current backend on its existing runtime and port.
- **OR-002:** Extend the current frontend route/navigation and reuse the Vite proxy.
- **OR-003:** Use existing H2 and SQL initialization conventions.
- **OR-004:** Do not connect to production DB2/CICS in the POC.
- **OR-005:** Reconcile the feature contract with the runtime OpenAPI publication path before merge.

## Modernization Enhancements Requiring Approval

- Use nullable date query parameters rather than invalid legacy DB2 sentinel dates.
- Use a standard JSON technical-error envelope and HTTP 500.
- Validate date syntax/calendar order at the API boundary.
- Use database-native pagination if proven behaviorally equivalent.

## Assumptions

- The current application can add a `PROCTRAN`-equivalent H2 table or approved existing transaction table.
- Existing repository patterns are the preferred implementation baseline.
- API date representation remains `YYYYMMDD` to preserve the legacy contract.

## Risks

- Missing DB2 DDL/nullability constraints.
- Ambiguous runtime OpenAPI authority.
- No evidenced account-not-found distinction.
- Unstable ordering for rows tied on date and time.
- Numeric COMMAREA reference conflicts with DB2 `CHAR(12)`.
- Security policy is not established by the supplied evidence.
