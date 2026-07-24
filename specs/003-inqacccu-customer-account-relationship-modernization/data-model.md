# data-model.md - INQACCCU Data Model

```java
record CustomerAccountsResponse(
    LegacyStatus legacyStatus,
    String customerNumber,
    int numberOfAccounts,
    List<AccountSummary> accounts,
    PortfolioSummary portfolioSummary
) {}

record LegacyStatus(
    String success,
    String failureCode,
    String customerFound,
    String message
) {}

record AccountSummary(
    String eyecatcher,
    String customerNumber,
    String sortCode,
    String accountNumber,
    String accountType,
    BigDecimal interestRate,
    LocalDate openedDate,
    Integer overdraftLimit,
    LocalDate lastStatementDate,
    LocalDate nextStatementDate,
    BigDecimal availableBalance,
    BigDecimal actualBalance
) {}

record PortfolioSummary(
    int accountCount,
    BigDecimal totalAvailableBalance,
    BigDecimal totalActualBalance,
    BigDecimal totalOverdraftLimit
) {}
```
