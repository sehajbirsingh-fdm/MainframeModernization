# Requirements Checklist — 005B INQTRAND Transaction Detail Inquiry

## Requirement Quality
- [x] Every mandatory behavior is expressed with stable IDs in `supporting/requirements.md`.
- [x] Requirements are observable or constrain implementation quality without turning into task steps.
- [x] Assumptions and risks are separated from mandatory requirements.

## Legacy Preservation
- [x] Exact five-part lookup is preserved.
- [x] SQLCODE 100 successful absence is preserved.
- [x] Composite transaction ID is preserved.
- [x] Read-only behavior is preserved.
- [x] Nullable/no-indicator behavior is visible rather than defaulted away.

## Modernization Separation
- [x] REST path/status/security choices are labeled modernization decisions.
- [x] Repository reuse is not labeled legacy evidence.
- [x] Historical OpenAPI is not treated as legacy authority.

## Functional Coverage
- [x] Found, not-found, field mapping, ID, date representation, UI and read-only behavior are covered.
- [x] Structural transport validation is explicit: sortCode 6 digits, accountNumber 8 digits, date 8 digits, time 6 digits, reference 12 digits, with leading zeroes preserved.
- [x] Structural validation excludes calendar-date semantic validation, HHMMSS semantic time-range validation, account-existence validation, and transaction-reference business validation.
- [x] Fixed-width preservation is scoped to transaction identity representation and does not require preserving COBOL fixed-CHAR padding for all modern JSON detail strings.
- [x] Successful absence contract is explicit and complete: HTTP 200, found=false, transaction=null, not 404, and not a technical failure.
- [x] Frontend integration is required (not optional): transaction list row -> five identity components -> detail navigation -> existing transaction API client -> INQTRAND detail endpoint -> found-detail or successful-absence UI.
- [x] INQTRANL list semantics are explicitly out of scope.
- [x] No hidden eyecatcher/delete/type filter is introduced.

## Non-Functional Coverage
- [x] Repository/integration direction is precise: remain within existing `com.bankofz.mainframemodernization.inqtran` vertical slice; extend existing `TransactionRepository` and `JdbcTransactionRepository`; reuse H2/`PROCTRAN`/JDBC; do not introduce a parallel top-level `inqtrand` repository subsystem without demonstrated architectural need.
- [x] Decimal precision, prepared SQL, testing and correlation requirements are defined.

## Security Coverage
- [x] Existing secured path/role is identified.
- [x] No separate/hard-coded authentication mechanism is approved.

## Operational Coverage
- [x] 400/500 conventions and correlation requirements are defined.
- [x] Runtime OpenAPI reconciliation is required.
- [x] H2 POC boundary is explicit.

## Specification Alignment
- [ ] `spec.md` alignment verification remains pending the established review workflow.
- [x] Empty-result and technical-failure semantics are distinct.

## OpenAPI Readiness
- [ ] Feature OpenAPI verification remains pending the established review workflow.
- [ ] Successful not-found response-schema verification remains pending the established review workflow.

## Traceability Readiness
- [ ] Traceability matrix verification remains pending the established review workflow.
- [ ] Implementation evidence is complete. **Not applicable pre-implementation; remains pending.**

## Assumptions and Risks
- [x] Missing production DDL/CICS definitions/auth wiring/runtime command evidence is explicitly visible.
- [x] No unresolved gap has been silently promoted to confirmed behavior.

## Final Gate
**PASS FOR REQUIREMENTS READINESS ONLY.** Approved requirements and upstream documentation are ready to drive downstream work. Downstream Spec/OpenAPI/Test Spec/Traceability/Plan/Tasks artifacts still require their established verification passes where applicable, and implementation evidence remains pending pre-implementation.


## Artifact Relationships

- **Upstream Inputs:** `supporting/requirements.md`, `spec.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `contracts/openapi.yaml`, `supporting/traceability-matrix.md`.
- **Downstream Consumers:** `supporting/copilot-build-prompt.md`, implementation start gate.
- **Authority Boundary:** Authoritative for pre-implementation documentation readiness only.
- **Conflict Handling:** Unchecked implementation evidence cannot be promoted to pass; any upstream documentation change requires re-running this checklist.
