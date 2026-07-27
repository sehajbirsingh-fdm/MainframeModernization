package com.bankofz.mainframemodernization.crecust.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record DateParts(
        @Min(1) @Max(31) int day,
        @Min(1) @Max(12) int month,
        @Min(1601) @Max(9999) int year
) {
}
