package com.sait.workshop05.controllers;

import com.sait.workshop05.database.BakeryDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Address;
import com.sait.workshop05.models.Bakery;
import com.sait.workshop05.models.Employee;
import com.sait.workshop05.models.Province;
import com.sait.workshop05.util.StringUtil;
import com.sait.workshop05.util.Validator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class BakeryLocationsController {

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_RX = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

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
    private TableColumn<Bakery, String> colBakeryAddress;

    @FXML
    private TableColumn<Bakery, String> colBakeryCity;

    @FXML
    private TableColumn<Bakery, String> colBakeryEmail;

    @FXML
    private TableColumn<Bakery, Integer> colBakeryId;

    @FXML
    private TableColumn<Bakery, String> colBakeryName;

    @FXML
    private TableColumn<Bakery, String> colBakeryPhone;

    @FXML
    private TableColumn<Bakery, String> colBakeryPostalCode;

    @FXML
    private TableColumn<Bakery, String> colBakeryProvince;

    @FXML
    private Label lblStatus1;

    @FXML
    private TableView<Bakery> tblBakeryLocations;

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
        txtAddressCity.clear();
        txtAddressLine1.clear();
        txtAddressLine2.clear();
        txtBakeryId.clear();
        txtAddressPostalCode.clear();
        txtBakeryEmail.clear();
        txtBakeryName.clear();
        txtBakeryPhone.clear();
        setComboBox();
    }

    @FXML
    void onCreate(ActionEvent event) {
        // validation variables
        String nameError = Validator.isValidName(txtBakeryName.getText(), "Name");
        String emailError = Validator.isValidEmail(txtBakeryEmail.getText());
        String phoneError = Validator.isValidPhoneNumber(txtBakeryPhone.getText());
        String address1Error = Validator.isValidAddress(txtAddressLine1.getText(), 1);
        String address2Error = Validator.isValidAddress(txtAddressLine2.getText(), 2);
        String cityError = Validator.isValidName(txtAddressCity.getText(), "City");
        String provinceError = Validator.isValidProvince(cboAddressProvince.getValue().getCode());
        String postalCodeError = Validator.isValidPostalCode(txtAddressPostalCode.getText());

        // display error message for name validation
        if (nameError != null) {
            showWarning("Validation Error", nameError);
            return;
        }

        // display error message for email error
        if (emailError != null) {
            showWarning("Validation Error", emailError);
            return;
        }

        // display error message for phone number
        if (phoneError != null) {
            showWarning("Validation Error", phoneError);
            return;
        }

        // display error messages for address lines
        if (address1Error != null) {
            showWarning("Validation Error", address1Error);
            return;
        }

        // display error messages for second address line
        if (address2Error != null) {
            showWarning("Validation Error", address2Error);
            return;
        }

        // display error message for city error
        if (cityError != null) {
            showWarning("Validation Error", cityError);
            return;
        }

        // display error message for province error
        if (provinceError != null) {
            showWarning("Validation Error", provinceError);
            return;
        }

        // display error message for postal code
        if (postalCodeError != null) {
            showWarning("Validation Error", postalCodeError);
            return;
        }
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

    private ObservableList<Bakery> bakeryList = FXCollections.observableArrayList();
    private FilteredList<Bakery> filtered;

    @FXML
    void initialize() {
        setComboBox();
        phoneNumberFormatter();
        postalCodeFormatter();
        setupTableColumns();
        setupSearchFiltering();
        displayBakeries();
    }

    /**
     * Sets up the search functionality
     */
    private void setupSearchFiltering() {
        filtered = new FilteredList<>(bakeryList, e -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = newText == null ? "" : newText.trim().toLowerCase();

            filtered.setPredicate(bakery -> {
                if (q.isEmpty()) return true;

                Address addr = bakery.getAddress();

                return String.valueOf(bakery.getBakeryId()).contains(q)
                        || StringUtil.containsIgnoreCase(bakery.getBakeryName(), q)
                        || StringUtil.containsIgnoreCase(bakery.getBakeryEmail(), q)
                        || StringUtil.containsIgnoreCase(bakery.getBakeryPhone(), q)
                        || (addr != null && (
                                StringUtil.containsIgnoreCase(bakery.getAddress().getAddressLine1(), q)
                                || StringUtil.containsIgnoreCase(bakery.getAddress().getAddressLine2(), q)
                                || StringUtil.containsIgnoreCase(bakery.getAddress().getAddressCity(), q)
                                || StringUtil.containsIgnoreCase(bakery.getAddress().getAddressProvince(), q)
                                || StringUtil.containsIgnoreCase(bakery.getAddress().getAddressPostalCode(), q)
                        ));

            });
        });

        SortedList<Bakery> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblBakeryLocations.comparatorProperty());
        tblBakeryLocations.setItems(sorted);
    }

    /**
     * Displays all bakeries
     */
    private void displayBakeries() {
        try {
            BakeryDAO dao = new BakeryDAO();
            bakeryList.setAll(dao.getAllBakeries());
        } catch (SQLException e) {
            LogData.handleException("GET_BAKERIES", e);
        }
    }

    /**
     * Sets up the columns with the appropriate values
     */
    private void setupTableColumns() {
        colBakeryId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getBakeryId()));
        colBakeryName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBakeryName()));
        colBakeryPhone.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBakeryPhone()));
        colBakeryEmail.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBakeryEmail()));
        colBakeryAddress.setCellValueFactory(cellData -> {
                    String fullAddress = cellData.getValue().getAddress().getAddressLine1();
                    String line2 =  cellData.getValue().getAddress().getAddressLine2();

                    if (line2 != null) {
                        fullAddress += ", " + line2;
                    }

                    return new ReadOnlyStringWrapper(fullAddress);
                });
        colBakeryCity.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAddress().getAddressCity()));
        colBakeryProvince.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAddress().getAddressProvince()));
        colBakeryPostalCode.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAddress().getAddressPostalCode()));
    }

    /**
     * Sets the values of the combo box
     */
    private void setComboBox() {
        // adds all provinces to combo box
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

    /**
     * Shows an alert with an error message
     * @param title The title of the alert
     * @param message the message displayed
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Formats the phone number into (XXX) XXX-XXXX format dynamically
     */
    private void phoneNumberFormatter() {
        txtBakeryPhone.textProperty().addListener((obs, oldText, newText) -> {
            // replace all non-digit characters with nothing
            String digits = newText.replaceAll("\\D", "");

            // trim to 10 digits
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            StringBuilder phoneFormatter = new StringBuilder();

            int digit = digits.length();
            if (digit > 0) {
                phoneFormatter.append("(");
            }

            if (digit >= 1) {
                phoneFormatter.append(digits.substring(0, Math.min(3, digit)));
            }

            if (digit >= 4) {
                phoneFormatter.append(") ");
                phoneFormatter.append(digits.substring(3, Math.min(6, digit)));
            }

            if (digit >= 7) {
                phoneFormatter.append("-");
                phoneFormatter.append(digits.substring(6));
            }

            if (!phoneFormatter.toString().equals(newText)) {
                txtBakeryPhone.setText(phoneFormatter.toString());
            }
        });
    }

    /**
     * Formats the postal code into "A1A 1A1" format dynamically
     */
    private void postalCodeFormatter() {
        txtAddressPostalCode.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(null)) {
                return;
            }

            String text = newText.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

            StringBuilder newPostal = new StringBuilder();

            for (int i = 0; i < text.length() && i < 6; i++) {
                // add a space after three characters
                if (i == 3) {
                    newPostal.append(" ");
                }
                // add normal characters
                newPostal.append(text.charAt(i));
            }

            if (!newPostal.equals(newText)) {
                txtAddressPostalCode.setText(newPostal.toString());
            }
        });
    }
}
