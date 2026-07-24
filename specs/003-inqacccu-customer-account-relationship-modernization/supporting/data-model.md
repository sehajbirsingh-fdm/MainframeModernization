# data-model.md - INQACCCU Legacy Business Data Model

## Scope

This document describes business data handled by legacy INQACCCU based on COBOL source and referenced copybooks. It describes legacy structures and observable transformations only.

## Confirmed Legacy Data Structures

### 1. Inquiry Input

| Field | Legacy representation | Business meaning | Source notes |
|---|---|---|---|
| Customer number | `CUSTOMER-NUMBER PIC 9(10)` | Customer identifier provided for inquiry | Fixed-width identifier; leading zeroes are significant in legacy-compatible representation. |

### 2. Internally Derived Inquiry Value

| Field | Legacy representation | Business meaning | Source notes |
|---|---|---|---|
| Sort code (internal constant) | `SORTCODE PIC 9(6) VALUE 987654` | Program-supplied sort code used for account retrieval | Not caller input; copied into SQL host variable in INQACCCU. |

### 3. Inquiry Status and Summary

| Field | Legacy representation | Business meaning | Source notes |
|---|---|---|---|
| COMM-SUCCESS | `PIC X` | Overall inquiry success indicator | Program initializes `N`; sets `Y` on successful processing paths. |
| COMM-FAIL-CODE | `PIC X` | Failure code for legacy error paths | Confirmed values used: `0`,`1`,`2`,`3`,`4`. |
| CUSTOMER-FOUND | `PIC X` | Customer validation result indicator | Set from INQCUST validation result, not inferred from account row count. |
| NUMBER-OF-ACCOUNTS | `PIC S9(8) BINARY` | Number of account records returned | Transport/program count for returned occurrences (max 20). |

### 4. Returned Account Record (repeating group)

`ACCOUNT-DETAILS OCCURS 1 TO 20 DEPENDING ON NUMBER-OF-ACCOUNTS`

| Field | Legacy commarea representation | Source DB2 column | Business meaning |
|---|---|---|---|
| COMM-EYE | `PIC X(4)` | ACCOUNT_EYECATCHER | Account record eyecatcher copied from source row. |
| COMM-CUSTNO | `PIC X(10)` | ACCOUNT_CUSTOMER_NUMBER | Customer identifier on account row. |
| COMM-SCODE | `PIC X(6)` | ACCOUNT_SORTCODE | Sort code on account row. |
| COMM-ACCNO | `PIC 9(8)` | ACCOUNT_NUMBER | Account identifier (fixed width). |
| COMM-ACC-TYPE | `PIC X(8)` | ACCOUNT_TYPE | Account type code/text as stored. |
| COMM-INT-RATE | `PIC 9(4)V99` | ACCOUNT_INTEREST_RATE | Interest rate value from account row. |
| COMM-OPENED | `PIC 9(8)` | ACCOUNT_OPENED (DATE) | Opened date in commarea output format. |
| COMM-OVERDRAFT | `PIC 9(8)` | ACCOUNT_OVERDRAFT_LIMIT | Overdraft limit value from account row. |
| COMM-LAST-STMT-DT | `PIC 9(8)` | ACCOUNT_LAST_STATEMENT (DATE) | Last statement date in commarea output format. |
| COMM-NEXT-STMT-DT | `PIC 9(8)` | ACCOUNT_NEXT_STATEMENT (DATE) | Next statement date in commarea output format. |
| COMM-AVAIL-BAL | `PIC S9(10)V99` | ACCOUNT_AVAILABLE_BALANCE | Available balance value from account row. |
| COMM-ACTUAL-BAL | `PIC S9(10)V99` | ACCOUNT_ACTUAL_BALANCE | Actual balance value from account row. |

## Confirmed Data Transformations

### 1. Identifier preservation

- Customer number and account number are fixed-width identifiers in legacy structures.
- Leading zeroes must be preserved in legacy-compatible representations.
- These fields should not be treated as numeric business quantities.

### 2. Date representation across layers

- DB2 source columns are declared as `DATE`.
- INQACCCU fetches account dates through host variables defined as `PIC X(10)`.
- INQACCCU rearranges date parts to commarea output format `DDMMYYYY` for:
  - `COMM-OPENED`
  - `COMM-LAST-STMT-DT`
  - `COMM-NEXT-STMT-DT`
- A future ISO date representation may exist in downstream modernization layers, but it is not the legacy commarea format.

### 3. Internally fixed sort code usage

- INQACCCU derives sort code from internal constant `987654` and uses it for account retrieval.
- Sort code is not a user-supplied inquiry data element in this program.

## Confirmed Model Boundaries

- This data model includes only data structures evidenced in INQACCCU and referenced copybooks.
- No additional business entities are confirmed for this program beyond inquiry input/status and account-detail records.

## Unresolved Data Questions (Evidence Needed)

| Topic | Current status |
|---|---|
| Null handling for nullable DB2 account columns in this fetch path | Not confirmed from INQACCCU source because no indicator-variable handling is shown. |
| Physical DB constraints (PK/FK/index/uniqueness) | Not confirmed by supplied copybooks alone; requires DB DDL evidence. |
| Definitive business enumeration for account type values | Not defined in INQACCCU/copybooks provided; only raw field carriage is confirmed. |
