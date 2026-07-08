# Prompt 02 - Domain model and DTOs

Using `docs/domain-model.md`, `docs/data-dictionary.md`, and `docs/mapping-matrix.md`, implement all model, DTO, and enum classes.

Create:
- CustomerRecord
- AddressResponse
- CustomerResponse
- LegacyInquiryStatus
- RiskAssessmentResponse
- CustomerInquiryResponse
- ErrorResponse
- FieldErrorResponse
- CustomerStatus enum
- LookupMode enum
- RiskRating enum

Rules:
- Keep fields exactly aligned with the mapping matrix.
- Use LocalDate in API response where required.
- Use Integer for legacy numeric dates in CustomerRecord.
- Do not add extra fields.
