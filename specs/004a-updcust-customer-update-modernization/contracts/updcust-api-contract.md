# UPDCUST API Contract (Specification)

## Endpoint
PUT /api/v1/customers/{customerNumber}

Optional query:
- sortCode (6-digit)

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

## Fail-Code Mapping
- T: invalid title
- 1: customer not found
- 2: read/select failure
- 3: update/write failure
- 4: insufficient meaningful payload
