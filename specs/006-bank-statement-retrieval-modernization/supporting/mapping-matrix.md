# Mapping Matrix - 006 Bank Statement Retrieval

| Legacy BNKSTMT concept | Legacy field/process | Modern contract field |
| --- | --- | --- |
| Account identity | HV_ACCT_SORTCODE, HV_ACCT_NUMBER | sortCode, accountNumber |
| Statement period input | DATECARD month + period derivation | period (YYYYMM) |
| Effective period start | PERIOD_FROM | summary.periodFrom (derived context) |
| Effective period end | PERIOD_TO | summary.periodTo (derived context) |
| Transaction count | TRANS_COUNT | summary.transactionCount |
| Totals | TOTAL_CREDITS, TOTAL_DEBITS | summary.totalCredits, summary.totalDebits |
| Balances | OPENING_BALANCE, CLOSING_BALANCE | summary.openingBalance, summary.closingBalance |
| Transaction row | PROCTRAN date/time/ref/type/desc/amount | entries[] fields |
