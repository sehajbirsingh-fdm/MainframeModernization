# Test Specification - CRECUST Modernization

## 1. Coverage Goals
- 100% business-rule coverage for BR-001 to BR-013.
- 100% legacy fail-code coverage for codes used by CRECUST.
- Controller-level coverage for all documented HTTP statuses.

## 2. Unit Test Set
- Title allowlist tests.
- DOB validation tests (year lower bound, invalid date, future date, age >150).
- Credit score averaging and fallback tests.
- Customer-number generation sequence tests.
- Date conversion tests (`YYYYMMDD` <-> ISO).

## 3. Service Integration Tests
- Happy path create.
- Fail code `T` path.
- Fail code `O`/`Z`/`Y` DOB paths.
- Fail code `4` control-state failure path.
- Fail code `1` persistence failure path.
- Representative credit fail-code paths (`A`..`H`).

## 4. Controller Tests
- `201` on success.
- `400` on malformed payload.
- `400` payload-validation paths return `legacyFailCode` value `0`.
- `422` on semantic business-rule failures.
- `503` on repository/control unavailability.
- `500` on unhandled failures.

## 5. Contract Tests
- Validate schema conformance with `contracts/openapi.yaml`.
- Validate required fields and date formats.
- Validate `legacyStatus` and error envelope shape.

## 6. Traceability Rule
Every test case must reference at least one requirement ID and one business-rule ID.
