package com.bankofz.mainframemodernization.inqacccu.domain;

import java.util.List;

public record AccountRelationshipResponse(
        LegacyStatus legacyStatus,
        String customerNumber,
        int numberOfAccounts,
        List<AccountSummary> accounts
) {
}
