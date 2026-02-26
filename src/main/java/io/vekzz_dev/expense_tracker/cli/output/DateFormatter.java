package io.vekzz_dev.expense_tracker.cli.output;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateFormatter {

    private DateFormatter() {
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd h:mm a", Locale.ENGLISH);

        return dateTime.format(formatter);
    }
}
