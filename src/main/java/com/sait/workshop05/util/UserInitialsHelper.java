// Contributor(s): Robbie
// Main: Robbie - Initials derivation for avatars and labels.

package com.sait.workshop05.util;

import java.util.Locale;

/**
 * Builds two letter style initials for avatars for example JD for John Doe or NM for Noah Martin.
 */
public final class UserInitialsHelper {

    private UserInitialsHelper() {}

    /**
     * Builds avatar initials from name fields and falls back to username.
     */
    public static String compute(String firstName, String lastName, String username) {
        String f = firstName != null ? firstName.trim() : "";
        String l = lastName != null ? lastName.trim() : "";
        if (!f.isEmpty() && !l.isEmpty()) {
            return (initialLetter(f) + initialLetter(l)).toUpperCase(Locale.ROOT);
        }
        if (!f.isEmpty()) {
            return f.length() >= 2
                    ? f.substring(0, 2).toUpperCase(Locale.ROOT)
                    : f.substring(0, 1).toUpperCase(Locale.ROOT);
        }
        String u = username != null ? username.trim() : "";
        if (u.isEmpty()) {
            return "?";
        }
        String[] parts = u.split("[^a-zA-Z0-9]+");
        if (parts.length >= 2) {
            String a = parts[0];
            String b = parts[parts.length - 1];
            if (!a.isEmpty() && !b.isEmpty()) {
                return (initialLetter(a) + initialLetter(b)).toUpperCase(Locale.ROOT);
            }
        }
        return u.length() >= 2 ? u.substring(0, 2).toUpperCase(Locale.ROOT) : u.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private static String initialLetter(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return String.valueOf(s.charAt(0));
    }
}
