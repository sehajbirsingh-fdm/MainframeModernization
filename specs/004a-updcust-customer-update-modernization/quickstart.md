# Quickstart: UPDCUST Customer Update Feature

## Purpose
Use this quickstart to validate end-to-end UPDCUST modernization behavior after implementation.

## Preconditions
- Backend API running.
- Existing customer data available in repository.
- Known customerNumber for test cases.

## Suggested Manual Test Sequence

1. Success update path
- Call PUT /api/v1/customers/{customerNumber} with valid title and firstName.
- Expect HTTP 200.
- Expect legacyStatus.updSuccess = Y.

2. Invalid title path
- Send title outside allow-list (example: Captain).
- Expect legacy fail code T.

3. Minimum meaningful update failure
- Send blank firstName, blank lastName, blank addressLine1.
- Expect legacy fail code 4.

4. Not found path
- Use non-existent customerNumber.
- Expect 404 + legacy fail code 1.

5. Select failure and update failure simulation
- Force repository read/write failure in test mode.
- Expect fail codes 2 and 3 respectively.

6. Gating parity checks
- Send title only with blank firstName: title should not change.
- Send addressLine2/city/postcode/country with blank addressLine1: address should not change.

7. UI placement check
- Open inquiry success result for existing customer.
- Confirm Update Customer action is visible and routes to edit form with prefilled values.

## Exit Conditions
- All fail codes T/1/2/3/4 are observed in their intended paths.
- Selective field update gates behave exactly as legacy.
- No non-copybook fields are required by API.
