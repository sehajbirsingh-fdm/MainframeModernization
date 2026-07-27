package com.bankofz.mainframemodernization.inqcust.domain;

public record LegacyInquiryStatus(
        String inquirySuccess,
        String inquiryFailCode,
        String message
) {
}
