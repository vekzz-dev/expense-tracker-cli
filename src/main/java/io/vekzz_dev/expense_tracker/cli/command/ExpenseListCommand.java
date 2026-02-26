package io.vekzz_dev.expense_tracker.cli.command;

import io.vekzz_dev.expense_tracker.cli.output.AsciiTableFormatter;
import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.service.ExpenseFilterService;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
import io.vekzz_dev.expense_tracker.util.PeriodValidator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Command(name = "list",
        description = "List expenses",
        mixinStandardHelpOptions = true
)
public class ExpenseListCommand implements Runnable {

    @Option(names = "-all", description = "List all expenses")
    private boolean listAll;

    @Option(names = "-d", description = "List expenses by date (yyyy-MM-dd). If no date is provided, today is used")
    private String date;

    @Option(names = "-m", description = "List expenses by month (1-12)")
    private String month;

    @Option(names = "-y", description = "List expenses by year (yyyy)")
    private String year;

    @Option(names = "-s", description = "Start date (yyyy-MM-dd) for custom range")
    private String startDate;

    @Option(names = "-f", description = "End date (yyyy-MM-dd) for custom range")
    private String endDate;

    private final ExpenseService expenseService;
    private final ExpenseFilterService expenseFilterService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ExpenseListCommand(ExpenseService expenseService, ExpenseFilterService expenseFilterService) {
        this.expenseService = expenseService;
        this.expenseFilterService = expenseFilterService;
    }

    @Override
    public void run() {

        if (!hasValidOption()) {
            printInvalidOptionMessage();
            return;
        }

        if (listAll) {
            handleListAll();
        } else if (date != null) {
            handleDateFilter();
        } else if (month != null) {
            handleMonthFilter();
        } else if (year != null) {
            handleYearFilter();
        } else {
            handleCustomRange();
        }
    }

    private void handleListAll() {
        var expenses = expenseService.getAll();
        printExpensesOrEmpty(expenses, "All expenses:");
    }

    private void handleDateFilter() {

        LocalDate targetDate = (date == null || date.isBlank())
                ? LocalDate.now()
                : PeriodValidator.validate(date, LocalDate.class);

        var expenses = expenseFilterService.filterByDay(targetDate);

        printExpensesOrEmpty(expenses,
                "Expenses for date " + targetDate + ":");
    }

    private void handleMonthFilter() {

        YearMonth ym = PeriodValidator.validate(month, YearMonth.class);

        var expenses = expenseFilterService.filterByMonth(ym);

        printExpensesOrEmpty(expenses,
                "Expenses for month " + ym + ":");
    }


    private void handleYearFilter() {

        Year y = PeriodValidator.validate(year, Year.class);

        var expenses = expenseFilterService.filterByYear(y);

        printExpensesOrEmpty(expenses,
                "Expenses for year " + y + ":");
    }


    private void handleCustomRange() {

        LocalDate start = PeriodValidator.validate(startDate, LocalDate.class);
        LocalDate end = PeriodValidator.validate(endDate, LocalDate.class);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        var expenses = expenseFilterService
                .filterByCustomRange(start, end);

        printExpensesOrEmpty(expenses,
                "Expenses from " + start + " to " + end + ":");
    }


    private boolean hasValidOption() {
        return listAll
                || date != null
                || month != null
                || year != null
                || (startDate != null && endDate != null);
    }

    private void printInvalidOptionMessage() {
        System.out.println("Please provide a valid option. Use --help for more information.");
    }

    private void printNoExpensesFoundMessage() {
        System.out.println("No expenses found");
    }

    private void printExpensesOrEmpty(List<Expense> expenses, String header) {

        if (expenses == null || expenses.isEmpty()) {
            printNoExpensesFoundMessage();
            return;
        }

        System.out.println(header);
        System.out.println(AsciiTableFormatter.buildExpensesTable(expenses));
    }
}
