# requirements.md - INQACCCU Modernization Requirements

## Purpose
Modernize INQACCCU customer-account relationship inquiry into a Spring Boot REST API.

## Source assets
- INQACCCU.cbl
- INQACCCUZ.cpy
- ACCOUNT.cpy
- ACCDB2.cpy
- INQCUST dependency and INQCUSTZ commarea

## Functional requirements
| ID | Requirement |
|---|---|
| FR-001 | Retrieve accounts by customer number. |
| FR-002 | Validate customer by linking to INQCUST-equivalent behavior before account retrieval. |
| FR-003 | Reject zero customer number as customer not found. |
| FR-004 | Reject customer number `9999999999` as customer not found. |
| FR-005 | Return up to 20 accounts. |
| FR-006 | Preserve success, failCode, customerFound, and numberOfAccounts. |
| FR-007 | Map account fields from DB2 ACCOUNT to API account DTO. |
| FR-008 | Convert account dates into ISO dates. |
| FR-009 | Return zero accounts when DB2 fetch reaches SQLCODE +100 with no error. |
| FR-010 | Convert DB2 open/fetch/close failures into structured API errors with legacy failure codes 2, 3, and 4 respectively. |
| FR-011 | Use mock repository for POC and adapter interface for future DB2/CICS/zOS Connect/MQ integration. |
| FR-012 | Provide optional portfolio summary from returned accounts. |
