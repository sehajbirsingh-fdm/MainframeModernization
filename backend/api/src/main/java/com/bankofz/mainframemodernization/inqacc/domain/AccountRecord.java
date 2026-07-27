package com.bankofz.mainframemodernization.inqacc.domain;

import java.math.BigDecimal;

public record AccountRecord(
        String eyecatcher,
        String customerNumber,
        String sortcode,
        String accountNumber,
        String accountType,
        BigDecimal interestRate,
        Integer accountOpened,
        Integer overdraftLimit,
        Integer lastStatementDate,
        Integer nextStatementDate,
        BigDecimal availableBalance,
        BigDecimal actualBalance
) {
}
