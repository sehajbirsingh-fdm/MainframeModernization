package com.bankofz.mainframemodernization.inqtran.repository.model;

import java.math.BigDecimal;

public record TransactionRow(
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
