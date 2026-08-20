package com.bankofz.mainframemodernization.inqstmt.domain;

import java.util.List;

public record AccountStatementResponse(
        String sortCode,
        String accountNumber,
        String period,
        StatementSummary summary,
        List<StatementEntry> entries
) {
}
