# Tasks: Frontend Website Shell And UI Consistency

Input: specs/frontend-modernization/spec.md, plan.md

Format: [ID] [P?] Description

## Phase 1: Shell Architecture

- [x] T101 Introduce shared layout shell in App routing.
- [x] T102 Add header with product context and top-right user chip.
- [x] T103 Add global navigation for features and informational pages.
- [x] T104 Add global footer.

## Phase 2: Route Expansion

- [x] T105 Add landing page at `/`.
- [x] T106 Add `/about` informational route.
- [x] T107 Add `/license` informational route.
- [x] T108 Keep existing feature routes unchanged.
- [x] T109 Add catch-all redirect to `/`.

## Phase 3: Visual Consistency

- [x] T110 Define centralized CSS theme tokens.
- [x] T111 Apply consistent styles to shared shell and navigation.
- [x] T112 Preserve and align existing feature-page primitives (`page`, `card`, `field`, etc.).
- [x] T113 Add active-link states and keyboard focus states.
- [x] T114 Add responsive behavior for desktop and mobile navigation/layout.

## Phase 4: Validation

- [x] T115 Run frontend test suite for regression validation.
- [x] T116 Confirm all tests pass after UI updates.

## Phase 5: Documentation

- [x] T117 Update spec with new shell and route requirements.
- [x] T118 Update plan with implementation and risk strategy.
- [x] T119 Update UI flow supporting document.
- [x] T120 Update supporting test and traceability docs to include shell requirements.

## Phase 6: UX Follow-Up Adjustments

- [x] T121 Fix sidebar active-state overlap between `/customers` and `/customers/create`.
- [x] T122 Add INQCUST/CRECUST parity checks so feature labels and nav states stay consistent.
- [x] T123 Refactor Create Customer form into compact side-by-side layout where feasible.
- [x] T124 Use Title dropdown in Create Customer with default value `Mr`.
- [x] T125 Update spec/supporting docs for active-state and compact create-form behavior.

## Phase 7: Statement Frontend Coverage

- [x] T126 Add Statement Inquiry route `/statements` and navigation entry.
- [x] T127 Implement statement inquiry form, validation, and API wiring.
- [x] T128 Add statement frontend unit tests (client, validation, page).

## Exit Criteria

- [x] QG-101 Website shell visible on all routes.
- [x] QG-102 Landing/About/License pages implemented.
- [x] QG-103 Existing feature behaviors preserved.
- [x] QG-104 Frontend tests passing.
- [x] QG-105 Supporting docs fully synchronized.
