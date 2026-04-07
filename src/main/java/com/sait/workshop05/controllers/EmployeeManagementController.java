package com.sait.workshop05.controllers;

import com.sait.workshop05.api.EmployeeApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.models.BakeryOption;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmployeeManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Employee> tblEmployees;
    @FXML private TableColumn<Employee, String> colEmployeeId;
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

    private final ObservableList<Employee> master = FXCollections.observableArrayList();
    private FilteredList<Employee> filtered;

    private List<UserOption> userOptions = new ArrayList<>();
    private List<BakeryOption> bakeryOptions = new ArrayList<>();
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
                        || StringUtil.containsIgnoreCase(emp.getEmployeeLastName(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeRole(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeePhone(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeEmail(), q)
                        || StringUtil.containsIgnoreCase(emp.getAddressDisplay(), q)
                        || StringUtil.containsIgnoreCase(emp.getEmployeeId(), q);
            });
            lblStatus.setText(filtered.size() + " employee(s) shown");
        });
        SortedList<Employee> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblEmployees.comparatorProperty());
        tblEmployees.setItems(sorted);
    }

    private void loadCombos() {
        try {
            userOptions = ReferenceApi.loadAdminUsers();
            bakeryOptions = ReferenceApi.loadBakeries();
            addressOptions = ReferenceApi.loadAddresses();
        } catch (Exception e) {
            LogData.handleException("LOAD_EMPLOYEE_COMBOS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load dropdown lists.", e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    // Refresh
    // ────────────────────────────────────────────────────────────

    private void refreshTable() {
        try {
            Map<Integer, String> addrMap = new HashMap<>();
            for (AddressOption a : ReferenceApi.loadAddresses()) {
                addrMap.put(a.getAddressId(), a.getLine1() + ", " + a.getCity());
            }
            master.clear();
            for (EmployeeApi.EmployeeRow row : EmployeeApi.listStaff()) {
                master.add(fromRow(row, addrMap));
            }
            lblStatus.setText(master.size() + " employee(s) loaded");
            LogData.logAction("READ", "Employee");
        } catch (Exception e) {
            LogData.handleException("READ_EMPLOYEES", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load employees.", e.getMessage());
        }
    }

    private Employee fromRow(EmployeeApi.EmployeeRow row, Map<Integer, String> addrMap) {
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
        e.setEmployeeEmail(row.workEmail != null ? row.workEmail : "");
        String ad = row.addressId != null ? addrMap.get(row.addressId) : null;
        e.setAddressDisplay(ad != null ? ad : "");
        return e;
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
        if (!isNew && existing.getUserId() != null && !existing.getUserId().isBlank()) {
            userOptions.stream().filter(u -> existing.getUserId().equals(u.getUserId()))
                    .findFirst().ifPresent(cbUser::setValue);
        }

        ComboBox<BakeryOption> cbBakery = new ComboBox<>(FXCollections.observableArrayList(bakeryOptions));
        cbBakery.setMaxWidth(Double.MAX_VALUE);
        if (!isNew && existing.getBakeryId() > 0) {
            bakeryOptions.stream().filter(b -> b.getBakeryId() == existing.getBakeryId())
                    .findFirst().ifPresent(cbBakery::setValue);
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
        addRow(grid, row++, "Bakery *", cbBakery);
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
            if (StringUtil.safe(tfFirstName.getText()).isBlank()) { showErr(lblError, "First name is required."); event.consume(); return; }
            if (StringUtil.safe(tfLastName.getText()).isBlank()) { showErr(lblError, "Last name is required."); event.consume(); return; }
            if (cbRole.getValue() == null) { showErr(lblError, "Role is required."); event.consume(); return; }
            if (StringUtil.safe(tfPhone.getText()).isBlank()) { showErr(lblError, "Phone is required."); event.consume(); return; }
            if (StringUtil.safe(tfEmail.getText()).isBlank()) { showErr(lblError, "Email is required."); event.consume(); return; }
            if (cbUser.getValue() == null) { showErr(lblError, "User account is required."); event.consume(); return; }
            if (cbBakery.getValue() == null) { showErr(lblError, "Bakery is required."); event.consume(); return; }
            String addr = AddressInputHelper.getTypedText(cbAddress);
            if (cbAddress.getValue() == null && addr.isBlank()) { showErr(lblError, "Address is required."); event.consume(); }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
            try {
                AddressOption addr = resolveAddress(cbAddress);
                int addressId = addr != null ? addr.getAddressId() : (isNew ? 0 : existing.getAddressId());
                String userId = cbUser.getValue().getUserId();
                int bakeryId = cbBakery.getValue().getBakeryId();
                String mi = tfMiddleInitial.getText().trim().isEmpty() ? null : tfMiddleInitial.getText().trim();
                String biz = tfBusinessPhone.getText().trim().isEmpty() ? null : tfBusinessPhone.getText().trim();

                if (isNew) {
                    EmployeeApi.create(userId, bakeryId, addressId,
                            tfFirstName.getText().trim(), mi, tfLastName.getText().trim(),
                            cbRole.getValue(), tfPhone.getText().trim(), biz, tfEmail.getText().trim());
                    LogData.logAction("CREATE", "Employee");
                    lblStatus.setText("Employee created.");
                } else {
                    EmployeeApi.update(existing.getEmployeeId(), userId, bakeryId, addressId,
                            tfFirstName.getText().trim(), mi, tfLastName.getText().trim(),
                            cbRole.getValue(), tfPhone.getText().trim(), biz, tfEmail.getText().trim());
                    LogData.logAction("UPDATE", "Employee");
                    lblStatus.setText("Employee updated.");
                }
                loadCombos();
                refreshTable();
            } catch (Exception ex) {
                LogData.handleException(isNew ? "CREATE_EMPLOYEE" : "UPDATE_EMPLOYEE", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save employee.", ex.getMessage());
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    // Delete
    // ────────────────────────────────────────────────────────────

    private void handleDeleteEmployee(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + emp.getEmployeeFirstName() + " " + emp.getEmployeeLastName() + "?");
        confirm.setContentText("This cannot be undone.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            EmployeeApi.delete(emp.getEmployeeId());
            LogData.logAction("DELETE", "Employee");
            Sentry.withScope(scope -> {
                scope.setTag("action", "DELETE");
                scope.setTag("entity", "employee");
                Sentry.captureMessage("Deleted employee " + emp.getEmployeeId()
                        + " (" + emp.getEmployeeFirstName() + " " + emp.getEmployeeLastName() + ")", SentryLevel.WARNING);
            });
            refreshTable();
            lblStatus.setText("Employee deleted.");
        } catch (Exception ex) {
            LogData.handleException("DELETE_EMPLOYEE", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete employee.", ex.getMessage());
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

    private void showErr(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private AddressOption resolveAddress(ComboBox<AddressOption> cbAddress) throws Exception {
        AddressOption selected = cbAddress.getValue();
        if (selected != null) return selected;
        String typed = AddressInputHelper.getTypedText(cbAddress);
        if (typed.isBlank()) return null;
        return ReferenceApi.createAddressFromTyped(typed);
    }
}
