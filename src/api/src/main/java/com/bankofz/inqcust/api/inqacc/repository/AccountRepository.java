package com.bankofz.inqcust.api.inqacc.repository;

import com.bankofz.inqcust.api.inqacc.domain.AccountRecord;

import java.util.Optional;

public interface AccountRepository {

    Optional<AccountRecord> findBySortcodeAndAccountNumber(String sortcode, String accountNumber);

    Optional<AccountRecord> findHighestAccountNumberBySortcode(String sortcode);
}
