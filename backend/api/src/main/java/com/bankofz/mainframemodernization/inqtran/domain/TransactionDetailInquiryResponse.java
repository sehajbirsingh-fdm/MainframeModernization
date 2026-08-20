package com.bankofz.mainframemodernization.inqtran.domain;

public record TransactionDetailInquiryResponse(
        boolean found,
        TransactionRecord transaction
) {
}
