package io.vekzz_dev.expense_tracker.exceptions;

public class TransactionException extends RuntimeException {
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
