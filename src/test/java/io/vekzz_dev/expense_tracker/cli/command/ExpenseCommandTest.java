package io.vekzz_dev.expense_tracker.cli.command;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseCommandTest {

    @Test
    void testRun_printsUsageMessage() {
        var command = new ExpenseCommand();
        var outStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outStream);

        System.setOut(printStream);
        command.run();
        System.setOut(System.out);

        var output = outStream.toString().trim();

        assertThat(output).isEqualTo("Use: expense-tracker e <subcommand>");
    }

    @Test
    void testCommand_isRunnable() {
        var command = new ExpenseCommand();

        assertThat(command).isInstanceOf(Runnable.class);
    }

    @Test
    void testCommand_hasPublicConstructor() {
        var command = new ExpenseCommand();

        assertThat(command).isNotNull();
    }

    @Test
    void testCommandRun_doesNotThrowException() {
        var command = new ExpenseCommand();
        var outStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outStream);

        System.setOut(printStream);
        var thrown = org.assertj.core.api.Assertions.catchThrowable(() -> command.run());
        System.setOut(System.out);

        assertThat(thrown).isNull();
    }
}
