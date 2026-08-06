# Spec Document for UPDCUST Modernization

Document ID: spec.md
Feature ID: 004a-updcust-customer-update-modernization
Authority: legacy-bankofz/base/cics/cobol/UPDCUST.cbl + legacy-bankofz/base/cics/copy/UPDCUST.cpy + legacy-bankofz/base/cics/copy/CUSTOMER.cpy + legacy-bankofz/base/cics/copy/CUSTDB2.cpy + legacy-bankofz/base/cics/copy/SORTCODE.cpy + legacy-bankofz/base/cics/copy/ABNDINFO.cpy + EXEC SQL INCLUDE SQLCA
Status: Draft - implementation-ready
Generated: 2026-07-30

## 1. Feature Overview
UPDCUST modernization defines customer update behavior that preserves observable legacy COBOL semantics while exposing a modern API and UI workflow. The legacy program updates selected customer fields only, returns COMMAREA status flags, and uses DB2-backed read-update behavior.

## 2. Objective
Deliver customer update capability that:
- Preserves legacy validation and fail-code semantics from UPDCUST.
- Uses copybook-defined fields only, without introducing non-authoritative fields.
- Supports professional UX placement by integrating update action from existing customer inquiry results.
- Remains POC-safe (no live CICS/DB2 integration required in runtime).

## 3. Scope
### In Scope
- Update existing customer record identified by customer number and optional sort code.
- Preserve title allow-list validation and legacy fail code behavior.
- Preserve selective-field update behavior (only certain fields update based on non-blank inputs).
- Preserve legacy update status signaling (`COMM-UPD-SUCCESS`, `COMM-UPD-FAIL-CD`).
- Return updated customer payload with copybook-backed fields.
- Define UI placement for update action and update form behavior.

### Out Of Scope
- Customer creation, deletion, account updates.
- Changes to credit-score generation logic.
- Changes to created-date or credit-score-review-date values during update.
- Any runtime connection to real mainframe/CICS/DB2 in POC mode.

## 4. Actors
- Bank operations user performing customer maintenance.
- Customer inquiry UI (source of update entry point).
- Customer update UI form.
- Customer update API endpoint.
- Repository abstraction for customer persistence.

## 5. Preconditions
- Customer exists in repository for the resolved key.
- Customer number path parameter is provided.
- Request payload follows copybook-constrained shape.
- For sort code resolution:
  - If request sort code is provided, it is used.
  - If absent/blank/low-values equivalent input, system falls back to configured legacy default sort code (from SORTCODE behavior).

## 6. Functional Requirements
### FR-001 Update Existing Customer
System shall update one existing customer record and return updated data on success.

### FR-002 Copybook-Constrained Update Fields
System shall only accept update fields mapped from UPDCUST COMMAREA:
- title, firstName, lastName
- dateOfBirth (day/month/year or ISO converted form)
- phone
- addressLine1, addressLine2, city, postcode, country
- status

### FR-003 Key Resolution
System shall resolve target customer by:
- customerNumber (required), and
- sortCode (optional; fallback to configured default when absent),
matching legacy behavior.

Customer number shall be normalized using legacy semantics before lookup:
- Numeric values shorter than 10 digits are left-padded with zeros.
- Spaces around numeric content are ignored.

### FR-004 Title Allow-List Validation
Allowed title values are:
- Professor, Mr, Mrs, Miss, Ms, Dr, Drs, Lord, Sir, Lady, blank
Any other title shall fail with legacy fail code T.

### FR-005 Minimum Meaningful Update Validation
If all of these are blank/absent:
- firstName
- lastName
- addressLine1
then request shall fail with legacy fail code 4.
Blank semantics for this rule follow legacy first-character behavior: values with first character as space are treated as blank.

### FR-006 Legacy Name Update Gate
Name/title update shall occur only when firstName is non-blank. In that case:
- title, firstName, and lastName are updated together.
If firstName is blank, title shall not be updated (legacy parity).

### FR-007 Legacy Address Update Gate
Address block update shall occur only when addressLine1 is non-blank. In that case:
- addressLine1, addressLine2, city, postcode, country are updated together.
If addressLine1 is blank, none of those address fields are updated (legacy parity).

### FR-008 Phone and Status Update Rules
- phone updates only when non-blank.
- status updates only when non-blank.
For parity, non-blank check is based on first character not being space.

### FR-008a Status Value Parity
UPDCUST does not enforce a status allow-list during update. Modernization shall not add a stricter status-value rule in parity mode.

### FR-008b Status Domain Governance Check
Because CUSTOMER copybook 88-level names define `ACTIVE`, `INACTIVE`, and `SUSPENDED`, parity-mode acceptance of any non-blank status value shall be explicitly confirmed with SMEs before production hardening. If SMEs reject open-ended values, stricter validation must be introduced via a documented non-parity mode.

### FR-009 Date Of Birth Update Rule
If DOB year is non-zero/non-blank, system computes integer yyyymmdd from provided components and updates DOB.
Modernization runtime validates DOB as ISO `yyyy-MM-dd` and rejects calendar-invalid values with HTTP 400.

### FR-010a No-Op Success Behavior
If request passes validation gates but does not satisfy any field-update gate, service may return success with no effective data change, preserving legacy parity.

### FR-010 Not Found Behavior
If target customer is not found, system returns failure with legacy fail code 1.

### FR-011 Data Access Failure Behavior
- Read/select failure maps to legacy fail code 2.
- Update failure maps to legacy fail code 3.

### FR-012 Success Behavior
On success:
- `legacyStatus.updSuccess = Y`
- `legacyStatus.updFailCode` is blank
- response returns updated customer values and immutable fields (createdDate, creditScore, creditScoreReviewDate).

### FR-012a Explicit Success Fail-Code Clearing
Modernization shall explicitly set `legacyStatus.updFailCode` to blank on every success response and must not rely on inherited caller or transport state.

### FR-012b Credit-Score-Review-Date Mapping Correction
Legacy UPDCUST performs a raw MOVE from binary host value to COMMAREA review-date group item, which can corrupt day/month/year fields. Modernization shall not preserve this defect. `creditScoreReviewDate` must be derived from numeric `yyyymmdd` semantics using the same decomposition style used for DOB/createdDate mapping.

### FR-013 API Contract
Canonical API for modernization:
- Method: PUT
- Path: /api/v1/customers/{customerNumber}
- Optional query parameter: sortCode

### FR-013a Published Contract Synchronization
The published runtime OpenAPI definition shall include the UPDCUST update operation (`PUT /api/v1/customers/{customerNumber}`), request/response schemas, and documented error envelopes for 400/401/403/404/422/500.

### FR-013b Authentication And Authorization
UPDCUST update endpoint shall be protected by authentication and authorization controls aligned to repository security policy:
- Unauthenticated request -> HTTP 401
- Authenticated but unauthorized request -> HTTP 403

### FR-014 UI Placement For Update Action
Update action shall be placed on Customer Inquiry success result as a primary action:
- Button label: Update Customer
- Route: /customers/{sortCode}/{customerNumber}/edit
- Edit form pre-populated from inquiry response.
- Button is displayed in the Customer Details panel (close to customer identity and status) for better operator discoverability.

### FR-014a Post-Update Navigation
After a successful update, UI shall navigate back to Customer Inquiry and present the updated customer values in inquiry context:
- Route after success: /customers
- Inquiry view must display updated customer details and success message.
- Inquiry form fields should remain prefilled with the same sort code and customer number used for the update.

### FR-014b UI Error Resilience
For any update failure (400/404/422/500, timeout, network, or non-standard error payload), UI shall:
- Keep rendering the current page without blank-screen failure.
- Show a user-safe error message and error details block when available.
- Preserve entered form values so operator can retry or correct input.

### FR-015 POC Repository Boundary
Feature shall use repository interfaces and local mock/H2-backed implementation only, with no direct mainframe integration in POC runtime.

## 7. Business Rules
- BR-001: Invalid title -> fail code T.
- BR-002: Customer not found -> fail code 1.
- BR-003: Data read failure -> fail code 2.
- BR-004: Data update failure -> fail code 3.
- BR-005: firstName blank AND lastName blank AND addressLine1 blank -> fail code 4.
- BR-006: Title change requires firstName non-blank.
- BR-007: Address change requires addressLine1 non-blank.
- BR-008: Phone update requires non-blank value.
- BR-009: Status update requires non-blank value.
- BR-010: DOB update requires provided year component.
- BR-011: First-character space is treated as blank for gate checks.
- BR-012: Payloads that pass validation but trigger no update gates may still return success.
- BR-013: On success, `updFailCode` is always explicitly blanked by service logic.
- BR-014: `creditScoreReviewDate` is computed from numeric `yyyymmdd` semantics; raw binary-to-group copy behavior is prohibited.
- BR-015: Status domain policy (`any non-blank` parity vs allow-list) requires SME sign-off before production hardening.

## 8. Validation Rules
### API-Level Structural Validation
- customerNumber required, numeric and normalizable to 10 digits (legacy left-pad behavior).
- sortCode when provided must be 6 digits.
- title max 10 chars.
- firstName max 50 chars.
- lastName max 50 chars.
- phone max 20 chars.
- addressLine1 max 50 chars.
- addressLine2 max 50 chars.
- city max 50 chars.
- postcode max 10 chars.
- country max 50 chars.
- status max 10 chars.

### Legacy-Parity Validation
- Title allow-list check.
- Minimum meaningful update check (name/address gate).
- Field-level update gating rules as in FR-006..FR-009.
- First-character blank semantics for all gate checks.
- No status allow-list enforcement in parity mode.

## 9. Response And Error Semantics
### Success
HTTP 200 with updated customer payload and:
- legacyStatus.updSuccess = Y
- legacyStatus.updFailCode = blank

### Failures
- 400 Bad Request: schema/format validation failure.
- 404 Not Found: customer not found (legacy fail code 1).
- 422 Unprocessable Entity: legacy business-rule failure (T or 4).
- 500 Internal Server Error: unexpected/select/update internal error (legacy fail 2 or 3 where applicable).

### Error Envelope
Error response shall include:
- code
- message
- legacyFailCode (when available)
- correlationId
- timestamp

## 10. Data Mapping Requirements
Mapping authority: supporting/mapping-matrix.md.

Mandatory mapping constraints:
- Copybook field lengths are authoritative.
- Output char fields trimmed.
- Numeric legacy date storage transformed to ISO date in API response.
- Request fields not present in UPDCUST copybook are rejected/ignored by contract.

## 11. Non-Functional Requirements
- NFR-001 Maintainability: thin controller, service-owned business logic, constructor injection only.
- NFR-002 Testability: every fail code and gate condition covered by automated tests.
- NFR-003 Observability: correlation ID and legacy fail code captured in logs and responses.
- NFR-004 Backward Safety: existing inquiry/create feature behavior remains unchanged.
- NFR-005 Frontend Resilience: error rendering must be defensive against malformed or unexpected backend error payloads.
- NFR-006 Contract Publication: runtime OpenAPI and feature contract remain synchronized for all UPDCUST update behaviors.
- NFR-007 Endpoint Security: update endpoint remains under active security matcher and role policy coverage.
- NFR-008 Data Integrity: response date fields must use deterministic numeric-to-date mapping and must not copy binary storage bytes into grouped day/month/year contracts.

## 12. Acceptance Criteria
- AC-001 Invalid title returns mapped failure with legacy fail code T.
- AC-002 Missing meaningful update payload (name/address gate) returns fail code 4.
- AC-003 Missing customer returns 404 and legacy fail code 1.
- AC-004 Select failure path maps to fail code 2.
- AC-005 Update failure path maps to fail code 3.
- AC-006 Name/title update occurs only when firstName is non-blank.
- AC-007 Address update occurs only when addressLine1 is non-blank.
- AC-008 Status and phone update only on non-blank input.
- AC-009 DOB updates when year provided.
- AC-010 Success response returns updated customer and legacy success status.
- AC-011 No-op payloads that pass gates preserve legacy parity behavior.
- AC-012 Update action is accessible from inquiry success UI via professional placement.
- AC-012a Update success redirects back to inquiry and shows updated customer values.
- AC-012b Update failure paths do not crash the UI and do not produce blank pages.
- AC-013 Customer number normalization (trim and zero-left-pad) matches legacy lookup behavior.
- AC-014 Status updates accept any non-blank value in parity mode unless stricter mode is explicitly introduced later.
- AC-015 Unauthenticated update requests return 401 and do not execute update logic.
- AC-016 Unauthorized update requests return 403 and do not execute update logic.
- AC-017 Runtime OpenAPI includes UPDCUST update endpoint and 400/401/403/404/422/500 response contracts.
- AC-018 On every successful response, legacy fail code is blank regardless of prior request/internal state.
- AC-019 `creditScoreReviewDate` is returned as a valid ISO date mapped from numeric `yyyymmdd`, not raw byte copy semantics.
- AC-020 SME confirms status-domain policy for parity mode and records decision in feature artifacts.

## 13. Assumptions And Decisions
- Sort code fallback is configuration-driven to preserve legacy SORTCODE behavior.
- Modernization safety profile enforces ISO date parse validity for DOB input and maps invalid dates to HTTP 400.
- UI update flow enters from inquiry result to reduce accidental edits and preserve operational context.
- Legacy COMMAREA behavior that depends on pre-cleared caller memory is not preserved where it harms API determinism.
