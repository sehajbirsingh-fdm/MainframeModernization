package com.bankofz.inqcust.api.inqacccu.domain;

import java.util.List;

public record AccountsList(
        int count,
        List<AccountSummary> accounts
) {
}
