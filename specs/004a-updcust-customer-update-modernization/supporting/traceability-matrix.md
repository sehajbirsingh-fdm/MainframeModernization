# Traceability Matrix: UPDCUST

| Requirement | Legacy Evidence | Test IDs |
|---|---|---|
| FR-004 title allow-list | UPDCUST title EVALUATE block | UT-001, UT-002 |
| FR-005 meaningful payload gate | blank first/last/address1 check | UT-003 |
| FR-006 name/title gate | IF first-name(1:1) NOT blank | UT-005, IT-004 |
| FR-007 address gate | IF addr-line1(1:1) NOT blank | UT-006, IT-004 |
| FR-008 phone/status gates | IF phone/status(1:1) NOT blank | UT-007, UT-008 |
| FR-008a status parity | no allow-list check in UPDCUST | UT-012 |
| FR-009 DOB gate | IF DOB year NOT 0 then compute | UT-009 |
| FR-010a no-op success parity | gate pass without effective update | ST-006 |
| FR-003 key normalization | UNSTRING + numeric zero-pad behavior | UT-011, IT-006 |
| FR-010 not found fail 1 | SQLCODE = 100 branch | ST-001, CT-004 |
| FR-011 fail 2/3 mappings | SQLCODE non-zero on select/update | ST-002, ST-003 |
| FR-012 success status | MOVE Y to COMM-UPD-SUCCESS | ST-004, CT-001 |
| FR-012a explicit fail-code blanking | modernization deterministic response rule | ST-007, CT-008 |
| FR-012b cs-review-date correction | legacy raw MOVE defect at UPDCUST success mapping | ST-008, IT-009 |
| FR-014 UI placement | modern UX requirement | FT-001, FT-002, FT-003 |
| FR-008b status governance check | CUSTOMER.cpy 88-level status names | IT-010 |
| BR-011 first-character blank semantics | (1:1) blank checks in COBOL | UT-010, IT-005 |

## Validation Completeness
- All observed COBOL fail codes (T,1,2,3,4) mapped to rules and tests.
- All selective update gates mapped to explicit tests.
- Copybook field limits mapped to API contract validation tests.
- Legacy cs-review-date raw MOVE defect is explicitly handled as non-parity carry-forward behavior.
