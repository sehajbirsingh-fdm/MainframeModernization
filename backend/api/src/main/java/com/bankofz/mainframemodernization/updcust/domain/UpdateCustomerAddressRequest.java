package com.bankofz.mainframemodernization.updcust.domain;

import jakarta.validation.constraints.Size;

public record UpdateCustomerAddressRequest(
        @Size(max = 50) String addressLine1,
        @Size(max = 50) String addressLine2,
        @Size(max = 50) String city,
        @Size(max = 10) String postalCode,
        @Size(max = 50) String country
) {
}
