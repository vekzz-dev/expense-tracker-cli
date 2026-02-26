package io.vekzz_dev.expense_tracker.cli.command;

import io.vekzz_dev.expense_tracker.cli.output.DateFormatter;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "add",
        description = "Add new expense",
        mixinStandardHelpOptions = true
)
public class ExpenseAddCommand implements Runnable {

    @Parameters(index = "0", description = "<description>")
    private String description;

    @Parameters(index = "1", description = "<amount>")
    private String amount;

    private final ExpenseService expenseService;

    public ExpenseAddCommand(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Override
    public void run() {
        var exp = expenseService.add(description, amount);

        String message = String.format("Successfully added expense with ID: %d at %s",
                exp.id(), DateFormatter.formatDateTime(exp.createdAt()));
        System.out.println(message);
    }
}
