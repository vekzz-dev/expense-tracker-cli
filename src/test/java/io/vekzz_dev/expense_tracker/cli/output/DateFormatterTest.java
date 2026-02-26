package io.vekzz_dev.expense_tracker.cli.output;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DateFormatterTest {

    @Test
    void testFormatDateTime_formatsMidnightCorrectly() {
        var dateTime = LocalDateTime.of(2024, 2, 15, 0, 0, 0);

        var result = DateFormatter.formatDateTime(dateTime);

        assertThat(result).isEqualTo("2024-02-15 12:00 AM");
    }

    @Test
    void testFormatDateTime_formatsNoonCorrectly() {
        var dateTime = LocalDateTime.of(2024, 2, 15, 12, 0, 0);

        var result = DateFormatter.formatDateTime(dateTime);

        assertThat(result).isEqualTo("2024-02-15 12:00 PM");
    }

    @Test
    void testFormatDateTime_formatsEveningCorrectly() {
        var dateTime = LocalDateTime.of(2024, 2, 15, 20, 30, 45);

        var result = DateFormatter.formatDateTime(dateTime);

        assertThat(result).isEqualTo("2024-02-15 8:30 PM");
    }

    @Test
    void testFormatDateTime_usesEnglishLocale() {
        var dateTime = LocalDateTime.of(2024, 2, 15, 10, 30, 0);

        var result = DateFormatter.formatDateTime(dateTime);

        assertThat(result).contains("AM");
    }

    @Test
    void testFormatDateTime_formatsWithSeconds() {
        var dateTime = LocalDateTime.of(2024, 2, 15, 14, 30, 45);

        var result = DateFormatter.formatDateTime(dateTime);

        assertThat(result).isEqualTo("2024-02-15 2:30 PM");
    }

    @Test
    void testFormatDateTime_formatsMultipleDates() {
        var date1 = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        var date2 = LocalDateTime.of(2024, 6, 15, 15, 30, 0);
        var date3 = LocalDateTime.of(2024, 12, 15, 23, 59, 59);

        var result1 = DateFormatter.formatDateTime(date1);
        var result2 = DateFormatter.formatDateTime(date2);
        var result3 = DateFormatter.formatDateTime(date3);

        assertThat(result1).isEqualTo("2024-01-15 10:00 AM");
        assertThat(result2).isEqualTo("2024-06-15 3:30 PM");
        assertThat(result3).isEqualTo("2024-12-15 11:59 PM");
    }

    @Test
    void testFormatDateTime_formatsEarlyMorning() {
        var dateTime = LocalDateTime.of(2024, 2, 15, 1, 15, 30);

        var result = DateFormatter.formatDateTime(dateTime);

        assertThat(result).isEqualTo("2024-02-15 1:15 AM");
    }

    @Test
    void testFormatDateTime_formatsLateAfternoon() {
        var dateTime = LocalDateTime.of(2024, 2, 15, 16, 45, 0);

        var result = DateFormatter.formatDateTime(dateTime);

        assertThat(result).isEqualTo("2024-02-15 4:45 PM");
    }
}
