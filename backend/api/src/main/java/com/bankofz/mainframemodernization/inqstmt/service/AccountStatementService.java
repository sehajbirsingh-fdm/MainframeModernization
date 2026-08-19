package com.bankofz.mainframemodernization.inqstmt.service;

import com.bankofz.mainframemodernization.inqstmt.domain.AccountStatementResponse;
import com.bankofz.mainframemodernization.inqstmt.domain.StatementEntry;
import com.bankofz.mainframemodernization.inqstmt.domain.StatementSummary;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementNotFoundException;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementRepositoryException;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementTechnicalException;
import com.bankofz.mainframemodernization.inqstmt.repository.StatementRepository;
import com.bankofz.mainframemodernization.inqstmt.repository.model.StatementEntryRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AccountStatementService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String DEFAULT_DESCRIPTION = "N/A";

    private final StatementRepository statementRepository;

    public AccountStatementService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public AccountStatementResponse retrieveStatement(String sortCode, String accountNumber, String period) {
        YearMonth yearMonth = YearMonth.parse(period, PERIOD_FORMATTER);
        String periodFrom = yearMonth.atDay(1).format(DAY_FORMATTER);
        String periodTo = yearMonth.atEndOfMonth().format(DAY_FORMATTER);

        try {
            if (!statementRepository.accountExists(sortCode, accountNumber)) {
                throw new StatementNotFoundException("Account not found");
            }

            BigDecimal openingBalance = statementRepository.sumAmountsBeforeDate(sortCode, accountNumber, periodFrom);
            List<StatementEntry> entries = statementRepository
                    .findEntriesWithinPeriod(sortCode, accountNumber, periodFrom, periodTo)
                    .stream()
                    .map(this::toEntry)
                    .toList();

            BigDecimal totalCredits = entries.stream()
                    .map(StatementEntry::amount)
                    .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDebits = entries.stream()
                    .map(StatementEntry::amount)
                    .filter(amount -> amount.compareTo(BigDecimal.ZERO) < 0)
                    .map(BigDecimal::abs)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal closingBalance = openingBalance.add(totalCredits).subtract(totalDebits);

            StatementSummary summary = new StatementSummary(
                    periodFrom,
                    periodTo,
                    openingBalance,
                    totalCredits,
                    totalDebits,
                    closingBalance,
                    entries.size()
            );

            return new AccountStatementResponse(sortCode, accountNumber, period, summary, entries);
        } catch (StatementRepositoryException exception) {
            throw new StatementTechnicalException("Statement retrieval failed", exception);
        }
    }

    private StatementEntry toEntry(StatementEntryRow row) {
        return new StatementEntry(
                row.date(),
                row.time(),
                row.reference(),
                row.type(),
                normalizeDescription(row.description()),
                row.amount()
        );
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return DEFAULT_DESCRIPTION;
        }
        return description;
    }
}
