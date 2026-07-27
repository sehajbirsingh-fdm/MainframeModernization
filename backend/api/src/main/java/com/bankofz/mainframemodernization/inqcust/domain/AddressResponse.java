package com.bankofz.mainframemodernization.inqcust.domain;

public record AddressResponse(
        String line1,
        String line2,
        String city,
        String postcode,
        String country
) {
}
