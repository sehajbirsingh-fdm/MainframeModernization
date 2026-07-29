# Traceability Matrix

| Source evidence | Business rule | Requirement | Acceptance criterion | Test | API contract |
|---|---|---|---|---|---|
| SQL exact sort/account predicates | BR-001 | FR-001 | AC-001 | TC-001 | path sortCode/accountNumber |
| SQL >= and <= dates | BR-002 | FR-002 | AC-002 | TC-002/003 | fromDate/toDate |
| no-from sentinel 0 | BR-003 | FR-002 | AC-002 | TC-004 | optional fromDate |
| no-to sentinel 99999999 | BR-004 | FR-002 | AC-002 | TC-005 | optional toDate |
| limit 0→50 | BR-005 | FR-003 | AC-003 | TC-006/007 | limit default/description |
| limit >100→100; OCCURS 100 | BR-006 | FR-003 | AC-003 | TC-008/009 | maxItems 100 |
| COBOL skip loop | BR-007 | FR-004 | AC-004 | TC-010/011/012 | offset |
| ORDER BY date/time DESC | BR-008 | FR-005 | AC-005 | TC-013 | operation description/spec |
| separate COUNT(*) | BR-009 | FR-006 | AC-006 | TC-014 | totalCount/returnedCount |
| SQLCODE +100 normal then success Y | BR-010 | FR-007 | AC-007 | TC-015 | 200 empty page |
| WS-TRAN-ID-PARTS | BR-011 | FR-008 | AC-008 | TC-016 | transactionId |
| ISO→YYYYMMDD conversion | BR-012 | FR-009 | AC-009 | TC-017 | Transaction.date |
| direct field MOVEs | BR-013 | FR-009 | AC-009 | TC-018 | Transaction schema |
| FOR FETCH ONLY, no write SQL | BR-014 | FR-010 | AC-010 | TC-019 | GET operation |
| ABNDPROC + CICS ABEND on SQL errors | BR-015 | FR-011 | AC-011 | TC-020/021 | 500 response |
| repository React conventions | n/a | FR-012 | AC-012 | TC-025/026/027 | same GET operation |
