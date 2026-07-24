# Intended System Blueprint - CRECUST

## 1. Target Objective
Modernize CRECUST customer creation into a Spring Boot service that preserves business semantics and legacy status observability while using mock persistence for POC.

## 2. Target Architecture
- Controller: one create endpoint (`POST /v1/customers`).
- Service: ordered business orchestration matching COBOL flow.
- Repository:
  - `CustomerRepository` for customer persistence.
  - `CustomerControlRepository` for number generation state.
- Adapter:
  - `CreditCheckGateway` for credit score orchestration simulation.
- Error layer:
  - centralized exception-to-envelope mapping.

## 3. Data Boundary
- Input payload contains copybook-backed fields only.
- Sortcode is system-owned.
- Output includes modern fields + legacy status metadata.

## 4. Runtime Modes
- Default mode: mock repository and mock control-state.
- Future mode: DB2/CICS adapters behind interfaces only.

## 5. Observability Baseline
- Correlation ID propagated in headers and payloads.
- Structured logging with fail-code visibility.
- No sensitive payload dumps in production logging mode.

## 6. SDD Governance
- No implementation starts before all SDD artifacts pass gate checks.
- All implementation decisions must trace back to `spec.md` and supporting matrices.
