# quickstart.md

## Startup

Backend:

- Directory: `src/api`
- Command: `mvn spring-boot:run`
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
- Frontend dev proxy routes `/api/*` and `/v1/*` to `http://localhost:8080`.

Optional configuration:

- Backend CORS override: `APP_CORS_ALLOWED_ORIGINS`.
- Frontend optional vars:
	- `VITE_API_BASE_URL`
	- `VITE_API_TIMEOUT_MS`

## INQACCCU API

- Endpoint: `GET /api/v1/customers/{customerNumber}/accounts`
- Input validation: `customerNumber` must match `^[0-9]{10}$`.

Reserved-value behavior:

- `0000000000` and `9999999999` are validly formatted and continue through business flow.
- They produce customer-not-found business outcomes in HTTP 200, not HTTP 400.

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

- `0000000001` -> customer `John Smith`, account numbers `1000000001`, `1000000002`
- `0000000002` -> customer `Priya Patel`, account number `2000000001`

## Verified Commands and Evidence

Backend verification commands executed:

- `mvn -q -DskipTests compile`
- `mvn -q -Dtest="InqacccuOpenApiConformanceTest,AccountRelationshipControllerTest,JsonAccountRelationshipRepositoryTest,AccountRelationshipServiceTest,AccountRelationshipMapperTest" test`
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

- Optional manual browser smoke run while backend and frontend servers are running.
