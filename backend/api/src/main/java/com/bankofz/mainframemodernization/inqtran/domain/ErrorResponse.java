package com.bankofz.mainframemodernization.inqtran.domain;

public record ErrorResponse(
        String code,
        String message,
        String correlationId
) {
}
