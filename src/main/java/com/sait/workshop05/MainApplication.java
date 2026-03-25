package com.sait.workshop05;

import com.sait.workshop05.database.DBUtil;
import com.sait.workshop05.util.StageIconHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // Test DB connection on startup
        try (Connection conn = DBUtil.getConnection()) {
            // Connection successful
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Load login view (staff-only: Admin / Employee)
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

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
