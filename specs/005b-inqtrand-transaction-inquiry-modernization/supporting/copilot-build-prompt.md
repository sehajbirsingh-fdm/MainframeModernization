# Copilot Build Prompt — 00B INQTRAND Transaction Detail Inquiry

## Purpose
Provide an implementation agent with authoritative instructions derived from the approved Feature 00B package. This prompt cannot add or override requirements.

## Authority and Precedence
Before coding, read in this order:
1. `supporting/program-analysis.md`
2. `supporting/dependency-map.md`
3. `supporting/business-rules.md`
4. `supporting/mapping-matrix.md`
5. `supporting/intended-system.md`
6. `supporting/architecture.md`
7. `research.md`
8. `data-model.md`
9. `supporting/requirements.md`
10. `spec.md`
11. `plan.md`
12. `tasks.md`
13. `contracts/openapi.yaml`
14. `supporting/test-spec.md`
15. `supporting/traceability-matrix.md`
16. `checklists/requirements.md`

If approved artifacts contradict one another, **stop and report the contradiction**. Do not choose a convenient interpretation.

## Scope
Implement only 00B INQTRAND transaction detail. Do not re-modernize INQTRANL and do not add transaction mutation/general management.

## Non-Negotiable Preserved Behavior
- exact five-part key;
- zero-or-one read-only lookup;
- found success;
- no-row is successful absence;
- composite ID exact hyphenation;
- no date-range/count/order/pagination/limit-offset semantics;
- no hidden record-eyecatcher/logical-delete/type filter;
- no unsupported detail null default.

## Approved Modern Contract
Implement:
`GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`

- 200 found true + transaction;
- 200 found false + null transaction;
- 400 ERR-001 for path syntax/shape;
- 401/403 through existing security;
- 500 ERR-500 for technical failure;
- five key segments preserve digit widths/leading zeros;
- no semantic calendar/clock rule beyond approved shape validation.

## Repository-First Constraints
At T001 re-inspect current repository. Prefer extending:
- `backend/api/src/main/java/com/bankofz/mainframemodernization/inqtran/`
- current transaction repository/JDBC/service/controller/mapper/domain/error patterns;
- `backend/api/src/main/resources/schema.sql`, `data.sql`, `openapi.yaml`;
- current inqtran backend tests;
- `frontend/app/src/features/transactionInquiry/`;
- `frontend/app/src/api/transactionInquiryClient.ts`;
- `frontend/app/src/domain/transactionTypes.ts`;
- `frontend/app/src/App.tsx`;
- current frontend/e2e tests.

Do not invent package paths that repository inspection disproves.

## Implementation Sequence
Execute T001 through T020 in `tasks.md` dependency order. Do not skip T004 schema/nullability inspection or T012 runtime OpenAPI reconciliation.

## Persistence Guardrails
- Prepared exact five-key query.
- No list count/order/pagination/range SQL.
- No extra eyecatcher/delete/type predicate.
- No null amount→zero/detail text→blank fallback unless upstream artifacts are formally amended.
- Maintain exact decimal precision.

## Frontend Guardrails
- Extend current transaction UI/client/types.
- Do not hard-code auth token.
- Preserve INQTRANL list behavior.
- Render found, not-found, loading and technical error states.

## Testing Obligations
Implement/execute TS-001..TS-022 as applicable. Specifically prove:
- successful no-row is 200;
- leading zeros survive;
- transactionId exact;
- exact SQL predicates/no list semantics;
- no hidden validity filter;
- no unsupported null default;
- 401/403/authorized behavior;
- runtime OpenAPI conformance;
- frontend found/not-found;
- INQTRANL regression.

If E2E is blocked by existing auth configuration, report BLOCKED with evidence instead of faking success.

## Stop Conditions
Stop and report before proceeding if:
- current repository conflicts materially with the approved architecture/contract;
- H2 schema makes a preserved behavior unsafe/ambiguous;
- an upstream artifact contradiction appears;
- implementing the feature would require a new business rule;
- auth cannot be integrated without inventing/hard-coding credentials.

## Completion Rules
Do not declare complete until:
- implementation tasks are done;
- tests have executed with evidence;
- runtime OpenAPI matches feature contract;
- `supporting/traceability-matrix.md` implementation evidence is populated;
- `checklists/code-review-checklist.md` is reviewed with evidence;
- `checklists/qa-review-checklist.md` has an executed decision;
- `quickstart.md` contains only verified current commands/ports;
- INQTRANL regression is accounted for.

## Required Completion Report
Report:
- files changed;
- task IDs completed;
- tests/commands and results;
- unresolved blockers/defects;
- traceability updates;
- code-review/QA status;
- confirmation that no unrelated scope was added.


## Artifact Relationships

- **Upstream Inputs:** `supporting/program-analysis.md` through `checklists/requirements.md` as listed above.
- **Downstream Consumers:** GitHub Copilot or another coding agent; code review/QA completion workflow.
- **Authority Boundary:** Authoritative for implementation-agent instructions only; it may not override approved upstream artifacts.
- **Conflict Handling:** On contradiction, stop and report; never silently select, merge, or invent requirements.
