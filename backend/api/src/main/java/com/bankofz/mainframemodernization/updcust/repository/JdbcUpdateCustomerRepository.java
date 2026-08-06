package com.bankofz.mainframemodernization.updcust.repository;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

@Repository
public class JdbcUpdateCustomerRepository implements UpdateCustomerRepository {

    private static final Pattern SAFE_IDENTIFIER_PATTERN =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DataSource dataSource;
    private final String tableReference;

    public JdbcUpdateCustomerRepository(
            DataSource dataSource,
            @Value("${app.db.schema:}") String schema,
            @Value("${app.db.table-name:CUSTOMER}") String tableName
    ) {
        this.dataSource = dataSource;
        this.tableReference = buildTableReference(schema, tableName);
    }

    @Override
    public CustomerRecord update(CustomerRecord customerRecord) {
        String sql = """
                UPDATE %s
                   SET CUSTOMER_TITLE = ?,
                       CUSTOMER_FIRST_NAME = ?,
                       CUSTOMER_LAST_NAME = ?,
                       CUSTOMER_DATE_OF_BIRTH = ?,
                       CUSTOMER_PHONE = ?,
                       CUSTOMER_ADDR_LINE1 = ?,
                       CUSTOMER_ADDR_LINE2 = ?,
                       CUSTOMER_CITY = ?,
                       CUSTOMER_POSTCODE = ?,
                       CUSTOMER_COUNTRY = ?,
                       CUSTOMER_STATUS = ?
                 WHERE CUSTOMER_SORTCODE = ?
                   AND CUSTOMER_NUMBER = ?
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerRecord.title());
            statement.setString(2, customerRecord.firstName());
            statement.setString(3, customerRecord.lastName());
            setNullableInt(statement, 4, customerRecord.dateOfBirth());
            statement.setString(5, customerRecord.phone());
            statement.setString(6, customerRecord.addressLine1());
            statement.setString(7, customerRecord.addressLine2());
            statement.setString(8, customerRecord.city());
            statement.setString(9, customerRecord.postcode());
            statement.setString(10, customerRecord.country());
            statement.setString(11, customerRecord.status());
            statement.setString(12, customerRecord.sortCode());
            statement.setString(13, customerRecord.customerNumber());

            int updatedRows = statement.executeUpdate();
            if (updatedRows != 1) {
                throw new IllegalStateException("Unexpected updated rows count: " + updatedRows);
            }

            return customerRecord;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update customer", exception);
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
