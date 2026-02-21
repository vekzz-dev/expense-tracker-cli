package io.vekzz_dev.expense_tracker.model;

public record ExpenseSummary(
        String description,
        ExpenseStatistics statistics
) {
}