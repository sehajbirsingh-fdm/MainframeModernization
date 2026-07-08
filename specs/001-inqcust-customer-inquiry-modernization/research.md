# research.md - Phase 0 Research

## Decision 1: Modernization pattern
**Decision**: Use strangler pattern for one bounded COBOL capability.  
**Rationale**: The POC should not imply full mainframe replacement. It demonstrates replacing INQCUST while the rest of the estate remains unchanged.  
**Alternatives considered**: Full rewrite, direct COBOL-to-Java translation, RAG assistant only.

## Decision 2: Data integration for POC
**Decision**: Use mock JSON repository.  
**Rationale**: No mainframe, CICS, IMS, or DB2 runtime is available. Mocking allows demonstration while keeping an adapter boundary for future integration.  
**Alternatives considered**: H2 database, direct DB2, z/OS Connect. H2 was unnecessary for the short POC; real integration is out of scope.

## Decision 3: API route design
**Decision**: Use `GET /api/v1/customers/{sortCode}/{customerNumber}`.  
**Rationale**: This mirrors the legacy key fields from INQCUST and CUSTOMER.  
**Alternatives considered**: `GET /customers/{customerId}`. Rejected because it hides sort code and weakens traceability.

## Decision 4: Legacy status preservation
**Decision**: Include `legacyStatus` object in API response.  
**Rationale**: Preserves the observable behavior of `INQCUST-INQ-SUCCESS` and `INQCUST-INQ-FAIL-CD` while still allowing modern HTTP status codes.  
**Alternatives considered**: Only HTTP status codes. Rejected because it loses the legacy semantics needed for modernization traceability.

## Decision 5: Risk assessment enhancement
**Decision**: Add derived risk assessment from status, credit score, and review date.  
**Rationale**: Demonstrates modernization value beyond lift-and-shift using fields already present in the legacy record.  
**Alternatives considered**: Customer segmentation. Rejected because risk assessment maps more directly to existing credit fields.
