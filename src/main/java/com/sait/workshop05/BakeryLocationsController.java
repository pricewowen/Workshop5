package com.sait.workshop05;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class BakeryLocationsController {

    @FXML
    private Button btnClear;

    @FXML
    private Button btnCreate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnUpdate;

    @FXML
    private TableColumn<?, ?> colAddressId;

    @FXML
    private TableColumn<?, ?> colBakeryEmail;

    @FXML
    private TableColumn<?, ?> colBakeryId;

    @FXML
    private TableColumn<?, ?> colBakeryName;

    @FXML
    private TableColumn<?, ?> colBakeryPhone;

    @FXML
    private Label lblStatus;

    @FXML
    private TableView<?> tblBakeryLocations;

    @FXML
    private TextField txtAddressId;

    @FXML
    private TextField txtBakeryEmail;

    @FXML
    private TextField txtBakeryId;

    @FXML
    private TextField txtBakeryName;

    @FXML
    private TextField txtBakeryPhone;

    @FXML
    private TextField txtSearch;

    @FXML
    void onClear(ActionEvent event) {

    }

    @FXML
    void onCreate(ActionEvent event) {

    }

    @FXML
    void onDelete(ActionEvent event) {

    }

    @FXML
    void onRefresh(ActionEvent event) {

    }

    @FXML
    void onUpdate(ActionEvent event) {

    }

}
