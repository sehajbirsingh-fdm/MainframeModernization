# Quickstart: Validate CRECUST Customer Create Spec Package

This guide validates the SDD artifacts for `004-crecust-customer-create-modernization` before implementation.

## Prerequisites
- Review complete for `spec.md`, `plan.md`, and `contracts/openapi.yaml`.
- Access to COBOL source and copybooks listed in spec authority.
- Mock customer data available in `mock-data/customer-records.json`.

## Step 1: Confirm Artifact Alignment
1. Open `spec.md` and confirm functional requirements FR-001 to FR-010.
2. Open `plan.md` and verify SDD gates align to feature execution.
3. Open `tasks.md` and confirm every requirement has implementation tasks.
4. Open `supporting/traceability-matrix.md` and verify no gaps.

## Step 2: Validate Contract
1. Open `contracts/openapi.yaml`.
2. Confirm endpoint is `POST /v1/customers`.
3. Confirm request schema uses only copybook-backed fields.
4. Confirm success response includes `legacyStatus` object and generated customer number.
5. Confirm error schema includes `legacyFailCode`.

## Step 3: Validate Rule Coverage
1. Verify title whitelist rule exists and maps to fail code `T`.
2. Verify DOB rules map to fail codes `O`, `Z`, `Y`.
3. Verify credit-check fallback and fail-code mappings (`A`-`H`) are documented.
4. Verify control-number generation and fail code `4` are documented.

## Step 4: Run Pre-Implementation Review
1. Use `checklists/requirements.md` to verify requirement completeness.
2. Use `checklists/code-review-checklist.md` to verify architecture constraints.
3. Use `checklists/qa-review-checklist.md` to verify test readiness.
4. Ensure review report in `reviews/` marks no open critical blockers.

## Expected Outcome
- SDD artifacts are complete, internally consistent, and implementation-ready.
- No implementation starts until all gates pass.
