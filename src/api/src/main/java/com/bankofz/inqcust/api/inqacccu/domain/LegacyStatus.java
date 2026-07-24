package com.bankofz.inqcust.api.inqacccu.domain;

public record LegacyStatus(
        String success,
        String failCode,
        String customerFound
) {
}
