# GitHub Copilot Instructions - INQCUST Modernization

You are building a Spring Boot modernization of a legacy CICS COBOL program called INQCUST.

Follow these rules strictly:

1. Treat `/docs/spec.md` as the source of truth.
2. Do not invent additional fields that are not in the copybooks or spec.
3. Use the exact field mapping in `/docs/mapping-matrix.md`.
4. Implement all acceptance criteria in `/docs/test-spec.md`.
5. Use Java 21 and Spring Boot 3.
6. Use constructor injection only. Do not use field injection.
7. Keep controller thin. Business behavior belongs in service classes.
8. Mock mainframe data using `/mock-data/customer-records.json`.
9. Do not connect to a real database, mainframe, CICS, IMS, or DB2 for the POC.
10. Create interfaces for future integration adapters.
11. Use `CustomerRepository` as the abstraction over the legacy CUSTOMER table.
12. Preserve legacy status behavior in the response using `LegacyInquiryStatus`.
13. Dates from legacy records are numeric `YYYYMMDD` values and must become ISO `yyyy-MM-dd` in JSON.
14. Trim trailing spaces from fixed-width CHAR fields.
15. Add unit tests for every business rule.
16. Add controller tests for every API response status.
17. Do not create frontend code.
18. Do not add Spring Security unless explicitly requested.
19. Do not use Lombok unless the prompt specifically asks for it; plain Java records/classes are preferred for clarity.
20. Ensure the code compiles and tests pass.
