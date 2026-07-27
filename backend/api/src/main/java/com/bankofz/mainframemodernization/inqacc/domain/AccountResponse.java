package com.bankofz.mainframemodernization.inqacc.domain;

import java.math.BigDecimal;

public record AccountResponse(
        String eyecatcher,
        String customerNumber,
        String sortcode,
        String accountNumber,
        String accountType,
        BigDecimal interestRate,
        String accountOpened,
        Integer overdraftLimit,
        String lastStatementDate,
        String nextStatementDate,
        BigDecimal availableBalance,
        BigDecimal actualBalance
) {
}
