# Modernization Report — INQTRANL

## Current State
`INQTRANL` is a CICS/COBOL/DB2 read-only list inquiry. It receives a fixed COMMAREA, applies date and pagination defaults, counts matching `PROCTRAN` rows, fetches them newest-first, paginates in COBOL, maps up to 100 rows, and returns success. DB2 failures are recorded through `ABNDPROC` to `ABNDFILE` and cause a CICS abend.

## Future State
The existing Spring Boot/React application exposes an equivalent GET operation backed by H2/JDBC for the POC, with a repository seam for future mainframe integration. Standard application monitoring and technical errors replace the CICS error-recording mechanism.

## Business Value
- Makes transaction inquiry accessible through the existing modern application.
- Preserves filter, order, pagination, and count behavior.
- Establishes testable, traceable requirements and a replaceable integration boundary.
- Avoids coupling the list feature to the separate detail operation.

## Risk Assessment
Overall risk: **Medium–High** until DDL/nullability, security, date validation, local data policy, and OpenAPI authority are resolved. Behavioral SQL is clear, but interface edge cases are under-specified.

## Complexity Assessment
**Moderate.** Core query behavior is straightforward. Complexity comes from two-query consistency, pagination equivalence, fixed-width data, date sentinels, missing null indicators, error translation, and integration into shared applications.

## Modernization Effort
A contained feature increment: specification approval, H2/schema decision, backend layers, one frontend route/page, and comprehensive tests. Live mainframe integration is a later effort.

## Recommended Delivery Approach
1. Approve open questions and contract.
2. Implement schema/domain/repository with integration tests.
3. Add service/controller and conformance tests.
4. Add frontend and E2E.
5. Run full regression and review gates.
6. Defer INQTRAND to a separate feature package.

## Roadmap
- **Phase A:** INQTRANL POC on H2.
- **Phase B:** contract/security hardening and performance testing.
- **Phase C:** approved DB2/CICS adapter.
- **Future feature:** INQTRAND single-transaction detail using the composite key, only after separate evidence/specification.

## Evidence Reviewed
All files copied under `legacy/`, including primary/related COBOL, COMMAREA and DB2 copybooks, ABND error assets, generator prompt, and repository discovery report.

## Assumptions
- Existing repository conventions remain valid at implementation time.
- Optional SQL predicates are an acceptable semantic modernization of invalid legacy date sentinels.
- No transaction-detail behavior is needed to complete list inquiry.

## Open Questions
See `program-analysis.md` section 18 and `../research.md`.

## SME Validation Checklist
- [ ] Confirm query key and account-number width.
- [ ] Confirm no account validation/not-found business outcome.
- [ ] Confirm date omission and range semantics.
- [ ] Confirm limit default/clamp and offset behavior.
- [ ] Confirm ordering and tie behavior.
- [ ] Confirm all field nullability/padding/reference character rules.
- [ ] Confirm composite ID format and intended downstream use.
- [ ] Confirm HTTP validation and error mapping.
- [ ] Confirm route security.
- [ ] Confirm H2 schema/data and OpenAPI authority.
- [ ] Confirm INQTRAND as a separate future feature.

## Readiness
**Specification package readiness: Review-ready. Implementation readiness: Conditional.** Development can begin only after the blocking schema/nullability, security, validation, data-policy, and OpenAPI-authority decisions are recorded.
