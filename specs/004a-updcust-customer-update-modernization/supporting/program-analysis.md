# Program Analysis: UPDCUST Customer Update Modernization

Legacy source: legacy-bankofz/base/cics/cobol/UPDCUST.cbl
Copybooks: UPDCUST.cpy, CUSTOMER.cpy, CUSTDB2.cpy, SORTCODE.cpy, ABNDINFO.cpy

## 1. Program Identity
- Program ID: UPDCUST
- Purpose: Update selected customer fields.
- Input/Output contract: DFHCOMMAREA using UPDCUST copybook.

## 2. Confirmed Processing Sequence
1. Resolve sort code input into desired key.
2. Validate title using explicit allow-list.
3. If invalid title: return fail T.
4. Validate minimum meaningful payload (name/address gate).
5. Resolve sortCode (fallback to SORTCODE when blank).
6. Parse customer number and select existing CUSTOMER row.
7. If not found: fail 1.
8. If select SQL error: fail 2.
9. Conditionally copy changed fields based on non-blank gate logic.
10. Execute DB2 update.
11. If update SQL error: fail 3.
12. Move updated values back to COMMAREA and set success Y.

## 3. Validation Evidence
- Title validation values: Professor, Mr, Mrs, Miss, Ms, Dr, Drs, Lord, Sir, Lady, spaces.
- Minimum payload failure: firstName blank AND lastName blank AND addressLine1 blank -> fail 4.

## 4. Selective Update Behavior
- Name/title update block runs only when firstName first character is non-blank.
- Address block update runs only when addressLine1 first character is non-blank.
- Phone updates only when first character non-blank.
- Status updates only when first character non-blank.
- DOB updates only when DOB year is non-zero.

## 5. Fail Codes Observed
- T: invalid title
- 1: customer not found
- 2: select/read error
- 3: update/write error
- 4: insufficient meaningful update payload

## 6. Modernization Implications
- Preserve fail-code semantics verbatim.
- Preserve selective update gates even if they feel unintuitive.
- Keep response-level legacy status to support parity and troubleshooting.
- Place update action in inquiry success context for professional UX continuity.
