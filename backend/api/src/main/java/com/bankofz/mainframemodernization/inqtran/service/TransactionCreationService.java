package com.bankofz.mainframemodernization.inqtran.service;

import com.bankofz.mainframemodernization.inqacc.repository.AccountRepository;
import com.bankofz.mainframemodernization.inqtran.domain.CreateTransactionRequest;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionRecord;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionRepositoryException;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionTechnicalException;
import com.bankofz.mainframemodernization.inqtran.mapper.TransactionInquiryMapper;
import com.bankofz.mainframemodernization.inqtran.repository.TransactionRepository;
import com.bankofz.mainframemodernization.inqtran.repository.model.TransactionRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class TransactionCreationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmss");

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionInquiryMapper mapper;
    private final Clock clock;
    private final ZoneId transactionZone;

    public TransactionCreationService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            TransactionInquiryMapper mapper,
            Clock clock,
            @Value("${app.inqtran.time-zone:America/Toronto}") String transactionTimeZone
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
        this.clock = clock;
        this.transactionZone = ZoneId.of(transactionTimeZone);
    }

    public TransactionRecord create(
            String sortCode,
            String accountNumber,
            CreateTransactionRequest request
    ) {
        if (accountRepository.findBySortcodeAndAccountNumber(sortCode, accountNumber).isEmpty()) {
            throw new IllegalArgumentException("Account does not exist");
        }

        String type = request.type().trim();
        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        if ("DBT".equals(type) && amount.signum() >= 0) {
            throw new IllegalArgumentException("DBT amount must be negative");
        }
        if ("CRD".equals(type) && amount.signum() <= 0) {
            throw new IllegalArgumentException("CRD amount must be positive");
        }

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), transactionZone);
        try {
            TransactionRow created = transactionRepository.create(
                    sortCode,
                    accountNumber,
                    DATE.format(now),
                    TIME.format(now),
                    type,
                    request.description().trim(),
                    amount
            );
            return mapper.toRecord(created);
        } catch (TransactionRepositoryException exception) {
            throw new TransactionTechnicalException("Transaction creation failed", exception);
        }
    }
}
