package com.bankofz.inqcust.api.inqacc.service;

import com.bankofz.inqcust.api.inqacc.domain.AccountResponse;
import com.bankofz.inqcust.api.inqacc.exception.AccountNotFoundException;
import com.bankofz.inqcust.api.inqacc.mapper.AccountResponseMapper;
import com.bankofz.inqcust.api.inqacc.repository.JdbcAccountRepository;
import com.bankofz.inqcust.api.inqacc.repository.MockAccountRepository;
import com.bankofz.inqcust.api.inqacc.validation.AccountInquiryValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountInquiryRepositoryParityTest {

    @Test
    void shouldReturnEquivalentResponseForCompositeKeyLookupAcrossRepositories() throws Exception {
        AccountInquiryService mockService = createMockService();
        AccountInquiryService jdbcService = createJdbcService();

        AccountResponse fromMock = mockService.inquireAccount("123456", "00000001");
        AccountResponse fromJdbc = jdbcService.inquireAccount("123456", "00000001");

        assertThat(fromJdbc).isEqualTo(fromMock);
    }

    @Test
    void shouldReturnEquivalentResponseForReservedHighestLookupAcrossRepositories() throws Exception {
        AccountInquiryService mockService = createMockService();
        AccountInquiryService jdbcService = createJdbcService();

        AccountResponse fromMock = mockService.inquireAccount("123456", "99999999");
        AccountResponse fromJdbc = jdbcService.inquireAccount("123456", "99999999");

        assertThat(fromMock.accountNumber()).isEqualTo("00000099");
        assertThat(fromJdbc).isEqualTo(fromMock);
    }

    @Test
    void shouldThrowNotFoundInBothRepositoriesForMissingAccount() throws Exception {
        AccountInquiryService mockService = createMockService();
        AccountInquiryService jdbcService = createJdbcService();

        assertThatThrownBy(() -> mockService.inquireAccount("123456", "00000123"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account record not found");

        assertThatThrownBy(() -> jdbcService.inquireAccount("123456", "00000123"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account record not found");
    }

    private AccountInquiryService createMockService() {
        MockAccountRepository mockAccountRepository = new MockAccountRepository("mock-data/account-records.json", new ObjectMapper());
        return new AccountInquiryService(mockAccountRepository, new AccountResponseMapper(), new AccountInquiryValidator());
    }

    private AccountInquiryService createJdbcService() throws Exception {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:inqacc-parity;MODE=DB2;DB_CLOSE_DELAY=-1", "sa", "sa");

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS ACCOUNT");
            statement.execute("""
                    CREATE TABLE ACCOUNT (
                        ACCOUNT_EYECATCHER VARCHAR(8),
                        ACCOUNT_CUSTOMER_NUMBER VARCHAR(10),
                        ACCOUNT_SORTCODE VARCHAR(6),
                        ACCOUNT_NUMBER VARCHAR(8),
                        ACCOUNT_TYPE VARCHAR(8),
                        ACCOUNT_INTEREST_RATE DECIMAL(10,2),
                        ACCOUNT_OPENED DATE,
                        ACCOUNT_OVERDRAFT_LIMIT INTEGER,
                        ACCOUNT_LAST_STATEMENT INTEGER,
                        ACCOUNT_NEXT_STATEMENT INTEGER,
                        ACCOUNT_AVAILABLE_BALANCE DECIMAL(12,2),
                        ACCOUNT_ACTUAL_BALANCE DECIMAL(12,2)
                    )
                    """);
        }

        insertRecord(dataSource, "ACCT", "1234567890", "123456", "00000001", "SAVINGS", "2.50", "2020-01-15", 5000, 20240131, 20240228, "15750.50", "15750.50");
        insertRecord(dataSource, "ACCT", "1234567891", "123456", "00000099", "CURRENT", "1.10", "2019-12-01", 2500, 20240215, 20240315, "420.99", "410.99");
        insertRecord(dataSource, "ACCT", "1234567892", "654321", "10000000", "BUSINESS", "0.75", "2018-05-20", 75000, 20240120, 20240220, "999999.01", "998120.11");

        JdbcAccountRepository jdbcAccountRepository = new JdbcAccountRepository(dataSource, "", "ACCOUNT");
        return new AccountInquiryService(jdbcAccountRepository, new AccountResponseMapper(), new AccountInquiryValidator());
    }

    private void insertRecord(
            DataSource dataSource,
            String eyecatcher,
            String customerNumber,
            String sortcode,
            String accountNumber,
            String accountType,
            String interestRate,
            String accountOpened,
            Integer overdraftLimit,
            Integer lastStatementDate,
            Integer nextStatementDate,
            String availableBalance,
            String actualBalance
    ) throws Exception {
        String sql = """
                INSERT INTO ACCOUNT (
                    ACCOUNT_EYECATCHER,
                    ACCOUNT_CUSTOMER_NUMBER,
                    ACCOUNT_SORTCODE,
                    ACCOUNT_NUMBER,
                    ACCOUNT_TYPE,
                    ACCOUNT_INTEREST_RATE,
                    ACCOUNT_OPENED,
                    ACCOUNT_OVERDRAFT_LIMIT,
                    ACCOUNT_LAST_STATEMENT,
                    ACCOUNT_NEXT_STATEMENT,
                    ACCOUNT_AVAILABLE_BALANCE,
                    ACCOUNT_ACTUAL_BALANCE
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eyecatcher);
            statement.setString(2, customerNumber);
            statement.setString(3, sortcode);
            statement.setString(4, accountNumber);
            statement.setString(5, accountType);
            statement.setBigDecimal(6, new java.math.BigDecimal(interestRate));
            statement.setDate(7, java.sql.Date.valueOf(accountOpened));
            statement.setInt(8, overdraftLimit);
            statement.setInt(9, lastStatementDate);
            statement.setInt(10, nextStatementDate);
            statement.setBigDecimal(11, new java.math.BigDecimal(availableBalance));
            statement.setBigDecimal(12, new java.math.BigDecimal(actualBalance));
            statement.executeUpdate();
        }
    }
}
