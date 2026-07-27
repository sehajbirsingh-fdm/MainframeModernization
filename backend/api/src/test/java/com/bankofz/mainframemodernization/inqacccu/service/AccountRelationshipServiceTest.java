package com.bankofz.mainframemodernization.inqacccu.service;

import com.bankofz.mainframemodernization.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.mainframemodernization.inqacccu.exception.RepositoryUnavailableException;
import com.bankofz.mainframemodernization.inqacccu.exception.RetrievalStageFailureException;
import com.bankofz.mainframemodernization.inqacccu.repository.AccountRelationshipRepository;
import com.bankofz.mainframemodernization.inqacccu.repository.model.AccountProjection;
import com.bankofz.mainframemodernization.inqacccu.repository.model.CustomerProjection;
import com.bankofz.mainframemodernization.inqacccu.repository.model.RelationshipProjection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountRelationshipServiceTest {

    private final AccountRelationshipRepository repository = mock(AccountRelationshipRepository.class);
    private final AccountRelationshipMapper mapper = new AccountRelationshipMapper(new DateMapper());
    private final AccountRelationshipService service = new AccountRelationshipService(repository, mapper);

    @Test
    void shouldReturnSuccessWhenRepositoryFindsCustomer() {
        RelationshipProjection projection = new RelationshipProjection(
                new CustomerProjection("0000000001", "John Smith", "123456", "INDIVIDUAL"),
                List.of(new AccountProjection(
                        "1000000001",
                        "123456",
                        "CHK",
                    20200115,
                        new BigDecimal("100.00"),
                        new BigDecimal("90.00"),
                        new BigDecimal("0.50"),
                        500,
                        20251231,
                        20260131
                ))
        );

        when(repository.findByCustomerNumber("0000000001")).thenReturn(Optional.of(projection));

        AccountRelationshipResponse response = service.inquire("0000000001");

        assertThat(response.legacyStatus().success()).isEqualTo("Y");
        assertThat(response.numberOfAccounts()).isEqualTo(1);
        assertThat(response.accounts()).hasSize(1);
    }

    @Test
    void shouldReturnNotFoundWhenRepositoryMissesCustomer() {
        when(repository.findByCustomerNumber("0000000099")).thenReturn(Optional.empty());

        AccountRelationshipResponse response = service.inquire("0000000099");

        assertThat(response.legacyStatus().success()).isEqualTo("N");
        assertThat(response.legacyStatus().failCode()).isEqualTo("1");
        assertThat(response.customerNumber()).isEqualTo("0000000099");
        assertThat(response.numberOfAccounts()).isZero();
        assertThat(response.accounts()).isEmpty();
    }

    @Test
    void shouldReturnOpenStageBusinessFailureWhenRepositorySignalsOpenFailure() {
        when(repository.findByCustomerNumber("0000000200"))
                .thenThrow(RetrievalStageFailureException.openStage("0000000200"));

        AccountRelationshipResponse response = service.inquire("0000000200");

        assertThat(response.legacyStatus().success()).isEqualTo("N");
        assertThat(response.legacyStatus().failCode()).isEqualTo("2");
        assertThat(response.legacyStatus().customerFound()).isEqualTo("Y");
        assertThat(response.customerNumber()).isEqualTo("0000000200");
        assertThat(response.numberOfAccounts()).isZero();
        assertThat(response.accounts()).isEmpty();
    }

    @Test
    void shouldReturnFetchStageBusinessFailureWhenRepositorySignalsFetchFailure() {
        when(repository.findByCustomerNumber("0000000300"))
                .thenThrow(RetrievalStageFailureException.fetchStage("0000000300"));

        AccountRelationshipResponse response = service.inquire("0000000300");

        assertThat(response.legacyStatus().success()).isEqualTo("N");
        assertThat(response.legacyStatus().failCode()).isEqualTo("3");
        assertThat(response.legacyStatus().customerFound()).isEqualTo("Y");
        assertThat(response.customerNumber()).isEqualTo("0000000300");
        assertThat(response.numberOfAccounts()).isZero();
        assertThat(response.accounts()).isEmpty();
    }

    @Test
    void shouldReturnCloseStageBusinessFailureWhenRepositorySignalsCloseFailure() {
        when(repository.findByCustomerNumber("0000000400"))
                .thenThrow(RetrievalStageFailureException.closeStage("0000000400"));

        AccountRelationshipResponse response = service.inquire("0000000400");

        assertThat(response.legacyStatus().success()).isEqualTo("N");
        assertThat(response.legacyStatus().failCode()).isEqualTo("4");
        assertThat(response.legacyStatus().customerFound()).isEqualTo("Y");
        assertThat(response.customerNumber()).isEqualTo("0000000400");
        assertThat(response.numberOfAccounts()).isZero();
        assertThat(response.accounts()).isEmpty();
    }

    @Test
    void shouldPropagateInfrastructureFailure() {
        when(repository.findByCustomerNumber("0000000001"))
                .thenThrow(new RepositoryUnavailableException("repo unavailable", new RuntimeException("boom")));

        assertThatThrownBy(() -> service.inquire("0000000001"))
                .isInstanceOf(RepositoryUnavailableException.class)
                .hasMessageContaining("repo unavailable");
    }
}
