package io.vekzz_dev.expense_tracker.exception;

public class TransactionException extends InfrastructureException {
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
