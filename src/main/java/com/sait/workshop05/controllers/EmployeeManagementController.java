package com.sait.workshop05.controllers;

import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.database.EmployeeDAO;
import com.sait.workshop05.database.SharedDAO;
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

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmployeeManagementController {

    @FXML private TableView<Employee> tblEmployees;
    @FXML private TableColumn<Employee, Integer> colEmployeeId;
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
    @FXML private TextField txtPhone;
    @FXML private TextField txtBusinessPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<UserOption> cboUser;
    @FXML private ComboBox<AddressOption> cboAddress;

    @FXML private Button btnRefresh;

    private final EmployeeDAO dao = new EmployeeDAO();
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
            List<UserOption> users = dao.getUserOptions();
            cboUser.setItems(FXCollections.observableArrayList(users));

            List<AddressOption> addresses = dao.getAddressOptions();
            AddressInputHelper.setAddressItems(cboAddress, addresses);
        } catch (SQLException e) {
            LogData.handleException("LOAD_EMPLOYEE_COMBOS", e);
            ErrorHandler.showErrorDialog("Database Error", "Could not load User/Address lists.", e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            master.clear();
            master.addAll(dao.getAllEmployees());
            lblStatus.setText(master.size() + " employee(s) loaded");
            LogData.logAction("READ", "Employee");
        } catch (SQLException e) {
            LogData.handleException("READ_EMPLOYEES", e);
            ErrorHandler.showErrorDialog("Database Error", "Could not load employees.", e.getMessage());
        }
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

        Employee e;
        try {
            e = buildFromForm(false);
        } catch (IllegalArgumentException ex) {
            ErrorHandler.showWarning("Validation", ex.getMessage());
            return;
        } catch (SQLException ex) {
            LogData.handleException("CREATE_EMPLOYEE_ADDRESS", ex);
            ErrorHandler.showErrorDialog("Database Error", "Could not resolve address.", ex.getMessage());
            return;
        }

        try {
            int newId = dao.insertEmployee(e);
            LogData.logAction("CREATE", "Employee");
            refreshTable();

            if (newId > 0) {
                selectEmployeeById(newId);
                lblStatus.setText("Created employee #" + newId);
            } else {
                lblStatus.setText("Created employee");
            }

        } catch (SQLException ex) {
            LogData.handleException("CREATE_EMPLOYEE", ex);

            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Create Failed", "Could not create employee.", friendly);
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

        Employee e;
        try {
            e = buildFromForm(true);
        } catch (IllegalArgumentException ex) {
            ErrorHandler.showWarning("Validation", ex.getMessage());
            return;
        } catch (SQLException ex) {
            LogData.handleException("UPDATE_EMPLOYEE_ADDRESS", ex);
            ErrorHandler.showErrorDialog("Database Error", "Could not resolve address.", ex.getMessage());
            return;
        }

        try {
            boolean ok = dao.updateEmployee(e);
            LogData.logAction("UPDATE", "Employee");
            refreshTable();
            selectEmployeeById(e.getEmployeeId());
            lblStatus.setText(ok ? "Updated employee #" + e.getEmployeeId() : "No update applied");
        } catch (SQLException ex) {
            LogData.handleException("UPDATE_EMPLOYEE", ex);

            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Update Failed", "Could not update employee.", friendly);
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
        confirm.setHeaderText("Delete employee #" + selected.getEmployeeId() + "?");
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            dao.deleteEmployee(selected.getEmployeeId());
            LogData.logAction("DELETE", "Employee");
            refreshTable();
            onClear();
            lblStatus.setText("Deleted employee #" + selected.getEmployeeId());
        } catch (SQLException ex) {
            LogData.handleException("DELETE_EMPLOYEE", ex);

            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete employee.", friendly);
        }
    }

    private Employee buildFromForm(boolean includeId) throws SQLException {
        Employee e = new Employee();

        if (includeId) {
            e.setEmployeeId(Integer.parseInt(txtEmployeeId.getText().trim()));
        }

        e.setEmployeeFirstName(txtFirstName.getText().trim());
        e.setEmployeeMiddleInitial(StringUtil.trimToNull(txtMiddleInitial.getText()));
        e.setEmployeeLastName(txtLastName.getText().trim());
        e.setEmployeeRole(cboRole.getValue());
        e.setEmployeePhone(txtPhone.getText().trim());
        e.setEmployeeBusinessPhone(StringUtil.trimToNull(txtBusinessPhone.getText()));
        e.setEmployeeEmail(txtEmail.getText().trim());

        UserOption u = cboUser.getValue();
        AddressOption a = resolveAddressSelection(true);

        e.setUserId(u.getUserId());
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
        AddressOption addr = cboAddress.getValue();
        String typedAddress = AddressInputHelper.getTypedText(cboAddress);

        if (isUpdate) {
            String id = StringUtil.safe(txtEmployeeId.getText());
            if (id.isBlank()) return ValidationResult.fail("Employee ID is missing (select a row first).");
            try {
                Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                return ValidationResult.fail("Employee ID is invalid.");
            }
        }

        if (first.isBlank()) return ValidationResult.fail("First name is required.");
        if (last.isBlank()) return ValidationResult.fail("Last name is required.");
        if (role == null || role.isBlank()) return ValidationResult.fail("Role is required.");
        if (phone.isBlank()) return ValidationResult.fail("Phone is required.");
        if (!StringUtil.PHONE_RX.matcher(phone).matches()) return ValidationResult.fail("Phone format looks invalid.");

        if (email.isBlank()) return ValidationResult.fail("Email is required.");
        if (!StringUtil.EMAIL_RX.matcher(email).matches()) return ValidationResult.fail("Email format looks invalid.");

        if (!mi.isBlank() && mi.trim().length() > 2) return ValidationResult.fail("Middle initial must be 1\u20132 characters.");

        if (user == null) return ValidationResult.fail("User is required (select a User).");
        if (addr == null && typedAddress.isBlank()) return ValidationResult.fail("Address is required.");

        // matches SQL limits
        if (first.length() > 50) return ValidationResult.fail("First name must be 50 characters or less.");
        if (last.length() > 50) return ValidationResult.fail("Last name must be 50 characters or less.");
        if (role.length() > 40) return ValidationResult.fail("Role must be 40 characters or less.");
        if (phone.length() > 20) return ValidationResult.fail("Phone must be 20 characters or less.");
        String biz = StringUtil.safe(txtBusinessPhone.getText());
        if (!biz.isBlank() && biz.length() > 20) return ValidationResult.fail("Business phone must be 20 characters or less.");
        if (email.length() > 254) return ValidationResult.fail("Email must be 254 characters or less.");

        return ValidationResult.ok();
    }

    private void selectEmployeeById(int id) {
        for (Employee e : master) {
            if (e.getEmployeeId() == id) {
                tblEmployees.getSelectionModel().select(e);
                tblEmployees.scrollTo(e);
                return;
            }
        }
    }

    private void selectUserById(int userId) {
        if (cboUser.getItems() == null) return;
        for (UserOption u : cboUser.getItems()) {
            if (u.getUserId() == userId) {
                cboUser.getSelectionModel().select(u);
                return;
            }
        }
        cboUser.getSelectionModel().clearSelection();
    }

    private AddressOption resolveAddressSelection(boolean required) throws SQLException {
        AddressOption selected = cboAddress.getValue();
        if (selected != null) return selected;

        String typed = AddressInputHelper.getTypedText(cboAddress);
        if (typed.isBlank()) {
            if (required) throw new IllegalArgumentException("Address is required.");
            return null;
        }

        AddressOption resolved = SharedDAO.findOrCreateAddressFromInput(typed);
        loadCombos();
        AddressInputHelper.selectAddressById(cboAddress, resolved.getAddressId());
        return resolved;
    }
}
