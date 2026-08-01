package com.bankofz.mainframemodernization.inqtran.service;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionInquiryResponse;
import com.bankofz.mainframemodernization.inqtran.domain.TransactionQueryCriteria;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionRepositoryException;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionTechnicalException;
import com.bankofz.mainframemodernization.inqtran.mapper.TransactionInquiryMapper;
import com.bankofz.mainframemodernization.inqtran.repository.TransactionRepository;
import com.bankofz.mainframemodernization.inqtran.repository.model.TransactionRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionInquiryServiceTest {

    private TransactionRepository transactionRepository;
    private TransactionInquiryService transactionInquiryService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        transactionInquiryService = new TransactionInquiryService(transactionRepository, new TransactionInquiryMapper());
    }

    @Test
    void shouldNormalizeLimitAndOffsetAndMapResponse() {
        TransactionQueryCriteria criteria = new TransactionQueryCriteria(
                "123456",
                "00000001",
                "20260701",
                "20260731",
                100,
                2
        );

        when(transactionRepository.countByCriteria(criteria)).thenReturn(5);
        when(transactionRepository.findByCriteria(criteria)).thenReturn(List.of(
                new TransactionRow("123456", "00000001", "20260728", "143015", "000000000123", "CRD", "Payroll deposit", new BigDecimal("125.50"))
        ));

        TransactionInquiryResponse response = transactionInquiryService.inquire(
                "123456",
                "00000001",
                "20260701",
                "20260731",
                150,
                2
        );

        assertThat(response.limit()).isEqualTo(100);
        assertThat(response.offset()).isEqualTo(2);
        assertThat(response.totalCount()).isEqualTo(5);
        assertThat(response.returnedCount()).isEqualTo(1);
        assertThat(response.transactions().getFirst().transactionId())
                .isEqualTo("123456-00000001-20260728-143015-000000000123");
    }

    @Test
    void shouldUseDefaultBoundsAndLimitWhenDatesAndLimitAreOmitted() {
        when(transactionRepository.countByCriteria(org.mockito.ArgumentMatchers.any())).thenReturn(0);
        when(transactionRepository.findByCriteria(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        transactionInquiryService.inquire("123456", "00000001", null, null, null, null);

        ArgumentCaptor<TransactionQueryCriteria> captor = ArgumentCaptor.forClass(TransactionQueryCriteria.class);
        verify(transactionRepository).countByCriteria(captor.capture());
        TransactionQueryCriteria captured = captor.getValue();

        assertThat(captured.lowerDateBound()).isEqualTo("00000000");
        assertThat(captured.upperDateBound()).isEqualTo("99999999");
        assertThat(captured.limit()).isEqualTo(50);
        assertThat(captured.offset()).isEqualTo(0);
    }

    @Test
    void shouldTreatZeroLimitAsDefaultFifty() {
        when(transactionRepository.countByCriteria(org.mockito.ArgumentMatchers.any())).thenReturn(0);
        when(transactionRepository.findByCriteria(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        transactionInquiryService.inquire("123456", "00000001", null, null, 0, 0);

        ArgumentCaptor<TransactionQueryCriteria> captor = ArgumentCaptor.forClass(TransactionQueryCriteria.class);
        verify(transactionRepository).countByCriteria(captor.capture());
        assertThat(captor.getValue().limit()).isEqualTo(50);
    }

    @Test
    void shouldPropagateRepositoryFailureAsTechnicalFailure() {
        when(transactionRepository.countByCriteria(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new TransactionRepositoryException("Failed", new RuntimeException("db down")));

        assertThatThrownBy(() -> transactionInquiryService.inquire("123456", "00000001", null, null, 50, 0))
                .isInstanceOf(TransactionTechnicalException.class)
                .hasMessage("Transaction inquiry failed");
    }
}
