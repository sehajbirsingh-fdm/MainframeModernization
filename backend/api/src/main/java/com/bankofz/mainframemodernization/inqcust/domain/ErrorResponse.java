package com.bankofz.mainframemodernization.inqcust.domain;

import java.util.List;

public record ErrorResponse(
        String errorCode,
        String message,
        List<FieldErrorResponse> fieldErrors
) {
}
