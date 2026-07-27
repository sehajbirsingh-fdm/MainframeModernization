package com.bankofz.mainframemodernization.inqacc.domain;

public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(
            String code,
            String message,
            String timestamp,
            String correlationId,
            String details
    ) {
    }
}
