package com.bankofz.mainframemodernization.inqcust.domain;

public record FieldErrorResponse(
        String field,
        String message
) {
}
