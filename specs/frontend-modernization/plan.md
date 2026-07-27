# Implementation Plan: INQCUST Frontend React Integration

Branch: frontend-modernization  
Date: 2026-07-13  
Spec: specs/frontend-modernization/spec.md  
Backend Source Of Truth: specs/001-inqcust-customer-inquiry-modernization/spec.md

## Summary

Build a simple React + TypeScript frontend that consumes the existing backend customer inquiry endpoint and renders UI states based on API responses. Keep backend unchanged. Keep mode switching configuration-only (mock/live). Use text inputs only for sort code and customer number. Do not introduce lookup dropdown controls.

## Scope

In scope:
- One inquiry page with two textboxes and submit action.
- Client-side format validation for sort code and customer number.
- API call to GET /api/v1/customers/{sortCode}/{customerNumber}.
- State rendering for loading, success, not found, validation error, system error, and timeout.
- Display legacyStatus and riskAssessment exactly as returned.
- Mock mode and live API mode switching via config.
- Frontend test coverage (unit, component, integration, e2e).

Out of scope:
- Backend business rule changes.
- New backend fields.
- Authentication/authorization implementation.
- Cross-feature navigation redesign.

## Technical Context

Language: TypeScript  
Framework: React  
Build Tool: Vite  
Routing: React Router  
Server State: React Query  
Validation: Zod and native input constraints  
Mocking: MSW  
Testing: Vitest, React Testing Library, Playwright

## UX Constraint

Mandatory control design:
- sortCode: textbox input only
- customerNumber: textbox input only
- No dropdown for lookup mode

Lookup mode is inferred by entered customer number:
- 0000000000 random
- 9999999999 latest
- all other valid values specific

## Architecture Approach

1. Create a frontend app module under frontend/app.
2. Add a small API client layer that builds endpoint path from textbox values.
3. Keep a single Inquiry page and render state-driven sections.
4. Add config-based mode switch:
   - mock mode: use MSW handlers
   - live mode: use backend base URL
5. Keep domain types aligned with backend response contract.

## QA-First Rules

- No implicit behavior not represented by API response.
- Validate before submit and validate response rendering paths.
- Every acceptance scenario has at least one automated test.
- Failure states are tested as first-class scenarios.
- Keyboard and screen-reader behavior validated in CI.

## Milestones

M1: Frontend scaffold and config mode switch  
M2: Inquiry form with textbox validation and submit  
M3: Result rendering for success/not found/risk status  
M4: Error and timeout states with retry  
M5: Test completion and documentation

## Implementation Slices

Slice 1: Setup and skeleton
- Create React TS app and folder conventions.
- Add route and empty Inquiry page.
- Add environment config for mock/live.

Slice 2: Inquiry form and client validation
- Add sortCode textbox and customerNumber textbox.
- Enforce regex checks: 6 digits and 10 digits.
- Block submit with inline validation messages.

Slice 3: API integration and loading/success states
- Call backend endpoint with text values.
- Render loading and success states.
- Render legacyStatus and riskAssessment exactly.

Slice 4: Error states and retry
- Handle 404, 400, 500, and timeout/offline.
- Add retry action and preserve user inputs.

Slice 5: Test hardening and accessibility
- Add unit/component/integration/e2e tests.
- Validate keyboard navigation and live region announcements.

## Risks And Mitigations

Risk: Frontend drifts from backend contract.  
Mitigation: Contract fixtures and traceability checks.

Risk: Dropdown introduced later by UI changes.  
Mitigation: Explicit no-dropdown requirement in tests and review checklist.

Risk: Mock/live behavior mismatch.  
Mitigation: Shared response fixtures used by MSW and integration tests.

Risk: Timeout handling inconsistent across browsers.  
Mitigation: Standardized timeout wrapper in API client and e2e coverage.

## Exit Criteria

- All FE acceptance scenarios mapped and tested.
- Textbox-only inquiry form implemented and verified.
- Mock mode and live mode both function by config switch only.
- CI test suite passes for unit/component/integration/e2e.
- Frontend docs updated for setup and troubleshooting.

## Open Questions

1. Where should frontend module live: frontend/app or evolve existing frontend/legacy-static?
2. Should backend base URL default to localhost:8080 in live mode?
3. What timeout threshold should be standard for retry UX?
