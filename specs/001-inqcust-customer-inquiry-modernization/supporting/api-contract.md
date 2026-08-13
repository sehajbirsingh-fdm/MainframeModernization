# api-contract.md

## Endpoint
```http
GET /api/v1/customers/{sortCode}/{customerNumber}
```

## Path parameters

| Name | Required | Validation | Example |
|---|---|---|---|
| sortCode | yes | `^[0-9]{6}$` | `123456` |
| customerNumber | yes | `^[0-9]{10}$` | `0000000001` |

## Lookup examples

### Specific customer
```http
GET /api/v1/customers/123456/0000000001
```

### Latest customer
```http
GET /api/v1/customers/123456/9999999999
```

### Random customer
```http
GET /api/v1/customers/123456/0000000000
```

## HTTP status strategy

| Scenario | HTTP status | legacyStatus |
|---|---:|---|
| Customer found | 200 | Y/0 |
| Specific not found | 404 | N/1 |
| Random not found | 404 | N/1 |
| Latest not found | 404 | N/9 |
| Invalid sort code or customer number | 400 | n/a |
| System error | 500 | N/9 where applicable |

Notes:
- For random lookup (`customerNumber=0000000000`), malformed candidate records are skipped during retries and treated as non-matches.
- HTTP 400 is only for request parameter validation failures, not candidate data-quality issues encountered during random retries.

## Headers
No special headers required for POC.

## Security
Authentication is out of scope for the POC. Add placeholder notes in README explaining where authentication would be added in production.
