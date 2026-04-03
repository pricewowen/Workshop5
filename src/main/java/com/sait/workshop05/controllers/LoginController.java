package com.sait.workshop05.controllers;

import com.sait.workshop05.api.ApiClient;
import com.sait.workshop05.api.dto.AuthResponseDto;
import com.sait.workshop05.api.dto.LoginRequestDto;
import com.sait.workshop05.database.EmployeeAccessDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.User;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.StageIconHelper;
import io.sentry.Sentry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class LoginController {

    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (roleComboBox != null) roleComboBox.getSelectionModel().selectFirst();
        if (passwordField != null) passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String selectedRole = roleComboBox.getValue();
        if (selectedRole == null || selectedRole.isEmpty()) {
            showError("Please select a role");
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        try {
            ApiClient api = ApiClient.getInstance();
            HttpResponse<String> response = api.post("/api/v1/auth/login", new LoginRequestDto(email, password));

            if (response.statusCode() == 200) {
                AuthResponseDto auth = api.getMapper().readValue(response.body(), AuthResponseDto.class);

                // Validate that the returned role matches the selected role
                if (!auth.getRole().equalsIgnoreCase(selectedRole)) {
                    showError("You do not have " + selectedRole + " access");
                    LogData.logAction("LOGIN_FAILED", "Role mismatch for email: " + email
                            + " (selected: " + selectedRole + ", actual: " + auth.getRole() + ")");
                    Sentry.withScope(scope -> {
                        scope.setTag("action", "LOGIN_FAILED");
                        scope.setTag("reason", "role_mismatch");
                        Sentry.captureMessage("Login failed: role mismatch for " + email, io.sentry.SentryLevel.WARNING);
                    });
                    return;
                }

                // Build a lightweight User object from the auth response
                User user = new User();
                user.setUsername(auth.getUsername());
                user.setEmail(email);
                user.setRole(auth.getRole().toUpperCase());

                // Store session and JWT
                api.setToken(auth.getToken());
                UserSession session = UserSession.getInstance();
                session.createSession(user, auth.getToken());

                // Attach user identity to Sentry for all subsequent events
                io.sentry.protocol.User sentryUser = new io.sentry.protocol.User();
                sentryUser.setUsername(auth.getUsername());
                sentryUser.setEmail(email);
                Sentry.setUser(sentryUser);
                Sentry.setTag("role", auth.getRole().toLowerCase());

                // Compute analytics eligibility for EMPLOYEE
                if (session.isEmployee()) {
                    Integer employeeId = EmployeeAccessDAO.getEmployeeIdByUserId(user.getUserId());
                    List<Integer> bakeryIds = EmployeeAccessDAO.getAccessibleBakeryIdsByUserId(user.getUserId());
                    session.setEmployeeAnalyticsAccess(employeeId, bakeryIds);

                    if (session.canAccessAnalytics()) {
                        LogData.logAction("LOGIN", "Employee login: " + auth.getUsername() + " (Analytics ENABLED)");
                    } else {
                        LogData.logAction("LOGIN", "Employee login: " + auth.getUsername() + " (Analytics DISABLED)");
                    }
                } else {
                    LogData.logAction("LOGIN", "Admin login: " + auth.getUsername());
                }

                openMainView();

            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Invalid email or password");
                LogData.logAction("LOGIN_FAILED", "Failed login for email: " + email);
                Sentry.withScope(scope -> {
                    scope.setTag("action", "LOGIN_FAILED");
                    scope.setTag("reason", "invalid_credentials");
                    Sentry.captureMessage("Login failed: invalid credentials for " + email, io.sentry.SentryLevel.WARNING);
                });
            } else {
                showError("Login failed (server error " + response.statusCode() + ")");
                LogData.logAction("LOGIN_FAILED", "Server error " + response.statusCode() + " for email: " + email);
                Sentry.withScope(scope -> {
                    scope.setTag("action", "LOGIN_FAILED");
                    scope.setTag("reason", "server_error");
                    scope.setTag("status_code", String.valueOf(response.statusCode()));
                    Sentry.captureMessage("Login failed: server error " + response.statusCode() + " for " + email, io.sentry.SentryLevel.ERROR);
                });
            }

        } catch (Exception e) {
            showError("Could not connect to the server. Is the API running?");
            LogData.handleException("LOGIN", e);
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

        scene.getStylesheets().add(this.getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

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
