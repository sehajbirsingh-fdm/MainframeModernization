# Data Model: UPDCUST Customer Update

## 1. Legacy COMMAREA Model (UPDCUST.cpy)

Primary fields:
- COMM-EYE: X(4)
- COMM-SCODE: X(6)
- COMM-CUSTNO: X(10)
- COMM-NAME:
  - COMM-TITLE: X(10)
  - COMM-FIRST-NAME: X(50)
  - COMM-LAST-NAME: X(50)
- COMM-DOB:
  - COMM-DOB-DAY: 2 digits
  - COMM-DOB-MONTH: 2 digits
  - COMM-DOB-YEAR: 4 digits
- COMM-PHONE: X(20)
- COMM-ADDR:
  - COMM-ADDR-LINE1: X(50)
  - COMM-ADDR-LINE2: X(50)
  - COMM-CITY: X(50)
  - COMM-POSTCODE: X(10)
  - COMM-COUNTRY: X(50)
- COMM-STATUS: X(10)
- COMM-CREATED-DATE: day/month/year
- COMM-CREDIT-SCORE: 3 digits
- COMM-CS-REVIEW-DATE: day/month/year
- COMM-UPD-SUCCESS: X(1)
- COMM-UPD-FAIL-CD: X(1)

## 2. Persistence Model (CUSTDB2.cpy)

CUSTOMER table columns used:
- CUSTOMER_EYECATCHER CHAR(4)
- CUSTOMER_SORTCODE CHAR(6)
- CUSTOMER_NUMBER CHAR(10)
- CUSTOMER_TITLE CHAR(10)
- CUSTOMER_FIRST_NAME CHAR(50)
- CUSTOMER_LAST_NAME CHAR(50)
- CUSTOMER_DATE_OF_BIRTH INTEGER
- CUSTOMER_PHONE CHAR(20)
- CUSTOMER_ADDR_LINE1 CHAR(50)
- CUSTOMER_ADDR_LINE2 CHAR(50)
- CUSTOMER_CITY CHAR(50)
- CUSTOMER_POSTCODE CHAR(10)
- CUSTOMER_COUNTRY CHAR(50)
- CUSTOMER_STATUS CHAR(10)
- CUSTOMER_CREATED_DATE INTEGER
- CUSTOMER_CREDIT_SCORE SMALLINT
- CUSTOMER_CS_REVIEW_DATE INTEGER

## 3. Modern API Model (Proposed)

Update request:
- customerNumber (path, required)
- sortCode (query, optional)
- title
- firstName
- lastName
- dateOfBirth (ISO yyyy-MM-dd, optional)
- phoneNumber
- address:
  - addressLine1
  - addressLine2
  - city
  - postalCode
  - country
- customerStatus

Update response:
- customerNumber
- sortCode
- title
- firstName
- lastName
- dateOfBirth (ISO)
- phoneNumber
- address { ... }
- customerStatus
- createdDate (ISO)
- creditScore
- creditScoreReviewDate (ISO)
- legacyStatus:
  - updSuccess
  - updFailCode

## 4. Update Mutability Matrix

Mutable when gated:
- title (only when firstName non-blank)
- firstName
- lastName
- phone
- address fields (only when addressLine1 non-blank)
- status
- DOB (when year present)

Immutable in UPDCUST flow:
- customerNumber
- sortCode
- createdDate
- creditScore
- creditScoreReviewDate
- eyecatcher
