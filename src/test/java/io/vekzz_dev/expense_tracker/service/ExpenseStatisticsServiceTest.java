package io.vekzz_dev.expense_tracker.service;

import io.vekzz_dev.expense_tracker.model.Expense;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseStatisticsServiceTest {

    private ExpenseStatisticsService service;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        service = new ExpenseStatisticsService();
        baseTime = LocalDateTime.of(2024, 2, 15, 10, 0);
    }

    private Expense createExpense(long id, String description, double amount) {
        return new Expense(id, description, Money.of(amount, "USD"), baseTime, baseTime);
    }

    private Expense createExpense(long id, String description, double amount, String currency) {
        return new Expense(id, description, Money.of(amount, currency), baseTime, baseTime);
    }

    @Test
    void testCalculateAllStatistics_multipleExpenses() {
        List<Expense> expenses = List.of(
                createExpense(1, "Coffee", 5.00),
                createExpense(2, "Lunch", 15.00),
                createExpense(3, "Dinner", 25.00)
        );

        var stats = service.calculateAllStatistics(expenses);

        assertThat(stats.total().getNumberStripped()).isEqualByComparingTo("45.00");
        assertThat(stats.average().getNumberStripped()).isEqualByComparingTo("15.00");
        assertThat(stats.max().getNumberStripped()).isEqualByComparingTo("25.00");
        assertThat(stats.min().getNumberStripped()).isEqualByComparingTo("5.00");
        assertThat(stats.count()).isEqualTo(3);
    }

    @Test
    void testCalculateAllStatistics_singleExpense() {
        List<Expense> expenses = List.of(createExpense(1, "Coffee", 10.00));

        var stats = service.calculateAllStatistics(expenses);

        assertThat(stats.total().getNumberStripped()).isEqualByComparingTo("10.00");
        assertThat(stats.average().getNumberStripped()).isEqualByComparingTo("10.00");
        assertThat(stats.max().getNumberStripped()).isEqualByComparingTo("10.00");
        assertThat(stats.min().getNumberStripped()).isEqualByComparingTo("10.00");
        assertThat(stats.count()).isEqualTo(1);
    }

    @Test
    void testCalculateAllStatistics_emptyList() {
        List<Expense> expenses = new ArrayList<>();

        var stats = service.calculateAllStatistics(expenses);

        assertThat(stats.total().getNumberStripped()).isEqualByComparingTo("0");
        assertThat(stats.average().getNumberStripped()).isEqualByComparingTo("0");
        assertThat(stats.max().getNumberStripped()).isEqualByComparingTo("0");
        assertThat(stats.min().getNumberStripped()).isEqualByComparingTo("0");
        assertThat(stats.count()).isEqualTo(0);
    }

    @Test
    void testCalculateTotal_sumsAllAmounts() {
        List<Expense> expenses = List.of(
                createExpense(1, "Coffee", 5.50),
                createExpense(2, "Lunch", 12.25),
                createExpense(3, "Dinner", 20.00)
        );

        var total = service.calculateTotal(expenses);

        assertThat(total.getNumberStripped()).isEqualByComparingTo("37.75");
    }

    @Test
    void testCalculateTotal_returnsZeroForEmptyList() {
        List<Expense> expenses = new ArrayList<>();

        var total = service.calculateTotal(expenses);

        assertThat(total.getNumberStripped()).isEqualByComparingTo("0");
    }

    @Test
    void testCalculateTotal_singleExpense() {
        List<Expense> expenses = List.of(createExpense(1, "Coffee", 7.50));

        var total = service.calculateTotal(expenses);

        assertThat(total.getNumberStripped()).isEqualByComparingTo("7.50");
    }

    @Test
    void testCalculateTotal_largeAmounts() {
        List<Expense> expenses = List.of(
                createExpense(1, "Item1", 999999.99),
                createExpense(2, "Item2", 1.01)
        );

        var total = service.calculateTotal(expenses);

        assertThat(total.getNumberStripped()).isEqualByComparingTo("1000001.00");
    }

    @Test
    void testCalculateTotal_smallAmounts_cents() {
        List<Expense> expenses = List.of(
                createExpense(1, "Item1", 0.01),
                createExpense(2, "Item2", 0.02),
                createExpense(3, "Item3", 0.03)
        );

        var total = service.calculateTotal(expenses);

        assertThat(total.getNumberStripped()).isEqualByComparingTo("0.06");
    }

    @Test
    void testCalculateAverage_multipleExpenses() {
        List<Expense> expenses = List.of(
                createExpense(1, "Coffee", 10.00),
                createExpense(2, "Lunch", 20.00),
                createExpense(3, "Dinner", 30.00)
        );

        var average = service.calculateAverage(expenses);

        assertThat(average.getNumberStripped()).isEqualByComparingTo("20.00");
    }

    @Test
    void testCalculateAverage_returnsZeroForEmptyList() {
        List<Expense> expenses = new ArrayList<>();

        var average = service.calculateAverage(expenses);

        assertThat(average.getNumberStripped()).isEqualByComparingTo("0");
    }

    @Test
    void testCalculateAverage_singleExpense() {
        List<Expense> expenses = List.of(createExpense(1, "Coffee", 15.00));

        var average = service.calculateAverage(expenses);

        assertThat(average.getNumberStripped()).isEqualByComparingTo("15.00");
    }

    @Test
    void testCalculateAverage_allSameAmount() {
        List<Expense> expenses = List.of(
                createExpense(1, "Item1", 10.00),
                createExpense(2, "Item2", 10.00),
                createExpense(3, "Item3", 10.00)
        );

        var average = service.calculateAverage(expenses);

        assertThat(average.getNumberStripped()).isEqualByComparingTo("10.00");
    }

    @Test
    void testCalculateAverage_handlesFractionalResult() {
        List<Expense> expenses = List.of(
                createExpense(1, "Item1", 10.00),
                createExpense(2, "Item2", 11.00)
        );

        var average = service.calculateAverage(expenses);

        assertThat(average.getNumberStripped()).isEqualByComparingTo("10.50");
    }

    @Test
    void testCalculateMax_returnsHighestAmount() {
        List<Expense> expenses = List.of(
                createExpense(1, "Coffee", 5.00),
                createExpense(2, "Lunch", 25.00),
                createExpense(3, "Dinner", 15.00)
        );

        var max = service.calculateMax(expenses);

        assertThat(max.getNumberStripped()).isEqualByComparingTo("25.00");
    }

    @Test
    void testCalculateMax_returnsZeroForEmptyList() {
        List<Expense> expenses = new ArrayList<>();

        var max = service.calculateMax(expenses);

        assertThat(max.getNumberStripped()).isEqualByComparingTo("0");
    }

    @Test
    void testCalculateMax_singleExpense() {
        List<Expense> expenses = List.of(createExpense(1, "Coffee", 12.50));

        var max = service.calculateMax(expenses);

        assertThat(max.getNumberStripped()).isEqualByComparingTo("12.50");
    }

    @Test
    void testCalculateMin_returnsLowestAmount() {
        List<Expense> expenses = List.of(
                createExpense(1, "Coffee", 5.00),
                createExpense(2, "Lunch", 25.00),
                createExpense(3, "Dinner", 15.00)
        );

        var min = service.calculateMin(expenses);

        assertThat(min.getNumberStripped()).isEqualByComparingTo("5.00");
    }

    @Test
    void testCalculateMin_returnsZeroForEmptyList() {
        List<Expense> expenses = new ArrayList<>();

        var min = service.calculateMin(expenses);

        assertThat(min.getNumberStripped()).isEqualByComparingTo("0");
    }

    @Test
    void testCalculateMin_singleExpense() {
        List<Expense> expenses = List.of(createExpense(1, "Coffee", 8.75));

        var min = service.calculateMin(expenses);

        assertThat(min.getNumberStripped()).isEqualByComparingTo("8.75");
    }

    @Test
    void testCalculateStatistics_allSameAmount() {
        List<Expense> expenses = List.of(
                createExpense(1, "Item1", 20.00),
                createExpense(2, "Item2", 20.00),
                createExpense(3, "Item3", 20.00)
        );

        var stats = service.calculateAllStatistics(expenses);

        assertThat(stats.max().getNumberStripped()).isEqualByComparingTo("20.00");
        assertThat(stats.min().getNumberStripped()).isEqualByComparingTo("20.00");
        assertThat(stats.average().getNumberStripped()).isEqualByComparingTo("20.00");
    }

    @Test
    void testCalculateStatistics_preservesCurrency() {
        List<Expense> expenses = List.of(
                createExpense(1, "Item1", 10.00, "EUR"),
                createExpense(2, "Item2", 20.00, "EUR")
        );

        var stats = service.calculateAllStatistics(expenses);

        assertThat(stats.total().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(stats.average().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(stats.max().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(stats.min().getCurrency().getCurrencyCode()).isEqualTo("EUR");
    }


}
