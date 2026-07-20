package com.bankofz.inqcust.api.inqacc.config;

import com.bankofz.inqcust.api.inqacc.repository.AccountRepository;
import com.bankofz.inqcust.api.inqacc.repository.JdbcAccountRepository;
import com.bankofz.inqcust.api.inqacc.repository.MockAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class DataModeConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    TestSupportConfiguration.class,
                    InqaccDataSourceConfiguration.class,
                    MockAccountRepository.class,
                    JdbcAccountRepository.class
            )
            .withPropertyValues("app.inqacc.mock-data.path=mock-data/account-records.json");

    @Test
    void shouldUseMockRepositoryAndNoDataSourceInMockMode() {
        contextRunner
                .withPropertyValues("app.data.mode=mock")
                .run(context -> {
                    assertThat(context).hasSingleBean(AccountRepository.class);
                    assertThat(context.getBean(AccountRepository.class)).isInstanceOf(MockAccountRepository.class);
                    assertThat(context).doesNotHaveBean(DataSource.class);
                });
    }

    @Test
    void shouldUseJdbcRepositoryAndDataSourceInDbMode() {
        contextRunner
                .withPropertyValues(
                        "app.data.mode=db",
                        "app.inqacc.db.url=jdbc:h2:mem:inqacc;MODE=DB2;DB_CLOSE_DELAY=-1",
                        "app.inqacc.db.username=sa",
                        "app.inqacc.db.password=sa",
                        "app.inqacc.db.driver-class-name=org.h2.Driver",
                        "app.inqacc.db.table-name=ACCOUNT"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AccountRepository.class);
                    assertThat(context.getBean(AccountRepository.class)).isInstanceOf(JdbcAccountRepository.class);
                    assertThat(context).hasSingleBean(DataSource.class);
                });
    }

    @Test
    void shouldFailStartupWhenDbModeMissingRequiredProperties() {
        contextRunner
                .withPropertyValues(
                        "app.data.mode=db",
                        "app.inqacc.db.table-name=ACCOUNT"
                )
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure().getMessage())
                            .contains("app.inqacc.db.url is required when app.data.mode=db");
                });
    }

    @Configuration
    static class TestSupportConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
