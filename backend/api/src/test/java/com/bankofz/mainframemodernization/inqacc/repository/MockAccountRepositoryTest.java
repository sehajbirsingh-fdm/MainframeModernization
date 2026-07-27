package com.bankofz.mainframemodernization.inqacc.repository;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class MockAccountRepositoryTest {

    private static final String DB_URL = "jdbc:h2:mem:inqacc_repo_test;MODE=DB2;DB_CLOSE_DELAY=-1";

    @Test
    void shouldFindAccountByCompositeKey() throws Exception {
        seedDatabase();
        JdbcAccountRepository repository = new JdbcAccountRepository(createDataSource(DB_URL), "", "ACCOUNT");

        assertThat(repository.findBySortcodeAndAccountNumber("123456", "00000099")).isPresent();
        assertThat(repository.findBySortcodeAndAccountNumber("123456", "00000000")).isEmpty();
    }

    @Test
    void shouldReturnHighestAccountNumberBySortcode() throws Exception {
        seedDatabase();
        JdbcAccountRepository repository = new JdbcAccountRepository(createDataSource(DB_URL), "", "ACCOUNT");

        assertThat(repository.findHighestAccountNumberBySortcode("123456"))
                .isPresent()
                .get()
                .extracting(account -> account.accountNumber())
            .isEqualTo("00000099");
    }

    private static void seedDatabase() throws SQLException {
        try (Connection connection = createDataSource(DB_URL).getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS ACCOUNT");
            statement.execute("""
                    CREATE TABLE ACCOUNT (
                        ACCOUNT_EYECATCHER VARCHAR(8),
                        ACCOUNT_CUSTOMER_NUMBER CHAR(10),
                        ACCOUNT_SORTCODE CHAR(6),
                        ACCOUNT_NUMBER CHAR(8),
                        ACCOUNT_TYPE VARCHAR(3),
                        ACCOUNT_INTEREST_RATE DECIMAL(8,2),
                        ACCOUNT_OPENED INTEGER,
                        ACCOUNT_OVERDRAFT_LIMIT INTEGER,
                        ACCOUNT_LAST_STATEMENT INTEGER,
                        ACCOUNT_NEXT_STATEMENT INTEGER,
                        ACCOUNT_AVAILABLE_BALANCE DECIMAL(15,2),
                        ACCOUNT_ACTUAL_BALANCE DECIMAL(15,2)
                    )
                    """);
        }

        insertAccount("0000000001", "123456", "00000001");
        insertAccount("0000000001", "123456", "00000099");
    }

    private static void insertAccount(String customerNumber, String sortCode, String accountNumber) throws SQLException {
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

        try (Connection connection = createDataSource(DB_URL).getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "ACCOUNT");
            statement.setString(2, customerNumber);
            statement.setString(3, sortCode);
            statement.setString(4, accountNumber);
            statement.setString(5, "CHK");
            statement.setBigDecimal(6, new BigDecimal("0.50"));
            statement.setInt(7, 20200115);
            statement.setInt(8, 500);
            statement.setInt(9, 20251231);
            statement.setInt(10, 20260131);
            statement.setBigDecimal(11, new BigDecimal("1520.45"));
            statement.setBigDecimal(12, new BigDecimal("1498.12"));
            statement.executeUpdate();
        }
    }

    private static DataSource createDataSource(String dbUrl) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(dbUrl);
        return dataSource;
    }
}
