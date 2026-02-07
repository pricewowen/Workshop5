package com.sait.workshop05;

import com.sait.workshop05.models.Province;
import com.sait.workshop05.util.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

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
        String nameError = Validator.isValidName(txtBakeryName.getText());
        String emailError = Validator.isValidEmail(txtBakeryEmail.getText());
        String phoneError = Validator.isValidPhoneNumber(txtBakeryPhone.getText());
        String address1Error = Validator.isValidAddress(txtAddressLine1.getText(), 1);
        String address2Error = Validator.isValidAddress(txtAddressLine2.getText(), 2);
        String cityError;
        String provinceError;
        String postalCodeError;

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

        if (address2Error != null) {
            showWarning("Validation Error", address2Error);
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

    @FXML
    void initialize() {
        setComboBox();
        phoneNumberFormatter();
    }

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
            }

            if (digit >= 4) {
                phoneFormatter.append(digits.substring(3, Math.min(6, digit)));
            }

            if (digit >= 7) {
                phoneFormatter.append("-");
            }

            if (digit >= 7) {
                phoneFormatter.append(digits.substring(6));
            }

            if (!phoneFormatter.toString().equals(newText)) {
                txtBakeryPhone.setText(phoneFormatter.toString());
            }
        });
    }
}
