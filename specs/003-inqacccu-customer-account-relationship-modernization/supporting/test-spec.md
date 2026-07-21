# test-spec.md - INQACCCU Test Specification

| Test ID | Scenario | Expected result |
|---|---|---|
| TC-001 | valid customer with accounts | success Y, failCode 0, accounts returned |
| TC-002 | customer number zero | success N, failCode 1, zero accounts |
| TC-003 | customer number 9999999999 | success N, failCode 1, zero accounts |
| TC-004 | customer validation fails | success N, failCode 1 |
| TC-005 | no account rows | success Y, numberOfAccounts 0 |
| TC-006 | one account | numberOfAccounts 1 |
| TC-007 | more than 20 accounts | only 20 returned |
| TC-008 | cursor open failure | success N, failCode 2 |
| TC-009 | fetch failure | success N, failCode 3 |
| TC-010 | close failure | success N, failCode 4 |
| TC-011 | date conversion | legacy dates convert to ISO dates |
| TC-012 | portfolio summary | totals match returned accounts |
