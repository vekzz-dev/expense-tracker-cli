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
            throw new InvalidExpenseException("Error: ID cannot be negative");
        }

        if (description == null || description.isBlank()) {
            throw new InvalidExpenseException("Error: description cannot be empty");
        }

        if (amount == null) {
            throw new InvalidExpenseException("Error: amount cannot be null");
        }

        if (amount.isNegativeOrZero()) {
            throw new InvalidExpenseException("Error: amount must be positive");
        }

        if (createdAt == null || updatedAt == null) {
            throw new InvalidExpenseException("Error: dates cannot be null");
        }

        if (updatedAt.isBefore(createdAt)) {
            throw new InvalidExpenseException("Error: updatedAt cannot be before createdAt");
        }
    }

    public Expense withId(long id) {
        return new Expense(id, description, amount, createdAt, updatedAt);
    }

    public Expense withDescription(String description) {
        return new Expense(id, description, amount, createdAt, updatedAt);
    }

    public Expense withAmount(Money amount) {
        return new Expense(id, description, amount, createdAt, updatedAt);
    }

    public Expense withUpdatedAt(LocalDateTime updatedAt) {
        return new Expense(id, description, amount, createdAt, updatedAt);
    }
}
