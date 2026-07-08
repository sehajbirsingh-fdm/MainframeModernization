# ONE FILE COPILOT CONTEXT - INQCUST BUILD TODAY



---

# FILE: PROJECT_CONTEXT.md

# INQCUST Copilot Build Pack - Project Context

## Goal
Build a Spring Boot REST API that modernizes the Bank of Z `INQCUST.cbl` customer inquiry COBOL program.

## Legacy assets used for this specification
- `INQCUST.cbl`: CICS COBOL customer inquiry program.
- `CUSTOMER.cpy`: customer record copybook.
- `INQCUSTZ.cpy`: CICS DFHCOMMAREA request/response copybook.
- `CUSTDB2.cpy`: DB2 CUSTOMER table declaration.

## Build target
- Java 21
- Spring Boot 3
- Maven
- REST/JSON API
- Mock data repository backed by local JSON
- JUnit 5 and Spring MockMvc tests
- OpenAPI/Swagger documentation

## Demo constraint
No mainframe, CICS, IMS, or DB2 runtime is available for this POC. Therefore, the API must use a repository interface with a mock implementation. The repository implementation must be replaceable later by Db2, z/OS Connect, CICS, MQ, or another enterprise adapter.

## Modernization pattern
Use the strangler pattern. Replace one bounded capability, `INQCUST`, while keeping the rest of the mainframe estate unchanged.

## Core API
```http
GET /api/v1/customers/{sortCode}/{customerNumber}
```

## Special customer numbers
- `0000000000`: random customer lookup.
- `9999999999`: latest/highest customer number lookup for the sort code.

## Legacy result fields
- `INQCUST-INQ-SUCCESS`: `Y` or `N`.
- `INQCUST-INQ-FAIL-CD`: `0`, `1`, `2`, or `9`.

## Key rule
The API must preserve legacy inquiry outcome semantics while exposing a modern REST contract.


---

# FILE: .github/copilot-instructions.md

# GitHub Copilot Instructions - INQCUST Modernization

You are building a Spring Boot modernization of a legacy CICS COBOL program called INQCUST.

Follow these rules strictly:

1. Treat `/docs/spec.md` as the source of truth.
2. Do not invent additional fields that are not in the copybooks or spec.
3. Use the exact field mapping in `/docs/mapping-matrix.md`.
4. Implement all acceptance criteria in `/docs/test-spec.md`.
5. Use Java 21 and Spring Boot 3.
6. Use constructor injection only. Do not use field injection.
7. Keep controller thin. Business behavior belongs in service classes.
8. Mock mainframe data using `/mock-data/customer-records.json`.
9. Do not connect to a real database, mainframe, CICS, IMS, or DB2 for the POC.
10. Create interfaces for future integration adapters.
11. Use `CustomerRepository` as the abstraction over the legacy CUSTOMER table.
12. Preserve legacy status behavior in the response using `LegacyInquiryStatus`.
13. Dates from legacy records are numeric `YYYYMMDD` values and must become ISO `yyyy-MM-dd` in JSON.
14. Trim trailing spaces from fixed-width CHAR fields.
15. Add unit tests for every business rule.
16. Add controller tests for every API response status.
17. Do not create frontend code.
18. Do not add Spring Security unless explicitly requested.
19. Do not use Lombok unless the prompt specifically asks for it; plain Java records/classes are preferred for clarity.
20. Ensure the code compiles and tests pass.


---

# FILE: docs/spec.md

# spec.md - INQCUST Customer Inquiry API Specification

## 1. Feature name
INQCUST Customer Inquiry Modernization API.

## 2. Feature summary
Build a Spring Boot REST API that implements the observable behavior of the legacy `INQCUST.cbl` COBOL program using a mock repository instead of live CICS/DB2 connectivity.

The API must support customer lookup by sort code and customer number and preserve legacy inquiry status semantics in the JSON response.

## 3. Source-of-truth legacy assets

### 3.1 COBOL program
`INQCUST.cbl` is a CICS COBOL customer inquiry program.

### 3.2 Customer copybook
`CUSTOMER.cpy` defines:
- `CUSTOMER-RECORD`
- `CUSTOMER-EYECATCHER`
- `CUSTOMER-KEY`
- `CUSTOMER-NAME`
- `CUSTOMER-DOB`
- `CUSTOMER-PHONE`
- `CUSTOMER-ADDRESS`
- `CUSTOMER-STATUS`
- `CUSTOMER-CREATED-DATE`
- `CUSTOMER-CREDIT-SCORE`
- `CUSTOMER-CS-REVIEW-DATE`

### 3.3 CICS commarea copybook
`INQCUSTZ.cpy` defines the inquiry request/response commarea fields.

### 3.4 DB2 DCLGEN
`CUSTDB2.cpy` declares the DB2 `CUSTOMER` table and column types.

## 4. API endpoint

### 4.1 Retrieve customer inquiry
```http
GET /api/v1/customers/{sortCode}/{customerNumber}
```

### 4.2 Path variables

#### sortCode
- Required.
- Exactly 6 digits.
- Regex: `^[0-9]{6}$`.
- Legacy mapping: `INQCUST-SCODE`, `CUSTOMER-SORTCODE`, `CUSTOMER_SORTCODE`.

#### customerNumber
- Required.
- Exactly 10 digits.
- Regex: `^[0-9]{10}$`.
- Legacy mapping: `INQCUST-CUSTNO`, `CUSTOMER-NUMBER`, `CUSTOMER_NUMBER`.
- Special value `0000000000`: random customer lookup.
- Special value `9999999999`: latest customer lookup.

## 5. Response schema

### 5.1 Success response
```json
{
  "legacyStatus": {
    "inquirySuccess": "Y",
    "inquiryFailCode": "0",
    "message": "Customer found"
  },
  "lookupMode": "SPECIFIC",
  "customer": {
    "eyecatcher": "CUST",
    "sortCode": "123456",
    "customerNumber": "0000000001",
    "title": "Mr",
    "firstName": "John",
    "lastName": "Smith",
    "dateOfBirth": "1975-01-01",
    "phone": "4165550101",
    "address": {
      "line1": "1 Main Street",
      "line2": "Suite 100",
      "city": "Toronto",
      "postcode": "M5H2N2",
      "country": "Canada"
    },
    "status": "ACTIVE",
    "createdDate": "2010-06-15",
    "creditScore": 742,
    "creditScoreReviewDate": "2026-01-15"
  },
  "riskAssessment": {
    "riskRating": "LOW",
    "reviewRequired": false,
    "reasons": []
  }
}
```

### 5.2 Not found response
```json
{
  "legacyStatus": {
    "inquirySuccess": "N",
    "inquiryFailCode": "1",
    "message": "Customer not found"
  },
  "lookupMode": "SPECIFIC",
  "customer": null,
  "riskAssessment": null
}
```

### 5.3 Latest customer not found response
```json
{
  "legacyStatus": {
    "inquirySuccess": "N",
    "inquiryFailCode": "9",
    "message": "No latest customer found for sort code"
  },
  "lookupMode": "LATEST",
  "customer": null,
  "riskAssessment": null
}
```

### 5.4 Validation error response
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "customerNumber must be exactly 10 digits",
  "fieldErrors": [
    {
      "field": "customerNumber",
      "message": "must match ^[0-9]{10}$"
    }
  ]
}
```

## 6. Lookup mode resolution

| Condition | lookupMode |
|---|---|
| customerNumber = `0000000000` | RANDOM |
| customerNumber = `9999999999` | LATEST |
| otherwise | SPECIFIC |

## 7. Detailed business rules

### BR-001 Sort code validation
The API must reject requests where `sortCode` is missing, not exactly 6 characters, or contains non-digits.

### BR-002 Customer number validation
The API must reject requests where `customerNumber` is missing, not exactly 10 characters, or contains non-digits.

### BR-003 Specific lookup
For normal customer numbers, the service shall call `CustomerRepository.findBySortCodeAndCustomerNumber(sortCode, customerNumber)`.

### BR-004 Specific customer found
When the repository returns a customer record, return HTTP 200, `inquirySuccess = Y`, `inquiryFailCode = 0`, and mapped customer data.

### BR-005 Specific customer not found
When the repository returns no customer record for specific lookup, return HTTP 404, `inquirySuccess = N`, `inquiryFailCode = 1`, and `customer = null`.

### BR-006 Latest customer lookup
When `customerNumber = 9999999999`, call `CustomerRepository.findLatestBySortCode(sortCode)`.

### BR-007 Latest customer found
When latest customer exists, return HTTP 200, `lookupMode = LATEST`, `inquirySuccess = Y`, `inquiryFailCode = 0`, and the latest customer.

### BR-008 Latest customer not found
When latest customer does not exist for the sort code, return HTTP 404, `lookupMode = LATEST`, `inquirySuccess = N`, `inquiryFailCode = 9`.

### BR-009 Random customer lookup
When `customerNumber = 0000000000`, retrieve latest customer number for the sort code, generate candidate customer numbers between 1 and latest customer number, and attempt lookup until found or retry limit reached.

### BR-010 Random lookup retry limit
Default retry limit is 1000. This value must be configurable via application property `inquiry.random.max-retries`.

### BR-011 Random customer found
When a random candidate exists, return HTTP 200, `lookupMode = RANDOM`, `inquirySuccess = Y`, `inquiryFailCode = 0`, and the found customer.

### BR-012 Random customer not found after retry limit
When no customer is found after retry limit, return HTTP 404, `lookupMode = RANDOM`, `inquirySuccess = N`, `inquiryFailCode = 1`.

### BR-013 Fixed-width CHAR trimming
All DB2 CHAR fields represented in mock data must be trimmed before being returned in JSON.

### BR-014 Date conversion
Legacy numeric dates in `YYYYMMDD` form must be converted to ISO `yyyy-MM-dd` strings. Invalid dates must cause a controlled system error response.

### BR-015 Customer status values
Valid customer status values are `ACTIVE`, `INACTIVE`, and `SUSPENDED`.

### BR-016 Eyecatcher
Expected eyecatcher value is `CUST`. If mock record has another eyecatcher, return the value but add a risk reason `UNEXPECTED_EYECATCHER` in risk assessment. Do not fail the inquiry.

### BR-017 Risk rating enhancement
Risk assessment is a modernization enhancement derived from existing legacy fields. It must not modify legacy behavior.

### BR-018 Risk rating LOW
Risk rating is LOW when:
- customer status is ACTIVE, and
- credit score is greater than or equal to 700, and
- credit score review date is not stale.

### BR-019 Risk rating MEDIUM
Risk rating is MEDIUM when:
- customer status is ACTIVE or INACTIVE, and
- credit score is between 600 and 699 inclusive, and
- no HIGH rule applies.

### BR-020 Risk rating HIGH
Risk rating is HIGH when:
- customer status is SUSPENDED, or
- credit score is less than 600.

### BR-021 Review required
`reviewRequired = true` when credit score review date is older than 12 months from the current system date.

### BR-022 Risk reasons
Risk assessment must include reasons explaining why a risk rating or review flag was assigned. Valid reason codes:
- `LOW_SCORE`
- `SUSPENDED_STATUS`
- `STALE_CREDIT_REVIEW`
- `INACTIVE_STATUS`
- `UNEXPECTED_EYECATCHER`

## 8. Derived fields
Risk assessment fields are new and do not exist in the legacy copybooks:
- `riskRating`
- `reviewRequired`
- `reasons`

## 9. Package structure
Copilot must generate the following structure:

```text
src/main/java/com/fdm/bankofz/customerinquiry/
  CustomerInquiryApplication.java
  controller/CustomerInquiryController.java
  dto/AddressResponse.java
  dto/CustomerResponse.java
  dto/CustomerInquiryResponse.java
  dto/ErrorResponse.java
  dto/FieldErrorResponse.java
  dto/LegacyInquiryStatus.java
  dto/RiskAssessmentResponse.java
  enums/CustomerStatus.java
  enums/LookupMode.java
  enums/RiskRating.java
  exception/CustomerNotFoundException.java
  exception/CustomerInquiryException.java
  exception/GlobalExceptionHandler.java
  mapper/CustomerMapper.java
  model/CustomerRecord.java
  repository/CustomerRepository.java
  repository/MockCustomerRepository.java
  service/CustomerInquiryService.java
  service/LookupModeResolver.java
  service/RandomCustomerSelector.java
  service/RiskAssessmentService.java
  service/LegacyStatusFactory.java
  util/LegacyDateConverter.java
src/main/resources/
  application.yml
  mock-data/customer-records.json
src/test/java/com/fdm/bankofz/customerinquiry/
  controller/CustomerInquiryControllerTest.java
  service/CustomerInquiryServiceTest.java
  service/RiskAssessmentServiceTest.java
  util/LegacyDateConverterTest.java
```

## 10. Non-functional requirements
- Local API response under 200ms using mock data.
- No real mainframe connections.
- No direct repository access from controller.
- 80%+ unit test coverage target for service classes.
- All business rules must have tests.
- OpenAPI must render in Swagger UI.
- Logs must include lookup mode, sort code, customer number, and outcome.

## 11. Implementation constraints
- Use Java 21.
- Use Spring Boot 3.
- Use Maven.
- Use constructor injection.
- Do not use field injection.
- Do not implement frontend.
- Do not connect to DB2.
- Do not use JPA for this POC.
- Do not use random behavior that makes tests flaky. Use an injectable `RandomCustomerSelector`.
- Use immutable DTOs where practical.
- Keep business logic out of controller.


---

# FILE: docs/mapping-matrix.md

# mapping-matrix.md - COBOL to DB2 to Java to API Mapping

| Business field | CUSTOMER.cpy | INQCUSTZ.cpy | CUSTDB2 column | Java model | API field | Notes |
|---|---|---|---|---|---|---|
| Eyecatcher | CUSTOMER-EYECATCHER PIC X(4) | INQCUST-EYE PIC X(4) | CUSTOMER_EYECATCHER CHAR(4) | eyecatcher String | customer.eyecatcher | Expected value `CUST`. |
| Sort code | CUSTOMER-SORTCODE PIC 9(6) DISPLAY | INQCUST-SCODE PIC X(6) | CUSTOMER_SORTCODE CHAR(6) NOT NULL | sortCode String | customer.sortCode | Key field. Must be 6 digits. |
| Customer number | CUSTOMER-NUMBER PIC 9(10) DISPLAY | INQCUST-CUSTNO PIC 9(10) | CUSTOMER_NUMBER CHAR(10) NOT NULL | customerNumber String | customer.customerNumber | Key field. Special values supported at request. |
| Title | CUSTOMER-TITLE PIC X(10) | INQCUST-TITLE PIC X(10) | CUSTOMER_TITLE CHAR(10) | title String | customer.title | Trim trailing spaces. |
| First name | CUSTOMER-FIRST-NAME PIC X(50) | INQCUST-FIRST-NAME PIC X(50) | CUSTOMER_FIRST_NAME CHAR(50) | firstName String | customer.firstName | Trim. |
| Last name | CUSTOMER-LAST-NAME PIC X(50) | INQCUST-LAST-NAME PIC X(50) | CUSTOMER_LAST_NAME CHAR(50) | lastName String | customer.lastName | Trim. |
| Date of birth | CUSTOMER-DOB group DD/MM/YYYY | INQCUST-DOB group DD/MM/YYYY | CUSTOMER_DATE_OF_BIRTH INTEGER | dateOfBirth LocalDate | customer.dateOfBirth | DB2 integer YYYYMMDD. API ISO date. |
| Phone | CUSTOMER-PHONE PIC X(20) | INQCUST-PHONE PIC X(20) | CUSTOMER_PHONE CHAR(20) | phone String | customer.phone | Trim. |
| Address line 1 | CUSTOMER-ADDR-LINE1 PIC X(50) | INQCUST-ADDR-LINE1 PIC X(50) | CUSTOMER_ADDR_LINE1 CHAR(50) | address.line1 String | customer.address.line1 | Trim. |
| Address line 2 | CUSTOMER-ADDR-LINE2 PIC X(50) | INQCUST-ADDR-LINE2 PIC X(50) | CUSTOMER_ADDR_LINE2 CHAR(50) | address.line2 String | customer.address.line2 | Trim. |
| City | CUSTOMER-CITY PIC X(50) | INQCUST-CITY PIC X(50) | CUSTOMER_CITY CHAR(50) | address.city String | customer.address.city | Trim. |
| Postcode | CUSTOMER-POSTCODE PIC X(10) | INQCUST-POSTCODE PIC X(10) | CUSTOMER_POSTCODE CHAR(10) | address.postcode String | customer.address.postcode | Trim. |
| Country | CUSTOMER-COUNTRY PIC X(50) | INQCUST-COUNTRY PIC X(50) | CUSTOMER_COUNTRY CHAR(50) | address.country String | customer.address.country | Trim. |
| Status | CUSTOMER-STATUS PIC X(10) | INQCUST-STATUS PIC X(10) | CUSTOMER_STATUS CHAR(10) | status CustomerStatus | customer.status | ACTIVE, INACTIVE, SUSPENDED. |
| Created date | CUSTOMER-CREATED-DATE group | INQCUST-CREATED-DATE group | CUSTOMER_CREATED_DATE INTEGER | createdDate LocalDate | customer.createdDate | DB2 integer YYYYMMDD. API ISO date. |
| Credit score | CUSTOMER-CREDIT-SCORE PIC 999 | INQCUST-CREDIT-SCORE PIC 999 | CUSTOMER_CREDIT_SCORE SMALLINT | creditScore Integer | customer.creditScore | 0-999. |
| Credit score review date | CUSTOMER-CS-REVIEW-DATE group | INQCUST-CS-REVIEW-DT group | CUSTOMER_CS_REVIEW_DATE INTEGER | creditScoreReviewDate LocalDate | customer.creditScoreReviewDate | DB2 integer YYYYMMDD. API ISO date. |
| Inquiry success | n/a | INQCUST-INQ-SUCCESS PIC X | n/a | legacyStatus.inquirySuccess | legacyStatus.inquirySuccess | Y/N. |
| Inquiry failure code | n/a | INQCUST-INQ-FAIL-CD PIC X | n/a | legacyStatus.inquiryFailCode | legacyStatus.inquiryFailCode | 0/1/2/9. |
| PCB pointer | n/a | INQCUST-PCB-POINTER PIC X(4) | n/a | not exposed | not exposed | Not needed for REST POC. |


---

# FILE: docs/data-dictionary.md

# data-dictionary.md - Customer Inquiry Data Dictionary

## Key structure
A customer is identified by `sortCode + customerNumber`.

## Field definitions

### eyecatcher
- Source: `CUSTOMER_EYECATCHER`, `CUSTOMER-EYECATCHER`, `INQCUST-EYE`.
- Type: string.
- Length: 4.
- Expected value: `CUST`.
- API path: `customer.eyecatcher`.

### sortCode
- Source: `CUSTOMER_SORTCODE`, `CUSTOMER-SORTCODE`, `INQCUST-SCODE`.
- Type: string.
- Length: 6.
- Validation: digits only.
- API path: `customer.sortCode`.

### customerNumber
- Source: `CUSTOMER_NUMBER`, `CUSTOMER-NUMBER`, `INQCUST-CUSTNO`.
- Type: string.
- Length: 10.
- Validation: digits only.
- Special values at request: `0000000000`, `9999999999`.
- API path: `customer.customerNumber`.

### title
- Type: string.
- Max length: 10.
- Fixed-width CHAR in legacy.
- Trim trailing spaces.

### firstName
- Type: string.
- Max length: 50.
- Trim trailing spaces.

### lastName
- Type: string.
- Max length: 50.
- Trim trailing spaces.

### dateOfBirth
- DB2 source: `CUSTOMER_DATE_OF_BIRTH INTEGER`.
- Legacy representation: integer `YYYYMMDD`, then split into day/month/year copybook fields.
- Java type: `LocalDate`.
- API format: ISO date `yyyy-MM-dd`.

### phone
- Type: string.
- Max length: 20.
- Trim trailing spaces.

### address
Address is a nested API object composed of line1, line2, city, postcode, country.

### status
- Legacy values from 88-levels: `ACTIVE`, `INACTIVE`, `SUSPENDED`.
- Java type: `CustomerStatus` enum.
- API type: string enum.

### createdDate
- DB2 source: `CUSTOMER_CREATED_DATE INTEGER`.
- Format: `YYYYMMDD`.
- Java: `LocalDate`.
- API: ISO date.

### creditScore
- DB2 source: `CUSTOMER_CREDIT_SCORE SMALLINT`.
- COBOL: `PIC 999`.
- Java: `Integer`.
- Validation: 0-999.

### creditScoreReviewDate
- DB2 source: `CUSTOMER_CS_REVIEW_DATE INTEGER`.
- Format: `YYYYMMDD`.
- Java: `LocalDate`.
- API: ISO date.

### legacyStatus.inquirySuccess
- Source: `INQCUST-INQ-SUCCESS`.
- Values: `Y` or `N`.

### legacyStatus.inquiryFailCode
- Source: `INQCUST-INQ-FAIL-CD`.
- Values:
  - `0`: success.
  - `1`: customer not found or random lookup failed.
  - `2`: handled CICS storm drain condition in legacy context.
  - `9`: latest customer not found or selected system/database failure path.


---

# FILE: docs/domain-model.md

# domain-model.md

## CustomerRecord
Internal model representing a DB2 CUSTOMER row or mock equivalent.

```java
public record CustomerRecord(
    String eyecatcher,
    String sortCode,
    String customerNumber,
    String title,
    String firstName,
    String lastName,
    Integer dateOfBirth,
    String phone,
    String addressLine1,
    String addressLine2,
    String city,
    String postcode,
    String country,
    String status,
    Integer createdDate,
    Integer creditScore,
    Integer creditScoreReviewDate
) {}
```

## CustomerResponse
External API representation.

```java
public record CustomerResponse(
    String eyecatcher,
    String sortCode,
    String customerNumber,
    String title,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String phone,
    AddressResponse address,
    CustomerStatus status,
    LocalDate createdDate,
    Integer creditScore,
    LocalDate creditScoreReviewDate
) {}
```

## AddressResponse
```java
public record AddressResponse(
    String line1,
    String line2,
    String city,
    String postcode,
    String country
) {}
```

## LegacyInquiryStatus
```java
public record LegacyInquiryStatus(
    String inquirySuccess,
    String inquiryFailCode,
    String message
) {}
```

## RiskAssessmentResponse
```java
public record RiskAssessmentResponse(
    RiskRating riskRating,
    boolean reviewRequired,
    List<String> reasons
) {}
```

## CustomerInquiryResponse
```java
public record CustomerInquiryResponse(
    LegacyInquiryStatus legacyStatus,
    LookupMode lookupMode,
    CustomerResponse customer,
    RiskAssessmentResponse riskAssessment
) {}
```

## Enums

```java
public enum CustomerStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

public enum LookupMode {
    SPECIFIC, RANDOM, LATEST
}

public enum RiskRating {
    LOW, MEDIUM, HIGH
}
```


---

# FILE: docs/architecture.md

# architecture.md

## Current legacy flow

```text
Calling CICS Program
        |
        v
DFHCOMMAREA using INQCUSTZ
        |
        v
INQCUST.cbl
        |
        +--> CUSTDB2 / SQLCA
        |
        +--> DB2 CUSTOMER table
        |
        +--> ABNDPROC for selected error handling
        |
        v
DFHCOMMAREA response
```

## POC target flow

```text
Swagger / Postman / Demo Client
        |
        v
CustomerInquiryController
        |
        v
CustomerInquiryService
        |
        +--> LookupModeResolver
        +--> CustomerRepository interface
        |       |
        |       v
        |   MockCustomerRepository
        |       |
        |       v
        |   mock-data/customer-records.json
        |
        +--> CustomerMapper
        +--> RiskAssessmentService
        +--> LegacyStatusFactory
        |
        v
CustomerInquiryResponse JSON
```

## Future production extension

```text
CustomerRepository interface
        |
        +--> MockCustomerRepository       # POC
        +--> Db2CustomerRepository        # Future direct Db2
        +--> ZosConnectCustomerRepository # Future z/OS Connect
        +--> CicsCustomerRepository       # Future CICS transaction wrapper
```

## Component responsibilities

| Component | Responsibility |
|---|---|
| CustomerInquiryController | HTTP endpoint, validation, delegates to service. |
| CustomerInquiryService | Orchestration and legacy behavior implementation. |
| LookupModeResolver | Converts customerNumber into SPECIFIC/RANDOM/LATEST. |
| CustomerRepository | Data source abstraction. |
| MockCustomerRepository | Loads and queries local JSON data. |
| CustomerMapper | Maps CustomerRecord to CustomerResponse. |
| LegacyDateConverter | Converts YYYYMMDD integers to LocalDate. |
| RiskAssessmentService | Adds modernization enhancement using credit score/status/review date. |
| LegacyStatusFactory | Creates legacy-style success/failure status response. |
| GlobalExceptionHandler | Converts validation/system errors into JSON responses. |


---

# FILE: docs/test-spec.md

# test-spec.md - Test Specification

## Unit tests

### TC-001 Specific customer found
Given mock data contains sort code `123456` and customer number `0000000001`, when service inquiry is executed, then response has:
- lookupMode `SPECIFIC`
- inquirySuccess `Y`
- inquiryFailCode `0`
- customerNumber `0000000001`

### TC-002 Specific customer not found
Given no mock data exists for customer `0000009999`, when service inquiry is executed, then response has:
- lookupMode `SPECIFIC`
- inquirySuccess `N`
- inquiryFailCode `1`
- customer null

### TC-003 Latest customer found
Given customers `0000000001`, `0000000002`, and `0000000005` exist for sort code `123456`, when customer number `9999999999` is requested, then customer `0000000005` is returned.

### TC-004 Latest customer not found
Given no customers exist for sort code `999999`, when customer number `9999999999` is requested, then response has inquirySuccess `N` and fail code `9`.

### TC-005 Random customer found
Given random selector returns a customer number that exists, when customer number `0000000000` is requested, then the matching customer is returned with inquirySuccess `Y`.

### TC-006 Random customer retry failure
Given random selector never returns an existing customer within retry limit, when random lookup is requested, then response has inquirySuccess `N` and fail code `1`.

### TC-007 Date conversion success
Given legacy date integer `19750101`, when converted, then result is `1975-01-01`.

### TC-008 Date conversion failure
Given invalid legacy date integer `19751301`, when converted, then controlled exception is thrown.

### TC-009 LOW risk
Given customer status ACTIVE, credit score 742, and non-stale review date, then risk rating is LOW.

### TC-010 MEDIUM risk
Given customer status ACTIVE and credit score 650, then risk rating is MEDIUM.

### TC-011 HIGH risk by status
Given customer status SUSPENDED, then risk rating is HIGH.

### TC-012 HIGH risk by score
Given credit score 580, then risk rating is HIGH.

### TC-013 Review required
Given credit score review date older than 12 months, then reviewRequired is true and reasons includes `STALE_CREDIT_REVIEW`.

## Controller tests

### CT-001 Valid request returns HTTP 200
GET `/api/v1/customers/123456/0000000001` returns 200 and JSON body contains `legacyStatus.inquirySuccess = Y`.

### CT-002 Invalid sort code returns HTTP 400
GET `/api/v1/customers/ABCDEF/0000000001` returns 400.

### CT-003 Invalid customer number returns HTTP 400
GET `/api/v1/customers/123456/ABC` returns 400.

### CT-004 Not found returns HTTP 404
GET `/api/v1/customers/123456/0000009999` returns 404 and fail code `1`.

### CT-005 Latest request returns HTTP 200
GET `/api/v1/customers/123456/9999999999` returns latest customer.

### CT-006 Random request returns HTTP 200 or 404 based on deterministic selector setup
Test random behavior at service level to avoid flaky controller tests.


---

# FILE: docs/traceability-matrix.md

# traceability-matrix.md

| Requirement / Rule | Source | Code artifact | Test case |
|---|---|---|---|
| Lookup by sort code + customer number | INQCUST.cbl SELECT WHERE CUSTOMER_SORTCODE and CUSTOMER_NUMBER | CustomerRepository.findBySortCodeAndCustomerNumber | TC-001, TC-002, CT-001, CT-004 |
| Random lookup `0000000000` | INQCUST.cbl random customer branch | CustomerInquiryService.randomLookup | TC-005, TC-006 |
| Latest lookup `9999999999` | INQCUST.cbl latest customer branch and GET-LAST-CUSTOMER-DB2 | CustomerRepository.findLatestBySortCode | TC-003, TC-004, CT-005 |
| Success flag Y and fail code 0 | INQCUST-INQ-SUCCESS / INQCUST-INQ-FAIL-CD | LegacyStatusFactory.success | TC-001 |
| Not found fail code 1 | SQLCODE 100 handling | LegacyStatusFactory.notFound | TC-002, TC-006, CT-004 |
| Latest lookup fail code 9 | GET-LAST-CUSTOMER-DB2 SQLCODE 100 handling | LegacyStatusFactory.latestNotFound | TC-004 |
| Date conversion | COMPUTE date year/month/day from YYYYMMDD | LegacyDateConverter | TC-007, TC-008 |
| Status values ACTIVE/INACTIVE/SUSPENDED | CUSTOMER.cpy 88-levels | CustomerStatus enum | TC-009 through TC-012 |
| Credit score | CUSTOMER-CREDIT-SCORE / CUSTOMER_CREDIT_SCORE | CustomerRecord.creditScore | TC-009 through TC-012 |
| Credit score review date | CUSTOMER-CS-REVIEW-DATE / CUSTOMER_CS_REVIEW_DATE | RiskAssessmentService.reviewRequired | TC-013 |
