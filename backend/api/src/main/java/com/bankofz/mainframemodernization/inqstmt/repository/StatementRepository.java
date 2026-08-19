package com.bankofz.mainframemodernization.inqstmt.repository;

import com.bankofz.mainframemodernization.inqstmt.repository.model.StatementEntryRow;

import java.math.BigDecimal;
import java.util.List;

public interface StatementRepository {

    boolean accountExists(String sortCode, String accountNumber);

    BigDecimal sumAmountsBeforeDate(String sortCode, String accountNumber, String beforeDateExclusive);

    List<StatementEntryRow> findEntriesWithinPeriod(
            String sortCode,
            String accountNumber,
            String periodFrom,
            String periodTo
    );
}
