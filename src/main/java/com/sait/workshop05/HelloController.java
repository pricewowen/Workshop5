package com.sait.workshop05;

import com.sait.workshop05.data.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        DatabaseConnection.getConnection();
    }
}