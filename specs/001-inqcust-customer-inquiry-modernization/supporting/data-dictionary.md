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
