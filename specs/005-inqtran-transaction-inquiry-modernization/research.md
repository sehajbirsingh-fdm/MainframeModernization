# Research and Decisions

## Confirmed Evidence
- `INQTRANL` uses sort code + 8-digit account number, optional date sentinels, limit/offset, total count, and a 100-entry array.
- DB2 query uses inclusive dates and descending date/time ordering.
- Pagination is performed after ordered fetch.
- Empty result is successful.
- `INQTRAND` is not called and is therefore not part of the list implementation.
- Existing repository stack is Java 21, Spring Boot 3.5.3, JDBC/H2, React/TypeScript/Vite, Vitest, and Playwright.

## Adopted Design Decisions
- Feature folder is `005-inqtran-transaction-inquiry-modernization` because the repository uses feature 005 for INQTRAN Transaction Inquiry.
- Preserve the full account key in the route rather than collapsing it into an unevidenced `accountId`.
- Preserve transaction date/time/reference as strings and amount as decimal.
- Model absent dates as optional predicates, not invalid sentinel date objects.
- Scope `INQTRAND` as a separate future feature in the same capability family.

## Decisions Requiring Repository Owner or SME
1. Runtime OpenAPI authority and merge strategy.
2. Security policy.
3. H2 transaction schema and local/demo data policy.
4. Calendar/date-order validation.
5. Null and padding handling.
6. Whether the broad existing transaction contract must be replaced, adapted, or deprecated.
7. Whether explicit `limit=0` should default to 50 in the HTTP API (current proposal preserves legacy behavior).

## Missing Evidence
- Production DDL/indexes and null constraints.
- CICS transaction/program definitions and caller behavior.
- DB2 package/bind details.
- JCL/deployment assets.
- Production examples and volume/performance characteristics.
- Authorization rules.
