# QA Review Checklist

- [ ] All AC-001 through AC-012 have automated coverage.
- [ ] All BR-001 through BR-015 map to tests.
- [ ] Inclusive date boundaries are tested.
- [ ] Omitted date boundaries are tested without invalid target dates.
- [ ] Limit omitted, zero, 50, 100, and >100 are tested.
- [ ] Offset 0, middle, exact end, and beyond end are tested.
- [ ] Descending date/time order is tested without asserting a tertiary tie order.
- [ ] Empty result is 200 and not 404.
- [ ] Counts equal repository/page behavior.
- [ ] Composite ID and every field mapping are tested.
- [ ] Leading zeros and decimal precision are preserved.
- [ ] Technical count/page failures return no partial data.
- [ ] OpenAPI/controller/runtime schemas match.
- [ ] Frontend loading, results, empty, and safe error states are covered.
- [ ] Existing feature regression suites pass.
- [ ] Approved security behavior is tested.
- [ ] Demo uses approved deterministic H2/test fixtures, not mock JSON.
