# Feature Specification: INQCUST Frontend React Customer Inquiry

**Feature Branch**: `002-inqcust-frontend-react`  
**Created**: 2026-07-13  
**Status**: Draft  
**Input**: Backend spec at `specs/001-inqcust-customer-inquiry-modernization/spec.md`.

## Purpose

Provide a React frontend for customer inquiry that consumes the existing backend API and reflects backend outcomes in clear, accessible UI states without redefining backend business rules.

## Relationship To Backend Spec

- Backend spec remains source of truth for business rules and response semantics.
- Frontend spec maps backend behavior into UX behavior.
- Backend endpoint consumed: `GET /api/v1/customers/{sortCode}/{customerNumber}`.

## User Stories

### Story 1: Banker inquiry lookup
As a banker, I want to enter sort code and customer number and quickly retrieve customer details so I can service customer inquiries during calls.

### Story 2: Customer-service exception handling
As a customer-service user, I want clear error states (invalid input, not found, server errors, timeout) so I can recover quickly and proceed with the next action.

### Story 3: Risk-aware review
As a customer-service user, I want risk assessment displayed exactly as returned by backend so I can make compliant servicing decisions.

## Acceptance Scenarios (Frontend Mapped To Backend)

### FE Scenario 1: Specific customer found (maps Backend Scenario 1)
Given user enters `123456` and `0000000001`  
When frontend calls backend endpoint  
Then UI shows success state  
And displays `legacyStatus.inquirySuccess = Y`  
And displays `legacyStatus.inquiryFailCode = 0`  
And renders customer data section.

### FE Scenario 2: Specific customer not found (maps Backend Scenario 2)
Given user enters `123456` and `0000009999`  
When frontend calls backend endpoint  
Then UI shows not-found state  
And displays `legacyStatus.inquirySuccess = N`  
And displays `legacyStatus.inquiryFailCode = 1`  
And indicates customer is unavailable.

### FE Scenario 3: Latest customer lookup (maps Backend Scenario 3)
Given user enters `123456` and `9999999999`  
When frontend calls backend endpoint  
Then UI shows success state  
And displays lookup mode as `LATEST`  
And renders returned customer details.

### FE Scenario 4: Random customer lookup (maps Backend Scenario 4)
Given user enters `123456` and `0000000000`  
When frontend calls backend endpoint  
Then UI shows success state  
And displays lookup mode as `RANDOM`  
And displays returned legacy status.

### FE Scenario 5: Invalid request (maps Backend Scenario 5)
Given sort code or customer number input is invalid format  
When user submits inquiry  
Then frontend blocks submission with client-side validation messages  
And if backend still returns 400, frontend renders backend validation details.

### FE Scenario 6: Risk assessment enhancement (maps Backend Scenario 6)
Given backend response includes `riskAssessment`  
When UI renders inquiry result  
Then UI displays `riskAssessment.riskRating` exactly  
And displays `riskAssessment.reasons` exactly as returned.

## Functional Requirements

- **FE-FR-001**: Frontend SHALL provide inquiry form with fields `sortCode` and `customerNumber`.
- **FE-FR-002**: Frontend SHALL validate `sortCode` with `^[0-9]{6}$` before submit.
- **FE-FR-003**: Frontend SHALL validate `customerNumber` with `^[0-9]{10}$` before submit.
- **FE-FR-004**: Frontend SHALL support command values `0000000000` (RANDOM) and `9999999999` (LATEST).
- **FE-FR-005**: Frontend SHALL show loading indicator while request is in progress.
- **FE-FR-006**: Frontend SHALL show success result state for HTTP 200 responses and render message content from backend response.
- **FE-FR-007**: Frontend SHALL show not-found result state for HTTP 404 responses and render message content from backend response.
- **FE-FR-008**: Frontend SHALL show validation error state for client validation and backend 400 responses.
- **FE-FR-009**: Frontend SHALL show backend error state for non-200/404 responses and render backend `errorCode`, `message`, and field errors when present.
- **FE-FR-010**: Frontend SHALL handle request timeout/network failure with retry action.
- **FE-FR-011**: Frontend SHALL render `legacyStatus` and `riskAssessment` values exactly as returned.
- **FE-FR-012**: Frontend SHALL call backend API at runtime for all inquiry outcomes; no runtime hardcoded fixture responses are allowed.

## Accessibility Requirements

- **FE-A11Y-001**: All inputs and actions SHALL be keyboard accessible.
- **FE-A11Y-002**: Inputs SHALL include accessible labels and descriptions.
- **FE-A11Y-003**: Focus indicators SHALL remain visible for all interactive elements.
- **FE-A11Y-004**: Validation and system messages SHALL be announced via ARIA live regions.
- **FE-A11Y-005**: Result sections SHALL use semantic headings/regions for screen reader navigation.

## Non-Functional Requirements

- **FE-NFR-001**: Initial page load (excluding API time) target <= 2.0s on standard dev laptop/browser profile.
- **FE-NFR-002**: Inquiry submission to UI state transition target <= 200ms after response arrival.
- **FE-NFR-003**: UI SHALL be responsive for mobile (>=360px width) and desktop layouts.
- **FE-NFR-004**: Frontend SHALL provide observability hooks for request start/success/error and mode (mock/live).

## Assumptions

- Backend endpoint and response schema remain as defined in backend spec and runtime OpenAPI.
- Authentication/authorization is out of scope for this feature.
- Frontend will be implemented in React with TypeScript.

## Out Of Scope

- Changing backend business rules or fail-code semantics.
- Introducing new backend fields not already defined.
- Production analytics vendor selection and dashboard implementation.
- Full design-system rollout beyond inquiry workflow screens.

## Open Questions

1. Should frontend expose lookup mode labels to users (friendly text) or raw enum values only?
2. What timeout threshold should be used before showing retry UI?
3. Is there a required branding/theme baseline for this React app?
4. Should mock mode use MSW interceptors or a local mock service endpoint?
