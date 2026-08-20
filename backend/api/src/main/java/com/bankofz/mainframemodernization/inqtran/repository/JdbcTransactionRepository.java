package com.bankofz.mainframemodernization.inqtran.repository;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionQueryCriteria;
import com.bankofz.mainframemodernization.inqtran.exception.TransactionRepositoryException;
import com.bankofz.mainframemodernization.inqtran.repository.model.TransactionRow;
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
public class JdbcTransactionRepository implements TransactionRepository {

    private static final Pattern SAFE_IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DataSource dataSource;
    private final String tableReference;

    public JdbcTransactionRepository(
            DataSource dataSource,
            @Value("${app.inqtran.db.schema:}") String schema,
            @Value("${app.inqtran.db.table-name:PROCTRAN}") String tableName
    ) {
        this.dataSource = dataSource;
        this.tableReference = buildTableReference(schema, tableName);
    }

    @Override
    public int countByCriteria(TransactionQueryCriteria criteria) {
        String sql = """
                SELECT COUNT(*)
                FROM %s
                WHERE PROCTRAN_SORTCODE = ?
                  AND PROCTRAN_NUMBER = ?
                  AND PROCTRAN_DATE >= ?
                  AND PROCTRAN_DATE <= ?
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCommonCriteria(statement, criteria);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        } catch (SQLException exception) {
            throw new TransactionRepositoryException("Failed transaction-count lookup", exception);
        }
    }

    @Override
    public List<TransactionRow> findByCriteria(TransactionQueryCriteria criteria) {
        String sql = """
                SELECT
                    PROCTRAN_SORTCODE,
                    PROCTRAN_NUMBER,
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
                ORDER BY PROCTRAN_DATE DESC, PROCTRAN_TIME DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCommonCriteria(statement, criteria);
            statement.setInt(5, criteria.offset());
            statement.setInt(6, criteria.limit());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<TransactionRow> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(new TransactionRow(
                            trim(resultSet.getString("PROCTRAN_SORTCODE")),
                            trim(resultSet.getString("PROCTRAN_NUMBER")),
                            trim(resultSet.getString("PROCTRAN_DATE")),
                            trim(resultSet.getString("PROCTRAN_TIME")),
                            trim(resultSet.getString("PROCTRAN_REF")),
                            trim(resultSet.getString("PROCTRAN_TYPE")),
                            trim(resultSet.getString("PROCTRAN_DESC")),
                            defaultAmount(resultSet.getBigDecimal("PROCTRAN_AMOUNT"))
                    ));
                }
                return rows;
            }
        } catch (SQLException exception) {
            throw new TransactionRepositoryException("Failed transaction-row lookup", exception);
        }
    }

    @Override
    public Optional<TransactionRow> findDetailByIdentity(
            String sortCode,
            String accountNumber,
            String date,
            String time,
            String reference
    ) {
        String sql = """
                SELECT
                    PROCTRAN_SORTCODE,
                    PROCTRAN_NUMBER,
                    PROCTRAN_DATE,
                    PROCTRAN_TIME,
                    PROCTRAN_REF,
                    PROCTRAN_TYPE,
                    PROCTRAN_DESC,
                    PROCTRAN_AMOUNT
                FROM %s
                WHERE PROCTRAN_SORTCODE = ?
                  AND PROCTRAN_NUMBER = ?
                  AND PROCTRAN_DATE = ?
                  AND PROCTRAN_TIME = ?
                  AND PROCTRAN_REF = ?
                """.formatted(tableReference);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sortCode);
            statement.setString(2, accountNumber);
            statement.setString(3, date);
            statement.setString(4, time);
            statement.setString(5, reference);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                TransactionRow firstRow = mapDetailRow(resultSet);
                if (resultSet.next()) {
                    throw new TransactionRepositoryException(
                            "Duplicate physical matches detected for transaction detail identity",
                            null
                    );
                }

                return Optional.of(firstRow);
            }
        } catch (SQLException exception) {
            throw new TransactionRepositoryException("Failed transaction-detail lookup", exception);
        }
    }

    private void bindCommonCriteria(PreparedStatement statement, TransactionQueryCriteria criteria) throws SQLException {
        statement.setString(1, criteria.sortCode());
        statement.setString(2, criteria.accountNumber());
        statement.setString(3, criteria.lowerDateBound());
        statement.setString(4, criteria.upperDateBound());
    }

    private String buildTableReference(String schema, String tableName) {
        String safeTableName = validateIdentifier(tableName, "app.inqtran.db.table-name");
        if (schema == null || schema.isBlank()) {
            return safeTableName;
        }

        String safeSchema = validateIdentifier(schema, "app.inqtran.db.schema");
        return safeSchema + "." + safeTableName;
    }

    private TransactionRow mapDetailRow(ResultSet resultSet) throws SQLException {
        BigDecimal amount = resultSet.getBigDecimal("PROCTRAN_AMOUNT");
        if (amount == null) {
            throw new TransactionRepositoryException("Matched detail row contains null PROCTRAN_AMOUNT", null);
        }

        return new TransactionRow(
                requireColumn(resultSet.getString("PROCTRAN_SORTCODE"), "PROCTRAN_SORTCODE"),
                requireColumn(resultSet.getString("PROCTRAN_NUMBER"), "PROCTRAN_NUMBER"),
                requireColumn(resultSet.getString("PROCTRAN_DATE"), "PROCTRAN_DATE"),
                requireColumn(resultSet.getString("PROCTRAN_TIME"), "PROCTRAN_TIME"),
                requireColumn(resultSet.getString("PROCTRAN_REF"), "PROCTRAN_REF"),
                requireColumn(resultSet.getString("PROCTRAN_TYPE"), "PROCTRAN_TYPE"),
                requireColumn(resultSet.getString("PROCTRAN_DESC"), "PROCTRAN_DESC"),
                amount
        );
    }

    private String requireColumn(String value, String column) {
        if (value == null) {
            throw new TransactionRepositoryException("Matched detail row contains null " + column, null);
        }
        return value.stripTrailing();
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

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trim(String value) {
        return value == null ? "" : value.stripTrailing();
    }
}
