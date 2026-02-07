package com.sait.workshop05.controllers;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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
    private Button btnLogout;

    @FXML
    private TextField srcSearchBar;

    @FXML
    void initialize() {
        applyRoleBasedVisibility();
        showDashboard();
    }

    /**
     * Hide sidebar buttons based on the current user's role.
     * Admin: sees everything
     * Employee: no Employees, Locations, or Analytics
     */
    private void applyRoleBasedVisibility() {
        UserSession session = UserSession.getInstance();

        if (session.isEmployee()) {
            // Hide admin-only buttons
            hideButton(btnEmployees);
            hideButton(btnLocations);
            hideButton(btnAnalytics);
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
        loadPage("activity-log-view.fxml");
    }

    @FXML
    void onAnalyticsClick(ActionEvent event) {
        // TODO: Phase 9
    }

    @FXML
    void onCustomersClick(ActionEvent event) {
        // TODO: Phase 5
    }

    @FXML
    void onDashboardClick(ActionEvent event) {
        loadPage("dashboard-view.fxml");
    }

    @FXML
    void onEmployeesClick(ActionEvent event) {
        loadPage("employee-management-view.fxml");
    }

    @FXML
    void onLocationsClick(ActionEvent event) {
        // TODO: Phase 7
    }

    @FXML
    void onMessagesClick(ActionEvent event) {
        // TODO: Phase 10
    }

    @FXML
    void onOrdersClick(ActionEvent event) {
        // TODO: Phase 6
    }

    @FXML
    void onProductsClick(ActionEvent event) {
        // TODO: Phase 4
    }

    @FXML
    void onRewardsClick(ActionEvent event) {
        // TODO: Phase 8
    }

    /**
     * Handle logout — clear session and return to login view
     */
    @FXML
    void onLogoutClick(ActionEvent event) {
        UserSession session = UserSession.getInstance();
        String username = session.getCurrentUser() != null ? session.getCurrentUser().getUsername() : "unknown";

        LogData.logAction("LOGOUT", "User logged out: " + username);
        session.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sait/workshop05/login-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Peelin' Good - Login");
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Logout Error", "Could not return to login screen", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load page in contentArea of main application
     * @param view the .fxml file to be displayed
     */
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
            showError("Navigation Error", "Could not load page: " + view, e.getMessage());
            e.printStackTrace();
        }
    }

    private URL resolveFxmlUrl(String view) {
        // Preferred standard Maven resources path:
        // src/main/resources/com/sait/workshop05/*.fxml
        URL url = getClass().getResource("/com/sait/workshop05/" + view);
        if (url != null) return url;

        // Fallback: dot folder structure
        url = getClass().getResource("/com.sait.workshop05/" + view);
        if (url != null) return url;

        // Fallback: relative to this class package
        return getClass().getResource(view);
    }

    private void showDashboard() {
        loadPage("dashboard-view.fxml");
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
