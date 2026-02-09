package io.vekzz_dev.expense_tracker.persistence;

import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionalOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class PersistenceIntegrationTest {

    @TempDir
    Path tempDir;

    private Path testDbPath;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        testDbPath = tempDir.resolve("test_expenses.db");
        DatabaseManager.setDbPath(testDbPath);
        DatabaseSetup.initialize();
        transactionManager = new TransactionManager();
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.setDbPath(null);
    }

    @Test
    void testDatabaseSetup_andTransactionManager_workTogether() {
        TransactionalOperation<Integer> insertOperation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                return stmt.executeUpdate("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('coffee', 500, '2024-01-01T10:00:00Z', '2024-01-01T10:00:00Z')");
            }
        };

        Integer rowsAffected = transactionManager.execute(insertOperation);

        assertThat(rowsAffected).isEqualTo(1);
    }

    @Test
    void testTransactionManager_queriesDataInsertedByPreviousTransaction() {
        TransactionalOperation<Void> insertOperation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('lunch', 1200, '2024-01-01T12:00:00Z', '2024-01-01T12:00:00Z')");
                stmt.execute("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('dinner', 2500, '2024-01-01T19:00:00Z', '2024-01-01T19:00:00Z')");
            }
            return null;
        };

        transactionManager.execute(insertOperation);

        TransactionalOperation<Integer> countOperation = conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM expenses")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        };

        Integer count = transactionManager.execute(countOperation);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testMultipleTransactions_maintainDataConsistency() {
        TransactionalOperation<Void> insertOperation1 = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('item1', 100, '2024-01-01T10:00:00Z', '2024-01-01T10:00:00Z')");
            }
            return null;
        };

        TransactionalOperation<Void> insertOperation2 = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('item2', 200, '2024-01-01T11:00:00Z', '2024-01-01T11:00:00Z')");
            }
            return null;
        };

        transactionManager.execute(insertOperation1);
        transactionManager.execute(insertOperation2);

        TransactionalOperation<Integer> countOperation = conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM expenses")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        };

        Integer count = transactionManager.execute(countOperation);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFailedTransaction_doesNotAffectSuccessfulTransactions() {
        TransactionalOperation<Void> successfulOperation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('success', 999, '2024-01-01T10:00:00Z', '2024-01-01T10:00:00Z')");
            }
            return null;
        };

        TransactionalOperation<Void> failingOperation = conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('fail', 0, '2024-01-01T10:00:00Z', '2024-01-01T10:00:00Z')");
                throw new RuntimeException("Intentional failure");
            }
        };

        transactionManager.execute(successfulOperation);
        catchThrowable(() -> transactionManager.execute(failingOperation));

        TransactionalOperation<Integer> countOperation = conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM expenses")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        };

        Integer count = transactionManager.execute(countOperation);

        assertThat(count).isEqualTo(1);
    }
}
