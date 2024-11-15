package com.example.yoga_dodanhtuyen;

public class YogaUtils {

    private YogaUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String formatDuration(int hours, int minutes, int seconds) {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
