package com.bankofz.inqcust.api.crecust.domain;

public record CreateCustomerErrorResponse(
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
