# Feature Specification: Frontend Website Shell And UI Consistency

**Feature Branch**: `frontend-modernization`
**Created**: 2026-07-13
**Updated**: 2026-07-30
**Status**: In Progress

## Purpose

Provide a consistent, website-level UI shell for all implemented banking workflows while preserving existing feature behavior and backend contracts.

## Scope

In scope:
- Add website landing page at `/`.
- Add global header with user identity chip on top-right.
- Add primary navigation container (sidebar on desktop, stacked on small screens).
- Add footer with program and license context.
- Add informational routes `/about` and `/license`.
- Keep existing feature routes and form/API behavior unchanged:
  - `/customers` (INQCUST)
  - `/customers/create` (CRECUST)
  - `/accounts` (INQACC)
  - `/customer-accounts` (INQACCCU)
- Apply consistent visual language and responsive behavior across all pages.

Out of scope:
- Backend business rule changes.
- New API fields or contract changes.
- Authentication and authorization redesign.
- New feature workflows beyond current implemented set.

## User Stories

### Story 1: Website landing experience
As a user, I want a proper landing page so I understand what this portal is and where to start.

### Story 2: Easy navigation
As a user, I want consistent navigation from every page so I can switch between workflows quickly.

### Story 3: Program context
As a stakeholder, I want About and License pages so the UI has complete website framing and governance context.

### Story 4: Consistent visual language
As a user, I want all feature screens to look cohesive so the app feels like one product instead of disconnected pages.

## Functional Requirements

- **FE-FR-101**: Frontend SHALL render a landing page at `/` with links into all implemented feature workflows.
- **FE-FR-102**: Frontend SHALL provide a global header with product title and top-right user identity chip.
- **FE-FR-103**: Frontend SHALL provide global navigation to feature routes and informational routes.
- **FE-FR-104**: Frontend SHALL render informational pages at `/about` and `/license`.
- **FE-FR-105**: Frontend SHALL render a global footer on all routes.
- **FE-FR-106**: Existing feature routes SHALL remain unchanged and reachable by direct URL.
- **FE-FR-107**: Existing feature form submissions and API calls SHALL remain functionally unchanged.
- **FE-FR-108**: Unknown routes SHALL redirect to `/`.
- **FE-FR-109**: Global navigation SHALL mark only the exact current route as active (for example, `/customers/create` SHALL not also activate `/customers`).
- **FE-FR-110**: Create Customer page SHALL use a Title dropdown with `Mr` selected by default.

## UX And Theme Requirements

- **FE-UX-101**: Shared layout SHALL include header, nav, content region, and footer.
- **FE-UX-102**: Navigation SHALL visibly indicate the active route.
- **FE-UX-103**: Shared design tokens (color, spacing, typography, radius) SHALL be defined centrally in global CSS.
- **FE-UX-104**: Cards, buttons, forms, status text, and tables SHALL follow consistent styling across all feature pages.
- **FE-UX-105**: Landing page SHALL provide quick-start actions for all implemented features.
- **FE-UX-106**: Create Customer form SHALL use compact layout patterns with side-by-side fields where feasible on desktop while remaining single-column on small screens.

## Accessibility Requirements

- **FE-A11Y-101**: Global navigation SHALL be keyboard accessible.
- **FE-A11Y-102**: Focus indicators SHALL remain visible across nav links, buttons, and inputs.
- **FE-A11Y-103**: Informational and feature pages SHALL preserve semantic heading structure.
- **FE-A11Y-104**: Existing feature live regions for runtime messages SHALL remain intact.

## Non-Functional Requirements

- **FE-NFR-101**: Layout SHALL be responsive for mobile (>=360px) and desktop.
- **FE-NFR-102**: UI shell update SHALL not regress existing frontend tests.
- **FE-NFR-103**: New shell SHALL require no backend API contract changes.

## Route Inventory (Expected)

- `/` -> landing page
- `/customers` -> INQCUST
- `/customers/create` -> CRECUST
- `/accounts` -> INQACC
- `/customer-accounts` -> INQACCCU
- `/about` -> About page
- `/license` -> License page

## Acceptance Scenarios

### FE Scenario A: Landing entry
Given user opens `/`
When page renders
Then user sees website header, navigation, landing quick-start content, and footer.

### FE Scenario B: Feature continuity
Given user navigates to existing feature routes
When interacting with forms and submissions
Then feature behavior and API outcomes match previous implementation.

### FE Scenario C: Informational pages
Given user opens `/about` or `/license`
When page renders
Then user sees consistent shell and route-specific informational content.

### FE Scenario D: Responsive navigation
Given user is on desktop or mobile viewport
When page renders
Then navigation remains usable and readable in each breakpoint layout.

### FE Scenario E: Unknown route handling
Given user opens an unknown URL path
When router resolves route
Then user is redirected to `/`.

### FE Scenario F: Exact active navigation state
Given user opens `/customers/create`
When sidebar renders
Then only Create Customer nav item is active.

### FE Scenario G: Compact create form and default title
Given user opens `/customers/create`
When create form renders
Then Title is shown as a dropdown with `Mr` selected by default
And core fields are arranged side-by-side where feasible on desktop.
