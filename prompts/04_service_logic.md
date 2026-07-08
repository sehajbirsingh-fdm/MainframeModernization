# Prompt 04 - Service logic

Implement service layer using `docs/spec.md`, `docs/test-spec.md`, and `docs/traceability-matrix.md`.

Create:
- LookupModeResolver
- CustomerInquiryService
- RandomCustomerSelector
- RiskAssessmentService
- LegacyStatusFactory
- CustomerMapper
- LegacyDateConverter

Must implement all business rules BR-001 through BR-022 from `docs/spec.md`.

Important:
- customerNumber `0000000000` = RANDOM.
- customerNumber `9999999999` = LATEST.
- otherwise SPECIFIC.
- Random lookup retry limit comes from property `inquiry.random.max-retries`, default 1000.
- Random behavior must be testable and not flaky.
- Convert YYYYMMDD integer dates to LocalDate.
- Trim fixed-width CHAR values.
- Add risk assessment without changing legacy status behavior.
