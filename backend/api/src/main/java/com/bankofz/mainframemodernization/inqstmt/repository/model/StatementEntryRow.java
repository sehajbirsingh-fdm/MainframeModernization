package com.bankofz.mainframemodernization.inqstmt.repository.model;

import java.math.BigDecimal;

public record StatementEntryRow(
        String date,
        String time,
        String reference,
        String type,
        String description,
        BigDecimal amount
) {
}
