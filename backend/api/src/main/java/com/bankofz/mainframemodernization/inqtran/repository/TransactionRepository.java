package com.bankofz.mainframemodernization.inqtran.repository;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionQueryCriteria;
import com.bankofz.mainframemodernization.inqtran.repository.model.TransactionRow;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    int countByCriteria(TransactionQueryCriteria criteria);

    List<TransactionRow> findByCriteria(TransactionQueryCriteria criteria);

    Optional<TransactionRow> findDetailByIdentity(
            String sortCode,
            String accountNumber,
            String date,
            String time,
            String reference
    );
}
