package com.bankofz.mainframemodernization.inqstmt.domain;

import java.math.BigDecimal;

public record StatementEntry(
        String date,
        String time,
        String reference,
        String type,
        String description,
        BigDecimal amount
) {
}
