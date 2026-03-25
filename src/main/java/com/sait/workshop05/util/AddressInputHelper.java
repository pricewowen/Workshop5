package com.sait.workshop05.util;

import com.sait.workshop05.models.AddressOption;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Editable {@link ComboBox} helper: type-to-filter against a master address list.
 * Does not write to the database; persistence stays in Create/Update/Place Order handlers.
 */
public final class AddressInputHelper {

    private static final String KEY_MASTER = "addressMasterItems";
    private static final String KEY_FILTERED = "addressFilteredItems";
    private static final String KEY_SUPPRESS = "addressFilterSuppress";

    private AddressInputHelper() {}

    public static void configureEditableAddressCombo(ComboBox<AddressOption> combo) {
        combo.setEditable(true);

        ObservableList<AddressOption> master = FXCollections.observableArrayList();
        FilteredList<AddressOption> filtered = new FilteredList<>(master, a -> true);

        combo.getProperties().put(KEY_MASTER, master);
        combo.getProperties().put(KEY_FILTERED, filtered);
        combo.setItems(filtered);

        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(AddressOption option) {
                return option == null ? "" : formatAddress(option);
            }

            @Override
            public AddressOption fromString(String string) {
                if (string == null) return null;
                String s = string.trim();
                if (s.isEmpty()) return null;

                ObservableList<AddressOption> m = getMaster(combo);
                if (m == null) return null;

                for (AddressOption option : m) {
                    if (formatAddress(option).equalsIgnoreCase(s) || option.toString().equalsIgnoreCase(s)) {
                        return option;
                    }
                }
                return null;
            }
        });

        combo.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (Boolean.TRUE.equals(combo.getProperties().get(KEY_SUPPRESS))) {
                return;
            }
            FilteredList<AddressOption> fl = getFiltered(combo);
            if (fl == null) {
                return;
            }

            String q = newText == null ? "" : newText.trim().toLowerCase();
            fl.setPredicate(a -> q.isEmpty() || matches(a, q));
        });

        // Do not commit / default-button activate from the address field; saving is via form buttons only.
        combo.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
            }
        });
    }

    public static void setAddressItems(ComboBox<AddressOption> combo, List<AddressOption> addresses) {
        ObservableList<AddressOption> master = getMaster(combo);
        if (master == null) {
            throw new IllegalStateException("Call configureEditableAddressCombo before setAddressItems");
        }
        master.setAll(addresses);
    }

    public static String getTypedText(ComboBox<AddressOption> combo) {
        if (combo.getEditor() == null) return "";
        String text = combo.getEditor().getText();
        return text == null ? "" : text.trim();
    }

    public static String formatAddress(AddressOption option) {
        if (option == null) return "";
        String city = option.getCity() == null ? "" : option.getCity().trim();
        return option.getLine1() + ", " + city + ", " + option.getProvince() + " " + option.getPostalCode();
    }

    public static void runWithSuppress(ComboBox<AddressOption> combo, Runnable action) {
        combo.getProperties().put(KEY_SUPPRESS, true);
        try {
            action.run();
        } finally {
            combo.getProperties().put(KEY_SUPPRESS, false);
        }
    }

    /** Select by id from the full master list (not the current filter). Resets filter so the row is visible. */
    public static void selectAddressById(ComboBox<AddressOption> combo, int addressId) {
        ObservableList<AddressOption> master = getMaster(combo);
        FilteredList<AddressOption> fl = getFiltered(combo);
        if (master == null) {
            return;
        }

        for (AddressOption a : master) {
            if (a.getAddressId() == addressId) {
                runWithSuppress(combo, () -> {
                    if (fl != null) {
                        fl.setPredicate(x -> true);
                    }
                    combo.setValue(a);
                    combo.getEditor().setText(formatAddress(a));
                });
                return;
            }
        }

        runWithSuppress(combo, () -> {
            if (fl != null) {
                fl.setPredicate(x -> true);
            }
            combo.getSelectionModel().clearSelection();
            combo.setValue(null);
            combo.getEditor().clear();
        });
    }

    public static void clearAddressField(ComboBox<AddressOption> combo) {
        FilteredList<AddressOption> fl = getFiltered(combo);
        runWithSuppress(combo, () -> {
            if (fl != null) {
                fl.setPredicate(x -> true);
            }
            combo.getSelectionModel().clearSelection();
            combo.setValue(null);
            if (combo.getEditor() != null) {
                combo.getEditor().clear();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static ObservableList<AddressOption> getMaster(ComboBox<AddressOption> combo) {
        Object v = combo.getProperties().get(KEY_MASTER);
        return v instanceof ObservableList<?> ? (ObservableList<AddressOption>) v : null;
    }

    @SuppressWarnings("unchecked")
    private static FilteredList<AddressOption> getFiltered(ComboBox<AddressOption> combo) {
        Object v = combo.getProperties().get(KEY_FILTERED);
        return v instanceof FilteredList<?> ? (FilteredList<AddressOption>) v : null;
    }

    private static boolean matches(AddressOption option, String q) {
        return formatAddress(option).toLowerCase().contains(q)
                || option.toString().toLowerCase().contains(q);
    }
}
