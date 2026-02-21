package io.vekzz_dev.expense_tracker.model;

import org.javamoney.moneta.Money;

public record ExpenseStatistics(
        Money total,
        Money average,
        Money max,
        Money min,
        long count
) {
}
