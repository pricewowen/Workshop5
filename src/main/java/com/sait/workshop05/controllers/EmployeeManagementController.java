package com.sait.workshop05.controllers;

import com.sait.workshop05.api.EmployeeApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.models.BakeryOption;
import com.sait.workshop05.models.UserOption;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Employee;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
import com.sait.workshop05.util.ValidationResult;
import com.sait.workshop05.util.AddressInputHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmployeeManagementController {

    @FXML private TableView<Employee> tblEmployees;
    @FXML private TableColumn<Employee, String> colEmployeeId;
    @FXML private TableColumn<Employee, String> colFirstName;
    @FXML private TableColumn<Employee, String> colMiddleInitial;
    @FXML private TableColumn<Employee, String> colLastName;
    @FXML private TableColumn<Employee, String> colRole;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colBusinessPhone;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colUser;
    @FXML private TableColumn<Employee, String> colAddress;

    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;

    @FXML private TextField txtEmployeeId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtMiddleInitial;
    @FXML private TextField txtLastName;
    @FXML private ComboBox<String> cboRole;
    @FXML private ComboBox<BakeryOption> cboBakery;
    @FXML private TextField txtPhone;
    @FXML private TextField txtBusinessPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<UserOption> cboUser;
    @FXML private ComboBox<AddressOption> cboAddress;

    @FXML private Button btnRefresh;

    private final ObservableList<Employee> master = FXCollections.observableArrayList();
    private FilteredList<Employee> filtered;

    @FXML
    void initialize() {
        setupColumns();
        setupRoleOptions();
        AddressInputHelper.configureEditableAddressCombo(cboAddress);
        setupSelectionBinding();
        setupSearchFiltering();

        loadCombos();
        refreshTable();
    }

    private void setupColumns() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("employeeFirstName"));
        colMiddleInitial.setCellValueFactory(new PropertyValueFactory<>("employeeMiddleInitial"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("employeeLastName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("employeeRole"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("employeePhone"));
        colBusinessPhone.setCellValueFactory(new PropertyValueFactory<>("employeeBusinessPhone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("employeeEmail"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userDisplay"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("addressDisplay"));

        tblEmployees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupRoleOptions() {
        cboRole.setItems(FXCollections.observableArrayList(
                "Admin",
                "Manager",
                "Employee",
                "Baker",
                "Cashier",
                "Customer Support"
        ));
    }

    private void setupSelectionBinding() {
        tblEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) return;

            txtEmployeeId.setText(String.valueOf(selected.getEmployeeId()));
            txtFirstName.setText(StringUtil.nz(selected.getEmployeeFirstName()));
            txtMiddleInitial.setText(StringUtil.nz(selected.getEmployeeMiddleInitial()));
            txtLastName.setText(StringUtil.nz(selected.getEmployeeLastName()));
            cboRole.setValue(selected.getEmployeeRole());
            txtPhone.setText(StringUtil.nz(selected.getEmployeePhone()));
            txtBusinessPhone.setText(StringUtil.nz(selected.getEmployeeBusinessPhone()));
            txtEmail.setText(StringUtil.nz(selected.getEmployeeEmail()));

            selectBakeryById(selected.getBakeryId());
            selectUserById(selected.getUserId());
            AddressInputHelper.selectAddressById(cboAddress, selected.getAddressId());
        });
    }

    private void setupSearchFiltering() {
        filtered = new FilteredList<>(master, e -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();

            filtered.setPredicate(emp -> {
                if (q.isEmpty()) return true;

                return StringUtil.containsIgnoreCase(emp.getEmployeeFirstName(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeMiddleInitial(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeLastName(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeRole(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeePhone(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeBusinessPhone(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeEmail(), q)
                        || StringUtil.containsIgnoreCase(emp.getUserDisplay(), q)
                        || StringUtil.containsIgnoreCase(emp.getAddressDisplay(), q)
                        || String.valueOf(emp.getEmployeeId()).contains(q)
                        || String.valueOf(emp.getUserId()).contains(q)
                        || String.valueOf(emp.getAddressId()).contains(q);
            });

            lblStatus.setText(filtered.size() + " employee(s) shown");
        });

        SortedList<Employee> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblEmployees.comparatorProperty());
        tblEmployees.setItems(sorted);
    }

    private void loadCombos() {
        try {
            cboUser.setItems(FXCollections.observableArrayList(ReferenceApi.loadAdminUsers()));
            cboBakery.setItems(FXCollections.observableArrayList(ReferenceApi.loadBakeries()));
            List<AddressOption> addresses = ReferenceApi.loadAddresses();
            AddressInputHelper.setAddressItems(cboAddress, addresses);
        } catch (Exception e) {
            LogData.handleException("LOAD_EMPLOYEE_COMBOS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load User/Address lists.", e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            Map<Integer, String> addrDisp = new HashMap<>();
            for (AddressOption a : ReferenceApi.loadAddresses()) {
                addrDisp.put(a.getAddressId(), a.getLine1() + ", " + a.getCity());
            }
            master.clear();
            for (EmployeeApi.EmployeeRow row : EmployeeApi.listStaff()) {
                master.add(fromEmployeeRow(row, addrDisp));
            }
            lblStatus.setText(master.size() + " employee(s) loaded");
            LogData.logAction("READ", "Employee");
        } catch (Exception e) {
            LogData.handleException("READ_EMPLOYEES", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load employees.", e.getMessage());
        }
    }

    private Employee fromEmployeeRow(EmployeeApi.EmployeeRow row, Map<Integer, String> addrDisp) {
        Employee e = new Employee();
        e.setEmployeeId(row.id != null ? row.id : "");
        e.setUserId(row.userId != null ? row.userId : "");
        e.setBakeryId(row.bakeryId != null ? row.bakeryId : 0);
        e.setAddressId(row.addressId != null ? row.addressId : 0);
        e.setEmployeeFirstName(row.firstName != null ? row.firstName : "");
        e.setEmployeeMiddleInitial(row.middleInitial);
        e.setEmployeeLastName(row.lastName != null ? row.lastName : "");
        e.setEmployeeRole(row.position != null ? row.position : "");
        e.setEmployeePhone(row.phone != null ? row.phone : "");
        e.setEmployeeBusinessPhone("");
        e.setEmployeeEmail(row.workEmail != null ? row.workEmail : "");
        e.setUserDisplay(row.userId != null ? row.userId : "");
        String ad = row.addressId != null ? addrDisp.get(row.addressId) : null;
        e.setAddressDisplay(ad != null ? ad : "");
        return e;
    }

    private void selectBakeryById(int bakeryId) {
        if (cboBakery.getItems() == null) return;
        for (BakeryOption b : cboBakery.getItems()) {
            if (b.getBakeryId() == bakeryId) {
                cboBakery.getSelectionModel().select(b);
                return;
            }
        }
        cboBakery.getSelectionModel().clearSelection();
    }

    @FXML
    private void onRefresh() {
        loadCombos();
        refreshTable();
    }

    @FXML
    private void onClear() {
        tblEmployees.getSelectionModel().clearSelection();
        txtEmployeeId.clear();
        txtFirstName.clear();
        txtMiddleInitial.clear();
        txtLastName.clear();
        cboRole.setValue(null);
        txtPhone.clear();
        txtBusinessPhone.clear();
        txtEmail.clear();
        cboUser.getSelectionModel().clearSelection();
        if (cboBakery != null) {
            cboBakery.getSelectionModel().clearSelection();
        }
        AddressInputHelper.clearAddressField(cboAddress);
        lblStatus.setText("Cleared");
    }

    @FXML
    private void onCreate() {
        ValidationResult vr = validateForm(false);
        if (!vr.isOk()) {
            LogData.logAction("VALIDATION_FAILED", "Employee");
            ErrorHandler.showWarning("Validation", vr.getMessage());
            return;
        }

        try {
            Employee e = buildFromForm(false);
            UserOption u = cboUser.getValue();
            BakeryOption bk = cboBakery.getValue();
            EmployeeApi.create(
                    u.getUserId(),
                    bk.getBakeryId(),
                    e.getAddressId(),
                    e.getEmployeeFirstName(),
                    e.getEmployeeMiddleInitial(),
                    e.getEmployeeLastName(),
                    e.getEmployeeRole(),
                    e.getEmployeePhone(),
                    e.getEmployeeBusinessPhone(),
                    e.getEmployeeEmail()
            );
            LogData.logAction("CREATE", "Employee");
            refreshTable();
            lblStatus.setText("Created employee");
        } catch (IllegalArgumentException ex) {
            ErrorHandler.showWarning("Validation", ex.getMessage());
        } catch (Exception ex) {
            LogData.handleException("CREATE_EMPLOYEE", ex);
            ErrorHandler.showErrorDialog("Create Failed", "Could not create employee.", ex.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        if (txtEmployeeId.getText() == null || txtEmployeeId.getText().trim().isEmpty()) {
            ErrorHandler.showWarning("Update", "Select an employee row to update.");
            return;
        }

        ValidationResult vr = validateForm(true);
        if (!vr.isOk()) {
            LogData.logAction("VALIDATION_FAILED", "Employee");
            ErrorHandler.showWarning("Validation", vr.getMessage());
            return;
        }

        try {
            Employee e = buildFromForm(true);
            UserOption u = cboUser.getValue();
            BakeryOption bk = cboBakery.getValue();
            EmployeeApi.update(
                    e.getEmployeeId(),
                    u.getUserId(),
                    bk.getBakeryId(),
                    e.getAddressId(),
                    e.getEmployeeFirstName(),
                    e.getEmployeeMiddleInitial(),
                    e.getEmployeeLastName(),
                    e.getEmployeeRole(),
                    e.getEmployeePhone(),
                    e.getEmployeeBusinessPhone(),
                    e.getEmployeeEmail()
            );
            LogData.logAction("UPDATE", "Employee");
            refreshTable();
            selectEmployeeById(e.getEmployeeId());
            lblStatus.setText("Updated employee " + e.getEmployeeId());
        } catch (IllegalArgumentException ex) {
            ErrorHandler.showWarning("Validation", ex.getMessage());
        } catch (Exception ex) {
            LogData.handleException("UPDATE_EMPLOYEE", ex);
            ErrorHandler.showErrorDialog("Update Failed", "Could not update employee.", ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Employee selected = tblEmployees.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Delete", "Select an employee row to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete employee " + selected.getEmployeeId() + "?");
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            EmployeeApi.delete(selected.getEmployeeId());
            LogData.logAction("DELETE", "Employee");
            refreshTable();
            onClear();
            lblStatus.setText("Deleted employee " + selected.getEmployeeId());
        } catch (Exception ex) {
            LogData.handleException("DELETE_EMPLOYEE", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete employee.", ex.getMessage());
        }
    }

    private Employee buildFromForm(boolean includeId) throws Exception {
        Employee e = new Employee();

        if (includeId) {
            e.setEmployeeId(txtEmployeeId.getText().trim());
        }

        e.setEmployeeFirstName(txtFirstName.getText().trim());
        e.setEmployeeMiddleInitial(StringUtil.trimToNull(txtMiddleInitial.getText()));
        e.setEmployeeLastName(txtLastName.getText().trim());
        e.setEmployeeRole(cboRole.getValue());
        e.setEmployeePhone(txtPhone.getText().trim());
        e.setEmployeeBusinessPhone(StringUtil.trimToNull(txtBusinessPhone.getText()));
        e.setEmployeeEmail(txtEmail.getText().trim());

        UserOption u = cboUser.getValue();
        BakeryOption bk = cboBakery.getValue();
        AddressOption a = resolveAddressSelection(true);

        e.setUserId(u != null ? u.getUserId() : "");
        e.setBakeryId(bk != null ? bk.getBakeryId() : 0);
        e.setAddressId(a.getAddressId());

        return e;
    }

    private ValidationResult validateForm(boolean isUpdate) {
        String first = StringUtil.safe(txtFirstName.getText());
        String last = StringUtil.safe(txtLastName.getText());
        String role = cboRole.getValue();
        String phone = StringUtil.safe(txtPhone.getText());
        String email = StringUtil.safe(txtEmail.getText());
        String mi = StringUtil.safe(txtMiddleInitial.getText());
        UserOption user = cboUser.getValue();
        BakeryOption bakery = cboBakery.getValue();
        AddressOption addr = cboAddress.getValue();
        String typedAddress = AddressInputHelper.getTypedText(cboAddress);

        if (isUpdate) {
            String id = StringUtil.safe(txtEmployeeId.getText());
            if (id.isBlank()) return ValidationResult.fail("Employee ID is missing (select a row first).");
            try {
                java.util.UUID.fromString(id);
            } catch (IllegalArgumentException ex) {
                return ValidationResult.fail("Employee ID is invalid.");
            }
        }

        if (first.isBlank()) return ValidationResult.fail("First name is required.");
        if (last.isBlank()) return ValidationResult.fail("Last name is required.");
        if (role == null || role.isBlank()) return ValidationResult.fail("Position / role is required.");
        if (phone.isBlank()) return ValidationResult.fail("Phone is required.");
        if (!StringUtil.PHONE_RX.matcher(phone).matches()) return ValidationResult.fail("Phone format looks invalid.");

        if (email.isBlank()) return ValidationResult.fail("Email is required.");
        if (!StringUtil.EMAIL_RX.matcher(email).matches()) return ValidationResult.fail("Email format looks invalid.");

        if (!mi.isBlank() && mi.trim().length() > 2) return ValidationResult.fail("Middle initial must be 1\u20132 characters.");

        if (user == null) return ValidationResult.fail("User is required (select a User).");
        if (bakery == null) return ValidationResult.fail("Bakery is required.");
        if (addr == null && typedAddress.isBlank()) return ValidationResult.fail("Address is required.");

        if (first.length() > 50) return ValidationResult.fail("First name must be 50 characters or less.");
        if (last.length() > 50) return ValidationResult.fail("Last name must be 50 characters or less.");
        if (role.length() > 40) return ValidationResult.fail("Role must be 40 characters or less.");
        if (phone.length() > 20) return ValidationResult.fail("Phone must be 20 characters or less.");
        String biz = StringUtil.safe(txtBusinessPhone.getText());
        if (!biz.isBlank() && biz.length() > 20) return ValidationResult.fail("Business phone must be 20 characters or less.");
        if (email.length() > 254) return ValidationResult.fail("Email must be 254 characters or less.");

        return ValidationResult.ok();
    }

    private void selectEmployeeById(String id) {
        for (Employee e : master) {
            if (e.getEmployeeId().equals(id)) {
                tblEmployees.getSelectionModel().select(e);
                tblEmployees.scrollTo(e);
                return;
            }
        }
    }

    private void selectUserById(String userId) {
        if (cboUser.getItems() == null) return;
        if (userId == null || userId.isBlank()) {
            cboUser.getSelectionModel().clearSelection();
            return;
        }
        for (UserOption u : cboUser.getItems()) {
            if (u.getUserId().equals(userId)) {
                cboUser.getSelectionModel().select(u);
                return;
            }
        }
        cboUser.getSelectionModel().clearSelection();
    }

    private AddressOption resolveAddressSelection(boolean required) throws Exception {
        AddressOption selected = cboAddress.getValue();
        if (selected != null) return selected;

        String typed = AddressInputHelper.getTypedText(cboAddress);
        if (typed.isBlank()) {
            if (required) throw new IllegalArgumentException("Address is required.");
            return null;
        }

        AddressOption resolved = ReferenceApi.createAddressFromTyped(typed);
        loadCombos();
        AddressInputHelper.selectAddressById(cboAddress, resolved.getAddressId());
        return resolved;
    }
}
