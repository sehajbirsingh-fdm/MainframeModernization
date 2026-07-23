package com.bankofz.inqcust.api.inqacccu.domain;

import java.math.BigDecimal;

public record AccountSummary(
        String accountNumber,
        String sortCode,
        String accountType,
        String accountTypeDescription,
        BigDecimal availableBalance,
        String availableBalanceCurrency,
        BigDecimal actualBalance,
        String actualBalanceCurrency,
        BigDecimal interestRate,
        Integer overdraftLimit,
        String lastStatementDate,
        String nextStatementDate
) {
}
