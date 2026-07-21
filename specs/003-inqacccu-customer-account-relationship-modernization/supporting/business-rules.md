# business-rules.md - INQACCCU Business Rule Extraction

| Rule ID | Source evidence | Extracted rule | Modern interpretation |
|---|---|---|---|
| BR-001 | `MOVE 'N' TO COMM-SUCCESS`, `MOVE '0' TO COMM-FAIL-CODE` | Initialize response as unsuccessful with fail code 0. | Initialize legacy status. |
| BR-002 | `CUSTOMER-CHECK` links to `INQCUST` | Customer existence is validated before account retrieval. | Call customer validation service/repository. |
| BR-003 | customer number zero check | Customer number zero is invalid/not found. | Return success N, fail code 1. |
| BR-004 | customer number `9999999999` check | 9999999999 is invalid/not found for this program. | Return success N, fail code 1. |
| BR-005 | cursor WHERE `ACCOUNT_CUSTOMER_NUMBER` and `ACCOUNT_SORTCODE` | Accounts are fetched by customer number and sort code. | Repository query by customerNumber + sortCode. |
| BR-006 | `NUMBER-OF-ACCOUNTS = 20` loop limit | Maximum 20 accounts returned. | Limit API response to 20 accounts for compatibility. |
| BR-007 | `SQLCODE = +100` in fetch | No more rows is normal completion. | Return success Y with current count. |
| BR-008 | cursor open SQLCODE not 0 | Open failure sets fail code 2 and zero accounts. | HTTP 500 or controlled error with failCode 2. |
| BR-009 | fetch SQLCODE not 0 | Fetch failure sets fail code 3 and zero accounts. | HTTP 500 or controlled error with failCode 3. |
| BR-010 | cursor close SQLCODE not 0 | Close failure sets fail code 4. | controlled error with failCode 4. |
| BR-011 | SQLCODE 923 | DB2 connection lost storm-drain condition. | infrastructure error reason. |
| BR-012 | AD2Z abend | DB2 deadlock diagnostics. | infrastructure error reason. |
| BR-013 | AFCR/AFCS/AFCT abends | VSAM storm-drain rollback; success N. | controlled rollback/error path. |
| BR-014 | `OCCURS 1 TO 20` | Commarea supports dynamic list of 1-20 accounts. | accounts array with max 20. |
| BR-015 | account date string reformat | DB2 DATE values converted to DDMMYYYY. | LocalDate ISO response. |
