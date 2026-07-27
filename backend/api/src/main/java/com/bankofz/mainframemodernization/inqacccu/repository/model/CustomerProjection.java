package com.bankofz.mainframemodernization.inqacccu.repository.model;

public record CustomerProjection(
        String customerNumber,
        String customerName,
        String sortCode,
        String customerType
) {
}
