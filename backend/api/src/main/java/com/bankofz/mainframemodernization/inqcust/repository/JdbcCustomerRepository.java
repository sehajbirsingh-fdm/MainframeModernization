package com.bankofz.mainframemodernization.inqcust.repository;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
public class JdbcCustomerRepository implements CustomerRepository {

    private static final Pattern SAFE_IDENTIFIER_PATTERN =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DataSource dataSource;
    private final String tableReference;

    public JdbcCustomerRepository(
            DataSource dataSource,
            @Value("${app.db.schema:}") String schema,
            @Value("${app.db.table-name:CUSTOMER}") String tableName
    ) {
        this.dataSource = dataSource;
        this.tableReference = buildTableReference(schema, tableName);
    }

    @Override
    public Optional<CustomerRecord> findBySortCodeAndCustomerNumber(String sortCode, String customerNumber) {
        String sql = """
                SELECT
                    CUSTOMER_EYECATCHER,
                    CUSTOMER_SORTCODE,
                    CUSTOMER_NUMBER,
                    CUSTOMER_TITLE,
                    CUSTOMER_FIRST_NAME,
                    CUSTOMER_LAST_NAME,
                    CUSTOMER_DATE_OF_BIRTH,
                    CUSTOMER_PHONE,
                    CUSTOMER_ADDR_LINE1,
                    CUSTOMER_ADDR_LINE2,
                    CUSTOMER_CITY,
                    CUSTOMER_POSTCODE,
                    CUSTOMER_COUNTRY,
                    CUSTOMER_STATUS,
                    CUSTOMER_CREATED_DATE,
                    CUSTOMER_CREDIT_SCORE,
                    CUSTOMER_CS_REVIEW_DATE
                FROM %s
                WHERE CUSTOMER_SORTCODE = ? AND CUSTOMER_NUMBER = ?
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);
            statement.setString(2, customerNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed DB lookup for specific customer", exception);
        }
    }

    @Override
    public List<CustomerRecord> findBySortCode(String sortCode) {
        String sql = """
                SELECT
                    CUSTOMER_EYECATCHER,
                    CUSTOMER_SORTCODE,
                    CUSTOMER_NUMBER,
                    CUSTOMER_TITLE,
                    CUSTOMER_FIRST_NAME,
                    CUSTOMER_LAST_NAME,
                    CUSTOMER_DATE_OF_BIRTH,
                    CUSTOMER_PHONE,
                    CUSTOMER_ADDR_LINE1,
                    CUSTOMER_ADDR_LINE2,
                    CUSTOMER_CITY,
                    CUSTOMER_POSTCODE,
                    CUSTOMER_COUNTRY,
                    CUSTOMER_STATUS,
                    CUSTOMER_CREATED_DATE,
                    CUSTOMER_CREDIT_SCORE,
                    CUSTOMER_CS_REVIEW_DATE
                FROM %s
                WHERE CUSTOMER_SORTCODE = ?
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<CustomerRecord> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed DB lookup for sort code", exception);
        }
    }

    @Override
    public Optional<CustomerRecord> findLatestBySortCode(String sortCode) {
        String sql = """
                SELECT
                    CUSTOMER_EYECATCHER,
                    CUSTOMER_SORTCODE,
                    CUSTOMER_NUMBER,
                    CUSTOMER_TITLE,
                    CUSTOMER_FIRST_NAME,
                    CUSTOMER_LAST_NAME,
                    CUSTOMER_DATE_OF_BIRTH,
                    CUSTOMER_PHONE,
                    CUSTOMER_ADDR_LINE1,
                    CUSTOMER_ADDR_LINE2,
                    CUSTOMER_CITY,
                    CUSTOMER_POSTCODE,
                    CUSTOMER_COUNTRY,
                    CUSTOMER_STATUS,
                    CUSTOMER_CREATED_DATE,
                    CUSTOMER_CREDIT_SCORE,
                    CUSTOMER_CS_REVIEW_DATE
                FROM %s
                WHERE CUSTOMER_SORTCODE = ?
                ORDER BY CUSTOMER_NUMBER DESC
                FETCH FIRST 1 ROW ONLY
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed DB lookup for latest customer", exception);
        }
    }

    private CustomerRecord mapRow(ResultSet resultSet) throws SQLException {
        return new CustomerRecord(
                resultSet.getString("CUSTOMER_EYECATCHER"),
                resultSet.getString("CUSTOMER_SORTCODE"),
                resultSet.getString("CUSTOMER_NUMBER"),
                resultSet.getString("CUSTOMER_TITLE"),
                resultSet.getString("CUSTOMER_FIRST_NAME"),
                resultSet.getString("CUSTOMER_LAST_NAME"),
                getNullableInt(resultSet, "CUSTOMER_DATE_OF_BIRTH"),
                resultSet.getString("CUSTOMER_PHONE"),
                resultSet.getString("CUSTOMER_ADDR_LINE1"),
                resultSet.getString("CUSTOMER_ADDR_LINE2"),
                resultSet.getString("CUSTOMER_CITY"),
                resultSet.getString("CUSTOMER_POSTCODE"),
                resultSet.getString("CUSTOMER_COUNTRY"),
                resultSet.getString("CUSTOMER_STATUS"),
                getNullableInt(resultSet, "CUSTOMER_CREATED_DATE"),
                getNullableInt(resultSet, "CUSTOMER_CREDIT_SCORE"),
                getNullableInt(resultSet, "CUSTOMER_CS_REVIEW_DATE")
        );
    }

    private Integer getNullableInt(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private String buildTableReference(String schema, String tableName) {
        String safeTableName = validateIdentifier(tableName, "app.db.table-name");
        if (schema == null || schema.isBlank()) {
            return safeTableName;
        }

        String safeSchema = validateIdentifier(schema, "app.db.schema");
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
}
