# Requirements Quality Checklist

Use this checklist to verify that Requirements, Specification, Plan, Tasks, Test Specification, and Traceability Matrix preserve frozen legacy behavior and satisfy approved requirements.

## Functional Requirements

- [ ] FR-001 through FR-014 are all represented in downstream artifacts with no omissions.
- [ ] Account identity filtering remains exact on sort code and account number only.
- [ ] Inclusive from/to filtering semantics are preserved for supplied boundaries.
- [ ] Omitted-date handling is represented as sentinel normalization with always-present date predicates, not as proven unconstrained filtering.
- [ ] Limit normalization is preserved (`0/omitted -> 50`, values above `100 -> 100`) and the 100-row maximum is enforced.
- [ ] Offset is applied after filtering and ordering and before returned-row selection.
- [ ] Ordering remains consistent and deterministic by transaction date descending then transaction time descending, with no invented tie-break semantics.
- [ ] `totalCount` and `returnedCount` semantics remain distinct and correct (pre-pagination vs returned rows).
- [ ] Empty-result behavior remains successful with zero returned rows and an empty transaction collection.
- [ ] Response transaction fields and transformations match legacy-evidenced mapping scope only.
- [ ] Read-only behavior is preserved; no transaction mutation is introduced.
- [ ] Retrieval failures never return partial successful results.
- [ ] Existing application compatibility is preserved, including no unrelated functionality changes and no architectural convention drift.
- [ ] INQTRAND remains explicitly out of scope as a separate future feature.

## Non-Functional Requirements

- [ ] Separation of concerns is preserved across controller, service, repository, mapper, and DTO responsibilities.
- [ ] Service layer retains ownership of business-rule orchestration and normalization logic.
- [ ] Persistence remains behind repository abstraction with future DB2/mainframe adapter compatibility.
- [ ] Validation ownership is explicit (boundary structural validation vs service-layer business normalization/enforcement).
- [ ] Observability and correlation conventions are followed.
- [ ] Feature-level traceability intent is preserved across all downstream artifacts.
- [ ] Required test coverage is represented (unit, repository integration, controller, OpenAPI conformance, frontend unit, E2E).

## Security Requirements

- [ ] No new feature-specific authorization behavior is invented beyond frozen evidence and approved policy.
- [ ] Boundary input validation remains syntactic/structural and does not invent unsupported domain rejection rules.
- [ ] SQL injection prevention remains in place through parameterized query behavior.
- [ ] Sensitive information is not leaked in client errors or standard logs.

## Operational Requirements

- [ ] Deployment/runtime compatibility with the existing backend and frontend setup is preserved.
- [ ] Existing configuration conventions (profiles, datasource binding, externalized settings) are preserved.
- [ ] Existing H2 and SQL initialization conventions are preserved for the POC path.
- [ ] No production DB2/CICS connectivity is introduced for this feature.
- [ ] Logging, monitoring, metrics, and correlation conventions remain aligned with Architecture.
- [ ] OpenAPI publication-path alignment is confirmed before merge.

## Modernization Decisions

- [ ] Approved modernization decisions remain explicitly separated from mandatory legacy-preservation requirements.
- [ ] Final omitted-date API behavior is backed by runtime evidence, SME approval, or an explicitly approved modernization decision before being treated as contractual.
- [ ] Date-validation and technical-error-envelope decisions are labeled as modernization decisions, not reclassified as legacy business rules.
- [ ] Database-native pagination is treated as conditional on behavioral equivalence proof.

## Legacy Preservation

- [ ] No validated legacy behavior has been removed, weakened, or altered without an approved modernization decision.
- [ ] No account-not-found or sentinel-account business behavior has been invented.
- [ ] Any unresolved evidence gaps remain documented as assumptions/risks, not converted into invented rules.
- [ ] Downstream artifacts keep omitted-date behavior provisional until the runtime/SME decision gate is resolved.

## Traceability

- [ ] Business Rules BR-001 through BR-019 map to Requirements without numbering drift.
- [ ] Requirements map forward to Specification acceptance criteria and behavior statements.
- [ ] Requirements map forward to Plan and Tasks work items.
- [ ] Requirements map forward to Test Specification coverage.
- [ ] End-to-end traceability is captured in the Traceability Matrix.
