# MainframeModernization Project Onboarding Guide

## 1. What This Project Is

This repository demonstrates modernization of IBM Z banking workloads, with a focused feature implementation for **INQCUST customer inquiry**.

At a high level, the project contains:
- Legacy-oriented artifacts and documentation for Bank of Z.
- A Spring Boot API module that exposes a modern REST endpoint for customer inquiry.
- Mock-first behavior for local development, with a DB-ready code path that can be enabled by configuration.

If you are new, start with this file, then read:
- `README.md`
- `specs/001-inqcust-customer-inquiry-modernization/spec.md`
- `src/api/README.md`

What is actively implemented right now:
- Backend API in `src/api` (Spring Boot, Java 21).
- New customer inquiry frontend in `src/frontend-react` (React + TypeScript + Vite).
- Legacy static frontend in `src/frontend` (vanilla HTML/JS), kept as a separate older implementation.

## 2. Feature Scope (INQCUST)

The modernized API feature is defined in:
- `specs/001-inqcust-customer-inquiry-modernization/spec.md`

Primary endpoint:
- `GET /api/v1/customers/{sortCode}/{customerNumber}`

Behavior summary:
- Specific lookup: normal 10-digit customer number.
- Random lookup command: `0000000000`.
- Latest lookup command: `9999999999`.

Important acceptance rules implemented in code:
- Path validation (`sortCode` 6 digits, `customerNumber` 10 digits).
- Legacy status mapping (`Y/N` success and fail codes).
- Random and latest lookup modes.
- Risk assessment enrichment (`LOW/MEDIUM/HIGH`) from status/score/review date.

## 3. Architecture Summary

### Runtime module
- Spring Boot service in `src/api`.

### Core layers
- Controller layer:
  - `src/api/src/main/java/com/bankofz/inqcust/api/controller`
- Service/business logic:
  - `src/api/src/main/java/com/bankofz/inqcust/api/service`
- Repository/data access abstraction:
  - `src/api/src/main/java/com/bankofz/inqcust/api/repository`
- Mapping/domain model:
  - `src/api/src/main/java/com/bankofz/inqcust/api/mapper`
  - `src/api/src/main/java/com/bankofz/inqcust/api/domain`
- Support config:
  - `src/api/src/main/java/com/bankofz/inqcust/api/support`

### Data source strategy
The API supports two runtime modes (same functionality, different source):
- `mock` mode (default): reads JSON mock records.
- `db` mode: uses DB-backed queries via managed DataSource.

Mode switch is config-only via `app.data.mode` in:
- `src/api/src/main/resources/application.properties`

## 4. Folder Structure (What Matters Most)

Top-level folders:
- `docs/`: user-facing project documentation site.
- `specs/`: feature specs, traceability, supporting design documents.
- `src/`: implementation assets.
- `mock-data/`: shared sample data.
- `prompts/`: guided AI implementation prompts.
- `scripts/`: utility scripts.

Within `src/`:
- `src/api`: active Spring Boot API module.
- `src/frontend-react`: active INQCUST customer inquiry frontend (current implementation).
- `src/frontend`: legacy/static frontend demo pages/assets (older stack, separate from React app).
- `src/base`: legacy-oriented source assets (reference/training/integration context).

Within `src/api/src/`:
- `main/java`: production Java source.
- `main/resources`: runtime properties and active OpenAPI YAML.
- `test/java`: test source.

Generated artifacts:
- `src/api/target`: build output only. Never edit manually.

## 5. API Module Technical Notes

Primary app config:
- `src/api/src/main/resources/application.properties`

Key properties:
- `app.data.mode=mock|db`
- `app.mock-data.path=...`
- `app.db.url`, `app.db.username`, `app.db.password`
- `app.db.table-name`

Production-oriented DB path details:
- Managed pooled DataSource (Hikari) configured in support layer.
- Repository validates configurable table identifier format for SQL safety.
- DB mode fails fast when required DB configuration is missing.

## 6. Spec-to-Code Traceability (Quick Map)

Spec artifact:
- `specs/001-inqcust-customer-inquiry-modernization/spec.md`

Supporting traceability docs:
- `specs/001-inqcust-customer-inquiry-modernization/supporting/traceability-matrix.md`
- `specs/001-inqcust-customer-inquiry-modernization/supporting/test-spec.md`
- `specs/001-inqcust-customer-inquiry-modernization/supporting/mapping-matrix.md`

Main implementation touchpoints:
- Controller endpoint and validation:
  - `src/api/src/main/java/com/bankofz/inqcust/api/controller/CustomerInquiryController.java`
- Lookup mode and orchestration:
  - `src/api/src/main/java/com/bankofz/inqcust/api/service/CustomerInquiryService.java`
- Risk logic:
  - `src/api/src/main/java/com/bankofz/inqcust/api/service/RiskAssessmentService.java`
- Data access abstraction:
  - `src/api/src/main/java/com/bankofz/inqcust/api/repository/CustomerRepository.java`

## 7. How To Run (New Developer)

### Prerequisites
- Java 21
- Maven

### Build and test
From `src/api`:

```bash
mvn test
```

### Run in mock mode (default)
From `src/api`:

```bash
mvn spring-boot:run
```

Important:
- Run backend commands from `src/api` only.
- Running `mvn spring-boot:run` from repository root will fail because the root is not the Spring Boot module.

Alternative from any folder:

```bash
mvn -f src/api/pom.xml spring-boot:run
```

### Run frontend (React)
From `src/frontend-react`:

```bash
npm install
npm run dev
```

Local dev URL:
- `http://localhost:5173`

Backend connectivity:
- Vite proxy forwards `/api/*` to `http://localhost:8080` by default.
- This avoids CORS issues in normal local development.

### Run in db mode
Set environment variables and mode before startup:

```bash
export APP_DB_URL='jdbc:db2://host:port/database'
export APP_DB_USERNAME='your_user'
export APP_DB_PASSWORD='your_password'
```

Then set `app.data.mode=db` and run:

```bash
mvn spring-boot:run
```

## 8. Common Pitfalls

- Typo in Maven command:
  - Correct: `mvn spring-boot:run`
  - Incorrect: `mvm spring-boot:run`
- Confusing source vs generated files:
  - Source is under `src/main/*` and `src/test/*`.
  - `target/` is generated.
- DB mode startup failures:
  - Usually missing/invalid DB properties or DB driver/environment mismatch.

## 9. Testing Strategy

Current tests cover:
- Controller behavior and HTTP status mapping.
- Service lookup modes and business rules.
- Risk assessment rules.
- Date conversion and mapping behavior.
- Repository behavior in mock mode and DB-mode simulation using in-memory DB for integration confidence.

Primary test tree:
- `src/api/src/test/java/com/bankofz/inqcust/api`

## 10. What To Read Next (Recommended Order)

1. `PROJECT_ONBOARDING_GUIDE.md` (this file)
2. `README.md`
3. `specs/001-inqcust-customer-inquiry-modernization/spec.md`
4. `src/api/README.md`
5. `src/api/src/main/java/com/bankofz/inqcust/api/controller/CustomerInquiryController.java`
6. `src/api/src/main/java/com/bankofz/inqcust/api/service/CustomerInquiryService.java`
7. `src/api/src/main/java/com/bankofz/inqcust/api/repository/MockCustomerRepository.java`
8. `src/api/src/test/java/com/bankofz/inqcust/api`

## 11. New Contributor Checklist

- Run tests successfully in `src/api`.
- Verify endpoint behavior in mock mode.
- Understand lookup command values (`0000000000`, `9999999999`).
- Understand spec acceptance scenarios and fail codes.
- Avoid editing generated `target/` artifacts.
- Keep data access changes behind `CustomerRepository` to preserve mode-switch behavior.

## 12. Structure Review: What Is Unnecessary vs Intentional

This section helps avoid deleting files that look redundant but are intentional.

Likely unnecessary (safe cleanup candidates):
- `src/frontend-react/src/assets/hero.png` (not referenced)
- `src/frontend-react/src/assets/react.svg` (not referenced)
- `src/frontend-react/src/assets/vite.svg` (not referenced)
- `src/frontend-react/public/icons.svg` (not referenced)

Generated/local-only (should not be committed):
- `src/frontend-react/node_modules/`
- `src/frontend-react/dist/`

Intentional but easy to confuse as duplicate:
- `src/frontend/` and `src/frontend-react/` both exist by design right now.
- If the team decides to standardize on React only, archive or remove `src/frontend/` in a separate cleanup PR after team approval.
