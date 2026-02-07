package com.sait.workshop05.controllers;

import com.sait.workshop05.database.AuthDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.User;
import com.sait.workshop05.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login view.
 * Handles authentication for Admin and Employee roles only.
 */
public class LoginController {

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // Default role selection
        if (roleComboBox != null) {
            roleComboBox.getSelectionModel().selectFirst();
        }

        // Enter key on password field triggers login
        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        // Validate role selection
        String selectedRole = roleComboBox.getValue();
        if (selectedRole == null || selectedRole.isEmpty()) {
            showError("Please select a role");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Attempt authentication
        User user = AuthDAO.authenticate(username, password, selectedRole);

        if (user != null) {
            // Create session
            UserSession.getInstance().createSession(user);
            LogData.logAction("LOGIN", "User logged in: " + username + " (Role: " + selectedRole + ")");

            // Navigate to main management view
            try {
                openMainView();
            } catch (IOException e) {
                showError("Error loading application: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Invalid username or password");
            LogData.logAction("LOGIN_FAILED", "Failed login attempt for username: " + username + " (Role: " + selectedRole + ")");
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    /**
     * Open the main management view after successful login
     */
    private void openMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sait/workshop05/main-view.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Peelin' Good - Management System");
        stage.setWidth(1400);
        stage.setHeight(850);
        stage.setMinWidth(1200);
        stage.setMinHeight(750);
        stage.centerOnScreen();
    }
}
