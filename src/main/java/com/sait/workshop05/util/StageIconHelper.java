// Contributor(s): Samantha
// Main: Samantha - Application window icon loading for JavaFX stages.

package com.sait.workshop05.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Objects;

/**
 * Window title bar and taskbar icon on most platforms. Loads synchronously from icon.png then bakery_logo.png on
 * the module path under com.sait.workshop05 in resources.
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
                // Windows shells often pick the first usable size, one image is enough.
                return;
            }
        }
    }

    /**
     * Synchronous load so icons apply on Windows where async loads often miss the first paint.
     */
    private static Image loadImageSync(String absolutePath) {
        // Prefer module resource lookup for java --module launches.
        Module mod = StageIconHelper.class.getModule();
        String modulePath = absolutePath.startsWith("/") ? absolutePath.substring(1) : absolutePath;
        try (InputStream in = mod.getResourceAsStream(modulePath)) {
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {
            // Try classpath lookup next so modular and non-modular launches both work.
        }

        try (InputStream in = StageIconHelper.class.getResourceAsStream(absolutePath)) {
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {
            // Return null so caller can try the next icon candidate.
        }

        return null;
    }
}
