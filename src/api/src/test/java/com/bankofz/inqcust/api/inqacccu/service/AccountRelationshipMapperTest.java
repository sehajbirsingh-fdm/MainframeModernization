package com.bankofz.inqcust.api.inqacccu.service;

import com.bankofz.inqcust.api.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.inqcust.api.inqacccu.repository.model.AccountProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.CustomerProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.RelationshipProjection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRelationshipMapperTest {

    private final AccountRelationshipMapper mapper = new AccountRelationshipMapper(new DateMapper());

    @Test
    void shouldMapProjectionToSuccessResponse() {
        RelationshipProjection projection = new RelationshipProjection(
                new CustomerProjection("0000000001  ", "John Smith   ", "123456", "INDIVIDUAL  "),
                List.of(new AccountProjection(
                        "1000000001",
                        "123456",
                        "CHK",
                        "Checking Account ",
                        new BigDecimal("1520.45"),
                        new BigDecimal("1498.12"),
                        new BigDecimal("0.50"),
                        500,
                        20251231,
                        20260131
                ))
        );

        AccountRelationshipResponse response = mapper.toSuccessResponse(projection);

        assertThat(response.legacyStatus().success()).isEqualTo("Y");
        assertThat(response.legacyStatus().failCode()).isEqualTo("0000");
        assertThat(response.customer().customerNumber()).isEqualTo("0000000001");
        assertThat(response.customer().customerName()).isEqualTo("John Smith");
        assertThat(response.accounts().count()).isEqualTo(1);
        assertThat(response.accounts().accounts().get(0).lastStatementDate()).isEqualTo("2025-12-31");
        assertThat(response.accounts().accounts().get(0).nextStatementDate()).isEqualTo("2026-01-31");
    }

    @Test
    void shouldMapNotFoundOutcome() {
        AccountRelationshipResponse response = mapper.toNotFoundResponse();

        assertThat(response.legacyStatus().success()).isEqualTo("N");
        assertThat(response.legacyStatus().failCode()).isEqualTo("1001");
        assertThat(response.customer()).isNull();
        assertThat(response.accounts()).isNull();
    }
}
