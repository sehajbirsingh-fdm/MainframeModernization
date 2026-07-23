package com.bankofz.inqcust.api.inqacccu.domain;

public record ValidationError(
        String field,
        String message
) {
}
