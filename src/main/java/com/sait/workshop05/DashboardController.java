package com.sait.workshop05;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardController {

    @FXML
    private Button btnNewOrder;

    @FXML
    private TableColumn<?, ?> colActions;

    @FXML
    private TableColumn<?, ?> colCustomer;

    @FXML
    private TableColumn<?, ?> colDate;

    @FXML
    private TableColumn<?, ?> colOrderId;

    @FXML
    private TableColumn<?, ?> colProducts;

    @FXML
    private TableColumn<?, ?> colStatus;

    @FXML
    private TableColumn<?, ?> colTotal;

    @FXML
    private Label lblActiveProducts;

    @FXML
    private Label lblTotalCustomers;

    @FXML
    private Label lblTotalOrders;

    @FXML
    private Label lblTotalRevenu;

    @FXML
    private TableView<?> tbvRecentOrders;

}
