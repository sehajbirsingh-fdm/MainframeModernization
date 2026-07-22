# architecture.md

## Purpose

Define the high-level system boundaries and responsibilities needed to deliver the approved intended INQACCCU modernization while preserving confirmed legacy behavior.

## Architectural Context

- Legacy source behavior is authoritative for business semantics.
- Frozen supporting artifacts define the approved modernization intent and preserved behavior baseline.
- This architecture is intentionally high-level and does not define implementation structure, technology products, deployment topology, or task sequencing.

## Confirmed Constraints

- Inquiry is read-only in business terms.
- Inquiry key is customer number.
- Customer validation is required before account retrieval.
- Sort code used for retrieval is internally derived fixed value 987654.
- Maximum returned accounts per inquiry is 20 in preserved baseline behavior.
- Account ordering is not guaranteed by legacy behavior.
- End-of-data equivalent to SQLCODE +100 is normal completion, not an error.
- Customer found with zero accounts is a valid successful outcome.
- Legacy status and failure distinctions must remain preserved at the business boundary.
- Fixed-width identifiers and leading zeroes must be preserved.

## System Context

The intended system has these major boundaries:

1. External inquiry interface boundary
- Accepts inquiry requests and returns inquiry outcomes.

2. Inquiry orchestration boundary
- Coordinates validation, retrieval, transformation, and outcome mapping.

3. Customer-validation capability boundary
- Provides customer-existence validation equivalent to legacy INQCUST behavior.

4. Account-data access boundary
- Provides read-only account retrieval for validated customer context and internally derived sort code.

5. Transformation and representation boundary
- Preserves legacy data semantics while producing external representations.

6. Status and error outcome boundary
- Preserves distinct legacy outcome states and failure semantics.

## Logical Components and Responsibilities

1. Inquiry Interface Component
- Receives customer-number inquiry input.
- Delegates to orchestration capability.
- Returns mapped business outcome.

2. Inquiry Orchestration Component
- Enforces inquiry flow sequence:
  - validate customer first
  - retrieve accounts only after validation success
  - apply response and status mapping

3. Customer Validation Dependency
- Represents the capability currently fulfilled by INQCUST behavior.
- Produces validation result used by orchestration.

4. Account Retrieval Component
- Performs read-only account retrieval using:
  - validated customer number
  - internally derived sort code 987654
- Preserves legacy constraints:
  - maximum 20 returned records
  - no implied deterministic ordering
  - normal end-of-data handling

5. Transformation Component
- Preserves identifier semantics (fixed-width and leading zeroes).
- Preserves full account information set.
- Preserves date semantics, including legacy DDMMYYYY output meaning.

6. Outcome Mapping Component
- Preserves business outcome distinctions:
  - customer not found
  - customer found with zero accounts
  - customer found with one or more accounts
  - retrieval failure paths matching legacy semantics
  - normal end-of-data as non-error completion

## High-Level Inquiry Flow

1. Receive customer-number inquiry.
2. Validate customer using customer-validation capability.
3. If validation fails, return preserved not-found/failure outcome.
4. If validation succeeds, retrieve accounts through read-only data-access boundary using internal sort code.
5. Transform retrieved data while preserving identifier and date semantics.
6. Map final business status/outcome and return response.

## Data and Transformation Boundaries

- Preserve complete legacy account information set:
  - eyecatcher
  - customer number
  - sort code
  - account number
  - account type
  - interest rate
  - opened date
  - overdraft limit
  - last statement date
  - next statement date
  - available balance
  - actual balance
- Preserve fixed-width identifier behavior and leading zeroes.
- Preserve legacy date semantics where account dates are represented in legacy output meaning as DDMMYYYY.
- Treat future external date-format choices as separate interface decisions.

## Error and Status Boundary

Architecture preserves distinct outcome categories equivalent to legacy behavior:

- customer validation/customer-not-found path
- customer found with zero accounts
- successful account retrieval with one or more accounts
- account retrieval failure categories equivalent to open/fetch/close failure paths
- normal end-of-data completion

This document does not finalize protocol-level status codes, envelope schemas, or endpoint-specific contract details.

## Cross-Cutting Concerns

- Input quality controls at the interface boundary.
- Consistent operational visibility and diagnosability.
- Consistent error propagation and outcome mapping across boundaries.

Tooling, platforms, and product-specific implementations for these concerns are outside this architecture baseline.

## Architecture Boundaries

This architecture intentionally does not define:

- class names, package structure, annotations, or method signatures
- exact interface contracts or endpoint paths
- ORM, repository framework, or driver selection
- security product or authentication/authorization mechanism
- infrastructure and deployment topology
- implementation phases or task sequence

## Risks and Unresolved Decisions

1. Customer-validation integration mechanism
- How the equivalent INQCUST validation capability is realized remains a project decision.

2. External interface style and contract detail
- Exact external contract conventions remain downstream decisions.

3. Future behavior beyond legacy 20-record bound
- Any expansion beyond preserved baseline requires explicit approval.

4. Ordering policy
- Whether future scope introduces deterministic ordering remains undecided.

5. Security requirements
- Required security posture and policy constraints are not yet finalized by authoritative requirements.

6. Production data-source strategy
- Long-term production data access approach remains undecided in this architecture baseline.

7. External date representation policy
- Target external date format conventions remain to be approved while preserving legacy semantics.
