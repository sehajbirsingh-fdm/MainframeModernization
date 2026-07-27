package com.bankofz.mainframemodernization.inqcust.domain;

import java.time.LocalDate;

public record CustomerResponse(
        String eyecatcher,
        String sortCode,
        String customerNumber,
        String title,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        AddressResponse address,
        CustomerStatus status,
        LocalDate createdDate,
        Integer creditScore,
        LocalDate creditScoreReviewDate
) {
}
