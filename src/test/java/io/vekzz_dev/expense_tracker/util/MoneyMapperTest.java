package io.vekzz_dev.expense_tracker.util;

import io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class MoneyMapperTest {

    @Test
    void testToMinor_convertsWholeDollarsToCents() {
        MonetaryAmount amount = Money.of(10.00, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(1000L);
    }

    @Test
    void testToMinor_convertsDecimalAmountsToCents() {
        MonetaryAmount amount = Money.of(12.50, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(1250L);
    }

    @Test
    void testToMinor_convertsAmountsLessThanOneDollar() {
        MonetaryAmount amount = Money.of(0.99, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(99L);
    }

    @Test
    void testToMinor_handlesZero() {
        MonetaryAmount amount = Money.of(0.00, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    void testToMinor_handlesLargeAmounts() {
        MonetaryAmount amount = Money.of(9999.99, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(999999L);
    }

    @Test
    void testToMinor_handlesFractionalCents() {
        MonetaryAmount amount = Money.of(12.345, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(1234L);
    }

    @Test
    void testToMinor_handlesNegativeAmounts() {
        MonetaryAmount amount = Money.of(-5.50, "USD");

        long result = MoneyMapper.toMinor(amount);

        assertThat(result).isEqualTo(-550L);
    }

    @Test
    void testParseMoney_parsesValidNumber() {
        var result = MoneyMapper.parseMoney("10.50");

        assertThat(result.getNumberStripped()).isEqualByComparingTo("10.50");
        assertThat(result.getCurrency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void testParseMoney_parsesWholeNumber() {
        var result = MoneyMapper.parseMoney("100");

        assertThat(result.getNumberStripped()).isEqualByComparingTo("100.00");
    }

    @Test
    void testParseMoney_parsesDecimalNumber() {
        var result = MoneyMapper.parseMoney("12.99");

        assertThat(result.getNumberStripped()).isEqualByComparingTo("12.99");
    }

    @Test
    void testParseMoney_throwsException_onInvalidNumber() {
        var thrown = catchThrowable(() -> MoneyMapper.parseMoney("not-a-number"));

        assertThat(thrown).isInstanceOf(InvalidAmountFormatException.class);
    }

    @Test
    void testParseMoney_throwsException_onEmptyString() {
        var thrown = catchThrowable(() -> MoneyMapper.parseMoney(""));

        assertThat(thrown).isInstanceOf(InvalidAmountFormatException.class);
    }

    @Test
    void testParseMoney_throwsException_onNegativeNumber() {
        var thrown = catchThrowable(() -> MoneyMapper.parseMoney("-10.50"));

        assertThat(thrown).isInstanceOf(InvalidAmountFormatException.class);
    }

}
