# Research and Decisions - 006 Bank Statement Retrieval

## Source Evidence
- BNKSTMT is a batch statement-generation program.
- Statement period is explicitly computed and printed.
- Account and transaction cursors are used for statement assembly.

## Decisions
- Use BNKSTMT as primary legacy authority for statement retrieval behavior.
- Keep statement retrieval as a separate capability from transaction inquiry (INQTRANL) and transaction detail (INQTRAND).
- Expose period as YYYYMM path input in modern API contract.

## Risks
- Legacy output is report-oriented (SYSPRINT), not JSON API.
- Balance/format rules must be captured precisely during implementation.
