package com.sait.workshop05.controllers;

import com.sait.workshop05.api.ApiClient;
import com.sait.workshop05.api.dto.AuthResponseDto;
import com.sait.workshop05.api.dto.LoginRequestDto;
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

import java.net.http.HttpResponse;
import java.io.IOException;
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
        if (roleComboBox != null) {
            roleComboBox.getItems().clear();
            roleComboBox.getItems().addAll("ADMIN", "EMPLOYEE");
            roleComboBox.getSelectionModel().selectFirst();
        }
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
            // Porting to API authentication
            ApiClient client = ApiClient.getInstance();
            LoginRequestDto loginRequest = new LoginRequestDto(email, password);
            HttpResponse<String> response = client.post("/auth/login", loginRequest);

            if (response.statusCode() == 200) {
                AuthResponseDto authResponse = client.getMapper().readValue(response.body(), AuthResponseDto.class);
                
                // Check if role matches
                if (!authResponse.getRole().equalsIgnoreCase(selectedRole)) {
                    String errorMsg = "Role mismatch: API returned '" + authResponse.getRole() + "' but '" + selectedRole + "' was selected.";
                    showError("Invalid role for this user");
                    System.err.println("[DEBUG_LOG] " + errorMsg);
                    LogData.logAction("LOGIN_FAILED", "Role mismatch for email: " + email + " (" + errorMsg + ")");
                    return;
                }

                client.setToken(authResponse.getToken());

                // Create a User object from response to maintain compatibility with existing session
                User user = new User();
                user.setUsername(authResponse.getUsername());
                user.setEmail(email);
                user.setRole(authResponse.getRole());
                
                // Store session
                UserSession session = UserSession.getInstance();
                session.createSession(user, authResponse.getToken());

                // Compute analytics eligibility for EMPLOYEE via API (future task)
                // For now, using the dummy DAO which returns null/empty
                if (session.isEmployee()) {
                    Integer employeeId = EmployeeAccessDAO.getEmployeeIdByUserId(user.getUserId());
                    List<Integer> bakeryIds = EmployeeAccessDAO.getAccessibleBakeryIdsByUserId(user.getUserId());
                    session.setEmployeeAnalyticsAccess(employeeId, bakeryIds);

                    if (session.canAccessAnalytics()) {
                        LogData.logAction("LOGIN", "Employee login: " + user.getUsername() + " (Analytics ENABLED)");
                    } else {
                        LogData.logAction("LOGIN", "Employee login: " + user.getUsername() + " (Analytics DISABLED)");
                    }
                } else {
                    LogData.logAction("LOGIN", selectedRole + " login via API: " + user.getUsername());
                }

                openMainView();

            } else {
                String errorMsg = "Login failed with status " + response.statusCode() + ": " + response.body();
                showError("Invalid email, password, or role");
                System.err.println("[DEBUG_LOG] " + errorMsg);
                LogData.logAction("LOGIN_FAILED", "Failed API login for email: " + email + " (" + errorMsg + ")");
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
