package com.bankofz.mainframemodernization.inqtran.domain;

import java.util.List;

public record TransactionInquiryResponse(
        String sortCode,
        String accountNumber,
        String fromDate,
        String toDate,
        int limit,
        int offset,
        int totalCount,
        int returnedCount,
        List<TransactionRecord> transactions
) {
}
