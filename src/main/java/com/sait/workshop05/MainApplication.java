package com.sait.workshop05;

import com.sait.workshop05.database.DBUtil;
import com.sait.workshop05.util.StageIconHelper;
import io.sentry.Sentry;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

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

        /*
        // Test DB connection on startup
        try (Connection conn = DBUtil.getConnection()) {
            // Connection successful
        } catch (Exception e) {
            Sentry.withScope(scope -> {
                scope.setLevel(io.sentry.SentryLevel.FATAL);
                Sentry.captureException(e);
            });
            e.printStackTrace();
            System.exit(1);
        }
        */

        // Load login view (staff-only: Admin / Employee)
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(MainApplication.class.getResource("styles.css").toExternalForm());

        stage.setTitle("Login");
        StageIconHelper.setAppIcon(stage);
        stage.setScene(scene);

        stage.setWidth(700);
        stage.setHeight(730);

        stage.setMinWidth(700);
        stage.setMinHeight(730);

        stage.setResizable(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
