package com.bankofz.mainframemodernization.crecust.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerAddressRequest(
        @NotBlank @Size(max = 50) String line1,
        @Size(max = 50) String line2,
        @NotBlank @Size(max = 50) String city,
        @NotBlank @Size(max = 10) String postcode,
        @NotBlank @Size(max = 50) String country
) {
}
