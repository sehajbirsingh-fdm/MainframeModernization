package com.bankofz.mainframemodernization.inqacc.repository;

import com.bankofz.mainframemodernization.inqacc.domain.AccountRecord;

import java.util.Optional;

public interface AccountRepository {

    Optional<AccountRecord> findBySortcodeAndAccountNumber(String sortcode, String accountNumber);

    Optional<AccountRecord> findHighestAccountNumberBySortcode(String sortcode);
}
