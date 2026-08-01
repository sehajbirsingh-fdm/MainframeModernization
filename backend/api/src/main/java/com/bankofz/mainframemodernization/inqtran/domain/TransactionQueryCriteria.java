package com.bankofz.mainframemodernization.inqtran.domain;

public record TransactionQueryCriteria(
        String sortCode,
        String accountNumber,
        String lowerDateBound,
        String upperDateBound,
        int limit,
        int offset
) {
}
