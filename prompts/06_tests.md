# Prompt 06 - Tests

Generate tests from `docs/test-spec.md`.

Create:
- CustomerInquiryServiceTest
- RiskAssessmentServiceTest
- LegacyDateConverterTest
- LookupModeResolverTest
- CustomerInquiryControllerTest

Every test case in `docs/test-spec.md` must be implemented.

Testing rules:
- Use JUnit 5.
- Use Mockito where helpful.
- Use MockMvc for controller tests.
- Do not rely on real randomness.
- Random customer behavior must be deterministic in tests.
- Assert exact legacyStatus values.
- Assert JSON paths in controller tests.
