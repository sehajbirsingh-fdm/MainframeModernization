package com.bankofz.mainframemodernization.inqtran.service;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionDetailInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionQueryCriteria;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionRecord;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionRepositoryException;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionTechnicalException;
import com.bankofz.mainframemodernization.inqtran.mapper.TransactionInquiryMapper;
import com.bankofz.mainframemodernization.inqtran.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionInquiryService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final String MIN_DATE_SENTINEL = "00000000";
    private static final String MAX_DATE_SENTINEL = "99999999";

    private final TransactionRepository transactionRepository;
    private final TransactionInquiryMapper transactionInquiryMapper;

    public TransactionInquiryService(
            TransactionRepository transactionRepository,
            TransactionInquiryMapper transactionInquiryMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.transactionInquiryMapper = transactionInquiryMapper;
    }

    public TransactionInquiryResponse inquire(
            String sortCode,
            String accountNumber,
            String fromDate,
            String toDate,
            Integer limit,
            Integer offset
    ) {
        int effectiveLimit = normalizeLimit(limit);
        int effectiveOffset = offset == null ? 0 : offset;
        String lowerDateBound = fromDate == null ? MIN_DATE_SENTINEL : fromDate;
        String upperDateBound = toDate == null ? MAX_DATE_SENTINEL : toDate;

        TransactionQueryCriteria criteria = new TransactionQueryCriteria(
                sortCode,
                accountNumber,
                lowerDateBound,
                upperDateBound,
                effectiveLimit,
                effectiveOffset
        );

        try {
            int totalCount = transactionRepository.countByCriteria(criteria);
            List<TransactionRecord> records = transactionRepository.findByCriteria(criteria).stream()
                    .map(transactionInquiryMapper::toRecord)
                    .toList();

            return new TransactionInquiryResponse(
                    sortCode,
                    accountNumber,
                    fromDate,
                    toDate,
                    effectiveLimit,
                    effectiveOffset,
                    totalCount,
                    records.size(),
                    records
            );
        } catch (TransactionRepositoryException exception) {
            throw new TransactionTechnicalException("Transaction inquiry failed", exception);
        }
    }

    public TransactionDetailInquiryResponse inquireDetail(
            String sortCode,
            String accountNumber,
            String date,
            String time,
            String reference
    ) {
        try {
            return transactionRepository.findDetailByIdentity(sortCode, accountNumber, date, time, reference)
                    .map(transactionInquiryMapper::toRecord)
                    .map(record -> new TransactionDetailInquiryResponse(true, record))
                    .orElseGet(() -> new TransactionDetailInquiryResponse(false, null));
        } catch (TransactionRepositoryException exception) {
            throw new TransactionTechnicalException("Transaction detail inquiry failed", exception);
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit == 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
