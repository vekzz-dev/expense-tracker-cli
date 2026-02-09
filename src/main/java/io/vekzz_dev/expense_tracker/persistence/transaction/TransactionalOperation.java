package io.vekzz_dev.expense_tracker.persistence.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionalOperation<T> {

    T execute(Connection conn) throws SQLException;
}
