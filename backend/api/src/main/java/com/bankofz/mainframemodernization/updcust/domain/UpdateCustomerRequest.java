package com.bankofz.mainframemodernization.updcust.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @Size(max = 10) String title,
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @Pattern(regexp = "^$|^[0-9]{4}-[0-9]{2}-[0-9]{2}$", message = "dateOfBirth must match yyyy-MM-dd")
        String dateOfBirth,
        @Size(max = 20) String phoneNumber,
        @Valid UpdateCustomerAddressRequest address,
        @Size(max = 10) String customerStatus
) {
}
