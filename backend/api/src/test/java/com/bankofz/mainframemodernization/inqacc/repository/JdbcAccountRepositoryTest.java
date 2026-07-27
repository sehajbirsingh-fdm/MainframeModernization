package com.bankofz.mainframemodernization.inqacc.repository;

import com.bankofz.mainframemodernization.inqacc.domain.AccountRecord;
import com.bankofz.mainframemodernization.inqacc.exception.RepositoryUnavailableException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcAccountRepositoryTest {

    @Test
    void shouldReturnRecordForSortcodeAndAccountNumber() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        stubRow(resultSet, "543210", "12345678");

        JdbcAccountRepository repository = new JdbcAccountRepository(dataSource, "", "ACCOUNT");

        Optional<AccountRecord> result = repository.findBySortcodeAndAccountNumber("543210", "12345678");

        assertThat(result).isPresent();
        assertThat(result.get().accountNumber()).isEqualTo("12345678");
        verify(preparedStatement).setString(1, "543210");
        verify(preparedStatement).setString(2, "12345678");
    }

    @Test
    void shouldThrowRepositoryUnavailableOnSqlFailure() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("db offline"));

        JdbcAccountRepository repository = new JdbcAccountRepository(dataSource, "", "ACCOUNT");

        assertThatThrownBy(() -> repository.findBySortcodeAndAccountNumber("543210", "12345678"))
                .isInstanceOf(RepositoryUnavailableException.class)
                .hasMessage("Failed JDBC account lookup");
    }

    @Test
    void shouldRejectUnsafeIdentifiers() {
        DataSource dataSource = mock(DataSource.class);

        assertThatThrownBy(() -> new JdbcAccountRepository(dataSource, "public;drop", "ACCOUNT"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.inqacc.db.schema");

        assertThatThrownBy(() -> new JdbcAccountRepository(dataSource, "", "ACCOUNT;DROP"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.inqacc.db.table-name");
    }

    private void stubRow(ResultSet resultSet, String sortcode, String accountNumber) throws SQLException {
        when(resultSet.getString(eq("ACCOUNT_EYECATCHER"))).thenReturn("ACCOUNT");
        when(resultSet.getString(eq("ACCOUNT_CUSTOMER_NUMBER"))).thenReturn("1000000001");
        when(resultSet.getString(eq("ACCOUNT_SORTCODE"))).thenReturn(sortcode);
        when(resultSet.getString(eq("ACCOUNT_NUMBER"))).thenReturn(accountNumber);
        when(resultSet.getString(eq("ACCOUNT_TYPE"))).thenReturn("CHK");
        when(resultSet.getBigDecimal(eq("ACCOUNT_INTEREST_RATE"))).thenReturn(new java.math.BigDecimal("1.25"));
        when(resultSet.getObject(eq("ACCOUNT_OPENED"))).thenReturn(Date.valueOf("2023-01-10"));
        when(resultSet.getInt(eq("ACCOUNT_OVERDRAFT_LIMIT"))).thenReturn(1500);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getObject(eq("ACCOUNT_LAST_STATEMENT"))).thenReturn(20240401);
        when(resultSet.getObject(eq("ACCOUNT_NEXT_STATEMENT"))).thenReturn(20240501);
        when(resultSet.getBigDecimal(eq("ACCOUNT_AVAILABLE_BALANCE"))).thenReturn(new java.math.BigDecimal("1800.00"));
        when(resultSet.getBigDecimal(eq("ACCOUNT_ACTUAL_BALANCE"))).thenReturn(new java.math.BigDecimal("1700.00"));
    }
}
