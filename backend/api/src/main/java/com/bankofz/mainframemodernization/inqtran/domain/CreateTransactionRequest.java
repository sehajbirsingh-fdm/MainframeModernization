package com.bankofz.mainframemodernization.inqtran.domain;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotBlank
        @Pattern(regexp = "^(DBT|CRD)$", message = "type must be DBT or CRD")
        String type,

        @NotBlank
        @Size(max = 40, message = "description must not exceed 40 characters")
        String description,

        @NotNull
        @Digits(integer = 10, fraction = 2)
        @DecimalMin(value = "-9999999999.99")
        @DecimalMax(value = "9999999999.99")
        BigDecimal amount
) {
}
