# Dual Model Analysis: UPDCUST Legacy vs Modern

## Legacy Model
- CICS COBOL program with COMMAREA input/output.
- DB2 select then update.
- Fail-code-driven outcomes in COMM-UPD-FAIL-CD.
- Selective updates based on blank/non-blank gate logic.

## Modern Model
- REST endpoint with structured JSON request/response.
- Service layer enforcing legacy parity rules.
- Repository abstraction replacing direct DB2 runtime dependency in POC.
- Error envelope with legacyFailCode retained for observability.

## Parity-Critical Behaviors
- Title allow-list and fail code T.
- Minimum meaningful payload gate fail code 4.
- Not-found/read/update fail codes 1/2/3.
- Conditional update gates for name, address, phone, status, DOB.

## Intentional Modern Additions
- Correlation ID in API responses.
- HTTP status mapping.
- UI entry point from inquiry success with explicit update action.
