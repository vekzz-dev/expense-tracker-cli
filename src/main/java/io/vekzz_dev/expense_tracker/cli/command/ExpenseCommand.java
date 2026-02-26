package io.vekzz_dev.expense_tracker.cli.command;

import picocli.CommandLine.Command;

@Command(
        name = "exp",
        description = "Command to expenses",
        version = "1.0",
        mixinStandardHelpOptions = true,
        subcommands = {
                ExpenseAddCommand.class,
                ExpenseDeleteCommand.class,
                ExpenseListCommand.class,
                ExpenseUpdateCommand.class,
                ExpenseSummaryCommand.class
        }
)
public class ExpenseCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use: expense-tracker e <subcommand>");
    }
}
