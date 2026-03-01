package io.vekzz_dev.expense_tracker.cli.output;

import de.vandermeer.asciitable.AsciiTable;
import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.model.ExpenseSummary;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public final class AsciiTableFormatter {

    private AsciiTableFormatter() {
    }

    private static String formatMoney(java.math.BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setCurrency(java.util.Currency.getInstance("USD"));
        return format.format(amount).replace("USD", "$");
    }

    public static String buildExpensesTable(List<Expense> expenses) {
        AtomicInteger index = new AtomicInteger(0);
        AsciiTable table = new AsciiTable();

        table.addRule();
        table.addRow("ID", "Description", "Amount", "Created at", "Updated at");
        table.addRule();

        expenses.forEach(exp -> {
            table.addRow(
                    exp.id(),
                    exp.description(),
                    formatMoney(exp.amount().getNumber().numberValue(java.math.BigDecimal.class)),
                    DateFormatter.formatDateTime(exp.createdAt()),
                    DateFormatter.formatDateTime(exp.updatedAt())
            );

            table.addRule();
            index.incrementAndGet();
        });

        if (index.get() < 1) table.addRule();

        return table.render();
    }

    public static String buildSummaryTable(ExpenseSummary summary) {
        AsciiTable table = new AsciiTable();

        table.addRule();
        table.addRow("Total amount: ", formatMoney(summary.statistics().total().getNumber()
                .numberValue(java.math.BigDecimal.class)));
        table.addRule();

        table.addRow("Average amount: ", formatMoney(summary.statistics().average().getNumber()
                .numberValue(java.math.BigDecimal.class)));
        table.addRule();

        table.addRow("Max amount: ", formatMoney(summary.statistics().max().getNumber()
                .numberValue(java.math.BigDecimal.class)));
        table.addRule();

        table.addRow("Min amount: ", formatMoney(summary.statistics().min().getNumber()
                .numberValue(java.math.BigDecimal.class)));
        table.addRule();

        table.addRow("Total expenses: ", summary.statistics().count());
        table.addRule();

        return table.render();
    }
}
