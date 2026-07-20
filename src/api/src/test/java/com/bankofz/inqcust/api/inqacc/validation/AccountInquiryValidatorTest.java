package com.bankofz.inqcust.api.inqacc.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountInquiryValidatorTest {

    private final AccountInquiryValidator validator = new AccountInquiryValidator();

    @Test
    void shouldAcceptValidValues() {
        assertThatCode(() -> validator.validateSortcode("543210")).doesNotThrowAnyException();
        assertThatCode(() -> validator.validateAccountNumber("12345678")).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidSortcode() {
        assertThatThrownBy(() -> validator.validateSortcode("ABCDE1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sortcode");
    }

    @Test
    void shouldRejectInvalidAccountNumber() {
        assertThatThrownBy(() -> validator.validateAccountNumber("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number");
    }
}
