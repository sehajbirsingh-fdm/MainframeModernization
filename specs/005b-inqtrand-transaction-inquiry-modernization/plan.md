# Plan — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Describe how the frozen Specification will be implemented in the existing repository without redefining behavior.

## Repository Inspection Summary
Current repository evidence establishes:
- backend: Java 21 / Spring Boot 3.5.3 / Maven;
- package slice: `backend/api/src/main/java/com/bankofz/mainframemodernization/inqtran/`;
- current controller/service/repository/mapper/domain/error conventions;
- H2 `PROCTRAN` initialized by resource SQL;
- frontend transaction feature/client/domain/routes;
- existing account GET security and correlation filters;
- backend, frontend, E2E, and OpenAPI-conformance test patterns.

Before editing, implementation must re-open the exact target files because discovery was read-only/static and runtime was not verified.

## Architecture Overview
Extend the existing transaction vertical slice:
frontend → existing transaction API client → secured detail GET → service → transaction repository → H2 PROCTRAN.

## Technology Stack
No stack additions:
- Java 21;
- Spring Boot 3.5.3;
- JDBC/DataSource;
- H2 DB2 mode;
- Maven;
- React/TypeScript/Vite;
- React Router / React Query where consistent;
- Vitest/Testing Library;
- Playwright;
- existing Spring Security.

## Existing Project Structure
Relevant evidence-backed areas:
- `backend/api/src/main/java/com/bankofz/mainframemodernization/inqtran/`
- `backend/api/src/main/resources/schema.sql`
- `backend/api/src/main/resources/data.sql`
- `backend/api/src/main/resources/openapi.yaml`
- `backend/api/src/test/java/com/bankofz/mainframemodernization/inqtran/`
- `frontend/app/src/features/transactionInquiry/`
- `frontend/app/src/api/transactionInquiryClient.ts`
- `frontend/app/src/domain/transactionTypes.ts`
- `frontend/app/src/App.tsx`
- `frontend/app/e2e/`

## Package and Placement Strategy
Prefer extending existing classes/interfaces when responsibility remains coherent:
- remain within `com.bankofz.mainframemodernization.inqtran` and extend the existing `TransactionRepository` interface with a zero-or-one exact detail lookup;
- extend the existing `JdbcTransactionRepository` with the matching prepared query, reusing existing H2/`PROCTRAN`/JDBC foundations;
- service gains detail method;
- controller gains detail GET;
- mapper/domain types are reused or minimally extended;
- frontend client/types/feature are extended.

Do not introduce a parallel top-level `inqtrand` repository hierarchy unless later architectural evidence demonstrates necessity.

Add dedicated detail-specific types/classes only when reuse would force list-only behavior (notably null defaults) or create unclear contracts.

## Component Design

### Persistence
Query all five identity columns with equality. Select only fields required for detail/mapping, following exact legacy mapping. No list count/order/pagination clauses.

### Service
- receive validated key;
- call exact lookup;
- row present → map found result;
- absent → found false;
- repository technical exception → feature technical exception;
- do not translate absence into exception.

### Controller/API
Add approved GET path under current account API hierarchy. Bean/path validation performs digit-width checks. Reuse existing error handling.

### Mapper/domain
Generate exact composite ID. Preserve exact amount. Ensure no list-specific null amount default enters the detail mapping.

## Data Flow
1. path inputs retained as strings;
2. validate exact digit shape and width (`sortCode` 6, `accountNumber` 8, `date` 8, `time` 6, `reference` 12) while preserving leading zeroes and avoiding numeric coercion;
3. convert date only as necessary for persistence representation, without new calendar business validation;
4. execute exact query;
5. map zero-or-one result;
6. serialize approved envelope.

## Persistence Strategy
- inspect `schema.sql` and current `JdbcTransactionRepository` first;
- reuse safe schema/table identifier handling;
- use PreparedStatement;
- return an optional/zero-or-one abstraction;
- do not treat this abstraction as proof of production DB2 physical uniqueness of the five-part key;
- if implementation/repository inspection reveals duplicate physical matches are possible, treat as a data/integration issue requiring explicit resolution;
- do not invent arbitrary ordering, first-row selection, or duplicate-resolution behavior;
- add deterministic seeds only when necessary for approved tests;
- never add a hidden record-validity/type/delete filter.

## API Implementation Strategy
Feature OpenAPI (`contracts/openapi.yaml`) is the approved 005B contract. Implement controller to it, then update `backend/api/src/main/resources/openapi.yaml` so runtime contract matches. Do not copy the broad historical endpoint by default.

## Frontend Integration Strategy
- extend `transactionInquiryClient.ts` with detail call;
- extend `transactionTypes.ts` minimally;
- add detail view/state under transaction feature;
- add route consistent with `/transactions/:sortCode/:accountNumber/:date/:time/:reference`;
- require list-to-detail integration using existing transaction list rows and the five identity components: list row -> detail navigation -> existing transaction API client -> approved INQTRAND detail endpoint -> found-detail or successful-absence presentation;
- do not alter list filtering/pagination behavior.

## Validation Strategy
External identity validation is structural only: `sortCode` exactly 6 digits, `accountNumber` exactly 8 digits, `date` exactly 8 digits, `time` exactly 6 digits, `reference` exactly 12 digits, with leading zeroes preserved.
Do not add Gregorian/calendar semantic validation, HHMMSS semantic clock-range validation, account-existence validation, or transaction-reference business validation.
A structurally valid but semantically unusual date/time proceeds through normal lookup behavior.

## Error Handling Strategy
- 400 `ERR-001`: transport validation;
- 401/403: existing security;
- 500 `ERR-500`: technical failure;
- 200 found false: absence.

## Logging and Observability
Reuse correlationId filter/MDC and existing error logging. No new observability stack.

## Security Strategy
Confirm detail path falls under existing matcher and requires `ACCOUNT_INQUIRER`. Add explicit tests for unauthenticated, unauthorized and authorized access. Frontend credential wiring must use the repository-approved mechanism; if no mechanism is present, record a blocker instead of hard-coding.

## Testing Strategy
1. repository tests for exact five-key lookup, absence, leading zeros and no list semantics;
2. service tests for found/not-found/technical mapping;
3. controller tests for path constraints and 200 envelope;
4. security tests;
5. OpenAPI conformance;
6. frontend client/component tests;
7. E2E found/not-found if auth/runtime can be configured;
8. INQTRANL regression.

## Runtime OpenAPI Reconciliation
The runtime resources OpenAPI currently lacks detail while a broad API file carries a historical detail signal. Treat feature contract as 005B source and update runtime resources contract; do not silently replace it with the historical path.

## Risks and Assumptions
- Existing `TransactionRow` may embody list-specific null defaults indirectly; inspect before reuse.
- Current H2 seed/nullability needs direct confirmation.
- Frontend auth gap may block E2E.
- Exact run commands/ports require repository inspection.
- Production DB2 behavior remains out of scope.

## Implementation Phases
1. **P0 — Reconfirm repository targets and runtime commands.**
2. **P1 — Extend domain/query/result mapping without behavior drift.**
3. **P2 — Implement exact repository lookup and persistence tests.**
4. **P3 — Implement service/controller/error/security behavior and tests.**
5. **P4 — Reconcile feature/runtime OpenAPI.**
6. **P5 — Integrate frontend detail client/view/route and tests.**
7. **P6 — Execute E2E/regression; resolve blockers.**
8. **P7 — Code review, QA review, Quickstart finalization, traceability evidence.**


## Artifact Relationships

- **Upstream Inputs:** `spec.md`, `supporting/architecture.md`, `research.md`, `data-model.md`, `supporting/requirements.md`, repository discovery.
- **Downstream Consumers:** `tasks.md`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`, `supporting/copilot-build-prompt.md`.
- **Authority Boundary:** Authoritative for implementation strategy and sequencing, not feature behavior.
- **Conflict Handling:** Specification/Requirements own behavior; repository inspection may refine placement but cannot silently change the approved contract.
