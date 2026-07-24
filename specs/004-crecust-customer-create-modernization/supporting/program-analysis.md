# Program Analysis: CRECUST Customer Create Modernization

**Document ID:** `program-analysis.md`  
**Legacy Source:** src/base/cics/cobol/CRECUST.cbl  
**Copybook Sources:** CRECUST.cpy, CUSTOMER.cpy, CUSTDB2.cpy, CUSTCTRL.cpy, NEWCUSNO.cpy

## 1. Program Inventory
- Program ID: `CRECUST`
- Type: CICS online create-customer transaction with DB2 writes and async credit-check orchestration.
- Entry COMMAREA: `CRECUST.cpy`
- Main output signals:
  - `COMM-SUCCESS`
  - `COMM-FAIL-CODE`
  - generated `COMM-NUMBER`

## 2. High-Level Flow (Observed in COBOL)
1. Validate `COMM-TITLE` against explicit allowlist.
2. Populate current date/time.
3. Execute credit-check orchestration (`CREDIT-CHECK` section).
4. Validate date of birth (`DATE-OF-BIRTH-CHECK` section).
5. Enqueue named counter (`ENQ-NAMED-COUNTER`).
6. Allocate next customer number via control-state update (`GET-LAST-CUSTOMER-DB2`).
7. Insert record into `CUSTOMER` table (`WRITE-CUSTOMER-DB2`).
8. Write transaction audit to `PROCTRAN` (`WRITE-PROCTRAN-DB2`).
9. Dequeue named counter.
10. Return COMMAREA with success/fail values.

## 3. Data Structures

### 3.1 COMMAREA (`CRECUST.cpy`)
- Key fields: `COMM-SORTCODE`, `COMM-NUMBER`.
- Name fields: `COMM-TITLE`, `COMM-FIRST-NAME`, `COMM-LAST-NAME`.
- DOB fields: day/month/year components.
- Address fields: line1/line2/city/postcode/country.
- Status and score fields: `COMM-STATUS`, `COMM-CREDIT-SCORE`, `COMM-CS-REVIEW-DATE`.
- Outcome fields: `COMM-SUCCESS`, `COMM-FAIL-CODE`.

### 3.2 CUSTOMER Copybook (`CUSTOMER.cpy`)
- Eyecatcher fixed value: `CUST`.
- Key: `CUSTOMER-SORTCODE` + `CUSTOMER-NUMBER`.
- Status value set includes `ACTIVE`, `INACTIVE`, `SUSPENDED`.

### 3.3 CUSTOMER DB2 Table (`CUSTDB2.cpy`)
- Dates are stored as INTEGER (`YYYYMMDD`) for DOB/created/review date fields.
- Customer number is `CHAR(10)` and sortcode `CHAR(6)`.

## 4. Legacy Business Rule Evidence
- Title allowlist from `EVALUATE COMM-TITLE` in `P010`.
- DOB checks in `DATE-OF-BIRTH-CHECK`:
  - year >=1601
  - CEEDAYS validity
  - age <=150
  - not future
- Control-number generation in `GET-LAST-CUSTOMER-DB2`:
  - select `CONTROL_VALUE_NUM`
  - increment
  - update control
  - move updated value to `COMM-NUMBER`

## 5. Credit-Check Behavior
- Issues up to 5 async child transaction requests (`OCR1`..`OCR5`).
- Waits 3 seconds then fetches available responses.
- If responses exist: average score and set review date randomly in next 21 days.
- If no usable responses: score `0`, review date fallback, fail path.

## 6. Persistence Behavior
- Customer insert into DB2 uses host variables mapped from COMMAREA and derived fields.
- Date conversions:
  - DOB -> `YYYYMMDD`
  - created date -> `YYYYMMDD`
  - review date -> `YYYYMMDD`
- Write-proctran failure path can ABEND (`HWPT`) after de-queue attempt.

## 7. Observable Failure Codes
- `T`, `O`, `Z`, `Y`, `1`, `3`, `4`, `5`, and credit-related `A`..`H`.
- Success returns `COMM-SUCCESS=Y` and blank fail code.

## 8. Modernization Implications
- Use `CustomerRepository` abstraction; no live DB2/CICS in POC.
- Use `CustomerControlRepository` abstraction for sequence state.
- Use `CreditCheckGateway` abstraction for async credit-check equivalent behavior.
- Preserve response-level legacy status and fail-code observability.
