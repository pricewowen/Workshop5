package com.sait.workshop05;

import com.sait.workshop05.api.ApiClient;
import com.sait.workshop05.util.StageIconHelper;
import com.sait.workshop05.util.StageSizing;
import io.sentry.Sentry;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Sentry.init(options -> {
            options.setEnableExternalConfiguration(true);
            options.setTag("platform", "desktop");
        });

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                Sentry.withScope(scope -> {
                    scope.setLevel(io.sentry.SentryLevel.FATAL);
                    Sentry.captureException(throwable);
                })
        );

        try {
            var ping = ApiClient.getInstance().get("/api/v1/tags");
            if (ping.statusCode() >= 500) {
                throw new RuntimeException("API error " + ping.statusCode());
            }
        } catch (Exception e) {
            Sentry.withScope(scope -> {
                scope.setLevel(io.sentry.SentryLevel.FATAL);
                Sentry.captureException(e);
            });
            e.printStackTrace();
            System.err.println("Cannot reach deployed Workshop 7 API at https://peelin-good-kdeft.ondigitalocean.app.");
            System.exit(1);
        }

        // Load login view (staff-only: Admin / Employee)
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(MainApplication.class.getResource("styles.css").toExternalForm());

        stage.setTitle("Login");
        StageIconHelper.setAppIcon(stage);
        stage.setScene(scene);

        StageSizing.applyLoginShellBounds(stage);
        stage.setResizable(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
