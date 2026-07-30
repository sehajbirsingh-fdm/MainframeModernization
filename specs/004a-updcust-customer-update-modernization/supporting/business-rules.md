# Business Rules: UPDCUST

## BR-001 Title Allow-List
Valid titles:
- Professor, Mr, Mrs, Miss, Ms, Dr, Drs, Lord, Sir, Lady, blank.
Invalid title => fail T.

## BR-002 Target Key Resolution
- customerNumber required.
- sortCode optional, fallback to configured SORTCODE behavior when absent.
- customerNumber normalized via trim + numeric parse + zero-left-pad to 10 digits.

## BR-003 Minimum Meaningful Payload Gate
If firstName, lastName, and addressLine1 are all blank, reject with fail 4.
For parity, values whose first character is space are treated as blank.

## BR-004 Customer Existence
No matching customer => fail 1.

## BR-005 Read Failure
Repository read/select failure => fail 2.

## BR-006 Write Failure
Repository update failure => fail 3.

## BR-007 Name/Title Gate
Name/title are updated only when firstName is non-blank.

## BR-008 Address Gate
Address fields are updated only when addressLine1 is non-blank.

## BR-009 Phone Gate
Phone is updated only when non-blank.

## BR-010 Status Gate
Status is updated only when non-blank.
No status allow-list validation is applied in strict parity mode.

## BR-011 DOB Gate
DOB is updated only when year is provided (non-zero/non-blank).

## BR-012 Success Status
On success:
- updSuccess = Y
- updFailCode = blank

## BR-013 No-Op Success Parity
Payload may pass BR-003 yet still produce no effective update if gate-driving fields are blank by first-character rule; this returns success in parity mode.
