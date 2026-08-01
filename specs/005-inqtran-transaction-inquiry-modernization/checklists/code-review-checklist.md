# Code Review Checklist

## 1. Repository Integration

- [ ] Existing backend application structure is reused; no parallel backend hierarchy was introduced. (Review repository structure)
- [ ] Existing frontend application structure is reused; no parallel frontend hierarchy was introduced. (Inspect frontend route and feature placement)
- [ ] Existing repository conventions are followed for packages, components, tests, and configuration. (Compare with adjacent implemented features)
- [ ] No duplicate shared utilities or duplicate infrastructure patterns were added. (Inspect shared utility and support areas)
- [ ] Repository-first implementation decisions were used for file/component placement. (Review placement against current repository evidence)
- [ ] Existing application extended; no second Spring Boot or React project. (Review repository structure)

## 2. Architecture and Component Responsibilities

- [ ] Controller is thin and constructor-injected. (Inspect controller)
- [ ] Service owns default/clamp/orchestration behavior. (Inspect service)
- [ ] Repository interface isolates JDBC and future adapters. (Inspect repository interface)
- [ ] Repository implementation remains persistence-only and does not own business orchestration. (Inspect repository implementation)
- [ ] Mapper exposes no invented fields and handles approved null/padding policy. (Inspect mapper)
- [ ] Business logic is not leaked into controllers or mappers. (Review controller and mapper responsibilities)
- [ ] Component responsibilities align with the approved architecture boundaries defined in supporting/architecture.md and the implementation responsibilities defined in plan.md. (Compare implementation with architecture.md and plan.md)

## 3. Persistence and Data Access

- [ ] SQL is parameterized and read-only. (Inspect JDBC query construction)
- [ ] Parameter binding is used for filter inputs; no request-driven SQL string concatenation is present. (Inspect query methods)
- [ ] Exact sort code and account filters are preserved. (Compare with business-rules.md and spec.md)
- [ ] Supplied date bounds are inclusive where approved. (Compare with business-rules.md and spec.md)
- [ ] Count/list filter parity is preserved. (Inspect count and row query paths)
- [ ] Ordering occurs before pagination behavior. (Inspect query and pagination flow)
- [ ] No tertiary ordering key was introduced. (Inspect ORDER BY behavior)
- [ ] No mock JSON persistence path was introduced. (Inspect persistence wiring)
- [ ] Future adapter seam remains preserved through repository abstraction. (Inspect repository interface and implementation boundaries)
- [ ] No live mainframe connection was introduced. (Inspect runtime/persistence configuration)

## 4. Validation and Business Behavior

- [ ] Approved validation only is implemented; unsupported rejection rules were not introduced. (Compare with supporting/requirements.md and spec.md)
- [ ] No unsupported account-existence validation was introduced. (Inspect service and controller validation)
- [ ] No unsupported 404 behavior was introduced for empty transaction outcomes. (Inspect controller responses)
- [ ] Optional omitted-date behavior matches the approved modernization contract. (Compare service behavior with spec.md and contracts/openapi.yaml)
- [ ] Verified legacy behavior is preserved where required by approved requirements. (Compare with supporting/business-rules.md)
- [ ] Exact filters, inclusive bounds, order, count, and pagination match approved behavior. (Compare with spec.md and test traces)
- [ ] No unevidenced tertiary sorting, account validation, or 404 behavior. (Inspect controller/service/repository)

## 5. API Contract

- [ ] Endpoint path, parameters, and response statuses match contracts/openapi.yaml. (Compare with OpenAPI)
- [ ] Response schemas match approved contract fields and constraints. (Compare DTOs with OpenAPI schemas)
- [ ] No undocumented response fields were introduced. (Inspect response payload construction)
- [ ] Feature and runtime OpenAPI are synchronized. (Compare feature contract with runtime publication)
- [ ] Contract behavior for populated, empty, validation-error, and technical-failure outcomes is preserved. (Compare with spec.md and OpenAPI)

## 6. Frontend Review

- [ ] Feature route integration follows existing frontend route/navigation conventions. (Inspect frontend route)
- [ ] Existing API-client pattern is reused for request construction. (Inspect frontend API client)
- [ ] Loading state behavior is present and consistent. (Inspect component state)
- [ ] Populated-result rendering aligns with approved response fields. (Inspect UI rendering)
- [ ] Empty-success rendering is non-error and contract-aligned. (Inspect empty state)
- [ ] Validation-error rendering is boundary-aligned and safe. (Inspect validation UI behavior)
- [ ] Technical-error rendering is safe and does not expose internals. (Inspect error UI behavior)
- [ ] Pagination behavior aligns to limit/offset semantics. (Inspect pagination controls)
- [ ] Subsequent inquiry replacement behavior is preserved. (Inspect state transitions)
- [ ] Existing shell/navigation behavior is preserved for unrelated routes. (Run shared route regression)

## 7. Security and Error Handling

- [ ] Existing route security policy is followed. (Inspect security configuration and route behavior)
- [ ] No new feature-specific authorization behavior was introduced. (Inspect security and controller flow)
- [ ] Parameterized JDBC query behavior is preserved for SQL-safety requirements. (Inspect repository queries)
- [ ] Technical failures do not leak SQL details. (Inspect error payloads and logs)
- [ ] Technical failures do not leak stack traces or internal implementation details. (Inspect error payloads)
- [ ] Safe error payload conventions are preserved. (Compare with existing error envelope behavior)
- [ ] Technical failures do not return partial successful pages. (Inspect service/controller failure flow)

## 8. Logging and Observability

- [ ] Logging avoids sensitive transaction payloads at normal levels. (Inspect logging statements)
- [ ] Logging avoids unnecessary full account identifiers where policy disallows. (Inspect logs and logging policy)
- [ ] Correlation/request identification is preserved where platform conventions support it. (Inspect request/correlation handling)
- [ ] Diagnostic logging remains sufficient for technical-failure analysis without sensitive leakage. (Inspect logging around failure paths)

## 9. Testing and Traceability

- [ ] Backend unit tests cover service normalization/orchestration and mapping behavior. (Review backend unit tests)
- [ ] Repository integration tests cover filters, ordering, pagination, and read-only/failure behavior. (Review repository integration tests)
- [ ] Controller/API tests cover 200/400/500 and empty-success behavior. (Review controller tests)
- [ ] OpenAPI contract tests cover endpoint/parameter/status/schema alignment and runtime reconciliation. (Review contract tests)
- [ ] Frontend tests cover request composition and all defined UI states. (Review frontend tests)
- [ ] End-to-end tests cover approved inquiry journeys and error journeys. (Review E2E tests)
- [ ] Regression suite evidence confirms no unrelated feature breakage. (Run regression suite)
- [ ] Traceability matrix remains consistent with requirements, tasks, and test-spec IDs. (Cross-check traceability matrix)
- [ ] Test implementation remains aligned with supporting/test-spec.md and does not introduce unsupported or unapproved verification scenarios. (Compare automated tests with supporting/test-spec.md)

## 10. Documentation and Operational Readiness

- [ ] OpenAPI documentation and runtime publication notes are current. (Compare contract and runtime OpenAPI)
- [ ] Backend/frontend usage documentation and quickstart are updated for Feature 005. (Review docs)
- [ ] Runtime OpenAPI, quickstart, README, and implementation documentation remain internally consistent. (Cross-check documentation)
- [ ] Documented commands are reproducible for build/test/run workflows. (Run documented commands)
- [ ] Proof-of-concept limitations are explicitly documented (no live DB2/CICS, deferred production-adapter concerns). (Review docs for limitations)

## 11. Scope Control

- [ ] INQTRAND implementation/detail route was not added to this feature. (Inspect routes/controllers)
- [ ] No second backend was introduced. (Review repository structure)
- [ ] No second frontend was introduced. (Review repository structure)
- [ ] No unsupported behavior was introduced beyond approved Feature 005 scope. (Compare implementation against spec.md and requirements)
- [ ] Implementation matches approved requirements, specification, and implementation constraints. (Cross-check supporting/requirements.md, spec.md, and plan.md)
- [ ] No untraced implementation behavior exists outside tasks/test-spec/traceability coverage. (Cross-check tasks, test-spec, and traceability matrix)
