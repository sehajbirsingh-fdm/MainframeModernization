# Spec Document for CRECUST Modernization

**Document ID:** `spec.md`  
**Pipeline:** mainframe_modernization  
**Authority:** src/base/cics/cobol/CRECUST.cbl + src/base/cics/copy/CRECUST.cpy + src/base/cics/copy/CUSTOMER.cpy + src/base/cics/copy/CUSTDB2.cpy + src/base/cics/copy/CUSTCTRL.cpy + src/base/cics/copy/NEWCUSNO.cpy + mock-data/customer-records.json  
**Status:** Draft - implementation-ready  
**Generated:** 2026-07-22

## 1. Feature Overview
CRECUST modernization defines a customer creation capability that preserves the legacy behavioral sequence of the CICS COBOL program while exposing it through a modern API boundary for POC use with mock data. The flow includes title validation, date-of-birth validation, customer-number allocation, credit score handling, customer persistence, and response status semantics aligned to legacy COMMAREA outcomes.

## 2. Objective
Provide a standardized create-customer feature that:
- Preserves source-supported legacy rules and fail-code behavior from CRECUST.
- Uses copybook-defined request/response fields only.
- Keeps POC execution mock-first with no live DB2/CICS/IMS dependencies.
- Produces deterministic, testable outcomes for success and failure paths.

## 3. Scope
### In Scope
- Create one customer record per request.
- Legacy title validation and date-of-birth validation.
- Legacy credit-score orchestration semantics and fallback outcomes.
- Legacy-style generated customer number behavior by sortcode.
- Response with legacy success/fail signaling plus mapped customer payload.
- Structured error response mapping for API consumers.
- Mock repository based on `mock-data/customer-records.json`.

### Out of Scope
- Updating or deleting existing customers.
- Account creation as part of this feature.
- Live DB2 table updates and live CICS async child transactions in POC.
- Batch customer onboarding.

## 4. Actors
- Bank employee (or system client) creating a customer.
- Customer creation API.
- Mock customer repository.
- Optional future credit-check integration adapter.

## 5. Preconditions
- Request is sent to canonical create endpoint.
- Payload fields follow copybook boundaries and expected formats.
- POC mode is active with mock repository.
- Sortcode is derived from system configuration (legacy behavior) and not client-controlled.

## 6. Functional Requirements
### FR-001 Create Customer
The system shall create one customer record with a generated customer number and return success metadata when all validation and persistence steps succeed.

### FR-002 Copybook-Constrained Input
The system shall only accept and process fields that exist in `CRECUST.cpy` request shape:
- title, firstName, lastName
- dateOfBirth (day/month/year)
- phone
- addressLine1, addressLine2, city, postcode, country
- status
- createdDate (day/month/year)

### FR-003 Allowed Title Validation
The system shall accept only these title values from legacy logic:
- `Professor`
- `Mr`
- `Mrs`
- `Miss`
- `Ms`
- `Dr`
- `Drs`
- `Lord`
- `Sir`
- `Lady`
- blank
Any other title value shall fail with legacy fail code `T`.

### FR-004 Date-Of-Birth Validation
The system shall apply source-supported date validation rules:
- Year must be >= 1601.
- Date must be valid calendar date (CEEDAYS equivalent validation semantics).
- Date must not be in the future.
- Implied age must not exceed 150 years.

### FR-005 Credit Score Handling
The system shall preserve observable credit-check outcomes:
- If credit-check orchestration succeeds with returned agency values, calculate average score and assign review date in next 21 days.
- If no credit data is returned or credit orchestration fails, credit score defaults to 0 and review date falls back to current date semantics, and the create flow returns failure with legacy fail-code mapping (no customer record creation).

### FR-006 Customer Number Generation
The system shall allocate a new customer number per sortcode by reading and incrementing control state (legacy `CONTROL` table behavior), preserving monotonic increment semantics.

### FR-007 Customer Persistence
The system shall persist customer data using copybook-aligned mapping and numeric date conversion:
- `DOB` as integer `YYYYMMDD`
- `createdDate` as integer `YYYYMMDD`
- `creditScoreReviewDate` as integer `YYYYMMDD`

### FR-008 Success Response
On success, the system shall return:
- `eyecatcher = CUST`
- generated `sortcode`
- generated `customerNumber`
- copied/derived customer fields
- legacy success indicator mapped to `commSuccess = Y`
- legacy fail code mapped to blank

### FR-009 Error Response and Legacy Status Mapping
The system shall map legacy fail outcomes into standardized HTTP responses while preserving legacy `commSuccess` and `commFailCode` observability in payload metadata.

### FR-010 POC Persistence Boundary
The system shall use `CustomerRepository` abstraction and mock data source only; no direct DB2/CICS/IMS calls in POC runtime.

## 7. Business Rules
### BR-001 Valid Title Rule
Title must be one of the explicit allowed values; otherwise fail code `T`.

### BR-002 DOB Lower Bound Rule
DOB year below 1601 is invalid; fail code `O`.

### BR-003 DOB Parsing Rule
Invalid calendar DOB fails with fail code `Z`.

### BR-004 DOB Future Rule
DOB in future fails with fail code `Y`.

### BR-005 DOB Max Age Rule
Derived age above 150 fails with fail code `O`.

### BR-006 Credit Default Rule
If credit-check returns no usable results, score becomes `0`; review date uses current-date fallback semantics.

### BR-007 Customer Number Allocation Rule
Next customer number is obtained by incrementing control value for the sortcode-specific control key.

### BR-008 Write Failure Rule
Customer persistence failure maps to legacy fail code `1`.

### BR-009 Named Counter Control Rule
Named counter enqueue/dequeue failures map to fail code `3` and `5` respectively.

### BR-010 Control Table Failure Rule
Failure reading/updating control state maps to fail code `4`.

### BR-011 Credit Orchestration Failure Codes
Legacy async credit orchestration fail codes are preserved:
- `A` container put failure
- `B` child transaction run failure
- `C` fetch-any notfinished with no data
- `D` fetch-any invalid request
- `E` get-container failure
- `F` child completion abend
- `G` security/credit-check fallback failure path
- `H` unknown child completion status

## 8. Validation Rules
- String fields are accepted as provided (including blank values) and passed through according to copybook field lengths in parity mode.
- `status` is passed through as a 10-character field from request to persistence in parity mode.
- `phone`, `postcode`, `addressLine2` may be blank.
- Date fields are validated using rule set in Section 7.

## 9. API Behavior
- Method and path: `POST /v1/customers`
- Request body: customer create payload
- Response:
  - `201 Created` on success
  - standardized error payload on failure
- Response always includes correlation ID.
- Response includes `legacyStatus` object with `commSuccess` and `commFailCode` to preserve legacy observability.

## 10. Success Behavior
On successful create:
- Return HTTP 201.
- Return created customer payload containing copybook-backed fields.
- Return generated `customerNumber` and system `sortcode`.
- Return `legacyStatus.commSuccess = "Y"` and blank fail code.

## 11. Error Behavior
- **400 Bad Request**: invalid title, invalid DOB, invalid payload shape.
- **422 Unprocessable Entity**: semantic business-rule failure mapped from legacy fail codes where request is syntactically valid but fails rule checks.
- **500 Internal Server Error**: unexpected processing failure.
- **503 Service Unavailable**: repository/control-state unavailable.

For request-shape and payload parsing failures (for example malformed JSON or request-body validation failures), the API returns `legacyFailCode = "0"` in the standardized error envelope.

Canonical error response structure:
```json
{
  "error": {
    "code": "ERR-001",
    "message": "Title is invalid. Allowed values are Professor, Mr, Mrs, Miss, Ms, Dr, Drs, Lord, Sir, Lady, or blank.",
    "legacyFailCode": "T",
    "correlationId": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2026-07-22T10:30:45.123Z"
  }
}
```

## 12. Legacy Observable Behavior Preservation
The modernization preserves these observable CRECUST behaviors:
- Title whitelist logic and fail code `T`.
- DOB validation window and fail codes `O`, `Z`, `Y`.
- Sortcode-derived customer-number generation flow.
- Credit-score average calculation when responses exist.
- Zero-credit fallback on missing credit responses.
- Customer create as a write operation with one output identity.
- Legacy success/fail markers represented in API metadata.

## 13. Data Mapping Requirements
- Mapping authority is `supporting/mapping-matrix.md`.
- Request maps to COMMAREA fields from `CRECUST.cpy`.
- Persistence model maps to `CUSTOMER` table fields from `CUSTDB2.cpy`.
- Integer legacy dates (`YYYYMMDD`) are returned as ISO `yyyy-MM-dd` in JSON responses.
- Fixed-width CHAR fields are trimmed on output.

## 14. Non-Functional Requirements
### NFR-001 Maintainability
Use clear service/repository boundaries with constructor injection only.

### NFR-002 Testability
Every business rule and fail code path must be covered with automated tests.

### NFR-003 Reliability
Customer-number generation must be deterministic and monotonic per sortcode in mock mode.

### NFR-004 Observability
Correlation ID and legacy fail code must be traceable in logs and error responses.

## 15. Acceptance Criteria
### AC-001 Valid Title Accepted
Given title in allowed set, request proceeds to later stages.

### AC-002 Invalid Title Rejected
Given title outside allowed set, API returns mapped error and legacy fail code `T`.

### AC-003 DOB Validation
Given invalid DOB per rules, API returns mapped business-rule failure with corresponding legacy code.

### AC-004 Customer Number Generated
Given valid request, API returns generated 10-digit customer number.

### AC-005 Customer Created
Given valid request and repository availability, API returns 201 and persisted record is retrievable.

### AC-006 Legacy Status Preserved
Given any result, `legacyStatus.commSuccess` and `legacyStatus.commFailCode` are included and correct.

### AC-007 Credit Score Fallback
Given no credit response scenario, score is 0 and review date fallback semantics are applied and the flow returns failure (matching legacy behavior).

### AC-008 Date Conversion
Given stored numeric dates, API returns ISO `yyyy-MM-dd` date strings.

### AC-009 Mock Boundary
No live DB2/CICS calls are made in POC mode.

### AC-010 Fail Code Mapping
All legacy fail codes used by CRECUST are mapped and test-covered.

## 16. Assumptions and Unresolved Decisions
### Assumptions
- `SORTCODE` constant remains environment-owned and not client-provided.
- Mock repository can simulate control-state increments for customer number generation.
- External credit-check integration is simulated in POC.

### Unresolved Decisions
- Whether `422` should be used for all business-rule failures or only specific fail codes.
- Whether title input should be normalized (`Mr` vs padded legacy format) at API boundary.
