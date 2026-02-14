package io.vekzz_dev.expense_tracker.persistence.mapper;

import io.vekzz_dev.expense_tracker.model.Expense;
import org.javamoney.moneta.Money;

import javax.money.Monetary;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ExpenseRowMapper {

    public static Expense map(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getLong("id"),
                rs.getString("description"),
                Money.ofMinor(Monetary.getCurrency("USD"), rs.getLong("amount")),
                LocalDateTime.parse(rs.getString("created_at")),
                LocalDateTime.parse(rs.getString("updated_at"))
        );
    }
}
