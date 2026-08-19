# Feature Specification - Bank Statement Retrieval Capability

## Feature Description
Provide a bank statement retrieval capability that returns a statement record of account activity for a requested period, aligned to legacy BNKSTMT batch statement-generation semantics.

## Authority
- legacy-bankofz/base/batch/pli/BNKSTMT.pli
- legacy-bankofz/base/batch/jcl/BNKSTMT.jcl

## User Story
US ACC-03 - Statement Retrieval
As a bank employee, I want to retrieve an account statement, so that I can provide a customer with a record of their account activity over a period.

## Scope
### In Scope
- Statement retrieval for one account identity and statement period.
- Statement summary values and transaction line items for the selected period.
- Read-only behavior.
- Legacy-aligned period semantics derived from BNKSTMT processing.
- Modernized historical opening-balance calculation for period start.

### Out of Scope
- Real batch job submission from the modern API.
- Any mutation of account or transaction data.
- Transaction-detail drilldown semantics from INQTRAND.
- Generic transaction inquiry APIs that are not statement-oriented.
- Returning CUSTOMER master-data block in statement response.

## API Behaviour
GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period}

Path parameters:
- sortCode: exactly 6 digits.
- accountNumber: exactly 8 digits.
- period: YYYYMM (statement month where MM is 01-12).

## Functional Requirements
- FR-001: Retrieve statement data for one account only using exact account predicate (`sortCode` and `accountNumber`) and requested period; multi-account batch looping behavior is not allowed in API flow.
- FR-002: Apply statement period boundaries using standard calendar logic (including leap-year handling for February) while preserving BNKSTMT-aligned period semantics.
- FR-003: Include statement summary fields: periodFrom, periodTo, opening balance, total credits, total debits, closing balance.
- FR-003a: Opening balance must represent a true historical balance at periodFrom and must not replicate BNKSTMT's legacy reverse-from-current formula. Modern implementation shall derive opening balance from period-bounded ledger semantics (point-in-time calculation).
- FR-004: Include ordered transaction entries for the period.
- FR-005: Return successful empty statement when account exists but no period transactions exist.
- FR-006: Return not-found outcome when account identity does not exist.
- FR-007: Preserve read-only behavior and avoid any data mutation.
- FR-008: Return technical-failure outcome for retrieval errors.
- FR-009: If account data exists but a related CUSTOMER row is missing, statement retrieval remains successful (HTTP 200) because CUSTOMER block is out of contract for this feature.
- FR-010: When transaction description is null in source data, API returns description value `N/A` for that entry.

## Non-Functional Requirements
- NFR-001: Maintain deterministic period and balance calculations per request.
- NFR-002: Keep controller thin and statement assembly logic in service layer.
- NFR-003: Protect sensitive account/transaction values in logs.

## Security Requirements
- SR-001: Endpoint requires authentication (401 for unauthenticated).
- SR-002: Endpoint requires authorization (403 for unauthorized).
- SR-003: Response and errors must follow standard error envelope conventions.

## Acceptance Criteria
- AC-001:
	Given an authenticated and authorized user and a valid account with period transactions,
	When the user calls GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} with a valid period,
	Then the response is 200 and includes statement summary plus ordered period transactions.
- AC-002:
	Given an authenticated and authorized user,
	When the user calls GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} with invalid path parameter format or an invalid month (not 01-12),
	Then the response is 400 and follows the standard error envelope.
- AC-003:
	Given a request without valid authentication,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called,
	Then the response is 401 and follows the standard error envelope.
- AC-004:
	Given an authenticated user without required authorization,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called,
	Then the response is 403 and follows the standard error envelope.
- AC-005:
	Given an authenticated and authorized user,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called for a non-existent account identity,
	Then the response is 404 and follows the not-found error contract.
- AC-006:
	Given an authenticated and authorized user and a simulated retrieval failure in dependencies,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called,
	Then the response is 500 and follows the technical-error envelope for endpoint-level validation.
- AC-007:
	Given an authenticated and authorized user and a leap-year February period,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called,
	Then the 200 response includes Feb 29 transactions when such transactions exist.
- AC-008:
	Given an authenticated and authorized user,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called,
	Then the response contains only rows for the requested account and excludes same-sortcode other-account rows.
- AC-009:
	Given an authenticated and authorized user and an existing account with statement data but missing CUSTOMER row,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called,
	Then the response is 200 and statement retrieval succeeds without requiring CUSTOMER block data.
- AC-010:
	Given an authenticated and authorized user and source transactions with null description values,
	When GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period} is called,
	Then each null description is returned as N/A in the response.
