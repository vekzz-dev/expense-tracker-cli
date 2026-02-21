package io.vekzz_dev.expense_tracker.service;

import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.model.ExpenseStatistics;
import org.javamoney.moneta.Money;

import java.util.List;

public class ExpenseStatisticsService {

    public ExpenseStatistics calculateAllStatistics(List<Expense> expenses) {
        var total = calculateTotal(expenses);
        var average = calculateAverage(expenses);
        var max = calculateMax(expenses);
        var min = calculateMin(expenses);
        long count = expenses.size();

        return new ExpenseStatistics(total, average, max, min, count);
    }

    public Money calculateTotal(List<Expense> expenses) {
        String currency = getCurrencyOrDefault(expenses);

        return expenses.stream()
                .map(Expense::amount)
                .reduce(Money.of(0, currency), Money::add);
    }

    public Money calculateAverage(List<Expense> expenses) {
        if (expenses.isEmpty()) return Money.of(0, "USD");

        var total = calculateTotal(expenses);
        return total.divide(expenses.size());
    }

    public Money calculateMax(List<Expense> expenses) {
        String currency = getCurrencyOrDefault(expenses);

        return expenses.stream()
                .map(Expense::amount)
                .max(Money::compareTo)
                .orElse(Money.of(0, currency));
    }

    public Money calculateMin(List<Expense> expenses) {
        String currency = getCurrencyOrDefault(expenses);

        return expenses.stream()
                .map(Expense::amount)
                .min(Money::compareTo)
                .orElse(Money.of(0, currency));
    }

    private String getCurrencyOrDefault(List<Expense> expenses) {
        return expenses.isEmpty()
                ? "USD"
                : expenses.getFirst().amount().getCurrency().getCurrencyCode();
    }
}
