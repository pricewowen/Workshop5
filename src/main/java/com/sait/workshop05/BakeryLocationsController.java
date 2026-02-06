package com.sait.workshop05;

import com.sait.workshop05.models.Province;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

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
    private ComboBox<Province> cboAddressProvince;

    @FXML
    private TableColumn<?, ?> colBakeryAddress;

    @FXML
    private TableColumn<?, ?> colBakeryCity;

    @FXML
    private TableColumn<?, ?> colBakeryEmail;

    @FXML
    private TableColumn<?, ?> colBakeryId;

    @FXML
    private TableColumn<?, ?> colBakeryName;

    @FXML
    private TableColumn<?, ?> colBakeryPhone;

    @FXML
    private TableColumn<?, ?> colBakeryPostalCode;

    @FXML
    private TableColumn<?, ?> colBakeryProvince;

    @FXML
    private Label lblStatus1;

    @FXML
    private TableView<?> tblBakeryLocations;

    @FXML
    private TextField txtAddressCity;

    @FXML
    private TextField txtAddressLine1;

    @FXML
    private TextField txtAddressLine2;

    @FXML
    private TextField txtAddressPostalCode;

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

    @FXML
    void initialize() {
        ObservableList<Province> provinces = FXCollections.observableArrayList(
                new Province("AB", "Alberta"),
                new Province("BC", "British Columbia"),
                new Province("MB", "Manitoba"),
                new Province("NB", "New Brunswick"),
                new Province("NL", "Newfoundland and Labrador"),
                new Province("NT", "Northwest Territories"),
                new Province("NS", "Nova Scotia"),
                new Province("NU", "Nunavut"),
                new Province("ON", "Ontario"),
                new Province("PE", "Prince Edward Island"),
                new Province("QC", "Quebec"),
                new Province("SK", "Saskatchewan"),
                new Province("YT", "Yukon")
        );
        // store province objects in combo box
        cboAddressProvince.setItems(provinces);
        cboAddressProvince.setValue(provinces.getFirst());

        // convert the province object into names
        cboAddressProvince.setConverter(new StringConverter<Province>() {
            @Override
            public String toString(Province province) {
                if (province != null) {
                    return province.getName();
                } else {
                    return "";
                }
            }

            @Override
            public Province fromString(String s) {
                return null;
            }
        });
    }

}
