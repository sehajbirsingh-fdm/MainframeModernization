# INQCUST, INQACC, and INQACCCU API Module

This module is the Spring Boot implementation of:

- INQCUST customer inquiry
- INQACC account inquiry
- INQACCCU customer-account relationship inquiry

This README reflects the current implementation.

## Runtime API

- Endpoint: `GET /api/v1/customers/{sortCode}/{customerNumber}`
- Controller mapping:
	- Base path: `/api/v1/customers`
	- Method path: `/{sortCode}/{customerNumber}`
- Validation:
	- `sortCode` must be 6 digits
	- `customerNumber` must be 10 digits

- Endpoint: `GET /v1/accounts/{sortcode}/{accountNumber}`
	- Requires bearer token and role `ACCOUNT_INQUIRER`
	- Validation:
		- `sortcode` must be 6 digits
		- `accountNumber` must be 8 digits

- Endpoint: `GET /api/v1/customers/{customerNumber}/accounts`
	- INQACCCU controller mapping:
		- Base path: `/api/v1/customers`
		- Method path: `/{customerNumber}/accounts`
	- Validation:
		- `customerNumber` must be 10 digits
	- Business outcomes are returned in HTTP 200 with `legacyStatus` (`success`/`failCode`/`customerFound`).
	- Validation and infrastructure failures are returned as HTTP 400/500 error payloads.

### Special Customer Numbers

- `0000000000` = RANDOM lookup mode
- `9999999999` = LATEST lookup mode
- Any other 10-digit number = SPECIFIC lookup mode

For INQACCCU:

- `0000000000` and `9999999999` are validly formatted values.
- They continue through business flow and return customer-not-found business outcomes (not HTTP 400).

### Special Account Numbers

- `99999999` = return highest account number for the given sortcode
- Any other 8-digit number = standard composite-key lookup mode

## Data Source Modes

Switching mode is configuration-only via `app.data.mode` in `src/main/resources/application.properties`.

- Default mode: `mock`
	- Uses `mock-data/customer-records.json`
	- INQACCCU uses `mock-data/account-relationship-records.json` via `app.inqacccu.mock-data.path`
- Optional mode: `db`
	- Uses repository-backed DB queries when DB properties are provided
	- INQACC db-mode properties:
		- `APP_INQACC_DB_URL`
		- `APP_INQACC_DB_USERNAME`
		- `APP_INQACC_DB_PASSWORD`
		- `APP_INQACC_DB_DRIVER` (optional)
		- `APP_INQACC_DB_SCHEMA` (optional)
		- `APP_INQACC_DB_MAX_POOL_SIZE` (optional)
		- `APP_INQACC_DB_MIN_IDLE` (optional)

Important: a production database is not configured in this project by default.
Live DB2 connectivity is not verified in this POC and is not required for POC acceptance.

## Repository Architecture

- `CustomerRepository` is the data access abstraction used by services.
- `MockCustomerRepository` currently provides:
	- mock JSON-backed behavior for local/default execution
	- DB-mode query paths for future integration
- Recent repository refactoring keeps service and controller behavior unchanged while supporting DB-mode latest lookup directly in SQL.

## OpenAPI And Traceability

- Active runtime OpenAPI file: `src/main/resources/openapi.yaml`
- INQACCCU frozen feature contract: `specs/003-inqacccu-customer-account-relationship-modernization/contracts/openapi.yaml`
- Build/runtime behavior should be aligned to these files, not legacy/generated artifacts.

## Build, Test, And Run

From `src/api`:

- Run tests: `mvn test`
- Run the service: `mvn spring-boot:run`

The backend starts on port `8080` by default.

Use the Spring Boot Maven commands above for local build and run.

### Running By Data Mode

- Mock mode (default)
	- No DB setup required.
	- Ensure `app.data.mode=mock` (already set by default in `src/main/resources/application.properties`).
	- Run: `mvn spring-boot:run`

- DB mode (optional)
	- Set `app.data.mode=db`.
	- Provide DB connection values:
		- `APP_DB_URL`
		- `APP_DB_USERNAME`
		- `APP_DB_PASSWORD`
	- Example (PowerShell):
		- `$env:APP_DATA_MODE='db'`
		- `$env:APP_DB_URL='jdbc:db2://host:port/database'`
		- `$env:APP_DB_USERNAME='your_user'`
		- `$env:APP_DB_PASSWORD='your_password'`
		- `$env:APP_INQACC_DB_URL='jdbc:db2://host:port/database'`
		- `$env:APP_INQACC_DB_USERNAME='your_user'`
		- `$env:APP_INQACC_DB_PASSWORD='your_password'`
		- `mvn spring-boot:run`

If DB mode is enabled without valid DB values, startup will fail by design.

## INQACC Auth Tokens For Local Verification

- `Bearer valid-inqacc-inquirer-token` -> authorized for `/v1/accounts/**`
- `Bearer valid-inqacc-limited-token` -> authenticated but forbidden (403)
- Missing/malformed/invalid token -> 401

Current INQACC authentication implementation is a deterministic development adapter that demonstrates bearer-header handling, 401/403 behavior, and role boundaries for the POC. It is not a production OAuth2 identity provider or JWT signature/issuer/expiry validation pipeline.

## Source Layout

- `src/main/java/com/bankofz/inqcust/api/controller`
	- REST endpoints and exception handling
- `src/main/java/com/bankofz/inqcust/api/service`
	- Lookup orchestration and business rules
- `src/main/java/com/bankofz/inqcust/api/repository`
	- Data access abstractions and implementations
- `src/main/java/com/bankofz/inqcust/api/domain`
	- API/domain records and enums
- `src/main/java/com/bankofz/inqcust/api/mapper`
	- Legacy-to-API mapping logic
- `src/main/java/com/bankofz/inqcust/api/support`
	- Supporting Spring configuration
- `src/main/resources`
	- Runtime properties and OpenAPI used by this service
- `src/test/java/com/bankofz/inqcust/api`
	- Unit and MVC tests

## Generated Artifacts

- `target/` is build output and should not be edited.

## INQACCCU Sample Inputs From Mock Data

Values in `mock-data/account-relationship-records.json`:

- `0000000001` (customer `John Smith`, accounts `1000000001`, `1000000002`)
- `0000000002` (customer `Priya Patel`, account `2000000001`)

Valid business not-found sample used in tests:

- `0000000999`

## Verified INQACCCU Test Evidence

Implemented backend test files:

- `src/test/java/com/bankofz/inqcust/api/inqacccu/contract/InqacccuOpenApiConformanceTest.java`
- `src/test/java/com/bankofz/inqcust/api/inqacccu/controller/AccountRelationshipControllerTest.java`
- `src/test/java/com/bankofz/inqcust/api/inqacccu/repository/JsonAccountRelationshipRepositoryTest.java`
- `src/test/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipMapperTest.java`
- `src/test/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipServiceTest.java`

Executed verification commands:

- `mvn -q -DskipTests compile`
- `mvn -q -Dtest="InqacccuOpenApiConformanceTest,AccountRelationshipControllerTest,JsonAccountRelationshipRepositoryTest,AccountRelationshipServiceTest,AccountRelationshipMapperTest" test`
- `mvn -q test`

