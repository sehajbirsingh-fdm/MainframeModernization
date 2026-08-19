# Mainframe Modernization API

Spring Boot 3 (Java 21) backend implementing:

- INQCUST customer inquiry
- INQACC account inquiry
- INQACCCU customer-account relationship inquiry
- CRECUST customer create

## Runtime Endpoints

- `GET /api/v1/customers/{sortCode}/{customerNumber}`
- `GET /v1/accounts/{sortcode}/{accountNumber}`
- `GET /api/v1/customers/{customerNumber}/accounts`
- `GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period}`
- `POST /v1/customers`

## Security

- INQACC and CRECUST endpoints require bearer auth with role `ACCOUNT_INQUIRER`.
- Local deterministic test tokens:
  - `Bearer valid-inqacc-inquirer-token` (authorized)
  - `Bearer valid-inqacc-limited-token` (authenticated but forbidden)

## Data Access Model

The backend is DB-first and uses JDBC repositories only.

- `CustomerRepository` -> `JdbcCustomerRepository`
- `CustomerCreateRepository` -> `JdbcCustomerCreateRepository`
- `AccountRepository` -> `JdbcAccountRepository`
- `AccountRelationshipRepository` -> `JdbcAccountRelationshipRepository`

No JSON/mock-data repository implementation is used at runtime.

## H2 Default Configuration

By default, the app runs with file-backed H2 and initializes schema/data from:

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`

Default datasource:

- `spring.datasource.url=jdbc:h2:file:./data/mainframe-modernization;MODE=DB2;AUTO_SERVER=TRUE`
- `spring.datasource.username=sa`
- `spring.datasource.password=`

H2 console:

- `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/mainframe-modernization`

## Package Structure

Base package:

- `com.bankofz.mainframemodernization`

Feature packages:

- `inqcust`
- `inqacc`
- `inqacccu`
- `crecust`
- `security`

## Build And Run

From `backend/api`:

- Run tests: `mvn test`
- Run app: `mvn spring-boot:run`

## Notes

- `target/` is generated build output.
- API contracts for features are maintained under `specs/`.
