package com.bankofz.mainframemodernization.inqacccu.domain;

import java.util.List;

public record ApiError(
        ErrorPayload error
) {

    public record ErrorPayload(
            String type,
            String message,
            List<ValidationError> details
    ) {
    }
}
