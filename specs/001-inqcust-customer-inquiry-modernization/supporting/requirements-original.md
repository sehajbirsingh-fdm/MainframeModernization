# requirements.md - INQCUST Customer Inquiry Modernization

## 1. Business objective
Modernize the legacy CICS COBOL customer inquiry capability implemented by `INQCUST.cbl` into a Spring Boot API while preserving observable customer inquiry behavior.

The POC must show that a junior engineer can use GitHub Copilot plus a precise SDD package to understand legacy COBOL behavior, generate a modern implementation, and validate the result with automated tests.

## 2. Legacy behavior summary
`INQCUST.cbl` receives a CICS commarea based on `INQCUSTZ.cpy`, reads the DB2 `CUSTOMER` table declared in `CUSTDB2.cpy`, and returns customer fields in the same commarea.

It supports three lookup modes:

| Mode | Trigger | Expected behavior |
|---|---|---|
| Specific customer | `customerNumber` is neither `0000000000` nor `9999999999` | Retrieve exact customer by sort code and customer number. |
| Random customer | `customerNumber = 0000000000` | Determine highest customer number, generate a random customer number, retry until found or retry limit reached. |
| Latest customer | `customerNumber = 9999999999` | Return the highest customer number for the sort code. |

## 3. Modern API objective
Expose customer inquiry through:

```http
GET /api/v1/customers/{sortCode}/{customerNumber}
```

## 4. In scope
- Spring Boot API.
- Mock customer repository using JSON data.
- Customer lookup by sort code and customer number.
- Latest customer lookup.
- Random customer lookup with deterministic testability.
- Legacy inquiry success/failure status in response.
- Date conversion from `YYYYMMDD` integer to ISO date.
- Field mapping from COBOL/DB2 to Java/API.
- Risk assessment enhancement using status, credit score, and credit score review date.
- Unit and integration tests.
- OpenAPI contract.

## 5. Out of scope
- Real CICS integration.
- Real DB2 integration.
- Real IMS integration.
- Real z/OS Connect integration.
- Authentication and authorization.
- Full Bank of Z modernization.
- Byte-for-byte DFHCOMMAREA compatibility.

## 6. Definition of done
- API runs locally.
- Swagger/OpenAPI renders.
- Mock data supports all positive and negative paths.
- Tests pass.
- All test cases in `test-spec.md` are implemented.
- Code traces back to the mapping matrix and business rules.
- Demo can show COBOL source, copybooks, SDD artifacts, running API, and tests.
