# Program Analysis — 005B INQTRAND Transaction Detail Inquiry

## Document Purpose
Establish the authoritative technical understanding of the supplied legacy `INQTRAND` capability. This artifact describes legacy execution only; REST, Java, frontend, and modernization choices are excluded.

## Program Identity
- **Program:** `INQTRAND`
- **Capability:** single transaction detail inquiry.
- **Execution:** CICS COBOL with embedded DB2 SQL and COMMAREA.
- **Primary source:** supplied `INQTRAND.cbl`.
- **Interface:** `INQTRAND.cpy`.
- **DB2 declaration:** `PROCDB2.cpy`.
- **Technical error structure:** `ABNDINFO.cpy`.
- **Other compile-time evidence:** `SORTCODE.cpy` and system `SQLCA`.

## Business Capability
`INQTRAND` accepts a five-part transaction identity—sort code, account number, transaction date, transaction time, and transaction reference—and evaluates those five components as equality predicates in the DB2 cursor. The program performs one FETCH and therefore consumes at most one matching `PROCTRAN` row. A found row is returned as detail. A missing row is explicitly successful with FOUND=`N`. DB2 technical failures follow the abend path.

## Repository-Relative Legacy Locations Established by Discovery
- `legacy-bankofz/base/cics/cobol/INQTRAND.cbl`
- `legacy-bankofz/base/cics/copy/INQTRAND.cpy`
- `legacy-bankofz/base/cics/copy/PROCDB2.cpy`
- `legacy-bankofz/base/cics/copy/PROCTRAN.cpy`
- `legacy-bankofz/base/cics/cobol/INQTRANL.cbl`
- `legacy-bankofz/base/cics/copy/INQTRANL.cpy`

`ABNDPROC.cbl`, `ABNDINFO.cpy`, and `SORTCODE.cpy` were supplied in the current evidence ZIP; the repository discovery report did not establish exact repository-relative paths for every one of those uploaded copies, so no path is invented.

## Evidence Reviewed
| Source | Role | Classification |
|---|---|---|
| `INQTRAND.cbl` | Primary control-flow authority | Confirmed Legacy Evidence |
| `INQTRAND.cpy` | COMMAREA layout | Confirmed Legacy Evidence |
| `PROCDB2.cpy` | DB2 `PROCTRAN` declarations | Confirmed Legacy Evidence |
| `PROCTRAN.cpy` | Related record/type/delete structure | Confirmed Legacy Evidence, related |
| `ABNDINFO.cpy` | Error information layout | Confirmed Legacy Evidence |
| `ABNDPROC.cbl` | Runtime error-path program | Confirmed Legacy Evidence |
| `SORTCODE.cpy` | Compile-time constant | Confirmed Legacy Evidence |
| `INQTRANL.cbl`, `INQTRANL.cpy` | Related list capability only | Confirmed Legacy Evidence, related |

## Inputs and Outputs
### Input COMMAREA
| Field | PIC | Meaning |
|---|---:|---|
| `INQTRAND-SORTCODE` | `9(6)` | Sort code |
| `INQTRAND-ACCNO` | `9(8)` | Account number |
| `INQTRAND-DATE` | `9(8)` | Date in `YYYYMMDD` representation |
| `INQTRAND-TIME` | `9(6)` | Time |
| `INQTRAND-REF` | `9(12)` | Reference |

### Output/control
- `INQTRAND-SUCCESS`: initialized `N`; becomes `Y` for both found and not-found.
- `INQTRAND-FOUND`: initialized `N`; becomes `Y` only for a fetched row.
- Found detail: transaction ID, sort code, account number, date, time, reference, type, description, amount.

## Program Structure and Paragraph-Level Control Flow
1. Establish CICS abend handling.
2. If COMMAREA eyecatcher is not `ITRD`, overwrite only `INQTRAND-EYE` with `ITRD`; this is normalization, not rejection.
3. Initialize SUCCESS=`N`, FOUND=`N`.
4. Move the five key components to host variables; rearrange date characters into DB2 date form.
5. OPEN the cursor.
6. FETCH once.
7. Interpret SQLCODE and map data or absence.
8. CLOSE the cursor.
9. CICS RETURN.
10. Technical SQL failures route through `ABEND-ROUTINE`; unexpected CICS abends use `ABEND-HANDLING`.

## SQL Processing
The cursor selects `PROCTRAN_EYECATCHER`, `PROCTRAN_SORTCODE`, `PROCTRAN_NUMBER`, `PROCTRAN_DATE`, `PROCTRAN_TIME`, `PROCTRAN_REF`, `PROCTRAN_TYPE`, `PROCTRAN_DESC`, and `PROCTRAN_AMOUNT` from `PROCTRAN`.

Every key predicate is equality:
- sort code;
- account number;
- date;
- time;
- reference.

The cursor is `FOR FETCH ONLY`. INQTRAND contains no date-range predicate, count query, ordering clause, pagination, limit, offset, or result array.

## Cursor Lifecycle
- **OPEN:** any non-zero SQLCODE → technical failure.
- **FETCH:** exactly one fetch.
  - SQLCODE 0 → found.
  - SQLCODE 100 → successful absence.
  - other → technical failure.
- **CLOSE:** any non-zero SQLCODE → technical failure.

## Data Transformations and Normalization
- Sort code, account number, time, and reference are moved without semantic transformation.
- Date is character-rearranged from `YYYYMMDD` to `YYYY-MM-DD` before SQL and back after fetch.
- There is no calendar-validity branch.
- Found transaction ID is `sortCode-accountNumber-date-time-reference`.
- Component lengths produce 44 material characters in an output `PIC X(50)` field.

## Found and Not-Found Semantics
### Found
SQLCODE 0 sets FOUND=`Y`, maps the row, builds the ID, and sets SUCCESS=`Y`.

### Not found
SQLCODE 100 leaves FOUND=`N`, sets SUCCESS=`Y`, and is explicitly commented as “not an error.” This is authoritative.

INQTRAND initializes only the two status flags before DB2 lookup (`SUCCESS='N'`, `FOUND='N'`). Source evidence does not show full output-area initialization before the FETCH path. On SQLCODE 100, the program does not explicitly clear or repopulate transaction-detail output fields. Therefore, supplied legacy evidence does not establish those detail fields as blank, zero, null, or otherwise reset for not-found outcomes.

## Nullability and Indicator Handling
`PROCDB2.cpy` permits null for several selected columns. INQTRAND supplies **no null indicator variables** in its FETCH. Therefore:
- source evidence does not establish null amount → zero;
- source evidence does not establish null text → blank;
- a matched row containing a nullable selected value may lead to a DB2 null-without-indicator SQL failure;
- modern null defaulting cannot be labeled legacy parity.

## Eyecatcher, Logical Delete, and Type
`PROCTRAN.cpy` defines a record eyecatcher, a logical-delete redefine, and type 88-level values. INQTRAND SQL does not predicate on any of those values and does not reject an unknown type after fetch. They are related structural evidence, not authorization for hidden filters.

## Success Conditions
| Condition | SUCCESS | FOUND | Outcome |
|---|---|---|---|
| Row fetched | Y | Y | Detail |
| SQLCODE 100 | Y | N | Successful absence |
| DB2 open/fetch/close error | no normal success | no normal success | Technical error path |

## Failure and Abend Handling
`ABEND-ROUTINE` populates `ABNDINFO` with CICS response data, application/task/transaction context, time/date, program and SQLCODE, sets abend code `ITRD`, links `ABNDPROC`, then issues `CICS ABEND ABCODE('ITRD') CANCEL NODUMP`.

The general `ABEND-HANDLING` path similarly builds error information, links `ABNDPROC`, and returns. `ABNDPROC` writes the error information to `ABNDFILE` through CICS.

## Runtime Dependencies
- CICS runtime services.
- DB2 table `PROCTRAN`.
- Error-path `ABNDPROC`.
- Indirect error-path `ABNDFILE` through ABNDPROC.

## Compile-Time Dependencies
- `COPY INQTRAND`
- `COPY SORTCODE`
- `EXEC SQL INCLUDE PROCDB2`
- `EXEC SQL INCLUDE SQLCA` (system source not supplied)
- `COPY ABNDINFO`

`PROCTRAN.cpy` and INQTRANL sources are related, not direct COPY/INCLUDE dependencies of INQTRAND.

## Control-Flow Diagram
```text
CICS caller
  |
  v
INQTRAND COMMAREA
  +--> normalize eyecatcher
  +--> SUCCESS=N / FOUND=N
  |
  v
move 5-part key + reshape date
  |
OPEN ---- SQL error ----------------------+
  |                                       |
FETCH once                                |
  |-- 0 --> map row, FOUND=Y, SUCCESS=Y   |
  |-- 100 -> FOUND=N, SUCCESS=Y            |
  `-- other ------------------------------+
  |                                       |
CLOSE --- SQL error ----------------------+
  |                                       |
RETURN                            ABEND-ROUTINE
                                      |
                                  LINK ABNDPROC
                                      |
                                  CICS ABEND ITRD
```

## Confirmed Evidence
- Exact five-part equality lookup.
- Single fetch.
- Successful not-found.
- Read-only behavior.
- Character-only date rearrangement.
- Composite transaction ID.
- No list semantics.
- No record-eyecatcher/delete/type filter.
- No null-indicator/defaulting behavior.

## Reasonable Inferences
- The five-part key is intended to uniquely identify one transaction; modern repository composite-PK evidence supports the interpretation but is not legacy-source proof of physical uniqueness.
- PIC 9 fields imply numeric-shaped COMMAREA values, but the source does not establish an HTTP-style validation contract.

## Remaining Uncertainties
- Inbound CICS transaction/program routing and caller.
- JCL/BMS/CICS resource definitions.
- Production DB2 DDL/indexes/constraints.
- Standalone system SQLCA definition.
- `ABNDFILE` resource definition and downstream operational processing.


## Artifact Relationships

- **Upstream Inputs:** `inputs(5).zip` legacy assets.
- **Downstream Consumers:** `supporting/dependency-map.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `research.md`, `supporting/requirements.md`, `spec.md`.
- **Authority Boundary:** Authoritative for supplied legacy INQTRAND execution behavior only.
- **Conflict Handling:** Supplied COBOL/copybook evidence wins over this analysis; repository modernization conventions cannot overwrite legacy truth.
