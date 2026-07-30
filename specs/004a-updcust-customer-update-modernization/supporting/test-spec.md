# Test Specification: UPDCUST

## 1. Unit Tests
- UT-001 Title allow-list valid values pass.
- UT-002 Invalid title fails with T.
- UT-003 Minimum payload gate fail with 4.
- UT-004 sortCode fallback logic when missing.
- UT-005 Name/title gate updates only when firstName non-blank.
- UT-006 Address gate updates only when addressLine1 non-blank.
- UT-007 Phone gate updates only when non-blank.
- UT-008 Status gate updates only when non-blank.
- UT-009 DOB updates only when year provided.
- UT-010 First-character space values are treated as blank in gate checks.
- UT-011 customerNumber normalization left-pads numeric values to 10 digits.
- UT-012 status accepts non-blank value without allow-list enforcement in parity mode.

## 2. Service Tests
- ST-001 Not found from repository -> fail 1.
- ST-002 Repository select failure -> fail 2.
- ST-003 Repository update failure -> fail 3.
- ST-004 Full success path -> success Y and blank fail code.
- ST-005 Immutable fields preserved after update.
- ST-006 No-op payload that passes validation returns parity-consistent success.

## 3. Controller Tests
- CT-001 Success returns 200 with updated payload.
- CT-002 Invalid schema/format returns 400.
- CT-003 Legacy business-rule failures return 422 with legacyFailCode.
- CT-004 Not found returns 404 with legacyFailCode 1.
- CT-005 Internal failures return 500 with correlationId.

## 4. Integration Tests
- IT-001 Update by explicit sortCode + customerNumber.
- IT-002 Update by customerNumber with sortCode fallback.
- IT-003 Verify trimmed output and ISO date conversion.
- IT-004 Verify parity behavior for title-only and address-partial update attempts.
- IT-005 Verify first-character-space payload handling for gate checks.
- IT-006 Verify customer lookup works with normalizable (short) numeric customerNumber input.

## 5. Frontend Tests (Spec Requirement)
- FT-001 Update Customer button visible on inquiry success.
- FT-002 Update Customer button hidden when inquiry failed.
- FT-003 Edit form prefilled with selected customer fields.
- FT-004 Submission shows mapped validation or business-rule failures.

## 6. Fail-Code Coverage Matrix
- T: invalid title
- 1: customer not found
- 2: select/read failure
- 3: update/write failure
- 4: insufficient meaningful payload

All fail codes are mandatory test coverage.
