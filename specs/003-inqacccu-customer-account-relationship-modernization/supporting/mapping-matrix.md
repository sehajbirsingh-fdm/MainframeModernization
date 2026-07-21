# mapping-matrix.md - INQACCCU Mapping Matrix

| Legacy field | DB2 column | Java field | API field |
|---|---|---|---|
| CUSTOMER-NUMBER | ACCOUNT_CUSTOMER_NUMBER | customerNumber | customerNumber |
| NUMBER-OF-ACCOUNTS | n/a | numberOfAccounts | numberOfAccounts |
| COMM-SUCCESS | n/a | success | legacyStatus.success |
| COMM-FAIL-CODE | n/a | failureCode | legacyStatus.failureCode |
| CUSTOMER-FOUND | n/a | customerFound | legacyStatus.customerFound |
| COMM-EYE | ACCOUNT_EYECATCHER | eyecatcher | accounts[].eyecatcher |
| COMM-CUSTNO | ACCOUNT_CUSTOMER_NUMBER | accountCustomerNumber | accounts[].customerNumber |
| COMM-SCODE | ACCOUNT_SORTCODE | sortCode | accounts[].sortCode |
| COMM-ACCNO | ACCOUNT_NUMBER | accountNumber | accounts[].accountNumber |
| COMM-ACC-TYPE | ACCOUNT_TYPE | accountType | accounts[].accountType |
| COMM-INT-RATE | ACCOUNT_INTEREST_RATE | interestRate | accounts[].interestRate |
| COMM-OPENED | ACCOUNT_OPENED | openedDate | accounts[].openedDate |
| COMM-OVERDRAFT | ACCOUNT_OVERDRAFT_LIMIT | overdraftLimit | accounts[].overdraftLimit |
| COMM-LAST-STMT-DT | ACCOUNT_LAST_STATEMENT | lastStatementDate | accounts[].lastStatementDate |
| COMM-NEXT-STMT-DT | ACCOUNT_NEXT_STATEMENT | nextStatementDate | accounts[].nextStatementDate |
| COMM-AVAIL-BAL | ACCOUNT_AVAILABLE_BALANCE | availableBalance | accounts[].availableBalance |
| COMM-ACTUAL-BAL | ACCOUNT_ACTUAL_BALANCE | actualBalance | accounts[].actualBalance |
