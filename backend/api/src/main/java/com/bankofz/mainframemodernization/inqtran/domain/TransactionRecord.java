package com.bankofz.mainframemodernization.inqtran.domain;

import java.math.BigDecimal;

public record TransactionRecord(
        String transactionId,
        String sortCode,
        String accountNumber,
        String date,
        String time,
        String reference,
        String type,
        String description,
        BigDecimal amount
) {
}
