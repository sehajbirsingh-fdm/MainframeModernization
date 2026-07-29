# Field Mapping Matrix

| Legacy/COMMAREA field | Copybook type | DB2 column | Java/API field | Transformation | Notes |
|---|---|---|---|---|---|
| INQTRANL-SORTCODE | 9(6) | PROCTRAN_SORTCODE CHAR(6) | sortCode string | Numeric display to fixed-width string | Preserve leading zeros. |
| INQTRANL-ACCNO | 9(8) | PROCTRAN_NUMBER CHAR(8) | accountNumber string | Numeric display to fixed-width string | Preserve leading zeros. |
| INQTRANL-FROM-DATE | 9(8) | predicate on PROCTRAN_DATE | fromDate string/null | Legacy 0 sentinel → absent lower bound; otherwise YYYYMMDD | Calendar validation requires approval. |
| INQTRANL-TO-DATE | 9(8) | predicate on PROCTRAN_DATE | toDate string/null | Legacy 99999999 sentinel → absent upper bound; otherwise YYYYMMDD | Calendar validation requires approval. |
| INQTRANL-LIMIT | 9(3) | pagination control | limit integer | 0/omitted→50; >100→100 | Effective value returned. |
| INQTRANL-OFFSET | 9(5) | pagination control | offset integer | Apply after filter/order | Default 0 in modern request. |
| INQTRANL-TOTAL-COUNT | 9(5) | COUNT(*) | totalCount integer | Direct count | Pre-pagination. |
| INQTRANL-RETURNED-COUNT | 9(3) | derived | returnedCount integer | Size of page | Must equal array size. |
| INQTRANL-SUCCESS | X | n/a | HTTP success/error | Y after full success | Not exposed as redundant response field. |
| INQTRANL-TRAN-ID | X(50) | derived | transactionId string | sc-account-date-time-ref | Not proven unique. |
| INQTRANL-TRAN-SORTCODE | 9(6) | PROCTRAN_SORTCODE | transaction.sortCode | Direct | Preserve leading zeros. |
| INQTRANL-TRAN-ACCNO | 9(8) | PROCTRAN_NUMBER | transaction.accountNumber | Direct | Preserve leading zeros. |
| INQTRANL-TRAN-DATE | 9(8) | PROCTRAN_DATE DATE | transaction.date string | DB2 date → YYYYMMDD | Null mapping requires validation. |
| INQTRANL-TRAN-TIME | 9(6) | PROCTRAN_TIME CHAR(6) | transaction.time string | Direct | Null/padding requires validation. |
| INQTRANL-TRAN-REF | 9(12) | PROCTRAN_REF CHAR(12) | transaction.reference string | Direct | DB2 allows characters; COMMAREA numeric conflict requires SME validation. |
| INQTRANL-TRAN-TYPE | X(3) | PROCTRAN_TYPE CHAR(3) | transaction.type string | Direct | Mapping requires SME validation for null/padding. |
| INQTRANL-TRAN-DESC | X(40) | PROCTRAN_DESC CHAR(40) | transaction.description string | Direct | Mapping requires SME validation for null/padding. |
| INQTRANL-TRAN-AMOUNT | S9(10)V99 | PROCTRAN_AMOUNT DECIMAL(12,2) | transaction.amount decimal | Exact decimal | Null mapping requires validation. |
| PROCTRAN_EYECATCHER | none in output | PROCTRAN_EYECATCHER CHAR(4) | not exposed | Selected but not moved to output | Do not invent API field. |
