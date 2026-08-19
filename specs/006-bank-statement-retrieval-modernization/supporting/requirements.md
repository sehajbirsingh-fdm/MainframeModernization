# Requirements - 006 Bank Statement Retrieval

## Functional Requirements
- FR-001 Retrieve statement by account and period.
- FR-002 Enforce period format YYYYMM with valid month range 01..12.
- FR-003 Build period boundaries using BNKSTMT-aligned semantics.
- FR-004 Return summary values including periodFrom and periodTo, plus transaction entries.
- FR-005 Return empty entries when no transactions exist in period.
- FR-006 Return not-found when account does not exist.
- FR-007 Return technical-error outcome on retrieval failure.
- FR-008 Enforce authentication and authorization.

## Non-Functional Requirements
- NFR-001 Deterministic period and totals computation.
- NFR-002 Clear separation of controller/service/repository concerns.
- NFR-003 Sensitive value protection in logs/errors.
