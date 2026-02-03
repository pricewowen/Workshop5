package com.sait.workshop05;

import com.sait.workshop05.logging.LogData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

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

    }

    @FXML
    void onCustomersClick(ActionEvent event) {

    }

    @FXML
    void onDashboardClick(ActionEvent event) {
        loadPage("dashboard-view.fxml");
    }

    @FXML
    void onEmployeesClick(ActionEvent event) {

    }

    @FXML
    void onLocationsClick(ActionEvent event) {

    }

    @FXML
    void onMessagesClick(ActionEvent event) {

    }

    @FXML
    void onOrdersClick(ActionEvent event) {

    }

    @FXML
    void onProductsClick(ActionEvent event) {

    }

    @FXML
    void onRewardsClick(ActionEvent event) {

    }

    /**
     * Load page in contentArea of main application
     * @param view the .fxml file to be displayed
     */
    private void loadPage(String view) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
            Node page = loader.load();

            // clear current area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

            // Make sure page fills the content area
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
        } catch (IOException e) {
            LogData.handleException("SYSTEM", "page_change", e);
        }
    }

    /**
     * Show the dashboard view
     */
    private void showDashboard() {
        loadPage("dashboard-view.fxml");
    }
}
