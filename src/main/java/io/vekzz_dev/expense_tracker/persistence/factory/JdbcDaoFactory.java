package io.vekzz_dev.expense_tracker.persistence.factory;

import io.vekzz_dev.expense_tracker.persistence.dao.ExpenseDao;
import io.vekzz_dev.expense_tracker.persistence.dao.jdbc.JdbcExpenseDao;

import java.sql.Connection;

public class JdbcDaoFactory implements DaoFactory {

    private final Connection conn;

    public JdbcDaoFactory(Connection conn) {
        this.conn = conn;
    }

    @Override
    public ExpenseDao expenseDao() {
        return new JdbcExpenseDao(conn);
    }
}
