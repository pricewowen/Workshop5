package com.sait.workshop05.controllers;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.User;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

        // set the admin/employee roles
        if (session.isEmployee()) {
            lblRole.setText("Employee");
        } else {
            lblRole.setText("Admin");
        }

        lblUsername.setText(session.getCurrentUser().getUsername());

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
        setActiveButton(btnActivityLog);
        loadPage("activity-log-view.fxml");
    }

    @FXML
    private void onAnalyticsClick() {
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

    /**
     * Sets the active button for styling
     * @param button that is active
     */
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }

        button.getStyleClass().add("active");
        activeButton = button;
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
            ErrorHandler.showErrorDialog("Navigation Error", "Could not load page: " + view, e.getMessage());
            LogData.handleException("LOAD_PAGE", e);
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
        setActiveButton(btnDashboard);
        loadPage("dashboard-view.fxml");
    }

}
