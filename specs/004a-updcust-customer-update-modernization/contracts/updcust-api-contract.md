# UPDCUST API Contract (Specification)

## Endpoint
PUT /api/v1/customers/{customerNumber}

Optional query:
- sortCode (6-digit)

Security:
- Requires authenticated caller.
- 401 returned when authentication is missing/invalid.
- 403 returned when caller lacks required authorization.

## Request Body
{
  "title": "Mr",
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "1990-01-01",
  "phoneNumber": "4165550101",
  "address": {
    "addressLine1": "1 Main Street",
    "addressLine2": "",
    "city": "Toronto",
    "postalCode": "M5H2N2",
    "country": "Canada"
  },
  "customerStatus": "ACTIVE"
}

## Success Response (200)
{
  "customerNumber": "0000000006",
  "sortCode": "123456",
  "title": "Mr",
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "1990-01-01",
  "phoneNumber": "4165550101",
  "address": {
    "addressLine1": "1 Main Street",
    "addressLine2": "",
    "city": "Toronto",
    "postalCode": "M5H2N2",
    "country": "Canada"
  },
  "customerStatus": "ACTIVE",
  "createdDate": "2026-07-22",
  "creditScore": 712,
  "creditScoreReviewDate": "2026-08-05",
  "legacyStatus": {
    "updSuccess": "Y",
    "updFailCode": " "
  }
}

Success invariants:
- `legacyStatus.updFailCode` is explicitly blanked on every success response.
- `creditScoreReviewDate` is computed from numeric `yyyymmdd` semantics and returned as valid ISO date.

## Error Response
{
  "error": {
    "code": "UPDCUST-VAL-001",
    "message": "Title is invalid.",
    "legacyFailCode": "T",
    "correlationId": "uuid",
    "timestamp": "2026-07-30T12:00:00Z"
  }
}

## Response Status Codes
- 200: update success
- 400: request validation failure (including invalid DOB format or calendar-invalid ISO date)
- 401: unauthenticated
- 403: unauthorized
- 404: customer not found (legacy fail 1)
- 422: legacy business-rule failure (legacy fail T or 4)
- 500: internal read/update failure (legacy fail 2 or 3 when applicable)

## Fail-Code Mapping
- T: invalid title
- 1: customer not found
- 2: read/select failure
- 3: update/write failure
- 4: insufficient meaningful payload

## Domain Policy Note
- In strict parity mode, status accepts any non-blank value (no allow-list check).
- CUSTOMER copybook 88-level values (`ACTIVE`, `INACTIVE`, `SUSPENDED`) imply a constrained domain; SME decision is required before production hardening.
