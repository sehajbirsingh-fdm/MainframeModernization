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
