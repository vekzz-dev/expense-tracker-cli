package io.vekzz_dev.expense_tracker.util;

import io.vekzz_dev.expense_tracker.exception.InvalidPeriodTypeException;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.function.Function;

public final class PeriodValidator {

    private PeriodValidator() {
    }

    private static final Map<Class<?>, Function<String, ?>> DATE_PARSERS = Map.of(
            LocalDate.class, (Function<String, LocalDate>) s ->
                    LocalDate.parse(s, DateTimeFormatter.ISO_DATE),
            YearMonth.class, (Function<String, YearMonth>) s ->
                    YearMonth.parse(s, DateTimeFormatter.ofPattern("yyyy-MM")),
            Year.class, (Function<String, Year>) s ->
                    Year.parse(s, DateTimeFormatter.ofPattern("yyyy"))
    );

    public static <R> R validate(String period, Class<R> timeClass) {
        try {
            var parser = DATE_PARSERS.get(timeClass);

            if (parser == null)
                throw new IllegalArgumentException("Period type not supported: " + period);

            return timeClass.cast(parser.apply(period));

        } catch (DateTimeParseException e) {
            throw new InvalidPeriodTypeException("Error: invalid date format for: " + period);
        }
    }
}
