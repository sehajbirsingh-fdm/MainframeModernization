package com.bankofz.mainframemodernization.inqacc.config;

import com.bankofz.mainframemodernization.inqacc.repository.AccountRepository;
import com.bankofz.mainframemodernization.inqacc.repository.JdbcAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class DataModeConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    TestSupportConfiguration.class,
                    JdbcAccountRepository.class
            );

    @Test
    void shouldUseJdbcRepositoryAndDataSource() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(AccountRepository.class);
                    assertThat(context.getBean(AccountRepository.class)).isInstanceOf(JdbcAccountRepository.class);
                    assertThat(context).hasSingleBean(DataSource.class);
                });
    }

    @Test
    void shouldFailStartupWhenTableNameIsInvalid() {
        contextRunner
                .withPropertyValues(
                        "app.inqacc.db.table-name=ACCOUNT;DROP"
                )
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure().getMessage())
                        .contains("Constructor threw exception");
                });
    }

    @Configuration
    static class TestSupportConfiguration {

        @Bean
        DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl("jdbc:h2:mem:inqacc_cfg;MODE=DB2;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }
    }
}
