# Research and Decisions - Feature 005 INQTRAN Transaction List Modernization

## 1. Purpose
This artifact records why modernization decisions were made for Feature 005 and how those decisions align with frozen upstream constraints.

It focuses on decision rationale, alternatives, tradeoffs, and unresolved evidence. It does not redefine business behavior, specify API contracts, finalize data models, or define implementation tasks.

## 2. Authoritative Inputs
The following frozen artifacts are treated as authoritative and were used as decision constraints:
- supporting/program-analysis.md
- supporting/dependency-map.md
- supporting/business-rules.md
- supporting/mapping-matrix.md
- supporting/intended-system.md
- supporting/architecture.md

Evidence classification terms used in this document:
- Confirmed upstream constraint
- Approved modernization direction
- Architectural design decision
- Remaining uncertainty

## 3. Research Questions
- Which modernization stack should host the capability with the least delivery and maintenance risk?
- How should the backend boundary be expressed so that legacy behavior is preserved without carrying forward legacy runtime structures?
- What local persistence strategy best supports development and demonstration while preserving adapter seams?
- How should architecture isolate orchestration from persistence and transport concerns?
- How should pagination, sentinel handling, and count semantics be preserved without prematurely fixing downstream contract detail?
- How should technical retrieval failure behavior be represented in modern runtime terms?
- Should INQTRAND be included in Feature 005 or remain outside scope?
- Which decisions must be deferred because evidence is incomplete?

## 4. Established Constraints
- Account identity filtering, inclusive date filtering, ordering, pagination order, and count semantics are fixed by upstream constraints.
- Read-only inquiry behavior is fixed by upstream constraints.
- No partial successful outcome after technical retrieval failure is fixed by upstream constraints.
- Deterministic composite transaction ID construction is fixed; uniqueness is unproven.
- INQTRAND is related but not a direct runtime dependency for INQTRANL list behavior.
- Date representation compatibility and null-handling behavior remain unresolved evidence areas.

Classification: Confirmed upstream constraint

## 5. Decision: Spring Boot Application Stack
Decision:
- Use the existing Spring Boot based backend stack already present in the repository for Feature 005.

Rationale:
- The repository already standardizes on Java 21 and Spring Boot conventions.
- Reusing the established stack reduces integration risk, onboarding cost, and operational divergence.
- Existing project tooling and test infrastructure align with this choice.

Alternatives considered:
- Introduce a separate runtime stack for this feature.
- Recreate a legacy-style runtime boundary for inquiry behavior.

Why alternatives were rejected:
- A separate stack increases platform and operational complexity without evidence of feature-level benefit.
- Legacy runtime recreation conflicts with modernization intent and does not improve preservation of business behavior.

Status:
- Approved modernization direction

## 6. Decision: REST-Style Backend Boundary
Decision:
- Use a REST-style backend boundary to expose inquiry capability through the existing modern application interface style.

Rationale:
- This matches current repository integration patterns between frontend and backend.
- It preserves separation between transport boundary concerns and application orchestration concerns.
- It supports progressive contract formalization in downstream artifacts.

Alternatives considered:
- Preserve COMMAREA-like interchange at system boundary.
- Use an internal-only invocation boundary and bypass standard application transport patterns.

Why alternatives were rejected:
- COMMAREA-style boundary couples modern clients to legacy representation and weakens maintainability.
- Nonstandard boundary patterns reduce consistency with the current application and increase adoption risk.

Status:
- Approved modernization direction

## 7. Decision: H2 for Local Development and Demonstration
Decision:
- Use H2 for local development and demonstration in the current project context.

Rationale:
- H2 is already used in the repository development workflow.
- It enables fast local execution and deterministic testing feedback loops.
- It supports the adapter abstraction without requiring production DB2 connectivity.

Alternatives considered:
- Connect directly to DB2 during feature development.
- Use a file-based fixture-only approach without a relational engine.

Why alternatives were rejected:
- Direct DB2 dependency increases setup friction and introduces environment coupling not required for feature-level modernization.
- Fixture-only approach weakens confidence in persistence-layer behavior and count/retrieval orchestration.

Status:
- Approved modernization direction

## 8. Decision: Layered Architecture
Decision:
- Use layered architecture with presentation, application, domain, persistence, and infrastructure responsibilities.

Rationale:
- Upstream constraints require clear ownership for normalization, orchestration, mapping, and error handling.
- Layering reduces coupling and supports focused testing seams.
- It improves long-term maintainability for future related inquiry capabilities.

Alternatives considered:
- Collapsed feature logic with minimal layer separation.
- Persistence-centric orchestration where retrieval semantics are spread across boundaries.

Why alternatives were rejected:
- Collapsed layering obscures responsibility ownership and increases regression risk for preserved behavior.
- Persistence-centric orchestration makes behavior harder to reason about and harder to verify against upstream constraints.

Status:
- Architectural design decision

## 9. Decision: Repository Abstraction
Decision:
- Keep persistence behind a repository abstraction with replaceable concrete adapters.

Rationale:
- Upstream dependency analysis confirms legacy behavior depends on data access patterns, not a specific modern adapter.
- The abstraction preserves flexibility for a future DB2/mainframe adapter without changing application orchestration.
- It improves testability and isolates persistence-specific concerns.

Alternatives considered:
- Direct persistence implementation usage from orchestration layer.
- One-off feature-specific data access with no abstraction seam.

Why alternatives were rejected:
- Direct implementation coupling reduces portability and increases rewrite cost for future adapter changes.
- No abstraction seam weakens isolation and cross-layer test strategy.

Status:
- Architectural design decision

## 10. Decision: Pagination Preservation Strategy
Decision:
- Preserve upstream pagination semantics by maintaining separation of filtered total counting from row retrieval and preserving offset/limit behavior constraints.

Rationale:
- Upstream artifacts establish pre-pagination total count and post-pagination returned count semantics.
- Preserving this split avoids behavioral drift for empty, partial, and offset-heavy cases.
- It aligns with architectural ownership where orchestration assembles final metadata.

Alternatives considered:
- Single-pass page-only retrieval semantics without explicit filtered total semantics.
- Implicit count derivation from returned rows.

Why alternatives were rejected:
- Those alternatives cannot preserve established total-versus-returned semantics and would change observable behavior.

Status:
- Confirmed upstream constraint reflected as architectural design decision

## 11. Decision: Legacy Sentinel Handling
Decision:
- Preserve sentinel-handling intent from upstream behavior while deferring final contract expression to downstream artifacts.

Rationale:
- Upstream artifacts confirm sentinel control behavior in legacy flow and associated conversion path uncertainty.
- Treating sentinel semantics as preserved constraints avoids business behavior drift.
- Deferring final boundary representation avoids prematurely locking unresolved date compatibility decisions.

Alternatives considered:
- Force strict rejection of sentinel-style controls at the modernization boundary.
- Treat sentinels as fully resolved null-equivalent semantics immediately.

Why alternatives were rejected:
- Rejection-first behavior risks breaking preserved legacy intent without approved downstream decision.
- Full null-equivalent assertion is not fully proven by available evidence.

Status:
- Confirmed upstream constraint plus remaining uncertainty

## 12. Decision: Domain Representation Principles
Decision:
- Preserve legacy-observed value semantics in domain representation, including deterministic composite ID construction and stable handling of fixed-width identifier semantics, while avoiding unevidenced enrichment.

Rationale:
- Upstream mapping and business-rule artifacts define what is observed and what is unresolved.
- Preserving representation intent supports traceable behavior equivalence.
- Avoiding enrichment prevents contract drift before downstream approval.

Alternatives considered:
- Normalize all values to a new inferred canonical model immediately.
- Add derived business fields not evidenced upstream.

Why alternatives were rejected:
- Canonical remapping may hide unresolved compatibility constraints.
- Additional fields are not evidence-backed and may misrepresent legacy behavior.

Status:
- Architectural design decision constrained by confirmed upstream behavior

## 13. Decision: Modern Technical Failure Handling
Decision:
- Represent technical retrieval failures through modern centralized error handling and observability patterns, preserving the no-partial-success rule.

Rationale:
- Upstream artifacts confirm legacy technical failure paths and operational abend recording role.
- Modern centralized handling keeps operational behavior diagnosable without recreating legacy runtime infrastructure.
- This preserves behavior intent while fitting current platform conventions.

Alternatives considered:
- Recreate legacy-style operational error store as feature functionality.
- Allow partial data return when count or retrieval subpaths fail.

Why alternatives were rejected:
- Recreating legacy operational store adds complexity with limited modernization value.
- Partial return conflicts with confirmed upstream no-partial-success constraint.

Status:
- Approved modernization direction with confirmed upstream constraint

## 14. Decision: Keep INQTRAND Outside Feature 005
Decision:
- Keep INQTRAND detail inquiry outside Feature 005 scope.

Rationale:
- Upstream dependency analysis confirms no direct runtime linkage from INQTRANL list flow to INQTRAND.
- Scope isolation reduces feature coupling and delivery risk.
- Related capability can be delivered as a separate feature slice with independent approval.

Alternatives considered:
- Include list and detail modernization together in Feature 005.
- Add provisional runtime linkage to detail behavior preemptively.

Why alternatives were rejected:
- Combined scope increases complexity and blurs traceability to current feature constraints.
- Preemptive linkage introduces behavior not required by current upstream evidence.

Status:
- Confirmed upstream constraint plus architectural design decision

## 15. Testing Research Conclusions
- Layered architecture and repository abstraction provide clear seams for test stratification.
- Critical preservation tests should target orchestration-level behavior constraints from frozen upstream artifacts.
- Persistence behavior should be validated through abstraction contracts, not transport boundary assumptions.
- Cross-layer integration testing remains necessary to verify end-to-end preservation of count/retrieval and failure behavior.

Classification:
- Architectural design decision

## 16. Decisions Deferred to Downstream Artifacts
The following are intentionally deferred:
- Exact contract expression of boundary-level date and control semantics.
- Final expression for control-field exposure and transport-level success/error representation.
- Detailed validation policy choices where upstream evidence is inconclusive.
- Final persistence schema representation details and null behavior policy.
- Detailed security policy decisions specific to this capability.

Classification:
- Remaining uncertainty

## 17. Remaining Evidence Gaps and Risks
- Date representation compatibility gap between declared DB2 date shape and host conversion path.
- Null-handling behavior gap where indicator-variable behavior is not evidenced.
- Type-compatibility edge risk for character-to-numeric-display conversions.
- Ordering tie ambiguity beyond date/time ordering.
- Missing production deployment and runtime definition details not needed for feature intent but relevant to operational rollout.

Classification:
- Remaining uncertainty

## 18. Decision Summary

| Topic | Decision | Classification |
|---|---|---|
| Application stack | Use existing Spring Boot based backend stack in this repository context | Approved modernization direction |
| Backend boundary style | Use REST-style backend boundary consistent with current application conventions | Approved modernization direction |
| Local/demo persistence | Use H2 for local development and demonstration | Approved modernization direction |
| Architecture shape | Use layered architecture with clear responsibility separation | Architectural design decision |
| Persistence seam | Use repository abstraction with replaceable adapters | Architectural design decision |
| Pagination behavior | Preserve split between filtered total count and paged row retrieval semantics | Confirmed upstream constraint |
| Sentinel handling | Preserve sentinel intent and defer final boundary expression where evidence is incomplete | Confirmed upstream constraint plus remaining uncertainty |
| Domain representation | Preserve evidence-backed semantics and avoid unevidenced enrichment | Architectural design decision |
| Technical failure handling | Use modern centralized technical-failure handling with no-partial-success preservation | Approved modernization direction with confirmed upstream constraint |
| Feature scope | Keep INQTRAND outside Feature 005 | Confirmed upstream constraint plus architectural design decision |
