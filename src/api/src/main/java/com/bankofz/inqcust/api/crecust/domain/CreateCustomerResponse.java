package com.bankofz.inqcust.api.crecust.domain;

import java.time.LocalDate;

public record CreateCustomerResponse(
        String eyecatcher,
        String sortCode,
        String customerNumber,
        String title,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String postcode,
        String country,
        String status,
        LocalDate createdDate,
        Integer creditScore,
        LocalDate creditScoreReviewDate,
        LegacyCreateStatus legacyStatus
) {
}
