# spec.md - INQACCCU Customer Account Relationship Modernization

## Feature
Customer Account Inquiry.

## Objective
Provide a modern inquiry capability, exposed through the backend API and existing frontend application, that preserves INQACCCU-equivalent externally observable business behavior, status semantics, and account data semantics.

## Scope

This specification defines the external feature contract for customer-account inquiry behavior.

This specification includes externally observable behavior for both API outcomes and frontend inquiry user experience.

This specification does not define implementation structure, deployment, or internal component design.

## Actors

- Banking channel or downstream consumer invoking customer-account inquiry.

## Preconditions

- Caller provides a customer number inquiry value.

## Frontend Interaction Scope

- A user can submit customer account inquiries through the existing frontend application.
- The frontend invokes the backend inquiry endpoint and presents returned outcomes.
- Frontend-visible behavior preserves the same inquiry semantics and status distinctions defined for the backend business outcomes.
- The frontend serves as a usable demonstration channel for legacy-equivalent inquiry capability and does not introduce new business functionality.

## Endpoint
```http
GET /api/v1/customers/{customerNumber}/accounts
```

## Request parameters

- `customerNumber` (required)
	- exactly 10 characters
	- digits only (`0-9`)
	- leading zeroes are significant and must be preserved

## Validation rules

1. `customerNumber` is required.
2. Blank values are invalid input.
3. Length other than 10 is invalid input.
4. Non-digit characters are invalid input.
5. `0000000000` and `9999999999` are syntactically valid identifiers but are treated as customer-not-found behavior.

Invalid input is distinct from customer-not-found behavior.

## Business behavior

1. Inquiry is read-only.
2. Customer validation is mandatory and occurs before account retrieval.
3. Customer existence is determined by customer validation outcome, not by the number of returned accounts.
4. Account retrieval uses customer number plus internally derived fixed sort code `987654`.
5. The response contains no more than 20 account records.
6. Account ordering is not guaranteed.
7. End-of-data equivalent to legacy SQLCODE +100 is treated as normal completion.

## Outcome behavior

### Successful retrieval with accounts

- `legacyStatus.success = "Y"`
- `legacyStatus.customerFound = "Y"`
- `numberOfAccounts` is between 1 and 20
- `accounts` contains returned account entries

### Successful retrieval with zero accounts

- `legacyStatus.success = "Y"`
- `legacyStatus.customerFound = "Y"`
- `numberOfAccounts = 0`
- `accounts` is empty

### Customer not found

- `legacyStatus.success = "N"`
- `legacyStatus.failCode = "1"`
- `legacyStatus.customerFound = "N"`
- `numberOfAccounts = 0`
- `accounts` is empty

### Retrieval failure behavior

- For failCode `2` (retrieval open-stage failure), the business response is returned with:
	- `legacyStatus.success = "N"`
	- `legacyStatus.failCode = "2"`
	- `legacyStatus.customerFound = "Y"`
	- `customerNumber` present (echoed from validated request)
	- `numberOfAccounts = 0`
	- `accounts` empty
- For failCode `3` (retrieval fetch-stage failure), the business response is returned with:
	- `legacyStatus.success = "N"`
	- `legacyStatus.failCode = "3"`
	- `legacyStatus.customerFound = "Y"`
	- `customerNumber` present (echoed from validated request)
	- `numberOfAccounts = 0`
	- `accounts` empty
- For failCode `4` (retrieval close-stage failure), the business response is returned with:
	- `legacyStatus.success = "N"`
	- `legacyStatus.failCode = "4"`
	- `legacyStatus.customerFound = "Y"`
	- `customerNumber` present (echoed from validated request)
	- `numberOfAccounts = 0`
	- `accounts` empty

### Infrastructure failure behavior

- If the service cannot process the request due to a non-business infrastructure error, an error response is returned.
- Infrastructure failure outcomes are distinct from business outcomes expressed with legacyStatus.

## Response contract

### Business outcome response

All business outcomes use this same business response structure: successful retrieval with accounts, successful retrieval with zero accounts, customer-not-found, and retrieval failure outcomes.

```json
{
	"legacyStatus": {
		"success": "Y",
		"failCode": "0",
		"customerFound": "Y"
	},
	"customerNumber": "0000001234",
	"numberOfAccounts": 2,
	"accounts": [
		{
			"eyecatcher": "ACCT",
			"customerNumber": "0000001234",
			"sortCode": "987654",
			"accountNumber": "12345678",
			"accountType": "CURRENT",
			"interestRate": 1.25,
			"openedDate": "2020-01-15",
			"overdraftLimit": 1000,
			"lastStatementDate": "2026-06-01",
			"nextStatementDate": "2026-07-01",
			"availableBalance": 3500.25,
			"actualBalance": 3400.00
		}
	]
}
```

### Supported account fields

- eyecatcher
- customerNumber
- sortCode
- accountNumber
- accountType
- interestRate
- openedDate
- overdraftLimit
- lastStatementDate
- nextStatementDate
- availableBalance
- actualBalance

### Date representation

- External API date representation for account dates in this specification is ISO `yyyy-MM-dd`.
- This external representation is distinct from legacy internal output semantics, where date values are handled in DDMMYYYY meaning.

### Error responses

- Business outcomes (including customer-not-found and zero-account success) return business outcome responses with `legacyStatus`.
- Validation failures return HTTP `400` and use the validation error response schema.
- Infrastructure failures return HTTP `500` and use the infrastructure error response schema.
- All error responses omit `legacyStatus`, `customerNumber`, `numberOfAccounts`, and `accounts`.

### Validation error response schema (HTTP 400)

```json
{
	"error": {
		"type": "VALIDATION_ERROR",
		"message": "Validation failed",
		"details": [
			{
				"field": "customerNumber",
				"reason": "must be exactly 10 digits"
			}
		]
	}
}
```

### Infrastructure error response schema (HTTP 500)

```json
{
	"error": {
		"type": "INFRASTRUCTURE_ERROR",
		"message": "Service unavailable due to infrastructure failure"
	}
}
```

## HTTP semantics

- HTTP 200 is returned for all business outcomes: successful retrieval with accounts, successful retrieval with zero accounts, customer-not-found, and retrieval failure outcomes represented through `legacyStatus`.
- HTTP 400 is returned for request validation failures and uses the validation error response schema.
- HTTP 500 is returned for infrastructure failures and uses the infrastructure error response schema.

## Infrastructure error contract

Infrastructure failures are represented by a separate error response schema, not by the business outcome response.

Infrastructure error responses omit `legacyStatus`, `customerNumber`, `numberOfAccounts`, and `accounts`.

## Frontend Observable Behavior

1. Inquiry submission behavior
- The frontend provides an inquiry submission interaction using customer number.
- Inquiry submission is read-only in business terms.

2. Frontend validation behavior
- Invalid inquiry input is presented to the user as validation feedback and is distinct from customer-not-found outcomes.
- Values that are syntactically valid customer numbers, including reserved values, are submitted and processed as business outcomes.

3. Loading and response transition behavior
- The frontend provides an observable in-progress state while waiting for inquiry completion.
- After completion, the in-progress state is replaced by the final outcome presentation.

4. Customer found with accounts presentation
- When inquiry outcome indicates customer found with accounts, the frontend presents returned account information.

5. Customer found with zero accounts presentation
- When inquiry outcome indicates customer found with zero accounts, the frontend presents a successful no-accounts result state.

6. Customer-not-found presentation
- When inquiry outcome indicates customer not found, the frontend presents a distinct not-found result state.

7. Retrieval failure presentation
- When inquiry outcome indicates retrieval-stage failure through preserved legacy status semantics, the frontend presents a failure outcome distinct from validation and infrastructure failures.

8. Infrastructure-error presentation
- When a non-business infrastructure failure is returned, the frontend presents a distinct system-error outcome separate from business outcome states.

9. Subsequent inquiry behavior
- After any prior outcome, users can update inquiry input and submit another inquiry through the same interaction flow.
- Each inquiry is evaluated independently and the displayed result reflects the latest completed inquiry outcome.

10. Externally visible data-format behavior
- Externally visible customer and account identifiers preserve leading zeroes in user input and displayed output.
- Account date values displayed from inquiry responses preserve the externally defined date representation.

## Example request

```http
GET /api/v1/customers/0000001234/accounts
```

## Example responses

### Example A: Customer found with accounts

```json
{
	"legacyStatus": {
		"success": "Y",
		"failCode": "0",
		"customerFound": "Y"
	},
	"customerNumber": "0000001234",
	"numberOfAccounts": 1,
	"accounts": [
		{
			"eyecatcher": "ACCT",
			"customerNumber": "0000001234",
			"sortCode": "987654",
			"accountNumber": "12345678",
			"accountType": "CURRENT",
			"interestRate": 1.25,
			"openedDate": "2020-01-15",
			"overdraftLimit": 1000,
			"lastStatementDate": "2026-06-01",
			"nextStatementDate": "2026-07-01",
			"availableBalance": 3500.25,
			"actualBalance": 3400.00
		}
	]
}
```

### Example B: Customer found with zero accounts

```json
{
	"legacyStatus": {
		"success": "Y",
		"failCode": "0",
		"customerFound": "Y"
	},
	"customerNumber": "0000005678",
	"numberOfAccounts": 0,
	"accounts": []
}
```

### Example C: Customer not found

```json
{
	"legacyStatus": {
		"success": "N",
		"failCode": "1",
		"customerFound": "N"
	},
	"customerNumber": "9999999999",
	"numberOfAccounts": 0,
	"accounts": []
}
```

### Example D: Invalid customer number (validation failure)

```json
{
	"error": {
		"type": "VALIDATION_ERROR",
		"message": "Validation failed",
		"details": [
			{
				"field": "customerNumber",
				"reason": "must be exactly 10 digits"
			}
		]
	}
}
```

### Example E: Infrastructure failure

```json
{
	"error": {
		"type": "INFRASTRUCTURE_ERROR",
		"message": "Service unavailable due to infrastructure failure"
	}
}
```

## Acceptance criteria

| ID | Scenario | Expected behavior |
|---|---|---|
| AC-001 | Valid customer with accounts | HTTP 200, `legacyStatus.success = "Y"`, accounts returned (1..20). |
| AC-002 | Customer number `0000000000` | HTTP 200, `legacyStatus.success = "N"`, `legacyStatus.failCode = "1"`, `legacyStatus.customerFound = "N"`, zero accounts. |
| AC-003 | Customer number `9999999999` | HTTP 200, `legacyStatus.success = "N"`, `legacyStatus.failCode = "1"`, `legacyStatus.customerFound = "N"`, zero accounts. |
| AC-004 | Customer validation fails | HTTP 200, `legacyStatus.success = "N"`, `legacyStatus.failCode = "1"`, `legacyStatus.customerFound = "N"`, zero accounts. |
| AC-005 | Retrieval open-stage failure | HTTP 200, `legacyStatus.success = "N"`, `legacyStatus.failCode = "2"`, zero accounts. |
| AC-006 | Retrieval fetch-stage failure | HTTP 200, `legacyStatus.success = "N"`, `legacyStatus.failCode = "3"`, zero accounts. |
| AC-007 | Retrieval close-stage failure | HTTP 200, `legacyStatus.success = "N"`, `legacyStatus.failCode = "4"`, `legacyStatus.customerFound = "Y"`, `customerNumber` present, zero accounts. |
| AC-008 | No accounts found for valid customer | HTTP 200, `legacyStatus.success = "Y"`, `legacyStatus.customerFound = "Y"`, zero accounts. |
| AC-009 | More than 20 available account rows | HTTP 200, response contains no more than 20 account records. |
| AC-010 | Account date fields | External response dates are ISO `yyyy-MM-dd`, preserving legacy date meaning. |
| AC-011 | Invalid customer number (missing, blank, non-digit, or length not equal to 10) | HTTP 400 using validation error response schema; request is not treated as customer-not-found. |
| AC-012 | Account ordering | Response does not guarantee deterministic account ordering. |
| AC-013 | Leading-zero handling | Leading zeroes are preserved in customer and account identifiers throughout request processing and response values. |
