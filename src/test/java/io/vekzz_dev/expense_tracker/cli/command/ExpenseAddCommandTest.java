package io.vekzz_dev.expense_tracker.cli.command;

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
class ExpenseAddCommandTest {

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
    void testRun_addsExpenseAndPrintsSuccessMessage() {
        var expense = new io.vekzz_dev.expense_tracker.model.Expense(
                1L, "Coffee", Money.of(5.00, "USD"),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(expenseService.add("Coffee", "5.00")).thenReturn(expense);

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("Coffee", "5.00");

        command.run();

        var output = outStream.toString();

        verify(expenseService).add("Coffee", "5.00");
        assertThat(output).contains("Successfully added expense with ID: 1");
        assertThat(output).contains("at");
    }

    @Test
    void testRun_withDecimalAmount() {
        var expense = new io.vekzz_dev.expense_tracker.model.Expense(
                1L, "Lunch", Money.of(12.50, "USD"),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(expenseService.add("Lunch", "12.50")).thenReturn(expense);

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("Lunch", "12.50");

        command.run();

        verify(expenseService).add("Lunch", "12.50");
    }

    @Test
    void testRun_withWholeNumber() {
        var expense = new io.vekzz_dev.expense_tracker.model.Expense(
                1L, "Item", Money.of(10.00, "USD"),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(expenseService.add("Item", "10")).thenReturn(expense);

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("Item", "10");

        command.run();

        verify(expenseService).add("Item", "10");
    }

    @Test
    void testRun_withLargeAmount() {
        var expense = new io.vekzz_dev.expense_tracker.model.Expense(
                1L, "Rent", Money.of(1500.00, "USD"),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(expenseService.add("Rent", "1500.00")).thenReturn(expense);

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("Rent", "1500.00");

        command.run();

        verify(expenseService).add("Rent", "1500.00");
    }

    @Test
    void testRun_withNegativeAmount_throwsException() {
        when(expenseService.add(anyString(), eq("-10")))
                .thenThrow(new io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException());

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("Item", "-10");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException.class);
    }

    @Test
    void testRun_withInvalidAmountFormat_throwsException() {
        when(expenseService.add(anyString(), eq("not-a-number")))
                .thenThrow(new io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException());

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("Item", "not-a-number");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException.class);
    }

    @Test
    void testRun_withZeroAmount_throwsException() {
        when(expenseService.add(anyString(), eq("0")))
                .thenThrow(new io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException());

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("Item", "0");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException.class);
    }

    @Test
    void testRun_withEmptyDescription_throwsException() {
        when(expenseService.add(anyString(), anyString()))
                .thenThrow(new io.vekzz_dev.expense_tracker.exception.InvalidExpenseException("Error: description cannot be empty"));

        var command = new ExpenseAddCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("", "10.00");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.InvalidExpenseException.class);
    }
}
