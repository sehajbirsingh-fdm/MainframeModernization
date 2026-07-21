# architecture.md

## Legacy
```text
DFHCOMMAREA(INQACCCU)
  -> INQACCCU.cbl
    -> CUSTOMER-CHECK
      -> EXEC CICS LINK PROGRAM('INQCUST')
    -> DB2 ACCOUNT cursor by customer number and sort code
    -> ACCOUNT-DETAILS OCCURS 1 TO 20
```

## Modern
```text
GET /api/v1/customers/{customerNumber}/accounts
  -> CustomerAccountsController
  -> CustomerAccountsService
     -> CustomerValidationClient
     -> AccountRelationshipRepository
     -> AccountMapper
     -> PortfolioSummaryService
```
