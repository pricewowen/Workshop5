package com.sait.workshop05.util;

import java.util.regex.Pattern;

/**
 * Shared string utility methods used across controllers.
 */
public final class StringUtil {

    public static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public static final Pattern PHONE_RX = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private StringUtil() {} // utility class

    /** Returns true if field (case-insensitive) contains the query string. */
    public static boolean containsIgnoreCase(String field, String q) {
        if (field == null) return false;
        return field.toLowerCase().contains(q);
    }

    /** Null to empty string. */
    public static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Null to empty trimmed string. */
    public static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /** Trim string, return null if empty. */
    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
