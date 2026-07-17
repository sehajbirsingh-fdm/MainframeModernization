# Data Model: INQACC Account Inquiry Modernization

## Entity: AccountInquiryRequest
- Purpose: Represents inbound inquiry identifiers.
- Fields:
  - sortcode: string, required, regex `^\\d{6}$`
  - accountNumber: string, required, regex `^\\d{8}$`
- Validation rules:
  - Both fields required and numeric with exact lengths.
  - Invalid values produce validation error response.

## Entity: AccountRecord
- Purpose: Canonical account data returned for successful inquiry.
- Fields:
  - eyecatcher: string
  - customerNumber: string
  - sortcode: string
  - accountNumber: string
  - accountType: string
  - accountStatus: string
  - accountName: string
  - accountBalance: string (decimal representation)
  - accountCurrency: string
  - accountOpenDate: string (ISO date)
  - accountManager: string
  - lastTransactionDate: string (ISO date)
- Validation/format constraints:
  - Date fields are serialized as `yyyy-MM-dd`.
  - Numeric-like values preserve expected formatting from source contract.

## Entity: ErrorResponse
- Purpose: Standardized error payload for all non-2xx outcomes.
- Fields:
  - correlationId: string
  - error:
    - code: string
    - message: string
    - details: array (optional per error type)
  - timestamp: string (ISO timestamp)
- Status mappings:
  - 400 validation failure
  - 401 unauthorized
  - 403 forbidden
  - 404 account not found
  - 500 internal server error
  - 503 service unavailable

## Entity: CorrelationContext
- Purpose: Request tracing context propagated through request lifecycle.
- Fields:
  - correlationId: string (UUID preferred)
  - requestTimestamp: datetime
- Rules:
  - If header missing, system generates correlation ID.
  - Correlation ID is echoed in response headers and payload.

## Relationships
- `AccountInquiryRequest` -> lookup in repository -> `AccountRecord` or `ErrorResponse`.
- `CorrelationContext` is associated with all request outcomes.

## State Transitions
- Request lifecycle:
  - RECEIVED -> VALIDATED -> AUTHORIZED -> LOOKUP_EXECUTED -> RESPONSE_RETURNED
- Failure transitions:
  - RECEIVED -> VALIDATION_FAILED -> RESPONSE_RETURNED
  - VALIDATED -> AUTH_FAILED -> RESPONSE_RETURNED
  - AUTHORIZED -> LOOKUP_FAILED_NOT_FOUND -> RESPONSE_RETURNED
  - AUTHORIZED -> LOOKUP_FAILED_SYSTEM -> RESPONSE_RETURNED
