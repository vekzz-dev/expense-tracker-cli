package io.vekzz_dev.expense_tracker.exception;

public class ExpenseNotFoundException extends DomainException {

    public ExpenseNotFoundException(long id) {
        super("Expense not found with ID: " + id);
    }
}
