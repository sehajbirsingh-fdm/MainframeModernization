# copilot-quality-checklist.md

Use this after Copilot generates code.

## Compile
- [ ] `mvn clean test` passes.
- [ ] No compilation warnings that matter.

## Spec alignment
- [ ] Endpoint path is exactly `/api/v1/customers/{sortCode}/{customerNumber}`.
- [ ] All fields from mapping matrix are present.
- [ ] No extra customer fields were invented.
- [ ] `0000000000` random lookup implemented.
- [ ] `9999999999` latest lookup implemented.
- [ ] Legacy status values are present in responses.

## Architecture
- [ ] Controller has no business logic.
- [ ] Service layer orchestrates behavior.
- [ ] Repository interface exists.
- [ ] Mock repository is isolated.
- [ ] No DB2, CICS, IMS, or real mainframe calls.

## Tests
- [ ] All test cases from `test-spec.md` implemented.
- [ ] Controller tests cover 200, 400, 404.
- [ ] Date conversion tested.
- [ ] Risk assessment tested.
- [ ] Random lookup deterministic in tests.

## Demo readiness
- [ ] Swagger works.
- [ ] Curl examples work.
- [ ] Mock data covers all demo scenarios.
- [ ] README explains future adapter replacement.
