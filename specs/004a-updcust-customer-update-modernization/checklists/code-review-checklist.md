# Code Review Checklist: 004a UPDCUST

- [ ] Service logic preserves exact legacy rule order.
- [ ] Fail-code mapping is complete and correct.
- [ ] No field injection; constructor injection only.
- [ ] No business logic in controller.
- [ ] Copybook field sizes and trims enforced.
- [ ] Date transformations are consistent and tested.
- [ ] Regression risk to inquiry/create features evaluated.
- [ ] Security matcher and authorization rules cover PUT /api/v1/customers/{customerNumber}.
- [ ] Runtime OpenAPI includes UPDCUST PUT endpoint and response contracts.
- [ ] Controller-level 401/403 tests present for update endpoint.
