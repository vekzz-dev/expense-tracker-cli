package io.vekzz_dev.expense_tracker;

import io.vekzz_dev.expense_tracker.cli.command.*;
import io.vekzz_dev.expense_tracker.exception.DomainException;
import io.vekzz_dev.expense_tracker.exception.InfrastructureException;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import io.vekzz_dev.expense_tracker.persistence.factory.DaoFactory;
import io.vekzz_dev.expense_tracker.persistence.factory.JdbcDaoFactory;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;
import io.vekzz_dev.expense_tracker.service.ExpenseFilterService;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
import io.vekzz_dev.expense_tracker.service.ExpenseStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.ParseResult;

import java.sql.Connection;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        SLF4JBridgeHandler.install();

        LOGGER.info("Application starting");
        DatabaseSetup.initialize();

        TransactionManager tx = new TransactionManager();
        Function<Connection, DaoFactory> factoryProvider = JdbcDaoFactory::new;

        ExpenseStatisticsService expenseStatisticsService = new ExpenseStatisticsService();
        ExpenseFilterService expenseFilterService = new ExpenseFilterService(tx, factoryProvider);
        ExpenseService expenseService = new ExpenseService(tx, factoryProvider, expenseFilterService, expenseStatisticsService);

        ExpenseCommand expenseCommand = new ExpenseCommand();
        ExpenseAddCommand expenseAddCommand = new ExpenseAddCommand(expenseService);
        ExpenseDeleteCommand expenseDeleteCommand = new ExpenseDeleteCommand(expenseService);
        ExpenseUpdateCommand expenseUpdateCommand = new ExpenseUpdateCommand(expenseService);
        ExpenseListCommand expenseListCommand = new ExpenseListCommand(expenseService, expenseFilterService);
        ExpenseSummaryCommand expenseSummaryCommand = new ExpenseSummaryCommand(expenseService);

        CommandLine cmdLine = new CommandLine(expenseCommand)
                .addSubcommand(expenseAddCommand)
                .addSubcommand(expenseDeleteCommand)
                .addSubcommand(expenseUpdateCommand)
                .addSubcommand(expenseListCommand)
                .addSubcommand(expenseSummaryCommand)
                .setExecutionExceptionHandler(new CustomExceptionHandler())
                .setExitCodeExceptionMapper(new CustomExceptionMapper());

        System.exit(cmdLine.execute(args));
    }

    private static class CustomExceptionHandler implements IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
            if (ex instanceof DomainException) {
                System.err.println(ex.getMessage());
                return 1;

            } else if (ex instanceof InfrastructureException) {
                System.err.println("Internal error, check the log file at ~/.expense_tracker/expense-tracker.log");
                LOGGER.error("Infrastructure exception: {}", ex.getMessage(), ex);
                return 1;

            } else if (ex instanceof RuntimeException) {
                System.err.println("Unexpected error: " + ex.getMessage());
                LOGGER.error("Unexpected exception: {}", ex.getMessage(), ex);
                return 1;
            }

            System.err.println("Unhandled error: " + ex.getMessage());
            LOGGER.error("Unhandled exception: {}", ex.getMessage(), ex);
            return 1;
        }
    }

    private static class CustomExceptionMapper implements IExitCodeExceptionMapper {
        @Override
        public int getExitCode(Throwable exception) {
            if (exception instanceof DomainException) {
                return 1;
            } else if (exception instanceof InfrastructureException) {
                return 1;
            }
            return 1;
        }
    }
}
