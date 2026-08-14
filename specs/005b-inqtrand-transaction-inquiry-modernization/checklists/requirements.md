# Requirements Checklist — 00B INQTRAND Transaction Detail Inquiry

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
- [x] Found, not-found, field mapping, ID, date representation, width, UI and read-only behavior are covered.
- [x] INQTRANL list semantics are explicitly out of scope.
- [x] No hidden eyecatcher/delete/type filter is introduced.

## Non-Functional Coverage
- [x] Repository reuse, decimal precision, prepared SQL, testing and correlation requirements are defined.

## Security Coverage
- [x] Existing secured path/role is identified.
- [x] No separate/hard-coded authentication mechanism is approved.

## Operational Coverage
- [x] 400/500 conventions and correlation requirements are defined.
- [x] Runtime OpenAPI reconciliation is required.
- [x] H2 POC boundary is explicit.

## Specification Alignment
- [x] `spec.md` implements all requirement families without adding list behavior.
- [x] Empty-result and technical-failure semantics are distinct.

## OpenAPI Readiness
- [x] Feature OpenAPI reflects the approved path, schemas, required path constraints and response statuses.
- [x] Successful not-found is represented by the 200 response schema.

## Traceability Readiness
- [x] BR, MM, requirement, specification, task, OpenAPI and TS identifiers are represented in the traceability matrix.
- [ ] Implementation evidence is complete. **Not applicable pre-implementation; remains pending.**

## Assumptions and Risks
- [x] Missing production DDL/CICS definitions/auth wiring/runtime command evidence is explicitly visible.
- [x] No unresolved gap has been silently promoted to confirmed behavior.

## Final Gate
**PASS FOR IMPLEMENTATION PLANNING.** Documentation is internally aligned for implementation. Runtime/code-specific uncertainties are assigned to T001/T004 and must be resolved before affected code is frozen.


## Artifact Relationships

- **Upstream Inputs:** `supporting/requirements.md`, `spec.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `contracts/openapi.yaml`, `supporting/traceability-matrix.md`.
- **Downstream Consumers:** `supporting/copilot-build-prompt.md`, implementation start gate.
- **Authority Boundary:** Authoritative for pre-implementation documentation readiness only.
- **Conflict Handling:** Unchecked implementation evidence cannot be promoted to pass; any upstream documentation change requires re-running this checklist.
