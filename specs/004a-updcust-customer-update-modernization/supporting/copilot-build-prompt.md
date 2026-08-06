# Copilot Build Prompt: 004a UPDCUST

Implement feature specs/004a-updcust-customer-update-modernization exactly.

Hard requirements:
- Source of truth: UPDCUST.cbl and listed copybooks.
- Do not invent fields not present in copybooks/spec.
- Preserve fail codes: T, 1, 2, 3, 4.
- Preserve selective update gates for name/title, address, phone, status, DOB.
- Keep controller thin, business logic in service.
- Use repository abstractions only.
- Return legacy status in response envelope.
- Add unit/service/controller/integration tests for all required paths.
- Do not change other feature behavior.

Frontend integration requirement:
- Place Update Customer action in inquiry success flow.
- Prefill edit form from selected customer.
