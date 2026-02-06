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
 * Controller for the login view
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button backButton;

    @FXML
    private Label roleLabel;

    @FXML
    private Label errorLabel;

    private String selectedRole;

    @FXML
    private void initialize() {
        System.out.println("LoginController initialized");

        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // Add enter key handler for login
        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }
    }

    /**
     * Set the selected role (called from RoleSelectionController)
     */
    public void setSelectedRole(String role) {
        this.selectedRole = role;
        if (roleLabel != null) {
            if (role.equals("EMPLOYEE")) {
                roleLabel.setText("Employee/Admin Login");
            } else {
                roleLabel.setText("Customer Login");
            }
        }
        System.out.println("Selected role: " + role);
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
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

            // Navigate to appropriate main view
            try {
                if (selectedRole.equals("EMPLOYEE") || selectedRole.equals("ADMIN")) {
                    openEmployeeMainView();
                } else {
                    openCustomerMainView();
                }
            } catch (IOException e) {
                showError("Error loading application: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Invalid username or password");
            LogData.logAction("LOGIN_FAILED", "Failed login attempt for username: " + username);
        }
    }

    /**
     * Handle back button click
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("role-selection-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Peelin' Good - Select Role");
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Error returning to role selection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle register button click - navigate to registration view
     */
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registration-view.fxml"));
            Scene scene = new Scene(loader.load());

            RegistrationController registrationController = loader.getController();
            registrationController.setSelectedRole(selectedRole);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Peelin' Good - Create Account");

            // Set window size to accommodate registration form content
            stage.setWidth(850);
            stage.setHeight(850);
            stage.setMinWidth(800);
            stage.setMinHeight(800);

            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Error loading registration form: " + e.getMessage());
            System.err.println("Error loading registration view: " + e.getMessage());
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
    }

    /**
     * Open employee/admin main view
     */
    private void openEmployeeMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
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

