package com.bankofz.inqcust.api.inqacc.service;

import com.bankofz.inqcust.api.inqacc.domain.AccountRecord;
import com.bankofz.inqcust.api.inqacc.domain.AccountResponse;
import com.bankofz.inqcust.api.inqacc.exception.AccountNotFoundException;
import com.bankofz.inqcust.api.inqacc.mapper.AccountResponseMapper;
import com.bankofz.inqcust.api.inqacc.repository.AccountRepository;
import com.bankofz.inqcust.api.inqacc.validation.AccountInquiryValidator;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountInquiryService {

    private static final String RESERVED_ACCOUNT_NUMBER = "99999999";

    private final AccountRepository accountRepository;
    private final AccountResponseMapper accountResponseMapper;
    private final AccountInquiryValidator accountInquiryValidator;

    public AccountInquiryService(
            AccountRepository accountRepository,
            AccountResponseMapper accountResponseMapper,
            AccountInquiryValidator accountInquiryValidator
    ) {
        this.accountRepository = accountRepository;
        this.accountResponseMapper = accountResponseMapper;
        this.accountInquiryValidator = accountInquiryValidator;
    }

    public AccountResponse inquireAccount(String sortcode, String accountNumber) {
        accountInquiryValidator.validateSortcode(sortcode);
        accountInquiryValidator.validateAccountNumber(accountNumber);

        Optional<AccountRecord> result;
        if (RESERVED_ACCOUNT_NUMBER.equals(accountNumber)) {
            result = accountRepository.findHighestAccountNumberBySortcode(sortcode);
        } else {
            result = accountRepository.findBySortcodeAndAccountNumber(sortcode, accountNumber);
        }

        AccountRecord record = result.orElseThrow(
                () -> new AccountNotFoundException("Account record not found")
        );

        return accountResponseMapper.map(record);
    }
}
