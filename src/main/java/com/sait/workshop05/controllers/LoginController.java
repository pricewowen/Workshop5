package com.sait.workshop05.controllers;

import com.sait.workshop05.api.ApiClient;
import com.sait.workshop05.api.dto.AuthResponseDto;
import com.sait.workshop05.api.dto.LoginRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.User;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.StageIconHelper;
import io.sentry.Sentry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private boolean passwordShown = false;

    @FXML
    private void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (passwordField != null) passwordField.setOnAction(event -> handleLogin());
        if (passwordVisible != null) passwordVisible.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleTogglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("Hide");
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            togglePasswordBtn.setText("Show");
        }
    }

    private String getCurrentPassword() {
        return passwordShown ? passwordVisible.getText() : passwordField.getText();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = getCurrentPassword();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        try {
            ApiClient api = ApiClient.getInstance();
            LoginRequestDto loginRequest = new LoginRequestDto(email, password);
            HttpResponse<String> response = api.post("/api/v1/auth/login", loginRequest);

            if (response.statusCode() == 200) {
                AuthResponseDto auth = api.getMapper().readValue(response.body(), AuthResponseDto.class);

                // Only ADMIN and EMPLOYEE accounts may access the management system
                String role = auth.getRole();
                if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("EMPLOYEE")) {
                    showError("This account does not have management access");
                    LogData.logAction("LOGIN_FAILED", "Non-staff login attempt for email: " + email
                            + " (role: " + role + ")");
                    Sentry.withScope(scope -> {
                        scope.setTag("action", "LOGIN_FAILED");
                        scope.setTag("reason", "insufficient_role");
                        Sentry.captureMessage("Login failed: non-staff role for " + email, io.sentry.SentryLevel.WARNING);
                    });
                    return;
                }

                api.setToken(auth.getToken());

                // Create a User object from response to maintain compatibility with existing session
                User user = new User();
                user.setUsername(auth.getUsername());
                user.setEmail(email);
                user.setRole(auth.getRole());

                // Store session
                UserSession session = UserSession.getInstance();
                session.createSession(user, auth.getToken());
                if (auth.getUserId() != null && !auth.getUserId().isBlank()) {
                    session.setApiUserId(auth.getUserId());
                }

                // Attach user identity to Sentry for all subsequent events
                io.sentry.protocol.User sentryUser = new io.sentry.protocol.User();
                sentryUser.setUsername(auth.getUsername());
                sentryUser.setEmail(email);
                Sentry.setUser(sentryUser);
                Sentry.setTag("role", auth.getRole().toLowerCase());

                // Compute analytics eligibility for EMPLOYEE (API: profile + bakery scope)
                if (session.isEmployee()) {
                    try {
                        HttpResponse<String> meRes = api.get("/api/v1/employee/me");
                        HttpResponse<String> brRes = api.get("/api/v1/employee/me/bakeries");
                        if (meRes.statusCode() == 200 && brRes.statusCode() == 200) {
                            var meNode = api.getMapper().readTree(meRes.body());
                            String empId = meNode.has("id") ? meNode.get("id").asText() : null;
                            List<Integer> bakeryIds = api.getMapper().readValue(brRes.body(), new TypeReference<List<Integer>>() {});
                            session.setEmployeeAnalyticsAccess(empId, bakeryIds != null ? bakeryIds : Collections.emptyList());
                        } else {
                            session.setEmployeeAnalyticsAccess(null, Collections.emptyList());
                        }
                    } catch (Exception ex) {
                        session.setEmployeeAnalyticsAccess(null, Collections.emptyList());
                    }

                    if (session.canAccessAnalytics()) {
                        LogData.logAction("LOGIN", "Employee login: " + user.getUsername() + " (Analytics ENABLED)");
                    } else {
                        LogData.logAction("LOGIN", "Employee login: " + user.getUsername() + " (Analytics DISABLED)");
                    }
                } else {
                    LogData.logAction("LOGIN", role + " login via API: " + user.getUsername());
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
            String errorMsg = "Authentication exception: " + e.getClass().getSimpleName() + " - " + e.getMessage();
            showError("Authentication error: " + e.getMessage());
            System.err.println("[DEBUG_LOG] " + errorMsg);
            e.printStackTrace();
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

        scene.getStylesheets().add(
                this.getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm()
        );

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Management System");
        StageIconHelper.setAppIcon(stage);

        stage.show();

        // 🔥 THIS is the only behavior change
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(true);
        });
    }
}
