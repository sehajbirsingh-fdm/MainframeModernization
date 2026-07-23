package com.bankofz.inqcust.api.inqacccu.domain;

import java.util.List;

public record ApiError(
        String code,
        String message,
        List<ValidationError> details
) {
}
