# Program Analysis: INQACCCU (Legacy Behavior)

## 1. Program Inventory

| Attribute | Value |
|---|---|
| Program ID | INQACCCU |
| Author | James O'Grady |
| Language | COBOL with CICS and embedded SQL |
| Compile directives in source | `CBL CICS('SP,EDF,DLI')`, `CBL SQL` |
| Program role | Online inquiry: find accounts associated with a customer |

## 2. Authoritative Sources Used

1. `src/base/cics/cobol/INQACCCU.cbl`
2. `src/base/cics/copy/INQACCCU.cpy`
3. `src/base/cics/copy/SORTCODE.cpy`
4. `src/base/cics/copy/INQCUSTZ.cpy`
5. `src/base/cics/copy/ACCDB2.cpy`
6. Supporting cross-check only where consistent with source:
   - `specs/003-inqacccu-customer-account-relationship-modernization/supporting/business-rules.md`
   - `specs/003-inqacccu-customer-account-relationship-modernization/supporting/mapping-matrix.md`

## 3. Inputs, Fixed Values, and Keys

### 3.1 Inquiry Input

- User-supplied inquiry input is `CUSTOMER-NUMBER` in commarea (`PIC 9(10)`).
- `CUSTOMER-NUMBER` is numeric and fixed length in the commarea definition.
- Because the field is fixed-width numeric, leading zeroes are significant and must be preserved in legacy-compatible representations.

### 3.2 Internally Fixed Sort Code

- INQACCCU does not take sort code from caller input.
- It uses constant `SORTCODE` from copybook with value `987654` (`77 SORTCODE PIC 9(6) VALUE 987654.`).
- Program flow copies this value into host variable before SQL open.

### 3.3 Account Identifier Handling

- Account number in output is `COMM-ACCNO PIC 9(8)`.
- Sort code and account number are fixed-width identifiers in output records.
- Legacy analysis requirement: preserve leading zeroes when represented outside COBOL numeric storage.

## 4. Customer Validation Behavior

INQACCCU validates customer existence before account retrieval:

1. `CUSTOMER-CHECK` section runs first.
2. `CUSTOMER-CHECK` links to program `INQCUST` using `INQCUST-COMMAREA`.
3. If `INQCUST-INQ-SUCCESS = 'Y'`, INQACCCU sets `CUSTOMER-FOUND = 'Y'`.
4. Otherwise, it sets `CUSTOMER-FOUND = 'N'` and zeroes `NUMBER-OF-ACCOUNTS`.

Important distinction confirmed from source:

- Customer existence is not inferred from account row count.
- A customer can be valid (`CUSTOMER-FOUND = 'Y'`) and still return zero accounts.

Outcome states:

1. Customer validation failed / customer not found:
   - `CUSTOMER-FOUND = 'N'`
   - `COMM-SUCCESS = 'N'`
   - `COMM-FAIL-CODE = '1'`
2. Customer found with zero accounts:
   - `CUSTOMER-FOUND = 'Y'`
   - `COMM-SUCCESS = 'Y'`
   - `NUMBER-OF-ACCOUNTS = 0`
   - no cursor failure code
3. Customer found with one or more accounts:
   - `CUSTOMER-FOUND = 'Y'`
   - `COMM-SUCCESS = 'Y'`
   - `NUMBER-OF-ACCOUNTS = 1..20`

## 5. Reserved Customer Numbers

The following values are explicitly handled in `CUSTOMER-CHECK` and immediately treated as not found:

- `0000000000` (checked using numeric zero comparison)
- `9999999999`

For both values, INQACCCU sets:

- `CUSTOMER-FOUND = 'N'`
- `NUMBER-OF-ACCOUNTS = 0`

Then mainline logic sets customer-not-found failure path:

- `COMM-SUCCESS = 'N'`
- `COMM-FAIL-CODE = '1'`

These are not random/latest lookup modes in INQACCCU.

## 6. Account Retrieval and Cursor Behavior

### 6.1 Selection Criteria

Cursor `ACC-CURSOR` query criteria:

- `ACCOUNT_CUSTOMER_NUMBER = :HV-ACCOUNT-CUST-NO`
- `ACCOUNT_SORTCODE = :HV-ACCOUNT-SORTCODE` (fixed sort code path)

### 6.2 Cursor Characteristics

- Declared with `FOR FETCH ONLY`.
- No `ORDER BY` clause in cursor definition.
- Returned order is therefore whatever DB2 access path provides; no deterministic business ordering is guaranteed by source.

### 6.3 Row Limit

- Fetch loop stops when either:
  - SQLCODE is non-zero, or
  - `NUMBER-OF-ACCOUNTS = 20`
- Maximum returned account records per call: 20.

### 6.4 SQLCODE +100

- `SQLCODE = +100` during fetch is treated as normal end-of-data.
- Program sets `COMM-SUCCESS = 'Y'` and exits fetch section without failure code escalation.

## 7. Returned Account Data (Complete Legacy Set)

Per returned occurrence in `ACCOUNT-DETAILS`:

1. `COMM-EYE` (eyecatcher)
2. `COMM-CUSTNO` (customer number)
3. `COMM-SCODE` (sort code)
4. `COMM-ACCNO` (account number)
5. `COMM-ACC-TYPE` (account type)
6. `COMM-INT-RATE` (interest rate)
7. `COMM-OPENED` (opened date)
8. `COMM-OVERDRAFT` (overdraft limit)
9. `COMM-LAST-STMT-DT` (last statement date)
10. `COMM-NEXT-STMT-DT` (next statement date)
11. `COMM-AVAIL-BAL` (available balance)
12. `COMM-ACTUAL-BAL` (actual balance)

No source evidence in INQACCCU output for separate account lifecycle status fields such as Active/Inactive/Closed.

## 8. Date Handling

Source date representations in this flow:

1. DB2 columns are `DATE` (`ACCOUNT_OPENED`, `ACCOUNT_LAST_STATEMENT`, `ACCOUNT_NEXT_STATEMENT`) from `ACCDB2` declaration.
2. Host variables are character `PIC X(10)` and receive DB2 date text form (yyyy-mm-dd representation).
3. INQACCCU moves host date text into `DB2-DATE-REFORMAT` and builds commarea date fields by concatenating day + month + year.

Result in commarea:

- `COMM-OPENED`, `COMM-LAST-STMT-DT`, `COMM-NEXT-STMT-DT` are output as `DDMMYYYY` numeric text layout.

If a modern representation is needed, it is an external transformation concern; legacy commarea format here is `DDMMYYYY`.

## 9. Status and Failure Behavior

Legacy status/control fields in commarea:

- `COMM-SUCCESS`
- `COMM-FAIL-CODE`
- `CUSTOMER-FOUND`
- `NUMBER-OF-ACCOUNTS`

Failure code meanings confirmed in source:

1. `COMM-FAIL-CODE = '1'`: customer validation / customer-not-found path
2. `COMM-FAIL-CODE = '2'`: cursor open failure
3. `COMM-FAIL-CODE = '3'`: cursor fetch failure
4. `COMM-FAIL-CODE = '4'`: cursor close failure

Additional observed behavior:

- Program initializes `COMM-SUCCESS = 'N'`, `COMM-FAIL-CODE = '0'` at entry.
- On SQL cursor failures (open/fetch/close), program sets failure code and performs rollback attempt.

## 10. Processing Sequence Summary

1. Initialize response status defaults.
2. Set required sort code from fixed `SORTCODE` constant.
3. Run `CUSTOMER-CHECK` (including reserved-number handling and INQCUST link).
4. If customer check fails: fail code `1` and return.
5. Open account cursor by customer + fixed sort code.
6. Fetch rows until end-of-data, error, or 20 rows.
7. Map each fetched row into `ACCOUNT-DETAILS` occurrence and convert dates to `DDMMYYYY`.
8. Close cursor.
9. On successful path, return with `COMM-SUCCESS = 'Y'`.

## 11. Confirmed Constraints and Explicit Unknowns

### 11.1 Confirmed Constraints

- One inquiry input: customer number.
- Internal fixed sort code: `987654`.
- Maximum of 20 account rows returned.
- Read-only cursor (`FOR FETCH ONLY`).
- No explicit SQL ordering (`ORDER BY` absent).

### 11.2 Explicit Unknowns (Not Claimed as Facts)

- Physical table key constraints (PK/FK) are not declared in the copybook include shown; do not treat key assumptions as confirmed from this source alone.
- Exact caller-side formatting/validation behavior before commarea population is outside this program.
- Character-space trimming behavior for account text fields is not explicitly performed in this program logic.