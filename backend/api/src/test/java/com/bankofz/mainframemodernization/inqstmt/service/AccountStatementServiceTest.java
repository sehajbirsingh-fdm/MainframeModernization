package com.bankofz.mainframemodernization.inqstmt.service;

import com.bankofz.mainframemodernization.inqstmt.domain.AccountStatementResponse;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementNotFoundException;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementRepositoryException;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementTechnicalException;
import com.bankofz.mainframemodernization.inqstmt.repository.StatementRepository;
import com.bankofz.mainframemodernization.inqstmt.repository.model.StatementEntryRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountStatementServiceTest {

    private StatementRepository statementRepository;
    private AccountStatementService accountStatementService;

    @BeforeEach
    void setUp() {
        statementRepository = mock(StatementRepository.class);
        accountStatementService = new AccountStatementService(statementRepository);
    }

    @Test
    void shouldBuildLeapYearStatementWithHistoricalOpeningBalance() {
        when(statementRepository.accountExists("123456", "00000001")).thenReturn(true);
        when(statementRepository.sumAmountsBeforeDate("123456", "00000001", "20280201"))
                .thenReturn(new BigDecimal("1000.00"));
        when(statementRepository.findEntriesWithinPeriod("123456", "00000001", "20280201", "20280229"))
                .thenReturn(List.of(
                        new StatementEntryRow("20280210", "101500", "000000000901", "CRD", "Bonus", new BigDecimal("200.00")),
                        new StatementEntryRow("20280229", "120000", "000000000902", "DBT", "Bill payment", new BigDecimal("-50.00"))
                ));

        AccountStatementResponse response = accountStatementService.retrieveStatement("123456", "00000001", "202802");

        assertThat(response.summary().periodFrom()).isEqualTo("20280201");
        assertThat(response.summary().periodTo()).isEqualTo("20280229");
        assertThat(response.summary().openingBalance()).isEqualByComparingTo("1000.00");
        assertThat(response.summary().totalCredits()).isEqualByComparingTo("200.00");
        assertThat(response.summary().totalDebits()).isEqualByComparingTo("50.00");
        assertThat(response.summary().closingBalance()).isEqualByComparingTo("1150.00");
        assertThat(response.summary().transactionCount()).isEqualTo(2);
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() {
        when(statementRepository.accountExists("123456", "00009999")).thenReturn(false);

        assertThatThrownBy(() -> accountStatementService.retrieveStatement("123456", "00009999", "202607"))
                .isInstanceOf(StatementNotFoundException.class)
                .hasMessage("Account not found");
    }

    @Test
    void shouldNormalizeNullOrBlankDescriptionsToNa() {
        when(statementRepository.accountExists("123456", "00000001")).thenReturn(true);
        when(statementRepository.sumAmountsBeforeDate("123456", "00000001", "20260701")).thenReturn(BigDecimal.ZERO);
        when(statementRepository.findEntriesWithinPeriod("123456", "00000001", "20260701", "20260731"))
                .thenReturn(List.of(
                        new StatementEntryRow("20260705", "080000", "000000000777", "CRD", null, new BigDecimal("15.00")),
                        new StatementEntryRow("20260706", "080000", "000000000778", "DBT", "   ", new BigDecimal("-5.00"))
                ));

        AccountStatementResponse response = accountStatementService.retrieveStatement("123456", "00000001", "202607");

        assertThat(response.entries()).extracting("description").containsExactly("N/A", "N/A");
    }

    @Test
    void shouldWrapRepositoryFailureAsTechnicalFailure() {
        when(statementRepository.accountExists("123456", "00000001"))
                .thenThrow(new StatementRepositoryException("lookup failed", new RuntimeException("db down")));

        assertThatThrownBy(() -> accountStatementService.retrieveStatement("123456", "00000001", "202607"))
                .isInstanceOf(StatementTechnicalException.class)
                .hasMessage("Statement retrieval failed");
    }
}
