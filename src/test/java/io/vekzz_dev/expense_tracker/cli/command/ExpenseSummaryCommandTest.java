package io.vekzz_dev.expense_tracker.cli.command;

import io.vekzz_dev.expense_tracker.model.ExpenseSummary;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseSummaryCommandTest {

    @Mock
    private ExpenseService expenseService;

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
    void testRun_withoutOptions_printsDailySummaryToday() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(100.00, "USD"), Money.of(25.00, "USD"),
                Money.of(50.00, "USD"), Money.of(10.00, "USD"), 4
        );
        var summary = new ExpenseSummary("Summary for 2024-02-15", stats);

        when(expenseService.getDailySummary("")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs();

        command.run();

        verify(expenseService).getDailySummary("");
        var output = outStream.toString();
        assertThat(output).contains("Summary for 2024-02-15");
        assertThat(output).contains("Total amount:");
        assertThat(output).contains("Average amount:");
        assertThat(output).contains("Max amount:");
        assertThat(output).contains("Min amount:");
        assertThat(output).contains("Total expenses:");
    }

    @Test
    void testRun_withAllOption_printsGeneralSummary() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(500.00, "USD"), Money.of(125.00, "USD"),
                Money.of(250.00, "USD"), Money.of(50.00, "USD"), 4
        );
        var summary = new ExpenseSummary("General summary", stats);

        when(expenseService.getGeneralSummary()).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-all");

        command.run();

        verify(expenseService).getGeneralSummary();
        var output = outStream.toString();
        assertThat(output).contains("General summary");
    }

    @Test
    void testRun_withDateOption_printsDailySummary() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(50.00, "USD"), Money.of(25.00, "USD"),
                Money.of(50.00, "USD"), Money.of(10.00, "USD"), 2
        );
        var summary = new ExpenseSummary("Summary for 2024-02-15", stats);

        when(expenseService.getDailySummary("2024-02-15")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-d", "2024-02-15");

        command.run();

        verify(expenseService).getDailySummary("2024-02-15");
    }

    @Test
    void testRun_withDateOptionNoDate_usesToday() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(50.00, "USD"), Money.of(25.00, "USD"),
                Money.of(50.00, "USD"), Money.of(10.00, "USD"), 2
        );
        var summary = new ExpenseSummary("Summary for today", stats);

        when(expenseService.getDailySummary("")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-d");

        command.run();

        verify(expenseService).getDailySummary("");
    }

    @Test
    void testRun_withMonthOption_printsMonthlySummary() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(200.00, "USD"), Money.of(50.00, "USD"),
                Money.of(100.00, "USD"), Money.of(20.00, "USD"), 4
        );
        var summary = new ExpenseSummary("Summary for 2024-02", stats);

        when(expenseService.getMonthlySummary("2024-02")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-m", "2024-02");

        command.run();

        verify(expenseService).getMonthlySummary("2024-02");
    }

    @Test
    void testRun_withMonthOptionNoMonth_usesCurrentMonth() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(200.00, "USD"), Money.of(50.00, "USD"),
                Money.of(100.00, "USD"), Money.of(20.00, "USD"), 4
        );
        var summary = new ExpenseSummary("Summary for current month", stats);

        when(expenseService.getMonthlySummary("")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-m");

        command.run();

        verify(expenseService).getMonthlySummary("");
    }

    @Test
    void testRun_withYearOption_printsYearlySummary() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(2400.00, "USD"), Money.of(600.00, "USD"),
                Money.of(1200.00, "USD"), Money.of(240.00, "USD"), 4
        );
        var summary = new ExpenseSummary("Summary for 2024", stats);

        when(expenseService.getYearlySummary("2024")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-y", "2024");

        command.run();

        verify(expenseService).getYearlySummary("2024");
    }

    @Test
    void testRun_withWeekOption_printsLast7DaysSummary() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(100.00, "USD"), Money.of(25.00, "USD"),
                Money.of(50.00, "USD"), Money.of(10.00, "USD"), 4
        );
        var summary = new ExpenseSummary("Summary for last 7 days", stats);

        when(expenseService.getLast7DaysSummary("2024-02-15")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-w", "2024-02-15");

        command.run();

        verify(expenseService).getLast7DaysSummary("2024-02-15");
    }

    @Test
    void testRun_withWeekOptionNoDate_usesToday() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(100.00, "USD"), Money.of(25.00, "USD"),
                Money.of(50.00, "USD"), Money.of(10.00, "USD"), 4
        );
        var summary = new ExpenseSummary("Summary for last 7 days", stats);

        when(expenseService.getLast7DaysSummary("")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-w");

        command.run();

        verify(expenseService).getLast7DaysSummary("");
    }

    @Test
    void testRun_withInvalidDate_throwsException() {
        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);

        org.assertj.core.api.Assertions.catchThrowable(() ->
                cmdLine.parseArgs("-d", "invalid-date"));
    }

    @Test
    void testRun_withInvalidMonth_throwsException() {
        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);

        org.assertj.core.api.Assertions.catchThrowable(() ->
                cmdLine.parseArgs("-m", "invalid-month"));
    }

    @Test
    void testRun_withInvalidYear_throwsException() {
        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);

        org.assertj.core.api.Assertions.catchThrowable(() ->
                cmdLine.parseArgs("-y", "invalid-year"));
    }

    @Test
    void testRun_withEmptyDatabase_printsNoDataMessage() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(0.00, "USD"), Money.of(0.00, "USD"),
                Money.of(0.00, "USD"), Money.of(0.00, "USD"), 0
        );
        var summary = new ExpenseSummary("Summary", stats);

        when(expenseService.getDailySummary("")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs();

        command.run();

        verify(expenseService).getDailySummary("");
    }

    @Test
    void testRun_withMultipleOptions_usesFirstMatch() {
        var stats = new io.vekzz_dev.expense_tracker.model.ExpenseStatistics(
                Money.of(100.00, "USD"), Money.of(25.00, "USD"),
                Money.of(50.00, "USD"), Money.of(10.00, "USD"), 4
        );
        var summary = new ExpenseSummary("Summary for 2024-02", stats);

        when(expenseService.getMonthlySummary("2024-02")).thenReturn(summary);

        var command = new ExpenseSummaryCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-m", "2024-02", "-y", "2024");

        command.run();

        verify(expenseService).getMonthlySummary("2024-02");
        verify(expenseService, never()).getYearlySummary(anyString());
    }
}
