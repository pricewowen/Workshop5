package com.sait.workshop05.controllers;

import java.io.IOException;
import java.net.URL;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StageIconHelper;
import io.sentry.Sentry;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Button btnActivityLog;

    @FXML
    private Button btnAnalytics;

    @FXML
    private Button btnCustomers;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnEmployees;

    @FXML
    private Button btnLocations;

    @FXML
    private Button btnMessages;

    @FXML
    private Button btnOrders;

    @FXML
    private Button btnProducts;

    @FXML
    private Button btnRewards;

    @FXML
    private Button btnRewardTier;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblRole;

    private Button activeButton;

    @FXML
    void initialize() {
        setUserLabels();
        applyRoleBasedVisibility();
        showDashboard();
    }

    /**
     * Sets the username labels at the top of the main layout
     */
    private void setUserLabels() {
        UserSession session = UserSession.getInstance();

        if (session.isEmployee()) {
            lblRole.setText("Employee");
        } else {
            lblRole.setText("Admin");
        }

        if (session.getCurrentUser() != null) {
            lblUsername.setText(session.getCurrentUser().getUsername());
        } else {
            lblUsername.setText("Unknown");
        }
    }

    /**
     * Hide sidebar buttons based on the current user's role.
     *
     * Admin: sees everything.
     * Employee:
     *  - no Employees
     *  - no Locations
     *  - Analytics only if "real" employee (Employee row + bakery access)
     */
    private void applyRoleBasedVisibility() {
        UserSession session = UserSession.getInstance();

        if (session.isEmployee()) {
            hideButton(btnEmployees);
            hideButton(btnLocations);

            // Only hide analytics if the employee is NOT eligible.
            if (!session.canAccessAnalytics()) {
                hideButton(btnAnalytics);
            }
        }
        // Admin sees everything — no hiding needed
    }

    /**
     * Hide a sidebar button and remove it from layout
     */
    private void hideButton(Button button) {
        if (button != null) {
            button.setVisible(false);
            button.setManaged(false);
        }
    }

    @FXML
    void onActivityLogClick(ActionEvent event) {
        setActiveButton(btnActivityLog);
        loadPage("activity-log-view.fxml");
    }

    @FXML
    private void onAnalyticsClick() {
        // Extra safety: even if button is visible somehow, enforce access.
        UserSession session = UserSession.getInstance();
        if (!session.canAccessAnalytics()) {
            ErrorHandler.showErrorDialog(
                    "Access Denied",
                    "Analytics not available",
                    "This account is not linked to an employee with analytics access."
            );
            return;
        }

        setActiveButton(btnAnalytics);
        loadPage("analytics-view.fxml");
    }

    @FXML
    void onCustomersClick(ActionEvent event) {
        setActiveButton(btnCustomers);
        loadPage("customer-management-view.fxml");
    }

    @FXML
    void onDashboardClick(ActionEvent event) {
        setActiveButton(btnDashboard);
        loadPage("dashboard-view.fxml");
    }

    @FXML
    void onEmployeesClick(ActionEvent event) {
        setActiveButton(btnEmployees);
        loadPage("employee-management-view.fxml");
    }

    @FXML
    void onLocationsClick(ActionEvent event) {
        setActiveButton(btnLocations);
        loadPage("bakery-locations-view.fxml");
    }

    @FXML
    void onMessagesClick(ActionEvent event) {
        setActiveButton(btnMessages);
        loadPage("messaging-view.fxml");
    }

    @FXML
    void onOrdersClick(ActionEvent event) {
        setActiveButton(btnOrders);
        loadPage("order-management-view.fxml");
    }

    @FXML
    void onProductsClick(ActionEvent event) {
        setActiveButton(btnProducts);
        loadPage("product-management-view.fxml");
    }

    @FXML
    void onRewardsClick(ActionEvent event) {
        setActiveButton(btnRewards);
        loadPage("reward-view.fxml");
    }

    @FXML
    void onRewardTierClick(ActionEvent event) {
        setActiveButton(btnRewardTier);
        loadPage("reward-tier-view.fxml");
    }

    /**
     * Handle logout — clear session and return to login view
     */
    @FXML
    void onLogoutClick(ActionEvent event) {
        UserSession session = UserSession.getInstance();
        String username = session.getCurrentUser() != null ? session.getCurrentUser().getUsername() : "unknown";

        LogData.logAction("LOGOUT", "User logged out: " + username);
        Sentry.setUser(null);
        session.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sait/workshop05/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            StageIconHelper.setAppIcon(stage);
            stage.setWidth(700);
            stage.setHeight(730);
            stage.setMinWidth(700);
            stage.setMinHeight(730);
            stage.centerOnScreen();
        } catch (IOException e) {
            ErrorHandler.showErrorDialog("Logout Error", "Could not return to login screen", e.getMessage());
            LogData.handleException("LOGOUT", e);
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }

        button.getStyleClass().add("active");
        activeButton = button;
    }

    private void loadPage(String view) {
        try {
            URL url = resolveFxmlUrl(view);

            if (url == null) {
                throw new IllegalStateException(
                        "FXML not found: " + view + "\n\n" +
                                "Tried:\n" +
                                "  /com/sait/workshop05/" + view + "\n" +
                                "  /com.sait.workshop05/" + view + "\n\n" +
                                "Fix by either:\n" +
                                "  1) Move FXML files into: src/main/resources/com/sait/workshop05/\n" +
                                "     (recommended), OR\n" +
                                "  2) Keep your current folder (com.sait.workshop05) and this loader will work."
                );
            }

            FXMLLoader loader = new FXMLLoader(url);
            Node page = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

        } catch (Exception e) {
            ErrorHandler.showErrorDialog("Navigation Error", "Could not load page: " + view, e.getMessage());
            LogData.handleException("LOAD_PAGE", e);
        }
    }

    private URL resolveFxmlUrl(String view) {
        URL url = getClass().getResource("/com/sait/workshop05/" + view);
        if (url != null) return url;

        url = getClass().getResource("/com.sait.workshop05/" + view);
        if (url != null) return url;

        return getClass().getResource(view);
    }

    private void showDashboard() {
        setActiveButton(btnDashboard);
        loadPage("dashboard-view.fxml");
    }
}