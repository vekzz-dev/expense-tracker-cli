package io.vekzz_dev.expense_tracker.models;

import org.javamoney.moneta.Money;

import java.time.LocalDateTime;

public record Expense(
        long id,
        String description,
        Money amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
