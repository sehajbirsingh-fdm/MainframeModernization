```markdown
# Business Rules – INQACC Account Inquiry Modernization

**Document ID:** `business-rules.md`  
**Pipeline:** mainframe_modernization  
**Target System:** Spring Boot 3.3.x + React 18.x (mock persistence POC)  
**Authority:** INQACC.cbl + ACCDB2.cpy + ACCOUNT.cpy + INQACC.cpy + INQACCCZ.cpy + requirements.md + spec.md  
**Status:** Implementation-ready canonical business rules  
**Generated:** 2024

---

## Rule Catalog

---

### BR-001: Account Record Lookup by Composite Key

**Rule ID:** `BR-001`  
**Category:** Data Retrieval  
**Statement:** The system must retrieve account records from the datastore using composite key ACCOUNT_SORTCODE and ACCOUNT_NUMBER.

**Trigger Conditions:**
- An account inquiry request is received with valid sortcode and account number values.
- Both sortcode and account number values are provided and non-empty.
- User has been authenticated and authorized for account inquiry (role: `ACCOUNT_INQUIRER` or above).

**Inputs:**
- `sortcode` (CHAR(6), NOT NULL) – Bank sort code identifier; format: 6 numeric digits.
- `accountNumber` (CHAR(8), NOT NULL) – Account number identifier; format: 8 numeric digits.

**Processing Logic:**
1. Validate sortcode format (numeric, exactly 6 digits; reject if non-numeric or length ≠ 6).
2. Validate account number format (numeric, exactly 8 digits; reject if non-numeric or length ≠ 8).
3. If `accountNumber = 99999999`, execute reserved-number branch: select highest `ACCOUNT_NUMBER` for supplied `ACCOUNT_SORTCODE`.
4. Otherwise execute normal lookup: `ACCOUNT_SORTCODE = sortcode AND ACCOUNT_NUMBER = accountNumber`.
5. Return single matching record if found; return not-found condition if no match exists.
6. Propagate correlation ID through all downstream calls.

**Outputs:**
- Account details including:
  - Account Eyecatcher (HV-ACCOUNT-EYECATCHER)
  - Customer Number (HV-ACCOUNT-CUST-NO)
  - Sort Code (HV-ACCOUNT-SORTCODE)
  - Account Number (HV-ACCOUNT-ACC-NO)
  - Account Type (HV-ACCOUNT-ACC-TYPE)
  - Interest Rate (HV-ACCOUNT-INT-RATE)
  - Opened Date (HV-ACCOUNT-OPENED)
  - Overdraft Limit (HV-ACCOUNT-OVERDRAFT-LIM)
  - Last Statement Date (HV-ACCOUNT-LAST-STMT)
  - Next Statement Date (HV-ACCOUNT-NEXT-STMT)
  - Available Balance (HV-ACCOUNT-AVAIL-BAL)
  - Actual Balance (HV-ACCOUNT-ACTUAL-BAL)
- Not-found indicator (HTTP 404 status code) if no matching record exists.
- Correlation ID attached to response header and structured log entry.

**Error Conditions:**
- `ERR-001`: Sortcode format invalid (non-numeric or length ≠ 6) → Return HTTP 400 Bad Request with validation error detail.
- `ERR-002`: Account number format invalid (non-numeric or length ≠ 8) → Return HTTP 400 Bad Request with validation error detail.
- `ERR-003`: Repository/service unavailable → Return HTTP 503 Service Unavailable with correlation ID.
- `ERR-004`: SQL execution error → Return HTTP 500 Internal Server Error with correlation ID.
- `ERR-005`: No matching record found (zero rows returned) → Return HTTP 404 Not Found.
- `ERR-006`: Authentication token missing or invalid → Return HTTP 401 Unauthorized.
- `ERR-007`: User lacks `ACCOUNT_INQUIRER` role → Return HTTP 403 Forbidden.

**Legacy Mapping:**
- Corresponds to INQACC.cbl lines 52–70 (EXEC SQL DECLARE ACC-CURSOR … WHERE clause).
- Corresponds to reserved-number branch in INQACC.cbl where `INQACC-ACCNO = 99999999` performs `READ-ACCOUNT-LAST` and selects `ORDER BY ACCOUNT_NUMBER DESC FETCH FIRST 1 ROWS ONLY`.
- Maps to COBOL host variables: `HV-ACCOUNT-SORTCODE`, `HV-ACCOUNT-ACC-NO`.
- Preserves DB2 composite-key semantics from ACCDB2.cpy table definition.

**Modernization Notes:**
- REST GET endpoint replaces CICS terminal transaction entry.
- Endpoint remains `GET /v1/accounts/{sortcode}/{accountNumber}` for both normal and reserved-number lookup behavior.
- `accountType` is returned account data and is never part of lookup predicate.
- Parameterized SQL prevents injection; input validation at boundary.
- Correlation ID propagation enables distributed tracing.

---

### BR-002: Sortcode Format Validation

**Rule ID:** `BR-002`  
**Category:** Input Validation  
**Statement:** Sortcode shall be validated as exactly 6 numeric digits before execution of account lookup.

**Trigger Conditions:**
- Sortcode parameter received in HTTP GET request path.

**Inputs:**
- `sortcode` (string from HTTP path parameter).

**Processing Logic:**
1. Check length: must be exactly 6 characters.
2. Check numeric content: all 6 characters must be digits (0–9).
3. If validation fails, immediately return error condition.
4. If validation succeeds, pass sortcode to BR-001 (lookup).

**Outputs:**
- Validation pass/fail indicator.
- On pass: sortcode accepted for lookup processing.
- On fail: error response with validation detail.

**Error Conditions:**
- `ERR-001`: Length not 6 → HTTP 400 Bad Request; error detail: `"sortcode must be exactly 6 digits"`.
- `ERR-002`: Non-numeric characters present → HTTP 400 Bad Request; error detail: `"sortcode must contain only numeric digits"`.

**Legacy Mapping:**
- Corresponds to COBOL input validation in INQACC.cbl (implicit in transaction entry; formalized in modernization).
- ACCDB2.cpy defines ACCOUNT_SORTCODE as CHAR(6) NOT NULL; validation enforces DB2 constraint at application layer.

**Modernization Notes:**
- Validation occurs at REST controller boundary (before service layer invocation).
- OpenAPI schema specifies pattern: `^\d{6}$`.
- Validation error is user-facing (client corrects input); distinct from backend errors.

---

### BR-003: Account Number Format Validation

**Rule ID:** `BR-003`  
**Category:** Input Validation  
**Statement:** Account number shall be validated as exactly 8 numeric digits before execution of account lookup.

**Trigger Conditions:**
- Account number parameter received in HTTP GET request path.

**Inputs:**
- `accountNumber` (string from HTTP path parameter).

**Processing Logic:**
1. Check length: must be exactly 8 characters.
2. Check numeric content: all 8 characters must be digits (0–9).
3. If validation fails, immediately return error condition.
4. If validation succeeds, pass account number to BR-001 (lookup).

**Outputs:**
- Validation pass/fail indicator.
- On pass: account number accepted for lookup processing.
- On fail: error response with validation detail.

**Error Conditions:**
- `ERR-001`: Length not 8 → HTTP 400 Bad Request; error detail: `"accountNumber must be exactly 8 digits"`.
- `ERR-002`: Non-numeric characters present → HTTP 400 Bad Request; error detail: `"accountNumber must contain only numeric digits"`.

**Legacy Mapping:**
- Corresponds to COBOL input validation in INQACC.cbl (implicit in transaction entry; formalized in modernization).
- ACCDB2.cpy defines ACCOUNT_NUMBER as CHAR(8) NOT NULL; validation enforces DB2 constraint at application layer.
- ACCOUNT.cpy structures account number (PIC 9(8)) confirming 8-digit numeric expectation.

**Modernization Notes:**
- Validation occurs at REST controller boundary.
- OpenAPI schema specifies pattern: `^\d{8}$`.
- Distinct from backend database error handling (BR-001 error conditions).

---

### BR-004: OAuth2 Bearer Token Authentication

**Rule ID:** `BR-004`  
**Category:** Security & Authentication  
**Statement:** All account inquiry requests shall require a valid OAuth2 bearer token in the Authorization header.

**Trigger Conditions:**
- HTTP GET request received at `/v1/accounts/{sortcode}/{accountNumber}`.
- Authorization header is evaluated before request processing continues.

**Inputs:**
- Authorization header value (format: `Bearer <JWT token>`).

**Processing Logic:**
1. Extract Authorization header from HTTP request.
2. If header missing → reject with HTTP 401 Unauthorized.
3. If header does not start with "Bearer " → reject with HTTP 401 Unauthorized.
4. Extract JWT token from header.
5. Validate token authenticity.
6. Validate token has not expired.
7. Verify token grants account inquiry access.
8. On success: pass authenticated principal to BR-005 (authorization check).
9. On failure: return HTTP 401 Unauthorized.

**Outputs:**
- Authenticated principal context with inquiry permission.
- Request proceeds to authorization check (BR-005).
- Or: HTTP 401 Unauthorized rejection.

**Error Conditions:**
- `ERR-006`: Authorization header missing → HTTP 401 Unauthorized; error detail: `"Authorization header required"`.
- `ERR-006`: Authorization header malformed (missing "Bearer " prefix) → HTTP 401 Unauthorized; error detail: `"Invalid Authorization header format"`.
- `ERR-006`: JWT token invalid (signature mismatch) → HTTP 401 Unauthorized; error detail: `"Invalid or expired token"`.
- `ERR-006`: JWT token expired → HTTP 401 Unauthorized; error detail: `"Token expired"`.

**Legacy Mapping:**
- INQACC.cbl runs in CICS transaction environment; CICS implicitly provides user identity via transaction server.
- Modernization makes authentication explicit via OAuth2 bearer tokens (cloud-native pattern).
- No legacy CICS-equivalent token validation; added as security enhancement.

**Modernization Notes:**
- Authentication is mandatory for account inquiry requests.
- Correlation ID is propagated through authentication failure responses.

---

### BR-005: Role-Based Authorization for Account Inquiry

**Rule ID:** `BR-005`  
**Category:** Security & Authorization  
**Statement:** Only authenticated users with the `ACCOUNT_INQUIRER` role (or higher privilege) shall be authorized to execute account inquiry.

**Trigger Conditions:**
- After successful authentication (BR-004).
- Before account inquiry service invocation.

**Inputs:**
- Authenticated principal context containing permissions.

**Processing Logic:**
1. Extract account inquiry permission from authenticated principal context.
2. Check if account inquiry permission is present.
3. If role present → authorization granted; proceed to BR-001 (lookup).
4. If role missing → authorization denied; return HTTP 403 Forbidden.

**Outputs:**
- Authorization pass/fail indicator.
- On pass: request proceeds to account inquiry service.
- On fail: HTTP 403 Forbidden with error detail.

**Error Conditions:**
- `ERR-007`: User lacks `ACCOUNT_INQUIRER` role → HTTP 403 Forbidden; error detail: `"User does not have permission to access this resource"`.

**Legacy Mapping:**
- INQACC.cbl runs in CICS transaction environment; CICS resource control enforces transaction access at system level.
- Modernization makes authorization explicit at application layer via role-based access control.
- Legacy transaction code (`INQACC`) implicitly restricts who can invoke; modernization uses explicit API authorization checks.

**Modernization Notes:**
- Authorization failure returns HTTP 403 (not 401); client has authenticated but is not permitted.
- Correlation ID is propagated through authorization failure responses.

---

### BR-006: Correlation ID Propagation

**Rule ID:** `BR-006`  
**Category:** Observability & Tracing  
**Statement:** Every account inquiry request shall be associated with a unique correlation ID that is propagated through all service layers, repository access, and logging; correlation ID shall be returned in response headers and structured logs.

**Trigger Conditions:**
- HTTP GET request received at `/v1/accounts/{sortcode}/{accountNumber}`.
- On request entry to REST controller.

**Inputs:**
- Optional X-Correlation-ID header in HTTP request (format: UUID v4).
- If not provided, system generates a new UUID.

**Processing Logic:**
1. Check for X-Correlation-ID header in incoming request.
2. If present and valid UUID format → use as correlation ID.
3. If missing or invalid → generate a new correlation ID.
4. Store correlation ID in thread-local context (MDC – Mapped Diagnostic Context).
5. Pass correlation ID to all downstream service calls (repository, logging).
6. Include correlation ID in all structured log entries.
7. Return correlation ID in response header (X-Correlation-ID).
8. Include correlation ID in error responses (JSON error envelope).

**Outputs:**
- Correlation ID in response X-Correlation-ID header.
- Correlation ID in all structured JSON log entries.
- Correlation ID in error response payload.

**Error Conditions:**
- No error condition; correlation ID generation is failsafe (always produces valid UUID).

**Legacy Mapping:**
- INQACC.cbl does not explicitly propagate correlation IDs (legacy CICS environment).
- Modernization adds correlation ID as operational enhancement for distributed tracing and debugging.

**Modernization Notes:**
- Correlation ID supports request traceability across API processing and logs.

---

### BR-007: Structured JSON Logging with Request Context

**Rule ID:** `BR-007`  
**Category:** Observability & Logging  
**Statement:** All account inquiry operations shall generate structured log entries containing operational metadata (correlation ID, request path/template, HTTP status, duration, event type).

**Trigger Conditions:**
- At request entry (controller).
- At service layer invocation.
- At repository layer invocation.
- At response exit (controller).
- On error occurrence.

**Inputs:**
- HTTP request metadata (method, path/template).
- Response status code and timing.

**Processing Logic:**
1. On request entry: log `{timestamp, correlationId, method, pathTemplate, event}`.
2. On lookup result: log `{timestamp, correlationId, statusCode, durationMs, event}`.
3. On error: log `{timestamp, correlationId, statusCode, errorCode, event}`.
4. Do not log bearer tokens, account numbers, customer numbers, balances, or full account payloads.

**Outputs:**
- Structured JSON log entries (one per major operation step).
- All logs include timestamp (ISO 8601), correlationId, and event identifier.

**Error Conditions:**
- No error condition; logging is non-blocking and failsafe.

**Legacy Mapping:**
- INQACC.cbl uses CICS logging (syslog or transaction server audit trail).
- Modernization uses structured JSON logging for cloud-native observability integration.

**Modernization Notes:**
- Structured logs are required for operational visibility and auditability.
- Correlation ID must be included in each operational event.

---

### BR-008: Error Response Standardization

**Rule ID:** `BR-008`  
**Category:** API Contract & Error Handling  
**Statement:** All error responses shall return a standardized JSON error envelope containing error code, error message, HTTP status code, correlation ID, and timestamp.
**Statement:** All error responses shall return a standardized JSON error envelope containing `error.code`, `error.message`, `error.correlationId`, and `error.timestamp`.

**Trigger Conditions:**
- Any error condition (validation failure, authentication failure, not-found, backend error).

**Inputs:**
- Error type (validation, authentication, authorization, not-found, backend).
- Error code (ERR-001 through ERR-007, defined in BR-001 through BR-007).
- Error message (human-readable description).
- HTTP status code (4xx for client errors, 5xx for server errors).
- Correlation ID (from BR-006).

**Processing Logic:**
1. Capture error type and code.
2. Map error to HTTP status code:
   - ERR-001, ERR-002 (validation) → HTTP 400 Bad Request.
   - ERR-006 (authentication) → HTTP 401 Unauthorized.
   - ERR-007 (authorization) → HTTP 403 Forbidden.
   - ERR-005 (not-found) → HTTP 404 Not Found.
   - ERR-003, ERR-004 (backend) → HTTP 500 or 503 depending on error type.
3. Build JSON error envelope with: `{error: {code, message, correlationId, timestamp}}`.
4. Return error envelope with corresponding HTTP status code.
5. Log error entry (see BR-007).

**Outputs:**
- JSON error response in HTTP body.
```