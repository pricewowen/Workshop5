package com.sait.workshop05.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Objects;

/**
 * Window title-bar and (on most platforms) taskbar icon.
 * <p>
 * Loads synchronously from the module path. Tries {@code icon.png} first, then {@code bakery_logo.png}.
 * Ensure those files live under {@code src/main/resources/com/sait/workshop05/} so they are on the runtime module path.
 */
public final class StageIconHelper {

    private static final String[] ICON_PATHS = {
            "/com/sait/workshop05/icon.png",
            "/com/sait/workshop05/bakery_logo.png"
    };

    private StageIconHelper() {}

    public static void setAppIcon(Stage stage) {
        Objects.requireNonNull(stage, "stage");
        stage.getIcons().clear();

        for (String path : ICON_PATHS) {
            Image image = loadImageSync(path);
            if (image != null && !image.isError()) {
                stage.getIcons().add(image);
                // Windows shells often pick the first usable size; one image is enough.
                return;
            }
        }
    }

    /**
     * Synchronous load — required for stage icons on Windows (async URL load often never applies in time).
     */
    private static Image loadImageSync(String absolutePath) {
        // Prefer module resource lookup (correct for {@code java --module com.sait.workshop05/...}).
        Module mod = StageIconHelper.class.getModule();
        String modulePath = absolutePath.startsWith("/") ? absolutePath.substring(1) : absolutePath;
        try (InputStream in = mod.getResourceAsStream(modulePath)) {
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {
            // fall through
        }

        try (InputStream in = StageIconHelper.class.getResourceAsStream(absolutePath)) {
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {
            // fall through
        }

        return null;
    }
}
