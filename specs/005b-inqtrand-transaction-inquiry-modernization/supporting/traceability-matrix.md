# Traceability Matrix — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Trace source evidence and approved decisions through rules/mappings, requirements, specification, plan, tasks, OpenAPI, tests, and future implementation evidence.

## Identifier Conventions
- BR-* business rules
- MM-* mappings
- FR/NFR/SEC/OPS/COMP/MOD requirements
- BS/INV/AC specification anchors
- T* tasks
- TS-* planned test scenarios

## Source-to-Rule Traceability
| Source | Rules / decisions |
|---|---|
| `INQTRAND.cbl` | BR-001..BR-012 |
| `INQTRAND.cpy` | BR-001,BR-002,BR-004..BR-007; fixed widths |
| `PROCDB2.cpy` | BR-002,BR-007,BR-012; MM DB fields/nullability |
| `PROCTRAN.cpy` | BR-011; type/delete related evidence |
| `ABNDINFO.cpy`, `ABNDPROC.cbl` | BR-008 / technical dependency |
| `INQTRANL*` | contrast only for BR-010; shared ID/schema context |
| repository discovery | R-003..R-008, NFR/SEC/OPS integration decisions |

## Complete Requirement Chain
| Requirement | Evidence / rule | Mapping | Specification | Plan | Task | OpenAPI | Test | Implementation Evidence |
|---|---|---|---|---|---|---|---|---|
| FR-001 | BR-002; INQTRAND.cpy | MM-001..MM-005 | BS-001 | Plan Component Design | T002,T009 | path params | TS-001,TS-006..TS-011 | Pending |
| FR-002 | BR-002,BR-010 | MM-001..MM-005 | BS-002 | Persistence Strategy | T005,T006 | GET semantics | TS-001,TS-017 | Pending |
| FR-003 | BR-004 | MM-016 | BS-003 | Service/Controller | T007,T009 | 200 found | TS-001 | Pending |
| FR-004 | BR-005 | MM-016,MM-017 | BS-004 | Service/Controller | T007,T009 | 200 notFound | TS-002 | Pending |
| FR-005 | BR-007 | MM-007..MM-015 | BS-003 | Mapper/domain | T002,T003 | TransactionDetail | TS-004 | Pending |
| FR-006 | BR-006 | MM-015 | BS-003/INV-004 | Mapper/domain | T003,T007 | transactionId schema | TS-003 | Pending |
| FR-007 | Approved modern structural-validation boundary; BR-003 legacy date reshaping evidence only | MM-003,MM-009 legacy date representation/reshaping context | Validation Rules; AC-007 | Validation Strategy | T002,T009,T010 | path regexes + operation validation-boundary description | TS-006..TS-012,TS-012a,TS-012b,TS-013 | Pending |
| FR-008 | BR-009 | — | BS-006/INV-006 | Persistence Strategy | T005 | GET | TS-005 | Pending |
| FR-009 | BR-011 | MM-006 | BS-002 | Persistence Strategy | T005,T006 | — | TS-018 | Pending |
| FR-010 | BR-012 | MM-014 | BS-005/Edge 4 | Mapping/Persistence | T003,T004,T005 | 500 schema | TS-015,TS-016 | Pending |
| FR-011 | INQTRAND.cpy; BR-002 | MM-001..MM-005 | Validation/Edge 2 | Validation Strategy | T002,T009 | path regexes | TS-013 | Pending |
| FR-012 | Approved R-001 | MM-001..MM-005 | API Behavior | API Strategy | T009,T012 | approved path | TS-019 | Pending |
| FR-013 | Approved R-008 | MM-001..MM-016 | US-004; AC-013 | Frontend Strategy | T014..T018 | response schemas | TS-020..TS-022,TS-021a | Pending |
| NFR-001 | Repository discovery | — | Scope/Invariants | Placement Strategy | T001,T002,T014,T016 | — | regression | Pending |
| NFR-002 | INQTRAND/PROCDB2 amount | MM-014 | Field rules | Mapping Strategy | T003 | amount:number | TS-004,TS-016 | Pending |
| NFR-003 | Repository discovery JDBC | MM-001..MM-005 | BS-002 | Persistence Strategy | T005 | — | TS-017 | Pending |
| NFR-004 | Repository test conventions | — | AC-016 | Testing Strategy | T006,T008,T010,T011,T013,T015,T018,T019 | — | TS-001..TS-022,TS-012a,TS-012b,TS-021a | Pending |
| NFR-005 | Repository correlation convention | — | Error Responses | Observability | T010,T019 | ErrorResponse | TS-015 | Pending |
| SEC-001 | Repository security matcher | — | Security | Security Strategy | T011 | bearerAuth | security scenarios | Pending |
| SEC-002 | Repository role evidence | — | Security | Security Strategy | T011 | bearerAuth | security scenarios | Pending |
| SEC-003 | Repository auth gap | — | US-004 | Frontend/Security | T001,T014 | bearerAuth | TS-020,TS-022 | Pending |
| OPS-001 | Repository validation convention | — | Error Responses | Error Strategy | T009,T010 | 400/ErrorResponse | TS-006..TS-012 | Pending |
| OPS-002 | BR-008; repository ERR-500 | — | BS-005; AC-017 | Error Strategy | T007,T010 | 500/ErrorResponse | TS-015,TS-016 | Pending |
| OPS-003 | Repository OpenAPI ambiguity | — | AC-015 | Runtime OpenAPI | T012,T013 | entire contract | TS-019 | Pending |
| OPS-004 | Repository H2 convention | — | Scope | Persistence Strategy | T004,T005 | — | repository tests | Pending |
| COMP-001 | BR-002 | MM-001..MM-005 | BS-001/BS-002 | Persistence | T005 | path | TS-001,TS-017 | Pending |
| COMP-002 | BR-005 | MM-016,MM-017 | BS-004 | Service | T007,T009 | 200 notFound | TS-002 | Pending |
| COMP-003 | BR-006 | MM-015 | INV-004 | Mapper | T003 | transactionId | TS-003 | Pending |
| COMP-004 | BR-010 | — | INV-005 | Persistence/API | T005,T009 | no list params | TS-017,TS-019 | Pending |
| COMP-005 | BR-011,BR-012 | MM-006,MM-014 | Edge Cases | Persistence/Mapping | T003,T005,T006 | technical error | TS-016,TS-018 | Pending |
| MOD-001 | Intended System | MM-016,MM-017 | API Behavior | API Strategy | T009 | REST/JSON | TS-019 | Pending |
| MOD-002 | R-001 | MM-001..MM-005 | BS-001 | API Strategy | T009,T012 | path | TS-019 | Pending |
| MOD-003 | R-002 | MM-016 | BS-003/BS-004 | Service | T002,T007 | response envelope | TS-001,TS-002 | Pending |
| MOD-004 | Repository discovery; R-004/R-008 | — | US-004; AC-013 | Placement/Frontend | T001,T014,T016,T017 | — | TS-020..TS-022,TS-021a | Pending |
| MOD-005 | Repository security/correlation | — | Security/Error | Security/Observability | T010,T011 | bearer/Error | security + TS-015 | Pending |
| MOD-006 | OpenAPI ambiguity; R-001 | — | AC-015 | Runtime OpenAPI | T012,T013 | feature contract | TS-019 | Pending |

## Frontend Traceability
FR-013 / MOD-004 link existing `frontend/app/src/features/transactionInquiry/`, `transactionInquiryClient.ts`, `transactionTypes.ts`, and `App.tsx` through T014-T018 and TS-020..TS-022,TS-021a. Required list-to-detail integration is verified and INQTRANL list behavior remains unchanged. No new frontend root is authorized.

## Implementation Evidence Columns
All implementation evidence is **Pending** because this package is pre-implementation. Populate with commit/file/test references only after executable work exists.

## Coverage Summary
- Requirements traced: all FR-001..FR-013, NFR-001..NFR-005, SEC-001..SEC-003, OPS-001..OPS-004, COMP-001..COMP-005, MOD-001..MOD-006.
- Legacy rule identifiers represented/accounted for in traceability: BR-001..BR-012 (including legacy-only/non-exposed mechanics where applicable).
- Mapping identifiers represented/accounted for in traceability: MM-001..MM-017 (including preserved semantics and non-exposed mappings where applicable).
- Planned tests: TS-001..TS-022, TS-012a, TS-012b, TS-021a.
- Implementation evidence: 0 complete / pending by design.

## Gaps and Exceptions
- BR-001 COMMAREA eyecatcher normalization has no direct REST field; it is retained as legacy analysis but intentionally not exposed by MOD-001.
- Inbound CICS routing, production DDL/indexes, ABNDFILE definition, exact current runtime commands/ports and frontend auth wiring remain unresolved evidence gaps.
- `dual-mode-analysis.md` is intentionally absent because this is single-model generation.


## Artifact Relationships

- **Upstream Inputs:**
	- Confirmed legacy facts: supplied legacy evidence plus approved legacy-analysis artifacts (`supporting/program-analysis.md`, `supporting/dependency-map.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`).
	- Approved observable behavior: `supporting/requirements.md`, `spec.md`.
	- Implementation architecture/strategy: `supporting/architecture.md`, `plan.md`.
	- Operational work planning: `tasks.md`.
	- External REST contract: `contracts/openapi.yaml`.
	- Planned verification authority: `supporting/test-spec.md`.
	- Repository evidence: current workspace repository artifacts where relevant.
- **Downstream Consumers:** `checklists/code-review-checklist.md`, `checklists/qa-review-checklist.md`, implementation reporting, `supporting/copilot-build-prompt.md`.
- **Authority Boundary:** Authoritative for cross-artifact trace relationships and coverage status, not for redefining requirements.
- **Conflict Handling:** The matrix connects authoritative sources but does not redefine them; stale/incorrect identifiers must be corrected from their owning authority. Implementation evidence remains pending until actually executed.
