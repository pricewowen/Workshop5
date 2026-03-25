package com.sait.workshop05.controllers;

import com.sait.workshop05.database.AuthDAO;
import com.sait.workshop05.database.EmployeeAccessDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.User;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.StageIconHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        if (roleComboBox != null) {
            roleComboBox.getSelectionModel().selectFirst();
        }

        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }
    }

    @FXML
    private void handleLogin() {
        String selectedRole = roleComboBox.getValue();
        if (selectedRole == null || selectedRole.isEmpty()) {
            showError("Please select a role");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        User user = AuthDAO.authenticate(username, password, selectedRole);

        if (user != null) {
            UserSession session = UserSession.getInstance();
            session.createSession(user);

            // IMPORTANT: compute analytics eligibility for EMPLOYEE
            if (session.isEmployee()) {
                Integer employeeId = EmployeeAccessDAO.getEmployeeIdByUserId(user.getUserId());
                List<Integer> bakeryIds = EmployeeAccessDAO.getAccessibleBakeryIdsByUserId(user.getUserId());
                session.setEmployeeAnalyticsAccess(employeeId, bakeryIds);

                if (session.canAccessAnalytics()) {
                    LogData.logAction("LOGIN", "Employee login: " + username + " (Analytics ENABLED)");
                } else {
                    LogData.logAction("LOGIN", "Employee login: " + username + " (Analytics DISABLED: generic/no bakery access)");
                }
            } else {
                LogData.logAction("LOGIN", "Admin login: " + username);
            }

            try {
                openMainView();
            } catch (IOException e) {
                showError("Error loading application: " + e.getMessage());
                LogData.handleException("LOGIN_OPEN_MAIN", e);
            }

        } else {
            showError("Invalid username or password");
            LogData.logAction("LOGIN_FAILED", "Failed login attempt for username: " + username + " (Role: " + selectedRole + ")");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void openMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sait/workshop05/main-view.fxml"));
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(this.getClass().getResource("/com/sait/workshop05/styles.css").toString());

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Management System");
        StageIconHelper.setAppIcon(stage);
        stage.setWidth(1400);
        stage.setHeight(850);
        stage.setMinWidth(1200);
        stage.setMinHeight(750);
        stage.centerOnScreen();
    }
}