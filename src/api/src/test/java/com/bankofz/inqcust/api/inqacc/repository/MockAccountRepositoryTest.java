package com.bankofz.inqcust.api.inqacc.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockAccountRepositoryTest {

    @Test
    void shouldFindAccountByCompositeKey() {
        MockAccountRepository repository = new MockAccountRepository("mock-data/account-records.json", new ObjectMapper());

        assertThat(repository.findBySortcodeAndAccountNumber("123456", "00000099")).isPresent();
        assertThat(repository.findBySortcodeAndAccountNumber("123456", "00000000")).isEmpty();
    }

    @Test
    void shouldReturnHighestAccountNumberBySortcode() {
        MockAccountRepository repository = new MockAccountRepository("mock-data/account-records.json", new ObjectMapper());

        assertThat(repository.findHighestAccountNumberBySortcode("123456"))
                .isPresent()
                .get()
                .extracting(account -> account.accountNumber())
            .isEqualTo("00000099");
    }
}
