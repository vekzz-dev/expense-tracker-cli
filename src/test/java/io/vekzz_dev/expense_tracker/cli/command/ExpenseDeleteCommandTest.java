package io.vekzz_dev.expense_tracker.cli.command;

import io.vekzz_dev.expense_tracker.service.ExpenseService;
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
class ExpenseDeleteCommandTest {

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
    void testRun_deletesExpenseAndPrintsSuccessMessage() {
        doNothing().when(expenseService).delete(1L);

        var command = new ExpenseDeleteCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("1");

        command.run();

        var output = outStream.toString();

        verify(expenseService).delete(1L);
        assertThat(output).contains("Expense 1 has been deleted");
    }

    @Test
    void testRun_withValidId() {
        doNothing().when(expenseService).delete(42L);

        var command = new ExpenseDeleteCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("42");

        command.run();

        verify(expenseService).delete(42L);
    }

    @Test
    void testRun_withNonExistentId_throwsException() {
        doThrow(new io.vekzz_dev.expense_tracker.exception.ExpenseNotFoundException(9999L))
                .when(expenseService).delete(9999L);

        var command = new ExpenseDeleteCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("9999");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.ExpenseNotFoundException.class);
    }

    @Test
    void testRun_withNegativeId_throwsException() {
        doThrow(new io.vekzz_dev.expense_tracker.exception.InvalidExpenseException("Error: ID cannot be negative"))
                .when(expenseService).delete(-1L);

        var command = new ExpenseDeleteCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("-1");

        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());

        assertThat(thrown).isInstanceOf(io.vekzz_dev.expense_tracker.exception.InvalidExpenseException.class);
    }

    @Test
    void testRun_withZeroId() {
        doNothing().when(expenseService).delete(0L);

        var command = new ExpenseDeleteCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("0");

        command.run();

        verify(expenseService).delete(0L);
    }

    @Test
    void testRun_withLargeId() {
        doNothing().when(expenseService).delete(999999999L);

        var command = new ExpenseDeleteCommand(expenseService);
        var cmdLine = new CommandLine(command);
        cmdLine.parseArgs("999999999");

        command.run();

        verify(expenseService).delete(999999999L);
    }
}
