package io.vekzz_dev.expense_tracker.exception;

public class ExpenseUpdateFailedException extends DomainException {

    public ExpenseUpdateFailedException(long id) {
        super("Expense update failed with ID: " + id);
    }
}
