# Research: CRECUST Customer Create Modernization

## Decision 1: Canonical Endpoint
- Decision: Use `POST /v1/customers` for modernized customer creation.
- Rationale: CRECUST is a write operation that creates a new customer identity.
- Alternatives:
  - `PUT /v1/customers/{id}`: rejected because ID is generated, not client-supplied.

## Decision 2: Sortcode Ownership
- Decision: Keep sortcode system-owned (legacy `SORTCODE` behavior), not API-input.
- Rationale: CRECUST moves sortcode from copybook constant, not user payload.
- Alternatives:
  - Client-supplied sortcode: rejected for parity risk and potential identity inconsistency.

## Decision 3: Customer Number Allocation Strategy
- Decision: Implement sortcode-scoped atomic control-number allocator in mock mode.
- Rationale: Mirrors `GET-LAST-CUSTOMER-DB2` select+increment behavior.
- Alternatives:
  - UUID customer number: rejected due to mismatch with copybook 10-digit numeric contract.

## Decision 4: Legacy Status Preservation
- Decision: Expose `legacyStatus.commSuccess` and `legacyStatus.commFailCode` in modern response metadata.
- Rationale: Needed for parity and troubleshooting across old/new flows.
- Alternatives:
  - Hide fail codes and rely only on HTTP: rejected due to observability gap for migration testing.

## Decision 5: Credit Check in POC
- Decision: Use adapter abstraction with deterministic mock results and fallback simulation.
- Rationale: CRECUST has complex async child-transaction logic; live CICS orchestration is out of scope.
- Alternatives:
  - Skip credit check entirely: rejected because it alters business behavior materially.

## Decision 6: Error Mapping
- Decision: Preserve legacy fail codes while mapping to HTTP statuses and standardized envelope.
- Rationale: Enables client simplicity without losing mainframe parity.
- Alternatives:
  - One generic error: rejected due to insufficient diagnostic detail.
