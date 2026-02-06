package com.sait.workshop05;

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
 * Controller for the registration view
 */
public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label roleLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private String selectedRole;

    @FXML
    private void initialize() {
        System.out.println("RegistrationController initialized");

        // Hide labels initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        if (successLabel != null) {
            successLabel.setVisible(false);
        }

        // Add enter key handler for registration
        if (confirmPasswordField != null) {
            confirmPasswordField.setOnAction(event -> handleRegister());
        }
    }

    /**
     * Set the selected role (called from LoginController)
     */
    public void setSelectedRole(String role) {
        this.selectedRole = role;
        if (roleLabel != null) {
            if (role.equals("EMPLOYEE")) {
                roleLabel.setText("Employee Registration");
            } else {
                roleLabel.setText("Customer Registration");
            }
        }
        System.out.println("Registration for role: " + role);
    }

    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister() {
        hideMessages();

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input
        String validationError = validateInput(username, email, password, confirmPassword);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        // Check if username already exists
        if (AuthDAO.getUserByUsername(username) != null) {
            showError("Username already exists. Please choose a different username.");
            LogData.logAction("REGISTRATION_FAILED", "Username already exists: " + username);
            return;
        }

        // Check if email already exists
        if (AuthDAO.getUserByEmail(email) != null) {
            showError("Email already exists. Please use a different email.");
            LogData.logAction("REGISTRATION_FAILED", "Email already exists: " + email);
            return;
        }

        // Create new user
        User newUser = AuthDAO.registerUser(username, email, password, selectedRole);

        if (newUser != null) {
            // Registration successful
            LogData.logAction("REGISTRATION", "New user registered: " + username + " (Role: " + selectedRole + ")");
            showSuccess("Registration successful! You can now log in.");

            // Auto-login after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        // Create session and navigate to main view
                        UserSession.getInstance().createSession(newUser);
                        try {
                            if (selectedRole.equals("EMPLOYEE") || selectedRole.equals("ADMIN")) {
                                openEmployeeMainView();
                            } else {
                                openCustomerMainView();
                            }
                        } catch (IOException e) {
                            showError("Error loading application: " + e.getMessage());
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            showError("Registration failed. Please try again.");
            LogData.logAction("REGISTRATION_FAILED", "Database error for username: " + username);
        }
    }

    /**
     * Validate registration input
     */
    private String validateInput(String username, String email, String password, String confirmPassword) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            return "All fields are required";
        }

        if (username.length() < 3) {
            return "Username must be at least 3 characters";
        }

        if (username.length() > 50) {
            return "Username must be less than 50 characters";
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Invalid email format";
        }

        if (email.length() > 254) {
            return "Email must be less than 254 characters";
        }

        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }

        return null;
    }

    /**
     * Handle back button click
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());

            LoginController loginController = loader.getController();
            loginController.setSelectedRole(selectedRole);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Peelin' Good - Login");

            // Resize back to login window size
            stage.setWidth(800);
            stage.setHeight(650);
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Error returning to login: " + e.getMessage());
            e.printStackTrace();
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
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    /**
     * Hide all messages
     */
    private void hideMessages() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        if (successLabel != null) {
            successLabel.setVisible(false);
        }
    }

    /**
     * Open employee/admin main view
     */
    private void openEmployeeMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Peelin' Good - Management System");
        stage.setWidth(1400);
        stage.setHeight(850);
        stage.setMinWidth(1200);
        stage.setMinHeight(750);
        stage.centerOnScreen();
    }

    /**
     * Open customer main view
     */
    private void openCustomerMainView() throws IOException {
        // For now, show a placeholder alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Portal");
        alert.setHeaderText("Welcome to Customer Portal");
        alert.setContentText("Customer interface will be implemented in Phase 3.\n\n" +
                           "You are now logged in as: " + UserSession.getInstance().getCurrentUser().getUsername());
        alert.showAndWait();

        // For testing, just open the employee view
        openEmployeeMainView();
    }
}

