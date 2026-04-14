package com.sait.workshop05.controllers;

import java.io.IOException;
import java.net.URL;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StageSizing;
import com.sait.workshop05.util.StageIconHelper;
import com.sait.workshop05.util.UserInitialsHelper;
import io.sentry.Sentry;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
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
    private Button btnProductSpecials;

    @FXML
    private Button btnRewards;

    @FXML
    private Button btnRewardTier;

    @FXML
    private Button btnUsers;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblRole;

    @FXML
    private Label lblInitials;

    @FXML
    private ImageView imgUserAvatar;

    @FXML
    private Label lblPageTitle;

    private Button activeButton;
    private final Map<Button, String> PAGE_TITLES = new LinkedHashMap<>();

    @FXML
    void initialize() {
        PAGE_TITLES.put(btnDashboard,   "Dashboard");
        PAGE_TITLES.put(btnOrders,      "Orders");
        PAGE_TITLES.put(btnProducts,        "Products");
        PAGE_TITLES.put(btnProductSpecials, "Product Specials");
        PAGE_TITLES.put(btnCustomers,       "Customers");
        PAGE_TITLES.put(btnEmployees,   "Employees");
        PAGE_TITLES.put(btnLocations,   "Locations");
        PAGE_TITLES.put(btnRewards,     "Rewards");
        PAGE_TITLES.put(btnRewardTier,  "Reward Tiers");
        PAGE_TITLES.put(btnMessages,    "Messages");
        PAGE_TITLES.put(btnAnalytics,   "Analytics");
        PAGE_TITLES.put(btnActivityLog, "Activity Log");
        PAGE_TITLES.put(btnUsers,       "Users");

        if (imgUserAvatar != null) {
            Circle clip = new Circle(18, 18, 18);
            imgUserAvatar.setClip(clip);
        }

        setUserLabels();
        applyRoleBasedVisibility();
        showDashboard();
    }

    /**
     * Sets the username labels at the top of the main layout
     */
    private void setUserLabels() {
        UserSession session = UserSession.getInstance();

        String role = session.isEmployee() ? "Employee" : "Admin";
        lblRole.setText(role);

        String username = session.getCurrentUser() != null
                ? session.getCurrentUser().getUsername() : "Unknown";
        lblUsername.setText(username);

        if (lblInitials != null) {
            String initials = UserInitialsHelper.compute(
                    session.getProfileFirstName(),
                    session.getProfileLastName(),
                    username);
            lblInitials.setText(initials);
        }

        configureSidebarAvatar(session, username);
    }

    private void configureSidebarAvatar(UserSession session, String username) {
        if (imgUserAvatar == null || lblInitials == null) {
            return;
        }

        String photoUrl = session.getProfilePhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) {
            showSidebarInitialsOnly();
            return;
        }

        Image image = new Image(photoUrl, 72, 72, true, true, true);

        Runnable applyLoadedOrFallback = () -> {
            if (image.isError()) {
                showSidebarInitialsOnly();
            } else {
                imgUserAvatar.setImage(image);
                imgUserAvatar.setVisible(true);
                imgUserAvatar.setManaged(true);
                lblInitials.setVisible(false);
                lblInitials.setManaged(false);
            }
        };

        if (image.getProgress() >= 1.0) {
            Platform.runLater(applyLoadedOrFallback);
        } else {
            image.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV != null && newV.doubleValue() >= 1.0) {
                    Platform.runLater(applyLoadedOrFallback);
                }
            });
            image.errorProperty().addListener((obs, oldErr, isErr) -> {
                if (Boolean.TRUE.equals(isErr)) {
                    Platform.runLater(this::showSidebarInitialsOnly);
                }
            });
        }
    }

    private void showSidebarInitialsOnly() {
        if (imgUserAvatar != null) {
            imgUserAvatar.setImage(null);
            imgUserAvatar.setVisible(false);
            imgUserAvatar.setManaged(false);
        }
        if (lblInitials != null) {
            lblInitials.setVisible(true);
            lblInitials.setManaged(true);
        }
    }

    /**
     * Hide sidebar buttons based on the current user's role.
     *
     * Admin: sees everything.
     * Employee:
     *  - no Employees
     *  - no Locations
     *  - no Users (login account management is admin-only)
     *  - Analytics only if "real" employee (Employee row + bakery access)
     */
    private void applyRoleBasedVisibility() {
        UserSession session = UserSession.getInstance();

        if (session.isEmployee()) {
            hideButton(btnEmployees);
            hideButton(btnLocations);
            hideButton(btnUsers);

            // Only hide analytics if the employee is NOT eligible.
            if (!session.canAccessAnalytics()) {
                hideButton(btnAnalytics);
            }
        }
        // Admin sees everything — no hiding needed
        if (btnUsers != null && btnUsers.isVisible()) {
            btnUsers.setTooltip(new Tooltip(
                    "User accounts: create Employee or Customer logins (admin only)."));
        }
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
    void onProductSpecialsClick(ActionEvent event) {
        setActiveButton(btnProductSpecials);
        loadPage("product-specials-view.fxml");
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

    @FXML
    void onUsersClick(ActionEvent event) {
        UserSession session = UserSession.getInstance();
        if (!session.isAdmin()) {
            ErrorHandler.showErrorDialog(
                    "Access Denied",
                    "User management is not available",
                    "Only administrators can open User management.");
            return;
        }
        setActiveButton(btnUsers);
        loadPage("user-management-view.fxml");
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
            StageSizing.applyMainShellBounds(stage);
            stage.setResizable(true);
        } catch (IOException e) {
            ErrorHandler.showErrorDialog("Logout Error", "Could not return to login screen", e);
            LogData.handleException("LOGOUT", e);
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        activeButton = button;
        if (lblPageTitle != null) {
            lblPageTitle.setText(PAGE_TITLES.getOrDefault(button, ""));
        }
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
            ErrorHandler.showErrorDialog("Navigation Error", "Could not load page: " + view, e);
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