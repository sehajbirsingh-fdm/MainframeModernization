# Quickstart

Feature: INQTRAN - Transaction Inquiry  
Feature ID: 005-inqtran-transaction-list-inquiry-modernization

## 1. Purpose

This Quickstart provides the minimum steps to execute and verify Feature 005 using the existing repository applications.

Repository structure and established conventions take precedence over illustrative examples in this guide.

## 2. Prerequisites

- A clean Git checkout of this repository.
- Java 21 available in your environment.
- Maven available in your environment.
- Node.js and npm available in your environment.

## 3. Repository and Startup Notes

- Reuse the existing backend application in `backend/api`.
- Reuse the existing frontend application in `frontend/app`.
- Do not create a second backend or frontend project for Feature 005.
- Follow current repository conventions for build, run, and verification workflows.
- H2 is the approved proof-of-concept persistence for this feature.

## 4. Backend Startup

Run from repository root:

```powershell
cd backend/api
mvn test
mvn spring-boot:run
```

Expected outcomes:

- The backend application starts successfully.
- H2 initialization completes according to existing backend startup behavior.
- Runtime OpenAPI is available from the running backend.
- Startup completes with no blocking startup errors.

## 5. Frontend Startup

Run from repository root:

```powershell
cd frontend/app
npm install
npm run test
npm run dev
```

Expected outcomes:

- The frontend application loads successfully.
- Existing application navigation remains available.
- The Feature 005 transaction inquiry route is accessible.

## 6. Automated Verification

Use the existing repository commands:

- Backend tests are run during backend startup preparation:

```powershell
cd backend/api
mvn test
```

- Frontend tests are run during frontend startup preparation:

```powershell
cd frontend/app
npm run test
```

- End-to-end verification from `frontend/app`:

```powershell
npm run test:e2e
```

- Run existing repository regression suites using the project's established workflows before completion.

## 7. Manual Verification

Use an approved H2 test account and run these checks in order.

1. Action: Open the existing frontend application and navigate to the transaction-list route.  
	Expected result: The inquiry UI is reachable through the existing application shell and route structure.
2. Action: Run a populated inquiry with valid account inputs and default paging controls.  
	Expected result: A successful response is rendered with transaction rows and coherent metadata (`totalCount` and `returnedCount`).
3. Action: Run a query that has no matching transactions.  
	Expected result: A successful empty state is shown (not a technical error state), with zero returned rows.
4. Action: Submit a valid inquiry with omitted date boundaries.  
	Expected result: The request is accepted and processed according to the approved modernization omitted-date contract behavior.
5. Action: Submit a valid inquiry with supplied inclusive date boundaries.  
	Expected result: Rows on both supplied boundary dates are included.
6. Action: Submit paging values to move to a later page (offset beyond the first page).  
	Expected result: Results reflect ordered, paged behavior and metadata remains coherent for the selected page.
7. Action: Trigger validation failures (for example malformed identity or pagination inputs).  
	Expected result: Validation failure behavior is returned and rendered according to approved API/UI handling.
8. Action: Execute a second successful inquiry after one successful result is already displayed.  
	Expected result: The new completed inquiry result replaces the previous completed result state.
9. Action: Inspect available UI navigation/actions from inquiry results.  
	Expected result: No transaction-detail (INQTRAND) navigation is present in Feature 005 flows.

## 8. API Contract Verification

- Verify runtime OpenAPI behavior and schema align with `contracts/openapi.yaml`.
- Verify approved HTTP status behavior for 200, 400, and 500 responses.
- Verify response payloads do not include undocumented fields.
- Confirm populated and empty success responses remain contract-aligned.

## 9. Quality Gates

Complete gates in this execution order:

1. Backend starts successfully.
2. Frontend starts successfully.
3. Manual verification succeeds.
4. Automated tests succeed.
5. Runtime OpenAPI matches the feature contract.
6. Regression passes.
7. Code review checklist is complete.
8. QA review checklist is complete.

## 10. Known Proof-of-Concept Limitations

- H2 is used in place of DB2 for this proof of concept.
- No live DB2, CICS, or mainframe dependency is required.
- Omitted-date behavior is the approved modernization contract for Feature 005 and is not proof of deployed legacy SQL runtime behavior.
- INQTRAND transaction-detail inquiry is out of scope.

## 11. Basic Troubleshooting

- Backend startup failure: verify you are running from `backend/api`, confirm Java 21 and Maven availability, then rerun `mvn test` and `mvn spring-boot:run`.
- Frontend connection issues: verify you are running from `frontend/app`, confirm dependencies were installed with `npm install`, then rerun `npm run dev`.
- H2 initialization problems: check backend startup logs for schema/data initialization errors and confirm startup completed before frontend verification.
- OpenAPI mismatch: compare runtime OpenAPI from the running backend with `contracts/openapi.yaml` and resolve drift before sign-off.
