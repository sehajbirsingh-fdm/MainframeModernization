# Intended System - INQTRAN Transaction List Inquiry

## 1. Purpose
This document defines the intended modern system behavior and boundaries for the INQTRAN transaction-list inquiry capability.

It bridges legacy-grounded upstream analysis and downstream design artifacts by stating:
- what the modern capability is intended to provide,
- what legacy-observed behavior must be preserved,
- what modernization direction is approved at a high level,
- what remains unresolved.

Evidence status terms used in this document:
- Confirmed upstream behavior: established by the frozen legacy-derived artifacts.
- Approved modernization direction: accepted modernization intent already reflected in current project artifacts.
- Reasonable design assumption: plausible working assumption not yet finalized by downstream contract decisions.
- Remaining uncertainty: behavior or decision not provable/final from current artifacts.

## 2. Modernization Goals
- Preserve proven legacy inquiry semantics while delivering the capability in the modern application context. (Approved modernization direction)
- Keep the capability read-only for transaction inquiry. (Confirmed upstream behavior)
- Improve operational maintainability by using current application conventions instead of CICS/COMMAREA runtime dependencies. (Approved modernization direction)
- Enable downstream architecture, requirements, and specification work without prematurely fixing API contract details. (Approved modernization direction)

## 3. Intended Capability
The intended capability is an account transaction-list inquiry that allows an operator to request transactions for a specific sort code and account number, optionally constrain by date boundaries, and receive:
- transaction rows,
- a filtered total count,
- a returned row count for the current page.

The capability is intended to preserve the established legacy behavior constraints while presenting that behavior through the modern system surface. (Approved modernization direction constrained by confirmed upstream behavior)

## 4. System Scope
In scope:
- Transaction-list inquiry for one account identity (sort code plus account number).
- Optional date-bound filtering behavior as constrained by upstream artifacts.
- Pagination behavior with legacy-equivalent limit normalization and offset application semantics.
- Returned transaction rows and metadata counts.

Out of scope in this artifact:
- Detailed interface contract fields and protocol-level outcomes.
- Single-transaction detail capability as a deliverable in this feature.
- Any write/update behavior.

## 5. Intended User Experience
At a high level, the intended user experience is:
- provide account identity and optional filters,
- request the transaction list,
- view ordered results and count metadata,
- observe an empty successful result when no rows match,
- observe a technical failure outcome when retrieval fails.

This artifact intentionally does not define exact request/response schema shape, protocol statuses, or endpoint paths. (Approved modernization direction)

## 6. System Context
The intended system operates within the existing modernization repository and connects:
- a user-facing interface,
- a backend inquiry capability,
- a persistence source representing transaction records.

Context boundaries:
- Upstream frozen artifacts establish the legacy behavior to preserve.
- Downstream architecture and specification artifacts will decide precise technical contracts.
- The intended system must remain consistent with currently approved project conventions without inventing unsupported business behavior.

## 7. Backend Responsibilities
The backend is intended to:
- accept inquiry inputs from the frontend boundary,
- apply preserved behavior constraints from upstream artifacts,
- orchestrate retrieval and count operations,
- enforce no-partial-success outcome on technical retrieval failure,
- return inquiry results and metadata for presentation.

This section is intentionally responsibility-level only and does not define component/class-level design. (Approved modernization direction)

## 8. Frontend Responsibilities
The frontend is intended to:
- capture supported inquiry inputs,
- submit an inquiry request,
- render returned rows and count metadata,
- present empty-result and technical-failure states clearly.

This section does not define detailed screen layout, exact field validation rules, or protocol contracts. (Approved modernization direction)

## 9. Persistence Responsibilities
Persistence is intended to:
- provide read-only access to transaction data for list retrieval,
- support filtered counting separate from paged row retrieval,
- preserve ordering and filtering semantics required by upstream constraints.

This artifact does not define physical schema, DDL, or final query formulation. (Approved modernization direction)

## 10. High-Level Request and Response Flow
1. User provides account identity and optional inquiry controls.
2. Frontend sends inquiry intent to backend.
3. Backend normalizes applicable controls according to preserved semantics.
4. Backend obtains filtered total count.
5. Backend obtains ordered transaction rows and applies offset/limit semantics for returned rows.
6. Backend assembles output rows plus counts and success indicator semantics.
7. Frontend renders results, empty-success, or technical-failure outcome.

Flow constraints:
- count and retrieval paths are both required for final response assembly,
- successful completion is tied to successful completion of both paths,
- technical retrieval failure must not produce partial successful output.

## 11. Preserved Legacy Semantics
The modern intended system is constrained by the following upstream-preserved behavior:
- account and sort-code filtering, (Confirmed upstream behavior)
- inclusive date-range filtering, (Confirmed upstream behavior)
- sentinel date handling, (Confirmed upstream behavior)
- limit normalization, (Confirmed upstream behavior)
- pagination after filtering and ordering, (Confirmed upstream behavior)
- ordering by date descending and time descending, (Confirmed upstream behavior)
- absence of a proven third tie-breaker, (Remaining uncertainty)
- pre-pagination total count, (Confirmed upstream behavior)
- post-pagination returned count, (Confirmed upstream behavior)
- maximum returned page size of 100, (Confirmed upstream behavior)
- success only after successful count and retrieval, (Confirmed upstream behavior)
- no partial successful result after technical retrieval failure, (Confirmed upstream behavior)
- deterministic composite transaction ID construction, (Confirmed upstream behavior)
- unproven transaction-ID uniqueness. (Remaining uncertainty)

These constraints are inherited from upstream artifacts and are not redefined here.

## 12. System Boundaries
This intended system document defines capability intent and boundaries only.

It does not finalize:
- interface contract syntax,
- detailed architecture layers and component wiring,
- implementation technology decisions beyond established repository direction,
- testing detail and acceptance criteria structure.

Boundary with related capability:
- alignment between transaction-list identifier components and transaction-detail key components can inform future work, but the detail inquiry remains a separate feature scope. (Reasonable design assumption based on upstream evidence)

## 13. Out of Scope
- Formal requirement identifiers and requirement trace links.
- User-story acceptance criteria and test-case catalogs.
- Detailed API contract design.
- Database physical design details.
- Detailed component architecture and deployment topology.
- Implementation task planning.
- New authentication or authorization behavior not already approved elsewhere.

## 14. Modernization Assumptions
- The modern system surface can represent the preserved inquiry semantics without reintroducing legacy runtime structures. (Approved modernization direction)
- Existing project conventions for frontend, backend, and persistence can host this capability. (Approved modernization direction)
- The preserved behavior constraints listed in upstream artifacts remain the governing source for this feature unless formally superseded. (Reasonable design assumption)

## 15. Constraints
- Must remain consistent with frozen upstream artifacts: program analysis, dependency map, business rules, and mapping matrix. (Confirmed constraint)
- Must preserve read-only inquiry behavior. (Confirmed upstream behavior)
- Must not invent unsupported domain rules to resolve legacy ambiguities. (Approved modernization direction)
- Must defer unresolved contract-level decisions to downstream requirements/specification artifacts. (Approved modernization direction)

## 16. Open Questions and Uncertainties
- Date representation compatibility uncertainty remains between declared DB2 date field representation and host-variable conversion path evidence. (Remaining uncertainty)
- Null retrieval behavior remains unresolved where legacy declarations allow nullable columns and explicit indicator handling is not evidenced. (Remaining uncertainty)
- Behavior for nonnumeric values when mapped into numeric-display output fields remains unresolved. (Remaining uncertainty)
- Final target representation and exposure of legacy control fields, including success and eyecatcher semantics, remains a downstream contract decision. (Remaining uncertainty)
- Any deterministic tie behavior beyond date/time ordering remains unproven. (Remaining uncertainty)

## 17. Upstream Evidence Alignment

| Intended-system statement | Supporting upstream artifact | Evidence status |
|---|---|---|
| The capability is an account transaction-list inquiry with optional date controls and pagination metadata. | supporting/program-analysis.md; supporting/business-rules.md | Confirmed upstream behavior |
| The modern system must preserve filter, ordering, pagination, count, and failure semantics defined upstream. | supporting/business-rules.md; supporting/mapping-matrix.md | Confirmed upstream behavior |
| Backend, frontend, and persistence are the intended high-level responsibility boundaries for delivery. | supporting/dependency-map.md; supporting/architecture.md (convention context) | Approved modernization direction |
| Count and retrieval are separate paths that together form the final response outcome. | supporting/program-analysis.md; supporting/business-rules.md; supporting/mapping-matrix.md | Confirmed upstream behavior |
| No partial successful output is allowed after technical retrieval failure. | supporting/business-rules.md; supporting/program-analysis.md | Confirmed upstream behavior |
| Composite transaction ID construction is deterministic, but uniqueness is not proven. | supporting/business-rules.md; supporting/mapping-matrix.md | Confirmed upstream behavior plus remaining uncertainty |
| Final contract expression for control fields and protocol outcomes is deferred to downstream artifacts. | supporting/mapping-matrix.md; supporting/business-rules.md | Approved modernization direction |
