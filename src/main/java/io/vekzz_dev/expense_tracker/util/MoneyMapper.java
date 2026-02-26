package io.vekzz_dev.expense_tracker.util;

import io.vekzz_dev.expense_tracker.exception.InvalidAmountFormatException;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.UnknownCurrencyException;
import java.math.BigDecimal;

public final class MoneyMapper {

    private MoneyMapper() {
    }

    public static long toMinor(MonetaryAmount amount) {
        CurrencyUnit currency = amount.getCurrency();
        int fractionDigits = currency.getDefaultFractionDigits();

        return amount
                .scaleByPowerOfTen(fractionDigits)
                .getNumber()
                .longValue();
    }

    public static Money parseMoney(String amount) {
        try {
            var decimal = new BigDecimal(amount);
            if (decimal.signum() <= 0) {
                throw new InvalidAmountFormatException();
            }
            return Money.of(decimal, "USD");
        } catch (NumberFormatException | UnknownCurrencyException e) {
            throw new InvalidAmountFormatException();
        }
    }
}
