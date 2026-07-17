# Research: INQACC Account Inquiry Modernization

## Decision 1: Canonical Composite Lookup Key
- Decision: Use `sortcode + accountNumber` as the authoritative inquiry key.
- Rationale: The endpoint contract in `spec.md` is `/v1/accounts/{sortcode}/{accountNumber}` and field mapping aligns to those values.
- Alternatives considered:
  - `accountNumber + accountType`: rejected due to conflict with endpoint contract and traceability artifacts.
  - Single-field account lookup: rejected because it weakens key precision and does not match specification.

## Decision 2: POC Persistence Strategy
- Decision: Use mock repository only, with no live DB2/CICS integration in this phase.
- Rationale: Scope and constraints explicitly prohibit production mainframe integration for the POC.
- Alternatives considered:
  - Direct DB2 adapter: rejected as out-of-scope for POC and higher operational complexity.
  - CICS passthrough integration: rejected for same scope and dependency reasons.

## Decision 3: Security Enforcement Pattern
- Decision: Enforce OAuth2/JWT authentication and role authorization before inquiry business logic.
- Rationale: Prevents unauthorized request execution and aligns to spec security baseline.
- Alternatives considered:
  - Service-only security checks: rejected because it delays rejection and weakens boundary protection.
  - Token-optional mode: rejected due to explicit auth requirements.

## Decision 4: Error Semantics and Envelope
- Decision: Standardize response errors to the documented envelope with correlation ID and mapped HTTP status.
- Rationale: Supports consistent client behavior and observability.
- Alternatives considered:
  - Framework default error body: rejected because it breaks contract consistency.
  - Multiple error payload shapes by failure type: rejected due to client complexity.

## Decision 5: Contract Source of Truth
- Decision: Retain `contracts/openapi.yaml` as external interface contract source for implementation validation.
- Rationale: Existing contract artifact already defines endpoint and schema expectations for tooling and tests.
- Alternatives considered:
  - Recreate contract from scratch in plan phase: rejected as redundant and risk-prone.
  - Spec-only without contract validation: rejected because it weakens conformance checks.
