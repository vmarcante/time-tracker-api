package com.vmarcante.time_tracker.core.shared.utils;

import java.util.Arrays;

public final class StringValidationUtils {

    private static final String LETTERS_AND_SPACES_BASE = "^[a-zA-ZÀ-ÿ\\s";
    private static final String PATTERN_SUFFIX = "]+$";
    private static final String MULTIPLE_SPACES_PATTERN = ".*\\s{2,}.*";
    private static final String CONTAINS_NUMBERS_PATTERN = ".*\\d.*";
    private static final String REGEX_SPECIAL_CHARS_PATTERN = "([\\[\\](){}.*+?^$|\\\\])";
    private static final String REGEX_ESCAPE_REPLACEMENT = "\\\\$1";

    private static final int[][] EMOJI_RANGES = {
            { 0x1F600, 0x1F64F }, // Emoticons
            { 0x1F300, 0x1F5FF }, // Misc Symbols and Pictographs
            { 0x1F680, 0x1F6FF }, // Transport and Map
            { 0x1F1E0, 0x1F1FF }, // Flags
            { 0x2600, 0x26FF }, // Misc symbols
            { 0x2700, 0x27BF }, // Dingbats
            { 0xFE00, 0xFE0F }, // Variation Selectors
            { 0x1F900, 0x1F9FF }, // Supplemental Symbols and Pictographs
            { 0x1FA70, 0x1FAFF }, // Symbols and Pictographs Extended-A
    };

    private StringValidationUtils() {
    }

    public static boolean containsEmoji(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return text.codePoints().anyMatch(
                codePoint -> Arrays.stream(EMOJI_RANGES)
                        .anyMatch(range -> codePoint >= range[0] && codePoint <= range[1]));
    }

    public static boolean containsOnlyLettersAndAllowed(String text, String allowedChars) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String pattern = LETTERS_AND_SPACES_BASE + escapeRegex(allowedChars) + PATTERN_SUFFIX;
        return text.matches(pattern);
    }

    public static boolean containsMultipleSpaces(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return text.matches(MULTIPLE_SPACES_PATTERN);
    }

    public static boolean containsNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return text.matches(CONTAINS_NUMBERS_PATTERN);
    }

    private static String escapeRegex(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.replaceAll(REGEX_SPECIAL_CHARS_PATTERN, REGEX_ESCAPE_REPLACEMENT);
    }

    public static boolean containsContent(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
