# Copilot Build Prompt — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Provide an implementation agent with authoritative instructions derived from the approved Feature 005B package. This prompt cannot add or override requirements.

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
17. `checklists/code-review-checklist.md`
18. `checklists/qa-review-checklist.md`
19. `quickstart.md`
20. `supporting/modernization-report.md`

Apply authority boundaries while implementing:
- legacy-analysis artifacts establish confirmed legacy evidence;
- `supporting/requirements.md` and `spec.md` establish approved observable behavior;
- `supporting/architecture.md` and `plan.md` establish implementation architecture/strategy;
- `tasks.md` establishes approved implementation work sequencing and done criteria;
- `contracts/openapi.yaml` encodes the approved external REST contract;
- `supporting/test-spec.md` establishes planned verification;
- `supporting/traceability-matrix.md` connects package coverage and implementation evidence tracking;
- code-review and QA checklists define completion gates after implementation evidence exists;
- `quickstart.md` owns verified operational instructions only;
- `supporting/modernization-report.md` is client-facing synthesis only;
- this Build Prompt instructs implementation and cannot redefine any upstream authority.

If approved artifacts contradict one another, **stop and report the contradiction**. Do not choose a convenient interpretation.

## Scope
Implement only 005B INQTRAND transaction detail. Do not re-modernize INQTRANL and do not add transaction mutation/general management.

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
- actual technical/persistence failure returns HTTP 500 with ERR-500 and correlationId;
- normal absence remains HTTP 200 with found=false and transaction=null (never 404/500 for absence);
- five identity components are strings with exact digit constraints: sortCode 6, accountNumber 8, date 8, time 6, reference 12;
- preserve leading zeroes and do not numerically coerce identity components;
- malformed structural input returns 400 ERR-001;
- do not introduce Gregorian/calendar semantic validation, HHMMSS semantic clock-range validation, account-existence validation, or transaction-reference business validation;
- structurally valid unusual values proceed through normal lookup behavior.

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

Before frontend edits, inspect current frontend repository structure and applicable frozen `frontend-modernization` architecture/specification artifacts. Determine which approved 005B behavior belongs in existing transaction feature files versus shared frontend integration points, then implement at the correct integration boundary.

Use these frontend integration rules:
- reuse existing shared infrastructure when appropriate;
- modify shared frontend files when genuinely required to integrate approved 005B behavior;
- do not create duplicate feature-specific infrastructure merely to avoid updating an appropriate shared component;
- do not create a second frontend root/application;
- do not restructure unrelated shared frontend code;
- preserve existing INQTRANL behavior.

Do not pre-decide that all changes belong only under `features/transactionInquiry`. Do not pre-decide that shared files must change either. Repository inspection plus applicable frozen frontend-modernization architecture determines the correct integration point.

If current repository structure materially conflicts with frozen 005B or applicable frontend-modernization architecture, stop and report the conflict before implementing.

Do not invent package paths that repository inspection disproves.

## Implementation Sequence
Execute T001 through T020 in `tasks.md` dependency order. Do not skip T004 schema/nullability inspection or T012 runtime OpenAPI reconciliation.

## Persistence Guardrails
- Prepared exact five-key query.
- No list count/order/pagination/range SQL.
- No extra eyecatcher/delete/type predicate.
- No null amount→zero/detail text→blank fallback unless upstream artifacts are formally amended.
- Maintain exact decimal precision.
- Expose the approved zero-or-one modern result abstraction without claiming supplied evidence proves production DB2 physical uniqueness.
- Do not add arbitrary ordering, silently select a first duplicate, or invent duplicate-resolution semantics.
- If implementation/repository testing reveals duplicate physical matches are possible, stop the affected implementation decision and report a data/integration issue requiring explicit resolution.
- Do not hide duplicate risk via ORDER BY, LIMIT, first-row selection, or equivalent behavior.

## Frontend Guardrails
- Extend current transaction UI/client/types and integrate through the correct feature-specific/shared frontend boundary established by repository inspection and applicable frozen frontend-modernization architecture.
- Required minimal integration is mandatory: existing INQTRANL transaction list row -> five decomposed identity components -> detail navigation -> existing transaction API client -> approved INQTRAND detail endpoint -> found-detail or successful-absence presentation.
- Do not hard-code auth token.
- Preserve INQTRANL list behavior.
- Render found, not-found, loading and technical error states.
- Do not invent unrelated frontend functionality or a second frontend application.

## Testing Obligations
Implement/execute approved scenarios TS-001..TS-022, TS-012a, TS-012b, and TS-021a as applicable. Specifically prove:
- exact five-key found behavior;
- successful no-row is 200;
- normal absence is not 404/500;
- transactionId is exact 44-character `sortCode-accountNumber-date-time-reference`;
- exact structural validation boundary for 6/8/8/6/12 string identity components;
- no invented date/time semantics;
- no account-existence validation;
- no transaction-reference business validation;
- leading zeros survive;
- exact SQL predicates/no list semantics;
- no hidden validity filter;
- no unsupported null default;
- physical-uniqueness/duplicate-selection guardrail is preserved;
- 401/403/authorized behavior;
- runtime OpenAPI conformance;
- required list-to-detail frontend integration;
- frontend found/not-found;
- INQTRANL regression.
- E2E evidence exists or explicit BLOCKED status is recorded with evidence.

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

- **Upstream Inputs:** authority chain listed above (`supporting/program-analysis.md` through `supporting/modernization-report.md`) plus current repository evidence and applicable frozen `frontend-modernization` architecture/specification artifacts for shared-vs-feature integration boundaries.
- **Downstream Consumers:** GitHub Copilot or another coding agent; code review/QA completion workflow.
- **Authority Boundary:** Authoritative for implementation-agent instructions only; it may not override approved upstream artifacts.
- **Conflict Handling:** On contradiction, stop and report; never silently select, merge, or invent requirements.
