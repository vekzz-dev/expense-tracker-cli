package io.vekzz_dev.expense_tracker.persistence.transaction;

import io.vekzz_dev.expense_tracker.exception.TransactionException;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TransactionManagerTest {

    @TempDir
    Path tempDir;

    private Path testDbPath;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        testDbPath = tempDir.resolve("test_expenses.db");
        DatabaseManager.setDbPath(testDbPath);
        transactionManager = new TransactionManager();
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.setDbPath(null);
    }

    @Test
    void testExecute_returnsResult_whenOperationSucceeds() {
        TransactionalOperation<String> operation = conn -> "success";

        String result = transactionManager.execute(operation);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void testExecute_commitsTransaction_whenOperationSucceeds() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY)");
        }

        TransactionalOperation<Void> operation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO test_table DEFAULT VALUES");
            }
            return null;
        };

        transactionManager.execute(operation);

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_table")) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
        }
    }

    @Test
    void testExecute_rollsBack_onSQLException() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY)");
        }

        TransactionalOperation<Void> operation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO test_table DEFAULT VALUES");
                throw new SQLException("Intentional error");
            }
        };

        Throwable thrown = catchThrowable(() -> transactionManager.execute(operation));

        assertThat(thrown).isInstanceOf(TransactionException.class)
                .hasMessageContaining("Failed to execute transaction");

        try (Connection conn = DatabaseManager.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_table")) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(0);
        }
    }

    @Test
    void testExecute_rollsBack_onRuntimeException() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY)");
        }

        TransactionalOperation<Void> operation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO test_table DEFAULT VALUES");
            }
            throw new RuntimeException("Intentional runtime error");
        };

        Throwable thrown = catchThrowable(() -> transactionManager.execute(operation));

        assertThat(thrown).isInstanceOf(TransactionException.class)
                .hasMessageContaining("Unexpected runtime error during transaction");

        try (Connection conn = DatabaseManager.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_table")) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(0);
        }
    }

    @Test
    void testExecute_throwsTransactionException_onConnectionFailure() {
        Path invalidPath = Path.of("/invalid/nonexistent/path/expenses.db");
        DatabaseManager.setDbPath(invalidPath);

        TransactionalOperation<Void> operation = conn -> null;

        Throwable thrown = catchThrowable(() -> transactionManager.execute(operation));

        assertThat(thrown).isInstanceOf(TransactionException.class)
                .hasMessageContaining("Could not open transaction");
    }

    @Test
    void testExecute_restoresAutoCommit_afterTransaction() throws SQLException {
        TransactionalOperation<Void> operation = conn -> {
            assertThat(conn.getAutoCommit()).isFalse();
            return null;
        };

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(true);
            transactionManager.execute(operation);

            assertThat(conn.getAutoCommit()).isTrue();
        }
    }

    @Test
    void testExecute_restoresFalseAutoCommit_afterTransaction() throws SQLException {
        TransactionalOperation<Void> operation = conn -> {
            assertThat(conn.getAutoCommit()).isFalse();
            return null;
        };

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            transactionManager.execute(operation);

            assertThat(conn.getAutoCommit()).isFalse();
        }
    }

    @Test
    void testExecute_preservesFalseAutoCommit_onRollback() throws SQLException {
        TransactionalOperation<Void> operation = conn -> {
            throw new SQLException("Test error");
        };

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            catchThrowable(() -> transactionManager.execute(operation));

            assertThat(conn.getAutoCommit()).isFalse();
        }
    }

    @Test
    void testExecuteVoid_executesOperation_whenSucceeds() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY)");
        }

        VoidTransactionalOperation operation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO test_table DEFAULT VALUES");
            }
        };

        transactionManager.executeVoid(operation);

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_table")) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
        }
    }

    @Test
    void testExecuteVoid_rollsBack_onSQLException() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY)");
        }

        VoidTransactionalOperation operation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO test_table DEFAULT VALUES");
                throw new SQLException("Intentional error");
            }
        };

        Throwable thrown = catchThrowable(() -> transactionManager.executeVoid(operation));

        assertThat(thrown).isInstanceOf(TransactionException.class)
                .hasMessageContaining("Failed to execute transaction");

        try (Connection conn = DatabaseManager.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_table")) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(0);
        }
    }

    @Test
    void testExecuteVoid_throwsTransactionException_onConnectionFailure() {
        Path invalidPath = Path.of("/invalid/nonexistent/path/expenses.db");
        DatabaseManager.setDbPath(invalidPath);

        VoidTransactionalOperation operation = conn -> {};

        Throwable thrown = catchThrowable(() -> transactionManager.executeVoid(operation));

        assertThat(thrown).isInstanceOf(TransactionException.class)
                .hasMessageContaining("Could not open transaction");
    }
}
