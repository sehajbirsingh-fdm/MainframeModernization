# Data Model: CRECUST Customer Create Modernization

## Entity: CreateCustomerRequest
- Purpose: Inbound customer-creation payload constrained by `CRECUST.cpy`.
- Fields:
  - title: string (allowed whitelist)
  - firstName: string
  - lastName: string
  - dateOfBirth: object
    - day: integer (1-31)
    - month: integer (1-12)
    - year: integer (>=1601)
  - createdDate: object
    - day: integer (1-31)
    - month: integer (1-12)
    - year: integer (>=1601)
  - phone: string
  - address: object
    - line1: string
    - line2: string
    - city: string
    - postcode: string
    - country: string
  - status: string (max 10 chars, passed through in parity mode)

## Entity: CustomerRecord
- Purpose: Canonical persisted customer aligned to `CUSTOMER.cpy` and `CUSTDB2.cpy`.
- Fields:
  - eyecatcher: string (`CUST`)
  - sortCode: string (6-digit, system-owned)
  - customerNumber: string (10-digit, generated)
  - title: string
  - firstName: string
  - lastName: string
  - dateOfBirth: integer (`YYYYMMDD`)
  - phone: string
  - addressLine1: string
  - addressLine2: string
  - city: string
  - postcode: string
  - country: string
  - status: string
  - createdDate: integer (`YYYYMMDD`)
  - creditScore: integer (0-999)
  - creditScoreReviewDate: integer (`YYYYMMDD`)

## Entity: CreateCustomerResponse
- Purpose: API response for successful create.
- Fields:
  - eyecatcher: string
  - sortCode: string
  - customerNumber: string
  - title: string
  - firstName: string
  - lastName: string
  - dateOfBirth: string (ISO `yyyy-MM-dd`)
  - phone: string
  - addressLine1: string
  - addressLine2: string
  - city: string
  - postcode: string
  - country: string
  - status: string
  - createdDate: string (ISO `yyyy-MM-dd`)
  - creditScore: integer
  - creditScoreReviewDate: string (ISO `yyyy-MM-dd`)
  - legacyStatus:
    - commSuccess: string (`Y` or `N`)
    - commFailCode: string (blank or legacy fail code)

## Entity: ErrorResponse
- Purpose: Standardized non-2xx payload.
- Fields:
  - error:
    - code: string
    - message: string
    - legacyFailCode: string (optional)
    - correlationId: string
    - timestamp: string (ISO date-time)

## Entity: CustomerControlState
- Purpose: Sortcode-scoped customer-number sequence state (mocked CONTROL-table equivalent).
- Fields:
  - controlName: string (`BANKZCUST` + sortcode + padding)
  - controlValueNum: integer

## Relationships
- `CreateCustomerRequest` -> validation -> service orchestration.
- Service -> `CustomerControlState` increment -> `CustomerRecord` creation -> `CreateCustomerResponse`.
- Any failure -> `ErrorResponse` + legacy status mapping.

## Transformations
- Input DOB components -> numeric `YYYYMMDD` for persistence.
- Numeric dates -> ISO `yyyy-MM-dd` on response.
- Fixed-width strings are trimmed for API output.
