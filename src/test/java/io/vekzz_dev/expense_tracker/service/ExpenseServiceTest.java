package io.vekzz_dev.expense_tracker.service;

import io.vekzz_dev.expense_tracker.exception.ExpenseAddingFailedException;
import io.vekzz_dev.expense_tracker.exception.ExpenseNotFoundException;
import io.vekzz_dev.expense_tracker.exception.InvalidPeriodTypeException;
import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import io.vekzz_dev.expense_tracker.persistence.factory.DaoFactory;
import io.vekzz_dev.expense_tracker.persistence.factory.JdbcDaoFactory;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ExpenseServiceTest {

    @TempDir
    Path tempDir;

    private TransactionManager tx;
    private ExpenseService expenseService;
    private ExpenseFilterService filterService;
    private ExpenseStatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        var testDbPath = tempDir.resolve("test_expenses.db");
        DatabaseManager.setDbPath(testDbPath);
        DatabaseSetup.initialize();
        tx = new TransactionManager();
        filterService = new ExpenseFilterService(tx, JdbcDaoFactory::new);
        statisticsService = new ExpenseStatisticsService();
        expenseService = new ExpenseService(tx, JdbcDaoFactory::new, filterService, statisticsService);
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.setDbPath(null);
    }

    private Expense insertTestExpense(String description, double amount) {
        return expenseService.add(description, Money.of(amount, "USD"));
    }

    @Test
    void testGetAll_returnsAllExpenses() {
        insertTestExpense("Coffee", 5.00);
        insertTestExpense("Lunch", 15.00);

        List<Expense> result = expenseService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void testGetAll_returnsEmptyList_noExpenses() {
        List<Expense> result = expenseService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetById_returnsExpense_whenExists() {
        var added = insertTestExpense("Coffee", 5.00);

        var result = expenseService.getById(added.id());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(added.id());
        assertThat(result.description()).isEqualTo("Coffee");
    }

    @Test
    void testGetById_throwsExpenseNotFoundException_whenNotFound() {
        var thrown = catchThrowable(() -> expenseService.getById(9999L));

        assertThat(thrown).isInstanceOf(ExpenseNotFoundException.class);
        assertThat(thrown.getMessage()).contains("9999");
    }

    @Test
    void testAdd_createsAndReturnsExpense() {
        var result = expenseService.add("Lunch", Money.of(12.50, "USD"));

        assertThat(result).isNotNull();
        assertThat(result.id()).isGreaterThan(0);
        assertThat(result.description()).isEqualTo("Lunch");
        assertThat(result.amount().getNumberStripped()).isEqualByComparingTo("12.50");
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    void testAdd_throwsExpenseAddingFailedException_whenInsertReturnsInvalidId() {
        Function<Connection, DaoFactory> failingFactory = conn -> new DaoFactory() {
            @Override
            public io.vekzz_dev.expense_tracker.persistence.dao.ExpenseDao expenseDao() {
                return new io.vekzz_dev.expense_tracker.persistence.dao.ExpenseDao() {
                    @Override
                    public long insert(Expense expense) {
                        return 0;
                    }

                    @Override
                    public Optional<Expense> findById(long id) {
                        return Optional.empty();
                    }

                    @Override
                    public List<Expense> findAll() {
                        return List.of();
                    }

                    @Override
                    public boolean update(Expense expense) {
                        return false;
                    }

                    @Override
                    public boolean delete(long id) {
                        return false;
                    }
                };
            }
        };

        var failingService = new ExpenseService(tx, failingFactory, filterService, statisticsService);

        var thrown = catchThrowable(() -> failingService.add("Test", Money.of(10, "USD")));

        assertThat(thrown).isInstanceOf(ExpenseAddingFailedException.class);
    }

    @Test
    void testUpdate_updatesExistingExpense() {
        var original = insertTestExpense("Coffee", 5.00);

        var result = expenseService.update(original.id(), "Espresso", Money.of(4.50, "USD"));

        assertThat(result.description()).isEqualTo("Espresso");
        assertThat(result.amount().getNumberStripped()).isEqualByComparingTo("4.50");
        assertThat(result.updatedAt()).isAfterOrEqualTo(original.updatedAt());
    }

    @Test
    void testUpdate_throwsExpenseNotFoundException_whenNotFound() {
        var thrown = catchThrowable(() -> expenseService.update(9999L, "Test", Money.of(10, "USD")));

        assertThat(thrown).isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void testDelete_removesExpense() {
        var added = insertTestExpense("Coffee", 5.00);

        expenseService.delete(added.id());

        var thrown = catchThrowable(() -> expenseService.getById(added.id()));
        assertThat(thrown).isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void testDelete_throwsExpenseNotFoundException_whenNotFound() {
        var thrown = catchThrowable(() -> expenseService.delete(9999L));

        assertThat(thrown).isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void testGetDailySummary_returnsCorrectSummary() {
        var today = LocalDate.now();
        insertTestExpense("Coffee", 5.00);
        insertTestExpense("Lunch", 15.00);

        var result = expenseService.getDailySummary(today.toString());

        assertThat(result).isNotNull();
        assertThat(result.description()).contains(today.toString());
        assertThat(result.statistics().count()).isEqualTo(2);
        assertThat(result.statistics().total().getNumberStripped()).isEqualByComparingTo("20.00");
    }

    @Test
    void testGetDailySummary_returnsEmptyStatistics_noExpenses() {
        var date = LocalDate.of(2020, 1, 1);

        var result = expenseService.getDailySummary(date.toString());

        assertThat(result.statistics().count()).isEqualTo(0);
        assertThat(result.statistics().total().getNumberStripped()).isEqualByComparingTo("0");
    }

    @Test
    void testGetDailySummary_throwsException_invalidDateFormat() {
        var thrown = catchThrowable(() -> expenseService.getDailySummary("invalid-date"));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testGetMonthlySummary_returnsCorrectSummary() {
        var currentMonth = LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
        insertTestExpense("Item1", 10.00);
        insertTestExpense("Item2", 20.00);

        var result = expenseService.getMonthlySummary(currentMonth);

        assertThat(result).isNotNull();
        assertThat(result.statistics().count()).isEqualTo(2);
    }

    @Test
    void testGetMonthlySummary_returnsEmptyStatistics_noExpenses() {
        var result = expenseService.getMonthlySummary("2020-01");

        assertThat(result.statistics().count()).isEqualTo(0);
    }

    @Test
    void testGetYearlySummary_returnsCorrectSummary() {
        var currentYear = String.valueOf(LocalDate.now().getYear());
        insertTestExpense("Item1", 100.00);
        insertTestExpense("Item2", 200.00);

        var result = expenseService.getYearlySummary(currentYear);

        assertThat(result).isNotNull();
        assertThat(result.statistics().count()).isEqualTo(2);
        assertThat(result.statistics().total().getNumberStripped()).isEqualByComparingTo("300.00");
    }

    @Test
    void testGetYearlySummary_returnsEmptyStatistics_noExpenses() {
        var result = expenseService.getYearlySummary("2020");

        assertThat(result.statistics().count()).isEqualTo(0);
    }

    @Test
    void testGetLast7DaysSummary_withTodayKeyword() {
        insertTestExpense("Today", 10.00);

        var result = expenseService.getLast7DaysSummary(LocalDate.now().toString());

        assertThat(result).isNotNull();
        assertThat(result.description()).contains("last 7 days");
        assertThat(result.statistics().count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testGetLast7DaysSummary_withSpecificDate() {
        var date = LocalDate.of(2024, 2, 15);
        insertTestExpense("Item", 10.00);

        var result = expenseService.getLast7DaysSummary(date.toString());

        assertThat(result).isNotNull();
        assertThat(result.description()).contains("2024-02-09");
        assertThat(result.description()).contains("2024-02-15");
    }

    @Test
    void testGetLast7DaysSummary_throwsException_invalidDate() {
        var thrown = catchThrowable(() -> expenseService.getLast7DaysSummary("invalid"));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testGetGeneralSummary_returnsCorrectSummary() {
        insertTestExpense("Item1", 50.00);
        insertTestExpense("Item2", 75.00);

        var result = expenseService.getGeneralSummary();

        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("General summary");
        assertThat(result.statistics().count()).isEqualTo(2);
        assertThat(result.statistics().total().getNumberStripped()).isEqualByComparingTo("125.00");
    }

    @Test
    void testGetGeneralSummary_returnsEmptyStatistics_noExpenses() {
        var result = expenseService.getGeneralSummary();

        assertThat(result.statistics().count()).isEqualTo(0);
        assertThat(result.statistics().total().getNumberStripped()).isEqualByComparingTo("0");
    }
}
