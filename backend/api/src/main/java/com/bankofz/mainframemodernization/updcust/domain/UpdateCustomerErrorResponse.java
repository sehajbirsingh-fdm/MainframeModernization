package com.bankofz.mainframemodernization.updcust.domain;

public record UpdateCustomerErrorResponse(
        ErrorBody error
) {
    public record ErrorBody(
            String code,
            String message,
            String legacyFailCode,
            String correlationId,
            String timestamp
    ) {
    }
}
