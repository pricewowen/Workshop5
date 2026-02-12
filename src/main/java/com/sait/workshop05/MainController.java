package com.sait.workshop05;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

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
    private Button btnRewardTier;

    @FXML
    private TextField srcSearchBar;

    @FXML
    void initialize() {
        showDashboard();
    }

    @FXML
    void onActivityLogClick(ActionEvent event) {
        loadPage("activity-log-view.fxml");
    }

    @FXML
    void onAnalyticsClick(ActionEvent event) {
        // TODO
    }

    @FXML
    void onCustomersClick(ActionEvent event) {
        // TODO
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
        // TODO
    }

    @FXML
    void onMessagesClick(ActionEvent event) {
        // TODO
    }

    @FXML
    void onOrdersClick(ActionEvent event) {
        // TODO
    }

    @FXML
    void onProductsClick(ActionEvent event) {
        // TODO
    }

    @FXML
    void onRewardsClick(ActionEvent event) {
        loadPage("reward-view.fxml");
    }

    @FXML
    void onRewardTierClick(ActionEvent event) {
        loadPage("reward-tier-view.fxml");
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

        // Your current project screenshot shows:
        // src/main/resources/com.sait.workshop05/*.fxml  (dot folder)
        url = getClass().getResource("/com.sait.workshop05/" + view);
        if (url != null) return url;

        // Fallback: relative to this class package (rarely correct unless you structure resources exactly that way)
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
