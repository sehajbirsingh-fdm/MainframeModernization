package com.bankofz.inqcust.api.inqacccu.domain;

public record AccountRelationshipResponse(
        LegacyStatus legacyStatus,
        CustomerSummary customer,
        AccountsList accounts
) {
}
