package com.bankofz.mainframemodernization.inqacccu.domain;

public record ValidationError(
        String field,
        String reason
) {
}
