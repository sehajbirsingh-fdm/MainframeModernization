# Frontend-Backend Contract: INQCUST Inquiry

## Scope

Defines how frontend consumes backend endpoint and maps responses to UI states.

Source of truth for backend behavior:
- `specs/001-inqcust-customer-inquiry-modernization/spec.md`

## Endpoint

- Method: `GET`
- Path: `/api/v1/customers/{sortCode}/{customerNumber}`
- Path params:
  - `sortCode`: 6 digits (`^[0-9]{6}$`)
  - `customerNumber`: 10 digits (`^[0-9]{10}$`)

## Command Values

- `0000000000` -> RANDOM lookup command
- `9999999999` -> LATEST lookup command

## Response Shape (Consumed By Frontend)

### Common envelope
- `legacyStatus`
  - `inquirySuccess`
  - `inquiryFailCode`
  - `message`
- `lookupMode` (`SPECIFIC|RANDOM|LATEST`)
- `customer` (nullable)
- `riskAssessment` (nullable)

### Success (200)
- `legacyStatus.inquirySuccess = Y`
- `customer` object present
- `riskAssessment` present for found customers

### Not found (404)
- `legacyStatus.inquirySuccess = N`
- `customer = null`
- `riskAssessment = null`

### Validation error (400)
- Error response object with validation details

### Server error (500)
- Error response object for system failure

## UI Mapping Table

| HTTP | Backend meaning | Frontend state | Required UI fields |
|---|---|---|---|
| 200 | Found/success | Success | `legacyStatus`, `lookupMode`, `customer`, `riskAssessment` |
| 404 | Not found | Not Found | `legacyStatus`, retry affordance |
| 400 | Validation issue | Validation Error | field errors and form-level error |
| 500 | System failure | System Error | generic message + retry |
| network timeout | Connectivity issue | Network Timeout | timeout/offline message + retry |

## Contract Rules

- Frontend SHALL not transform `legacyStatus` and `riskAssessment` values semantically.
- Frontend SHALL not infer business outcomes not present in backend response.
- Frontend SHALL preserve backend fail-code semantics in display/debug views.

## Configuration Contract

- Frontend mode switch:
  - Mock mode: API calls routed to mock handlers/data.
  - Live mode: API calls routed to backend base URL.
- Switching mode is configuration-only (no component-level branching).

## Error Handling Contract

- Frontend must gracefully handle malformed/partial payloads with safe fallback UI.
- Frontend logs contract mismatch events to observability hook.

## Assumptions

- Backend response fields remain backward-compatible for this feature lifecycle.
- Frontend and backend are versioned and deployed with compatible contract revisions.

## Out Of Scope

- Backend authentication token flow.
- API versioning policy definition.
- Cross-service fallback orchestration.

## Open Questions

1. Should UI expose fail code to end users or only in diagnostic panel?
2. Is there a formal backend version header to assert contract compatibility?
3. Should frontend cache prior successful inquiry responses?
