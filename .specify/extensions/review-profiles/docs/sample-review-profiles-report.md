# Sample API Review Report

This sample shows what `/speckit.review-profiles.review` should produce for a
example demo. It is intentionally written as if the reviewer found issues.

Example saved path:

```text
specs/004-customer-summary-api/reviews/review-v001-20260610-143012.md
```

## Report Metadata

- Report version: v001
- Generated at: 2026-06-10T14:30:12-04:00
- Timestamp: 20260610-143012
- Profile: api
- Result: NEEDS WORK
- Feature directory: specs/004-customer-summary-api
- Base branch: main
- Head commit: abc1234
- Report file: specs/004-customer-summary-api/reviews/review-v001-20260610-143012.md

## Review Result

- Profile: api
- Result: NEEDS WORK
- Blockers: 1
- Major issues: 3
- Minor issues: 2
- Info notes: 1

## Executive Summary

The implementation adds a new customer summary API, but it is not ready for
human approval yet. The main concern is missing authorization on the new
endpoint. The change also appears to update the response contract without an
OpenAPI update or negative-path tests. QA should not begin broad regression
testing until the blocker and major test gaps are resolved.

## Changed Files Reviewed

- `src/api/customers/routes.ts`
- `src/services/customer-summary-service.ts`
- `src/models/customer-summary.ts`
- `tests/customer-summary.test.ts`
- `specs/004-customer-summary-api/spec.md`
- `specs/004-customer-summary-api/tasks.md`

## Rules Loaded

- `rules/api-code-review-rules.md`
- `rules/security-code-quality-rules.md`

## Findings

### BLOCKER-001: New customer summary endpoint does not enforce authorization

- Evidence: `src/api/customers/routes.ts:42` registers
  `GET /api/customers/:id/summary`, but the route only checks authentication and
  does not verify that the caller can access the requested customer.
- Why it matters: A signed-in user could potentially request another customer's
  summary data by changing the path parameter.
- Suggested fix: Add the same customer-level authorization check used by the
  existing `GET /api/customers/:id` endpoint, then add a regression test for a
  user requesting a customer they do not own.
- Recommended owner: Dev Lead

### MAJOR-001: Public API contract changed without OpenAPI update

- Evidence: `riskScore` was added to the customer summary response in
  `src/models/customer-summary.ts`, but no OpenAPI or schema file changed.
- Why it matters: API consumers and QA contract tests will not know that the
  response shape changed.
- Suggested fix: Update the API schema and include an example response showing
  the new field and its nullability rules.
- Recommended owner: Dev

### MAJOR-002: Missing validation failure tests

- Evidence: `tests/customer-summary.test.ts` covers the happy path, but there is
  no test for malformed customer IDs or unauthorized customer access.
- Why it matters: The most likely failures for this endpoint are boundary and
  access-control failures, not the happy path.
- Suggested fix: Add tests for invalid ID format, missing authentication,
  authenticated but unauthorized user, and customer not found.
- Recommended owner: QA

### MAJOR-003: Error response shape does not match existing API pattern

- Evidence: The new route returns `{ message: "Not found" }`, while nearby
  routes return `{ error: { code, message, requestId } }`.
- Why it matters: Inconsistent errors break API consumer handling and make support
  troubleshooting harder.
- Suggested fix: Reuse the shared error response helper used by existing
  customer endpoints.
- Recommended owner: Dev

### MINOR-001: Add structured logging for failed summary lookups

- Evidence: Failed summary generation returns an error response but does not log
  customer ID, request ID, or failure category.
- Why it matters: Support will have less context when investigating production
  failures.
- Suggested fix: Add structured warning logs without including PII.
- Recommended owner: Dev

### MINOR-002: Task completion evidence is incomplete

- Evidence: `tasks.md` marks contract docs complete, but no schema or OpenAPI
  file changed in the diff.
- Why it matters: Reviewers need to trust that checked tasks map to real
  implementation evidence.
- Suggested fix: Either add the missing schema change or mark the task
  incomplete.
- Recommended owner: Dev

## Spec Traceability

| Spec/task item | Evidence found | Status | Notes |
| --- | --- | --- | --- |
| Add customer summary endpoint | Route and service added | Complete | Needs authz fix |
| Return risk score in response | Model updated | Partial | Missing API schema update |
| Add endpoint tests | Happy-path test only | Partial | Missing negative tests |
| Update contract docs | No matching diff | Missing | Major issue |

## Test Coverage Review

| Required test area | Evidence found | Status | Notes |
| --- | --- | --- | --- |
| Happy path | Present | Pass | Covers one valid customer |
| Authentication failure | Not found | Fail | Add 401 test |
| Authorization failure | Not found | Fail | Add 403 test |
| Validation failure | Not found | Fail | Add malformed ID test |
| Backward compatibility | Not found | Fail | Add contract/schema test |

## QA Notes

- Verify a user cannot access another customer's summary by changing the ID.
- Verify invalid customer IDs return the standard validation error shape.
- Verify customer-not-found returns the existing API error contract.
- Verify the new `riskScore` field is documented and stable for API consumers.
- Run contract tests after OpenAPI/schema updates are added.

## PR Comment Draft

This first-pass API review found 1 blocker and 3 major issues. The merge should
wait until customer-level authorization is added to the new summary endpoint,
the API schema is updated, and negative-path tests cover auth, validation, and
not-found behavior. Human review should focus next on the authorization pattern
and whether `riskScore` is safe to expose to API consumers.
