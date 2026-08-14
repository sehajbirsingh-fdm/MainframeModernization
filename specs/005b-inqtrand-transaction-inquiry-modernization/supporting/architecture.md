# Architecture — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Define target structure, component boundaries, interfaces, and constraints using repository-first evidence.

## Architectural Drivers
Repository reuse, exact five-part read-only lookup, zero-or-one result, H2/JDBC POC, existing security/correlation, normal-not-found separation, layered testability.

## Current-State Context
Repository discovery confirms:
- Java 21, Spring Boot 3.5.3, Maven.
- JDBC/DataSource repositories; no JPA repository layer.
- H2 DB2 mode with `schema.sql` / `data.sql`.
- existing `com.bankofz.mainframemodernization.inqtran` vertical slice.
- React + TypeScript + Vite, React Router, fetch clients, React Query.
- Vitest/Testing Library and Playwright.
- `/api/v1/accounts/**` secured with `ACCOUNT_INQUIRER`.
- existing correlation-ID filter/MDC conventions.

## Target Context
Extend the current `inqtran` feature slice, not a new top-level transaction subsystem.

## Component Model
```text
React transaction feature
  -> transaction API client
  -> GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}
  -> transaction controller boundary
  -> service/detail behavior
  -> transaction repository abstraction
  -> JDBC / H2 PROCTRAN
```

## Backend Components
Evidence-backed root:
`backend/api/src/main/java/com/bankofz/mainframemodernization/inqtran/`

Repository boundary decision:
- Extend the existing `TransactionRepository` / `JdbcTransactionRepository` boundary inside the current `inqtran` slice for the exact five-key detail lookup.
- Rationale: repository discovery confirms this abstraction/JDBC implementation already owns transaction persistence concerns in the same feature slice and already operates on `PROCTRAN`; introducing a separate parallel detail repository hierarchy is unnecessary for the approved scope and would conflict with the approved intent to extend, not fork, the transaction subsystem.

Responsibilities:
- Controller: path/shape validation and delegation.
- Service: preserve found versus successful-absence semantics and separate technical-failure outcomes.
- Repository: exact five-key lookup returning zero-or-one matched transaction data.
- JDBC implementation: prepared exact predicate query.
- Mapper/domain/API boundary: reuse compatible transaction types and expose the approved modern envelope semantics (`found` + nullable/absent transaction detail) per Intended System decisions.
- Exception handler: reuse validation/technical response conventions.

Do not invent parallel packages merely because the operation is detail.

## Frontend Components
Evidence-backed locations:
- `frontend/app/src/features/transactionInquiry/`
- `frontend/app/src/api/transactionInquiryClient.ts`
- `frontend/app/src/domain/transactionTypes.ts`
- `frontend/app/src/App.tsx`

Add detail behavior/routes inside these foundations with minimal approved navigation flow:

Transaction list row
-> five identity components (sortCode, accountNumber, date, time, reference)
-> transaction detail route
-> existing transaction API-client foundation
-> approved INQTRAND detail endpoint
-> detail state (`found:true`) or successful-absence state (`found:false`, transaction null).

## Persistence Boundary
- reuse H2 `PROCTRAN`;
- reuse DataSource/PreparedStatement style;
- predicates: all five identity columns;
- no count/order/pagination/date-range SQL;
- no record-eyecatcher/delete/type predicate;
- no detail null defaulting;
- no real DB2 connectivity in POC.

## API Boundary
Approved path:
`GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`.

It stays under existing secured account paths. Historical `/accounts/{accountId}/transactions/{transactionId}` broad-OpenAPI signal is not authority.

Approved response boundary (modern, not legacy COMMAREA shape):
- found: `{ "found": true, "transaction": {...} }`
- successful absence: `{ "found": false, "transaction": null }`

This resolves the wire/transport representation decisions deferred in MM-016 and MM-017 while preserving legacy business semantics (found vs successful absence vs technical failure).

## Data Flow
1. client supplies fixed-width key;
2. existing security authenticates/authorizes;
3. controller validates approved exact digit widths (sortCode 6, accountNumber 8, date 8, time 6, reference 12);
4. service requests one lookup;
5. repository executes exact prepared query;
6. row → `found:true` with detail + derived ID;
7. no row → 200 `found:false`, `transaction:null`;
8. technical failure → structured 500 with correlation ID.

## Error Flow
- syntax/shape validation → 400 `ERR-001`;
- unauthenticated → 401;
- unauthorized → 403;
- technical persistence/unexpected → 500 `ERR-500`;
- normal absence → 200, not 404.

Structured HTTP error handling is the modern architectural replacement for legacy CICS/ABNDPROC technical-failure mechanics. The target preserves the distinction between normal business outcomes (found or successful absence) and technical failure without reproducing CICS abend internals.

## Configuration and Runtime Topology
No new process. Existing backend/H2/frontend topology remains. Exact commands and ports must be verified at implementation time.

## Security Boundary
Reuse bearer token handling and `ACCOUNT_INQUIRER`; no alternate feature auth.

## Observability Boundary
Reuse `CorrelationIdFilter`, MDC correlationId, existing error/logging conventions.

## Architectural Constraints
- Java 21 / Spring Boot 3.5.3.
- constructor injection.
- thin controllers; service behavior.
- JDBC, not JPA.
- H2 POC, no real mainframe connectivity.
- current package/frontend organization.
- runtime OpenAPI reconciliation.
- no duplicate transaction hierarchy without proven need.

## Architectural Risks
- broad/historical detail contract differs from runtime absence of detail.
- INQTRANL null amount→zero cannot be blindly reused.
- frontend auth gap.
- seed sufficiency unknown.


## Artifact Relationships

- **Upstream Inputs:** `supporting/intended-system.md`, `supporting/dependency-map.md`, `supporting/mapping-matrix.md`, `repository-discovery-report.md` (stable repository-discovery reference).
- **Downstream Consumers:** `research.md`, `data-model.md`, `plan.md`, `tasks.md`, `supporting/copilot-build-prompt.md`.
- **Authority Boundary:** Authoritative for approved target boundaries and repository integration constraints.
- **Conflict Handling:** If implementation-time inspection disproves a path/convention, reconcile architecture/plan first; preserved legacy behavior still wins.
