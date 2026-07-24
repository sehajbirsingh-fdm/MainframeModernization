package com.bankofz.inqcust.api.crecust.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotNull @Size(max = 10) String title,
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotNull @Valid DateParts dateOfBirth,
        @NotNull @Valid DateParts createdDate,
        @NotNull @Size(max = 20) String phone,
        @NotNull @Valid CreateCustomerAddressRequest address,
        @NotNull @Size(max = 10) String status
) {
}
