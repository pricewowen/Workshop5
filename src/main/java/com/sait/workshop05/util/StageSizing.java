package com.sait.workshop05.util;

import javafx.stage.Stage;

/**
 * Default stage dimensions for the main shell: first-run login, post-login main view, and
 * login after logout all use the same bounds via {@link #applyMainShellBounds(Stage)}.
 */
public final class StageSizing {

    private StageSizing() {
    }

    public static final double MAIN_WIDTH = 1400;
    public static final double MAIN_HEIGHT = 850;
    public static final double MAIN_MIN_WIDTH = 1200;
    public static final double MAIN_MIN_HEIGHT = 750;

    /**
     * Sets min size first, then outer size, then centers — same sequence for cold boot, login,
     * and logout so the window geometry always matches.
     */
    public static void applyMainShellBounds(Stage stage) {
        if (stage == null) {
            return;
        }
        stage.setMinWidth(MAIN_MIN_WIDTH);
        stage.setMinHeight(MAIN_MIN_HEIGHT);
        stage.setWidth(MAIN_WIDTH);
        stage.setHeight(MAIN_HEIGHT);
        stage.centerOnScreen();
    }
}
