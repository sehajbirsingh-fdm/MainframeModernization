# quickstart.md

## Startup

Backend:

- Directory: `src/api`
- Command: `mvn spring-boot:run`
- Required Maven step: `mvn -q -DskipTests compile`
- Port: `8080`

Frontend:

- Directory: `src/frontend-react`
- Commands:
	- `npm install`
	- `npm run dev`
- Port: `5173`
- INQACCCU route: `http://localhost:5173/customer-accounts`

## Default Runtime Behavior

- Default mode is mock (`app.data.mode=mock`).
- INQACCCU mock data path: `mock-data/account-relationship-records.json`.
- No database is required in default mock mode.
- No environment variables are required for default local startup.
- Frontend dev proxy routes `/api/*` and `/v1/*` to `http://localhost:8080`.

Optional configuration:

- Backend CORS override: `APP_CORS_ALLOWED_ORIGINS`.
- Frontend optional vars:
	- `VITE_API_BASE_URL`
	- `VITE_API_TIMEOUT_MS`

## INQACCCU API

- Endpoint: `GET /api/v1/customers/{customerNumber}/accounts`
- Input validation: `customerNumber` must match `^[0-9]{10}$`.
- Success response shape: `legacyStatus`, `customerNumber`, `numberOfAccounts`, `accounts[]`.
- Account fields returned: `eyecatcher`, `customerNumber`, `sortCode`, `accountNumber`, `accountType`, `interestRate`, `openedDate`, `overdraftLimit`, `lastStatementDate`, `nextStatementDate`, `availableBalance`, `actualBalance`.
- Validation errors (HTTP 400): `error.type=VALIDATION_ERROR`, `error.message=Validation failed`, `error.details[].field/reason`.
- Infrastructure errors (HTTP 500): `error.type=INFRASTRUCTURE_ERROR`, `error.message=Service unavailable due to infrastructure failure`.

Reserved-value behavior:

- `0000000000` and `9999999999` are validly formatted and continue through business flow.
- They produce customer-not-found business outcomes in HTTP 200, not HTTP 400.

## Demo Workflow

1. Open `http://localhost:5173/customer-accounts`.
2. Enter a 10-digit customer number and click `Inquire`.
3. Success example (`0000000001`):
	- Status message `Inquiry successful`
	- Customer summary shows `0000000001`
	- Accounts heading shows `Accounts (2)`
	- Account rows show fields including `ACCT`, account numbers, sort code, and opened date.
4. Success example (`0000000002`):
	- Status message `Inquiry successful`
	- Customer summary shows `0000000002`
	- Accounts heading shows `Accounts (1)`.
5. Customer-not-found example (`0000000999`):
	- Status message `Customer not found`
	- Legacy fail code `1`
	- Customer summary preserves input customer number.
6. Validation error example (`ABC`):
	- Inline validation error `Customer number must be exactly 10 digits.`
7. Infrastructure error behavior:
	- If backend returns HTTP 500, UI shows `Backend Error` with `INFRASTRUCTURE_ERROR` and `Service unavailable due to infrastructure failure`.

## Navigation

- Main navigation links are available for `INQCUST` (`/customers`), `INQACC` (`/accounts`), and `INQACCCU` (`/customer-accounts`).

## Sample Calls

From repository root:

```bash
curl http://localhost:8080/api/v1/customers/0000000001/accounts
curl http://localhost:8080/api/v1/customers/0000000002/accounts
curl http://localhost:8080/api/v1/customers/0000000999/accounts
curl http://localhost:8080/api/v1/customers/0000000000/accounts
curl http://localhost:8080/api/v1/customers/9999999999/accounts
```

## Valid Sample Inputs From Mock Data

- `0000000001` -> account numbers `1000000001`, `1000000002`
- `0000000002` -> account number `2000000001`

## Verified Commands and Evidence

Backend verification commands executed:

- `mvn -q -DskipTests compile`
- `mvn -q -Dtest="JsonAccountRelationshipRepositoryTest,AccountRelationshipMapperTest,AccountRelationshipServiceTest" test`
- `mvn -q -Dtest="AccountRelationshipControllerTest,InqacccuOpenApiConformanceTest" test`
- `mvn -q test`

Frontend verification commands executed:

- `npm test`
- `npm run build`
- `npm run test:e2e`

Implemented INQACCCU test evidence:

- Backend contract: `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/contract/InqacccuOpenApiConformanceTest.java`
- Backend controller: `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/controller/AccountRelationshipControllerTest.java`
- Backend repository: `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/repository/JsonAccountRelationshipRepositoryTest.java`
- Backend service/mapper:
	- `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipServiceTest.java`
	- `src/api/src/test/java/com/bankofz/inqcust/api/inqacccu/service/AccountRelationshipMapperTest.java`
- Frontend API client: `src/frontend-react/src/api/customerAccountInquiryClient.test.ts`
- Frontend page/validation:
	- `src/frontend-react/src/features/customerAccountInquiry/CustomerAccountInquiryPage.test.tsx`
	- `src/frontend-react/src/features/customerAccountInquiry/validation.test.ts`
- Browser-level E2E: `src/frontend-react/e2e/inqacccu.e2e.spec.ts`

## Manual Verification Remaining

- None specific to INQACCCU after automated backend, frontend unit, frontend E2E, and build verification.
