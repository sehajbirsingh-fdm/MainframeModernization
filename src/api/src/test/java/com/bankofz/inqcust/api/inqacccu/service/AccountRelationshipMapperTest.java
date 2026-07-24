package com.bankofz.inqcust.api.inqacccu.service;

import com.bankofz.inqcust.api.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.inqcust.api.inqacccu.repository.model.AccountProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.CustomerProjection;
import com.bankofz.inqcust.api.inqacccu.repository.model.RelationshipProjection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
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
                20200115,
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
        assertThat(response.legacyStatus().failCode()).isEqualTo("0");
        assertThat(response.customerNumber()).isEqualTo("0000000001");
        assertThat(response.numberOfAccounts()).isEqualTo(1);
        assertThat(response.accounts()).hasSize(1);
        assertThat(response.accounts().get(0).eyecatcher()).isEqualTo("ACCT");
        assertThat(response.accounts().get(0).customerNumber()).isEqualTo("0000000001");
        assertThat(response.accounts().get(0).openedDate()).isEqualTo("2020-01-15");
        assertThat(response.accounts().get(0).lastStatementDate()).isEqualTo("2025-12-31");
        assertThat(response.accounts().get(0).nextStatementDate()).isEqualTo("2026-01-31");
    }

    @Test
    void shouldMapNotFoundOutcome() {
        AccountRelationshipResponse response = mapper.toNotFoundResponse("0000000099");

        assertThat(response.legacyStatus().success()).isEqualTo("N");
        assertThat(response.legacyStatus().failCode()).isEqualTo("1");
        assertThat(response.customerNumber()).isEqualTo("0000000099");
        assertThat(response.numberOfAccounts()).isZero();
        assertThat(response.accounts()).isEmpty();
    }

    @Test
    void shouldCapReturnedAccountsAtTwenty() {
        List<AccountProjection> manyAccounts = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            manyAccounts.add(new AccountProjection(
                    String.format("%08d", i),
                    "123456",
                    "CHK",
                    20200115,
                    new BigDecimal("10.00"),
                    new BigDecimal("10.00"),
                    new BigDecimal("0.50"),
                    100,
                    20251231,
                    20260131
            ));
        }

        RelationshipProjection projection = new RelationshipProjection(
                new CustomerProjection("0000000001", "John Smith", "123456", "INDIVIDUAL"),
                manyAccounts
        );

        AccountRelationshipResponse response = mapper.toSuccessResponse(projection);

        assertThat(response.numberOfAccounts()).isEqualTo(20);
        assertThat(response.accounts()).hasSize(20);
    }
}
