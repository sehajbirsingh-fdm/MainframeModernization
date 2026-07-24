# Copilot Build Prompt Context - CRECUST

Use this feature context when implementing `004-crecust-customer-create-modernization`:

1. Follow `spec.md` as behavior authority.
2. Use only fields from `CRECUST.cpy`, `CUSTOMER.cpy`, `CUSTDB2.cpy`.
3. Keep controller thin; service owns business behavior.
4. Preserve legacy status behavior via `legacyStatus.commSuccess` and `legacyStatus.commFailCode`.
5. Use `CustomerRepository` as persistence abstraction.
6. Use mock data and mock control-state only; no live DB2/CICS.
7. Convert integer dates `YYYYMMDD` to ISO `yyyy-MM-dd` in responses.
8. Add tests for every fail code path defined in spec.
9. Use Java 21 and Spring Boot 3.
10. Constructor injection only.
