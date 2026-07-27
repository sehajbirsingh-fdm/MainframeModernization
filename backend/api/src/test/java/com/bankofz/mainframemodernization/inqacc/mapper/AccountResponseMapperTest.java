package com.bankofz.mainframemodernization.inqacc.mapper;

import com.bankofz.mainframemodernization.inqacc.domain.AccountRecord;
import com.bankofz.mainframemodernization.inqacc.domain.AccountResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountResponseMapperTest {

    private final AccountResponseMapper mapper = new AccountResponseMapper();

    @Test
    void shouldTrimAndConvertLegacyFields() {
        AccountRecord record = new AccountRecord(
                "ACCOUNT   ",
                "1000000001 ",
                "543210",
                "12345678",
                "CHK ",
                new BigDecimal("1.2500"),
                20230110,
                1500,
                20240401,
                20240501,
                new BigDecimal("1800.00"),
                new BigDecimal("1700.00")
        );

        AccountResponse response = mapper.map(record);

        assertThat(response.eyecatcher()).isEqualTo("ACCOUNT");
        assertThat(response.customerNumber()).isEqualTo("1000000001");
        assertThat(response.accountType()).isEqualTo("CHK");
        assertThat(response.accountOpened()).isEqualTo("2023-01-10");
        assertThat(response.lastStatementDate()).isEqualTo("2024-04-01");
        assertThat(response.nextStatementDate()).isEqualTo("2024-05-01");
        assertThat(response.interestRate()).isEqualByComparingTo("1.2500");
        assertThat(response.availableBalance()).isEqualByComparingTo("1800.00");
    }
}
