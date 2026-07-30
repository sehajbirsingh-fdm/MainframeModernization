# Implementation Plan: Frontend Website Shell And Consistent Theme

Branch: frontend-modernization
Date: 2026-07-30
Spec: specs/frontend-modernization/spec.md

## Summary

Implement a shared website shell around existing feature pages and unify visual styling without changing backend integrations or feature logic.

## Objectives

- Deliver landing page, global header, nav, footer, About page, and License page.
- Keep all feature routes and behavior intact.
- Ensure consistent typography, spacing, components, and responsive layout.
- Preserve existing test pass status.

## Technical Approach

1. Refactor router to nested layout using React Router:
   - Parent layout with header/sidebar/footer and `<Outlet />`.
   - Child routes for feature pages plus informational pages.
2. Set `/` as landing route with quick-start links.
3. Add route redirects for unknown paths back to `/`.
4. Consolidate shared styles in `index.css`:
   - Theme tokens (colors, spacing, type).
   - Shared shell styles and responsive breakpoints.
   - Navigation active states and focus states.
5. Keep feature page components unchanged unless required for compatibility.
6. Run frontend tests to validate non-regression.

## Risks And Mitigations

Risk: Navigation refactor could break direct route rendering.
Mitigation: Preserve existing child route paths and keep feature components unchanged.

Risk: Styling changes could reduce readability on small screens.
Mitigation: Add explicit mobile breakpoints for layout and grid collapse.

Risk: Cross-feature visual inconsistency persists.
Mitigation: Use one shared global stylesheet and existing common class names (`page`, `page-header`, `card`, `field`, `actions`).

## Validation Strategy

- Execute existing Vitest suite after changes.
- Manually verify route navigation:
  - `/`, `/customers`, `/customers/create`, `/accounts`, `/customer-accounts`, `/about`, `/license`.
- Manually verify responsive behavior at mobile and desktop widths.

## Deliverables

- Updated `frontend/app/src/App.tsx` with shell and routes.
- Updated `frontend/app/src/index.css` with consistent theme and responsive layout.
- Updated spec artifacts in `specs/frontend-modernization/` and `specs/frontend-modernization/supporting/`.
