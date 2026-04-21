// Contributor(s): Robbie
// Main: Robbie - Staff login to Workshop 7 JWT session and main view load.

package com.sait.workshop05.controllers;

import com.sait.workshop05.api.ApiClient;
import com.sait.workshop05.api.dto.AuthResponseDto;
import com.sait.workshop05.api.dto.LoginRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.User;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StageSizing;
import com.sait.workshop05.util.StageIconHelper;
import io.sentry.Sentry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

/**
 * Staff login obtains JWT session state then loads the main shell scene.
 */
public class LoginController {

    @FXML private TextField loginIdField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private javafx.scene.shape.SVGPath iconEye;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private static final String ICON_EYE_OPEN =
            "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5zm0 12.5c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private static final String ICON_EYE_OFF =
            "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

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
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
        }
        if (iconEye != null) {
            iconEye.setContent(passwordShown ? ICON_EYE_OFF : ICON_EYE_OPEN);
        }
        if (togglePasswordBtn != null) {
            String tip = passwordShown ? "Hide password" : "Show password";
            togglePasswordBtn.setAccessibleText(tip);
            if (togglePasswordBtn.getTooltip() == null) {
                togglePasswordBtn.setTooltip(new Tooltip(tip));
            } else {
                togglePasswordBtn.getTooltip().setText(tip);
            }
        }
    }

    private String getCurrentPassword() {
        return passwordShown ? passwordVisible.getText() : passwordField.getText();
    }

    @FXML
    private void handleLogin() {
        String principal = loginIdField.getText().trim();
        String password = getCurrentPassword();

        if (principal.isEmpty() || password.isEmpty()) {
            showError("Please enter email or username and password");
            return;
        }

        try {
            ApiClient api = ApiClient.getInstance();
            LoginRequestDto loginRequest = LoginRequestDto.fromLoginPrincipal(principal, password);
            HttpResponse<String> response = api.post("/api/v1/auth/login", loginRequest);

            if (response.statusCode() == 200) {
                AuthResponseDto auth = api.getMapper().readValue(response.body(), AuthResponseDto.class);

                // Customer accounts must not open this desktop shell.
                String role = auth.getRole();
                if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("EMPLOYEE")) {
                    showError("This account does not have management access");
                    LogData.logAction("LOGIN_FAILED", "Non-staff login attempt for: " + principal
                            + " (role: " + role + ")");
                    Sentry.withScope(scope -> {
                        scope.setTag("action", "LOGIN_FAILED");
                        scope.setTag("reason", "insufficient_role");
                        Sentry.captureMessage("Login failed: non-staff role for " + principal, io.sentry.SentryLevel.WARNING);
                    });
                    return;
                }

                api.setToken(auth.getToken());

                User user = new User();
                user.setUsername(auth.getUsername());
                String sessionEmail = auth.getEmail();
                if (sessionEmail == null || sessionEmail.isBlank()) {
                    sessionEmail = principal.contains("@") ? principal : "";
                }
                user.setEmail(sessionEmail);
                user.setRole(auth.getRole());

                UserSession session = UserSession.getInstance();
                session.createSession(user, auth.getToken());
                session.setProfileDisplayHints(auth.getFirstName(), auth.getLastName(), auth.getProfilePhotoPath());
                if (auth.getUserId() != null && !auth.getUserId().isBlank()) {
                    session.setApiUserId(auth.getUserId());
                }

                io.sentry.protocol.User sentryUser = new io.sentry.protocol.User();
                sentryUser.setUsername(auth.getUsername());
                sentryUser.setEmail(sessionEmail != null && !sessionEmail.isBlank() ? sessionEmail : null);
                Sentry.setUser(sentryUser);
                Sentry.setTag("role", auth.getRole().toLowerCase());

                // Employee dashboard charts need a profile id and at least one bakery id from the API.
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

                } else {
                    LogData.logAction("LOGIN", role + " login via API: " + user.getUsername());
                }

                openMainView();

            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Invalid email, username, or password");
                LogData.logAction("LOGIN_FAILED", "Failed login for: " + principal);
                Sentry.withScope(scope -> {
                    scope.setTag("action", "LOGIN_FAILED");
                    scope.setTag("reason", "invalid_credentials");
                    Sentry.captureMessage("Login failed: invalid credentials for " + principal, io.sentry.SentryLevel.WARNING);
                });
            } else {
                showError("Login failed (server error " + response.statusCode() + ")");
                LogData.logAction("LOGIN_FAILED", "Server error " + response.statusCode() + " for: " + principal);
                Sentry.withScope(scope -> {
                    scope.setTag("action", "LOGIN_FAILED");
                    scope.setTag("reason", "server_error");
                    scope.setTag("status_code", String.valueOf(response.statusCode()));
                    Sentry.captureMessage("Login failed: server error " + response.statusCode() + " for " + principal, io.sentry.SentryLevel.ERROR);
                });
            }

        } catch (Exception e) {
            LogData.handleException("LOGIN", e);
            String userMsg = ErrorHandler.userFacingMessage(e);
            showError(userMsg != null && !userMsg.isBlank()
                    ? userMsg
                    : "We could not sign you in. Check your connection and try again.");
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
        StageSizing.applyMainShellBounds(stage);
        stage.setResizable(true);
    }
}
