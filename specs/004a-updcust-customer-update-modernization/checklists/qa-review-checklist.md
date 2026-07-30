# QA Review Checklist: 004a UPDCUST

- [ ] Happy path update returns 200 and updated fields.
- [ ] Invalid title returns legacy fail T.
- [ ] Blank name/address gate returns legacy fail 4.
- [ ] Unknown customer returns legacy fail 1.
- [ ] Simulated read failure returns legacy fail 2.
- [ ] Simulated write failure returns legacy fail 3.
- [ ] Name/title and address gate behaviors match legacy parity.
- [ ] Update action appears in inquiry success flow and prefills edit form.
