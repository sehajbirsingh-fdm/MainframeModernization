package com.bankofz.mainframemodernization.inqstmt.domain;

public record ErrorResponse(
        String code,
        String message,
        String correlationId
) {
}
