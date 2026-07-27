package com.bankofz.mainframemodernization.inqacccu.repository.model;

import java.math.BigDecimal;

public record AccountProjection(
        String accountNumber,
        String sortCode,
        String accountType,
        Integer openedDate,
        BigDecimal availableBalance,
        BigDecimal actualBalance,
        BigDecimal interestRate,
        Integer overdraftLimit,
        Integer lastStatementDate,
        Integer nextStatementDate
) {
}
