// Contributor(s): Owen
// Main: Owen - String helpers for parsing and display.

package com.sait.workshop05.util;

import java.util.regex.Pattern;

/**
 * Shared string utility methods used across controllers.
 */
public final class StringUtil {

    /** Shared email validation pattern for form checks. */
    public static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    /** Shared phone validation pattern for permissive input matching. */
    public static final Pattern PHONE_RX = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private StringUtil() {} // Prevent instantiation of static helpers.

    /** Case-insensitive contains helper for table filter predicates. */
    public static boolean containsIgnoreCase(String field, String q) {
        if (field == null) return false;
        return field.toLowerCase().contains(q);
    }

    /** Returns empty string when value is null. */
    public static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Trims and normalizes null to empty for safe text input checks. */
    public static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /** Trims value and returns null when no visible characters remain. */
    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
