# Requirements Quality Checklist

- [ ] Every functional requirement maps to at least one evidenced business rule.
- [ ] No account-not-found or sentinel account behavior has been invented.
- [ ] Date omission, inclusion, ordering, offset, default limit, and maximum limit are explicit.
- [ ] Empty results are specified as success.
- [ ] Total count and returned count semantics are distinct.
- [ ] Response fields match the copybook/mapping matrix only.
- [ ] INQTRAND is explicitly out of scope as a separate future feature.
- [ ] Modernization validation and error-envelope decisions are labeled as such.
- [ ] Security, nullability, OpenAPI authority, and H2 data policy are unresolved until approved.
- [ ] Non-functional, operational, and test requirements fit existing repository conventions.
