package io.vekzz_dev.expense_tracker.exception;

public class ExpenseAddingFailedException extends DomainException {

    public ExpenseAddingFailedException() {
        super("Error while adding expense");
    }
}
