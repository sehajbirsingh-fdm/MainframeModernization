# Code Review Checklist

- [ ] Existing application extended; no second Spring Boot or React project.
- [ ] Controller is thin and constructor-injected.
- [ ] Service owns default/clamp/orchestration behavior.
- [ ] Repository interface isolates JDBC and future adapters.
- [ ] SQL is parameterized and read-only.
- [ ] Exact filters, inclusive bounds, order, count, and pagination match spec.
- [ ] Identifiers are strings and leading zeros survive.
- [ ] BigDecimal is used for amount.
- [ ] Mapper exposes no invented fields and handles approved null/padding policy.
- [ ] No unevidenced tertiary sorting, account validation, or 404 behavior.
- [ ] Technical failures do not leak SQL/stack details or partial pages.
- [ ] Logging avoids sensitive transaction/account data.
- [ ] Route security follows the explicit approved decision.
- [ ] Feature and runtime OpenAPI are synchronized.
- [ ] Tests are traceable and existing regressions pass.
- [ ] No mock JSON or live mainframe connection was introduced.
- [ ] INQTRAND implementation/detail route was not added to this feature.
