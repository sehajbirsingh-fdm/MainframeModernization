# Frontend Test Specification: Website Shell + Banking Features

## Test Strategy

Test pyramid for frontend:
- Unit tests: utility functions, validators, mappers.
- Component tests: shell, nav, page sections, form, result, error and accessibility behavior.
- Integration tests (mocked API): state transitions by HTTP/status payload.
- E2E tests: route navigation journey and inquiry outcomes.

## Test Coverage Matrix

### FE-TC-101 Landing route renders website shell
Given user opens `/`
When app renders
Then header, nav, landing content, and footer are visible.

### FE-TC-102 Navigation links route correctly
Given user clicks links in sidebar/top nav
When route changes
Then expected page component renders for each target route.

### FE-TC-103 Active route style indication
Given a route is active
When navigation renders
Then active link style is applied for that route.

### FE-TC-108 Active route exclusivity
Given user opens `/customers/create`
When sidebar renders
Then only Create Customer nav link is active and Customer Inquiry is not active.

### FE-TC-104 About and License pages render
Given user opens `/about` or `/license`
When page renders
Then informational content appears within shared shell.

### FE-TC-105 Unknown route redirects to landing
Given user opens an unmatched route
When router resolves it
Then app redirects to `/`.

### FE-TC-001 Form validation: sortCode format
Given invalid sort code input  
When submit is attempted  
Then frontend shows format error and blocks request.

### FE-TC-002 Form validation: customerNumber format
Given invalid customer number input  
When submit is attempted  
Then frontend shows format error and blocks request.

### FE-TC-003 Success scenario (maps backend scenario 1)
Given API returns 200 with specific customer payload  
When inquiry is submitted  
Then success panel renders `legacyStatus`, `customer`, and `riskAssessment`.

### FE-TC-004 Not found scenario (maps backend scenario 2)
Given API returns 404 with not-found payload  
When inquiry is submitted  
Then not-found UI renders with `legacyStatus` and retry action.

### FE-TC-005 Latest scenario (maps backend scenario 3)
Given API returns 200 for `9999999999` with lookup mode `LATEST`  
When inquiry is submitted  
Then UI shows `LATEST` and returned customer.

### FE-TC-006 Random scenario (maps backend scenario 4)
Given API returns 200 for `0000000000` with lookup mode `RANDOM`  
When inquiry is submitted  
Then UI shows `RANDOM` and returned status/customer.

### FE-TC-007 Backend validation error (maps backend scenario 5)
Given API returns 400 with field errors  
When inquiry is submitted  
Then UI maps and displays backend validation details.

### FE-TC-008 Risk assessment rendering (maps backend scenario 6)
Given API success payload includes `riskAssessment.riskRating=HIGH`  
When UI renders result  
Then risk card shows rating and reasons exactly.

### FE-TC-009 System error handling
Given API returns 500  
When inquiry is submitted  
Then system error state appears with retry action.

### FE-TC-010 Network timeout handling
Given request times out  
When inquiry is submitted  
Then timeout state appears and user can retry.

### FE-TC-011 Accessibility keyboard flow
Given keyboard-only user  
When navigating form and result sections  
Then all actions are reachable and visible focus is present.

### FE-TC-012 Accessibility error announcement
Given validation/system errors occur  
When state updates  
Then screen reader live region announces relevant message.

### FE-TC-106 Responsive shell behavior
Given desktop and mobile breakpoints
When layout renders
Then shell adapts without hiding required navigation actions.

### FE-TC-107 Non-regression feature behavior
Given existing feature page tests
When test suite runs after shell/theme updates
Then prior behavior-oriented tests remain green.

### FE-TC-109 Create form compact layout and default title
Given user opens `/customers/create`
When create form renders
Then title is a dropdown with `Mr` selected by default
And key fields are arranged in side-by-side rows on desktop.

## Unit Test Targets

- Input regex validators
- Command value helpers (`0000000000`, `9999999999`)
- API response-to-view model transforms (non-semantic)
- Error normalization utilities

## Component Test Targets

- Inquiry form
- Loading indicator
- Success result panel
- Not-found panel
- Error/timeout panels
- Risk assessment card

## Integration Test Targets (Mocked API)

- End-to-end state transitions by status code
- Retry behavior
- Mode switch impact (mock/live config path selection)

## E2E Test Targets

- Landing to feature navigation flow
- Happy path specific lookup
- Latest command lookup
- Random command lookup
- Not found path
- Invalid request path
- System error path with retry

## Assumptions

- Frontend uses stable mock API tooling for deterministic tests.
- Backend contract fixtures are kept versioned with spec updates.

## Out Of Scope

- Performance/load tests at browser scale.
- Visual regression baseline tooling selection.
- Cross-browser matrix beyond default supported environments.

## Open Questions

1. Which e2e environment should be default in CI: mock API only or live backend smoke?
2. Should contract fixtures be generated from OpenAPI or handwritten snapshots?
3. What minimum accessibility standard target is required (WCAG level)?
