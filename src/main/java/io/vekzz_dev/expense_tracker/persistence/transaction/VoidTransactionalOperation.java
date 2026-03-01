package io.vekzz_dev.expense_tracker.persistence.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface VoidTransactionalOperation {

    void execute(Connection conn) throws SQLException;
}
