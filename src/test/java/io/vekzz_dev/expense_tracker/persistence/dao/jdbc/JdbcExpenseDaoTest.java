package io.vekzz_dev.expense_tracker.persistence.dao.jdbc;

import io.vekzz_dev.expense_tracker.models.Expense;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionalOperation;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcExpenseDaoTest {

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
    void testSave_insertsExpenseAndReturnsId() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense = new Expense(
                0L,
                "Coffee",
                Money.of(5.00, "USD"),
                now,
                now
        );

        Long id = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.save(expense);
        });

        assertThat(id).isGreaterThan(0);

        Long count = transactionManager.execute(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM expenses")) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        });

        assertThat(count).isEqualTo(1);
    }

    @Test
    void testSave_assignsDifferentIdsToMultipleExpenses() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense1 = new Expense(0L, "Coffee", Money.of(5.00, "USD"), now, now);
        Expense expense2 = new Expense(0L, "Lunch", Money.of(12.50, "USD"), now, now);

        Long id1 = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.save(expense1);
        });

        Long id2 = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.save(expense2);
        });

        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).isLessThan(id2);
    }

    @Test
    void testSave_storesCorrectValues() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense = new Expense(
                0L,
                "Groceries",
                Money.of(25.99, "USD"),
                now,
                now
        );

        Long id = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.save(expense);
        });

        String description = transactionManager.execute(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT description FROM expenses WHERE id = " + id)) {
                return rs.next() ? rs.getString("description") : null;
            }
        });

        assertThat(description).isEqualTo("Groceries");
    }

    @Test
    void testFindById_returnsExpense_whenExists() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense = new Expense(0L, "Coffee", Money.of(5.00, "USD"), now, now);

        Long id = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.save(expense);
        });

        Optional<Expense> found = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findById(id);
        });

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(id);
        assertThat(found.get().description()).isEqualTo("Coffee");
        assertThat(found.get().amount().getNumberStripped()).isEqualByComparingTo("5.00");
    }

    @Test
    void testFindById_returnsOptionalEmpty_whenNotFound() {
        Optional<Expense> found = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findById(9999L);
        });

        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll_returnsAllExpenses() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense1 = new Expense(0L, "Coffee", Money.of(5.00, "USD"), now, now);
        Expense expense2 = new Expense(0L, "Lunch", Money.of(12.50, "USD"), now, now);

        transactionManager.execute((TransactionalOperation<Void>) conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            dao.save(expense1);
            dao.save(expense2);
            return null;
        });

        List<Expense> expenses = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findAll();
        });

        assertThat(expenses).hasSize(2);
        assertThat(expenses).extracting("description").containsExactlyInAnyOrder("Coffee", "Lunch");
    }

    @Test
    void testFindAll_returnsEmptyList_whenNoExpenses() {
        List<Expense> expenses = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findAll();
        });

        assertThat(expenses).isEmpty();
    }

    @Test
    void testUpdate_modifiesExistingExpense() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense = new Expense(0L, "Coffee", Money.of(5.00, "USD"), now, now);

        Long id = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.save(expense);
        });

        LocalDateTime updatedAt = LocalDateTime.now();
        Expense updated = new Expense(id, "Espresso", Money.of(4.50, "USD"), now, updatedAt);

        Boolean result = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.update(updated);
        });

        assertThat(result).isTrue();

        Optional<Expense> found = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findById(id);
        });

        assertThat(found).isPresent();
        assertThat(found.get().description()).isEqualTo("Espresso");
        assertThat(found.get().amount().getNumberStripped()).isEqualByComparingTo("4.50");
    }

    @Test
    void testDelete_removesExpense() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense = new Expense(0L, "Coffee", Money.of(5.00, "USD"), now, now);

        Long id = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.save(expense);
        });

        Boolean result = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.delete(id);
        });

        assertThat(result).isTrue();

        Optional<Expense> found = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findById(id);
        });

        assertThat(found).isEmpty();
    }

    @Test
    void testFullCRUD_cycle() {
        LocalDateTime now = LocalDateTime.now();

        Long id = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            Expense expense = new Expense(0L, "Coffee", Money.of(5.00, "USD"), now, now);
            return dao.save(expense);
        });

        Optional<Expense> found = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findById(id);
        });
        assertThat(found).isPresent();

        Boolean updated = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            Expense expense = new Expense(id, "Latte", Money.of(6.50, "USD"), now, LocalDateTime.now());
            return dao.update(expense);
        });
        assertThat(updated).isTrue();

        Optional<Expense> afterUpdate = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findById(id);
        });
        assertThat(afterUpdate.get().description()).isEqualTo("Latte");

        Boolean deleted = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.delete(id);
        });
        assertThat(deleted).isTrue();

        Optional<Expense> afterDelete = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.findById(id);
        });
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void testSave_throwsDataAccessException_onDatabaseError() {
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("linux") ||
                        System.getProperty("os.name").toLowerCase().contains("mac"),
                "PosixFilePermissions only supported on Unix-like systems");

        Path readOnlyDir = tempDir.resolve("readonly");
        try {
            java.nio.file.Files.createDirectory(readOnlyDir);
            java.nio.file.Files.setPosixFilePermissions(readOnlyDir, java.util.Set.of(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ
            ));
        } catch (java.io.IOException e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Failed to setup test directory");
        }

        Path invalidPath = readOnlyDir.resolve("subdir").resolve("file.db");
        DatabaseManager.setDbPath(invalidPath);

        LocalDateTime now = LocalDateTime.now();
        Expense expense = new Expense(0L, "Coffee", Money.of(5.00, "USD"), now, now);

        Throwable thrown = catchThrowable(() -> {
            transactionManager.execute((TransactionalOperation<Void>) conn -> {
                JdbcExpenseDao dao = new JdbcExpenseDao(conn);
                dao.save(expense);
                return null;
            });
        });

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.TransactionException.class);
    }

    @Test
    void testUpdate_returnsFalse_whenNotFound() {
        LocalDateTime now = LocalDateTime.now();
        Expense expense = new Expense(9999L, "Coffee", Money.of(5.00, "USD"), now, now);

        Boolean result = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.update(expense);
        });

        assertThat(result).isFalse();
    }

    @Test
    void testDelete_returnsFalse_whenNotFound() {
        Boolean result = transactionManager.execute(conn -> {
            JdbcExpenseDao dao = new JdbcExpenseDao(conn);
            return dao.delete(9999L);
        });

        assertThat(result).isFalse();
    }
}
