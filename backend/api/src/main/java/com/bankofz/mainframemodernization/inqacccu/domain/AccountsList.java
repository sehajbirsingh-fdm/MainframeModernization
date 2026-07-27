package com.bankofz.mainframemodernization.inqacccu.domain;

import java.util.List;

public record AccountsList(
        int count,
        List<AccountSummary> accounts
) {
}
