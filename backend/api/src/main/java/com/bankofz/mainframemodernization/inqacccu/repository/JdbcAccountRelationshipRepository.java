package com.bankofz.mainframemodernization.inqacccu.repository;

import com.bankofz.mainframemodernization.inqacccu.exception.RepositoryUnavailableException;
import com.bankofz.mainframemodernization.inqacccu.exception.RetrievalStageFailureException;
import com.bankofz.mainframemodernization.inqacccu.repository.model.AccountProjection;
import com.bankofz.mainframemodernization.inqacccu.repository.model.CustomerProjection;
import com.bankofz.mainframemodernization.inqacccu.repository.model.RelationshipProjection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
public class JdbcAccountRelationshipRepository implements AccountRelationshipRepository {

    private static final Pattern SAFE_IDENTIFIER_PATTERN =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DataSource dataSource;
    private final String customerTable;
    private final String accountTable;
    private final String simulationTable;

    public JdbcAccountRelationshipRepository(
            DataSource dataSource,
            @Value("${app.db.schema:}") String customerSchema,
            @Value("${app.db.table-name:CUSTOMER}") String customerTableName,
            @Value("${app.inqacc.db.schema:}") String accountSchema,
            @Value("${app.inqacc.db.table-name:ACCOUNT}") String accountTableName,
            @Value("${app.inqacccu.db.schema:}") String simulationSchema,
            @Value("${app.inqacccu.simulation-table:RELATIONSHIP_SIMULATION}") String simulationTableName
    ) {
        this.dataSource = dataSource;
        this.customerTable = tableReference(customerSchema, customerTableName, "app.db");
        this.accountTable = tableReference(accountSchema, accountTableName, "app.inqacc.db");
        this.simulationTable = tableReference(simulationSchema, simulationTableName, "app.inqacccu");
    }

    @Override
    public Optional<RelationshipProjection> findByCustomerNumber(String customerNumber) {
        try (Connection connection = dataSource.getConnection()) {
            Optional<String> simulationStage = loadSimulationStage(connection, customerNumber);
            simulationStage.ifPresent(stage -> simulateFailure(stage, customerNumber));

            Optional<CustomerProjection> customer = loadCustomer(connection, customerNumber);
            if (customer.isEmpty()) {
                return Optional.empty();
            }

            simulationStage.ifPresent(stage -> simulateFailure(stage, customerNumber));
            List<AccountProjection> accounts = loadAccounts(connection, customerNumber);
            simulationStage.ifPresent(stage -> simulateFailure(stage, customerNumber));

            return Optional.of(new RelationshipProjection(customer.get(), accounts));
        } catch (SQLException exception) {
            throw new RepositoryUnavailableException("Unable to read account relationship data", exception);
        }
    }

    private Optional<String> loadSimulationStage(Connection connection, String customerNumber) throws SQLException {
        String sql = "SELECT SIMULATION_STAGE FROM %s WHERE CUSTOMER_NUMBER = ?".formatted(simulationTable);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.ofNullable(resultSet.getString("SIMULATION_STAGE"));
                }
                return Optional.empty();
            }
        }
    }

    private Optional<CustomerProjection> loadCustomer(Connection connection, String customerNumber) throws SQLException {
        String sql = """
                SELECT CUSTOMER_NUMBER, CUSTOMER_FIRST_NAME, CUSTOMER_LAST_NAME, CUSTOMER_SORTCODE, CUSTOMER_STATUS
                FROM %s
                WHERE CUSTOMER_NUMBER = ?
                FETCH FIRST 1 ROW ONLY
                """.formatted(customerTable);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String firstName = trim(resultSet.getString("CUSTOMER_FIRST_NAME"));
                    String lastName = trim(resultSet.getString("CUSTOMER_LAST_NAME"));
                    String customerName = (firstName + " " + lastName).trim();
                    return Optional.of(new CustomerProjection(
                            trim(resultSet.getString("CUSTOMER_NUMBER")),
                            customerName,
                            trim(resultSet.getString("CUSTOMER_SORTCODE")),
                            trim(resultSet.getString("CUSTOMER_STATUS"))
                    ));
                }
                return Optional.empty();
            }
        }
    }

    private List<AccountProjection> loadAccounts(Connection connection, String customerNumber) throws SQLException {
        String sql = """
                SELECT
                    ACCOUNT_NUMBER,
                    ACCOUNT_SORTCODE,
                    ACCOUNT_TYPE,
                    ACCOUNT_OPENED,
                    ACCOUNT_AVAILABLE_BALANCE,
                    ACCOUNT_ACTUAL_BALANCE,
                    ACCOUNT_INTEREST_RATE,
                    ACCOUNT_OVERDRAFT_LIMIT,
                    ACCOUNT_LAST_STATEMENT,
                    ACCOUNT_NEXT_STATEMENT
                FROM %s
                WHERE ACCOUNT_CUSTOMER_NUMBER = ?
                ORDER BY ACCOUNT_NUMBER
                """.formatted(accountTable);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AccountProjection> accounts = new ArrayList<>();
                while (resultSet.next()) {
                    accounts.add(new AccountProjection(
                            trim(resultSet.getString("ACCOUNT_NUMBER")),
                            trim(resultSet.getString("ACCOUNT_SORTCODE")),
                            trim(resultSet.getString("ACCOUNT_TYPE")),
                            getNullableInt(resultSet, "ACCOUNT_OPENED"),
                            defaultBigDecimal(resultSet.getBigDecimal("ACCOUNT_AVAILABLE_BALANCE")),
                            defaultBigDecimal(resultSet.getBigDecimal("ACCOUNT_ACTUAL_BALANCE")),
                            defaultBigDecimal(resultSet.getBigDecimal("ACCOUNT_INTEREST_RATE")),
                            getNullableInt(resultSet, "ACCOUNT_OVERDRAFT_LIMIT"),
                            getNullableInt(resultSet, "ACCOUNT_LAST_STATEMENT"),
                            getNullableInt(resultSet, "ACCOUNT_NEXT_STATEMENT")
                    ));
                }
                return accounts;
            }
        }
    }

    private Integer getNullableInt(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void simulateFailure(String stage, String customerNumber) {
        if ("OPEN_FAILURE".equals(stage)) {
            throw RetrievalStageFailureException.openStage(customerNumber);
        }
        if ("FETCH_FAILURE".equals(stage)) {
            throw RetrievalStageFailureException.fetchStage(customerNumber);
        }
        if ("CLOSE_FAILURE".equals(stage)) {
            throw RetrievalStageFailureException.closeStage(customerNumber);
        }
    }

    private String tableReference(String schema, String tableName, String prefix) {
        String safeTableName = validateIdentifier(tableName, prefix + ".table-name");
        if (schema == null || schema.isBlank()) {
            return safeTableName;
        }
        String safeSchema = validateIdentifier(schema, prefix + ".schema");
        return safeSchema + "." + safeTableName;
    }

    private String validateIdentifier(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(fieldName + " must not be blank");
        }
        String trimmed = value.trim();
        if (!SAFE_IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalStateException(fieldName + " contains unsupported characters");
        }
        return trimmed;
    }

    private String trim(String value) {
        return value == null ? "" : value.stripTrailing();
    }
}
