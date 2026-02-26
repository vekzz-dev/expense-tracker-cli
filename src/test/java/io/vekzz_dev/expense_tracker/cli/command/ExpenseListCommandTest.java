package io.vekzz_dev.expense_tracker.cli.command;

import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.service.ExpenseFilterService;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
import io.vekzz_dev.expense_tracker.util.PeriodValidator;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseListCommandTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private ExpenseFilterService expenseFilterService;

    private ByteArrayOutputStream outStream;
    private PrintStream printStream;

    @BeforeEach
    void setUp() {
        outStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outStream);
        System.setOut(printStream);
    }

    @AfterEach
    void tearDown() {
        System.setOut(System.out);
    }

    @Test
    void testRun_withAllOption_listsAllExpenses() {
        var expenses = List.of(
                new Expense(1L, "Coffee", Money.of(5.00, "USD"), LocalDateTime.now(), LocalDateTime.now()),
                new Expense(2L, "Lunch", Money.of(15.00, "USD"), LocalDateTime.now(), LocalDateTime.now())
        );

        when(expenseService.getAll()).thenReturn(expenses);

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-all");

        command.run();

        verify(expenseService).getAll();
        var output = outStream.toString();
        assertThat(output).contains("All expenses:");
    }

    @Test
    void testRun_withDateOption_listsExpensesForDate() {
        var targetDate = LocalDate.of(2024, 2, 15);
        var expenses = List.of(
                new Expense(1L, "Coffee", Money.of(5.00, "USD"), LocalDateTime.now(), LocalDateTime.now())
        );

        when(expenseFilterService.filterByDay(targetDate)).thenReturn(expenses);

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-d", "2024-02-15");

        command.run();

        verify(expenseFilterService).filterByDay(targetDate);
        var output = outStream.toString();
        assertThat(output).contains("Expenses for date 2024-02-15:");
    }

    @Test
    void testRun_withDateOptionNoDate_usesToday() {
        var today = LocalDate.now();
        var expenses = Collections.<Expense>emptyList();

        when(expenseFilterService.filterByDay(today)).thenReturn(expenses);

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-d", "");

        command.run();

        verify(expenseFilterService).filterByDay(today);
    }

    @Test
    void testRun_withMonthOption_listsExpensesForMonth() {
        var expenses = Collections.<Expense>emptyList();

        when(expenseFilterService.filterByMonth(any())).thenReturn(expenses);

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-m", "2024-02");

        command.run();

        verify(expenseFilterService).filterByMonth(any());
    }

    @Test
    void testRun_withYearOption_listsExpensesForYear() {
        var expenses = Collections.<Expense>emptyList();

        when(expenseFilterService.filterByYear(any())).thenReturn(expenses);

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-y", "2024");

        command.run();

        verify(expenseFilterService).filterByYear(any());
    }

    @Test
    void testRun_withCustomRangeOption_listsExpensesInRange() {
        var expenses = Collections.<Expense>emptyList();

        when(expenseFilterService.filterByCustomRange(any(), any())).thenReturn(expenses);

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-s", "2024-02-10", "-f", "2024-02-15");

        command.run();

        verify(expenseFilterService).filterByCustomRange(any(), any());
    }

    @Test
    void testRun_withoutAnyOptions_printsInvalidMessage() {
        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs();

        command.run();

        verifyNoInteractions(expenseService, expenseFilterService);
        var output = outStream.toString();
        assertThat(output).contains("Please provide a valid option. Use --help for more information.");
    }

    @Test
    void testRun_withDateOptionInvalidFormat_throwsException() {
        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);

        org.assertj.core.api.Assertions.catchThrowable(() ->
                cmdLine.parseArgs("-d", "invalid-date"));
    }

    @Test
    void testRun_withMonthOptionInvalidFormat_throwsException() {
        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);

        org.assertj.core.api.Assertions.catchThrowable(() ->
                cmdLine.parseArgs("-m", "invalid-month"));
    }

    @Test
    void testRun_withYearOptionInvalidFormat_throwsException() {
        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);

        org.assertj.core.api.Assertions.catchThrowable(() ->
                cmdLine.parseArgs("-y", "invalid-year"));
    }

    @Test
    void testRun_withCustomRangeInvalidFormat_throwsException() {
        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);

        org.assertj.core.api.Assertions.catchThrowable(() ->
                cmdLine.parseArgs("-s", "invalid", "-f", "invalid"));
    }

    @Test
    void testRun_withCustomRangeOnlyEnd_printsInvalidMessage() {
        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-f", "2024-02-15");

        command.run();

        verifyNoInteractions(expenseService, expenseFilterService);
        var output = outStream.toString();
        assertThat(output).contains("Please provide a valid option. Use --help for more information.");
    }

    @Test
    void testRun_withEmptyDatabase_printsNoExpensesMessage() {
        when(expenseService.getAll()).thenReturn(Collections.<Expense>emptyList());

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-all");

        command.run();

        var output = outStream.toString();
        assertThat(output).contains("No expenses found");
    }

    @Test
    void testRun_withLeapYearDate() {
        var expenses = Collections.<Expense>emptyList();

        when(expenseFilterService.filterByDay(any())).thenReturn(expenses);

        var command = new ExpenseListCommand(expenseService, expenseFilterService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-d", "2024-02-29");

        command.run();

        verify(expenseFilterService).filterByDay(any());
    }
}
