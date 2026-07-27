package com.bankofz.mainframemodernization.inqacccu.domain;

public record CustomerSummary(
        String customerNumber,
        String customerName,
        String sortCode,
        String customerType
) {
}
