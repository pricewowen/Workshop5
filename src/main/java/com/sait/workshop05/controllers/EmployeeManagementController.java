package com.sait.workshop05.controllers;

import com.sait.workshop05.database.AddressOption;
import com.sait.workshop05.database.EmployeeDAO;
import com.sait.workshop05.database.UserOption;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Employee;
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
import java.util.regex.Pattern;

public class EmployeeManagementController {

    private static final String LOG_USER = "EMPLOYEE_VIEW";

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

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_RX = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    @FXML
    void initialize() {
        setupColumns();
        setupRoleOptions();
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
            txtFirstName.setText(nz(selected.getEmployeeFirstName()));
            txtMiddleInitial.setText(nz(selected.getEmployeeMiddleInitial()));
            txtLastName.setText(nz(selected.getEmployeeLastName()));
            cboRole.setValue(selected.getEmployeeRole());
            txtPhone.setText(nz(selected.getEmployeePhone()));
            txtBusinessPhone.setText(nz(selected.getEmployeeBusinessPhone()));
            txtEmail.setText(nz(selected.getEmployeeEmail()));

            selectUserById(selected.getUserId());
            selectAddressById(selected.getAddressId());
        });
    }

    private void setupSearchFiltering() {
        filtered = new FilteredList<>(master, e -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();

            filtered.setPredicate(emp -> {
                if (q.isEmpty()) return true;

                return contains(emp.getEmployeeFirstName(), q)
                        || contains(emp.getEmployeeMiddleInitial(), q)
                        || contains(emp.getEmployeeLastName(), q)
                        || contains(emp.getEmployeeRole(), q)
                        || contains(emp.getEmployeePhone(), q)
                        || contains(emp.getEmployeeBusinessPhone(), q)
                        || contains(emp.getEmployeeEmail(), q)
                        || contains(emp.getUserDisplay(), q)
                        || contains(emp.getAddressDisplay(), q)
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
            cboAddress.setItems(FXCollections.observableArrayList(addresses));
        } catch (SQLException e) {
            LogData.handleException("LOAD_EMPLOYEE_COMBOS", e);
            showError("Database Error", "Could not load User/Address lists.", e.getMessage());
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
            showError("Database Error", "Could not load employees.", e.getMessage());
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
        cboAddress.getSelectionModel().clearSelection();
        lblStatus.setText("Cleared");
    }

    @FXML
    private void onCreate() {
        ValidationResult vr = validateForm(false);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "Employee");
            showWarning("Validation", vr.message);
            return;
        }

        Employee e = buildFromForm(false);

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

            String friendly = friendlyDbMessage(ex);
            showError("Create Failed", "Could not create employee.", friendly);
        }
    }

    @FXML
    private void onUpdate() {
        if (txtEmployeeId.getText() == null || txtEmployeeId.getText().trim().isEmpty()) {
            showWarning("Update", "Select an employee row to update.");
            return;
        }

        ValidationResult vr = validateForm(true);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "Employee");
            showWarning("Validation", vr.message);
            return;
        }

        Employee e = buildFromForm(true);

        try {
            boolean ok = dao.updateEmployee(e);
            LogData.logAction("UPDATE", "Employee");
            refreshTable();
            selectEmployeeById(e.getEmployeeId());
            lblStatus.setText(ok ? "Updated employee #" + e.getEmployeeId() : "No update applied");
        } catch (SQLException ex) {
            LogData.handleException("UPDATE_EMPLOYEE", ex);

            String friendly = friendlyDbMessage(ex);
            showError("Update Failed", "Could not update employee.", friendly);
        }
    }

    @FXML
    private void onDelete() {
        Employee selected = tblEmployees.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Delete", "Select an employee row to delete.");
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

            String friendly = friendlyDbMessage(ex);
            showError("Delete Failed", "Could not delete employee.", friendly);
        }
    }

    private Employee buildFromForm(boolean includeId) {
        Employee e = new Employee();

        if (includeId) {
            e.setEmployeeId(Integer.parseInt(txtEmployeeId.getText().trim()));
        }

        e.setEmployeeFirstName(txtFirstName.getText().trim());
        e.setEmployeeMiddleInitial(trimToNull(txtMiddleInitial.getText()));
        e.setEmployeeLastName(txtLastName.getText().trim());
        e.setEmployeeRole(cboRole.getValue());
        e.setEmployeePhone(txtPhone.getText().trim());
        e.setEmployeeBusinessPhone(trimToNull(txtBusinessPhone.getText()));
        e.setEmployeeEmail(txtEmail.getText().trim());

        UserOption u = cboUser.getValue();
        AddressOption a = cboAddress.getValue();

        e.setUserId(u.getUserId());
        e.setAddressId(a.getAddressId());

        return e;
    }

    private ValidationResult validateForm(boolean isUpdate) {
        String first = safe(txtFirstName.getText());
        String last = safe(txtLastName.getText());
        String role = cboRole.getValue();
        String phone = safe(txtPhone.getText());
        String email = safe(txtEmail.getText());
        String mi = safe(txtMiddleInitial.getText());
        UserOption user = cboUser.getValue();
        AddressOption addr = cboAddress.getValue();

        if (isUpdate) {
            String id = safe(txtEmployeeId.getText());
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
        if (!PHONE_RX.matcher(phone).matches()) return ValidationResult.fail("Phone format looks invalid.");

        if (email.isBlank()) return ValidationResult.fail("Email is required.");
        if (!EMAIL_RX.matcher(email).matches()) return ValidationResult.fail("Email format looks invalid.");

        if (!mi.isBlank() && mi.trim().length() > 2) return ValidationResult.fail("Middle initial must be 1–2 characters.");

        if (user == null) return ValidationResult.fail("User is required (select a User).");
        if (addr == null) return ValidationResult.fail("Address is required (select an Address).");

        // matches SQL limits
        if (first.length() > 50) return ValidationResult.fail("First name must be 50 characters or less.");
        if (last.length() > 50) return ValidationResult.fail("Last name must be 50 characters or less.");
        if (role.length() > 40) return ValidationResult.fail("Role must be 40 characters or less.");
        if (phone.length() > 20) return ValidationResult.fail("Phone must be 20 characters or less.");
        String biz = safe(txtBusinessPhone.getText());
        if (!biz.isBlank() && biz.length() > 20) return ValidationResult.fail("Business phone must be 20 characters or less.");
        if (email.length() > 254) return ValidationResult.fail("Email must be 254 characters or less.");

        return ValidationResult.ok();
    }

    private String friendlyDbMessage(SQLException ex) {
        String sqlState = ex.getSQLState();
        String msg = (ex.getMessage() == null) ? "" : ex.getMessage();

        if (sqlState != null && sqlState.startsWith("23")) {
            if (msg.toLowerCase().contains("uq_employee_user") || msg.toLowerCase().contains("duplicate")) {
                return "That User is already linked to another employee. Pick a different User.";
            }
            if (msg.toLowerCase().contains("fk_batch_employee") || msg.toLowerCase().contains("fk_review_employee")
                    || msg.toLowerCase().contains("foreign key constraint")) {
                return "This employee is referenced by other records (e.g., batches/reviews). Remove those references first.";
            }
            return "This operation violates a database constraint (duplicate or referenced record).";
        }

        return msg.isBlank() ? "Unknown database error." : msg;
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

    private void selectAddressById(int addressId) {
        if (cboAddress.getItems() == null) return;
        for (AddressOption a : cboAddress.getItems()) {
            if (a.getAddressId() == addressId) {
                cboAddress.getSelectionModel().select(a);
                return;
            }
        }
        cboAddress.getSelectionModel().clearSelection();
    }

    private void showWarning(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showError(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private static boolean contains(String field, String q) {
        if (field == null) return false;
        return field.toLowerCase().contains(q);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static class ValidationResult {
        final boolean ok;
        final String message;

        private ValidationResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        static ValidationResult ok() {
            return new ValidationResult(true, "");
        }

        static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg);
        }
    }
}
