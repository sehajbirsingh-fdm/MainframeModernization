# Quickstart: Validate INQACC Account Inquiry Plan

This guide validates the feature behavior described in `spec.md` using the contract in `contracts/openapi.yaml` and entities in `data-model.md`.

## Prerequisites
- Java 21 installed
- Build tool configured for the backend module (Maven or Gradle, depending on implementation path)
- Mock account data available for inquiry scenarios
- OAuth2 test token strategy available for secured endpoint checks

## Setup
1. Ensure active feature context points to `specs/002-inqacc-account-inquiry-modernization`.
2. Open the contract file `contracts/openapi.yaml` and confirm endpoint/path matches the spec.
3. Prepare one known-valid `sortcode/accountNumber` pair and one known-invalid pair.

## Validation Scenarios

### Scenario A: Successful Account Inquiry
1. Send GET request to `/v1/accounts/{sortcode}/{accountNumber}` with valid token and known record.
2. Expect HTTP 200 response.
3. Verify payload structure matches `AccountRecord` from `data-model.md`.
4. Verify `X-Correlation-ID` exists and matches body correlation identifier.

### Scenario B: Validation Failure
1. Send request with malformed sortcode or account number.
2. Expect HTTP 400 response.
3. Verify standardized error payload with code/message and correlation identifier.

### Scenario C: Unauthorized/Forbidden Access
1. Send request without token.
2. Expect HTTP 401.
3. Send request with token lacking required role/scope.
4. Expect HTTP 403.

### Scenario D: Not Found
1. Send request with valid format but non-existent composite key.
2. Expect HTTP 404.
3. Verify standardized error payload.

### Scenario E: System Failure Path
1. Simulate repository/system failure (test-mode fault injection).
2. Expect HTTP 500 or HTTP 503 based on failure category.
3. Verify no internal stack details leak to response body.

## Expected Outcomes
- All status codes and response payloads align with `spec.md` and `contracts/openapi.yaml`.
- Request handling is traceable via correlation identifiers.
- Contract and data-model conformance is maintained across success and failure paths.

## Notes
- This quickstart is for validation and readiness checks only.
- Detailed implementation tasks belong in `tasks.md` generation phase.
