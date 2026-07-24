# qa-review-checklist.md

**Document ID:** `qa-review-checklist-inqacccu-001`  
**Pipeline:** `mainframe_modernization`  
**Target System:** INQACCCU Customer Account Inquiry REST API  
**Generated:** 2026-07-23  
**Status:** Post-Implementation QA Review Completed  

---

## 1. Requirement Coverage Verification

### 1.1 Functional Requirements Checklist

| REQ ID | Requirement Title | Artifact Source | Verification Method | Status | Notes |
|--------|-------------------|-----------------|---------------------|--------|-------|
| REQ-FUNC-001 | Accept 10-digit customer number via REST endpoint | requirements.md, spec.md | Automated test evidence + code review | PASS | Backend regex validation and 400 behavior verified by `AccountRelationshipControllerTest#shouldReturnBadRequestForInvalidCustomerNumber` and `InqacccuOpenApiConformanceTest#invalidInputShouldReturnValidationErrorShape`; frontend validation evidence in `validation.test.ts`. |
| REQ-FUNC-002 | Return 0-20 account records for valid customer | requirements.md, spec.md, tasks.md | Automated test evidence + mock data review | PASS | Valid customer retrieval is verified and 20-record boundary is covered by `AccountRelationshipMapperTest#shouldCapReturnedAccountsAtTwenty`; response count and collection remain aligned. |
| REQ-FUNC-003 | Return account details: account number, sort code, balance, interest rate, statement date | spec.md, contracts/openapi.yaml | Automated test evidence + DTO review | PASS | Runtime `AccountSummary` includes `eyecatcher`, `openedDate`, and all contract account fields; assertions exist in backend/frontend conformance and page tests. |
| REQ-FUNC-004 | Return customer not found indicator (`customerFound: false`, `numberOfAccounts: 0`) | requirements.md, spec.md | Automated test evidence | PASS | Customer-not-found business behavior is verified as HTTP 200 with `legacyStatus.customerFound = N`, top-level `numberOfAccounts = 0`, and empty `accounts[]`. |
| REQ-FUNC-005 | Preserve legacy observable behavior as default path | requirements.md, spec.md, plan.md | Automated tests + manual legacy comparison | MANUAL VERIFICATION REQUIRED | Automated tests validate implemented behavior branches, but no executed legacy COBOL output comparison evidence is present in current QA artifacts. |
| REQ-ARCH-001 | Backend Stack: Java 21, Spring Boot 3.3.x, Maven 3.9+ | plan.md, pom.xml | Configuration review | PARTIAL | Java 21 is configured and Spring Boot 3.x is used (`3.5.3`). Pre-checklist `3.3.x` assumption is outdated. Maven runtime version was not validated in this documentation pass. |
| REQ-ARCH-002 | Frontend Stack: React 18.x, TypeScript 5.x, Vite 5.x, Node.js 20 LTS | plan.md, package.json | Configuration review | PARTIAL | Implemented versions are React 19.x, TypeScript 6.x, Vite 8.x. Node.js runtime version evidence is not captured in this QA artifact. |
| REQ-SEC-001 | OAuth2 Resource Server with JWT bearer token validation | requirements.md, spec.md, test-spec.md | Scope check | NOT APPLICABLE | Frozen INQACCCU requirements/spec do not define OAuth2/JWT as feature acceptance criteria for `/api/v1/customers/{customerNumber}/accounts`. |
| REQ-SEC-002 | Role-based access control (RBAC) for customer-account endpoints | requirements.md, spec.md, test-spec.md | Scope check | NOT APPLICABLE | RBAC requirement is not present in frozen INQACCCU feature artifacts for this endpoint. |
| REQ-SEC-003 | Input validation: strict 10-digit customer number format | requirements.md, spec.md | Automated test evidence | PARTIAL | Non-numeric invalid case is verified in backend/frontend tests; explicit backend coverage for null/empty/length 9/11 is not evidenced in current automated suite. |
| REQ-SEC-004 | TLS 1.2+ transport security | requirements.md, spec.md, test-spec.md | Scope check | NOT APPLICABLE | Deployment transport-hardening is out of scope for frozen INQACCCU feature implementation review. |
| REQ-SEC-005 | Secrets handling via environment variables or secret manager | requirements.md, spec.md, tasks.md | Scope check | NOT APPLICABLE | DB startup secret-management controls are not part of frozen INQACCCU feature acceptance criteria in this POC scope. |
| REQ-OBS-001 | Structured JSON logging with correlation ID per request | requirements.md, spec.md, plan.md | Evidence review | MANUAL VERIFICATION REQUIRED | No INQACCCU automated evidence captured for correlation ID propagation or structured JSON logs in this pass. |
| REQ-OBS-002 | OpenTelemetry-ready tracing instrumentation | requirements.md, spec.md, test-spec.md | Scope check | NOT APPLICABLE | OpenTelemetry readiness is not a frozen INQACCCU feature requirement. |
| REQ-PERSIST-001 | Mock repository layer for POC (no live DB2 or CICS) | requirements.md, plan.md, tasks.md | Automated test + code/config review | PASS | `JsonAccountRelationshipRepository` uses `mock-data/account-relationship-records.json`; verified by `JsonAccountRelationshipRepositoryTest` and runtime property `app.inqacccu.mock-data.path`. |
| REQ-API-001 | REST API conforms to OpenAPI 3.0.3 specification | contracts/openapi.yaml, runtime openapi.yaml, spec.md | Contract and runtime comparison + automated test evidence | PARTIAL | Contract-shape drift findings are resolved; conformance tests now assert required business response structure and explicit business failCode outcomes (`1`,`2`,`3`,`4`). Remaining gap is broader schema-level validation depth beyond current test harness. |

---

### 1.2 Non-Functional Requirements Checklist

| NFR ID | NFR Title | Acceptance Criteria | Verification Method | Status | Notes |
|--------|-----------|---------------------|---------------------|--------|-------|
| NFR-001 | Performance: Response time <= 200ms (p95) for customer inquiry | Latency measurement under normal load | Load test evidence review | MANUAL VERIFICATION REQUIRED | No executed load/perf results are present in current automated evidence set. |
| NFR-002 | Availability: 99.9% uptime SLA in production | Uptime monitoring and incident tracking | Scope check | NOT APPLICABLE | Production SLA monitoring is outside frozen INQACCCU feature scope for this documentation pass. |
| NFR-003 | Scalability: Horizontal scaling via container orchestration | Auto-scaling policy definition | Scope check | NOT APPLICABLE | Kubernetes/HPA concerns are out-of-scope production controls for the feature checklist. |
| NFR-004 | Data Consistency: Account list returned is point-in-time snapshot | Transactional query semantics | Evidence review | MANUAL VERIFICATION REQUIRED | INQACCCU uses JSON mock repository; no transactional/snapshot verification evidence exists. |
| NFR-005 | Backward Compatibility: API versioning strategy (v1 baseline) | URL path includes version; deprecation headers | Contract and route review | PARTIAL | Versioned path `/api/v1/...` is implemented; no v2 coexistence/deprecation-header evidence exists. |
| NFR-006 | Security: No sensitive data (passwords, tokens, PII) in logs | Log sanitization configuration | Scope and evidence review | MANUAL VERIFICATION REQUIRED | No executed log-sanitization audit evidence captured in current test artifacts. |
| NFR-007 | Observability: Structured JSON logging with correlation ID | All logs include ISO 8601 timestamp, level, logger name, message, correlation ID | Evidence review | MANUAL VERIFICATION REQUIRED | No automated INQACCCU evidence for structured logging/correlation ID propagation. |
| NFR-008 | Metrics: Prometheus-compatible metrics export | Metrics endpoint at `/actuator/metrics`; latency, error rate, custom metrics | Scope and config review | NOT APPLICABLE | Prometheus-specific export/metrics controls are not frozen INQACCCU feature requirements. |
| NFR-009 | Tracing: OpenTelemetry instrumentation ready | Spans exported via OTLP or compatible exporter; trace propagation across services | Scope check | NOT APPLICABLE | OpenTelemetry tracing readiness is outside frozen INQACCCU feature requirements. |
| NFR-010 | Correlation ID: UUID v4 format; propagated to downstream calls | Correlation ID header format validation; header propagation across services | Evidence review | MANUAL VERIFICATION REQUIRED | No captured evidence for `X-Correlation-ID` generation/propagation in INQACCCU flow. |
| NFR-011 | Error Handling: Standardized JSON error response structure | All errors return `{ code, message, details, correlationId }` | Automated test evidence + schema review | FAIL | Implemented INQACCCU errors return `{code,message,details}` without `correlationId`; 400/500 verified, 401/403/404 not part of endpoint behavior. |
| NFR-012 | Accessibility (Frontend): WCAG 2.1 AA compliance | React UI component accessibility audit | Evidence review | MANUAL VERIFICATION REQUIRED | No automated accessibility evidence (Axe/screen-reader audit) is present. |
| NFR-013 | Code Quality: Minimum 80% code coverage for backend | JUnit 5 test suite coverage report | Coverage report review | MANUAL VERIFICATION REQUIRED | No Jacoco coverage report evidence captured in this documentation-only pass. |
| NFR-014 | Documentation: API documentation auto-generated from OpenAPI | Swagger UI available at `/swagger-ui.html` | Manual runtime verification | MANUAL VERIFICATION REQUIRED | Springdoc dependency exists, but Swagger endpoint availability was not executed/verified in this pass. |

---

## 2. Business Rule-to-Test Coverage Matrix

### 2.1 Business Rule Verification

| BR ID | Business Rule | Trigger Condition | Expected Output | Test Type | Test Case ID | Status | Notes |
|-------|---------------|--------------------|-----------------|-----------|--------------|--------|-------|
| BR001 | Customer Inquiry Acceptance Rule | Valid 10-digit customer number | `legacyStatus.customerFound='Y'`; accounts returned in implemented shape | Unit + Integration | TC-BR001-001 | PASS | Verified by `InqacccuOpenApiConformanceTest#successPayloadShouldExposeRequiredShapes`, `AccountRelationshipControllerTest#shouldReturnSuccessPayload`, and Playwright success flow. |
| BR001 | Customer Inquiry Acceptance Rule | Invalid/missing customer number | Validation failure is distinct from business not-found | Unit | TC-BR001-002 | PARTIAL | Non-numeric invalid path verified (400) in backend/frontend tests; missing/null and all length variants not explicitly evidenced. |
| BR002 | Account Balance Retrieval Rule | Valid customer with N accounts (0 <= N <= 20) | Return N account records from mock repository relationship payload | Integration | TC-BR002-001 | PARTIAL | Retrieval for valid customers is verified (N=1/N=2 examples); no automated boundary evidence for N=20. |
| BR002 | Account Balance Retrieval Rule | Valid customer with > 20 accounts | Return capped 20 accounts | Integration | TC-BR002-002 | PASS | Capped behavior is implemented in mapper (`limit(20)`) and verified by `AccountRelationshipMapperTest#shouldCapReturnedAccountsAtTwenty`. |
| BR003 | Input Validation Strictness Rule | Non-numeric customer ID | Return `400 Bad Request` with validation error payload | Unit | TC-BR003-001 | PASS | Verified by `AccountRelationshipControllerTest#shouldReturnBadRequestForInvalidCustomerNumber` and `InqacccuOpenApiConformanceTest#invalidInputShouldReturnValidationErrorShape`. |
| BR003 | Input Validation Strictness Rule | Customer ID length != 10 | Return `400 Bad Request` | Unit | TC-BR003-002 | PARTIAL | Regex rule implies rejection; explicit backend automated tests for 9/11-digit cases are not present. |
| BR003 | Input Validation Strictness Rule | Leading/trailing whitespace in customer ID | Reject invalid formatting | Unit | TC-BR003-003 | MANUAL VERIFICATION REQUIRED | No explicit automated whitespace case evidence captured in backend tests. |
| BR004 | Account Status Preservation Rule | Query returns all account statuses (Active, Inactive, Closed) | Return all accounts regardless of status | Integration | TC-BR004-001 | NOT APPLICABLE | Account status is not part of frozen INQACCCU contract/implemented DTO for this feature path. |
| BR005 | OAuth2 Authorization Rule | Valid JWT with required role | Authorization enforced at endpoint | Integration | TC-BR005-001 | NOT APPLICABLE | OAuth2/JWT/RBAC is not a frozen INQACCCU feature requirement for this endpoint. |
| BR005 | OAuth2 Authorization Rule | Valid JWT with insufficient role | Return 403 | Integration | TC-BR005-002 | NOT APPLICABLE | Out-of-scope for frozen INQACCCU feature acceptance artifacts. |
| BR005 | OAuth2 Authorization Rule | Missing or invalid JWT | Return 401 | Unit | TC-BR005-003 | NOT APPLICABLE | Out-of-scope for frozen INQACCCU feature acceptance artifacts. |
| BR006 | Secrets Handling Rule | Database credentials required at startup | Secrets handling controls | Unit | TC-BR006-001 | NOT APPLICABLE | Startup secret-management controls are outside frozen INQACCCU feature QA scope for this POC. |
| BR006 | Secrets Handling Rule | Missing required secret at startup | Startup failure behavior | Unit | TC-BR006-002 | NOT APPLICABLE | Out-of-scope production/control-plane concern for this feature checklist. |

---

## 3. Data Mapping Validation Checklist

### 3.1 Legacy-to-Modern Data Structure Mapping

| Legacy Copybook | Legacy Field | Type/Length | Modern Java DTO | Modern Type | Mapping Test | Status | Notes |
|-----------------|--------------|-------------|-----------------|-------------|--------------|--------|-------|
| INQACCCUZ.cpy | CUSTOMER-NUMBER | PIC 9(10) | `AccountRelationshipResponse.customerNumber` | `String` | String format; leading zero preservation | PASS | `AccountRelationshipMapperTest` and `InqacccuOpenApiConformanceTest` verify top-level customer number preservation (`0000000001`). |
| INQACCCUZ.cpy | CUSTOMER-FOUND | PIC X ('Y'/'N') | `AccountRelationshipResponse.legacyStatus.customerFound` | `String ('Y'/'N')` | Y/N preservation | PASS | Implemented as Y/N status semantics; verified in not-found and success tests across backend/frontend. |
| INQACCCUZ.cpy | NUMBER-OF-ACCOUNTS | S9(8) BINARY | `AccountRelationshipResponse.numberOfAccounts` | `Integer` | Count alignment with returned list | PASS | Count is top-level and validated against returned collection in mapper/conformance tests; 20-cap behavior is also verified. |
| ACCOUNT.cpy | ACCOUNT-NUMBER | PIC 9(8) | `AccountSummary.accountNumber` | `String` | Identifier preservation | PARTIAL | Returned and asserted in backend/frontend/E2E tests; explicit dedicated leading-zero account-number test case is not present. |
| ACCOUNT.cpy | SORT-CODE | PIC 9(6) | `AccountSummary.sortCode` | `String` | Numeric string mapping | PASS | Verified in mock data and frontend/backend assertions. |
| ACCOUNT.cpy | ACCOUNT-BALANCE | PIC S9(13)V99 COMP-3 | `AccountSummary.availableBalance` + `AccountSummary.actualBalance` | `BigDecimal` | Decimal precision mapping | PASS | Mapper and payload tests verify decimal values are retained in JSON payload. |
| ACCOUNT.cpy | INTEREST-RATE | PIC 9(3)V99 COMP-3 | `AccountSummary.interestRate` | `BigDecimal` | Decimal scale preservation | PASS | Verified in mapper/controller/frontend payload assertions. |
| ACCOUNT.cpy | STATEMENT-DATE | PIC 9(8) (YYYYMMDD) | `AccountSummary.lastStatementDate` / `nextStatementDate` | `String (ISO yyyy-MM-dd)` | Numeric-to-ISO conversion | PASS | Verified by `AccountRelationshipMapperTest` and `InqacccuOpenApiConformanceTest` date assertions. |
| ACCOUNT.cpy | ACCOUNT-STATUS | PIC X(8) | Not implemented in runtime INQACCCU DTO | N/A | Scope validation | NOT APPLICABLE | Account status field is not part of frozen INQACCCU response contract for this feature implementation. |
| ACCDB2.cpy | EYE-CATCHER | PIC X(4) ('ACCT') | `AccountSummary.eyecatcher` | `String` | Contract-field parity check | PASS | `eyecatcher` is present in runtime DTO and validated in mapper and OpenAPI conformance assertions. |

---

## 4. QA Review Outcome Snapshot

- Automated verification completed: backend contract/controller/service/repository/mapper tests, frontend API client/validation/page tests, and Playwright INQACCCU E2E tests.
- Manual smoke testing still required: whitespace/length-edge validation variants, runtime Swagger endpoint check, explicit log/correlation verification, and legacy-output comparison.
- No implementation evidence currently captured: performance/load results, accessibility audit, coverage-threshold report.
- Out-of-scope production concerns (marked NOT APPLICABLE): OAuth2/JWT/RBAC controls for INQACCCU endpoint, TLS deployment hardening, Kubernetes/HPA/SLA/Prometheus/OpenTelemetry production operations.
- Frontend test gap closure: previously broad "missing frontend tests" concern was narrowed to retrieval-stage failure presentation for failCode `2`/`3`/`4`; this is now covered by explicit frontend tests distinct from HTTP 500/system-error handling.