package com.vmarcante.time_tracker.core.shared.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parses a date-time string using the provided formatter.
     * Returns null if the input is null or blank.
     */
    public static LocalDateTime parseDateTime(String dateTime, DateTimeFormatter formatter) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }

        try {
            String normalizedDateTime = normalizeMilliseconds(dateTime);
            return LocalDateTime.parse(normalizedDateTime, formatter);
        } catch (Exception e) {
            log.error("[Date Utils] Error parsing date time: {} | Formatter: {}", dateTime, formatter, e);
            return null;
        }
    }

    /**
     * Returns the start of the day (00:00:00) for the given date.
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Returns the end of the day (23:59:59) for the given date.
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }

    /**
     * Normalizes milliseconds in a date-time string to always have exactly 3 digits.
     * Truncates if more than 3 digits, pads with zeros if less than 3 digits.
     * Example: "2025-07-01 10:11:26.951"   -> "2025-07-01 10:11:26.951"
     *          "2025-07-01 10:11:26.99"    -> "2025-07-01 10:11:26.990"
     *          "2025-07-01 10:11:26.9"     -> "2025-07-01 10:11:26.900"
     *          "2026-07-31 13:10:31.061071" -> "2026-07-31 13:10:31.061"
     */
    public static String normalizeMilliseconds(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return dateTime;
        }
        int dotIndex = dateTime.lastIndexOf('.');
        if (dotIndex == -1) {
            return dateTime;
        }
        String beforeDot = dateTime.substring(0, dotIndex + 1);
        String afterDot = dateTime.substring(dotIndex + 1);
        if (afterDot.length() > 3) {
            afterDot = afterDot.substring(0, 3);
        } else {
            while (afterDot.length() < 3) {
                afterDot += "0";
            }
        }
        return beforeDot + afterDot;
    }

}
