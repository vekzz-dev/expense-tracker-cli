package io.vekzz_dev.expense_tracker.persistence.transaction;

import io.vekzz_dev.expense_tracker.exception.TransactionException;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    public <T> T execute(TransactionalOperation<T> operation) {
        try (Connection conn = DatabaseManager.getConnection()) {

            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                T result = operation.execute(conn);
                conn.commit();
                return result;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw new TransactionException("Transaction failed", e);

            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new TransactionException("Could not open transaction", e);
        }
    }
}
