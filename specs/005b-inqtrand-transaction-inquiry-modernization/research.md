# Research — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Record approved modernization decisions, alternatives, rationale, constraints, and consequences.

## Decision Summary
| ID | Choice | Classification |
|---|---|---|
| R-001 | five explicit key path segments | Approved Modernization Decision |
| R-002 | 200 + `found:false` for absence | Approved Modernization Decision preserving BR-005 |
| R-003 | extend existing `TransactionRepository` / `JdbcTransactionRepository` boundary in `inqtran` | Approved Modernization Decision |
| R-004 | extend existing `inqtran` vertical slice | Approved Modernization Decision |
| R-005 | fixed-width digit strings externally | Approved Modernization Decision |
| R-006 | preserve opaque 3-char type as opaque modern string (no invented enum semantics) | Approved Modernization Decision preserving legacy fact |
| R-007 | do not introduce silent detail null defaults or inherit INQTRANL list null-defaulting | Approved Modernization Decision constrained by legacy evidence |
| R-008 | extend existing transaction frontend | Approved Modernization Decision |

## Repository Findings
Current INQTRANL supplies reusable package structure, H2 PROCTRAN, JDBC patterns, mapper/domain foundations, security, correlation handling, tests, frontend organization and contract-conformance style. Reuse is permitted only where it does not import list semantics.

## Persistence Decision
Use existing POC H2/DB2-mode PROCTRAN through JDBC; add exact detail lookup by extending the existing `TransactionRepository` / `JdbcTransactionRepository` boundary within `inqtran`. No JPA and no new transaction store.

Rejected alternative:
- separate/parallel transaction-detail repository hierarchy: rejected because existing transaction persistence boundary already owns PROCTRAN and approved architecture extends rather than forks the transaction subsystem.

## API Design Decisions
### R-001 — explicit decomposed identity
Use `/api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`.

Rationale: directly represents legacy identity, avoids unapproved composite-ID parsing, and remains inside existing secured account path.

### Rejected alternatives
- historical broad `/accounts/{accountId}/transactions/{transactionId}`: not runtime-authoritative and not derived from frozen evidence;
- composite ID only: adds parsing/identity ambiguity;
- both: unnecessary contract expansion.

## Empty-Result Decision
Use 200 with `found` boolean and nullable transaction. 404 is rejected because SQLCODE 100 is explicitly successful in legacy behavior.

## Validation Decisions
Exact digit widths: 6/8/8/6/12. No calendar-day or clock-range semantic validation is introduced.

## Mapping Decisions
Preserve leading zeros, 8-digit date, exact decimal precision, exact composite ID, and no silent detail null defaults.

R-006 classification note:
- Confirmed Legacy Evidence: INQTRAND treats transaction type as opaque 3-character data.
- Approved Modernization Decision: preserve that as an opaque string representation and avoid introducing a closed enum or additional type semantics not established by legacy evidence.

R-007 classification note:
- Confirmed Legacy Evidence: INQTRAND fetches nullable selected DB2 columns without indicator variables; legacy evidence does not establish null->zero or other null-default rules.
- Approved Modernization Decision: do not introduce silent null->zero/default behavior for detail and do not automatically inherit INQTRANL list-specific null-defaulting.
- Constraint: this does not require reproducing legacy absence of SQL indicator variables as a target implementation mechanism.

## Frontend Decisions
Extend existing transaction feature. Approved minimal integration is list-row to detail navigation using the five transaction identity components, without re-modernizing INQTRANL list behavior. Frontend auth wiring remains unresolved; no hard-coded token.

## Testing Decisions
Use established service, H2 repository, MockMvc, security, OpenAPI conformance, frontend, and Playwright layers. Explicitly test successful absence, leading zeros, exact ID, no list query params, and null-default protection.

## Alternatives Considered / Rejected
- parallel `inqtrand` package: rejected;
- new table: rejected;
- JPA: rejected;
- 404 absence: rejected;
- closed type enum: rejected;
- eyecatcher/delete/type filter: rejected.

## Risks and Consequences
Runtime/broad OpenAPI ambiguity; incompatible list null default; frontend auth gap; seed coverage uncertainty; unverified exact commands/ports.

## Open Questions
1. Approved frontend credential/token source?
2. Exact current H2 nullable columns and seed coverage?
3. Exact current backend/frontend commands and ports?
4. Production DB2 constraints beyond supplied declarations?


## Artifact Relationships

- **Upstream Inputs:** `supporting/intended-system.md`, `supporting/architecture.md`, `supporting/program-analysis.md`, `supporting/mapping-matrix.md`, repository discovery.
- **Downstream Consumers:** `data-model.md`, `supporting/requirements.md`, `spec.md`, `plan.md`, `tasks.md`, `supporting/copilot-build-prompt.md`.
- **Authority Boundary:** Authoritative for modernization decisions/rationale, not legacy facts.
- **Conflict Handling:** Revisit a decision if it conflicts with preserved behavior or later repository inspection; unresolved questions remain unresolved.
