# Traceability Matrix

**Document ID:** `traceability-matrix-inqacccu-001`  
**Pipeline:** `mainframe_modernization`  
**Purpose:** Map legacy artifacts through business rules, requirements, specification, and test cases  
**Last Updated:** 2026-07-23  

---

## 1. Legacy Artifact to Modern Component Mapping

### 1.1 Program and Transaction Mapping

| Legacy Artifact         | Legacy Type          | Modern Component                     | Modern Type                     | Mapping Rationale                     |
|-------------------------|----------------------|--------------------------------------|----------------------------------|---------------------------------------|
| `src/base/cics/cobol/INQACCCU.cbl`   | COBOL CICS Program    | `com.bankofz.inqcust.api.inqacccu.controller.AccountRelationshipController`         | Spring Boot REST Controller      | CICS inquiry entrypoint mapped to REST inquiry endpoint      |
| `src/base/cics/cobol/INQACCCU.cbl`   | CICS Online Inquiry    | `GET /api/v1/customers/{customerNumber}/accounts` | HTTP REST Endpoint               | Transaction-style inquiry exposed as read-only HTTP GET |
| `src/base/cics/cobol/INQACCCU.cbl`   | COBOL Program Logic    | `com.bankofz.inqcust.api.inqacccu.service.AccountRelationshipService`             | Spring Service Layer             | Business outcome orchestration in service layer           |

### 1.2 Copybook-to-DTO Mapping

| Legacy Copybook         | Legacy Structure      | Modern Java Class                   | Field Mapping                    | Notes                                  |
|-------------------------|----------------------|--------------------------------------|----------------------------------|----------------------------------------|
| `copybooks/INQACCCUZ.cpy` | Communication Area   | `com.bankofz.inqcust.api.inqacccu.controller.AccountRelationshipController`           | CUSTOMER-NUMBER → path variable `customerNumber`     | Input binding via `@PathVariable` + `@Pattern`                          |
| `copybooks/INQACCCUZ.cpy` | Communication Area   | `com.bankofz.inqcust.api.inqacccu.domain.LegacyStatus`          | COMM-SUCCESS/COMM-FAIL-CODE/CUSTOMER-FOUND → `success`/`failCode`/`customerFound`    | Legacy status projection into API response                         |
| `copybooks/INQACCCUZ.cpy` | Communication Area   | `com.bankofz.inqcust.api.inqacccu.domain.AccountsList`          | NUMBER-OF-ACCOUNTS → `count` | Count exposed with account collection              |
| `copybooks/INQACCCUZ.cpy` | ACCOUNT-DETAILS (OCCURS 1 TO 20) | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary[]`               | Repeating group → JSON array in `accounts.accounts`      | Cardinality preserved by data and mapping |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-DATA structure | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary`                    | Selected account fields mapped for API output              | Mapped from `AccountProjection` via `AccountRelationshipMapper`                  |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-CUST-NO       | `com.bankofz.inqcust.api.inqacccu.domain.CustomerSummary.customerNumber`    | PIC 9(10) → String               | Leading zeroes preserved by string representation              |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-SORT-CODE     | `com.bankofz.inqcust.api.inqacccu.domain.CustomerSummary.sortCode` / `AccountSummary.sortCode`          | PIC 9(6) → String                | Returned from mock data repository                        |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-NUMBER        | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.accountNumber`     | PIC 9(8) → String                | Returned as externally visible identifier                        |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-TYPE          | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.accountType`       | PIC X(8) → String                | Account type plus description in runtime model           |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-INTEREST-RATE | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.interestRate`      | packed numeric → BigDecimal         | Decimal interest rate retained         |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-OVERDRAFT-LIMIT | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.overdraftLimit`   | numeric → Integer                  | Integer overdraft limit               |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-LAST-STMT-DATE | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.lastStatementDate` | numeric `YYYYMMDD` → ISO date string             | Converted by `DateMapper`                       |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-NEXT-STMT-DATE | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.nextStatementDate` | numeric `YYYYMMDD` → ISO date string             | Converted by `DateMapper`                       |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-AVAILABLE-BALANCE | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.availableBalance` | signed numeric → BigDecimal       | Signed amount preserved               |
| `copybooks/ACCOUNT.cpy`  | ACCOUNT-ACTUAL-BALANCE | `com.bankofz.inqcust.api.inqacccu.domain.AccountSummary.actualBalance`     | signed numeric → BigDecimal       | Signed amount preserved               |
| `copybooks/ACCDB2.cpy`   | SQL DECLARE ACCOUNT   | `com.bankofz.inqcust.api.inqacccu.repository.AccountRelationshipRepository`                | `findByCustomerNumber(String)` abstraction         | Implemented with mock JSON adapter |

### 1.3 Data Store Access Mapping

| Legacy Method            | Legacy Technology     | Modern Method                       | Modern Technology                | Migration Path                       |
|-------------------------|----------------------|--------------------------------------|----------------------------------|--------------------------------------|
| DLI/DB2 read inquiry calls       | CICS/DB2             | `AccountRelationshipRepository.findByCustomerNumber()` | Repository interface + JSON adapter          | Mock for POC; adapter boundary retained for future integration |
| COBOL data retrieval and output shaping           | COBOL program flow       | `AccountRelationshipService.inquire()` + `AccountRelationshipMapper`       | Spring service + mapper components         | Service orchestration and mapping separation         |
| Static data for local verification     | Mainframe datasets / DB rows | `JsonAccountRelationshipRepository.readAll()`            | `ObjectMapper` over `mock-data/account-relationship-records.json`         | Deterministic mock data source for local/CI    |

---

## 2. Requirement to Spec to Test Traceability

### 2.1 Functional Requirements Traceability

| Requirement ID | Requirement Description                                           | Spec Reference                          | Spec ID     | Implementation Component                | Test Case ID |
|----------------|------------------------------------------------------------------|-----------------------------------------|-------------|-----------------------------------------|---------------|
| FR-001         | Accept a customer-number inquiry as the business inquiry key. | Request parameters + Validation rules            | AC-011    | `AccountRelationshipController.inquire(String customerNumber)` with `@Pattern("^[0-9]{10}$")`    | `AccountRelationshipControllerTest#shouldReturnBadRequestForInvalidCustomerNumber`; `InqacccuOpenApiConformanceTest#invalidInputShouldReturnValidationErrorShape`   |
| FR-002         | Perform customer validation before account retrieval. | Business behavior (customer validation sequence)                    | AC-004    | No explicit INQCUST-equivalent validation adapter present in current implementation; repository query directly drives found/not-found outcome    | No automated evidence in current codebase   |
| FR-003         | Treat reserved customer numbers 0000000000 and 9999999999 as customer-not-found outcomes. | Validation rules + Outcome behavior                    | AC-002, AC-003  | Behavior is represented as business not-found through not-found mapping (`LegacyStatus N/1001/N`) | `InqacccuOpenApiConformanceTest#businessNotFoundShouldReturn200WithLegacyStatusN`; `validation.test.ts#accepts 10-digit customer numbers including reserved values`   |
| FR-004         | Derive sort code internally as fixed value 987654 and not from caller input. | Business behavior                    | AC-001, AC-013    | Endpoint does not accept sort code input; repository returns sort code from mock record; no explicit fixed-987654 derivation component | No automated evidence for fixed `987654` derivation in current codebase |
| FR-005         | Retrieve customer-associated account data in read-only inquiry mode. | Business behavior                    | AC-001    | `AccountRelationshipRepository` + `JsonAccountRelationshipRepository` read-only lookup by customer number | `JsonAccountRelationshipRepositoryTest#shouldLoadRelationshipByCustomerNumber`; `InqacccuOpenApiConformanceTest#successPayloadShouldExposeRequiredShapes` |
| FR-006         | Preserve customer-found with zero accounts as valid successful outcome. | Outcome behavior                    | AC-008    | Success envelope supports `accounts.count = 0` with empty accounts list | `CustomerAccountInquiryPage.test.tsx#supports subsequent inquiry update and shows latest result` |
| FR-007         | Preserve maximum returned account count of 20. | Business behavior                    | AC-009    | No explicit cap logic in current repository/service; boundedness depends on data source | No automated evidence in current codebase |
| FR-008         | Preserve end-of-data equivalent (SQLCODE +100) as normal completion. | Business behavior                    | AC-008    | No DB cursor semantics in JSON-backed implementation | No automated evidence in current codebase |
| FR-009         | Preserve legacy status semantics including success/failure/customerFound and returned-account count. | Outcome behavior + Response contract                    | AC-001, AC-008    | `LegacyStatus`, `AccountsList`, `AccountRelationshipMapper` | `AccountRelationshipMapperTest#shouldMapProjectionToSuccessResponse`; `AccountRelationshipServiceTest#shouldReturnSuccessWhenRepositoryFindsCustomer`; `InqacccuOpenApiConformanceTest#successPayloadShouldExposeRequiredShapes` |
| FR-010         | Preserve legacy failure-path distinctions including fail codes for retrieval stages. | Outcome behavior                    | AC-005, AC-006, AC-007    | Current mapper supports not-found (`1001`) and success (`0000`) only; retrieval-stage failCode 2/3/4 mappings are not implemented | No automated evidence in current codebase |
| FR-011         | Preserve complete returned account information set. | Response contract supported account fields                    | AC-001    | Current `AccountSummary` exposes account number, sort code, type/description, balances, interest, overdraft, statement dates; fields such as eyecatcher/openedDate are not present in runtime DTO | `InqacccuOpenApiConformanceTest#successPayloadShouldExposeRequiredShapes`; `AccountRelationshipMapperTest#shouldMapProjectionToSuccessResponse` |
| FR-012         | Preserve fixed-width identifier semantics with leading zeroes. | Request parameters + Frontend observable behavior                    | AC-013    | String-based identifiers in backend and frontend domain models (`customerNumber`, `accountNumber`) | `validation.test.ts#accepts 10-digit customer numbers including reserved values`; `CustomerAccountInquiryPage.test.tsx#renders success response with accounts`; `inqacccu.e2e.spec.ts#renders successful account inquiry through frontend and backend` |
| FR-013         | Preserve legacy date semantics while exposing external date representation. | Date representation                    | AC-010    | `DateMapper` converts numeric `YYYYMMDD` to ISO `yyyy-MM-dd` in mapper | `AccountRelationshipMapperTest#shouldMapProjectionToSuccessResponse`; `InqacccuOpenApiConformanceTest#successPayloadShouldExposeRequiredShapes` |
| FR-014         | Do not imply deterministic account ordering. | Business behavior                    | AC-012    | No sorting logic in service/repository; order reflects source data | No automated evidence in current codebase |
| FR-015         | Provide a usable user-facing inquiry channel. | Frontend interaction scope                    | AC-001    | Route `/customer-accounts`, page `CustomerAccountInquiryPage`, nav registration in `App.tsx` | `CustomerAccountInquiryPage.test.tsx`; `inqacccu.e2e.spec.ts` |
| FR-016         | Present associated account results when customer found with accounts. | Frontend observable behavior                    | AC-001    | Accounts table rendering in `CustomerAccountInquiryPage` | `CustomerAccountInquiryPage.test.tsx#renders success response with accounts`; `inqacccu.e2e.spec.ts#renders successful account inquiry through frontend and backend` |
| FR-017         | Present distinct outcome when customer found with zero accounts. | Frontend observable behavior                    | AC-008    | UI message "No accounts found for this customer." in success state with count 0 | `CustomerAccountInquiryPage.test.tsx#supports subsequent inquiry update and shows latest result` |
| FR-018         | Present distinct outcome when customer not found. | Frontend observable behavior                    | AC-002, AC-003    | Not-found outcome shown from business 200 payload | `CustomerAccountInquiryPage.test.tsx#renders not-found business outcome inside 200 payload`; `inqacccu.e2e.spec.ts#renders customer-not-found business outcome from backend response` |
| FR-019         | Provide user-visible feedback for invalid inquiry input. | Frontend validation behavior                    | AC-011    | `validateCustomerAccountInput` and field-level error rendering | `validation.test.ts#rejects invalid values`; `CustomerAccountInquiryPage.test.tsx#shows validation error for malformed customer number` |
| FR-020         | Present distinct outcome for non-business infrastructure failure. | Infrastructure failure behavior                    | AC-011 (error semantics in current runtime), HTTP 500 handling    | `AccountRelationshipExceptionHandler` (`ERR-005`) + frontend backend-error display | `AccountRelationshipControllerTest#shouldReturnInternalErrorForRepositoryFailure`; `CustomerAccountInquiryPage.test.tsx#renders backend 500 error response`; `customerAccountInquiryClient.test.ts#returns backend error for 400/500 responses` |
| FR-021         | Preserve leading zeroes in visible identifiers across input/output presentation. | Frontend observable behavior                    | AC-013    | String input/output handling in API and UI types | `validation.test.ts#accepts 10-digit customer numbers including reserved values`; `inqacccu.e2e.spec.ts#supports subsequent inquiry and updates to latest completed result` |
| FR-022         | Allow subsequent inquiries in same interaction flow. | Frontend observable behavior                    | AC-001    | Same-page re-submit flow in `CustomerAccountInquiryPage` | `CustomerAccountInquiryPage.test.tsx#supports subsequent inquiry update and shows latest result`; `inqacccu.e2e.spec.ts#supports subsequent inquiry and updates to latest completed result` |

### 2.2 Non-Functional Requirements Traceability

| Requirement ID | Requirement Statement | Spec Section | Spec ID | Implementation Component | Test Case ID |
|----------------|-----------------------|--------------|---------|--------------------------|---------------|
| NFR-001        | All inputs strictly validated before processing | Validation rules | AC-011 | `@Pattern` validation on controller path variable plus frontend validation helper | `AccountRelationshipControllerTest#shouldReturnBadRequestForInvalidCustomerNumber`; `validation.test.ts#rejects invalid values` |
| NFR-002        | Error responses in standardized JSON structure | Error responses | AC-011 | `ApiError` / `ValidationError` payloads from `AccountRelationshipExceptionHandler` | `InqacccuOpenApiConformanceTest#invalidInputShouldReturnValidationErrorShape`; `AccountRelationshipControllerTest#shouldReturnInternalErrorForRepositoryFailure` |
| NFR-003        | No live CICS/DB2 connection in POC | Scope | AC-001 | `JsonAccountRelationshipRepository` over mock JSON data | `JsonAccountRelationshipRepositoryTest#shouldLoadRelationshipByCustomerNumber` |
| NFR-004        | Frozen feature contract is preserved while runtime OpenAPI is implementation-facing | Contract conformance | AC-001, AC-011 | Frozen contract in `specs/.../contracts/openapi.yaml`; runtime spec in `src/api/src/main/resources/openapi.yaml` | `InqacccuOpenApiConformanceTest#contractFileShouldContainRequiredPathAndStatuses` |
| NFR-005        | Backend/frontend integration path is executable in local development | Frontend interaction scope | AC-001 | `/customer-accounts` frontend route and API client call to `/api/v1/customers/{customerNumber}/accounts` | `inqacccu.e2e.spec.ts#renders successful account inquiry through frontend and backend`; `customerAccountInquiryClient.test.ts#returns success payload for 200 response` |
| NFR-006        | Request timeout handling is user-visible in frontend | Frontend observable behavior | AC-011 (error handling behavior) | AbortController timeout path in `inquireCustomerAccounts` | `customerAccountInquiryClient.test.ts#returns timeout on aborted requests` |
| NFR-007        | Network failure handling is user-visible in frontend | Frontend observable behavior | AC-011 (error handling behavior) | Network-error branch in `inquireCustomerAccounts` | `customerAccountInquiryClient.test.ts#returns network error on transport failure` |
| NFR-008        | Local builds and automated suites execute successfully | Test execution evidence | AC-001 | Maven/Vitest/Playwright suites recorded in implementation addendum | Evidence listed in Section 5.4 and 5.5 |
| NFR-009        | Security behavior is inherited from existing module policy (no INQACCCU-specific policy introduced) | Test-spec scope | AC-001 | Controller tests import shared INQACC security components with filters disabled for endpoint behavior testing | `AccountRelationshipControllerTest` |

---

## 3. Rule-to-Test Coverage Indicators

### 3.1 Business Rule Traceability

| Rule ID | Rule Description                                                                 | Test Case ID |
|---------|----------------------------------------------------------------------------------|---------------|
| BR-001  | Inquiry is read-only and returns account relationship records by customer number. | `JsonAccountRelationshipRepositoryTest#shouldLoadRelationshipByCustomerNumber`   |
| BR-003  | Customer number format is digits-only and exactly 10 characters.  | `AccountRelationshipControllerTest#shouldReturnBadRequestForInvalidCustomerNumber`; `validation.test.ts#rejects invalid values`   |
| BR-008  | Customer-not-found maps to legacy not-found status semantics.                      | `InqacccuOpenApiConformanceTest#businessNotFoundShouldReturn200WithLegacyStatusN`; `AccountRelationshipMapperTest#shouldMapNotFoundOutcome`   |
| BR-009  | Valid customer with zero accounts remains a successful business outcome.        | `CustomerAccountInquiryPage.test.tsx#supports subsequent inquiry update and shows latest result`       |
| BR-010  | Valid customer with one or more accounts maps to success status and account list.        | `InqacccuOpenApiConformanceTest#successPayloadShouldExposeRequiredShapes`; `AccountRelationshipServiceTest#shouldReturnSuccessWhenRepositoryFindsCustomer`       |
| BR-013  | Account statement dates are converted into external API date format.        | `AccountRelationshipMapperTest#shouldMapProjectionToSuccessResponse`       |
| BR-016  | No explicit DB cursor end-of-data path exists in JSON-backed implementation.        | No automated evidence in current codebase       |

---

## 4. Test Case Coverage Map

### 4.1 Unit Test Coverage (Spring Boot Backend)

| Test Case ID | Test Name | Related Rule(s) | Related Requirement(s) | Class Under Test | Assertion(s) | Status |
|---------------|-----------|------------------|------------------------|------------------|---------------|--------|
| TC-022-001    | Contract path and statuses present in frozen contract | BR-001 | FR-005, FR-009 | `InqacccuOpenApiConformanceTest` | Contract file contains `/api/v1/customers/{customerNumber}/accounts` and 200/400/500 response families | PASS |
| TC-022-002    | Success payload shape conformance | BR-010, BR-013 | FR-009, FR-011, FR-013 | `InqacccuOpenApiConformanceTest` | 200 payload includes legacy status, customer summary, account list, and ISO date values | PASS |
| TC-022-003    | Business not-found outcome over HTTP 200 | BR-008 | FR-003, FR-018 | `InqacccuOpenApiConformanceTest` | Not-found request returns `success=N`, `failCode=1001`, null customer/accounts | PASS |
| TC-022-004    | Invalid input error shape | BR-003 | FR-001, FR-019 | `InqacccuOpenApiConformanceTest` | Invalid customer number returns HTTP 400 with `ERR-001` validation payload | PASS |
| TC-024-001    | Controller success response | BR-010 | FR-005, FR-016 | `AccountRelationshipControllerTest` | 200 response contains expected status and summary fields | PASS |
| TC-024-002    | Controller bad request response | BR-003 | FR-001, FR-019 | `AccountRelationshipControllerTest` | Invalid customer number returns HTTP 400 and `ERR-001` | PASS |
| TC-024-003    | Controller infrastructure error response | BR-001 | FR-020 | `AccountRelationshipControllerTest` | Repository failure surfaced as HTTP 500 with `ERR-005` | PASS |
| TC-023-001    | Repository JSON lookup by customer number | BR-001 | FR-005 | `JsonAccountRelationshipRepositoryTest` | Repository loads mock-data record and returns matching account projection | PASS |
| TC-022-005    | Service success orchestration | BR-010 | FR-005, FR-009 | `AccountRelationshipServiceTest` | Repository hit maps to success legacy status and non-zero account count | PASS |
| TC-022-006    | Service not-found orchestration | BR-008 | FR-003, FR-009 | `AccountRelationshipServiceTest` | Repository miss maps to not-found legacy status/fail code | PASS |
| TC-022-007    | Mapper success projection mapping | BR-010, BR-013 | FR-009, FR-011, FR-013 | `AccountRelationshipMapperTest` | Mapper trims fields, preserves identifiers, converts statement dates | PASS |
| TC-022-008    | Mapper not-found mapping | BR-008 | FR-003, FR-009 | `AccountRelationshipMapperTest` | Mapper emits not-found legacy status and null customer/accounts | PASS |
| TC-025-001    | Frontend input validation feedback | BR-003 | FR-019 | `validation.test.ts`, `CustomerAccountInquiryPage.test.tsx` | Invalid input shows field-level message and blocks submission flow | PASS |
| TC-026-001    | Frontend API client success and error handling | BR-001 | FR-015, FR-020 | `customerAccountInquiryClient.test.ts` | Handles 200, 400/500, timeout, and network-failure result branches | PASS |
| TC-025-002    | Frontend result presentation | BR-009, BR-010 | FR-016, FR-017, FR-018 | `CustomerAccountInquiryPage.test.tsx` | Renders success, zero-account, not-found, and backend error states | PASS |
| TC-027-001    | Browser E2E integrated happy path | BR-010 | FR-015, FR-016 | `inqacccu.e2e.spec.ts` | End-to-end flow renders successful inquiry with customer and accounts | PASS |
| TC-027-002    | Browser E2E subsequent inquiry | BR-001 | FR-022 | `inqacccu.e2e.spec.ts` | Updated input and resubmission renders latest completed result | PASS |
| TC-027-003    | Browser E2E customer-not-found path | BR-008 | FR-018 | `inqacccu.e2e.spec.ts` | End-to-end flow renders distinct not-found outcome | PASS |

---

## 5. INQACCCU Implementation Evidence Addendum (2026-07-22)

### 5.1 OpenAPI Verification Paths

- Frozen feature contract (unchanged): `specs/003-inqacccu-customer-account-relationship-modernization/contracts/openapi.yaml`
- Runtime OpenAPI document (modified for implementation): `src/api/src/main/resources/openapi.yaml`

### 5.2 Implemented Endpoint Traceability

| Capability | Implemented Artifact |
|---|---|
| Endpoint mapping | `src/api/src/main/java/com/bankofz/inqcust/api/inqacccu/controller/AccountRelationshipController.java` |
| Orchestration service | `src/api/src/main/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipService.java` |
| Domain response mapping | `src/api/src/main/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipMapper.java` |
| Date conversion | `src/api/src/main/java/com/bankofz/inqcust/api/inqacccu/service/DateMapper.java` |
| Repository abstraction | `src/api/src/main/java/com/bankofz/inqcust/api/inqacccu/repository/AccountRelationshipRepository.java` |
| Mock-data repository | `src/api/src/main/java/com/bankofz/inqcust/api/inqacccu/repository/JsonAccountRelationshipRepository.java` |
| Mock-data source | `mock-data/account-relationship-records.json` |

### 5.3 Frontend Integration Traceability

| Capability | Implemented Artifact |
|---|---|
| Route registration | `src/frontend-react/src/App.tsx` |
| Route URL | `/customer-accounts` |
| API client | `src/frontend-react/src/api/customerAccountInquiryClient.ts` |
| Domain typing | `src/frontend-react/src/domain/customerAccountTypes.ts` |
| Page behavior | `src/frontend-react/src/features/customerAccountInquiry/CustomerAccountInquiryPage.tsx` |

### 5.4 Executed Verification Commands

| Area | Command | Outcome |
|---|---|---|
| Backend compile | `mvn -q -DskipTests compile` | PASS |
| Backend targeted INQACCCU tests | `mvn -q -Dtest="InqacccuOpenApiConformanceTest,AccountRelationshipControllerTest,JsonAccountRelationshipRepositoryTest,AccountRelationshipServiceTest,AccountRelationshipMapperTest" test` | PASS |
| Backend full tests | `mvn -q test` | PASS |
| Frontend unit/integration tests | `npm test` | PASS |
| Frontend build | `npm run build` | PASS |
| Browser-level E2E | `npm run test:e2e` | PASS |

### 5.5 Automated Test Evidence References

Backend tests:

- `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/contract/InqacccuOpenApiConformanceTest.java`
- `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/controller/AccountRelationshipControllerTest.java`
- `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/repository/JsonAccountRelationshipRepositoryTest.java`
- `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipMapperTest.java`
- `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipServiceTest.java`

Frontend tests:

- `src/frontend-react/src/api/customerAccountInquiryClient.test.ts`
- `src/frontend-react/src/features/customerAccountInquiry/validation.test.ts`
- `src/frontend-react/src/features/customerAccountInquiry/CustomerAccountInquiryPage.test.tsx`

Browser-level E2E tests:

- `src/frontend-react/e2e/inqacccu.e2e.spec.ts`