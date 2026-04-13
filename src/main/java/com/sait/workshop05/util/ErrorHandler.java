package com.sait.workshop05.util;

import com.sait.workshop05.logging.LogData;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.sql.SQLException;
import java.util.List;

/**
 * Centralised error handling utility (Phase 12).
 * Provides consistent dialog display and DB error translation.
 */
public class ErrorHandler {

    /**
     * Show an error dialog with title and message.
     */
    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message != null ? sanitizeApiError(message) : null);
        alert.showAndWait();
    }

    /**
     * Show an error dialog with title, header, and detailed content.
     * Content is sanitized to remove raw server/HTTP responses.
     */
    public static void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content != null ? sanitizeApiError(content) : null);
        alert.showAndWait();
    }

    /**
     * Same as {@link #showErrorDialog(String, String, String)} but derives a short, user-safe
     * detail line from {@code cause} (HTTP codes, JSON {@code message}, SQL states, stack-trace trim).
     */
    public static void showErrorDialog(String title, String header, Throwable cause) {
        if (cause == null) {
            showErrorDialog(title, header, (String) null);
            return;
        }
        showErrorDialog(title, header, friendlyMessage(cause));
    }

    /**
     * Short detail text for inline labels or custom dialogs (no UI).
     */
    public static String userFacingMessage(Throwable t) {
        if (t == null) {
            return null;
        }
        return friendlyMessage(t);
    }

    /**
     * Show a formatted list of validation errors.
     */
    public static void showValidationErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Please fix the following:\n\n");
        for (int i = 0; i < errors.size(); i++) {
            sb.append(i + 1).append(". ").append(errors.get(i)).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText("Form validation failed");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(Math.min(errors.size() + 3, 12));
        textArea.setStyle("-fx-font-size: 13px;");

        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.showAndWait();
    }

    /**
     * Show a warning dialog.
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show an informational dialog.
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Translate a SQLException into a user-friendly message.
     * Follows the friendlyDbMessage() pattern from EmployeeManagementController.
     */
    public static String friendlyDbMessage(SQLException ex) {
        String sqlState = ex.getSQLState();
        String msg = (ex.getMessage() == null) ? "" : ex.getMessage().toLowerCase();

        if (sqlState != null && sqlState.startsWith("23")) {
            // Integrity constraint violations
            if (msg.contains("duplicate")) {
                return "A record with those values already exists (duplicate entry).";
            }
            if (msg.contains("check constraint") || msg.contains("chk_")) {
                return "A value violates a database rule (e.g., negative price or invalid format).";
            }
            if (msg.contains("foreign key constraint") || msg.contains("fk_")) {
                return "This record is referenced by other data. Remove those references first.";
            }
            return "This operation violates a database constraint.";
        }

        if (sqlState != null && sqlState.startsWith("42")) {
            // Syntax or access errors
            if (msg.contains("doesn't exist") || msg.contains("does not exist")) {
                return "A required database table does not exist. Check the database schema.";
            }
            return "Database access or syntax error.";
        }

        if (sqlState != null && sqlState.startsWith("08")) {
            return "Could not connect to the database. Check your connection settings.";
        }

        String original = ex.getMessage();
        if (original == null || original.isBlank()) {
            return "Unknown database error.";
        }
        return sanitizeApiError(original);
    }

    /**
     * Handle an exception: log it and show a dialog.
     */
    public static void handleException(String context, Exception e) {
        LogData.handleException(context, e);
        showErrorDialog("Error", context, friendlyMessage(e));
    }

    private static String friendlyMessage(Throwable e) {
        if (causeChainContains(e, UnknownHostException.class) || causeChainContains(e, ConnectException.class)) {
            return "Could not reach the server. Check your internet connection.";
        }
        if (causeChainContains(e, HttpTimeoutException.class)) {
            return "The request timed out. Please try again.";
        }
        if (e instanceof SQLException) {
            return friendlyDbMessage((SQLException) e);
        }
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return "An unexpected error occurred.";
        }
        return sanitizeApiError(msg);
    }

    private static boolean causeChainContains(Throwable e, Class<? extends Throwable> type) {
        Throwable t = e;
        int depth = 0;
        while (t != null && depth++ < 12) {
            if (type.isInstance(t)) {
                return true;
            }
            Throwable next = t.getCause();
            if (next == t) {
                break;
            }
            t = next;
        }
        return false;
    }

    private static String sanitizeApiError(String message) {
        if (message == null || message.isBlank()) {
            return "Something went wrong. Please try again.";
        }
        message = message.trim();
        int nl = message.indexOf('\n');
        if (nl > 0 && (message.contains("\tat ") || message.length() > 400)) {
            message = message.substring(0, nl).trim();
        }
        message = message.replaceFirst("(?i)^java\\.lang\\.runtimeexception:\\s*", "");
        message = message.replaceFirst("(?i)^java\\.lang\\.exception:\\s*", "");
        message = message.replaceFirst("(?i)^java\\.io\\.ioexception:\\s*", "");
        String extracted = tryExtractJsonMessage(message);
        if (extracted != null && !extracted.isBlank()) {
            message = extracted;
        }
        if (message.matches("(?s).*(failed|error): \\d{3}.*")) {
            int code = extractHttpStatusCode(message);
            if (code == 401 || code == 403) return "You do not have permission to perform this action.";
            if (code == 404) return "The requested resource was not found.";
            if (code == 409) return "A conflict occurred — this record may already exist.";
            if (code >= 500) return "A server error occurred. Please try again later.";
            if (code > 0) return "Request failed (HTTP " + code + "). Please try again.";
        }
        if (message.length() > 200) return message.substring(0, 200) + "...";
        return message;
    }

    /**
     * Pull a short server message out of JSON error bodies (e.g. Spring {@code {"message":"..."}}).
     */
    private static String tryExtractJsonMessage(String raw) {
        int i = raw.indexOf('{');
        if (i < 0) return null;
        String json = raw.substring(i);
        for (String key : new String[] {"\"message\"", "\"detail\"", "\"error\""}) {
            int k = json.indexOf(key);
            if (k < 0) continue;
            int colon = json.indexOf(':', k + key.length());
            if (colon < 0) continue;
            int start = colon + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            if (start >= json.length()) continue;
            if (json.charAt(start) == '"') {
                StringBuilder sb = new StringBuilder();
                for (int p = start + 1; p < json.length(); p++) {
                    char c = json.charAt(p);
                    if (c == '\\' && p + 1 < json.length()) {
                        sb.append(json.charAt(p + 1));
                        p++;
                        continue;
                    }
                    if (c == '"') break;
                    sb.append(c);
                }
                String s = sb.toString().trim();
                if (!s.isEmpty()) return s;
            }
        }
        return null;
    }

    private static int extractHttpStatusCode(String message) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{3})").matcher(message);
        while (m.find()) {
            int code = Integer.parseInt(m.group(1));
            if (code >= 100 && code < 600) return code;
        }
        return -1;
    }
}
