# Tasks: INQACCCU Customer-Account Relationship Modernization

## Phase 1: Setup Tasks

| Task ID | Title | Description | Dependencies | Acceptance / Done Criteria |
|---|---|---|---|---|
| T001 | [X] Establish feature implementation skeleton | Create INQACCCU-specific backend and frontend feature scaffolding in existing modules without creating new applications. | None | Backend package/file skeleton and frontend feature folder structure exist in existing modules; project builds still resolve. |
| T002 | [X] Wire feature routing and module registration | Register INQACCCU backend endpoint wiring and frontend route/menu entry points in current app shells. | T001 | Backend route is discoverable by Spring mapping; frontend route is reachable from app navigation and loads feature page shell. |
| T003 | [X] Align runtime configuration for local integration | Add or update feature configuration keys for backend runtime and frontend API base/proxy usage in existing config locations. | T001 | Backend starts with feature config in local mode; frontend calls resolve to backend through configured base URL/proxy path. |

## Phase 2: Backend Tasks

| Task ID | Title | Description | Dependencies | Acceptance / Done Criteria |
|---|---|---|---|---|
| T004 | [X] Implement INQACCCU API/domain response models | Create backend response and error model classes for customer-account inquiry aligned to frozen contract fields and naming. | T001 | Model classes compile; fields/types align to frozen contract structures used by controller/service mappings. |
| T005 | [X] Implement customerNumber request validation path | Implement path-parameter validation and validation-error translation for INQACCCU endpoint inputs. | T004 | Invalid customerNumber inputs return standardized validation error payload; valid-format reserved values continue through business path. |
| T006 | [X] Implement customer validation capability adapter | Implement service/repository boundary for customer validation before account retrieval, reusing INQCUST-equivalent behavior. | T004 | Customer validation dependency is invoked before account retrieval in orchestration flow; adapter is unit-testable. |
| T007 | [X] Implement account retrieval repository and query flow | Implement read-only account retrieval by validated customer number and internally derived sort code using repository abstraction. | T006 | Retrieval path returns account rows through abstraction, enforces read-only behavior, and supports bounded return volume. |
| T008 | [X] Implement legacy-to-contract account/date mapping | Implement mapping/conversion logic for account fields, identifier preservation, and date conversion to external representation. | T007 | Mapper output contains all required account fields with correct external date representation and identifier preservation. |
| T009 | [X] Implement legacy status and outcome mapping | Implement backend outcome mapping for success, not-found, and retrieval-stage failCode paths into business response envelope. | T006, T007, T008 | Response builder maps statuses/failCodes consistently for all supported business outcomes. |
| T010 | [X] Implement INQACCCU orchestration service | Implement end-to-end service flow: validate customer, retrieve accounts, map outcomes, and return contract response. | T005, T006, T007, T008, T009 | Service composes complete inquiry response and handles retrieval/open/fetch/close failure mapping paths. |
| T011 | [X] Implement INQACCCU controller endpoint | Implement GET /api/v1/customers/{customerNumber}/accounts controller method and integrate with orchestration service. | T010 | Endpoint returns application/json business/error payloads with expected status code handling and schema shape. |
| T012 | [X] Enforce runtime API conformance to frozen OpenAPI contract | Implement and verify runtime endpoint/request/response conformance against the frozen INQACCCU OpenAPI contract used as the authoritative reference. | T011 | Runtime INQACCCU API implementation conforms to frozen path, parameter, response schema, and example expectations. |

## Phase 3: Frontend Tasks

| Task ID | Title | Description | Dependencies | Acceptance / Done Criteria |
|---|---|---|---|---|
| T013 | [X] Implement frontend domain types for INQACCCU | Add frontend types/interfaces for business and error responses used by INQACCCU inquiry flow. | T004 | Frontend type definitions compile and align to backend response/error payloads consumed by UI. |
| T014 | [X] Implement INQACCCU API client | Add frontend API client function for INQACCCU endpoint invocation and normalized response/error handling. | T013, T011 | Client submits customer-number inquiry to INQACCCU endpoint and returns typed success/validation/infrastructure outcomes. |
| T015 | [X] Implement frontend input validation logic | Implement customer-number validation logic and user feedback behavior in feature form handling. | T013 | Validation behavior blocks invalid submit attempts and presents actionable field-level feedback. |
| T016 | [X] Implement INQACCCU inquiry page UI | Build/extend inquiry page in existing frontend module for input, submit action, loading indicator, and outcome rendering. | T014, T015 | Page renders inquiry form and displays returned outcomes for accounts, zero accounts, not found, and retrieval/infrastructure errors. |
| T017 | [X] Implement account result presentation componentization | Implement UI sections/table/cards for rendering returned account records and summary fields from inquiry responses. | T016 | All returned account fields are rendered from API payload with stable formatting and preserved identifiers. |
| T018 | [X] Implement subsequent inquiry interaction flow | Implement inquiry-page interaction where users update inquiry input and submit another request, and the UI displays the newly completed inquiry result. | T016 | Users can submit additional inquiries in same page flow and UI reflects latest completed response without stale state leakage. |

## Phase 4: Integration Tasks

| Task ID | Title | Description | Dependencies | Acceptance / Done Criteria |
|---|---|---|---|---|
| T019 | [X] Integrate frontend route with application navigation | Connect INQACCCU inquiry page into existing frontend route map and app navigation entry points. | T016 | Feature route is reachable through configured navigation and direct route access. |
| T020 | [X] Integrate frontend-backend request path in local runtime | Validate and adjust frontend API base/proxy configuration and backend CORS/runtime settings for local integrated execution. | T003, T014, T019 | Local run supports successful browser-to-backend requests for INQACCCU without manual request rewrites. |
| T021 | [X] Integrate mock data and adapter behavior for scenario coverage | Ensure backend adapters/mock data paths support required inquiry outcome scenarios used by feature and tests. | T007, T009, T010 | Controlled test scenarios are available for all supported inquiry outcomes and failCode paths. |

## Phase 5: Testing Tasks

| Task ID | Title | Description | Dependencies | Acceptance / Done Criteria |
|---|---|---|---|---|
| T022 | [X] Add backend unit tests for orchestration and mapping | Implement unit tests for service orchestration, status/failCode mapping, count bounds, and mapping transformations. | T010 | Unit test suite covers major orchestration branches and mapping outputs; tests pass in CI/local execution. |
| T023 | [X] Add backend repository/adapter tests | Implement tests for retrieval behavior, row-cap enforcement, read-only path handling, and end-of-data behavior. | T007, T021 | Repository tests verify bounded retrieval and non-error end-of-data handling; tests pass consistently. |
| T024 | [X] Add backend controller and exception-handler tests | Implement controller tests for success/business outcomes and validation/infrastructure error responses. | T011 | Controller tests verify response status/content types/schema structure across supported response classes. |
| T025 | [X] Add frontend validation and page behavior tests | Implement frontend tests for input validation, loading behavior, and inquiry outcome rendering paths. | T016, T018 | Frontend tests pass for invalid input, success rendering, zero-account rendering, not-found, and error presentation. |
| T026 | [X] Add frontend API client tests | Implement tests for client request path, response parsing, and normalized error handling. | T014 | API client tests pass for success, validation, and infrastructure/network failure handling. |
| T027 | [X] Add integrated backend-frontend flow tests | Implement integration/E2E tests to verify user inquiry flow from frontend submit through backend response presentation. | T020, T022, T024, T025, T026 | Integrated test run validates end-to-end inquiry behavior and passes in local/CI pipeline. |
| T028 | [X] Add contract conformance verification tests | Implement automated checks that backend responses conform to frozen OpenAPI schemas for INQACCCU endpoint. | T012, T024 | Contract verification passes for response families used by INQACCCU endpoint. |

## Phase 6: Documentation Tasks

| Task ID | Title | Description | Dependencies | Acceptance / Done Criteria |
|---|---|---|---|---|
| T029 | [X] Update backend module documentation | Update src/api module documentation for INQACCCU endpoint usage, configuration, and test/run instructions. | T011, T024 | Backend docs include INQACCCU endpoint/runtime/test details and are accurate for local execution. |
| T030 | [X] Update frontend module documentation | Update src/frontend-react documentation for INQACCCU route usage, API integration configuration, and test/run instructions. | T019, T025 | Frontend docs include INQACCCU feature route and execution instructions aligned to implemented flow. |
| T031 | [X] Update feature quickstart and traceability references | Update feature quickstart/supporting references to point to implemented backend/frontend execution and verification commands. | T027, T028, T029, T030 | Feature quickstart and supporting references are synchronized with implemented workflow and validation steps. |

## Completion Checklist

- [X] All tasks T001-T031 are completed.
- [X] Backend and frontend build/test pipelines pass with INQACCCU changes.
- [X] Runtime endpoint and frontend route are integrated and manually smoke-tested.
- [X] Frozen contract conformance checks pass for INQACCCU responses.
- [X] Documentation updates are complete and match implemented behavior.
