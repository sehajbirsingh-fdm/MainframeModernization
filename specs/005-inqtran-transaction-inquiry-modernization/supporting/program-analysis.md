# Program Analysis - INQTRANL

## Evidence Classification

- Confirmed evidence: directly visible in the supplied legacy sources and copybooks.
- Reasonable inference: constrained conclusion supported by multiple evidence points.
- Remaining uncertainty: behavior not provable from supplied artifacts.

## Legacy Artifacts Reviewed

- `INQTRANL.cbl`
- `INQTRANL.cpy`
- `PROCDB2.cpy`
- `SORTCODE.cpy`
- `ABNDPROC.cbl`
- `ABNDINFO.cpy`
- `INQTRAND.cbl`
- `INQTRAND.cpy`

## Program Purpose and Scope

### Confirmed evidence

- `INQTRANL` is a CICS COBOL program (`PROGRAM-ID. INQTRANL`) that reads a transaction list from DB2 `PROCTRAN` for one sort code and account number, with date-range filtering and pagination semantics implemented in COBOL.
  - Evidence: `INQTRANL.cbl` program header comment; SQL declaration `DECLARE TRAN-CURSOR`; paragraphs `GTC010` (`GET-TOTAL-COUNT`), `RTD010` (`READ-TRANSACTIONS-DB2`), `FTD010` (`FETCH-TRANSACTION-DATA`).
- It returns through COMMAREA:
  - total matching row count,
  - returned row count,
  - success flag,
  - up to 100 transaction rows.
  - Evidence: `INQTRANL.cpy` layout (`INQTRANL-TOTAL-COUNT`, `INQTRANL-RETURNED-COUNT`, `INQTRANL-SUCCESS`, `INQTRANL-TRANSACTIONS OCCURS 100`); assignments in `INQTRANL.cbl` paragraph `A010`; row population in `FTD010`.

### Reasonable inference

- The business capability is account transaction list inquiry (list, not detail retrieval).

### Remaining uncertainty

- None on primary scope.

## Inputs and Outputs

### Legacy structure (declared in copybook, not executable logic)

#### COMMAREA structure (confirmed)

From `INQTRANL.cpy`:

- `INQTRANL-EYE PIC X(4)` with `88 INQTRANL-EYE-VALID VALUE 'ITRL'`.
- Inputs:
  - `INQTRANL-SORTCODE PIC 9(6)`
  - `INQTRANL-ACCNO PIC 9(8)`
  - `INQTRANL-FROM-DATE PIC 9(8)` with `88 INQTRANL-NO-FROM-DATE VALUE 0`
  - `INQTRANL-TO-DATE PIC 9(8)` with `88 INQTRANL-NO-TO-DATE VALUE 99999999`
  - `INQTRANL-LIMIT PIC 9(3)` with `88 INQTRANL-DEFAULT-LIMIT VALUE 50`
  - `INQTRANL-OFFSET PIC 9(5)`
- Outputs:
  - `INQTRANL-TOTAL-COUNT PIC 9(5)`
  - `INQTRANL-RETURNED-COUNT PIC 9(3)`
  - `INQTRANL-SUCCESS PIC X` with 88-level true/false declarations
  - `INQTRANL-TRANSACTIONS OCCURS 100 TIMES` with row fields listed below.

  #### Row field details (confirmed)

From `INQTRANL.cpy`:

- `INQTRANL-TRAN-ID PIC X(50)`
- `INQTRANL-TRAN-SORTCODE PIC 9(6)`
- `INQTRANL-TRAN-ACCNO PIC 9(8)`
- `INQTRANL-TRAN-DATE PIC 9(8)`
  - `INQTRANL-TRAN-DATE-GRP REDEFINES INQTRANL-TRAN-DATE`
  - `YYYY PIC 9999`, `MM PIC 99`, `DD PIC 99`
- `INQTRANL-TRAN-TIME PIC 9(6)`
  - `INQTRANL-TRAN-TIME-GRP REDEFINES INQTRANL-TRAN-TIME`
  - `HH PIC 99`, `MM PIC 99`, `SS PIC 99`
- `INQTRANL-TRAN-REF PIC 9(12)`
- `INQTRANL-TRAN-TYPE PIC X(3)`
- `INQTRANL-TRAN-DESC PIC X(40)`
- `INQTRANL-TRAN-AMOUNT PIC S9(10)V99`

### Runtime behavior (executed by COBOL)

#### Input normalization and defaulting (confirmed)

From `INQTRANL.cbl` paragraph `A010`:

- If eyecatcher invalid, value is overwritten to `ITRL` (no rejection path).
- If `INQTRANL-NO-FROM-DATE`, from-date is set to `0`.
- If `INQTRANL-NO-TO-DATE`, to-date is set to `99999999`.
- If limit is `0`, limit becomes `50`.
- If limit is greater than `100`, limit becomes `100`.
- Offset is not range-validated beyond COMMAREA type.

Declared-vs-executed distinction:

- `INQTRANL.cpy` 88-level values define named conditions (`INQTRANL-EYE-VALID`, `INQTRANL-NO-FROM-DATE`, `INQTRANL-NO-TO-DATE`, `INQTRANL-DEFAULT-LIMIT`) but do not themselves perform defaulting.
- Defaulting/normalization is executed by conditional statements in `INQTRANL.cbl` paragraph `A010`.

#### Output population (confirmed)

From `INQTRANL.cbl` paragraphs `A010` and `FTD010`:

- `INQTRANL-TOTAL-COUNT <- WS-TOTAL-COUNT`.
- `INQTRANL-RETURNED-COUNT <- WS-FETCH-COUNT`.
- `INQTRANL-SUCCESS <- 'Y'` on normal completion.
- Transaction rows are populated only for fetched rows beyond offset and up to limit.

Declared-vs-executed distinction:

- `INQTRANL.cpy` declares `INQTRANL-SUCCESS` and 88-level values (`Y`/`N`).
- `INQTRANL.cbl` sets `INQTRANL-SUCCESS` explicitly in `A010` on normal completion; no instruction in `A010` sets it to `N` on entry.

#### Maximum transaction array size (confirmed)

- Hard maximum is 100 rows by COMMAREA (`OCCURS 100`) and limit capping at 100.
  - Evidence: `INQTRANL.cpy` (`INQTRANL-TRANSACTIONS OCCURS 100`), `INQTRANL.cbl` paragraph `A010` (`IF INQTRANL-LIMIT > 100 MOVE 100 TO INQTRANL-LIMIT`).

## COPY and Program Dependencies

### COPY/INCLUDE dependencies in INQTRANL (confirmed)

- `COPY SORTCODE`.
- `EXEC SQL INCLUDE PROCDB2 END-EXEC`.
- `EXEC SQL INCLUDE SQLCA END-EXEC`.
- `COPY ABNDINFO` (inside `ABNDINFO-REC`).
- `COPY INQTRANL REPLACING INQTRANL-COMMAREA BY DFHCOMMAREA`.

### Role of each dependency (confirmed)

- `INQTRANL.cpy`: COMMAREA contract.
- `PROCDB2.cpy`: DB2 table declaration for `PROCTRAN`.
- `SQLCA`: SQLCODE and SQL diagnostics area.
- `ABNDINFO.cpy`: structure passed to `ABNDPROC`.
- `SORTCODE.cpy`: constant `SORTCODE PIC 9(6) VALUE 987654`; not used by processing logic.
  - Evidence of non-use in logic: no reference to `SORTCODE` identifier in executable paragraphs `A010`, `GTC010`, `RTD010`, `FTD010`, `AR010`, `AH010`.

## DB2 Table and Host Variable Mapping

### PROCTRAN declaration from PROCDB2 (confirmed)

From `PROCDB2.cpy`:

- `PROCTRAN_EYECATCHER CHAR(4)`
- `PROCTRAN_SORTCODE CHAR(6) NOT NULL`
- `PROCTRAN_NUMBER CHAR(8) NOT NULL`
- `PROCTRAN_DATE CHAR(8)`
- `PROCTRAN_TIME CHAR(6)`
- `PROCTRAN_REF CHAR(12)`
- `PROCTRAN_TYPE CHAR(3)`
- `PROCTRAN_DESC CHAR(40)`
- `PROCTRAN_AMOUNT DECIMAL(12, 2)`

### Data-type mismatches between COMMAREA and DB2/host variables

#### Confirmed evidence

- COMMAREA numeric fields map to CHAR host variables for predicates and output moves:
  - sort code (`PIC 9(6)` <-> `PIC X(6)`),
  - account (`PIC 9(8)` <-> `PIC X(8)`),
  - reference (`PIC 9(12)` <-> `PIC X(12)`).
  - Evidence: `INQTRANL.cpy` vs `HOST-PROCTRAN-ROW` in `INQTRANL.cbl`.
- Date width mismatch in query-related host variables:
  - DB2 declaration says `PROCTRAN_DATE CHAR(8)` in `PROCDB2.cpy`.
  - Program uses `HV-PROCTRAN-DATE PIC X(10)`, `HV-QUERY-FROM-DATE PIC X(10)`, `HV-QUERY-TO-DATE PIC X(10)` and converts to/from `YYYY-MM-DD`.
  - Evidence: `PROCDB2.cpy` (`PROCTRAN_DATE CHAR(8)`); `INQTRANL.cbl` host declarations (`HV-PROCTRAN-DATE PIC X(10)`, `HV-QUERY-FROM-DATE PIC X(10)`, `HV-QUERY-TO-DATE PIC X(10)`); conversion paragraphs `CYI010` and `CIY010`; predicates in SQL declaration `TRAN-CURSOR` and `TRAN-COUNT-CURSOR`.

#### Reasonable inference

- The code expects DB2 date values in hyphenated text format for cursor predicates and row mapping, despite `PROCDB2` showing `CHAR(8)`.

#### Remaining uncertainty

- Whether `PROCDB2.cpy` is stale or the SQL engine performs implicit conversion compatible with the deployed schema is not provable from supplied artifacts alone.
- No NULL indicator variables are declared for nullable columns (`DATE`, `TIME`, `REF`, `TYPE`, `DESC`, `AMOUNT`) despite nullable definitions in `PROCDB2.cpy`.

## SQL Analysis

### Cursor declarations (confirmed)

From `INQTRANL.cbl` SQL declarations:

- List cursor: `TRAN-CURSOR`
  - `SELECT PROCTRAN_EYECATCHER, PROCTRAN_SORTCODE, PROCTRAN_NUMBER, PROCTRAN_DATE, PROCTRAN_TIME, PROCTRAN_REF, PROCTRAN_TYPE, PROCTRAN_DESC, PROCTRAN_AMOUNT`
  - `FROM PROCTRAN`
  - `WHERE PROCTRAN_SORTCODE = :HV-QUERY-SORTCODE`
  - `AND PROCTRAN_NUMBER = :HV-QUERY-ACCNO`
  - `AND PROCTRAN_DATE >= :HV-QUERY-FROM-DATE`
  - `AND PROCTRAN_DATE <= :HV-QUERY-TO-DATE`
  - `ORDER BY PROCTRAN_DATE DESC, PROCTRAN_TIME DESC`
  - `FOR FETCH ONLY`
- Count cursor: `TRAN-COUNT-CURSOR`
  - `SELECT COUNT(*) FROM PROCTRAN`
  - same predicates as list cursor,
  - no `ORDER BY`.

### Host variables (confirmed)

- Predicate variables:
  - `HV-QUERY-SORTCODE PIC X(6)`
  - `HV-QUERY-ACCNO PIC X(8)`
  - `HV-QUERY-FROM-DATE PIC X(10)`
  - `HV-QUERY-TO-DATE PIC X(10)`
- Pagination variables populated but not used in SQL text:
  - `HV-QUERY-LIMIT PIC S9(4) COMP`
  - `HV-QUERY-OFFSET PIC S9(8) COMP`
- Row fetch target variables are `HOST-PROCTRAN-ROW` fields.

### COUNT cursor vs LIST cursor behavior

#### Confirmed evidence

- `GET-TOTAL-COUNT` opens, fetches once from `TRAN-COUNT-CURSOR`, closes cursor.
- `READ-TRANSACTIONS-DB2` opens `TRAN-CURSOR`, delegates fetch loop to `FETCH-TRANSACTION-DATA`, then closes cursor.
- Count result is copied to `WS-TOTAL-COUNT` and then to COMMAREA total count.
- COUNT returns the unpaginated total matching row count.
  - Evidence: SQL declaration `TRAN-COUNT-CURSOR` is `SELECT COUNT(*) ...` with account/date predicates only; no limit/offset logic in `GTC010`; pagination is applied only in `FTD010`.

### SQL predicates and date filtering behavior

#### Confirmed evidence

- Account filter is exact equality on sort code and account number.
- Date filter is inclusive lower and upper bound (`>=` and `<=`).
- Omitted-date source conditions are defined in COMMAREA as:
  - `INQTRANL-NO-FROM-DATE VALUE 0`,
  - `INQTRANL-NO-TO-DATE VALUE 99999999`.
  - Evidence: `INQTRANL.cpy`.
- In `A010`, omitted-date conditions trigger sentinel assignments:
  - if `INQTRANL-NO-FROM-DATE`, `INQTRANL-FROM-DATE <- 0`;
  - if `INQTRANL-NO-TO-DATE`, `INQTRANL-TO-DATE <- 99999999`.
  - Evidence: `INQTRANL.cbl` `A010`.
- Sentinel/defaulted date values are mechanically converted through the same path used for non-omitted values:
  - `INQTRANL-*-DATE -> WS-DATE-YYYYMMDD -> CONVERT-YYYYMMDD-TO-ISO -> WS-DATE-ISO -> HV-QUERY-*-DATE`.
  - Evidence: `INQTRANL.cbl` `GTC010`, `RTD010`, `CYI010`.
- Both count and list SQL statements always retain both date predicates:
  - `PROCTRAN_DATE >= :HV-QUERY-FROM-DATE`
  - `PROCTRAN_DATE <= :HV-QUERY-TO-DATE`
  - Evidence: `DECLARE TRAN-COUNT-CURSOR`, `DECLARE TRAN-CURSOR`.
- The shown INQTRANL path includes no alternate SQL text that removes a date predicate when a boundary is omitted.
- The shown INQTRANL path includes no pre-SQL calendar-validity or sentinel-validity guard for input dates before predicate execution.
- Non-acceptable SQLCODE values from cursor OPEN/FETCH/CLOSE paths route to `ABEND-ROUTINE` technical-failure handling.
  - Evidence: SQLCODE checks in `GTC010`, `RTD010`, `FTD010`.

#### Reasonable inference

- Omitted-date processing in legacy is sentinel-normalization plus bounded predicates, not a distinct predicate-omission mode in SQL text.
- Converted sentinel values can reach DB2 predicate evaluation as plain host-variable strings without additional INQTRANL-side date-sanity screening.

#### Remaining uncertainty

- Repository evidence does not conclusively establish deployed `PROCTRAN_DATE` runtime type/format behavior for this path.
- Runtime treatment of converted sentinel values in the date predicates is unresolved from static evidence alone.
- It is not provable from supplied artifacts that omitted-date handling always yields an unconstrained effective boundary.
- It is not provable from supplied artifacts that omitted-date handling definitively produces SQLCODE `-180`.

### Ordering and tie behavior

#### Confirmed evidence

- List query ordering is exactly:
  - first `PROCTRAN_DATE DESC`,
  - then `PROCTRAN_TIME DESC`.
- No additional tie-breaker column exists.

#### Remaining uncertainty

- Order of rows sharing same date and time is unspecified by the shown SQL.

### Pagination approach and offset/limit handling

#### Confirmed evidence

- No SQL `OFFSET`/`FETCH FIRST` clause is used.
- Pagination is implemented in COBOL loop `FTD010`:
  - fetch rows one by one,
  - increment `WS-SKIP-COUNT` until it reaches `HV-QUERY-OFFSET`,
  - then copy rows and increment `WS-FETCH-COUNT`,
  - stop when `SQLCODE = 100` or `WS-FETCH-COUNT >= HV-QUERY-LIMIT`.

#### Reasonable inference

- OFFSET cost is linear in fetched rows because skipped rows are still fetched from DB2.

### SQLCODE paths (confirmed)

- OPEN count cursor: SQLCODE must be `0` or abend routine is called.
- FETCH count cursor: SQLCODE allowed values are `0` and `100`; other nonzero values call abend routine.
- CLOSE count cursor: SQLCODE must be `0` or abend routine is called.
- OPEN list cursor: SQLCODE must be `0` or abend routine is called.
- FETCH list cursor loop:
  - `0` => process/skip row,
  - `100` => normal end-of-data,
  - other nonzero => abend routine.
- CLOSE list cursor: SQLCODE must be `0` or abend routine is called.

### Resource lifecycle (confirmed)

- Count cursor lifecycle is open -> fetch once -> close in `GET-TOTAL-COUNT`.
- List cursor lifecycle is open -> repeated fetch loop -> close in `READ-TRANSACTIONS-DB2`.
  - Evidence: `GTC010` (`OPEN TRAN-COUNT-CURSOR`, single `FETCH`, `CLOSE TRAN-COUNT-CURSOR`); `RTD010` + `FTD010` (`OPEN TRAN-CURSOR`, looped `FETCH`, `CLOSE TRAN-CURSOR`).

## Error-Path Analysis

### Cursor OPEN failures

#### Confirmed evidence

- On nonzero SQLCODE after `OPEN TRAN-COUNT-CURSOR` or `OPEN TRAN-CURSOR`, `ABEND-ROUTINE` is performed.
  - Evidence: `GTC010`, `RTD010`.

### Cursor FETCH failures

#### Confirmed evidence

- Count fetch: nonzero and not `100` invokes `ABEND-ROUTINE`.
- List fetch: nonzero and not `100` invokes `ABEND-ROUTINE`.
  - Evidence: `GTC010`, `FTD010`.

### SQLCODE +100 handling

#### Confirmed evidence

- List fetch `+100` is treated as normal loop termination.
- Count fetch `+100` is accepted by code path (not treated as abend).

#### Remaining uncertainty

- For `COUNT(*)`, whether `+100` can occur in this deployed DB2 setup is not established from artifacts.

### Cursor CLOSE failures

#### Confirmed evidence

- Nonzero SQLCODE after `CLOSE TRAN-COUNT-CURSOR` or `CLOSE TRAN-CURSOR` invokes `ABEND-ROUTINE`.
  - Evidence: close checks in `GTC010` and `RTD010`.

### COUNT cursor processing vs LIST cursor processing

#### Confirmed evidence

- COUNT cursor returns one aggregate value into `WS-TOTAL-COUNT`.
- LIST cursor drives row-by-row copy into COMMAREA array with offset/limit logic.
  - Evidence: `FETCH TRAN-COUNT-CURSOR INTO :WS-TOTAL-COUNT` in `GTC010`; iterative `FETCH TRAN-CURSOR` and row mapping in `FTD010`.

### ABNDPROC invocation and technical abend behavior

#### Confirmed evidence

- `ABEND-ROUTINE` builds `ABNDINFO-REC`, populates EIB response fields, APPLID, task/transaction id, time/date, SQLCODE, freeform text, links to `ABNDPROC`, then executes `EXEC CICS ABEND ABCODE('ITRL') CANCEL NODUMP`.
  - Evidence: `INQTRANL.cbl` paragraph `AR010`.
- `ABEND-HANDLING` (registered by `EXEC CICS HANDLE ABEND`) also builds `ABNDINFO-REC`, links `ABNDPROC`, then returns.
  - Evidence: `INQTRANL.cbl` paragraph `AH010`.
- `ABNDPROC` writes the received record to CICS file `ABNDFILE` keyed by `ABND-VSAM-KEY`.
  - Evidence: `ABNDPROC.cbl` paragraph `A010`, `EXEC CICS WRITE FILE('ABNDFILE') ... RIDFLD(ABND-VSAM-KEY)` and `ABNDINFO.cpy` key layout.

### Success path and zero-result behavior

#### Confirmed evidence

- Normal completion path sets `INQTRANL-SUCCESS` to `Y` and returns.
- Zero-result list is not an error:
  - list fetch loop stops on `SQLCODE = 100`,
  - returned count remains 0,
  - program still sets success to `Y`.
- If no rows match predicates, total count can still be 0 via count cursor fetch value.
  - Evidence: `A010` sets success to `Y` after `PERFORM GET-TOTAL-COUNT` and `PERFORM READ-TRANSACTIONS-DB2`; `FTD010` loop exits on `SQLCODE = 100`; returned count sourced from `WS-FETCH-COUNT`.

## Composite Transaction ID Construction

### Confirmed evidence

- Transaction ID is assembled in `WS-TRAN-ID-PARTS`:
  - sortcode(6) + '-' + account(8) + '-' + date(8) + '-' + time(6) + '-' + ref(12)
- Total formatted length before destination padding is 38 characters.
- Moved into `INQTRANL-TRAN-ID PIC X(50)`.
  - Evidence: `WS-TRAN-ID-PARTS` definition and moves in `FTD010`.

### Reasonable inference

- Destination `X(50)` preserves the 38-character composite plus trailing spaces.

## Relationship with INQTRAND

### Confirmed evidence

- `INQTRAND` is a separate program that retrieves one transaction by composite key (`sortcode`, `accno`, `date`, `time`, `ref`) using its own cursor `TRAND-CURSOR` and COMMAREA (`INQTRAND.cpy`).
- Neither `INQTRANL` nor `INQTRAND` contains a direct `CALL`/`LINK` to the other.

### Reasonable inference

- They are complementary list/detail operations over the same DB2 table.

## Legacy Constraints

### Confirmed evidence

- Maximum returned rows are constrained to 100 by both COMMAREA structure and runtime cap.
  - Evidence: `INQTRANL.cpy` (`INQTRANL-TRANSACTIONS OCCURS 100`); `INQTRANL.cbl` paragraph `A010` limit cap.
- Pagination is implemented in COBOL fetch-loop logic, not SQL syntax.
  - Evidence: SQL declarations `TRAN-CURSOR` and `TRAN-COUNT-CURSOR` contain no SQL offset/limit clause; runtime skip/count in `FTD010`.
- DB2 access path is read-only cursor processing for list retrieval.
  - Evidence: both cursor declarations use `FOR FETCH ONLY`; runtime performs `OPEN`, `FETCH`, `CLOSE` only (`GTC010`, `RTD010`, `FTD010`).
- Transaction identifier returned in COMMAREA is generated by runtime concatenation logic.
  - Evidence: `WS-TRAN-ID-PARTS` structure and moves in `FTD010`.
- SQL result ordering is constrained to date-desc/time-desc with no third key in declared ORDER BY.
  - Evidence: `DECLARE TRAN-CURSOR ... ORDER BY PROCTRAN_DATE DESC, PROCTRAN_TIME DESC`.

## IMS, MQ, and CICS Dependency Assessment

### CICS dependencies (confirmed)

- `PROCEDURE DIVISION USING DFHCOMMAREA`.
- `EXEC CICS HANDLE ABEND`.
- `EXEC CICS ASSIGN APPLID` and `ASSIGN PROGRAM`.
- EIB fields (`EIBRESP`, `EIBRESP2`, `EIBTASKN`, `EIBTRNID`).
- `ASKTIME`, `FORMATTIME`, `LINK`, `ABEND`, `RETURN`.

### IMS dependencies

#### Confirmed evidence

- Compile option includes `DLI` in `CBL CICS('SP,EDF,DLI')`.

#### Remaining uncertainty

- No DL/I calls, PCBs, or IMS statements are present in supplied program body; runtime IMS dependency is not demonstrated by these artifacts.

### MQ dependencies (confirmed)

- No MQ API usage appears in supplied source.

## Observable Processing Flow

### Confirmed evidence

1. Register CICS abend handler and initialize counters (`A010`).
2. Normalize eyecatcher and defaults; cap limit (`A010`).
3. Convert date inputs for SQL host variables (`GET-TOTAL-COUNT`, `READ-TRANSACTIONS-DB2`).
4. Open/fetch/close count cursor (`GET-TOTAL-COUNT`).
5. Open list cursor (`READ-TRANSACTIONS-DB2`).
6. Fetch loop applies offset skip and limit bound while mapping result rows (`FETCH-TRANSACTION-DATA`).
7. Close list cursor (`READ-TRANSACTIONS-DB2`).
8. Move counts, set success `Y`, return (`A010`, `GET-ME-OUT-OF-HERE`).
9. Any SQL error path records abend info through `ABNDPROC` then issues CICS ABEND (`ABEND-ROUTINE`).

## Unsupported Assumptions Removed

### Confirmed removals from prior analysis

- Removed modernization suitability commentary and target-architecture discussion.
- Removed API/security/OpenAPI design assumptions not provable from legacy source.
- Removed non-legacy requirements framing.

## Remaining Open Questions

- Is `PROCDB2.cpy` date definition (`CHAR(8)`) fully aligned with deployed DB2 schema used by this program, given the program-side `YYYY-MM-DD` host variable conversion (`X(10)`)?
- Are nullable `PROCTRAN` columns guaranteed populated in production data, given no SQL NULL indicator host variables are declared?
- Can `PROCTRAN_REF` contain non-numeric characters in data, and if so how is move behavior to numeric COMMAREA field handled operationally?
- Is there any external caller-side validation of calendar correctness for input `FROM-DATE`/`TO-DATE` values?
