package com.bankofz.inqcust.api.inqacccu.repository.model;

import java.math.BigDecimal;

public record AccountProjection(
        String accountNumber,
        String sortCode,
        String accountType,
        String accountTypeDescription,
        BigDecimal availableBalance,
        BigDecimal actualBalance,
        BigDecimal interestRate,
        Integer overdraftLimit,
        Integer lastStatementDate,
        Integer nextStatementDate
) {
}
