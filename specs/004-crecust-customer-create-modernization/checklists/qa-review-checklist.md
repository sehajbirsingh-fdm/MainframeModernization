# QA Review Checklist - CRECUST

## Functional QA
- [ ] Happy path create returns 201 with expected fields.
- [ ] Invalid title returns mapped error and fail code `T`.
- [ ] DOB invalid scenarios return mapped fail codes (`O`, `Z`, `Y`).
- [ ] Control-state failure returns fail code `4` mapping.
- [ ] Persistence failure returns fail code `1` mapping.

## Contract QA
- [ ] Request schema validation enforced.
- [ ] Response schema fields complete and correctly typed.
- [ ] Error schema includes `legacyFailCode` where applicable.

## Traceability QA
- [ ] Every requirement has at least one automated test.
- [ ] Every business rule has at least one automated test.
- [ ] Every legacy fail code has at least one automated test.
