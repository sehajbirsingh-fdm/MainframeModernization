# Quickstart — 005B INQTRAND Transaction Detail Inquiry

## Purpose
Provide a safe run/verification guide without inventing commands, ports, credentials, or runtime behavior that the supplied repository discovery did not verify.

## Prerequisites
Repository evidence establishes the project uses:
- Java 21;
- Maven for the Spring Boot backend;
- Node-based React/TypeScript/Vite frontend;
- H2 POC persistence.

Exact supported local tool versions beyond Java 21 must be confirmed from current repository files during T001.

## Repository Location
Repository discovery reported the repository root as:
`C:/Users/Alex/data/personal_stuff/jobs/fdm/spec_driven_development_pod/MainframeModernization`

Begin from the root of your checked-out `MainframeModernization` repository.
This absolute path is discovery evidence from 2026-08-14 only; it is not a required runtime location and may differ by workstation.

## Configuration
Before running:
1. inspect `backend/api/src/main/resources/application.properties`;
2. inspect current security/dev-token configuration;
3. inspect frontend environment configuration under `frontend/app/src/config/env.ts`;
4. confirm no hard-coded feature credential is required.

## Database Setup
Repository discovery confirms H2 initialization through:
- `backend/api/src/main/resources/schema.sql`
- `backend/api/src/main/resources/data.sql`

It also reports an H2 file URL in DB2 mode. Do not change schema/data solely from this Quickstart; implementation T004 owns verification.

## Backend Startup
The discovery report confirms Maven and a Spring Boot Maven plugin, and that the backend README contains Maven commands. **It does not record the exact startup command.**

Before implementation completion, T001/T020 must copy the exact current supported backend command from `backend/api/README.md` or the build configuration into this section and verify it by execution.

**Status now:** unresolved by supplied evidence; no command invented.

## Frontend Startup
The discovery report confirms React/Vite and `frontend/app/package.json`, but does not record an exact startup command or port.

T001/T020 must inspect current `package.json` scripts, record the supported command, and verify it.

**Status now:** unresolved by supplied evidence; no command invented.

## Startup Order
Expected architecture is database/bootstrap within backend, backend API, then frontend. Exact process behavior must be verified during implementation.

## Expected Runtime Ports
Not established by the supplied discovery report. Do not assume 8080/5173. Populate after T001/T020 verification.

## API Smoke Tests
After implementation and runtime URL verification, exercise:

### Found
`GET /api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}`

Expected: 200, `found=true`, transaction detail.

### Not found
Use a complete key known not to exist.

Expected: 200, `found=false`, `transaction=null`.

### Validation
Use malformed structural input against the exact string constraints:
- `sortCode` 6 digits;
- `accountNumber` 8 digits;
- `date` 8 digits;
- `time` 6 digits;
- `reference` 12 digits.

Malformed structural input can include incorrect width and/or non-digit content.

Expected: 400 / ERR-001.

Structurally valid but semantically unusual date/time values are not rejected by newly invented Gregorian/calendar or HHMMSS semantic validation alone and proceed through normal lookup behavior. Do not add account-existence or transaction-reference business validation at this boundary.

### Technical failure
Exercise this only where a technical/persistence failure is safely reproducible in the implementation environment.

Expected: 500 / ERR-500 with correlationId.

Normal transaction absence remains a successful 200 (`found=false`, `transaction=null`) and is not a technical failure.

### Security
Exercise unauthenticated, unauthorized and authorized paths according to repository-approved dev authentication setup.

Expected outcomes:
- unauthenticated request -> 401;
- authenticated caller without `ACCOUNT_INQUIRER` -> 403;
- authenticated caller with `ACCOUNT_INQUIRER` -> allowed to reach normal feature behavior.

Do not invent usernames, passwords, bearer tokens, or response-body details not established by repository/runtime evidence.

## Frontend Smoke Tests
1. Open the existing transaction feature.
2. Verify required list-to-detail integration end-to-end: existing transaction list row -> five decomposed identity components (`sortCode`, `accountNumber`, `date`, `time`, `reference`) -> detail navigation -> existing transaction API client -> approved INQTRAND detail endpoint -> found-detail or successful-absence presentation.
3. Confirm detail fields and composite ID.
4. Request a missing detail and confirm normal not-found state.
5. Confirm list inquiry still works unchanged.

## Approved Manual Scenarios
- found row;
- successful absence;
- leading-zero identity;
- validation error;
- authorized/unauthorized behavior;
- technical error only where safely reproducible.

## Automated Test Commands
Exact Maven/npm/Playwright commands are not quoted because discovery did not record them. T020 must populate them from current `pom.xml`, `package.json`, README and test configuration after successful execution.

## Expected Results
All approved tests pass, or a scenario is explicitly FAIL/BLOCKED with evidence. Planned tests are never represented as executed passes.

## Troubleshooting
- If detail returns 404 for absence, behavior contradicts FR-004.
- If amount becomes zero only because DB value is null, inspect detail mapper/repository against BR-012.
- If frontend receives 401 while backend security is active, inspect the known frontend auth wiring gap; do not hard-code a token.
- If runtime OpenAPI lacks detail, reconcile T012/T013.
- If list behavior changes, treat as regression.

## Proof-of-Concept Limitations
- H2 is the approved POC store, not production DB2.
- Production CICS/DB2 resource definitions were not supplied.
- Runtime commands/ports/auth must be verified in the actual implementation environment.
- No mainframe adapter is implemented by this starter kit.

## Shutdown and Cleanup
Use the repository's verified process/container commands once identified. No shutdown command is invented here.


## Artifact Relationships

- **Upstream Inputs:** `supporting/requirements.md`, `spec.md`, `supporting/architecture.md`, `plan.md`, `tasks.md`, `contracts/openapi.yaml`, `supporting/test-spec.md`, `supporting/traceability-matrix.md`, repository/runtime evidence.
- **Downstream Consumers:** Developers, reviewers, QA, final implementation report.
- **Authority Boundary:** Supporting Requirements + Specification remain authoritative for approved feature behavior; OpenAPI remains authoritative for the approved external REST contract; Architecture/Plan/Tasks define implementation direction/work; repository/runtime evidence determines actual commands, ports, environment configuration, and executable setup facts; Quickstart records verified operational instructions.
- **Conflict Handling:** Current verified runtime evidence wins over stale Quickstart text for commands, ports, environment/configuration, and execution facts. Implementation/runtime behavior does not silently override conflicting frozen Requirements/Specification/OpenAPI behavior; behavioral conflicts must be reconciled rather than documented as new truth.
