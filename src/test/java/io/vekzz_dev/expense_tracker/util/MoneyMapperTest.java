package io.vekzz_dev.expense_tracker.util;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyMapperTest {

    @Test
    void testToMinor_convertsWholeDollarsToCents() {
        Money amount = Money.of(10.00, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(1000L);
    }

    @Test
    void testToMinor_convertsDecimalAmountsToCents() {
        Money amount = Money.of(12.50, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(1250L);
    }

    @Test
    void testToMinor_convertsAmountsLessThanOneDollar() {
        Money amount = Money.of(0.99, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(99L);
    }

    @Test
    void testToMinor_handlesZero() {
        Money amount = Money.of(0.00, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    void testToMinor_handlesLargeAmounts() {
        Money amount = Money.of(9999.99, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(999999L);
    }

    @Test
    void testToMinor_handlesFractionalCents() {
        Money amount = Money.of(12.345, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(1234L);
    }

    @Test
    void testToMinor_handlesNegativeAmounts() {
        Money amount = Money.of(-5.50, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(-550L);
    }
}
