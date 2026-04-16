package com.sait.workshop05.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;

/**
 * Configures dialogs to be resizable and responsive to screen size.
 * The dialog grows when content (e.g. validation errors) expands, and
 * its width/height are capped relative to the primary screen.
 *
 * <pre>DialogHelper.configureResponsive(dialog, 440);</pre>
 */
public final class DialogHelper {

    private DialogHelper() {}

    /**
     * Makes the dialog resizable, auto-growing with content, and
     * constrains dimensions to the current screen.
     *
     * @param dialog       the dialog to configure
     * @param minWidth     minimum width in pixels (use 0 to skip)
     */
    public static void configureResponsive(Dialog<?> dialog, double minWidth) {
        dialog.setResizable(true);

        DialogPane pane = dialog.getDialogPane();
        pane.setMinHeight(Region.USE_PREF_SIZE);

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        if (minWidth > 0) {
            pane.setMinWidth(minWidth);
        }
        pane.setMaxWidth(Math.min(screen.getWidth() * 0.7, 800));
        pane.setMaxHeight(screen.getHeight() * 0.85);
    }
}
