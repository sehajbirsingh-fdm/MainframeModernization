package com.bankofz.inqcust.api.inqacc.repository;

import com.bankofz.inqcust.api.inqacc.domain.AccountRecord;
import com.bankofz.inqcust.api.inqacc.exception.RepositoryUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
@ConditionalOnProperty(name = "app.data.mode", havingValue = "db")
public class JdbcAccountRepository implements AccountRepository {

    private static final Pattern SAFE_IDENTIFIER_PATTERN =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DataSource dataSource;
    private final String tableReference;

    public JdbcAccountRepository(
            DataSource dataSource,
            @Value("${app.inqacc.db.schema:}") String schema,
            @Value("${app.inqacc.db.table-name:ACCOUNT}") String tableName
    ) {
        this.dataSource = dataSource;
        this.tableReference = buildTableReference(schema, tableName);
    }

    @Override
    public Optional<AccountRecord> findBySortcodeAndAccountNumber(String sortcode, String accountNumber) {
        String sql = """
                SELECT
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
                FROM %s
                WHERE ACCOUNT_SORTCODE = ? AND ACCOUNT_NUMBER = ?
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortcode);
            statement.setString(2, accountNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryUnavailableException("Failed JDBC account lookup", exception);
        }
    }

    @Override
    public Optional<AccountRecord> findHighestAccountNumberBySortcode(String sortcode) {
        String sql = """
                SELECT
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
                FROM %s
                WHERE ACCOUNT_SORTCODE = ?
                ORDER BY ACCOUNT_NUMBER DESC
                FETCH FIRST 1 ROW ONLY
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortcode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryUnavailableException("Failed JDBC highest-account lookup", exception);
        }
    }

    private AccountRecord mapRow(ResultSet resultSet) throws SQLException {
        return new AccountRecord(
                resultSet.getString("ACCOUNT_EYECATCHER"),
                resultSet.getString("ACCOUNT_CUSTOMER_NUMBER"),
                resultSet.getString("ACCOUNT_SORTCODE"),
                resultSet.getString("ACCOUNT_NUMBER"),
                resultSet.getString("ACCOUNT_TYPE"),
                resultSet.getBigDecimal("ACCOUNT_INTEREST_RATE"),
                toLegacyDate(resultSet.getObject("ACCOUNT_OPENED")),
                getNullableInt(resultSet, "ACCOUNT_OVERDRAFT_LIMIT"),
                toLegacyDate(resultSet.getObject("ACCOUNT_LAST_STATEMENT")),
                toLegacyDate(resultSet.getObject("ACCOUNT_NEXT_STATEMENT")),
                getBigDecimal(resultSet, "ACCOUNT_AVAILABLE_BALANCE"),
                getBigDecimal(resultSet, "ACCOUNT_ACTUAL_BALANCE")
        );
    }

    private BigDecimal getBigDecimal(ResultSet resultSet, String columnName) throws SQLException {
        BigDecimal value = resultSet.getBigDecimal(columnName);
        return value == null ? BigDecimal.ZERO : value;
    }

    private Integer getNullableInt(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private Integer toLegacyDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Long longValue) {
            return Math.toIntExact(longValue);
        }
        if (value instanceof Date dateValue) {
            String compact = dateValue.toLocalDate().toString().replace("-", "");
            return Integer.parseInt(compact);
        }
        if (value instanceof String stringValue) {
            return Integer.parseInt(stringValue.replace("-", ""));
        }
        throw new IllegalStateException("Unsupported date representation: " + value.getClass().getName());
    }

    private String buildTableReference(String schema, String tableName) {
        String safeTableName = validateIdentifier(tableName, "app.inqacc.db.table-name");
        if (schema == null || schema.isBlank()) {
            return safeTableName;
        }

        String safeSchema = validateIdentifier(schema, "app.inqacc.db.schema");
        return safeSchema + "." + safeTableName;
    }

    private String validateIdentifier(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(fieldName + " is required when app.data.mode=db");
        }
        String trimmed = value.trim();
        if (!SAFE_IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalStateException(fieldName + " contains unsupported characters");
        }
        return trimmed;
    }
}
