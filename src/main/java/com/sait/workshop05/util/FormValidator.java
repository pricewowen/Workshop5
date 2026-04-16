package com.sait.workshop05.util;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Collects rules per control, renders inline error labels, and reveals errors
 * on blur / after the first submit. Each {@code wrap()} returns a VBox holding
 * the original control plus its error label — add that to your layout instead
 * of the bare control.
 *
 * <pre>
 * FormValidator form = new FormValidator();
 * VBox usernameField = form.field(tfUsername)
 *         .required("Username is required")
 *         .min(3, "At least 3 characters")
 *         .wrap();
 * VBox pwField = form.field(pfPassword).required("Password is required").min(6, "Min 6 characters").wrap();
 * VBox confirmField = form.field(pfConfirm).required("Please confirm password").wrap();
 * form.match(pfPassword, pfConfirm, "Passwords do not match");
 * // ...in save handler:
 * if (!form.validateAll()) event.consume();
 * </pre>
 */
public final class FormValidator {

    public static final String ERROR_STYLE = "form-error";
    public static final String INVALID_STYLE = "field-invalid";

    private final List<FieldEntry> entries = new ArrayList<>();

    public FieldBuilder field(TextInputControl control) {
        FieldEntry e = new FieldEntry(control, () -> control.getText() == null ? "" : control.getText());
        entries.add(e);
        return new FieldBuilder(e);
    }

    public <T> ChoiceBuilder<T> choice(ComboBox<T> combo) {
        FieldEntry e = new FieldEntry(combo, () -> combo.getValue() == null ? "" : String.valueOf(combo.getValue()));
        entries.add(e);
        combo.valueProperty().addListener((obs, o, n) -> e.revalidateIfTouched());
        return new ChoiceBuilder<>(e);
    }

    /**
     * Adds a cross-field rule: {@code b}'s value must equal {@code a}'s value.
     * Both fields re-check this rule whenever either changes.
     */
    public FormValidator match(TextInputControl a, TextInputControl b, String message) {
        FieldEntry entryB = findEntry(b);
        if (entryB == null) {
            throw new IllegalStateException("Call field(confirm) before match(password, confirm, ...)");
        }
        entryB.rules.add(v -> a.getText() != null && a.getText().equals(v) ? null : message);
        a.textProperty().addListener((ChangeListener<String>) (obs, o, n) -> entryB.revalidateIfTouched());
        return this;
    }

    /** Validates every field, reveals all errors, returns true when all pass. */
    public boolean validateAll() {
        boolean allOk = true;
        for (FieldEntry e : entries) {
            e.touched = true;
            if (!e.apply()) allOk = false;
        }
        return allOk;
    }

    private FieldEntry findEntry(Control c) {
        for (FieldEntry e : entries) if (e.control == c) return e;
        return null;
    }

    // ─────────────────────────── Builders ───────────────────────────

    public static final class FieldBuilder {
        private final FieldEntry entry;

        private FieldBuilder(FieldEntry entry) { this.entry = entry; }

        public FieldBuilder required(String message) {
            entry.rules.add(v -> v.trim().isEmpty() ? message : null);
            return this;
        }

        public FieldBuilder min(int length, String message) {
            entry.rules.add(v -> v.length() >= length ? null : message);
            return this;
        }

        public FieldBuilder max(int length, String message) {
            entry.rules.add(v -> v.length() <= length ? null : message);
            return this;
        }

        public FieldBuilder matches(String regex, String message) {
            entry.rules.add(v -> v.matches(regex) ? null : message);
            return this;
        }

        /** Custom rule: return null if valid, otherwise the error message. */
        public FieldBuilder rule(Function<String, String> check) {
            entry.rules.add(check::apply);
            return this;
        }

        public Node wrap() { return entry.wrap(); }
    }

    public static final class ChoiceBuilder<T> {
        private final FieldEntry entry;

        private ChoiceBuilder(FieldEntry entry) { this.entry = entry; }

        public ChoiceBuilder<T> required(String message) {
            entry.rules.add(v -> v.isEmpty() ? message : null);
            return this;
        }

        public Node wrap() { return entry.wrap(); }
    }

    // ─────────────────────────── Internal ───────────────────────────

    private interface Rule { String check(String value); }

    private static final class FieldEntry {
        final Control control;
        final Label error = new Label();
        final List<Rule> rules = new ArrayList<>();
        final java.util.function.Supplier<String> reader;
        boolean touched = false;
        VBox wrapper;

        FieldEntry(Control c, java.util.function.Supplier<String> reader) {
            this.control = c;
            this.reader = reader;
            error.getStyleClass().add(ERROR_STYLE);
            error.setWrapText(true);
            error.setVisible(false);
            error.setManaged(false);
            if (c instanceof TextInputControl tic) {
                tic.textProperty().addListener((obs, o, n) -> revalidateIfTouched());
            }
            c.focusedProperty().addListener((obs, was, now) -> {
                if (was && !now) {
                    touched = true;
                    apply();
                }
            });
        }

        /** Returns true if all rules pass. Reveals/hides the error label and toggles invalid style. */
        boolean apply() {
            String msg = null;
            String value = reader.get();
            for (Rule r : rules) {
                msg = r.check(value);
                if (msg != null) break;
            }
            boolean ok = msg == null;
            if (ok || !touched) {
                error.setVisible(false);
                error.setManaged(false);
                control.getStyleClass().remove(INVALID_STYLE);
            } else {
                error.setText(msg);
                error.setVisible(true);
                error.setManaged(true);
                if (!control.getStyleClass().contains(INVALID_STYLE)) {
                    control.getStyleClass().add(INVALID_STYLE);
                }
            }
            return ok;
        }

        void revalidateIfTouched() {
            if (touched) apply();
        }

        VBox wrap() {
            if (wrapper == null) {
                control.setMaxWidth(Double.MAX_VALUE);
                wrapper = new VBox(4, control, error);
                wrapper.setMaxWidth(Double.MAX_VALUE);
            }
            return wrapper;
        }
    }
}
