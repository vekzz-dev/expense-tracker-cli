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
class ExpenseUpdateCommandTest {

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
    void testRun_updatesDescriptionOnly() {
        var expense = new io.vekzz_dev.expense_tracker.model.Expense(
                1L, "Coffee", Money.of(5.00, "USD"),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(expenseService.update(1L, "Espresso", null)).thenReturn(expense);

        var command = new ExpenseUpdateCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("1", "-d", "Espresso");

        command.run();

        verify(expenseService).update(1L, "Espresso", null);
    }

    @Test
    void testRun_updatesAmountOnly() {
        var expense = new io.vekzz_dev.expense_tracker.model.Expense(
                1L, "Coffee", Money.of(4.50, "USD"),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        when(expenseService.update(1L, null, "4.50")).thenReturn(expense);

        var command = new ExpenseUpdateCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("1", "-a", "4.50");

        command.run();

        verify(expenseService).update(1L, null, "4.50");
    }

    @Test
    void testRun_withEmptyDescriptionAndEmptyAmount_throwsException() {
        doThrow(new io.vekzz_dev.expense_tracker.exception.ExpenseUpdateFailedException(
                "Error: at least one description or amount must be provided"))
                .when(expenseService).update(1L, null, null);

        var command = new ExpenseUpdateCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("1");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.ExpenseUpdateFailedException.class);
    }

    @Test
    void testRun_withInvalidAmount_throwsException() {
        doThrow(new io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException())
                .when(expenseService).update(1L, null, "invalid");

        var command = new ExpenseUpdateCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("1", "-a", "invalid");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException.class);
    }

    @Test
    void testRun_withNonExistentId_throwsException() {
        doThrow(new io.vekzz_dev.expense_tracker.exception.ExpenseNotFoundException(9999L))
                .when(expenseService).update(9999L, "New", "10.00");

        var command = new ExpenseUpdateCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("9999", "-d", "New", "-a", "10.00");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.ExpenseNotFoundException.class);
    }

    @Test
    void testRun_withNegativeId_throwsException() {
        doThrow(new io.vekzz_dev.expense_tracker.exception.InvalidExpenseException("Error: ID cannot be negative"))
                .when(expenseService).update(-1L, "Test", "10.00");

        var command = new ExpenseUpdateCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-1", "-d", "Test", "-a", "10.00");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.InvalidExpenseException.class);
    }
}
