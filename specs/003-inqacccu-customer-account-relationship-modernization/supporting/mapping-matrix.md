# mapping-matrix.md - INQACCCU Legacy Field Mapping Matrix

## Confirmed Mappings

| Legacy source field | DB2 source column (if applicable) | Future interface field | Transformation / formatting | Notes / evidence |
|---|---|---|---|---|
| CUSTOMER-NUMBER (input in INQACCCU commarea) | n/a (inquiry input) | request.customerNumber | Fixed-width identifier (10 chars/digits). Preserve leading zeroes. | `CUSTOMER-NUMBER PIC 9(10)` in INQACCCU commarea copybook. |
| SORTCODE constant (program/copybook) -> COMM-SCODE output | ACCOUNT_SORTCODE | response.accounts[].sortCode | Set internally to fixed value `987654`; not caller-supplied. Preserve fixed width (6). | `77 SORTCODE PIC 9(6) VALUE 987654`; moved to SQL host var and output field. |
| NUMBER-OF-ACCOUNTS | n/a | response.numberOfAccounts | Integer count populated by fetch loop, maximum 20. | `NUMBER-OF-ACCOUNTS` + loop cap at 20 + OCCURS 1 TO 20. |
| COMM-SUCCESS | n/a | response.legacyStatus.success | One-character success flag (`Y`/`N` in observed flow). | Set at entry and on success/failure paths in INQACCCU. |
| COMM-FAIL-CODE | n/a | response.legacyStatus.failCode | One-character failure code. Confirmed values: `0`,`1`,`2`,`3`,`4`. | Initialized `0`; set to `1` not-found; `2/3/4` DB2 cursor failures. |
| CUSTOMER-FOUND | n/a | response.legacyStatus.customerFound | One-character flag (`Y`/`N`). Determined by INQCUST validation, not row count. | Set in `CUSTOMER-CHECK` via `INQCUST-INQ-SUCCESS`. |
| COMM-EYE | ACCOUNT_EYECATCHER | response.accounts[].eyecatcher | Direct move from fetched row to output occurrence. | `MOVE HV-ACCOUNT-EYECATCHER TO COMM-EYE(...)`. |
| COMM-CUSTNO | ACCOUNT_CUSTOMER_NUMBER | response.accounts[].customerNumber | Direct move. Preserve identifier formatting/leading zeroes. | `MOVE HV-ACCOUNT-CUST-NO TO COMM-CUSTNO(...)`. |
| COMM-SCODE | ACCOUNT_SORTCODE | response.accounts[].sortCode | Direct move from fetched row; value path constrained by fixed sort code filter. | `MOVE HV-ACCOUNT-SORTCODE TO COMM-SCODE(...)`. |
| COMM-ACCNO | ACCOUNT_NUMBER | response.accounts[].accountNumber | Direct move. Fixed-width identifier (8 chars/digits); preserve leading zeroes. | `COMM-ACCNO PIC 9(8)`; moved from `HV-ACCOUNT-ACC-NO`. |
| COMM-ACC-TYPE | ACCOUNT_TYPE | response.accounts[].accountType | Direct move; no source-defined enumeration in INQACCCU. | `MOVE HV-ACCOUNT-ACC-TYPE TO COMM-ACC-TYPE(...)`. |
| COMM-INT-RATE | ACCOUNT_INTEREST_RATE | response.accounts[].interestRate | Direct numeric move (`S9(4)V99` host to `9(4)V99` commarea field). | `MOVE HV-ACCOUNT-INT-RATE TO COMM-INT-RATE(...)`. |
| COMM-OPENED | ACCOUNT_OPENED (DATE) | response.accounts[].openedDate | DB2 date text fetched into host var then rearranged to `DDMMYYYY` in commarea output. | Uses `DB2-DATE-REFORMAT` then `STRING day month year INTO COMM-OPENED(...)`. |
| COMM-OVERDRAFT | ACCOUNT_OVERDRAFT_LIMIT | response.accounts[].overdraftLimit | Direct numeric move. | `MOVE HV-ACCOUNT-OVERDRAFT-LIM TO COMM-OVERDRAFT(...)`. |
| COMM-LAST-STMT-DT | ACCOUNT_LAST_STATEMENT (DATE) | response.accounts[].lastStatementDate | DB2 date text rearranged to `DDMMYYYY` in commarea output. | `STRING day month year INTO COMM-LAST-STMT-DT(...)`. |
| COMM-NEXT-STMT-DT | ACCOUNT_NEXT_STATEMENT (DATE) | response.accounts[].nextStatementDate | DB2 date text rearranged to `DDMMYYYY` in commarea output. | `STRING day month year INTO COMM-NEXT-STMT-DT(...)`. |
| COMM-AVAIL-BAL | ACCOUNT_AVAILABLE_BALANCE | response.accounts[].availableBalance | Direct numeric move. | `MOVE HV-ACCOUNT-AVAIL-BAL TO COMM-AVAIL-BAL(...)`. |
| COMM-ACTUAL-BAL | ACCOUNT_ACTUAL_BALANCE | response.accounts[].actualBalance | Direct numeric move. | `MOVE HV-ACCOUNT-ACTUAL-BAL TO COMM-ACTUAL-BAL(...)`. |

## Confirmed Transformation Notes

1. Identifier handling
- `CUSTOMER-NUMBER` and `COMM-ACCNO` are fixed-width identifiers in legacy structures and should be represented in a way that preserves leading zeroes.

2. Date handling
- Source DB2 account dates are declared as `DATE` columns.
- INQACCCU receives them through `PIC X(10)` host variables and emits `COMM-OPENED`, `COMM-LAST-STMT-DT`, and `COMM-NEXT-STMT-DT` in `DDMMYYYY` layout.
- If a future interface uses ISO date format, that conversion is outside the current legacy program output and should be treated as a downstream transformation.

3. Fixed sort code handling
- Account retrieval and mapped sort code output are based on internally fixed `SORTCODE = 987654`, not a caller-supplied sort code field.

## Unresolved / Not Confirmed by Current Source Set

| Topic | Current status |
|---|---|
| DB2 null handling behavior for nullable columns in this fetch path | Not confirmed from INQACCCU because no indicator-variable handling is shown in this source. |
| Physical PK/FK/index/uniqueness constraints | Not confirmed by copybook declarations alone; requires DB DDL evidence. |
| Any mapping for unsupported fields (account status, currency, relationship state/effective dates, pagination metadata, cache metadata) | No source evidence; intentionally not mapped. |
