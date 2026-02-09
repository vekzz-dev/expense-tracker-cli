package io.vekzz_dev.expense_tracker.exception;

public class TransactionException extends RuntimeException {

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
