# Dependency Map — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Classify normal runtime, failure-path, compile-time, indirect, and merely related dependencies.

## Program Boundary
Primary boundary is `INQTRAND`. INQTRANL is a neighboring transaction-list capability and is outside 005B implementation scope.

## Inbound Runtime Dependencies
- A CICS caller supplies an INQTRAND COMMAREA.
- Exact caller/routing definitions are not supplied.

## Outbound Runtime Dependencies
### Normal path
- DB2 `PROCTRAN`.
- CICS runtime services.

### Failure path
- CICS LINK to `ABNDPROC`.
- INQTRAND populates and passes error information structured by `ABNDINFO` to `ABNDPROC`.
- `ABNDPROC` writes that error information to `ABNDFILE` through CICS.
- INQTRAND can issue abend code `ITRD`.

## Database Dependencies
| Dependency | Type | Usage |
|---|---|---|
| `PROCTRAN` | DB2 runtime table | exact detail lookup |
| `PROCDB2.cpy` | SQL INCLUDE | column/host declarations |
| production DDL/index | Missing evidence | physical constraints/performance unknown |

## CICS Services
Direct `EXEC CICS` command/service dependencies used by INQTRAND:
- HANDLE ABEND
- ASSIGN
- LINK
- ABEND
- RETURN
- ASKTIME
- FORMATTIME

## CICS Runtime Context (EIB)
INQTRAND also depends on CICS-provided EIB runtime context values for diagnostics/error information (`EIBRESP`, `EIBRESP2`, `EIBTASKN`, `EIBTRNID`). These are runtime context fields, not separate `EXEC CICS` command invocations.

## Called or Linked Programs
### ABNDPROC
- Direction: INQTRAND → ABNDPROC.
- Runtime type: technical-error path only.
- Purpose: centralized abend information processing/logging.

## COPY Dependencies
- `INQTRAND.cpy`
- `SORTCODE.cpy`
- `ABNDINFO.cpy`

## SQL INCLUDE Dependencies
- `PROCDB2.cpy`
- system `SQLCA` (referenced; standalone source not supplied)

## Indirect Dependencies
`ABNDFILE` is indirect through ABNDPROC, not directly written by INQTRAND.

## Related Programs That Are Not Direct Dependencies
- `INQTRANL.cbl` / `INQTRANL.cpy`: shared transaction/list context; no INQTRAND call/link.
- `PROCTRAN.cpy`: related record structure and type/delete evidence; not copied by INQTRAND.

## Missing Runtime Definitions
- inbound CICS program/transaction resource definitions;
- production DB2 DDL/index;
- ABNDFILE resource definition;
- JCL/BMS evidence, none supplied.

## Dependency Diagram
```text
CICS caller
   |
   v
INQTRAND
   | \
   |  \ compile: INQTRAND.cpy, SORTCODE.cpy, ABNDINFO.cpy,
   |             PROCDB2.cpy, SQLCA
   |
   +---- normal ----> DB2 PROCTRAN
   |
   `---- failure ---> ABNDPROC ---> ABNDFILE

Related only: INQTRANL.cbl/.cpy, PROCTRAN.cpy
```

## Uncertainty Register
| ID | Uncertainty | Impact |
|---|---|---|
| DEP-U01 | inbound CICS routing absent | caller/routing cannot be documented exactly |
| DEP-U02 | production DDL/index absent | physical uniqueness/null/performance unknown |
| DEP-U03 | ABNDFILE definition absent | operational error persistence incomplete |
| DEP-U04 | SQLCA source absent | standard INCLUDE known only by reference |


## Artifact Relationships

- **Upstream Inputs:** `supporting/program-analysis.md` and legacy assets.
- **Downstream Consumers:** `supporting/intended-system.md`, `supporting/architecture.md`, `research.md`, `plan.md`, `supporting/modernization-report.md`.
- **Authority Boundary:** Authoritative for dependency classification/direction, not target architecture.
- **Conflict Handling:** Source COPY/INCLUDE/LINK/SQL evidence overrides this map; missing deployment evidence stays unresolved.
