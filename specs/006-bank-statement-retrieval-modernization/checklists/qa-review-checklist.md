# QA Review Checklist - 006 Bank Statement Retrieval

- [ ] Valid account and period returns statement summary and entries.
- [ ] Valid account with no period transactions returns empty statement entries.
- [ ] Invalid period format returns 400.
- [ ] Unauthenticated request returns 401.
- [ ] Unauthorized request returns 403.
- [ ] Missing account returns 404 contract outcome.
- [ ] Technical failures return 500 contract outcome.
