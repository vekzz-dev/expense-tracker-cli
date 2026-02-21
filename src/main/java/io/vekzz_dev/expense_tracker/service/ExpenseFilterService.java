package io.vekzz_dev.expense_tracker.service;

import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.persistence.factory.DaoFactory;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

public class ExpenseFilterService {

    private final TransactionManager tx;
    private final Function<Connection, DaoFactory> factoryProvider;

    public ExpenseFilterService(TransactionManager tx, Function<Connection, DaoFactory> factoryProvider) {
        this.tx = tx;
        this.factoryProvider = factoryProvider;
    }

    public List<Expense> filterByDay(LocalDate dayToFilter) {
        return getAllExpenses().stream()
                .filter(expense -> {
                    var day = expense.createdAt().toLocalDate();

                    return day.equals(dayToFilter);
                })
                .toList();
    }

    public List<Expense> filterByCustomRange(LocalDate startDate, LocalDate endDate) {
        return getAllExpenses().stream()
                .filter(expense -> {
                    var day = expense.createdAt().toLocalDate();

                    return !day.isBefore(startDate) && !day.isAfter(endDate);
                })
                .toList();
    }

    public List<Expense> filterByMonth(YearMonth yearMonthToFilter) {
        return getAllExpenses().stream()
                .filter(expense -> {
                    var yearMonth = YearMonth.of(expense.createdAt().getYear(), expense.createdAt().getMonth());

                    return yearMonth.equals(yearMonthToFilter);
                })
                .toList();
    }

    public List<Expense> filterByYear(Year yearToFilter) {
        return getAllExpenses().stream()
                .filter(expense -> {
                    var year = Year.of(expense.createdAt().getYear());

                    return year.equals(yearToFilter);
                })
                .toList();
    }

    private List<Expense> getAllExpenses() {
        return tx.execute(conn -> factoryProvider
                .apply(conn)
                .expenseDao()
                .findAll());
    }
}
