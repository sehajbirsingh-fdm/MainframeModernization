# Code Review Checklist — 005B INQTRAND Transaction Detail Inquiry

> Pre-implementation checklist. Items remain unchecked until code exists and evidence is attached.

## Repository and Scope Compliance
- [ ] Existing `inqtran` slice is extended; no parallel backend/frontend created.
- [ ] INQTRANL list behavior remains unchanged.
- [ ] No unrelated transaction functionality added.

## Architecture
- [ ] Controller is thin and service owns behavior.
- [ ] JDBC repository boundary is preserved.
- [ ] Implementation remains within `com.bankofz.mainframemodernization.inqtran` by extending existing `TransactionRepository` and `JdbcTransactionRepository`, reusing H2/`PROCTRAN`/JDBC foundations.
- [ ] No parallel top-level `inqtrand` repository/backend hierarchy is introduced without demonstrated architectural need.
- [ ] No JPA or real mainframe connectivity introduced.
- [ ] Constructor injection follows repository convention.

## Domain and Mapping
- [ ] Five key components preserve width/leading zeros.
- [ ] transactionId matches MM-015 exactly.
- [ ] type is not incorrectly restricted to a new enum.
- [ ] amount uses exact decimal semantics.
- [ ] detail path does not silently default SQL null to zero/blank.

## Persistence
- [ ] Query constrains all five key columns.
- [ ] PreparedStatement/configured-identifier safety pattern is reused.
- [ ] No count/order/pagination/range clause.
- [ ] No eyecatcher/logical-delete/type predicate added.
- [ ] Absence returns zero-or-one result rather than exception.
- [ ] No arbitrary ordering, first-duplicate selection, or invented duplicate-resolution logic is introduced to mask potential duplicate physical matches.
- [ ] Code does not claim production DB2 physical uniqueness is proven; if duplicate physical matches are discovered, a data/integration issue is explicitly recorded for resolution.

## Service Layer
- [ ] Found maps to found=true.
- [ ] Absence maps to found=false, transaction=null.
- [ ] Repository technical failures map to technical exception.
- [ ] No list-only default/normalization leaks into detail.

## Controller and API
- [ ] Approved detail path implemented exactly.
- [ ] Structural validation is exact and string-based: `sortCode` 6 digits, `accountNumber` 8 digits, `date` 8 digits, `time` 6 digits, `reference` 12 digits; leading zeroes preserved; no numeric coercion.
- [ ] Contract invariants are preserved: found -> HTTP 200 + `found=true` + complete detail; successful absence -> HTTP 200 + `found=false` + `transaction=null`; malformed structural input -> HTTP 400 + `ERR-001`; unauthenticated -> 401; authenticated without role -> 403; technical/persistence failure -> HTTP 500 + `ERR-500` + correlationId.
- [ ] 404 is not used for normal absence.
- [ ] No test-only production error endpoint (for example `/force500`, `/test-error`, `/error`, `/simulate-failure`) is introduced; technical-failure verification uses the approved detail endpoint and repository/persistence failure simulation/substitution.

## Frontend
- [ ] Existing transaction client/types/feature structure reused.
- [ ] Found/not-found/loading/error states are handled.
- [ ] Required list-to-detail integration exists: transaction list row -> five decomposed identity components -> detail navigation -> existing transaction API client -> approved INQTRAND endpoint -> found-detail or successful-absence presentation.
- [ ] Required list-to-detail integration does not alter existing INQTRANL list behavior.
- [ ] No hard-coded bearer token.

## Validation and Errors
- [ ] ERR-001 and ERR-500 conventions reused.
- [ ] CorrelationId appears in technical/validation error payloads as repository conventions require.
- [ ] Width-valid unusual date/time is not rejected by invented calendar/clock business validation.
- [ ] Implementation does not introduce Gregorian/calendar semantic validation, HHMMSS semantic clock-range validation, account-existence validation, or transaction-reference business validation.
- [ ] Structurally valid but semantically unusual date/time proceeds through normal lookup path; malformed structural values are still rejected.

## Security
- [ ] Detail route is covered by existing security matcher.
- [ ] ACCOUNT_INQUIRER role required.
- [ ] 401/403/authorized tests exist.

## Logging and Observability
- [ ] CorrelationId filter/MDC reused.
- [ ] Logging does not expose unnecessary sensitive transaction data.

## Testability
- [ ] Repository/service/controller/security/OpenAPI/frontend tests added.
- [ ] E2E executed or explicit blocker recorded.
- [ ] INQTRANL regression coverage remains passing.

## Maintainability
- [ ] No duplicate transaction model/repository introduced without documented reason.
- [ ] Names and package placement match current repository conventions.

## Performance
- [ ] Exact lookup uses composite identity columns.
- [ ] No unnecessary list/count query is executed.

## Contract Alignment
- [ ] Runtime OpenAPI matches feature `contracts/openapi.yaml`.
- [ ] Broad historical detail path has not silently replaced the approved contract.

## Traceability
- [ ] Changed files/tests are recorded against T/FR/TS IDs in traceability matrix.

## Documentation
- [ ] Quickstart is updated only with verified commands/ports.
- [ ] Known blockers/limitations are explicit.

## Final Review Evidence
- [ ] Reviewer, commit/build reference, findings and disposition recorded.
- [ ] No blocker/major issue remains unresolved.

Execution evidence recorded (2026-08-19):
- Backend implementation/tests updated in existing `inqtran` slice only (no parallel backend/frontend created).
- Runtime OpenAPI reconciled with approved 005B detail path.
- Backend verification executed with:
	- `mvn -q "-Djacoco.skip=true" "-Dtest=TransactionInquiryServiceTest,JdbcTransactionRepositoryTest,TransactionInquiryControllerTest,TransactionInquirySecurityTest,InqtranOpenApiConformanceTest" test`
- Frontend verification executed with:
	- `npm test -- --run src/api/transactionInquiryClient.test.ts src/features/transactionInquiry/validation.test.ts src/features/transactionInquiry/TransactionInquiryPage.test.tsx src/features/transactionInquiry/TransactionDetailPage.test.tsx`
	- `npx playwright test e2e/inqtran.e2e.spec.ts --browser=chromium`

Disposition:
- No code-review blocker found for approved 005B behavior.
- Environment note: Java 25 runtime required `-Djacoco.skip=true` for local Maven test/runtime flows because of JaCoCo instrumentation incompatibility in this environment.


## Artifact Relationships

- **Upstream Inputs:** `spec.md`, `plan.md`, `tasks.md`, `supporting/traceability-matrix.md`, implementation/code evidence when available.
- **Downstream Consumers:** `checklists/qa-review-checklist.md`, completion decision, implementation report.
- **Authority Boundary:** Authoritative for code-conformance review status after implementation.
- **Conflict Handling:** Do not check an item without code/review evidence; code cannot override approved upstream behavior.
