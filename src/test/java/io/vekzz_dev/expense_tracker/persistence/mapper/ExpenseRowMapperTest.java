package io.vekzz_dev.expense_tracker.persistence.mapper;

import io.vekzz_dev.expense_tracker.models.Expense;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Test
    void testMap_createsExpenseFromResultSet() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("description")).thenReturn("Coffee");
        when(resultSet.getLong("amount")).thenReturn(500L);
        when(resultSet.getString("created_at")).thenReturn("2024-01-15T10:30:00");
        when(resultSet.getString("updated_at")).thenReturn("2024-01-15T10:30:00");

        Expense expense = ExpenseRowMapper.map(resultSet);

        assertThat(expense.id()).isEqualTo(1L);
        assertThat(expense.description()).isEqualTo("Coffee");
        assertThat(expense.amount().getNumberStripped()).isEqualByComparingTo("5.00");
        assertThat(expense.amount().getCurrency().getCurrencyCode()).isEqualTo("USD");
        assertThat(expense.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        assertThat(expense.updatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }

    @Test
    void testMap_convertsStringToLocalDateTime() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(2L);
        when(resultSet.getString("description")).thenReturn("Lunch");
        when(resultSet.getLong("amount")).thenReturn(1200L);
        when(resultSet.getString("created_at")).thenReturn("2024-02-20T14:45:30");
        when(resultSet.getString("updated_at")).thenReturn("2024-02-20T14:45:30");

        Expense expense = ExpenseRowMapper.map(resultSet);

        assertThat(expense.createdAt()).isEqualTo(LocalDateTime.of(2024, 2, 20, 14, 45, 30));
        assertThat(expense.updatedAt()).isEqualTo(LocalDateTime.of(2024, 2, 20, 14, 45, 30));
    }

    @Test
    void testMap_convertsLongToMoney() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(3L);
        when(resultSet.getString("description")).thenReturn("Groceries");
        when(resultSet.getLong("amount")).thenReturn(2550L);
        when(resultSet.getString("created_at")).thenReturn("2024-03-10T08:00:00");
        when(resultSet.getString("updated_at")).thenReturn("2024-03-10T08:00:00");

        Expense expense = ExpenseRowMapper.map(resultSet);

        assertThat(expense.amount().getNumberStripped()).isEqualByComparingTo("25.50");
        assertThat(expense.amount().getCurrency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void testMap_handlesZeroAmount() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(4L);
        when(resultSet.getString("description")).thenReturn("Free item");
        when(resultSet.getLong("amount")).thenReturn(0L);
        when(resultSet.getString("created_at")).thenReturn("2024-04-01T12:00:00");
        when(resultSet.getString("updated_at")).thenReturn("2024-04-01T12:00:00");

        Expense expense = ExpenseRowMapper.map(resultSet);

        assertThat(expense.amount().getNumberStripped()).isEqualByComparingTo("0");
    }

    @Test
    void testMap_handlesLargeAmount() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(5L);
        when(resultSet.getString("description")).thenReturn("Rent");
        when(resultSet.getLong("amount")).thenReturn(150000L);
        when(resultSet.getString("created_at")).thenReturn("2024-05-01T00:00:00");
        when(resultSet.getString("updated_at")).thenReturn("2024-05-01T00:00:00");

        Expense expense = ExpenseRowMapper.map(resultSet);

        assertThat(expense.amount().getNumberStripped()).isEqualByComparingTo("1500.00");
    }

    @Test
    void testMap_throwsSQLException_whenResultSetThrows() throws SQLException {
        when(resultSet.getLong("id")).thenThrow(new SQLException("Database error"));

        assertThatThrownBy(() -> ExpenseRowMapper.map(resultSet))
                .isInstanceOf(SQLException.class)
                .hasMessage("Database error");
    }

    @Test
    void testMap_handlesInvalidDateTimeFormat() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(6L);
        when(resultSet.getString("description")).thenReturn("Test");
        when(resultSet.getLong("amount")).thenReturn(100L);
        when(resultSet.getString("created_at")).thenReturn("invalid-date");

        assertThatThrownBy(() -> ExpenseRowMapper.map(resultSet))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }
}
