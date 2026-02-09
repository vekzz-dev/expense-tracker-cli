package io.vekzz_dev.expense_tracker.util;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

public class MoneyMapper {

    public static long toMinor(MonetaryAmount amount) {
        CurrencyUnit currency = amount.getCurrency();
        int fractionDigits = currency.getDefaultFractionDigits();

        return amount
                .scaleByPowerOfTen(fractionDigits)
                .getNumber()
                .longValue();
    }
}
