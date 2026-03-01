package io.vekzz_dev.expense_tracker.util;

import io.vekzz_dev.expense_tracker.exception.InvalidPeriodTypeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class PeriodValidatorTest {

    @Test
    void testValidate_parsesValidLocalDate() {
        var result = PeriodValidator.validate("2024-02-15", LocalDate.class);

        assertThat(result).isEqualTo(LocalDate.of(2024, 2, 15));
    }

    @Test
    void testValidate_parsesValidYearMonth() {
        var result = PeriodValidator.validate("2024-02", YearMonth.class);

        assertThat(result).isEqualTo(YearMonth.of(2024, 2));
    }

    @Test
    void testValidate_parsesValidYear() {
        var result = PeriodValidator.validate("2024", Year.class);

        assertThat(result).isEqualTo(Year.of(2024));
    }

    @Test
    void testValidate_parsesLeapYearDate() {
        var result = PeriodValidator.validate("2024-02-29", LocalDate.class);

        assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    void testValidate_parsesMinDate() {
        var result = PeriodValidator.validate("0001-01-01", LocalDate.class);

        assertThat(result).isEqualTo(LocalDate.of(1, 1, 1));
    }

    @Test
    void testValidate_parsesMaxDate() {
        var result = PeriodValidator.validate("9999-12-31", LocalDate.class);

        assertThat(result).isEqualTo(LocalDate.of(9999, 12, 31));
    }

    @Test
    void testValidate_throwsException_invalidLocalDateFormat_slashes() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("2024/02/15", LocalDate.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidLocalDateFormat_reverseOrder() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("15-02-2024", LocalDate.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidLocalDateFormat_shortYear() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("24-02-15", LocalDate.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidLocalDate_invalidMonth() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("2024-13-01", LocalDate.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidLocalDate_invalidDay() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("2024-02-30", LocalDate.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidLocalDate_nonLeapYear() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("2023-02-29", LocalDate.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidYearMonthFormat_slashes() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("2024/02", YearMonth.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidYearMonthFormat_reverseOrder() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("02-2024", YearMonth.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidYearMonth_invalidMonth() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("2024-13", YearMonth.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidYearFormat_shortYear() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("24", Year.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_invalidYearFormat_letters() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("not-a-year", Year.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_emptyString() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("", LocalDate.class));

        assertThat(thrown).isInstanceOf(InvalidPeriodTypeException.class);
    }

    @Test
    void testValidate_throwsException_unsupportedType() {
        var thrown = catchThrowable(() -> PeriodValidator.validate("2024-02-15T10:00:00", LocalDateTime.class));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown.getMessage()).contains("Period type not supported");
    }

    @Test
    void testValidate_throwsException_nullString() {
        var thrown = catchThrowable(() -> PeriodValidator.validate(null, LocalDate.class));

        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }
}
