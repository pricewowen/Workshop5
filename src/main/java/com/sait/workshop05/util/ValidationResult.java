// Contributor(s): Owen
// Main: Owen - Validation result value type for form checks.

package com.sait.workshop05.util;

/**
 * Reusable validation result for form validation across controllers.
 */
public class ValidationResult {
    private final boolean ok;
    private final String message;

    private ValidationResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public boolean isOk() { return ok; }
    public String getMessage() { return message; }

    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult fail(String msg) {
        return new ValidationResult(false, msg);
    }
}
