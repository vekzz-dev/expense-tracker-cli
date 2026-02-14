package io.vekzz_dev.expense_tracker.model;

import io.vekzz_dev.expense_tracker.exception.InvalidExpenseException;
import org.javamoney.moneta.Money;

import java.time.LocalDateTime;

public record Expense(
        long id,
        String description,
        Money amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public Expense {
        if (id < 0) {
            throw new InvalidExpenseException("Id cannot be negative");
        }

        if (description == null || description.isBlank()) {
            throw new InvalidExpenseException("Description cannot be empty");
        }

        if (amount == null) {
            throw new InvalidExpenseException("Amount cannot be null");
        }

        if (amount.isNegativeOrZero()) {
            throw new InvalidExpenseException("Amount must be positive");
        }

        if (createdAt == null || updatedAt == null) {
            throw new InvalidExpenseException("Dates cannot be null");
        }

        if (updatedAt.isBefore(createdAt)) {
            throw new InvalidExpenseException("updatedAt cannot be before createdAt");
        }
    }

    public Expense withId(long id) {
        return new Expense(id, description, amount, createdAt, updatedAt);
    }

    public Expense withUpdateValues(String description, Money amount, LocalDateTime updatedAt) {
        return new Expense(id, description, amount, createdAt, updatedAt);
    }
}
