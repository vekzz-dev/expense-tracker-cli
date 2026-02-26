package io.vekzz_dev.expense_tracker.cli.command;

import io.vekzz_dev.expense_tracker.service.ExpenseService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "delete",
        description = "Delete a expense",
        mixinStandardHelpOptions = true
)
public class ExpenseDeleteCommand implements Runnable {

    @Parameters(index = "0", description = "<id>")
    private long id;

    private final ExpenseService expenseService;

    public ExpenseDeleteCommand(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Override
    public void run() {
        expenseService.delete(id);

        String message = String.format("Expense %d has been deleted", id);
        System.out.println(message);
    }
}
