# Spec Document for INQACC Modernization

**Document ID:** `spec.md`  
**Pipeline:** mainframe_modernization  
**Authority:** src/base/cics/cobol/INQACC.cbl + src/base/cics/copy/ACCDB2.cpy + src/base/cics/copy/ACCOUNT.cpy + src/base/cics/copy/INQACC.cpy + src/base/cics/copy/INQACCCZ.cpy + checklists/requirements.md + supporting/program-analysis.md + supporting/mapping-matrix.md + supporting/intended-system.md  
**Status:** Draft — requires review  
**Generated:** 2026-07-17

## 1. Feature Overview
INQACC account inquiry modernization defines a read-only account lookup capability that exposes legacy inquiry behavior through a web-accessible API and a React web UI used to exercise, validate, test, and demonstrate the API. The feature preserves core legacy inquiry semantics while operating in POC mode with mock persistence and no live mainframe connectivity.

## 2. Objective
Provide a standardized account inquiry feature that:
- Preserves authoritative legacy lookup semantics and observable outcomes.
- Uses a consistent request key and endpoint contract.
- Enforces only source-supported security, validation, and error behaviors.
- Stays within POC boundaries (read-only inquiry, mock repository, no live DB2/CICS).

## 3. Scope
### In Scope
- Read-only account inquiry.
- Composite-key lookup using sort code and account number.
- Endpoint path `GET /v1/accounts/{sortcode}/{accountNumber}`.
- Validation and standardized error responses.
- Mock repository operation for POC.
- Mandatory authentication and authorization at the API boundary for inquiry access.
- React frontend behavior for exercising, validating, and demonstrating inquiry flow: input entry, validation feedback, submit, loading state, success rendering, not-found/error rendering, and repeat inquiry.
- Safe observability requirements that are explicitly supported by source authorities.

### Out of Scope
- Account creation, update, deletion, or any write operation.
- Live DB2 connectivity.
- Live CICS connectivity.
- Batch/multi-account inquiry workflows unless separately approved.

## 4. Actors
- Bank employee performing account inquiry.
- Account inquiry API.
- Mock repository (POC data source).
- Authentication/authorization boundary service.

## 5. Preconditions
- Request targets `GET /v1/accounts/{sortcode}/{accountNumber}`.
- `sortcode` and `accountNumber` are supplied in path parameters.
- POC mode is active with mock repository.
- Request includes a valid bearer token.

## 6. Functional Requirements
### FR-001 Lookup Key and Retrieval
The system shall retrieve at most one account record using composite key `ACCOUNT_SORTCODE + ACCOUNT_NUMBER`.

### FR-001A Reserved Account Number Branch
When `accountNumber = 99999999`, the system shall execute the legacy reserved-number behavior: retrieve the highest account number for the supplied `sortcode` and return that record. The endpoint and request parameters remain unchanged.

### FR-002 Read-Only Inquiry Behavior
The system shall support inquiry only and shall not create, modify, or delete account records.

### FR-003 POC Repository Boundary
The system shall use mock repository data in this POC and shall not call live DB2 or live CICS.

### FR-004 Response Mapping
The system shall return response fields mapped from legacy structures according to source mapping rules, including required conversions and trimming behavior documented in this specification.

### FR-005 Validation and Error Responses
The system shall validate path parameter formats and return standardized error responses with correct HTTP semantics.

### FR-006 Security (Source-Supported)
The system shall require bearer-token authentication and role-based authorization for inquiry access.

### FR-007 Safe Observability
The system shall record traceable operational events without logging credentials or sensitive business data.

## 7. Business Rules
### BR-001 Composite Key Rule
Lookup key is `sortcode + accountNumber` and corresponds to `ACCOUNT_SORTCODE + ACCOUNT_NUMBER`.

### BR-002 Sortcode Format Rule
`sortcode` must be numeric and exactly 6 digits.

### BR-003 Account Number Format Rule
`accountNumber` must be numeric and exactly 8 digits.

### BR-004 Security Rule
Missing/invalid/expired authentication results in 401. Authenticated users without required permission result in 403.

### BR-005 Error Rule
Malformed input returns 400. Valid key with no record returns 404. Unexpected internal failure returns 500. Repository or service unavailability returns 503.

## 8. Validation Rules
- `sortcode` regex: `^\d{6}$`
- `accountNumber` regex: `^\d{8}$`
- Empty, missing, or malformed path values are invalid.
- Validation failures return HTTP 400 and a standardized error payload.

## 9. API Behaviour
- Method and path: `GET /v1/accounts/{sortcode}/{accountNumber}`
- Request body: none
- Required path params: `sortcode`, `accountNumber`
- Correlation ID is generated or propagated by the backend and returned for traceability.
- Response content: account inquiry success payload or standardized error payload
- This specification does not define implementation classes, annotations, packages, or frameworks.

## 10. Success Behaviour
On successful lookup:
- Return HTTP 200.
- Return one account record mapped from authoritative legacy fields.
- Return the same 12 response fields for both normal and reserved-number lookups:
  - `eyecatcher`
  - `customerNumber`
  - `sortcode`
  - `accountNumber`
  - `accountType`
  - `interestRate`
  - `accountOpened`
  - `overdraftLimit`
  - `lastStatementDate`
  - `nextStatementDate`
  - `availableBalance`
  - `actualBalance`
- Preserve documented transformations (trimming, date conversion, decimal conversion).
- No write-side effects occur.

## 11. Error Behaviour
- **400 Bad Request**: malformed `sortcode` or `accountNumber`.
- **401 Unauthorized**: missing, malformed, invalid, or expired authentication.
- **403 Forbidden**: authentication succeeded but permission is insufficient.
- **404 Not Found**: key format is valid but no matching account exists.
- **500 Internal Server Error**: unexpected internal failure.
- **503 Service Unavailable**: generic repository/service-unavailable response for transient backend unavailability in POC operation.

Canonical error response structure:
```json
{
  "error": {
    "code": "ERR-001",
    "message": "Sortcode format invalid. Must be exactly 6 numeric digits.",
    "correlationId": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2024-01-15T10:30:45.123Z"
  }
}
```

## 12. Legacy Observable Behaviour
The following legacy-observable behaviors are preserved based on source analysis:

- **Lookup key**: composite `ACCOUNT_SORTCODE + ACCOUNT_NUMBER`.
- **Reserved account number branch**: `accountNumber = 99999999` returns the record with the highest account number for the supplied `sortcode`.
- **Record selection**: selection is based on the composite lookup key and returns a single matching account record or no match.
- **Not-found outcome**: valid key with zero matching records maps to not-found behavior.
- **Field trimming**: fixed-width character fields are trimmed for response representation.
- **Date conversion**: source DB2 DATE values (handled through COBOL date decomposition) are converted to ISO `yyyy-MM-dd` in responses.
- **Decimal conversion**: packed/binary numeric formats are converted to decimal response values with expected scale/sign handling.
- **Legacy status/error mapping**: legacy abend/error semantics are translated to standardized HTTP error outcomes.
- **Fields transformed or omitted**: legacy transport-specific fields (for example COMMAREA control/pointer fields and multi-occurrence envelope structures) are not exposed directly in modern response payloads.

## 13. Data Mapping Requirements
- Mapping source authorities are supporting/program-analysis.md and supporting/mapping-matrix.md.
- Composite key fields in request map to `ACCOUNT_SORTCODE` and `ACCOUNT_NUMBER`.
- Response fields map from authoritative account table/copybook fields.
- Transformation requirements:
  - Trim trailing spaces from fixed-width character values.
  - Convert date representations to ISO date strings.
  - Convert decimal/binary legacy numeric fields to correctly scaled decimal values.
 - POC nullable-field policy: mock records are complete and API responses include all 12 successful-response fields (no omitted/null data in normal POC responses).

## 14. Non-Functional Requirements Supported by Source Evidence
### NFR-001 Security Baseline (Supported)
- Bearer-token authentication and role-based authorization are source-supported and in scope.

### NFR-002 Transport Security (Supported)
- TLS 1.2+ requirement is source-supported.

### NFR-003 Observability Baseline (Supported)
- Structured logging and correlation-ID propagation are source-supported.

### NFR-004 Safe Logging (Mandatory)
- Logging shall not include bearer tokens, account numbers, customer numbers, balances, or full account payloads.
- Logging shall contain operational metadata only: correlation ID, request path/template, HTTP status, duration, and event type.

### NFR-005 Optional Enhancement
- Distributed tracing integration may be added as an optional enhancement and is not a mandatory requirement for this POC specification.

## 15. Acceptance Criteria
### AC-001 Exact Input Validation
Given an inquiry request with `sortcode` not matching `^\d{6}$` or `accountNumber` not matching `^\d{8}$`, the API returns HTTP 400 with standardized error payload.

### AC-002 Successful Composite-Key Lookup
Given valid `sortcode` and `accountNumber` that exist in the repository, the API returns HTTP 200 with one mapped account record.

### AC-002A Reserved-Number Lookup
Given valid `sortcode` and `accountNumber = 99999999`, the API returns HTTP 200 with the account record having the highest account number for that sortcode.

### AC-003 No-Match Behaviour
Given valid `sortcode` and `accountNumber` that do not exist, the API returns HTTP 404 with standardized error payload.

### AC-004 Response Field Mapping
Given a successful lookup, response fields reflect authoritative mapping and conversions (trimmed CHAR fields, ISO date format, correctly scaled decimals).

### AC-005 Read-Only Behaviour
For all inquiry requests, no create/update/delete side effects occur in account data.

### AC-006 Mock-Repository Boundary
In POC mode, inquiry requests are served from mock repository only, with no live DB2/CICS calls.

### AC-007 Error Semantics
The API uses HTTP semantics exactly as specified in Section 11, including 401 vs 403 distinction and 500/503 handling.

### AC-008 Safe Logging
Operational logs exclude bearer tokens, account numbers, customer numbers, balances, and full account detail payloads while preserving traceability metadata.

### AC-009 Security Behaviour
Missing/invalid/expired authentication returns 401 and authenticated-but-unauthorized requests return 403.

### AC-010 Legacy Behaviour Preservation
The documented legacy observable behaviors in Section 12 are preserved and testable.

## 16. Assumptions and Unresolved Decisions
### Assumptions
- Supporting artifacts under `supporting/` and source requirements in `checklists/requirements.md` are treated as current authority inputs for this rewrite.
- Security and observability baselines remain in scope per source authorities unless governance explicitly removes them.

### Unresolved Decisions
- None. Legacy lookup behavior and system-intent authority references have been resolved in this correction.
