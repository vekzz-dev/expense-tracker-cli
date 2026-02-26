package io.vekzz_dev.expense_tracker.cli.output;

import de.vandermeer.asciitable.AsciiTable;
import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.model.ExpenseSummary;
import io.vekzz_dev.expense_tracker.model.ExpenseStatistics;

import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.util.List;
import java.util.Locale;

public final class AsciiTableFormatter {

    private AsciiTableFormatter() {
    }

    public static String buildExpensesTable(List<Expense> expenses) {
        MonetaryAmountFormat fmt = MonetaryFormats.getAmountFormat(Locale.US);
        AsciiTable table = new AsciiTable();

        table.addRule();
        table.addRow("ID", "Description", "Amount", "Created at", "Updated at");
        table.addRule();

        expenses.forEach(exp -> {
            table.addRow(
                    exp.id(),
                    exp.description(),
                    fmt.format(exp.amount()),
                    DateFormatter.formatDateTime(exp.createdAt()),
                    DateFormatter.formatDateTime(exp.updatedAt())
            );

            table.addRule();
        });

        table.addRule();

        return table.render();
    }

    public static String buildSummaryTable(ExpenseSummary summary) {
        MonetaryAmountFormat fmt = MonetaryFormats.getAmountFormat(Locale.US);
        AsciiTable table = new AsciiTable();

        table.addRule();
        table.addRow(summary.description(), "");
        table.addRule();

        table.addRow("Total amount: ", fmt.format(summary.statistics().total()));
        table.addRule();

        table.addRow("Average amount: ", fmt.format(summary.statistics().average()));
        table.addRule();

        table.addRow("Max amount: ", fmt.format(summary.statistics().max()));
        table.addRule();

        table.addRow("Min amount: ", fmt.format(summary.statistics().min()));
        table.addRule();

        table.addRow("Total expenses: ", summary.statistics().count());
        table.addRule();

        return table.render();
    }
}
