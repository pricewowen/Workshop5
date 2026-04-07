package com.sait.workshop05.controllers;

import com.sait.workshop05.database.EmployeeDAO;
import com.sait.workshop05.database.SharedDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.models.Employee;
import com.sait.workshop05.models.UserOption;
import com.sait.workshop05.util.AddressInputHelper;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Employee> tblEmployees;
    @FXML private TableColumn<Employee, Integer> colEmployeeId;
    @FXML private TableColumn<Employee, String> colFirstName;
    @FXML private TableColumn<Employee, String> colLastName;
    @FXML private TableColumn<Employee, String> colRole;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colAddress;
    @FXML private TableColumn<Employee, Void> colActions;

    // ── Toolbar ────────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnNewEmployee;

    private final EmployeeDAO dao = new EmployeeDAO();
    private final ObservableList<Employee> master = FXCollections.observableArrayList();
    private FilteredList<Employee> filtered;

    // Cached options for dialogs
    private List<UserOption> userOptions = new ArrayList<>();
    private List<AddressOption> addressOptions = new ArrayList<>();

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupActionsColumn();
        setupSearchFiltering();
        loadCombos();
        refreshTable();
    }

    private void setupColumns() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("employeeFirstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("employeeLastName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("employeeRole"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("employeePhone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("employeeEmail"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("addressDisplay"));
        tblEmployees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-icon-edit");
                deleteBtn.getStyleClass().add("btn-icon-delete");
                editBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    showEmployeeDialog(emp);
                });
                deleteBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    handleDeleteEmployee(emp);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
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
                        || StringUtil.containsIgnoreCase(emp.getAddressDisplay(), q)
                        || String.valueOf(emp.getEmployeeId()).contains(q);
            });
            lblStatus.setText(filtered.size() + " employee(s) shown");
        });

        SortedList<Employee> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblEmployees.comparatorProperty());
        tblEmployees.setItems(sorted);
    }

    private void loadCombos() {
        try {
            userOptions = dao.getUserOptions();
            addressOptions = dao.getAddressOptions();
        } catch (SQLException e) {
            LogData.handleException("LOAD_EMPLOYEE_COMBOS", e);
            ErrorHandler.showErrorDialog("Database Error", "Could not load User/Address lists.", e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    // Refresh
    // ────────────────────────────────────────────────────────────

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

    // ────────────────────────────────────────────────────────────
    // Create / Edit Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onNewEmployee() {
        showEmployeeDialog(null);
    }

    private void showEmployeeDialog(Employee existing) {
        boolean isNew = existing == null;

        TextField tfFirstName = new TextField(isNew ? "" : StringUtil.nz(existing.getEmployeeFirstName()));
        TextField tfMiddleInitial = new TextField(isNew ? "" : StringUtil.nz(existing.getEmployeeMiddleInitial()));
        tfMiddleInitial.setMaxWidth(70);
        TextField tfLastName = new TextField(isNew ? "" : StringUtil.nz(existing.getEmployeeLastName()));
        TextField tfPhone = new TextField(isNew ? "" : StringUtil.nz(existing.getEmployeePhone()));
        TextField tfBusinessPhone = new TextField(isNew ? "" : StringUtil.nz(existing.getEmployeeBusinessPhone()));
        TextField tfEmail = new TextField(isNew ? "" : StringUtil.nz(existing.getEmployeeEmail()));

        ComboBox<String> cbRole = new ComboBox<>(FXCollections.observableArrayList(
                "Admin", "Manager", "Employee", "Baker", "Cashier", "Customer Support"));
        if (!isNew) cbRole.setValue(existing.getEmployeeRole());

        ComboBox<UserOption> cbUser = new ComboBox<>(FXCollections.observableArrayList(userOptions));
        cbUser.setMaxWidth(Double.MAX_VALUE);
        if (!isNew && existing.getUserId() > 0) {
            userOptions.stream().filter(u -> u.getUserId() == existing.getUserId())
                    .findFirst().ifPresent(cbUser::setValue);
        }

        ComboBox<AddressOption> cbAddress = new ComboBox<>();
        AddressInputHelper.configureEditableAddressCombo(cbAddress);
        AddressInputHelper.setAddressItems(cbAddress, addressOptions);
        if (!isNew && existing.getAddressId() > 0) {
            AddressInputHelper.selectAddressById(cbAddress, existing.getAddressId());
        }
        cbAddress.setMaxWidth(Double.MAX_VALUE);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #B85C4C; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        GridPane grid = buildFormGrid();
        int row = 0;
        addRow(grid, row++, "First Name *", tfFirstName);
        addRow(grid, row++, "Middle Initial", tfMiddleInitial);
        addRow(grid, row++, "Last Name *", tfLastName);
        addRow(grid, row++, "Role *", cbRole);
        addRow(grid, row++, "Email *", tfEmail);
        addRow(grid, row++, "Phone *", tfPhone);
        addRow(grid, row++, "Business Phone", tfBusinessPhone);
        addRow(grid, row++, "User Account *", cbUser);
        addRow(grid, row, "Address *", cbAddress);

        VBox content = new VBox(12, grid, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "New Employee" : "Edit Employee");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modal-dialog-pane");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String err = validateDialog(tfFirstName, tfLastName, tfPhone, tfEmail,
                    tfMiddleInitial, cbRole, cbUser, cbAddress);
            if (err != null) {
                lblError.setText(err);
                lblError.setVisible(true);
                lblError.setManaged(true);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            Employee e = isNew ? new Employee() : existing;
            e.setEmployeeFirstName(tfFirstName.getText().trim());
            e.setEmployeeMiddleInitial(StringUtil.trimToNull(tfMiddleInitial.getText()));
            e.setEmployeeLastName(tfLastName.getText().trim());
            e.setEmployeeRole(cbRole.getValue());
            e.setEmployeePhone(tfPhone.getText().trim());
            e.setEmployeeBusinessPhone(StringUtil.trimToNull(tfBusinessPhone.getText()));
            e.setEmployeeEmail(tfEmail.getText().trim());
            if (cbUser.getValue() != null) e.setUserId(cbUser.getValue().getUserId());

            try {
                AddressOption addr = resolveAddress(cbAddress);
                if (addr != null) e.setAddressId(addr.getAddressId());

                if (isNew) {
                    int newId = dao.insertEmployee(e);
                    LogData.logAction("CREATE", "Employee");
                    refreshTable();
                    if (newId > 0) { selectEmployeeById(newId); lblStatus.setText("Created employee #" + newId); }
                } else {
                    dao.updateEmployee(e);
                    LogData.logAction("UPDATE", "Employee");
                    refreshTable();
                    selectEmployeeById(e.getEmployeeId());
                    lblStatus.setText("Updated employee #" + e.getEmployeeId());
                }
            } catch (SQLException ex) {
                LogData.handleException(isNew ? "CREATE_EMPLOYEE" : "UPDATE_EMPLOYEE", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save employee.", ErrorHandler.friendlyDbMessage(ex));
            }
        });
    }

    private String validateDialog(TextField tfFirst, TextField tfLast, TextField tfPhone,
                                   TextField tfEmail, TextField tfMI,
                                   ComboBox<String> cbRole, ComboBox<UserOption> cbUser,
                                   ComboBox<AddressOption> cbAddress) {
        if (StringUtil.safe(tfFirst.getText()).isBlank()) return "First name is required.";
        if (StringUtil.safe(tfLast.getText()).isBlank()) return "Last name is required.";
        if (cbRole.getValue() == null) return "Role is required.";
        if (StringUtil.safe(tfPhone.getText()).isBlank()) return "Phone is required.";
        if (!StringUtil.PHONE_RX.matcher(StringUtil.safe(tfPhone.getText())).matches()) return "Phone format looks invalid.";
        if (StringUtil.safe(tfEmail.getText()).isBlank()) return "Email is required.";
        if (!StringUtil.EMAIL_RX.matcher(StringUtil.safe(tfEmail.getText())).matches()) return "Email format looks invalid.";
        String mi = StringUtil.safe(tfMI.getText());
        if (!mi.isBlank() && mi.trim().length() > 2) return "Middle initial must be 1-2 characters.";
        if (cbUser.getValue() == null) return "User account is required.";
        String addr = AddressInputHelper.getTypedText(cbAddress);
        if (cbAddress.getValue() == null && addr.isBlank()) return "Address is required.";
        return null;
    }

    // ────────────────────────────────────────────────────────────
    // Delete
    // ────────────────────────────────────────────────────────────

    private void handleDeleteEmployee(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete employee #" + emp.getEmployeeId() + "?");
        confirm.setContentText("This cannot be undone.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            dao.deleteEmployee(emp.getEmployeeId());
            LogData.logAction("DELETE", "Employee");
            Sentry.withScope(scope -> {
                scope.setTag("action", "DELETE");
                scope.setTag("entity", "employee");
                Sentry.captureMessage("Deleted employee #" + emp.getEmployeeId()
                        + " (" + emp.getEmployeeFirstName() + " " + emp.getEmployeeLastName() + ")", SentryLevel.WARNING);
            });
            refreshTable();
            lblStatus.setText("Deleted employee #" + emp.getEmployeeId());
        } catch (SQLException ex) {
            LogData.handleException("DELETE_EMPLOYEE", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete employee.", ErrorHandler.friendlyDbMessage(ex));
        }
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private GridPane buildFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(130);
        c0.setHgrow(Priority.NEVER);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setMaxWidth(Double.MAX_VALUE);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, Control control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        control.setMaxWidth(Double.MAX_VALUE);
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
    }

    private AddressOption resolveAddress(ComboBox<AddressOption> cbAddress) throws SQLException {
        AddressOption selected = cbAddress.getValue();
        if (selected != null) return selected;
        String typed = AddressInputHelper.getTypedText(cbAddress);
        if (typed.isBlank()) return null;
        AddressOption resolved = SharedDAO.findOrCreateAddressFromInput(typed);
        loadCombos();
        return resolved;
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
}
