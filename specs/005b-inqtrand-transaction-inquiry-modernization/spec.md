# Specification — 005B INQTRAND Transaction Detail Inquiry

## Feature Overview

Feature 005B modernizes the legacy INQTRAND transaction-detail inquiry. It enables an authorized user to retrieve at most one transaction by the complete five-part legacy identity while preserving the legacy distinction between successful absence and technical failure.

## Objective

Provide an implementation-ready, observable behavior contract for transaction detail that:
- preserves confirmed INQTRAND outcomes;
- integrates with the existing modern transaction capability;
- excludes INQTRANL list behavior;
- separates modernization transport decisions from legacy evidence.

## Scope

### In Scope
- This feature includes exact transaction-detail lookup by the complete five-part identity.
- This feature includes presentation of both found and not-found outcomes.
- This feature includes the approved transaction detail fields and derived `transactionId` representation.
- This feature includes fixed-width path input handling at the transport boundary.
- This feature includes the approved REST/JSON contract for transaction-detail inquiry.
- This feature includes existing account inquiry security and correlation behavior.
- This feature includes minimal integration with the existing frontend transaction flow.
- This feature includes alignment of tests and OpenAPI with the approved feature contract.

### Out of Scope
- This feature excludes transaction list modernization.
- This feature excludes date-range filtering behavior.
- This feature excludes pagination, limit, offset, count, and ordering behavior.
- This feature excludes transaction create, update, and delete operations.
- This feature excludes transaction type-code management.
- This feature excludes hidden deletion or eyecatcher filtering behavior.
- This feature excludes production DB2 connectivity.
- This feature excludes unrelated account or customer capability changes.

## Actors

- **Authorized account inquiry user:** requests transaction detail.
- **Modern frontend/API consumer:** submits the exact key and presents result.
- **Modern backend:** validates transport shape and performs one exact read-only lookup.
- **Persistence adapter:** reads current POC `PROCTRAN`.

## Preconditions

1. Caller is authenticated and authorized under existing account inquiry security.
2. Caller supplies all five path identity components.
3. Current POC transaction persistence is available for normal lookup.
4. Direct API detail lookup does not require INQTRANL list inquiry to be executed first.
5. Minimal approved frontend integration requires supported list-to-detail navigation flow: transaction list row -> five transaction identity components -> transaction-detail navigation -> existing transaction API client -> INQTRAND detail endpoint -> found-detail or successful-absence presentation.

## User Stories

### US-001 — View a known transaction
As an authorized account inquiry user, I want to retrieve a transaction using its complete identity so that I can inspect its details.

Acceptance intent:
- exact five-key lookup;
- one detail object;
- composite transaction ID;
- no list semantics.

### US-002 — Handle a missing transaction normally
As an authorized user, I want a missing transaction to be represented as a normal inquiry result so that absence is distinguishable from a system failure.

Acceptance intent:
- HTTP 200;
- `found=false`;
- `transaction=null`;
- no 404/500 solely because no row exists.

### US-003 — See a technical failure distinctly
As an operator/client, I want data-access failures to remain technical errors with correlation information so they are not confused with normal absence.

### US-004 — Navigate from existing transaction UI
As a user viewing transaction information, I want the existing transaction UI to request detail using the row’s complete key without creating a second transaction application.

## Behavioral Specification

### BS-001 — Input identity
The authoritative inquiry identity is:
1. sortCode;
2. accountNumber;
3. date;
4. time;
5. reference.

All components are mandatory.

### BS-002 — Lookup behavior
The backend performs exactly one logical transaction-detail query whose predicates constrain every identity component with equality. It must not add:
- fromDate/toDate;
- result order;
- pagination;
- result count;
- limit/offset;
- PROCTRAN record-eyecatcher predicate;
- logical-delete predicate;
- transaction-type predicate.

### BS-003 — Found behavior
When the exact five-part lookup yields a retrievable transaction-detail result:
- return HTTP 200;
- `found=true`;
- `transaction` contains the approved detail fields.

### BS-004 — Not-found behavior
When no row matches:
- return HTTP 200;
- `found=false`;
- `transaction=null`.

No-row is not a validation error, authorization error, or technical failure.

### BS-005 — Technical behavior
If the persistence operation cannot complete or a matched row cannot be mapped under the preserved detail rules, return the existing structured technical error response with HTTP 500 / `ERR-500` and correlationId.

### BS-006 — Read-only invariant
No request in this feature mutates transaction state.

## Business Rules

This specification applies the relevant confirmed legacy behavioral rules while keeping modernization transport, architecture, error, and security mechanisms under their approved modern authorities. The transport-visible rules most directly expressed here are:
- BR-002: The transaction-detail lookup uses exact equality on all five identity components.
- BR-003: Date handling is limited to approved reshaping/representation behavior and does not introduce new semantic calendar validation.
- BR-004: When a matching transaction is retrieved, the inquiry returns a successful found response.
- BR-005: When no matching transaction is retrieved, the inquiry returns a successful not-found response rather than an error.
- BR-006: The `transactionId` is the exact composite of the five identity components using the approved hyphenated format.
- BR-009: Transaction-detail inquiry behavior is strictly read-only and performs no mutation.
- BR-010: Transaction-detail inquiry does not apply list-query semantics such as range, ordering, pagination, or count behavior.
- BR-011: The lookup does not apply hidden-record filtering predicates such as eyecatcher or logical-delete metadata filters.
- BR-012: Matched nullable detail values are not silently defaulted using unsupported list-style null substitution behavior.

## Validation Rules

Transport-shape validation is an **Approved Modernization Decision**.

| Parameter | Required | Constraint |
|---|---|---|
| `sortCode` | yes | exactly 6 digits |
| `accountNumber` | yes | exactly 8 digits |
| `date` | yes | exactly 8 digits |
| `time` | yes | exactly 6 digits |
| `reference` | yes | exactly 12 digits |

### Validation boundary
- Do not reject an 8-digit date solely because it is not a valid Gregorian date.
- Do not reject a 6-digit time solely because it is outside a semantic clock range.
- Those semantic rules are not confirmed legacy behavior.
- Invalid digit width/characters are transport syntax violations and produce the existing validation response.

## API Behavior

### Operation
`GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`

### Security
Existing account GET security applies. The operation requires authenticated access with `ACCOUNT_INQUIRER`.

## Request Parameters

All five parameters are path parameters. There are no query parameters for date range, pagination, limit, offset, count, or ordering.

## Success Responses

### Found — HTTP 200

```json
{
  "found": true,
  "transaction": {
    "transactionId": "123456-12345678-20260814-142530-000000000123",
    "sortCode": "123456",
    "accountNumber": "12345678",
    "date": "20260814",
    "time": "142530",
    "reference": "000000000123",
    "type": "DBT",
    "description": "Sample transaction",
    "amount": 125.50
  }
}
```

Field rules:
- `transactionId`: exact five-part concatenation with four hyphens.
- `type`: opaque string up to legacy width 3; no closed enumeration is imposed.
- `description`: transaction description with fixed database padding removed where representational.
- `amount`: exact decimal number, not silently manufactured from SQL null.

## Empty-Result Behavior

### Not Found — HTTP 200

```json
{
  "found": false,
  "transaction": null
}
```

This is required by FR-004 / COMP-002 and preserves BR-005.

## Error Responses

### Validation — HTTP 400
Use current structured error payload convention with code `ERR-001` for path syntax/shape validation failures.

### Authentication — HTTP 401
Existing security behavior.

### Authorization — HTTP 403
Existing security behavior.

### Technical — HTTP 500
Use current structured error payload with `ERR-500` and correlationId for repository/technical failures.

A missing transaction must not be converted into 500.

## Examples

### Leading-zero identity
`GET /api/v1/accounts/012345/00123456/20260814/090005/000000000001`

The modern boundary must not numerically coerce away leading zeros.

### Width-invalid path
A 5-digit sort code is rejected as validation error before the detail lookup.

### Semantically unusual but width-valid date/time
An 8-digit date or 6-digit time with unusual calendar/clock values is not rejected by a newly invented semantic validation rule and must proceed through the normal lookup path. If no matching row exists, the result is successful absence (HTTP 200 with `found=false`, `transaction=null`). HTTP 500 with `ERR-500` applies only to actual persistence/technical failure.

## Edge Cases

1. **No matching row:** 200 / found false.
2. **Leading zeros:** retained throughout identity and ID generation.
3. **Description padding:** fixed CHAR trailing padding may be removed; substantive content retained.
4. **Nullable selected data:** do not use list-specific null-to-zero/blank defaults to force success.
5. **Unexpected type value:** return as opaque data if otherwise retrievable; do not reject solely because it is absent from `PROCTRAN.cpy` 88-level names.
6. **Eyecatcher/delete metadata:** no extra predicate.
7. **Multiple physical matches:** legacy evidence establishes exact five-part equality predicates and a single FETCH (at-most-one consumed row), but production physical uniqueness is not proven by supplied production DB2 DDL/index/constraint evidence. If implementation inspection reveals duplicates are possible, treat as a data/integration issue and do not invent selection ordering or arbitrary duplicate selection behavior.
8. **List route coexistence:** existing `/transactions` list operation remains unchanged.

## Feature Invariants

- INV-001: The transaction-detail identity always consists of exactly five components.
- INV-002: Whenever `found=false`, the response `transaction` value is `null`.
- INV-003: Whenever `found=true`, the response contains a complete approved transaction detail result.
- INV-004: The `transactionId` always reflects the returned five identity fields exactly in approved composition order.
- INV-005: The operation never introduces list-query semantics.
- INV-006: The operation remains read-only for all requests.
- INV-007: Transaction absence is always represented as a successful inquiry outcome.
- INV-008: Technical failure is always represented distinctly from transaction absence.
- INV-009: Approved modernization transport decisions are not represented as confirmed legacy behavior.

## Acceptance Criteria

- **AC-001 / FR-001:** Given a caller prepares a transaction-detail request, when the caller omits any one of the five key path components, then the request is not accepted as a valid feature request because all five components are mandatory.
- **AC-002 / FR-002:** Given a structurally valid five-part transaction identity, when the backend executes the detail lookup, then the repository query constrains all five key columns by equality and contains no list count, order, range, pagination, limit, or offset behavior.
- **AC-003 / FR-003:** Given a seeded transaction that exactly matches all five identity components, when the caller invokes the detail endpoint, then the response is HTTP 200 with `found=true` and one transaction detail object.
- **AC-004 / FR-004:** Given a structurally valid five-part identity that matches no stored transaction, when the caller invokes the detail endpoint, then the response is HTTP 200 with `found=false` and `transaction=null`.
- **AC-005 / FR-005:** Given a successful found transaction-detail response, when the response payload is inspected, then the transaction object contains all nine approved detail fields.
- **AC-006 / FR-006:** Given a successful found transaction-detail response, when `transactionId` is evaluated, then it equals the exact hyphenated composition of the returned five identity components.
- **AC-007 / FR-007:** Given a detail request, when transport validation is applied, then validation remains structural only: `date` must be exactly 8 digits and `time` must be exactly 6 digits, with no invented Gregorian-calendar or HHMMSS semantic-range validation, no account-existence validation, and no transaction-reference business validation.
- **AC-008 / FR-008:** Given any transaction-detail request, when the request is processed, then no transaction mutation path is executed.
- **AC-009 / FR-009:** Given a structurally valid five-part identity, when the repository lookup is generated, then SQL adds no eyecatcher, logical-delete, or transaction-type predicate.
- **AC-010 / FR-010:** Given a matched row with nullable detail values, when the response is mapped, then nullable-detail handling is not silently defaulted to list behavior.
- **AC-011 / FR-011:** Given a request that includes leading zeros in identity components, when the request is processed and echoed through detail behavior, then leading-zero key values round-trip without numeric coercion.
- **AC-012 / FR-012:** Given the 005B feature contract, when the transaction-detail operation is published, then it exposes the approved GET path `GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`.
- **AC-013 / FR-013:** Given a user selects a transaction row in the existing transaction frontend flow, when detail navigation is performed, then the frontend uses the five identity components, invokes the existing transaction API client and approved detail endpoint, and renders either found-detail or successful-absence state.
- **AC-014 / SEC-001..SEC-003:** Given the approved endpoint security configuration, when unauthenticated, unauthorized, and authorized requests are exercised, then the endpoint demonstrates existing 401, 403, and authorized-access behavior.
- **AC-015 / OPS-003:** Given the runtime API description, when OpenAPI is produced for 005B, then it includes a contract-equivalent detail path and schemas.
- **AC-016 / NFR-004:** Given the required backend, frontend, contract, and E2E verification scope, when the test suite is executed, then all required tests pass or are explicitly blocked with evidence.
- **AC-017 / OPS-002:** Given an authenticated and authorized caller supplies a structurally valid five-part transaction identity and the transaction persistence operation encounters an actual technical failure, when the caller invokes `GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`, then the system returns HTTP 500 using the existing structured technical-error response with `ERR-500` and a `correlationId`.

## Non-Goals

No list rework, transaction mutation, type taxonomy redesign, mainframe adapter implementation, or unrelated UI overhaul.

## Traceability

| Specification area | Requirements / rules |
|---|---|
| Identity and lookup | FR-001, FR-002, BR-002 |
| Found | FR-003, BR-004 |
| Not found | FR-004, BR-005 |
| Fields / ID | FR-005, FR-006, BR-006, BR-007 |
| Legacy date representation/reshaping | BR-003 |
| Structural validation boundary (modern) | FR-007 |
| Read-only | FR-008, BR-009 |
| Filtering/null | FR-009, FR-010, BR-011, BR-012 |
| Width | FR-011 |
| REST path/UI | FR-012, FR-013, MOD-001..MOD-004, supporting/intended-system.md, research.md |
| Security/errors | SEC-*, OPS-001..OPS-002 |


## Artifact Relationships

- **Upstream Inputs:** `supporting/requirements.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `supporting/architecture.md`, `research.md`, `data-model.md`.
- **Downstream Consumers:** `plan.md`, `tasks.md`, `contracts/openapi.yaml`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`.
- **Authority Boundary:** Authoritative for observable feature behavior and acceptance criteria, not implementation structure.
- **Conflict Handling:** If Specification conflicts with confirmed legacy behavior, approved supporting requirements, approved modernization decisions, or approved architecture/repository constraints, reconcile those upstream authorities first; implementation must not choose an alternative silently.
