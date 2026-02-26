package io.vekzz_dev.expense_tracker.cli.command;

import io.vekzz_dev.expense_tracker.cli.output.AsciiTableFormatter;
import io.vekzz_dev.expense_tracker.model.ExpenseSummary;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "summary",
        description = "Show expense summaries",
        mixinStandardHelpOptions = true
)
public class ExpenseSummaryCommand implements Runnable {

    @Option(names = "-all", description = "Show general summary")
    private boolean general;

    @Option(names = "-d", arity = "0..1",
            description = "Daily summary (yyyy-MM-dd). If not provided, today is used")
    private String date;

    @Option(names = "-m", arity = "0..1",
            description = "Monthly summary (yyyy-MM). If not provided, current month is used")
    private String month;

    @Option(names = "-y", arity = "0..1",
            description = "Yearly summary (yyyy). If not provided, current year is used")
    private String year;

    @Option(names = "-w", arity = "0..1",
            description = "Last 7 days summary from date (yyyy-MM-dd). If not provided, today is used")
    private String weekFrom;

    private final ExpenseService expenseService;

    public ExpenseSummaryCommand(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @Override
    public void run() {

        ExpenseSummary summary;

        if (general) {
            summary = expenseService.getGeneralSummary();
        } else if (date != null) {
            summary = expenseService.getDailySummary(defaultIfNull(date));
        } else if (month != null) {
            summary = expenseService.getMonthlySummary(defaultIfNull(month));
        } else if (year != null) {
            summary = expenseService.getYearlySummary(defaultIfNull(year));
        } else if (weekFrom != null) {
            summary = expenseService.getLast7DaysSummary(defaultIfNull(weekFrom));
        } else {
            // comportamiento por defecto: resumen diario de hoy
            summary = expenseService.getDailySummary("");
        }

        printSummary(summary);
    }

    private String defaultIfNull(String value) {
        return value == null ? "" : value;
    }

    private void printSummary(ExpenseSummary summary) {

        if (summary == null || summary.statistics() == null) {
            System.out.println("No data available for this period.");
            return;
        }

        System.out.println(AsciiTableFormatter.buildSummaryTable(summary));
    }
}
