package com.bankofz.mainframemodernization.inqstmt.repository;

import com.bankofz.mainframemodernization.inqstmt.exception.StatementRepositoryException;
import com.bankofz.mainframemodernization.inqstmt.repository.model.StatementEntryRow;
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
import java.util.regex.Pattern;

@Repository
public class JdbcStatementRepository implements StatementRepository {

    private static final Pattern SAFE_IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DataSource dataSource;
    private final String accountTableReference;
    private final String transactionTableReference;

    public JdbcStatementRepository(
            DataSource dataSource,
            @Value("${app.inqstmt.db.schema:}") String schema,
            @Value("${app.inqstmt.account.table-name:ACCOUNT}") String accountTableName,
            @Value("${app.inqstmt.transaction.table-name:PROCTRAN}") String transactionTableName
    ) {
        this.dataSource = dataSource;
        this.accountTableReference = buildTableReference(schema, accountTableName, "app.inqstmt.account.table-name");
        this.transactionTableReference = buildTableReference(schema, transactionTableName, "app.inqstmt.transaction.table-name");
    }

    @Override
    public boolean accountExists(String sortCode, String accountNumber) {
        String sql = """
                SELECT COUNT(*)
                FROM %s
                WHERE ACCOUNT_SORTCODE = ?
                  AND ACCOUNT_NUMBER = ?
                """.formatted(accountTableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);
            statement.setString(2, accountNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new StatementRepositoryException("Failed account existence lookup", exception);
        }
    }

    @Override
    public BigDecimal sumAmountsBeforeDate(String sortCode, String accountNumber, String beforeDateExclusive) {
        String sql = """
                SELECT COALESCE(SUM(PROCTRAN_AMOUNT), 0)
                FROM %s
                WHERE PROCTRAN_SORTCODE = ?
                  AND PROCTRAN_NUMBER = ?
                  AND PROCTRAN_DATE < ?
                """.formatted(transactionTableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);
            statement.setString(2, accountNumber);
            statement.setString(3, beforeDateExclusive);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal sum = resultSet.getBigDecimal(1);
                    return sum == null ? BigDecimal.ZERO : sum;
                }
                return BigDecimal.ZERO;
            }
        } catch (SQLException exception) {
            throw new StatementRepositoryException("Failed pre-period balance lookup", exception);
        }
    }

    @Override
    public List<StatementEntryRow> findEntriesWithinPeriod(
            String sortCode,
            String accountNumber,
            String periodFrom,
            String periodTo
    ) {
        String sql = """
                SELECT
                    PROCTRAN_DATE,
                    PROCTRAN_TIME,
                    PROCTRAN_REF,
                    PROCTRAN_TYPE,
                    PROCTRAN_DESC,
                    PROCTRAN_AMOUNT
                FROM %s
                WHERE PROCTRAN_SORTCODE = ?
                  AND PROCTRAN_NUMBER = ?
                  AND PROCTRAN_DATE >= ?
                  AND PROCTRAN_DATE <= ?
                ORDER BY PROCTRAN_DATE ASC, PROCTRAN_TIME ASC, PROCTRAN_REF ASC
                """.formatted(transactionTableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);
            statement.setString(2, accountNumber);
            statement.setString(3, periodFrom);
            statement.setString(4, periodTo);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<StatementEntryRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(new StatementEntryRow(
                            trim(resultSet.getString("PROCTRAN_DATE")),
                            trim(resultSet.getString("PROCTRAN_TIME")),
                            trim(resultSet.getString("PROCTRAN_REF")),
                            trim(resultSet.getString("PROCTRAN_TYPE")),
                            trimNullable(resultSet.getString("PROCTRAN_DESC")),
                            defaultAmount(resultSet.getBigDecimal("PROCTRAN_AMOUNT"))
                    ));
                }
                return rows;
            }
        } catch (SQLException exception) {
            throw new StatementRepositoryException("Failed statement entry lookup", exception);
        }
    }

    private String buildTableReference(String schema, String tableName, String fieldName) {
        String safeTableName = validateIdentifier(tableName, fieldName);
        if (schema == null || schema.isBlank()) {
            return safeTableName;
        }

        String safeSchema = validateIdentifier(schema, "app.inqstmt.db.schema");
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

    private String trimNullable(String value) {
        return value == null ? null : value.stripTrailing();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
