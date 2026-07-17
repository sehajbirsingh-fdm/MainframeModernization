# Tasks: INQCUST Frontend React Integration

Input: specs/001a-inqcust-frontend/spec.md, plan.md, supporting docs  
Prerequisites: backend API running and existing spec 001 available

Format: [ID] [P?] Description
- P means task can run in parallel.
- QA tasks are mandatory gates.

## Phase 1: Setup

- [ ] T001 Create React TypeScript app module at src/frontend-react.
- [ ] T002 Add dependencies: react-router-dom, @tanstack/react-query, zod, msw.
- [ ] T003 Add test dependencies: vitest, @testing-library/react, @testing-library/user-event, playwright.
- [ ] T004 Create env config for mode switch:
  - VITE_APP_MODE=mock or live
  - VITE_API_BASE_URL for live mode
- [ ] T005 Define project structure for pages, features, api, components, types, tests.

## Phase 2: Domain And API Contract

- [ ] T006 [P] Create frontend types matching backend response envelope.
- [ ] T007 [P] Create API client for GET /api/v1/customers/{sortCode}/{customerNumber}.
- [ ] T008 [P] Create normalized error model for 400, 404, 500, timeout.
- [ ] T009 Add request timeout wrapper and retry action helper.

## Phase 3: UI Form (Textbox Only)

- [ ] T010 Build Inquiry page with two textboxes:
  - sortCode textbox
  - customerNumber textbox
- [ ] T011 Enforce no lookup dropdown control in UI.
- [ ] T012 Add client validation:
  - sortCode regex 6 digits
  - customerNumber regex 10 digits
- [ ] T013 Add submit behavior only when inputs are valid.
- [ ] T014 Preserve entered textbox values after request completion or failure.

## Phase 4: State Rendering From API Response

- [ ] T015 Add loading state while request is in progress.
- [ ] T016 Add success state for 200 with customer details rendering.
- [ ] T017 Add not found state for 404.
- [ ] T018 Add backend validation state for 400.
- [ ] T019 Add system error state for 500.
- [ ] T020 Add timeout/offline state with retry action.
- [ ] T021 Render legacyStatus fields exactly as returned.
- [ ] T022 Render riskAssessment fields exactly as returned.
- [ ] T023 Render lookupMode exactly from response; do not infer mode for output.

## Phase 5: Accessibility

- [ ] T024 Add explicit labels and descriptions for both textboxes.
- [ ] T025 Ensure keyboard-only navigation for form and results.
- [ ] T026 Ensure visible focus styles for interactive elements.
- [ ] T027 Add ARIA live region announcements for validation and system errors.
- [ ] T028 Move focus to first error on failed submit.

## Phase 6: Runtime API Integration

- [ ] T029 Implement runtime API calls using VITE_API_BASE_URL.
- [ ] T030 Ensure runtime has no hardcoded fixture-response path.
- [ ] T031 Verify special command values are passed through exactly:
  - 0000000000 random
  - 9999999999 latest

## Phase 7: QA Validation Tasks

- [ ] T033 Unit tests: validators and mode helpers.
- [ ] T034 Component tests: textbox form behavior and inline validation.
- [ ] T035 Integration tests: 200, 404, 400, 500, timeout state transitions.
- [ ] T036 Integration tests: verify no dropdown exists for lookup mode.
- [ ] T037 Integration tests: verify legacyStatus and riskAssessment exact rendering.
- [ ] T038 E2E happy path: specific customer found.
- [ ] T039 E2E unhappy paths: invalid input, not found, system error, timeout.
- [ ] T040 Accessibility test pass for keyboard and announcement behavior.

## Phase 8: Documentation And Handover

- [ ] T041 Create frontend README with run/test/config steps.
- [ ] T042 Document mock/live mode switching with examples.
- [ ] T043 Link frontend docs from PROJECT_ONBOARDING_GUIDE.md.
- [ ] T044 Update traceability matrix with implemented test IDs.

## QA Exit Gate (Must Pass)

- [ ] QG-001 Textbox-only input model enforced (no dropdown).
- [ ] QG-002 All 6 mapped scenarios pass.
- [ ] QG-003 Runtime API connectivity verified (frontend calls backend for all outcomes).
- [ ] QG-004 Accessibility checks pass for labels, keyboard, focus, and announcements.
- [ ] QG-005 No frontend business-rule drift from backend source of truth.

## Open Questions

1. Should timeout retry be automatic once before manual retry?
2. Should fail code be visible in end-user UI or diagnostics section only?
3. Which CI environment should run Playwright by default?
