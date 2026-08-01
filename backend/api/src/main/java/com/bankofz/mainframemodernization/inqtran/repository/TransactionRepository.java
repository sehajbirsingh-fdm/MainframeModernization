package com.bankofz.mainframemodernization.inqtran.repository;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionQueryCriteria;
import com.bankofz.mainframemodernization.inqtran.repository.model.TransactionRow;

import java.util.List;

public interface TransactionRepository {

    int countByCriteria(TransactionQueryCriteria criteria);

    List<TransactionRow> findByCriteria(TransactionQueryCriteria criteria);
}
