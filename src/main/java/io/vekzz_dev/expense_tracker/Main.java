package io.vekzz_dev.expense_tracker;

import io.vekzz_dev.expense_tracker.cli.command.ExpenseAddCommand;
import io.vekzz_dev.expense_tracker.cli.command.ExpenseCommand;
import io.vekzz_dev.expense_tracker.cli.command.ExpenseDeleteCommand;
import io.vekzz_dev.expense_tracker.cli.command.ExpenseListCommand;
import io.vekzz_dev.expense_tracker.cli.command.ExpenseSummaryCommand;
import io.vekzz_dev.expense_tracker.cli.command.ExpenseUpdateCommand;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import io.vekzz_dev.expense_tracker.persistence.factory.DaoFactory;
import io.vekzz_dev.expense_tracker.persistence.factory.JdbcDaoFactory;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;
import io.vekzz_dev.expense_tracker.service.ExpenseFilterService;
import io.vekzz_dev.expense_tracker.service.ExpenseService;
import io.vekzz_dev.expense_tracker.service.ExpenseStatisticsService;
import picocli.CommandLine;

import java.sql.Connection;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) {
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
                .addSubcommand(expenseSummaryCommand);

        System.exit(cmdLine.execute(args));
    }
}
