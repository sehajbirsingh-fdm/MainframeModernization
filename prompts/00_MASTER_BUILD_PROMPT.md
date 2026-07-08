# Master build prompt

Act as a senior Java/Spring Boot modernization engineer and mainframe modernization architect.

You are in a repository containing a full SDD package for modernizing the legacy COBOL CICS program INQCUST into a Spring Boot customer inquiry API.

Before generating code, read these files:

1. `.github/copilot-instructions.md`
2. `docs/spec.md`
3. `docs/mapping-matrix.md`
4. `docs/data-dictionary.md`
5. `docs/domain-model.md`
6. `docs/architecture.md`
7. `docs/test-spec.md`
8. `openapi/openapi.yaml`
9. `mock-data/customer-records.json`

Build exactly what the spec defines.

Do not invent fields.
Do not connect to a real database.
Do not create frontend code.
Do not use field injection.
Do not skip tests.

Generate a Java 21 Spring Boot 3 Maven service implementing `GET /api/v1/customers/{sortCode}/{customerNumber}`.

The implementation must support:
- specific customer lookup
- latest customer lookup for `9999999999`
- random customer lookup for `0000000000`
- legacy status response values
- risk assessment enhancement
- validation
- exception handling
- unit tests
- controller tests
- Swagger/OpenAPI support

After creating files, explain what was created and list commands to run tests and start the service.
