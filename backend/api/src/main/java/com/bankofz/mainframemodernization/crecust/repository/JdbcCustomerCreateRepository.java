package com.bankofz.mainframemodernization.crecust.repository;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

@Repository
public class JdbcCustomerCreateRepository implements CustomerCreateRepository {

    private static final Pattern SAFE_IDENTIFIER_PATTERN =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DataSource dataSource;
    private final String tableReference;

    public JdbcCustomerCreateRepository(
            DataSource dataSource,
            @Value("${app.db.schema:}") String schema,
            @Value("${app.db.table-name:CUSTOMER}") String tableName
    ) {
        this.dataSource = dataSource;
        this.tableReference = buildTableReference(schema, tableName);
    }

    @Override
    public long nextCustomerNumber(String sortCode) {
        String sql = "SELECT COALESCE(MAX(CAST(CUSTOMER_NUMBER AS BIGINT)), 0) + 1 FROM %s WHERE CUSTOMER_SORTCODE = ?"
                .formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                return 1L;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to allocate next customer number", exception);
        }
    }

    @Override
    public CustomerRecord save(CustomerRecord customerRecord) {
        String sql = """
                INSERT INTO %s (
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
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerRecord.eyecatcher());
            statement.setString(2, customerRecord.sortCode());
            statement.setString(3, customerRecord.customerNumber());
            statement.setString(4, customerRecord.title());
            statement.setString(5, customerRecord.firstName());
            statement.setString(6, customerRecord.lastName());
            setNullableInt(statement, 7, customerRecord.dateOfBirth());
            statement.setString(8, customerRecord.phone());
            statement.setString(9, customerRecord.addressLine1());
            statement.setString(10, customerRecord.addressLine2());
            statement.setString(11, customerRecord.city());
            statement.setString(12, customerRecord.postcode());
            statement.setString(13, customerRecord.country());
            statement.setString(14, customerRecord.status());
            setNullableInt(statement, 15, customerRecord.createdDate());
            setNullableInt(statement, 16, customerRecord.creditScore());
            setNullableInt(statement, 17, customerRecord.creditScoreReviewDate());
            statement.executeUpdate();
            return customerRecord;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to persist customer", exception);
        }
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
            return;
        }
        statement.setInt(index, value);
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
