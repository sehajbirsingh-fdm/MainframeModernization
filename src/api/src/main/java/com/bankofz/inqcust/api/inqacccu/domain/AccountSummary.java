package com.bankofz.inqcust.api.inqacccu.domain;

import java.math.BigDecimal;

public record AccountSummary(
        String eyecatcher,
        String customerNumber,
        String sortCode,
        String accountNumber,
        String accountType,
        BigDecimal interestRate,
        String openedDate,
        Integer overdraftLimit,
        String lastStatementDate,
        String nextStatementDate,
        BigDecimal availableBalance,
        BigDecimal actualBalance
) {
}
