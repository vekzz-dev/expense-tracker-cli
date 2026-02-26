package io.vekzz_dev.expense_tracker.cli.command;


import io.vekzz_dev.expense_tracker.cli.output.DateFormatter;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "update",
        description = "Update a existing expense",
        mixinStandardHelpOptions = true
)
public class ExpenseUpdateCommand implements Runnable {

    @Parameters(index = "0", description = "<id>")
    private long id;

    @Option(names = {"-d", "--description"}, description = "<description>")
    private String description;

    @Option(names = {"-a", "--amount"}, description = "<amount>")
    private String amount;

    private final ExpenseService expenseService;

    public ExpenseUpdateCommand(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Override
    public void run() {
        var exp = expenseService.update(id, description, amount);

        String message = String.format("Updated expense with ID: %d at %s",
                exp.id(), DateFormatter.formatDateTime(exp.updatedAt()));
        System.out.println(message);
    }
}
