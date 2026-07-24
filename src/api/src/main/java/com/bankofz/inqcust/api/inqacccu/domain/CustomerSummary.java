package com.bankofz.inqcust.api.inqacccu.domain;

public record CustomerSummary(
        String customerNumber,
        String customerName,
        String sortCode,
        String customerType
) {
}
