# Program Analysis - BNKSTMT

## Legacy Artifacts Reviewed
- legacy-bankofz/base/batch/pli/BNKSTMT.pli
- legacy-bankofz/base/batch/jcl/BNKSTMT.jcl

## Confirmed Behavior
- BNKSTMT is a batch statement-generation program.
- Statement period is determined in GET_STATEMENT_PERIOD.
- Account rows are read via ACCT_CURSOR.
- Period-bounded transactions are read via TRAN_CURSOR.
- Statement content is formatted for report output (SYSPRINT).

## Modernization Interpretation
- Statement retrieval API should preserve period and statement semantics.
- Report-oriented output is transformed to structured API response.
