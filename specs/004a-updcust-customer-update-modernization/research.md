# Research: UPDCUST Legacy Behavior Extraction

Date: 2026-07-30
Feature: 004a-updcust-customer-update-modernization

## Sources Reviewed
- legacy-bankofz/base/cics/cobol/UPDCUST.cbl
- legacy-bankofz/base/cics/copy/UPDCUST.cpy
- legacy-bankofz/base/cics/copy/CUSTOMER.cpy
- legacy-bankofz/base/cics/copy/CUSTDB2.cpy
- legacy-bankofz/base/cics/copy/SORTCODE.cpy
- legacy-bankofz/base/cics/copy/ABNDINFO.cpy

## Confirmed Legacy Rules
1. Title allow-list validation is explicit and returns fail code T on invalid value.
2. Update request fails with fail code 4 when firstName, lastName, and addressLine1 are all blank.
3. Customer lookup uses sortCode + customerNumber, with sortCode fallback when not supplied.
4. SQL select not-found returns fail code 1.
5. SQL select failure (non-zero, non-100) returns fail code 2.
6. SQL update failure returns fail code 3.
7. Name/title updates only when firstName is non-blank.
8. Address fields update only when addressLine1 is non-blank.
9. Phone updates only when non-blank.
10. Status updates only when non-blank.
11. DOB updates only when year is provided; legacy code does not perform calendar validation here.

## Design Decision For Professional UX Placement
- Update action should be placed on inquiry success result.
- This avoids detached editing flow and uses verified key context.
- Proposed route: /customers/{sortCode}/{customerNumber}/edit.

## Open Clarification Targets For Implementation Phase
- Whether to support patch-style sparse payloads or strict full payload with blanks for unchanged values.
- Whether to keep DOB validation parity-only or add optional hardening mode behind config.
- Whether customerStatus should be constrained to known statuses at API layer or left parity-open.
