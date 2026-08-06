# Mapping Matrix: UPDCUST

## 1. Request Mapping (API -> UPDCUST COMMAREA)

| API field | COMMAREA field | Rule |
|---|---|---|
| customerNumber (path) | COMM-CUSTNO | Required, 10 chars |
| sortCode (query optional) | COMM-SCODE | Optional, fallback default if blank |
| title | COMM-NAME.COMM-TITLE | X(10), allow-list validated |
| firstName | COMM-NAME.COMM-FIRST-NAME | X(50), name-gate driver |
| lastName | COMM-NAME.COMM-LAST-NAME | X(50) |
| dateOfBirth (yyyy-MM-dd) | COMM-DOB day/month/year | Parsed into numeric components |
| phoneNumber | COMM-PHONE | X(20), update only when non-blank |
| address.addressLine1 | COMM-ADDR.COMM-ADDR-LINE1 | X(50), address-gate driver |
| address.addressLine2 | COMM-ADDR.COMM-ADDR-LINE2 | X(50) |
| address.city | COMM-ADDR.COMM-CITY | X(50) |
| address.postalCode | COMM-ADDR.COMM-POSTCODE | X(10) |
| address.country | COMM-ADDR.COMM-COUNTRY | X(50) |
| customerStatus | COMM-STATUS | X(10), update only when non-blank |

## 2. Persistence Mapping (COMMAREA/Host Vars -> CUSTOMER)

| Logical field | DB2 column | Notes |
|---|---|---|
| sortCode | CUSTOMER_SORTCODE | CHAR(6) |
| customerNumber | CUSTOMER_NUMBER | CHAR(10) |
| title | CUSTOMER_TITLE | CHAR(10) |
| firstName | CUSTOMER_FIRST_NAME | CHAR(50) |
| lastName | CUSTOMER_LAST_NAME | CHAR(50) |
| dob | CUSTOMER_DATE_OF_BIRTH | INTEGER yyyymmdd |
| phone | CUSTOMER_PHONE | CHAR(20) |
| addressLine1 | CUSTOMER_ADDR_LINE1 | CHAR(50) |
| addressLine2 | CUSTOMER_ADDR_LINE2 | CHAR(50) |
| city | CUSTOMER_CITY | CHAR(50) |
| postcode | CUSTOMER_POSTCODE | CHAR(10) |
| country | CUSTOMER_COUNTRY | CHAR(50) |
| status | CUSTOMER_STATUS | CHAR(10) |

## 3. Response Mapping (COMMAREA/DB -> API)

| Source | API field | Transform |
|---|---|---|
| COMM-CUSTNO | customerNumber | trim |
| COMM-SCODE | sortCode | trim |
| COMM-NAME.* | name fields | trim |
| COMM-DOB | dateOfBirth | to ISO yyyy-MM-dd |
| COMM-PHONE | phoneNumber | trim |
| COMM-ADDR.* | address fields | trim |
| COMM-STATUS | customerStatus | trim |
| COMM-CREATED-DATE | createdDate | to ISO yyyy-MM-dd |
| COMM-CREDIT-SCORE | creditScore | numeric |
| CUSTOMER_CREDIT_SCORE_REVIEW_DATE (numeric) | creditScoreReviewDate | numeric yyyymmdd to ISO yyyy-MM-dd (computed decomposition) |
| COMM-UPD-SUCCESS | legacyStatus.updSuccess | direct |
| service success status | legacyStatus.updFailCode | explicit blank on success |
| legacy failure code mapping | legacyStatus.updFailCode | mapped fail code for failure responses |

## 4. Copybook Authorities
- UPDCUST.cpy: request/response COMMAREA structure
- CUSTOMER.cpy: logical customer domain structure
- CUSTDB2.cpy: DB2 table column constraints
- SORTCODE.cpy: fallback sort code behavior
- ABNDINFO.cpy: abend/reporting context (non-contract)

## 5. Known Legacy Defect Handling
- UPDCUST.cbl raw MOVE of host `HV-CUSTOMER-CS-REVIEW-DATE` to grouped `COMM-CS-REVIEW-DATE` is treated as a legacy defect and not carried into modernization response mapping.
