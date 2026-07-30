# Frontend Traceability Matrix: Website Shell + Banking Features

## Purpose

Link frontend requirements and tests to backend scenarios and endpoint contract fields.

| Frontend Ref | Description | Backend Scenario/Rule | Endpoint Field(s) | Test Cases |
|---|---|---|---|---|
| FE-FR-101 | Landing page at `/` | n/a | n/a | FE-TC-101 |
| FE-FR-102 | Global header with user chip | n/a | n/a | FE-TC-101 |
| FE-FR-103 | Global navigation across routes | n/a | n/a | FE-TC-102, FE-TC-103 |
| FE-FR-104 | Informational routes `/about` and `/license` | n/a | n/a | FE-TC-104 |
| FE-FR-105 | Global footer rendered on routes | n/a | n/a | FE-TC-101 |
| FE-FR-106 | Existing feature routes unchanged | Existing feature behavior continuity | route paths | FE-TC-107 |
| FE-FR-107 | Existing feature logic preserved | Backend endpoint behavior unchanged | existing endpoint contracts | FE-TC-107 |
| FE-FR-108 | Unknown routes redirect to `/` | n/a | n/a | FE-TC-105 |
| FE-FR-109 | Exact-only active nav state | n/a | route path match behavior | FE-TC-108 |
| FE-FR-110 | Create page title dropdown default `Mr` | n/a | frontend form field `title` | FE-TC-109 |
| FE-UX-101..105 | Shared shell/theme consistency | n/a | n/a | FE-TC-101, FE-TC-102, FE-TC-106 |
| FE-UX-106 | Compact side-by-side create form layout | n/a | frontend layout classes | FE-TC-109, FE-TC-106 |
| FE-FR-001 | Inquiry form with required inputs | Backend FR-001 | path params `sortCode`, `customerNumber` | FE-TC-001, FE-TC-002 |
| FE-FR-002 | sortCode client validation | Backend FR-002 | `sortCode` | FE-TC-001 |
| FE-FR-003 | customerNumber client validation | Backend FR-003 | `customerNumber` | FE-TC-002 |
| FE-FR-004 | command values supported | Backend BR-001, BR-001a, BR-002 | `customerNumber` commands | FE-TC-005, FE-TC-006 |
| FE-FR-005 | loading state during request | Backend endpoint call lifecycle | n/a | FE-TC-003 to FE-TC-010 |
| FE-FR-006 | success rendering | Backend Scenario 1 | `legacyStatus`, `customer`, `riskAssessment` | FE-TC-003 |
| FE-FR-007 | not-found rendering | Backend Scenario 2 | `legacyStatus`, `customer` | FE-TC-004 |
| FE-FR-008 | validation error rendering | Backend Scenario 5 | error payload fields | FE-TC-007 |
| FE-FR-009 | system error rendering | Backend 500 response behavior | error payload fields | FE-TC-009 |
| FE-FR-010 | timeout and retry behavior | Frontend resilience requirement | n/a | FE-TC-010 |
| FE-FR-011 | exact legacy/risk field display | Backend FR-007, FR-012, BR-008..BR-011 | `legacyStatus.*`, `riskAssessment.*` | FE-TC-003, FE-TC-004, FE-TC-008 |
| FE-FR-012 | config-based mock/live mode | Backend integration mode strategy | base URL/mode config | FE-TC-010 + integration config tests |
| FE-A11Y-001..005 | keyboard, labels, focus, announcements | Frontend accessibility requirements | n/a | FE-TC-011, FE-TC-012 |

## Scenario Mapping

| Frontend Scenario | Backend Scenario |
|---|---|
| FE Scenario 1: specific found | Backend Scenario 1 |
| FE Scenario 2: specific not found | Backend Scenario 2 |
| FE Scenario 3: latest lookup | Backend Scenario 3 |
| FE Scenario 4: random lookup | Backend Scenario 4 |
| FE Scenario 5: invalid request | Backend Scenario 5 |
| FE Scenario 6: risk enhancement rendering | Backend Scenario 6 |

Additional shell scenarios:
- FE Scenario A: landing entry
- FE Scenario B: feature continuity in shared shell
- FE Scenario C: informational pages
- FE Scenario D: responsive navigation
- FE Scenario E: unknown route redirect

## Contract Field Mapping Summary

- Success view consumes:
  - `legacyStatus.inquirySuccess`
  - `legacyStatus.inquiryFailCode`
  - `legacyStatus.message`
  - `lookupMode`
  - `customer` object
  - `riskAssessment.riskRating`
  - `riskAssessment.reviewRequired`
  - `riskAssessment.reasons[]`

- Error views consume:
  - 400 validation response fields
  - 500 error response fields
  - network/timeout client error metadata

## Assumptions

- Backend scenario numbering remains stable.
- Contract fields used by frontend stay backward compatible.

## Out Of Scope

- Detailed backend implementation traceability.
- Release-level change management workflow.

## Open Questions

1. Should traceability IDs be added into PR templates for mandatory linkage?
2. Do we require automated traceability checks in CI?
3. Should frontend traceability include design artifact IDs as well?
