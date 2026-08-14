# Code Review Checklist — 00B INQTRAND Transaction Detail Inquiry

> Pre-implementation checklist. Items remain unchecked until code exists and evidence is attached.

## Repository and Scope Compliance
- [ ] Existing `inqtran` slice is extended; no parallel backend/frontend created.
- [ ] INQTRANL list behavior remains unchanged.
- [ ] No unrelated transaction functionality added.

## Architecture
- [ ] Controller is thin and service owns behavior.
- [ ] JDBC repository boundary is preserved.
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

## Service Layer
- [ ] Found maps to found=true.
- [ ] Absence maps to found=false, transaction=null.
- [ ] Repository technical failures map to technical exception.
- [ ] No list-only default/normalization leaks into detail.

## Controller and API
- [ ] Approved detail path implemented exactly.
- [ ] Only digit-width validation is added for key semantics.
- [ ] 200/400/401/403/500 behaviors match contract.
- [ ] 404 is not used for normal absence.

## Frontend
- [ ] Existing transaction client/types/feature structure reused.
- [ ] Found/not-found/loading/error states are handled.
- [ ] List-to-detail navigation, if added, uses decomposed key without altering list inquiry.
- [ ] No hard-coded bearer token.

## Validation and Errors
- [ ] ERR-001 and ERR-500 conventions reused.
- [ ] CorrelationId appears in technical/validation error payloads as repository conventions require.
- [ ] Width-valid unusual date/time is not rejected by invented calendar/clock business validation.

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


## Artifact Relationships

- **Upstream Inputs:** `spec.md`, `plan.md`, `tasks.md`, `supporting/traceability-matrix.md`, implementation/code evidence when available.
- **Downstream Consumers:** `checklists/qa-review-checklist.md`, completion decision, implementation report.
- **Authority Boundary:** Authoritative for code-conformance review status after implementation.
- **Conflict Handling:** Do not check an item without code/review evidence; code cannot override approved upstream behavior.
