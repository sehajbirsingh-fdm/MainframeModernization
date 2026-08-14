# Specification — 00B INQTRAND Transaction Detail Inquiry

## Feature Overview

Feature 00B modernizes the legacy INQTRAND transaction-detail inquiry. It enables an authorized user to retrieve at most one transaction by the complete five-part legacy identity while preserving the legacy distinction between successful absence and technical failure.

## Objective

Provide an implementation-ready, observable behavior contract for transaction detail that:
- preserves confirmed INQTRAND outcomes;
- integrates with the existing modern transaction capability;
- excludes INQTRANL list behavior;
- separates modernization transport decisions from legacy evidence.

## Scope

### In Scope
- exact five-part transaction lookup;
- found and not-found result presentation;
- transaction detail fields and derived transactionId;
- fixed-width path input;
- REST/JSON contract;
- existing account inquiry security/correlation;
- minimal existing-frontend integration;
- tests and OpenAPI alignment.

### Out of Scope
- transaction list modernization;
- date-range filtering;
- pagination/limit/offset/count/order;
- transaction create/update/delete;
- type-code management;
- hidden deletion/eyecatcher filtering;
- production DB2 connectivity;
- unrelated account/customer capability changes.

## Actors

- **Authorized account inquiry user:** requests transaction detail.
- **Modern frontend/API consumer:** submits the exact key and presents result.
- **Modern backend:** validates transport shape and performs one exact read-only lookup.
- **Persistence adapter:** reads current POC `PROCTRAN`.

## Preconditions

1. Caller is authenticated and authorized under existing account inquiry security.
2. Caller supplies all five path identity components.
3. Current POC transaction persistence is available for normal lookup.
4. The feature does not require INQTRANL list inquiry to be executed first; list-to-detail navigation is a UI convenience only.

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
When exactly one matching row is retrievable:
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

This specification consumes BR-001..BR-012. The transport-visible rules most directly expressed here are:
- BR-002 exact five-part lookup;
- BR-003 date reshaping only;
- BR-004 found success;
- BR-005 not-found success;
- BR-006 composite ID;
- BR-009 read-only;
- BR-010 no list semantics;
- BR-011 no hidden record filter;
- BR-012 no unsupported null default.

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
An 8-digit date or 6-digit time with unusual calendar/clock values is not rejected by a newly invented semantic validation rule. Persistence behavior determines whether the lookup yields absence or technical failure.

## Edge Cases

1. **No matching row:** 200 / found false.
2. **Leading zeros:** retained throughout identity and ID generation.
3. **Description padding:** fixed CHAR trailing padding may be removed; substantive content retained.
4. **Nullable selected data:** do not use list-specific null-to-zero/blank defaults to force success.
5. **Unexpected type value:** return as opaque data if otherwise retrievable; do not reject solely because it is absent from `PROCTRAN.cpy` 88-level names.
6. **Eyecatcher/delete metadata:** no extra predicate.
7. **Multiple physical matches:** legacy intent is zero-or-one; current modern composite PK suggests uniqueness. If implementation inspection reveals duplicates are possible, treat as a data/integration issue and do not invent selection ordering.
8. **List route coexistence:** existing `/transactions` list operation remains unchanged.

## Feature Invariants

- INV-001: exactly five identity components.
- INV-002: found=false implies transaction=null.
- INV-003: found=true implies a complete transaction detail result.
- INV-004: transactionId reflects the returned five identity fields exactly.
- INV-005: no list query semantics.
- INV-006: operation is read-only.
- INV-007: absence is successful.
- INV-008: technical failure is not absence.
- INV-009: modernization transport decisions are not mislabeled as legacy behavior.

## Acceptance Criteria

- **AC-001 / FR-001:** request cannot omit any key component.
- **AC-002 / FR-002:** repository lookup constrains all five key columns and contains no list count/order/pagination behavior.
- **AC-003 / FR-003:** seeded matching transaction produces 200, found true, one detail.
- **AC-004 / FR-004:** non-matching complete key produces 200, found false, null transaction.
- **AC-005 / FR-005:** found response contains all nine approved detail fields.
- **AC-006 / FR-006:** ID equals exact hyphenated five-part composition.
- **AC-007 / FR-007:** external date remains 8 digits; implementation does not add calendar-validation business semantics.
- **AC-008 / FR-008:** no transaction mutation path exists.
- **AC-009 / FR-009:** SQL adds no eyecatcher/delete/type predicate.
- **AC-010 / FR-010:** matched nullable-detail handling is not silently defaulted to list behavior.
- **AC-011 / FR-011:** leading-zero keys round-trip correctly.
- **AC-012 / FR-012:** feature contract exposes the approved GET path.
- **AC-013 / FR-013:** existing transaction frontend can render found and not-found detail states.
- **AC-014 / SEC-001..SEC-003:** endpoint demonstrates 401/403/authorized behavior through existing security.
- **AC-015 / OPS-003:** runtime OpenAPI includes a contract-equivalent 00B path and schemas.
- **AC-016 / NFR-004:** required backend/frontend/contract/E2E tests pass or are explicitly blocked with evidence.

## Non-Goals

No list rework, transaction mutation, type taxonomy redesign, mainframe adapter implementation, or unrelated UI overhaul.

## Traceability

| Specification area | Requirements / rules |
|---|---|
| Identity and lookup | FR-001, FR-002, BR-002 |
| Found | FR-003, BR-004 |
| Not found | FR-004, BR-005 |
| Fields / ID | FR-005, FR-006, BR-006, BR-007 |
| Date | FR-007, BR-003 |
| Read-only | FR-008, BR-009 |
| Filtering/null | FR-009, FR-010, BR-011, BR-012 |
| Width | FR-011 |
| REST path/UI | FR-012, FR-013, MOD-001..MOD-004 |
| Security/errors | SEC-*, OPS-001..OPS-002 |


## Artifact Relationships

- **Upstream Inputs:** `supporting/requirements.md`, `supporting/business-rules.md`, `supporting/mapping-matrix.md`, `supporting/intended-system.md`, `data-model.md`.
- **Downstream Consumers:** `plan.md`, `tasks.md`, `contracts/openapi.yaml`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`.
- **Authority Boundary:** Authoritative for observable feature behavior and acceptance criteria, not implementation structure.
- **Conflict Handling:** If Specification conflicts with Supporting Requirements or confirmed rules, reconcile the upstream requirement/evidence first; implementation must not choose an alternative silently.
