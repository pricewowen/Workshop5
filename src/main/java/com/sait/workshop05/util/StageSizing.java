// Contributor(s): Samantha
// Main: Samantha - Stage bounds presets for login and main shells.

package com.sait.workshop05.util;

import javafx.stage.Stage;

/**
 * Stage dimensions for the login shell ({@link #applyLoginShellBounds(Stage)}) and the
 * post-login main shell ({@link #applyMainShellBounds(Stage)}).
 */
public final class StageSizing {

    private StageSizing() {
    }

    public static final double MAIN_WIDTH = 1450;
    public static final double MAIN_HEIGHT = 900;
    public static final double MAIN_MIN_WIDTH = 1100;
    public static final double MAIN_MIN_HEIGHT = 650;

    public static final double LOGIN_WIDTH = 900;
    public static final double LOGIN_HEIGHT = 600;
    public static final double LOGIN_MIN_WIDTH = 780;
    public static final double LOGIN_MIN_HEIGHT = 540;

    /**
     * Sets min size then outer size then centers so cold boot and login and logout share the same geometry steps.
     */
    public static void applyMainShellBounds(Stage stage) {
        applyBounds(stage, MAIN_MIN_WIDTH, MAIN_MIN_HEIGHT, MAIN_WIDTH, MAIN_HEIGHT);
    }

    /**
     * Applies login shell dimensions used before authentication.
     */
    public static void applyLoginShellBounds(Stage stage) {
        applyBounds(stage, LOGIN_MIN_WIDTH, LOGIN_MIN_HEIGHT, LOGIN_WIDTH, LOGIN_HEIGHT);
    }

    private static void applyBounds(Stage stage, double minW, double minH, double w, double h) {
        if (stage == null) {
            return;
        }
        stage.setMinWidth(minW);
        stage.setMinHeight(minH);
        stage.setWidth(w);
        stage.setHeight(h);
        stage.centerOnScreen();
    }
}
