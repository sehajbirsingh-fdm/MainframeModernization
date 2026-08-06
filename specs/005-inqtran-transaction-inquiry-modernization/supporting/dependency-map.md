# Dependency Map

## Direct INQTRANL dependencies

```mermaid
graph LR
  Caller[CICS caller] -->|DFHCOMMAREA shape from INQTRANL.cpy| L

  subgraph CICS_Runtime[CICS runtime environment]
    L[INQTRANL]
    A[ABNDPROC]
  end

  L -->|EXEC SQL (OPEN/FETCH/CLOSE)| D[(DB2 subsystem)]
  D -->|table access| P[(PROCTRAN table)]

  L -->|EXEC SQL INCLUDE SQLCA (SQL status)| S[SQLCA]
  L -->|COPY and EXEC SQL INCLUDE at compile time| C1[INQTRANL.cpy / SORTCODE.cpy / ABNDINFO.cpy / PROCDB2.cpy]

  L -.error path only: EXEC CICS LINK.-> A
  A -->|EXEC CICS WRITE| V[(ABNDFILE VSAM/CICS file)]
```

Evidence anchors for the diagram:

- Runtime environment and COMMAREA entry: `INQTRANL.cbl` `PROCEDURE DIVISION USING DFHCOMMAREA`.
- DB2 access: SQL declarations `TRAN-CURSOR`, `TRAN-COUNT-CURSOR`; runtime SQL in `GTC010`, `RTD010`, `FTD010`.
- Error-only ABNDPROC dependency: `PERFORM ABEND-ROUTINE` from SQL error checks in `GTC010`/`RTD010`/`FTD010`, `EXEC CICS LINK PROGRAM(WS-ABEND-PGM)` in `AR010` and `AH010`.
- ABNDFILE dependency location: `ABNDPROC.cbl` paragraph `A010`, `EXEC CICS WRITE FILE('ABNDFILE') ... RIDFLD(ABND-VSAM-KEY)`.

## Dependency Classification

### Inbound dependencies

- CICS caller provides COMMAREA to `INQTRANL`.
  - Evidence: `INQTRANL.cbl` `PROCEDURE DIVISION USING DFHCOMMAREA`; `COPY INQTRANL REPLACING ... BY DFHCOMMAREA`.

### Outbound dependencies

- Normal execution dependency: DB2 read access through embedded SQL cursors.
  - Evidence: `DECLARE TRAN-COUNT-CURSOR`; `DECLARE TRAN-CURSOR`; execution in `GTC010`, `RTD010`, `FTD010`.
- Error-handling-only dependency: CICS `LINK` to `ABNDPROC`.
  - Evidence: `EXEC CICS LINK PROGRAM(WS-ABEND-PGM)` in `AR010` and `AH010`; invoked by SQL error branches and abend handler.

### Compile-time COPY dependencies

- `INQTRANL.cpy` for COMMAREA layout.
- `SORTCODE.cpy` for constant declaration.
- `ABNDINFO.cpy` for abend payload structure.
  - Evidence: `COPY` statements in `INQTRANL.cbl` working-storage/linkage sections.

### SQL INCLUDE dependencies

- `PROCDB2` included via `EXEC SQL INCLUDE PROCDB2 END-EXEC` (table declaration for `PROCTRAN`).
- `SQLCA` included via `EXEC SQL INCLUDE SQLCA END-EXEC` (SQLCODE/status).

### Runtime CICS services

- `HANDLE ABEND`, `ASSIGN APPLID`, `ASSIGN PROGRAM`, `ASKTIME`, `FORMATTIME`, `LINK`, `ABEND`, `RETURN`.
  - Evidence: paragraphs `A010`, `AR010`, `AH010`, `PTD010`, `GMOFH010` in `INQTRANL.cbl`.

### DB2 dependencies

- DB2 subsystem dependency via embedded SQL runtime.
- Logical table dependency on `PROCTRAN` (declared in `PROCDB2.cpy`; selected by both cursors).

### VSAM dependencies

- No direct VSAM file I/O in `INQTRANL`.
- Indirect VSAM dependency exists only through `ABNDPROC`, which writes to `ABNDFILE`.
  - Evidence: no `EXEC CICS READ/WRITE FILE` in `INQTRANL.cbl`; `ABNDPROC.cbl` paragraph `A010` performs `WRITE FILE('ABNDFILE')`.

### Missing runtime definitions

- CICS program and transaction definitions.
- `ABNDFILE` definition/record metadata beyond `ABNDINFO`.
- DB2 DDL/indexes and bind configuration.
- JCL/deployment assets.

## Related Legacy Programs

`INQTRAND` reads one `PROCTRAN` row by sort code, account number, date, time, and reference. Its input aligns with fields embedded in each INQTRANL composite transaction ID. However:

- INQTRANL contains no `CALL`, `LINK`, `XCTL`, or transaction reference to INQTRAND in `A010`, `GTC010`, `RTD010`, `FTD010`, `AR010`, or `AH010`.
- INQTRAND contains no reference to INQTRANL (separate `PROGRAM-ID`, separate cursor declaration `TRAND-CURSOR`, separate COMMAREA copybook `INQTRAND.cpy`).
- No UI/navigation evidence was supplied.

**Decision:** same broad business capability, separate future modernization feature. It is not required to implement or verify the INQTRANL list operation.
