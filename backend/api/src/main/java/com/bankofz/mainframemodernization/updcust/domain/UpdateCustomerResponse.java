package com.bankofz.mainframemodernization.updcust.domain;

import java.time.LocalDate;

public record UpdateCustomerResponse(
        String customerNumber,
        String sortCode,
        String title,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber,
        UpdateCustomerAddressRequest address,
        String customerStatus,
        LocalDate createdDate,
        Integer creditScore,
        LocalDate creditScoreReviewDate,
        LegacyUpdateStatus legacyStatus
) {
}
