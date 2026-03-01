package io.vekzz_dev.expense_tracker.cli.output;

import io.vekzz_dev.expense_tracker.model.Expense;
import io.vekzz_dev.expense_tracker.model.ExpenseStatistics;
import io.vekzz_dev.expense_tracker.model.ExpenseSummary;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AsciiTableFormatterTest {

    private final LocalDateTime baseTime = LocalDateTime.of(2024, 2, 15, 10, 30, 0);

    @Test
    void testBuildExpensesTable_generatesCorrectStructure() {
        var expense = new Expense(1, "Coffee", Money.of(5.00, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("ID");
        assertThat(result).contains("Description");
        assertThat(result).contains("Amount");
        assertThat(result).contains("Created at");
        assertThat(result).contains("Updated at");
        assertThat(result).contains("1");
        assertThat(result).contains("Coffee");
        assertThat(result).contains("5.00");
        assertThat(result).contains("2024-02-15");
    }

    @Test
    void testBuildExpensesTable_withSingleExpense() {
        var expense = new Expense(1, "Coffee", Money.of(5.00, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("1");
        assertThat(result).contains("Coffee");
        assertThat(result).contains("5.00");
    }

    @Test
    void testBuildExpensesTable_withMultipleExpenses() {
        var expense1 = new Expense(1, "Coffee", Money.of(5.00, "USD"), baseTime, baseTime);
        var expense2 = new Expense(2, "Lunch", Money.of(15.50, "USD"), baseTime, baseTime);
        var expense3 = new Expense(3, "Dinner", Money.of(25.99, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense1, expense2, expense3);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("Coffee");
        assertThat(result).contains("Lunch");
        assertThat(result).contains("Dinner");
        assertThat(result).contains("15.50");
        assertThat(result).contains("25.99");
    }

    @Test
    void testBuildExpensesTable_formatsAmountCorrectly() {
        var expense = new Expense(1, "Item", Money.of(12.50, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("12.50");
    }

    @Test
    void testBuildExpensesTable_withEmptyList() {
        List<Expense> expenses = Collections.emptyList();

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("ID");
        assertThat(result).contains("Description");
        assertThat(result).contains("Amount");
        assertThat(result).contains("Created at");
        assertThat(result).contains("Updated at");
    }

    @Test
    void testBuildExpensesTable_withLongDescription() {
        var longDescription = "This is a very long expense description that contains many words";
        var expense = new Expense(1, longDescription, Money.of(10.00, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("many words");
    }

    @Test
    void testBuildExpensesTable_withSmallDecimals() {
        var expense = new Expense(1, "Item", Money.of(0.01, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("0.01");
    }

    @Test
    void testBuildExpensesTable_withLargeAmount() {
        var expense = new Expense(1, "Rent", Money.of(1500.00, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("1,500.00");
    }

    @Test
    void testBuildExpensesTable_withVeryLargeAmount() {
        var expense = new Expense(1, "Item", Money.of(999999.99, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("999,999.99");
    }

    @Test
    void testBuildExpensesTable_formatsDateTimes() {
        var expense = new Expense(1, "Item", Money.of(10.00, "USD"), baseTime, baseTime);
        List<Expense> expenses = List.of(expense);

        var result = AsciiTableFormatter.buildExpensesTable(expenses);

        assertThat(result).contains("2024-02-15");
        assertThat(result).contains("10:30 AM");
    }

    @Test
    void testBuildSummaryTable_generatesCorrectStructure() {
        var stats = new ExpenseStatistics(Money.of(100.00, "USD"), Money.of(25.00, "USD"),
                Money.of(50.00, "USD"), Money.of(10.00, "USD"), 4);
        var summary = new ExpenseSummary("Test Summary", stats);

        var result = AsciiTableFormatter.buildSummaryTable(summary);

        assertThat(result).contains("Total amount:");
        assertThat(result).contains("Average amount:");
        assertThat(result).contains("Max amount:");
        assertThat(result).contains("Min amount:");
        assertThat(result).contains("Total expenses:");
        assertThat(result).contains("100.00");
        assertThat(result).contains("25.00");
        assertThat(result).contains("50.00");
        assertThat(result).contains("10.00");
        assertThat(result).contains("4");
    }

    @Test
    void testBuildSummaryTable_withZeroValues() {
        var stats = new ExpenseStatistics(Money.of(0.00, "USD"), Money.of(0.00, "USD"),
                Money.of(0.00, "USD"), Money.of(0.00, "USD"), 0);
        var summary = new ExpenseSummary("Test Summary", stats);

        var result = AsciiTableFormatter.buildSummaryTable(summary);

        assertThat(result).contains("0.00");
        assertThat(result).contains("0");
    }

    @Test
    void testBuildSummaryTable_formatsLargeAmounts() {
        var stats = new ExpenseStatistics(Money.of(1000000.00, "USD"), Money.of(250000.00, "USD"),
                Money.of(500000.00, "USD"), Money.of(100000.00, "USD"), 4);
        var summary = new ExpenseSummary("Test Summary", stats);

        var result = AsciiTableFormatter.buildSummaryTable(summary);

        assertThat(result).contains("1,000,000.00");
        assertThat(result).contains("250,000.00");
        assertThat(result).contains("500,000.00");
        assertThat(result).contains("100,000.00");
    }

    @Test
    void testBuildSummaryTable_withSingleExpense() {
        var stats = new ExpenseStatistics(Money.of(50.00, "USD"), Money.of(50.00, "USD"),
                Money.of(50.00, "USD"), Money.of(50.00, "USD"), 1);
        var summary = new ExpenseSummary("Test Summary", stats);

        var result = AsciiTableFormatter.buildSummaryTable(summary);

        assertThat(result).contains("50.00");
        assertThat(result).contains("1");
    }

    @Test
    void testBuildSummaryTable_withMultipleExpenses() {
        var stats = new ExpenseStatistics(Money.of(150.00, "USD"), Money.of(30.00, "USD"),
                Money.of(100.00, "USD"), Money.of(10.00, "USD"), 5);
        var summary = new ExpenseSummary("Test Summary", stats);

        var result = AsciiTableFormatter.buildSummaryTable(summary);

        assertThat(result).contains("150.00");
        assertThat(result).contains("30.00");
        assertThat(result).contains("5");
    }
}
