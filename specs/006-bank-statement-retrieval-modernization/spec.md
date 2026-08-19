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

### Out of Scope
- Real batch job submission from the modern API.
- Any mutation of account or transaction data.
- Transaction-detail drilldown semantics from INQTRAND.
- Generic transaction inquiry APIs that are not statement-oriented.

## API Behaviour
GET /api/v1/accounts/{sortCode}/{accountNumber}/statements/{period}

Path parameters:
- sortCode: exactly 6 digits.
- accountNumber: exactly 8 digits.
- period: YYYYMM (statement month where MM is 01-12).

## Functional Requirements
- FR-001: Retrieve statement data for exact account identity and period.
- FR-002: Apply statement period boundaries consistent with BNKSTMT period logic.
- FR-003: Include statement summary fields: periodFrom, periodTo, opening balance, total credits, total debits, closing balance.
- FR-004: Include ordered transaction entries for the period.
- FR-005: Return successful empty statement when account exists but no period transactions exist.
- FR-006: Return not-found outcome when account identity does not exist.
- FR-007: Preserve read-only behavior and avoid any data mutation.
- FR-008: Return technical-failure outcome for retrieval errors.

## Non-Functional Requirements
- NFR-001: Maintain deterministic period and balance calculations per request.
- NFR-002: Keep controller thin and statement assembly logic in service layer.
- NFR-003: Protect sensitive account/transaction values in logs.

## Security Requirements
- SR-001: Endpoint requires authentication (401 for unauthenticated).
- SR-002: Endpoint requires authorization (403 for unauthorized).
- SR-003: Response and errors must follow standard error envelope conventions.

## Acceptance Criteria
- AC-001: Valid account/period returns statement summary and period transactions.
- AC-002: Invalid path parameter format or invalid statement month (outside 01-12) returns 400.
- AC-003: Unauthenticated request returns 401.
- AC-004: Unauthorized request returns 403.
- AC-005: Missing account returns not-found contract outcome.
- AC-006: Retrieval failure returns 500 technical-error contract outcome.
