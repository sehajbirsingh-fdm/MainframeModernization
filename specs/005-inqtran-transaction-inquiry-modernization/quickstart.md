# Quickstart

These commands follow the repository discovery report and must be re-verified in the actual checkout after implementation.

Feature: INQTRAN - Transaction Inquiry
Feature ID: 005-inqtran-transaction-list-inquiry-modernization

## Backend
```powershell
cd backend/api
mvn test
mvn spring-boot:run
```

## Frontend
```powershell
cd frontend/app
npm install
npm run test
npm run dev
```

## End-to-end
Run the repository's Playwright command from `frontend/app` after confirming the configured script:
```powershell
npm run test:e2e
```

## Manual verification
1. Open the existing frontend application.
2. Navigate to the transaction-list route.
3. Enter an approved H2 test account.
4. Verify default limit 50 and zero offset.
5. Apply inclusive dates.
6. Verify a later page by offset.
7. Verify a no-match query returns an empty success state.
8. Verify no transaction-detail navigation is present.

## Contract and quality gates
- Backend unit/integration/controller tests pass.
- Frontend unit and E2E tests pass.
- Runtime OpenAPI matches `contracts/openapi.yaml`.
- Existing feature regression tests pass.
- QA and code review checklists are complete.
- No live DB2/CICS integration, second project, or mock JSON was added.
