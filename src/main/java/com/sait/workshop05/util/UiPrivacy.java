// Contributor(s): Robbie
// Main: Robbie - Privacy-safe labels for orders and customer display.

package com.sait.workshop05.util;

import java.util.regex.Pattern;

/**
 * Hides UUID-shaped identifiers from user-visible strings where policy is to avoid exposing raw ids.
 */
public final class UiPrivacy {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private UiPrivacy() {}

    public static boolean looksLikeUuid(String s) {
        return s != null && UUID_PATTERN.matcher(s.trim()).matches();
    }

    /** Non-UUID strings pass through. UUID or blank input becomes a dash placeholder character. */
    public static String maskUuid(String s) {
        if (s == null || s.isBlank()) {
            return "—";
        }
        return looksLikeUuid(s) ? "—" : s;
    }

    /**
     * Short opaque token for UI when the real value is a UUID (e.g. reward row id).
     * Does not reveal the full identifier.
     */
    public static String compactRef(String s) {
        if (s == null || s.isBlank()) {
            return "—";
        }
        if (!looksLikeUuid(s)) {
            return s;
        }
        String hex = s.replace("-", "").toLowerCase();
        return hex.length() >= 8 ? "·" + hex.substring(0, 8).toUpperCase() : "—";
    }

    /**
     * Fallback customer label when the API did not send a display name. Blank id maps to Guest. Otherwise use Customer. Never show a raw UUID.
     */
    public static String customerDisplayFallback(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return "Guest";
        }
        return "Customer";
    }

    /**
     * Human-facing order label. Prefers API orderNumber when present. For UUID internal ids builds ORD- plus eight hex chars.
     */
    public static String friendlyOrderNumber(String apiOrderNumber, String internalId) {
        if (apiOrderNumber != null) {
            String t = apiOrderNumber.trim();
            if (!t.isEmpty() && !looksLikeUuid(t)) {
                return t;
            }
        }
        if (internalId == null || internalId.isBlank()) {
            return "—";
        }
        if (looksLikeUuid(internalId)) {
            String hex = internalId.replace("-", "").toLowerCase();
            if (hex.length() >= 8) {
                return "ORD-" + hex.substring(0, 8).toUpperCase();
            }
        }
        return internalId;
    }
}
