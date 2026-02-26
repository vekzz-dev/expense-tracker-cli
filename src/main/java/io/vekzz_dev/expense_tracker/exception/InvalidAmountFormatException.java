package io.vekzz_dev.expense_tracker.exception;

public class InvalidAmountFormatException extends DomainException {

    public InvalidAmountFormatException() {
        super("Error: invalid amount format. Use '<amount>', e.g., '12.50'");
    }
}
