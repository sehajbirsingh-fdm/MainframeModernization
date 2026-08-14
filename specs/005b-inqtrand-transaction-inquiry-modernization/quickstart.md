# Quickstart — 00B INQTRAND Transaction Detail Inquiry

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

This path is discovery evidence from 2026-08-14 and may differ on another workstation.

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
Use an incorrect-width key.

Expected: 400 / ERR-001.

### Security
Exercise unauthenticated, unauthorized and authorized paths according to repository-approved dev authentication setup.

## Frontend Smoke Tests
1. Open the existing transaction feature.
2. Navigate/request a known detail.
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

- **Upstream Inputs:** `supporting/architecture.md`, `plan.md`, `tasks.md`, `contracts/openapi.yaml`, `supporting/test-spec.md`, repository discovery.
- **Downstream Consumers:** Developers, reviewers, QA, final implementation report.
- **Authority Boundary:** Authoritative only for verified setup/run/verification instructions; currently unresolved commands are explicitly marked.
- **Conflict Handling:** Implementation/runtime evidence wins over stale Quickstart text; update this file after execution instead of guessing.
