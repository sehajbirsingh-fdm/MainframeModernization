# business-rules.md - INQACCCU Legacy Business and Processing Rules

## Confirmed Legacy Business Behavior

1. BR-001 - Inquiry input
Only customer number is supplied for inquiry in the INQACCCU commarea (`CUSTOMER-NUMBER PIC 9(10)`).

2. BR-002 - Fixed sort code
Sort code is not supplied by caller input. INQACCCU uses internal constant `SORTCODE` with value `987654`.

3. BR-003 - Fixed-width identifier handling
Customer number (`PIC 9(10)`) and account number (`PIC 9(8)`) are fixed-width identifiers in legacy structures; leading zeroes are significant and must be preserved in legacy-compatible representations.

4. BR-004 - Customer validation sequence
INQACCCU performs customer validation before account retrieval by linking to program `INQCUST` (`CUSTOMER-CHECK` section).

5. BR-005 - Customer existence decision source
Customer existence is determined by `INQCUST-INQ-SUCCESS`, not by account row count.

6. BR-006 - Reserved customer number: zero value
Customer number `0000000000` (numeric zero check) follows customer-not-found path in INQACCCU.

7. BR-007 - Reserved customer number: all nines
Customer number `9999999999` follows customer-not-found path in INQACCCU.

8. BR-008 - Customer-not-found outcome
When customer validation fails/not found, INQACCCU returns `CUSTOMER-FOUND = 'N'`, `COMM-SUCCESS = 'N'`, `COMM-FAIL-CODE = '1'`, and zero accounts.

9. BR-009 - Valid customer with zero accounts
A valid customer may return zero accounts and this is a successful outcome (`CUSTOMER-FOUND = 'Y'`, `COMM-SUCCESS = 'Y'`, `NUMBER-OF-ACCOUNTS = 0`).

10. BR-010 - Valid customer with one or more accounts
When account rows are fetched successfully, `CUSTOMER-FOUND = 'Y'`, `COMM-SUCCESS = 'Y'`, and `NUMBER-OF-ACCOUNTS` is populated from 1 to 20.

11. BR-011 - Account retrieval filter
Account rows are retrieved using both customer number and fixed sort code in cursor WHERE predicates.

12. BR-012 - Returned account fields (per occurrence)
INQACCCU returns these account fields:
- `COMM-EYE`
- `COMM-CUSTNO`
- `COMM-SCODE`
- `COMM-ACCNO`
- `COMM-ACC-TYPE`
- `COMM-INT-RATE`
- `COMM-OPENED`
- `COMM-OVERDRAFT`
- `COMM-LAST-STMT-DT`
- `COMM-NEXT-STMT-DT`
- `COMM-AVAIL-BAL`
- `COMM-ACTUAL-BAL`

13. BR-013 - Date conversion in account output
DB2 account date values are fetched into character host variables and rearranged by INQACCCU into `DDMMYYYY` format for `COMM-OPENED`, `COMM-LAST-STMT-DT`, and `COMM-NEXT-STMT-DT`.

14. BR-014 - Status fields used by this program
Legacy status/summary fields are `COMM-SUCCESS`, `COMM-FAIL-CODE`, `CUSTOMER-FOUND`, and `NUMBER-OF-ACCOUNTS`.

15. BR-015 - Failure code meanings
INQACCCU sets failure codes as follows:
- `1` customer validation/customer-not-found path
- `2` cursor open failure
- `3` cursor fetch failure
- `4` cursor close failure

16. BR-016 - End-of-data behavior
`SQLCODE +100` during fetch is treated as normal end-of-data, not as an error.

## Confirmed Technical and Transport Constraints

17. TC-001 - Cursor access mode
The account cursor is declared read-only with `FOR FETCH ONLY`.

18. TC-002 - Ordering behavior
The cursor SQL contains no `ORDER BY`; INQACCCU does not guarantee deterministic account ordering.

19. TC-003 - Maximum returned rows per call
INQACCCU returns at most 20 account rows because the commarea and fetch loop are bounded at 20. This is a confirmed program/transport limit, not evidence of a business policy on maximum accounts a customer may own.

20. TC-004 - No source-evidenced row de-duplication
INQACCCU contains no explicit de-duplication logic for fetched account rows.

21. TC-005 - Eyecatcher treatment in this program
Eyecatcher is copied to output (`COMM-EYE`) as a field. No explicit executable check in INQACCCU rejects rows based on eyecatcher value mismatch.

## Explicitly Unresolved Source Questions

1. UQ-001 - Null handling details
INQACCCU source shown does not define explicit indicator-variable handling for nullable DB2 fields in this cursor fetch path; null-field behavior requires additional DB2/host-variable evidence.

2. UQ-002 - Physical DB constraints
Primary-key and foreign-key constraints are not proven by the copybook declarations alone and require database DDL evidence.

3. UQ-003 - Caller-side formatting before commarea
Any upstream caller normalization/validation before INQACCCU receives `CUSTOMER-NUMBER` is outside this program source.
