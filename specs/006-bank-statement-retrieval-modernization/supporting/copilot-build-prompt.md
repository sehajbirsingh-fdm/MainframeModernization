# Copilot Build Prompt - 006 Bank Statement Retrieval

Build only the bank statement retrieval capability for ACC-03.

Authority:
- specs/006-bank-statement-retrieval-modernization/spec.md
- supporting/requirements.md
- supporting/program-analysis.md
- supporting/mapping-matrix.md
- contracts/openapi.yaml
- legacy-bankofz/base/batch/pli/BNKSTMT.pli
- legacy-bankofz/base/batch/jcl/BNKSTMT.jcl

Rules:
- Preserve period-based statement semantics.
- Keep capability distinct from INQTRANL and INQTRAND.
- Keep controller thin and logic in service.
- No live batch/JCL execution in POC runtime.
