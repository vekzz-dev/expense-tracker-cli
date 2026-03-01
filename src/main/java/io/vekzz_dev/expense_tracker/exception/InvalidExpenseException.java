package io.vekzz_dev.expense_tracker.exception;

public class InvalidExpenseException extends DomainException {

    public InvalidExpenseException(String message) {
        super(message);
    }
}
