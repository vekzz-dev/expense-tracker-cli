package io.vekzz_dev.expense_tracker.service;

import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import io.vekzz_dev.expense_tracker.persistence.factory.JdbcDaoFactory;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseFilterServiceTest {

    @TempDir
    Path tempDir;

    private TransactionManager tx;
    private ExpenseFilterService filterService;

    @BeforeEach
    void setUp() {
        var testDbPath = tempDir.resolve("test_expenses.db");
        DatabaseManager.setDbPath(testDbPath);
        DatabaseSetup.initialize();
        tx = new TransactionManager();
        filterService = new ExpenseFilterService(tx, JdbcDaoFactory::new);
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.setDbPath(null);
    }

    private void insertExpense(String description, double amount, LocalDateTime createdAt) {
        tx.execute(conn -> {
            var dao = new JdbcDaoFactory(conn).expenseDao();
            var expense = new Expense(0, description, Money.of(amount, "USD"), createdAt, createdAt);
            return dao.insert(expense);
        });
    }

    @Test
    void testFilterByDay_returnsMatchingExpenses() {
        var targetDate = LocalDate.of(2024, 2, 15);
        var dateTime = targetDate.atTime(10, 30);

        insertExpense("Coffee", 5.00, dateTime);
        insertExpense("Lunch", 15.00, dateTime.plusHours(2));
        insertExpense("Other Day", 10.00, targetDate.plusDays(1).atTime(10, 0));

        List<Expense> result = filterService.filterByDay(targetDate);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("description").containsExactlyInAnyOrder("Coffee", "Lunch");
    }

    @Test
    void testFilterByDay_returnsEmptyList_noMatch() {
        var targetDate = LocalDate.of(2024, 2, 15);
        insertExpense("Coffee", 5.00, targetDate.plusDays(5).atTime(10, 0));

        List<Expense> result = filterService.filterByDay(targetDate);

        assertThat(result).isEmpty();
    }

    @Test
    void testFilterByDay_returnsEmptyList_emptyDatabase() {
        List<Expense> result = filterService.filterByDay(LocalDate.of(2024, 2, 15));

        assertThat(result).isEmpty();
    }

    @Test
    void testFilterByDay_multipleExpensesSameDay() {
        var targetDate = LocalDate.of(2024, 2, 15);

        insertExpense("Morning", 5.00, targetDate.atTime(6, 0));
        insertExpense("Afternoon", 10.00, targetDate.atTime(14, 0));
        insertExpense("Evening", 15.00, targetDate.atTime(20, 0));

        List<Expense> result = filterService.filterByDay(targetDate);

        assertThat(result).hasSize(3);
    }

    @Test
    void testFilterByDay_expensesAtDayBoundary() {
        var targetDate = LocalDate.of(2024, 2, 15);

        insertExpense("StartOfDay", 5.00, targetDate.atStartOfDay());
        insertExpense("EndOfDay", 10.00, targetDate.atTime(23, 59, 59));

        List<Expense> result = filterService.filterByDay(targetDate);

        assertThat(result).hasSize(2);
    }

    @Test
    void testFilterByMonth_returnsMatchingExpenses() {
        var targetMonth = YearMonth.of(2024, 2);

        insertExpense("Feb1", 5.00, LocalDate.of(2024, 2, 1).atTime(10, 0));
        insertExpense("Feb15", 10.00, LocalDate.of(2024, 2, 15).atTime(10, 0));
        insertExpense("Feb29", 15.00, LocalDate.of(2024, 2, 29).atTime(10, 0));
        insertExpense("Jan", 20.00, LocalDate.of(2024, 1, 15).atTime(10, 0));
        insertExpense("Mar", 25.00, LocalDate.of(2024, 3, 15).atTime(10, 0));

        List<Expense> result = filterService.filterByMonth(targetMonth);

        assertThat(result).hasSize(3);
        assertThat(result).extracting("description").containsExactlyInAnyOrder("Feb1", "Feb15", "Feb29");
    }

    @Test
    void testFilterByMonth_returnsEmptyList_noMatch() {
        var targetMonth = YearMonth.of(2024, 2);
        insertExpense("Jan", 5.00, LocalDate.of(2024, 1, 15).atTime(10, 0));

        List<Expense> result = filterService.filterByMonth(targetMonth);

        assertThat(result).isEmpty();
    }

    @Test
    void testFilterByMonth_crossMonthBoundaries() {
        insertExpense("Jan", 5.00, LocalDate.of(2024, 1, 31).atTime(23, 59));
        insertExpense("Feb", 10.00, LocalDate.of(2024, 2, 1).atStartOfDay());
        insertExpense("Mar", 15.00, LocalDate.of(2024, 3, 1).atStartOfDay());

        List<Expense> febResult = filterService.filterByMonth(YearMonth.of(2024, 2));
        List<Expense> janResult = filterService.filterByMonth(YearMonth.of(2024, 1));

        assertThat(febResult).hasSize(1);
        assertThat(janResult).hasSize(1);
    }

    @Test
    void testFilterByYear_returnsMatchingExpenses() {
        insertExpense("Y2023", 5.00, LocalDate.of(2023, 6, 15).atTime(10, 0));
        insertExpense("Y2024_Jan", 10.00, LocalDate.of(2024, 1, 15).atTime(10, 0));
        insertExpense("Y2024_Dec", 15.00, LocalDate.of(2024, 12, 31).atTime(10, 0));
        insertExpense("Y2025", 20.00, LocalDate.of(2025, 1, 1).atTime(10, 0));

        List<Expense> result = filterService.filterByYear(Year.of(2024));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("description").containsExactlyInAnyOrder("Y2024_Jan", "Y2024_Dec");
    }

    @Test
    void testFilterByYear_returnsEmptyList_noMatch() {
        insertExpense("Y2023", 5.00, LocalDate.of(2023, 6, 15).atTime(10, 0));

        List<Expense> result = filterService.filterByYear(Year.of(2024));

        assertThat(result).isEmpty();
    }

    @Test
    void testFilterByYear_crossYearBoundaries() {
        insertExpense("Dec31", 5.00, LocalDate.of(2023, 12, 31).atTime(23, 59));
        insertExpense("Jan1", 10.00, LocalDate.of(2024, 1, 1).atStartOfDay());

        List<Expense> result2023 = filterService.filterByYear(Year.of(2023));
        List<Expense> result2024 = filterService.filterByYear(Year.of(2024));

        assertThat(result2023).hasSize(1);
        assertThat(result2024).hasSize(1);
    }

    @Test
    void testFilterByCustomRange_returnsExpensesInRange() {
        var startDate = LocalDate.of(2024, 2, 10);
        var endDate = LocalDate.of(2024, 2, 15);

        insertExpense("Before", 5.00, LocalDate.of(2024, 2, 5).atTime(10, 0));
        insertExpense("Start", 10.00, startDate.atTime(10, 0));
        insertExpense("Middle", 15.00, LocalDate.of(2024, 2, 12).atTime(10, 0));
        insertExpense("End", 20.00, endDate.atTime(10, 0));
        insertExpense("After", 25.00, LocalDate.of(2024, 2, 20).atTime(10, 0));

        List<Expense> result = filterService.filterByCustomRange(startDate, endDate);

        assertThat(result).hasSize(3);
        assertThat(result).extracting("description").containsExactlyInAnyOrder("Start", "Middle", "End");
    }

    @Test
    void testFilterByCustomRange_includesBoundaryDates() {
        var startDate = LocalDate.of(2024, 2, 10);
        var endDate = LocalDate.of(2024, 2, 10);

        insertExpense("ExactDate", 10.00, startDate.atTime(12, 0));

        List<Expense> result = filterService.filterByCustomRange(startDate, endDate);

        assertThat(result).hasSize(1);
    }

    @Test
    void testFilterByCustomRange_singleDayRange() {
        var date = LocalDate.of(2024, 2, 15);

        insertExpense("Target", 10.00, date.atTime(10, 0));
        insertExpense("Before", 5.00, date.minusDays(1).atTime(10, 0));
        insertExpense("After", 15.00, date.plusDays(1).atTime(10, 0));

        List<Expense> result = filterService.filterByCustomRange(date, date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Target");
    }

    @Test
    void testFilterByCustomRange_returnsEmptyList_noMatch() {
        var startDate = LocalDate.of(2024, 2, 10);
        var endDate = LocalDate.of(2024, 2, 15);

        insertExpense("Before", 5.00, LocalDate.of(2024, 1, 15).atTime(10, 0));
        insertExpense("After", 10.00, LocalDate.of(2024, 3, 15).atTime(10, 0));

        List<Expense> result = filterService.filterByCustomRange(startDate, endDate);

        assertThat(result).isEmpty();
    }

    @Test
    void testFilterByCustomRange_largeDateRange() {
        var startDate = LocalDate.of(2020, 1, 1);
        var endDate = LocalDate.of(2024, 12, 31);

        insertExpense("In2020", 5.00, LocalDate.of(2020, 6, 15).atTime(10, 0));
        insertExpense("In2022", 10.00, LocalDate.of(2022, 6, 15).atTime(10, 0));
        insertExpense("In2024", 15.00, LocalDate.of(2024, 6, 15).atTime(10, 0));
        insertExpense("Before2019", 20.00, LocalDate.of(2019, 12, 31).atTime(10, 0));
        insertExpense("After2025", 25.00, LocalDate.of(2025, 1, 1).atTime(10, 0));

        List<Expense> result = filterService.filterByCustomRange(startDate, endDate);

        assertThat(result).hasSize(3);
    }

    @Test
    void testFilterByDay_leapYear_feb29() {
        var leapYearDate = LocalDate.of(2024, 2, 29);

        insertExpense("Feb29_2024", 10.00, leapYearDate.atTime(10, 0));
        insertExpense("Feb28_2024", 5.00, LocalDate.of(2024, 2, 28).atTime(10, 0));

        List<Expense> result = filterService.filterByDay(leapYearDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Feb29_2024");
    }
}
