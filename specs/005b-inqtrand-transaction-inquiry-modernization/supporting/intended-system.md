# Intended System — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Bridge verified INQTRAND behavior to the approved future-state capability.

## Current Capability Summary
Legacy INQTRAND performs one read-only five-part lookup and returns either detail or successful absence; technical DB2 failures are separate.

## Modernization Objectives
- expose detail through the existing modern application;
- preserve five-part identity and zero-or-one semantics;
- preserve successful not-found;
- reuse established transaction/persistence/security/correlation/testing/frontend foundations where compatible;
- avoid a parallel transaction subsystem.

## Behaviors to Preserve
Preserve observable legacy/business semantics:
- exact five-part equality lookup semantics;
- found (`SUCCESS=Y`, `FOUND=Y`) versus successful absence (`SUCCESS=Y`, `FOUND=N`) semantics;
- composite transaction identity construction semantics;
- read-only behavior;
- zero-or-one detail semantics and absence of list/pagination/count/range behavior;
- absence of hidden record-validity/type filtering unless separately approved;
- legacy non-establishment of null-to-zero or other defaulting behavior.

Treat legacy implementation mechanics as evidence/context, not mandatory target mechanics:
- CICS/DB2 abend implementation details do not need literal reproduction;
- lack of SQL indicator variables in legacy is not itself a target requirement.

Target failures must preserve the business distinction between normal found/not-found outcomes and technical failure outcomes, without reproducing CICS-specific implementation internals.

## Approved Modernization Changes
1. Represent the CICS COMMAREA operation as REST/JSON.
2. Authoritative detail path:
   `/api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`.
   Rationale: legacy INQTRAND lookup contract is directly expressed by five identity components (sort code, account number, date, time, reference). This path maps those confirmed components one-to-one and avoids introducing a required inbound composite-transactionId parsing contract not established by legacy INQTRAND. Historical broader repository API definitions are treated as signals, not automatically authoritative runtime contracts.
3. Resolve MM-016 wire representation decision as an Approved Modernization Decision:
   found response uses `{ "found": true, "transaction": {...} }` and successful absence uses `{ "found": false, "transaction": null }`.
   This `found` JSON field is a modern representation choice and not a legacy COMMAREA field.
4. Resolve MM-017 transport decision as an Approved Modernization Decision:
   successful absence is represented as HTTP 200 with `{ "found": false, "transaction": null }`.
   Rationale: legacy SQLCODE 100 yields `SUCCESS=Y` and `FOUND=N`, so absence is a normal successful business outcome; technical/data-access faults remain separate error outcomes.
5. Validate exact digit widths at the transport boundary as an Approved Modernization Decision:
   - sortCode: exactly 6 digits
   - accountNumber: exactly 8 digits
   - date: exactly 8 digits
   - time: exactly 6 digits
   - reference: exactly 12 digits
   This does not introduce calendar-date validity, HHMMSS semantic range validation, account-existence prechecks, or additional transaction-reference business validation.
6. Extend existing `inqtran` vertical slice and H2/JDBC persistence.
7. Reuse account-inquiry security and correlation conventions.
8. Integrate detail into the existing transaction frontend with explicit list-row to detail navigation using the five identity components already present in transaction-list rows (or decomposed from list context where needed). This is the approved minimal frontend integration for this feature and does not re-modernize INQTRANL behavior beyond navigation.

All are **Approved Modernization Decisions**, not legacy facts.

## System Boundary
### In scope
Detail API/backend lookup, feature OpenAPI, minimal detail UI integration, tests, runtime contract reconciliation.

### Out of scope
INQTRANL re-modernization, transaction mutation, list behavior changes, real mainframe connectivity, general transaction management.

## User and System Interactions
A permitted account-inquiry user provides/selects all five transaction key components. The application shows detail or a normal not-found state; data-access faults are technical errors.

## Target Operating Concept
Extend the existing Spring Boot + React modernization app. POC persistence remains H2/DB2 mode behind JDBC/repository boundaries suitable for later adapter substitution.

## Legacy-to-Modern Transition Narrative
CICS protocol flags become HTTP/JSON semantics while the business outcome remains: one exact identity → found detail or successful absence.

## Assumptions and Open Decisions
- inspect current H2 schema/nullability/seeds before coding;
- frontend bearer-token wiring remains unresolved and no hard-coded token is approved;
- production DB2 adapter is future scope;
- exact current run commands/ports must be verified from repository files before Quickstart execution.


## Artifact Relationships

- **Upstream Inputs:** `supporting/program-analysis.md`, `supporting/dependency-map.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`.
- **Downstream Consumers:** `supporting/architecture.md`, `research.md`, `data-model.md`, `supporting/requirements.md`, `spec.md`, `supporting/modernization-report.md`.
- **Authority Boundary:** Authoritative for approved future-state capability and scope, not low-level implementation.
- **Conflict Handling:** Confirmed legacy outcomes have precedence; repository conventions shape integration but cannot erase them.
