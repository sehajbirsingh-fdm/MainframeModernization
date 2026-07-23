package com.bankofz.inqcust.api.inqacccu.service;

import com.bankofz.inqcust.api.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.inqcust.api.inqacccu.repository.AccountRelationshipRepository;
import com.bankofz.inqcust.api.inqacccu.repository.model.AccountProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.CustomerProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.RelationshipProjection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
                        "Checking Account",
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
        assertThat(response.accounts().count()).isEqualTo(1);
    }

    @Test
    void shouldReturnNotFoundWhenRepositoryMissesCustomer() {
        when(repository.findByCustomerNumber("0000000099")).thenReturn(Optional.empty());

        AccountRelationshipResponse response = service.inquire("0000000099");

        assertThat(response.legacyStatus().success()).isEqualTo("N");
        assertThat(response.legacyStatus().failCode()).isEqualTo("1001");
    }
}
