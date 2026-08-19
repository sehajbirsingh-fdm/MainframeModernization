package com.bankofz.mainframemodernization.inqstmt.domain;

import java.math.BigDecimal;

public record StatementSummary(
        String periodFrom,
        String periodTo,
        BigDecimal openingBalance,
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        BigDecimal closingBalance,
        int transactionCount
) {
}
