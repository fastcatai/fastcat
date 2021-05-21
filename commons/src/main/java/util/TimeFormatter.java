package util;

import javafx.util.Duration;

import java.util.Locale;

public final class TimeFormatter {
    public static String formatTime(Duration time) {
        return String.format(Locale.US,
                "%02d:%02d:%02d:%03d",
                (int) time.toHours(),
                (int) time.toMinutes() % 60,
                (int) time.toSeconds() % 60,
                (int) time.toMillis() % 1000);
    }

    public static String formatTimeDetailed(double millis) {
        return String.format(Locale.US,
                "%02d:%02d:%02d:%03d",
                (int) (millis / (60 * 60 * 1000.0)), // hours
                (int) ((millis / (60 * 1000.0)) % 60), // minutes
                (int) ((millis / 1000.0)) % 60, // seconds
                (int) (millis % 1000));
    }

    public static String formatTimeLong(double millis) {
        return String.format(Locale.US,
                "%02d:%02d:%06.3f",
                (int) (millis / (60 * 60 * 1000.0)), // hours
                (int) ((millis / (60 * 1000.0)) % 60), // minutes
                (millis / 1000.0));
    }

    public static String formatTimeMiddle(double millis) {
        return String.format(Locale.US,
                "%02d:%06.3f",
                (int) ((millis / (60 * 1000.0)) % 60), // minutes
                (millis / 1000.0));
    }

    public static String formatTimeShort(double millis) {
        int minutes = (int) ((millis / (60 * 1000.0)) % 60);
        double seconds = (millis / 1000.0);
        if (minutes <= 0)
            return String.format(Locale.US, "%.1f", seconds);
        else return String.format(Locale.US, "%02d:%.1f", minutes, seconds);
    }
}
