package com.sait.workshop05.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * Attaches a {@link TextFormatter} to a {@link TextField} that accepts only
 * digits and auto-formats them as {@code (123)-456-7890}. Input is capped at
 * 10 digits; non-digit characters typed by the user are silently ignored.
 *
 * <pre>PhoneFieldFormatter.apply(tfPhone);</pre>
 */
public final class PhoneFieldFormatter {

    private PhoneFieldFormatter() {}

    /** Attaches the formatter to the given field. */
    public static void apply(TextField field) {
        field.setTextFormatter(new TextFormatter<>(filter()));
        field.setPromptText("(123)-456-7890");
    }

    /** Strips formatting, returning raw digits only. */
    public static String rawDigits(TextField field) {
        String text = field.getText();
        return text == null ? "" : text.replaceAll("\\D", "");
    }

    /** Pre-formats an existing raw or formatted phone string into the field. */
    public static void setPhone(TextField field, String phone) {
        if (phone == null || phone.isBlank()) {
            field.setText("");
            return;
        }
        String digits = phone.replaceAll("\\D", "");
        field.setText(format(digits));
    }

    private static UnaryOperator<TextFormatter.Change> filter() {
        return change -> {
            if (!change.isContentChange()) return change;

            String proposed = change.getControlNewText();
            String digits = proposed.replaceAll("\\D", "");

            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            String formatted = format(digits);
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());

            int caretPos = formatted.length();
            change.selectRange(caretPos, caretPos);

            return change;
        };
    }

    private static String format(String digits) {
        int len = digits.length();
        if (len == 0) return "";
        if (len <= 3) return "(" + digits;
        if (len <= 6) return "(" + digits.substring(0, 3) + ")-" + digits.substring(3);
        return "(" + digits.substring(0, 3) + ")-" + digits.substring(3, 6) + "-" + digits.substring(6);
    }
}
