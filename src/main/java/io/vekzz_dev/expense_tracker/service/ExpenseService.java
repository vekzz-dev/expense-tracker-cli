package io.vekzz_dev.expense_tracker.service;

import io.vekzz_dev.expense_tracker.exception.ExpenseAddingFailedException;
import io.vekzz_dev.expense_tracker.exception.ExpenseNotFoundException;
import io.vekzz_dev.expense_tracker.exception.ExpenseUpdateFailedException;
import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.model.ExpenseSummary;
import io.vekzz_dev.expense_tracker.persistence.factory.DaoFactory;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;
import io.vekzz_dev.expense_tracker.util.MoneyMapper;
import io.vekzz_dev.expense_tracker.util.PeriodValidator;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

public class ExpenseService {

    private final TransactionManager tx;
    private final Function<Connection, DaoFactory> factoryProvider;

    private final ExpenseFilterService expenseFilter;
    private final ExpenseStatisticsService expenseStatistics;

    public ExpenseService(TransactionManager tx, Function<Connection, DaoFactory> factoryProvider,
                          ExpenseFilterService expenseFilter, ExpenseStatisticsService expenseStatistics) {
        this.tx = tx;
        this.factoryProvider = factoryProvider;
        this.expenseFilter = expenseFilter;
        this.expenseStatistics = expenseStatistics;
    }

    public List<Expense> getAll() {
        return tx.execute(conn -> factoryProvider
                .apply(conn)
                .expenseDao()
                .findAll());
    }

    public Expense getById(long id) {
        var expense = tx.execute(conn -> factoryProvider
                .apply(conn)
                .expenseDao()
                .findById(id));

        return expense.orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public Expense add(String description, String amount) {
        var amountMoney = MoneyMapper.parseMoney(amount);
        var now = LocalDateTime.now();
        var expense = new Expense(0, description, amountMoney, now, now);

        long generatedId = tx.execute(conn -> factoryProvider
                .apply(conn)
                .expenseDao()
                .insert(expense));

        if (generatedId < 1) throw new ExpenseAddingFailedException();

        return expense.withId(generatedId);
    }

    public Expense update(long id, String description, String amount) {
        return tx.execute(conn -> {
            var expenseDao = factoryProvider.apply(conn).expenseDao();

            var updatedExp = expenseDao.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));

            if (description.isBlank() && amount.isBlank())
                throw new ExpenseUpdateFailedException("Error: at least one description or amount must be provided");

            if (!description.isBlank()) updatedExp = updatedExp.withDescription(description);

            if (!amount.isBlank()) updatedExp = updatedExp.withAmount(MoneyMapper.parseMoney(amount));

            var now = LocalDateTime.now();
            updatedExp = updatedExp.withUpdatedAt(now);

            if (!expenseDao.update(updatedExp)) throw new ExpenseUpdateFailedException(id);
            return updatedExp;
        });
    }

    public void delete(long id) {
        tx.executeVoid(conn -> {
            var expenseDao = factoryProvider.apply(conn).expenseDao();

            if (!expenseDao.delete(id)) throw new ExpenseNotFoundException(id);
        });
    }

    public ExpenseSummary getDailySummary(String date) {
        var day = date.isBlank()
                ? LocalDate.now()
                : PeriodValidator.validate(date, LocalDate.class);

        var dailyExpenses = expenseFilter.filterByDay(day);
        var stats = expenseStatistics.calculateAllStatistics(dailyExpenses);

        var description = String.format("Summary for %s", day.toString());
        return new ExpenseSummary(description, stats);
    }

    public ExpenseSummary getMonthlySummary(String date) {
        var month = date.isEmpty()
                ? YearMonth.now()
                : PeriodValidator.validate(date, YearMonth.class);

        var monthExpenses = expenseFilter.filterByMonth(month);
        var stats = expenseStatistics.calculateAllStatistics(monthExpenses);

        var description = String.format("Summary for %s ", month.toString());
        return new ExpenseSummary(description, stats);
    }

    public ExpenseSummary getYearlySummary(String date) {
        var year = date.isBlank()
                ? Year.now()
                : PeriodValidator.validate(date, Year.class);

        var yearExpenses = expenseFilter.filterByYear(year);
        var stats = expenseStatistics.calculateAllStatistics(yearExpenses);

        var description = String.format("Summary for %s ", year.toString());
        return new ExpenseSummary(description, stats);
    }

    public ExpenseSummary getLast7DaysSummary(String date) {
        var endDate = date.isBlank()
                ? LocalDate.now()
                : PeriodValidator.validate(date, LocalDate.class);
        var startDate = endDate.minusDays(6);

        var weekExpenses = expenseFilter.filterByCustomRange(startDate, endDate);
        var stats = expenseStatistics.calculateAllStatistics(weekExpenses);

        var description = String.format("Summary for last 7 days from %s to %s",
                startDate, endDate);
        return new ExpenseSummary(description, stats);
    }

    public ExpenseSummary getGeneralSummary() {
        var expenses = getAll();

        var stats = expenseStatistics.calculateAllStatistics(expenses);

        return new ExpenseSummary("General summary", stats);
    }
}
