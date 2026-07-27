package com.bankofz.mainframemodernization.inqacccu.repository;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class JsonAccountRelationshipRepositoryTest {

    private static final String DB_URL = "jdbc:h2:mem:inqacccu_repo_test;MODE=DB2;DB_CLOSE_DELAY=-1";

    @Test
    void shouldLoadRelationshipByCustomerNumber() throws Exception {
        seedDatabase();

        JdbcAccountRelationshipRepository repository = new JdbcAccountRelationshipRepository(
                createDataSource(DB_URL),
                "",
                "CUSTOMER",
                "",
                "ACCOUNT",
                "",
                "RELATIONSHIP_SIMULATION"
        );

        var result = repository.findByCustomerNumber("0000000001");

        assertThat(result).isPresent();
        assertThat(result.get().accounts()).hasSize(1);
        assertThat(result.get().accounts().get(0).accountNumber()).isEqualTo("10000001");
    }

    private static void seedDatabase() throws SQLException {
        try (Connection connection = createDataSource(DB_URL).getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS CUSTOMER");
            statement.execute("DROP TABLE IF EXISTS ACCOUNT");
            statement.execute("DROP TABLE IF EXISTS RELATIONSHIP_SIMULATION");

            statement.execute("""
                    CREATE TABLE CUSTOMER (
                        CUSTOMER_NUMBER CHAR(10),
                        CUSTOMER_FIRST_NAME VARCHAR(50),
                        CUSTOMER_LAST_NAME VARCHAR(50),
                        CUSTOMER_SORTCODE CHAR(6),
                        CUSTOMER_STATUS VARCHAR(10)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE ACCOUNT (
                        ACCOUNT_CUSTOMER_NUMBER CHAR(10),
                        ACCOUNT_SORTCODE CHAR(6),
                        ACCOUNT_NUMBER CHAR(8),
                        ACCOUNT_TYPE VARCHAR(3),
                        ACCOUNT_OPENED INTEGER,
                        ACCOUNT_AVAILABLE_BALANCE DECIMAL(15,2),
                        ACCOUNT_ACTUAL_BALANCE DECIMAL(15,2),
                        ACCOUNT_INTEREST_RATE DECIMAL(8,2),
                        ACCOUNT_OVERDRAFT_LIMIT INTEGER,
                        ACCOUNT_LAST_STATEMENT INTEGER,
                        ACCOUNT_NEXT_STATEMENT INTEGER
                    )
                    """);
            statement.execute("""
                    CREATE TABLE RELATIONSHIP_SIMULATION (
                        CUSTOMER_NUMBER CHAR(10),
                        SIMULATION_STAGE VARCHAR(20)
                    )
                    """);
        }

        try (Connection connection = createDataSource(DB_URL).getConnection();
             PreparedStatement customer = connection.prepareStatement(
                     "INSERT INTO CUSTOMER (CUSTOMER_NUMBER, CUSTOMER_FIRST_NAME, CUSTOMER_LAST_NAME, CUSTOMER_SORTCODE, CUSTOMER_STATUS) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement account = connection.prepareStatement(
                     "INSERT INTO ACCOUNT (ACCOUNT_CUSTOMER_NUMBER, ACCOUNT_SORTCODE, ACCOUNT_NUMBER, ACCOUNT_TYPE, ACCOUNT_OPENED, ACCOUNT_AVAILABLE_BALANCE, ACCOUNT_ACTUAL_BALANCE, ACCOUNT_INTEREST_RATE, ACCOUNT_OVERDRAFT_LIMIT, ACCOUNT_LAST_STATEMENT, ACCOUNT_NEXT_STATEMENT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
        ) {
            customer.setString(1, "0000000001");
            customer.setString(2, "John");
            customer.setString(3, "Smith");
            customer.setString(4, "123456");
            customer.setString(5, "INDIVIDUAL");
            customer.executeUpdate();

            account.setString(1, "0000000001");
            account.setString(2, "123456");
            account.setString(3, "10000001");
            account.setString(4, "CHK");
            account.setInt(5, 20200115);
            account.setBigDecimal(6, new java.math.BigDecimal("1520.45"));
            account.setBigDecimal(7, new java.math.BigDecimal("1498.12"));
            account.setBigDecimal(8, new java.math.BigDecimal("0.5"));
            account.setInt(9, 500);
            account.setInt(10, 20251231);
            account.setInt(11, 20260131);
            account.executeUpdate();
        }
    }

    private static DataSource createDataSource(String dbUrl) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(dbUrl);
        return dataSource;
    }
}
