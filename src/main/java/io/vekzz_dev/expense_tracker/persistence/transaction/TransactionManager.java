package io.vekzz_dev.expense_tracker.persistence.transaction;

import io.vekzz_dev.expense_tracker.exception.DomainException;
import io.vekzz_dev.expense_tracker.exception.InfrastructureException;
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

            } catch (DomainException | InfrastructureException e) {
                conn.rollback();
                throw e;

            } catch (SQLException e) {
                conn.rollback();
                throw new TransactionException("Failed to execute transaction", e);

            } catch (RuntimeException e) {
                conn.rollback();
                throw new TransactionException("Unexpected runtime error during transaction", e);

            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new TransactionException("Could not open transaction", e);
        }
    }

    public void executeVoid(VoidTransactionalOperation operation) {
        execute(conn -> {
            operation.execute(conn);
            return null;
        });
    }
}
