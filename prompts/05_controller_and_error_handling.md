# Prompt 05 - Controller and error handling

Implement controller and exception handling using `docs/api-contract.md` and `openapi/openapi.yaml`.

Create:
- CustomerInquiryController
- CustomerNotFoundException
- CustomerInquiryException
- GlobalExceptionHandler

Endpoint:
```http
GET /api/v1/customers/{sortCode}/{customerNumber}
```

Validation:
- sortCode must match `^[0-9]{6}$`
- customerNumber must match `^[0-9]{10}$`

Responses:
- 200 found
- 400 validation error
- 404 not found / special lookup failed
- 500 system error

Add clear logging for:
- sortCode
- customerNumber
- lookupMode
- outcome

Do not put business logic in the controller.
