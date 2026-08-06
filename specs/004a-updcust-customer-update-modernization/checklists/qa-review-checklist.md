# QA Review Checklist: 004a UPDCUST

- [ ] Happy path update returns 200 and updated fields.
- [ ] Invalid title returns legacy fail T.
- [ ] Blank name/address gate returns legacy fail 4.
- [ ] Unknown customer returns legacy fail 1.
- [ ] Simulated read failure returns legacy fail 2.
- [ ] Simulated write failure returns legacy fail 3.
- [ ] Name/title and address gate behaviors match legacy parity.
- [ ] Update action appears in inquiry success flow and prefills edit form.
- [ ] Unauthenticated update request returns 401.
- [ ] Unauthorized update request returns 403.
- [ ] Runtime OpenAPI documents PUT /api/v1/customers/{customerNumber} and 400/401/403/404/422/500 responses.
