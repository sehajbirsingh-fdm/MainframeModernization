# UI Flow: Website Shell + Banking Features

## Overview

This document defines navigation and page flow for the React frontend after shell modernization.

## Global Navigation Model

Routes exposed in global nav:
- `/` (Landing)
- `/customers` (INQCUST)
- `/customers/create` (CRECUST)
- `/accounts` (INQACC)
- `/customer-accounts` (INQACCCU)
- `/about`
- `/license`

Unknown routes:
- Any unmatched path redirects to `/`.

## Layout Regions

1. Header:
- Product title and modernization context.
- User identity chip shown in top-right.

2. Navigation:
- Desktop: left sidebar with grouped links.
- Mobile: stacked nav links with same route coverage.
- Active route highlight required.
- Active state must be exact-match only, so nested routes do not double-highlight parent links.

3. Content region:
- Renders route-specific page via router outlet.
- Existing feature pages retain form and result behavior.

4. Footer:
- Program and license context visible on all routes.

## Landing Flow

1. User lands on `/`.
2. User sees quick-start cards linking to feature routes.
3. User selects a workflow and navigates directly.

## Feature Flow Continuity

All existing inquiry/create flows remain unchanged inside shared shell:
- INQCUST customer inquiry.
- CRECUST customer creation.
- INQACC account inquiry.
- INQACCCU customer-account relationship inquiry.

Create Customer layout behavior:
- Title is selected from a dropdown, defaulting to `Mr`.
- Form uses compact desktop grid with side-by-side fields where feasible.
- Mobile layout collapses to single-column fields.

## Informational Page Flow

- `/about`: program intent and modernization goals.
- `/license`: legal and usage summary with reference to repository license.

## Accessibility Notes

- Header/nav/content/footer use semantic structure.
- Nav and links are keyboard accessible.
- Focus states are visible for links, inputs, and buttons.
- Existing live-region messaging in feature pages remains intact.

## Responsive Behavior

- Desktop: two-column layout with sidebar + content.
- Tablet/mobile: single-column stack with nav above content.
- Data-heavy tables continue horizontal scroll via `table-wrap`.
