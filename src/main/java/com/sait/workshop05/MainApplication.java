package com.sait.workshop05;

import com.sait.workshop05.database.DBUtil;
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
        stage.setTitle("Peelin' Good - Login");
        stage.setScene(scene);

        stage.setWidth(800);
        stage.setHeight(600);

        stage.setMinWidth(800);
        stage.setMinHeight(600);

        stage.setResizable(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
