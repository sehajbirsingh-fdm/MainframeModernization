package com.bankofz.mainframemodernization.inqacc.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class AccountInquiryValidator {

    private static final Pattern SORTCODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^\\d{8}$");

    public void validateSortcode(String sortcode) {
        if (sortcode == null || !SORTCODE_PATTERN.matcher(sortcode).matches()) {
            throw new IllegalArgumentException("Sortcode must be exactly 6 numeric digits");
        }
    }

    public void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || !ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            throw new IllegalArgumentException("Account number must be exactly 8 numeric digits");
        }
    }
}
