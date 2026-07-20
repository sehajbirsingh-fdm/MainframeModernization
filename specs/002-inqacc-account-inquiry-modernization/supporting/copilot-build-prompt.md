# INQACC Copilot Build Prompt

## 1. Purpose
This document controls implementation execution for INQACC modernization inside the existing repository.

Implement the approved tasks while strictly preserving frozen behavior, contract, architecture, and scope.

This prompt does not define a competing architecture. It operationalizes the approved artifacts.

## 2. Authority hierarchy
Use this precedence order for all implementation decisions:

1. `spec.md` (authoritative business and observable behavior)
2. `contracts/openapi.yaml` (authoritative machine-readable API contract)
3. `plan.md` (authoritative technical architecture and implementation design)
4. `tasks.md` (authoritative implementation work and acceptance criteria)
5. `supporting/mapping-matrix.md` (field-mapping support)
6. `supporting/test-spec.md` (test-design support)
7. `supporting/traceability-matrix.md` (traceability support)
8. `supporting/program-analysis.md` and legacy copybooks/COBOL (context only)

Legacy evidence must not override frozen spec, contract, plan, or tasks.

## 3. Frozen artifact protection
Do not modify:

- `spec.md`
- `plan.md`
- `tasks.md`
- `contracts/openapi.yaml`

Do not silently reinterpret conflicting requirements.
Do not add endpoints, response fields, business rules, validation semantics, security semantics, or logging behavior outside approved artifacts.
Do not weaken approved validation, security, error handling, or safe-logging requirements.

If a genuine conflict is found:

1. identify exact files and sections;
2. explain the conflict;
3. stop the affected task;
4. do not invent a resolution;
5. continue only unaffected tasks when safe.

## 4. Existing repository and scope protection
Before creating files or changing build configuration:

1. inspect current backend/frontend source roots and module layout;
2. reuse established structure and conventions;
3. implement in-place inside existing modules;
4. do not scaffold a second backend or frontend;
5. do not create duplicate app modules;
6. create only files required by frozen tasks;
7. prefer incremental edits to existing build files.

If expected directories/modules are missing, report discrepancy first. Do not invent a new topology.

## 5. Approved stack
Backend:

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Validation
- Spring Security
- Spring JDBC
- HikariCP
- Jackson
- JUnit 5
- Spring Boot test tooling

Frontend:

- existing `src/frontend-react` React + TypeScript + Vite stack
- repository-configured frontend dependency versions
- existing frontend module conventions and patterns
- approved frontend testing tooling required by frozen tasks

Test-only database:

- H2 allowed only for automated JDBC repository tests
- H2 must not be used as mock-mode runtime persistence
- H2 results must not be represented as proof of live DB2 compatibility

Do not introduce:

- Spring Data JPA
- Hibernate
- JPA entities
- OpenFeign
- live CICS integration
- live DB2 connectivity as POC acceptance requirement

Frontend reconciliation constraints:

- do not create a duplicate frontend application
- do not migrate the existing frontend between JavaScript and TypeScript solely for INQACC
- do not upgrade or downgrade React solely for INQACC; use the module's configured version

## 6. Approved architecture
Preserve this architecture exactly:

AccountInquiryController
    -> AccountInquiryService
        -> AccountRepository
            -> MockAccountRepository
            -> JdbcAccountRepository
                -> canonical account domain model
                    -> shared mapper
                        -> OpenAPI response model

There must be:

- one controller
- one application service
- one repository interface
- one mock repository adapter
- one JDBC repository adapter
- one canonical domain model
- one shared response mapper
- one validation flow
- one centralized error-handling flow
- one correlation and safe-logging flow

Repository implementations are data-access adapters only.
They must not independently implement request validation, reserved-number business decisions, API response mapping, HTTP error behavior, authorization, or duplicated business rules.

## 7. Runtime modes
Mock mode (`app.data.mode=mock`):

- default POC runtime
- activates `MockAccountRepository`
- uses controlled in-memory or approved local mock data
- requires no DB credentials
- creates no `DataSource`
- creates no Hikari pool
- attempts no database connection
- makes no DB2 or CICS call

Database mode (`app.data.mode=db`):

- activates `JdbcAccountRepository`
- activates database configuration boundary
- creates Hikari-backed `DataSource`
- uses Spring JDBC and parameterized SQL
- reads URL/username/password and applicable schema/table settings from external configuration
- fails clearly at startup when required config is missing or invalid
- returns the same canonical domain model used by mock mode

Use this wording:

DB2-ready JDBC architecture with configuration-driven database activation, not verified live DB2 integration.

Do not claim credentials alone guarantee connectivity to unknown DB2 environments.
Real DB2 activation may additionally require compatible DB2 JDBC driver, confirmed schema/table/column mappings, network access, permissions, and TLS/security settings.

## 8. Required business behavior
Preserve reserved account-number behavior in one shared service flow:

For accountNumber not equal to 99999999:
- perform standard lookup using sortcode + accountNumber

For accountNumber equal to 99999999:
- AccountInquiryService makes branch decision once
- call repository technical operation to find highest account number by sortcode

Repository implementations may use different technical query mechanisms, but must not redefine reserved-number business meaning.

## 9. Implementation execution procedure
Follow task dependencies from `tasks.md`. Task IDs are identifiers, not sequence guarantees.
Do not assume numeric order, including T052-T057.
Implement one task at a time, or one tightly coupled dependency group when tasks are inseparable.
Do not implement future tasks early simply because they appear related.

Before implementing each task:

1. read task scope and acceptance criteria;
2. read dependency tasks and verify all prerequisite dependencies are complete;
3. if any required dependency is incomplete, stop that task and report the blocker (do not implement around missing prerequisites);
4. read relevant sections in spec, contract, and plan;
5. inspect existing code and module layout;
6. implement only approved scope for that task;
7. run focused tests for changed behavior;
8. report changed files and validation evidence;
9. mark task complete only when all acceptance criteria and required tests are satisfied, and repository workflow permits status updates.

Do not mark tasks complete solely because files were created.

## 10. Testing and validation procedure
Derive tests from `tasks.md` and `supporting/test-spec.md`.
Include:

- service tests for standard and reserved flows
- mock repository tests
- JDBC repository tests using H2 only
- row-mapping tests
- parameterized-query tests
- conditional bean-selection tests
- proof that mock mode creates no `DataSource`
- startup-failure tests for incomplete db-mode config
- service parity tests across repository implementations
- controller and API error-contract tests
- correlation and safe-logging tests
- frontend validation/state/stale-response/rendering tests
- OpenAPI conformance tests

Do not invent coverage percentages unless explicitly required by authoritative artifacts.
Do not describe H2 test results as live DB2 verification.

## 11. Security, logging and credential rules
Security:

- preserve approved bearer-auth and authorization behavior from frozen artifacts
- do not add alternate security models outside approved scope

Logging:

- never log raw account numbers, customer numbers, balances, bearer tokens, credentials, full requests/responses, or full account payloads
- do not log raw lookup keys when treated as sensitive by approved plan
- log safe operational metadata only (for example: correlation ID, operation name, outcome, HTTP status, repository mode, sanitized failure category, elapsed time)

Credentials:

- keep all DB credentials and sensitive settings external
- never commit secrets to source control

## 12. Conflict and blocker handling
If blocked by artifact conflict, missing dependency, or repository mismatch:

1. identify task ID and exact blocker;
2. cite conflicting artifact sections;
3. stop only blocked work;
4. continue safe independent work when possible;
5. report required human decision explicitly.

Never resolve conflicts by changing frozen artifacts.

## 13. Completion report format
After each implementation batch, report:

- tasks attempted
- tasks completed
- tasks blocked
- files created
- files modified
- tests executed
- test results
- acceptance criteria verified
- unresolved conflicts
- confirmation frozen artifacts were unchanged
- confirmation no duplicate application/module was created
- confirmation no out-of-scope technology or behavior was introduced
