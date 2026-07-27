# UI Flow: INQCUST Frontend React

## Overview

This document describes user navigation and state transitions for the customer inquiry UI.

Backend endpoint consumed:
- `GET /api/v1/customers/{sortCode}/{customerNumber}`

## Primary Flow

1. User lands on Customer Inquiry page.
2. User enters `sortCode` and `customerNumber`.
3. User submits inquiry.
4. Frontend validates input.
5. If valid, frontend sends API request.
6. UI transitions to loading state.
7. UI transitions to one of: success, not found, validation error, system error, network timeout.

## Screen/State Model

### State A: Initial
- Empty form
- Submit disabled until minimally valid input (optional UX decision)

### State B: Client Validation Error
- Inline errors under fields
- Focus moved to first invalid field
- Error summary announced in ARIA live region

### State C: Loading
- Submit button disabled
- Progress indicator visible
- Optional cancel action if request cancellation is supported

### State D: Success (HTTP 200)
- Display customer header information
- Display `legacyStatus` block exactly
- Display `lookupMode` (`SPECIFIC`, `RANDOM`, `LATEST`)
- Display `riskAssessment` block exactly

### State E: Not Found (HTTP 404)
- Display not-found message
- Display `legacyStatus` values exactly
- Keep form values for quick correction/retry

### State F: Validation Error From Backend (HTTP 400)
- Display backend validation message list
- Map field-specific errors to corresponding inputs when possible

### State G: System Error (HTTP 5xx)
- Display non-technical failure message with retry action
- Optional expandable technical details for troubleshooting mode

### State H: Network Timeout/Connectivity Error
- Display timeout/offline message
- Provide retry action
- Preserve existing form data

## Special Input Commands UX

- `0000000000`:
  - UI helper text: command value triggers random lookup mode.
- `9999999999`:
  - UI helper text: command value triggers latest customer lookup mode.

## Scenario Mapping

- UI Scenario 1 -> Backend Scenario 1 (specific found)
- UI Scenario 2 -> Backend Scenario 2 (specific not found)
- UI Scenario 3 -> Backend Scenario 3 (latest)
- UI Scenario 4 -> Backend Scenario 4 (random)
- UI Scenario 5 -> Backend Scenario 5 (invalid request)
- UI Scenario 6 -> Backend Scenario 6 (risk assessment)

## Accessibility Notes

- All form controls have associated `<label>` elements.
- Errors use `aria-describedby` and `aria-live` regions.
- Result region gets focus heading after state transition.
- Color is never sole indicator of success/error state.

## Assumptions

- Single-page workflow for inquiry (no multi-step wizard).
- Backend returns JSON in consistent structure for all handled statuses.

## Out Of Scope

- Cross-feature navigation shell design.
- Internationalization/localization.
- Authentication gating flow.

## Open Questions

1. Should result cards persist when user edits inputs after a successful lookup?
2. Should we auto-submit when command values are detected?
3. Should retry use exponential backoff or manual-only retry?
