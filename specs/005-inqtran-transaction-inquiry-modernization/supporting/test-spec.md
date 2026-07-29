# Test Specification

| Test ID | Scenario | Expected result | Trace |
|---|---|---|---|
| TC-001 | Exact sort/account with matching and nonmatching rows | Only exact account rows | BR-001, FR-001, AC-001 |
| TC-002 | Row exactly on fromDate | Included | BR-002, AC-002 |
| TC-003 | Row exactly on toDate | Included | BR-002, AC-002 |
| TC-004 | Omit fromDate | No lower-bound exclusion | BR-003, AC-002 |
| TC-005 | Omit toDate | No upper-bound exclusion | BR-004, AC-002 |
| TC-006 | Omit limit | Effective limit 50 | BR-005, AC-003 |
| TC-007 | limit=0 | Effective limit 50 | BR-005, AC-003 |
| TC-008 | limit=100 | At most 100 rows | BR-006, AC-003 |
| TC-009 | limit=101 | Effective limit 100 | BR-006, AC-003 |
| TC-010 | offset=0 | First ordered row returned | BR-007, AC-004 |
| TC-011 | offset within results | Correct preceding rows skipped | BR-007, AC-004 |
| TC-012 | offset >= total | 200, total preserved, returned 0 | BR-007, BR-010 |
| TC-013 | Mixed dates/times | Descending date then time | BR-008, AC-005 |
| TC-014 | More matches than limit | totalCount full; returnedCount page size | BR-009, AC-006 |
| TC-015 | No match | 200, 0/0, empty array | BR-010, AC-007 |
| TC-016 | Composite key mapping | Exact hyphenated 5-part ID | BR-011, AC-008 |
| TC-017 | DB2 DATE mapping | API date YYYYMMDD | BR-012, AC-009 |
| TC-018 | Field mapping including negative/decimal amount | Exact mapped values and decimal precision | BR-013, AC-009 |
| TC-019 | Inquiry execution | No write repository method/SQL | BR-014, AC-010 |
| TC-020 | Count repository failure | 500, no page | BR-015, AC-011 |
| TC-021 | Page repository failure | 500, no partial page | BR-015, AC-011 |
| TC-022 | Invalid sort code width/characters | 400 | SR-002, spec validation |
| TC-023 | Invalid account width/characters | 400 | SR-002, spec validation |
| TC-024 | Negative limit/offset | 400 | SR-002, spec validation |
| TC-025 | Frontend successful query | Metadata and rows render | FR-012, AC-012 |
| TC-026 | Frontend empty query | Empty state renders, not error | FR-012, AC-012 |
| TC-027 | Frontend technical failure | Safe error state renders | FR-011, FR-012 |
| TC-028 | OpenAPI conformance | Runtime path/status/schema match authored contract | OR-005 |

## Boundary and Regression Coverage
- Leading-zero sort codes/account numbers/references.
- 50, 100, and >100 datasets.
- Offset 99999.
- Date ties and time ties without assuming unevidenced tertiary order.
- Existing INQCUST, INQACC, INQACCCU, and CRECUST tests remain green.
- Security regression according to approved route policy.

## Test Data Rule
Use deterministic test fixtures in test scope. Do not introduce mock JSON. Any local/demo H2 seed data must be separately approved and must not be inferred from examples in this package.
