# research.md - INQACCCU Legacy Research Findings

## Scope

This research summarizes source-grounded observations from INQACCCU and referenced copybooks. It separates confirmed legacy behavior from possible modernization considerations.

## Confirmed Legacy Observations

1. Customer validation occurs before account retrieval.
- INQACCCU runs CUSTOMER-CHECK and links to INQCUST.
- CUSTOMER-FOUND is set from INQCUST-INQ-SUCCESS, not from account-row count.

2. Inquiry input and internally supplied value are distinct.
- Customer number is the inquiry input (fixed-width identifier).
- Sort code is not caller input; it is internally fixed by SORTCODE copybook value 987654.

3. Account retrieval behavior is constrained and read-only.
- Cursor is declared FOR FETCH ONLY.
- SQL predicate uses both customer number and sort code.
- Query has no ORDER BY, so row ordering is not guaranteed by business logic.

4. Return-size constraint is program/transport bound.
- INQACCCU returns at most 20 account rows due to OCCURS 1 TO 20 and fetch-loop limit.

5. End-of-data and error handling are distinct.
- SQLCODE +100 during fetch is normal end-of-data.
- Confirmed failure-code paths:
  - 1: customer validation/customer-not-found
  - 2: cursor open failure
  - 3: cursor fetch failure
  - 4: cursor close failure

6. Valid customer with zero accounts is a supported legacy outcome.
- CUSTOMER-FOUND can be Y with NUMBER-OF-ACCOUNTS = 0 and success path retained.

7. Legacy account data shape is explicit.
- Returned account fields are:
  - eyecatcher
  - customer number
  - sort code
  - account number
  - account type
  - interest rate
  - opened date
  - overdraft limit
  - last statement date
  - next statement date
  - available balance
  - actual balance

8. Date handling includes an internal representation change.
- DB2 account dates are declared as DATE.
- INQACCCU receives date text in host variables and rearranges to DDMMYYYY in commarea output fields.

9. Identifier handling requires fixed-width preservation.
- Customer number and account number must preserve leading zeroes in legacy-compatible handling.

## Confirmed Legacy Constraints

- Maximum returned account records per call: 20.
- Account order is undefined by source (no ORDER BY).
- Customer validation is mandatory before account retrieval.
- Sort code used by this program is internally fixed to 987654.

## Modernization Observations (Non-Prescriptive)

- Downstream interfaces may need an explicit representation strategy for fixed-width identifiers so leading zeroes are preserved.
- Downstream interfaces may need explicit date-format translation rules because legacy commarea output is DDMMYYYY, while other interfaces commonly use ISO date formats.
- Any behavior beyond the 20-record legacy bound should be treated as future design scope, not current legacy behavior.

## Unresolved Questions Requiring Additional Evidence

1. Nullability handling in this fetch path.
- INQACCCU source does not show indicator-variable handling for nullable DB2 columns in this query path.

2. Physical database constraints.
- PK, FK, index, and uniqueness constraints are not confirmed by the provided copybook declarations alone.

3. Account-type value semantics.
- Source confirms field carriage but does not provide authoritative enumeration semantics for account type in the provided artifacts.

4. Additional database DDL evidence.
- Full DDL would be needed to confirm column-level nullability constraints and physical keys.
