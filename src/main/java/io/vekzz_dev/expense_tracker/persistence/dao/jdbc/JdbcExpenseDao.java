package io.vekzz_dev.expense_tracker.persistence.dao.jdbc;

import io.vekzz_dev.expense_tracker.exception.DataAccessException;
import io.vekzz_dev.expense_tracker.models.Expense;
import io.vekzz_dev.expense_tracker.persistence.dao.ExpenseDao;
import io.vekzz_dev.expense_tracker.persistence.mapper.ExpenseRowMapper;
import io.vekzz_dev.expense_tracker.util.MoneyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExpenseDao implements ExpenseDao {

    private final Connection conn;

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcExpenseDao.class);

    public JdbcExpenseDao(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Optional<Expense> findById(long id) {
        String sql = "SELECT * FROM expenses WHERE id = ?";

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next()
                        ? Optional.of(ExpenseRowMapper.map(rs))
                        : Optional.empty();
            }

        } catch (SQLException e) {
            LOGGER.error("Error while finding expense with id={}", id, e);
            throw new DataAccessException("Failed to retrieve expense with id=" + id, e);
        }
    }

    @Override
    public List<Expense> findAll() {
        String sql = "SELECT * FROM expenses";
        List<Expense> expenses = new ArrayList<>();

        try (var stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                expenses.add(ExpenseRowMapper.map(rs));
            }
            return expenses;

        } catch (SQLException e) {
            LOGGER.error("Error while finding all expenses", e);
            throw new DataAccessException("Failed to retrieve all expenses", e);
        }
    }

    @Override
    public long save(Expense expense) {
        String sql = "INSERT INTO expenses (description, amount, created_at, updated_at) VALUES (?, ?, ?, ?)";

        try (var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, expense.description());
            stmt.setLong(2, MoneyMapper.toMinor(expense.amount()));
            stmt.setString(3, expense.createdAt().toString());
            stmt.setString(4, expense.updatedAt().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new DataAccessException("Saving expenses failed, no rows affected");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new DataAccessException("Saving expenses failed, no ID obtained");
            }

        } catch (SQLException e) {
            LOGGER.error("Error while saving expense", e);
            throw new DataAccessException("Failed to save expense", e);
        }
    }

    @Override
    public void update(Expense expense) {
        String sql = "UPDATE expenses SET description = ?, amount = ?, updated_at = ?  WHERE id = ?";

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, expense.description());
            stmt.setLong(2, MoneyMapper.toMinor(expense.amount()));
            stmt.setString(3, expense.updatedAt().toString());
            stmt.setLong(4, expense.id());

            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error("Error while updating expense with id={}", expense.id(), e);
            throw new DataAccessException("Failed to update expense", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error("Error while deleting expense with id={}", id, e);
            throw new DataAccessException("Failed to delete expense", e);
        }
    }
}
