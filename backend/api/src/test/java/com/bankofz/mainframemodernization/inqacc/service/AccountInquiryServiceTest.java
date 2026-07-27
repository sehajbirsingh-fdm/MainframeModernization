package com.bankofz.mainframemodernization.inqacc.service;

import com.bankofz.mainframemodernization.inqacc.domain.AccountRecord;
import com.bankofz.mainframemodernization.inqacc.domain.AccountResponse;
import com.bankofz.mainframemodernization.inqacc.exception.AccountNotFoundException;
import com.bankofz.mainframemodernization.inqacc.mapper.AccountResponseMapper;
import com.bankofz.mainframemodernization.inqacc.repository.AccountRepository;
import com.bankofz.mainframemodernization.inqacc.validation.AccountInquiryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountInquiryServiceTest {

    private AccountRepository accountRepository;
    private AccountResponseMapper accountResponseMapper;
    private AccountInquiryValidator accountInquiryValidator;
    private AccountInquiryService accountInquiryService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountResponseMapper = mock(AccountResponseMapper.class);
        accountInquiryValidator = mock(AccountInquiryValidator.class);
        accountInquiryService = new AccountInquiryService(accountRepository, accountResponseMapper, accountInquiryValidator);
    }

    @Test
    void shouldReturnMappedResponseForStandardLookup() {
        AccountRecord record = sampleRecord("12345678", "543210");
        AccountResponse response = sampleResponse("12345678", "543210");
        when(accountRepository.findBySortcodeAndAccountNumber("543210", "12345678")).thenReturn(Optional.of(record));
        when(accountResponseMapper.map(record)).thenReturn(response);

        AccountResponse result = accountInquiryService.inquireAccount("543210", "12345678");

        assertThat(result).isEqualTo(response);
        verify(accountRepository).findBySortcodeAndAccountNumber("543210", "12345678");
        verify(accountRepository, never()).findHighestAccountNumberBySortcode(anyString());
    }

    @Test
    void shouldUseHighestLookupForReservedAccountNumber() {
        AccountRecord record = sampleRecord("99999998", "543210");
        AccountResponse response = sampleResponse("99999998", "543210");
        when(accountRepository.findHighestAccountNumberBySortcode("543210")).thenReturn(Optional.of(record));
        when(accountResponseMapper.map(record)).thenReturn(response);

        AccountResponse result = accountInquiryService.inquireAccount("543210", "99999999");

        assertThat(result).isEqualTo(response);
        verify(accountRepository).findHighestAccountNumberBySortcode("543210");
        verify(accountRepository, never()).findBySortcodeAndAccountNumber(anyString(), anyString());
    }

    @Test
    void shouldThrowNotFoundWhenRepositoryHasNoRecord() {
        when(accountRepository.findBySortcodeAndAccountNumber("543210", "12345678")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountInquiryService.inquireAccount("543210", "12345678"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account record not found");
    }

    private AccountRecord sampleRecord(String accountNumber, String sortcode) {
        return new AccountRecord(
                "ACCOUNT",
                "1000000001",
                sortcode,
                accountNumber,
                "CHK",
                new BigDecimal("1.25"),
                20230110,
                1500,
                20240401,
                20240501,
                new BigDecimal("1800.00"),
                new BigDecimal("1700.00")
        );
    }

    private AccountResponse sampleResponse(String accountNumber, String sortcode) {
        return new AccountResponse(
                "ACCOUNT",
                "1000000001",
                sortcode,
                accountNumber,
                "CHK",
                new BigDecimal("1.25"),
                "2023-01-10",
                1500,
                "2024-04-01",
                "2024-05-01",
                new BigDecimal("1800.00"),
                new BigDecimal("1700.00")
        );
    }
}
