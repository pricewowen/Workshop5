package com.sait.workshop05.controllers;

import com.sait.workshop05.api.EmployeeApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.models.BakeryOption;
import com.sait.workshop05.models.Employee;
import com.sait.workshop05.models.UserOption;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.AddressInputHelper;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
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
import java.util.stream.Collectors;

public class EmployeeManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Employee> tblEmployees;
    @FXML private TableColumn<Employee, String> colFirstName;
    @FXML private TableColumn<Employee, String> colLastName;
    @FXML private TableColumn<Employee, String> colRole;
    @FXML private TableColumn<Employee, String> colBakery;
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
    private List<UserOption> allUsers = new ArrayList<>();

    private List<UserOption> userOptions = new ArrayList<>();
    private List<BakeryOption> bakeryOptions = new ArrayList<>();
    private List<AddressOption> addressOptions = new ArrayList<>();
    private boolean isLoading = false;

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupActionsColumn();
        setupSearchFiltering();
        tblEmployees.setPlaceholder(new Label("Loading employees…"));
        loadAllAsync();
    }

    private void setupColumns() {
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("employeeFirstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("employeeLastName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("employeeRole"));
        colBakery.setCellValueFactory(new PropertyValueFactory<>("bakeryDisplay"));
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
                        || StringUtil.containsIgnoreCase(emp.getBakeryDisplay(), q)
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

    private record EmployeeBootstrapData(
            List<UserOption> users,
            List<BakeryOption> bakeries,
            List<AddressOption> addresses,
            List<EmployeeApi.EmployeeRow> rows
    ) {
    }

    private void loadAllAsync() {
        isLoading = true;
        lblStatus.setText("Loading employees…");
        tblEmployees.setPlaceholder(new Label("Loading employees…"));
        if (btnRefresh != null) {
            btnRefresh.setDisable(true);
        }
        Task<EmployeeBootstrapData> task = new Task<>() {
            @Override
            protected EmployeeBootstrapData call() throws Exception {
                List<UserOption> users = ReferenceApi.loadAdminUsers();
                List<BakeryOption> bakeries = ReferenceApi.loadBakeries();
                List<AddressOption> addresses = ReferenceApi.loadAddresses();
                List<EmployeeApi.EmployeeRow> rows = EmployeeApi.listStaff();
                return new EmployeeBootstrapData(users, bakeries, addresses, rows);
            }
        };
        task.setOnSucceeded(e -> {
            if (btnRefresh != null) {
                btnRefresh.setDisable(false);
            }
            isLoading = false;
            applyBootstrap(task.getValue());
            LogData.logAction("READ", "Employee");
        });
        task.setOnFailed(e -> {
            if (btnRefresh != null) {
                btnRefresh.setDisable(false);
            }
            Throwable t = task.getException();
            LogData.handleException("READ_EMPLOYEES", new RuntimeException(t));
            isLoading = false;
            lblStatus.setText("Could not load employees.");
            tblEmployees.setPlaceholder(new Label("Could not load employees."));
            ErrorHandler.showErrorDialog("API Error", "Could not load employees.", t);
        });
        new Thread(task, "employees-load").start();
    }

    private void applyBootstrap(EmployeeBootstrapData d) {
        allUsers = new ArrayList<>(d.users);
        userOptions = d.users;
        bakeryOptions = d.bakeries;
        addressOptions = d.addresses;

        Map<Integer, String> addrMap = new HashMap<>();
        for (AddressOption a : d.addresses) {
            addrMap.put(a.getAddressId(), a.getLine1() + ", " + a.getCity());
        }
        Map<Integer, String> bakeryMap = new HashMap<>();
        for (BakeryOption b : d.bakeries) {
            bakeryMap.put(b.getBakeryId(), b.getBakeryName());
        }
        master.clear();
        for (EmployeeApi.EmployeeRow row : d.rows) {
            master.add(fromRow(row, addrMap, bakeryMap));
        }
        lblStatus.setText(master.size() + " employee(s) loaded");
        if (filtered != null && filtered.isEmpty()) {
            tblEmployees.setPlaceholder(new Label(
                    master.isEmpty()
                            ? "No employees to display."
                            : "No employees match the current filter."));
        }
    }

    private Employee fromRow(EmployeeApi.EmployeeRow row, Map<Integer, String> addrMap,
                             Map<Integer, String> bakeryMap) {
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
        String bk = row.bakeryId != null ? bakeryMap.get(row.bakeryId) : null;
        e.setBakeryDisplay(bk != null ? bk : "");
        return e;
    }

    @FXML
    private void onRefresh() {
        loadAllAsync();
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

        List<String> roleList = new ArrayList<>(java.util.Arrays.asList(
                "Manager", "Employee", "Baker", "Cashier", "Customer Support"));
        if (UserSession.getInstance().isAdmin()) {
            roleList.add(0, "Admin");
        }
        ComboBox<String> cbRole = new ComboBox<>(FXCollections.observableArrayList(roleList));
        if (!isNew) cbRole.setValue(existing.getEmployeeRole());

        final ComboBox<UserOption> cbUserNew;
        final TextField tfUserReadOnly;

        if (isNew) {
            List<UserOption> assignablePool;
            try {
                assignablePool = ReferenceApi.loadUnlinkedStaffUsersForNewEmployee();
            } catch (Exception ex) {
                LogData.handleException("LOAD_ASSIGNABLE_USERS", ex);
                assignablePool = new ArrayList<>();
            }
            final List<UserOption> assignableForFilter = new ArrayList<>(assignablePool);
            cbUserNew = new ComboBox<>(FXCollections.observableArrayList(assignablePool));
            cbUserNew.setMaxWidth(Double.MAX_VALUE);
            cbUserNew.setEditable(true);
            cbUserNew.setConverter(new javafx.util.StringConverter<UserOption>() {
                @Override
                public String toString(UserOption option) {
                    return option == null ? "" : option.getUsername();
                }
                @Override
                public UserOption fromString(String s) {
                    if (s == null || s.isBlank()) return null;
                    String norm = s.trim();
                    return assignableForFilter.stream()
                            .filter(u -> u.getUsername().equalsIgnoreCase(norm))
                            .findFirst()
                            .orElse(null);
                }
            });
            cbUserNew.setOnKeyReleased(evt -> {
                javafx.scene.input.KeyCode code = evt.getCode();
                if (code == javafx.scene.input.KeyCode.ENTER || code == javafx.scene.input.KeyCode.ESCAPE
                        || code == javafx.scene.input.KeyCode.UP || code == javafx.scene.input.KeyCode.DOWN) {
                    return;
                }
                String typed = cbUserNew.getEditor().getText();
                cbUserNew.getSelectionModel().clearSelection();
                String lc = typed == null ? "" : typed.toLowerCase();
                List<UserOption> match = lc.isEmpty()
                        ? new ArrayList<>(assignableForFilter)
                        : assignableForFilter.stream()
                        .filter(u -> u.getUsername().toLowerCase().contains(lc))
                        .collect(Collectors.toList());
                cbUserNew.setItems(FXCollections.observableArrayList(match));
                if (typed != null) {
                    cbUserNew.getEditor().setText(typed);
                    cbUserNew.getEditor().positionCaret(typed.length());
                }
                if (!match.isEmpty()) {
                    cbUserNew.show();
                }
            });
            tfUserReadOnly = null;
        } else {
            cbUserNew = null;
            tfUserReadOnly = new TextField();
            tfUserReadOnly.setMaxWidth(Double.MAX_VALUE);
            tfUserReadOnly.setEditable(false);
            tfUserReadOnly.setFocusTraversable(false);
            tfUserReadOnly.setStyle("-fx-opacity: 1; -fx-control-inner-background: #f0ebe4;");
            String uid = existing.getUserId();
            String label = "";
            if (uid != null && !uid.isBlank()) {
                label = allUsers.stream()
                        .filter(u -> uid.equals(u.getUserId()))
                        .map(UserOption::getUsername)
                        .findFirst()
                        .orElse(uid);
            }
            tfUserReadOnly.setText(label.isBlank() ? "(no linked user)" : label);
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
        if (isNew) {
            addRow(grid, row++, "Username *", cbUserNew);
        } else {
            addRow(grid, row++, "Username (read-only)", tfUserReadOnly);
        }
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

        ButtonType saveType = new ButtonType(
                isNew ? "Create Employee" : "Save Changes",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (StringUtil.safe(tfFirstName.getText()).isBlank()) { showErr(lblError, "First name is required."); event.consume(); return; }
            if (StringUtil.safe(tfLastName.getText()).isBlank()) { showErr(lblError, "Last name is required."); event.consume(); return; }
            if (isNew && cbUserNew != null && cbUserNew.isEditable() && cbUserNew.getConverter() != null) {
                String typed = cbUserNew.getEditor().getText();
                UserOption match = cbUserNew.getConverter().fromString(typed);
                cbUserNew.setValue(match);
            }
            if (cbRole.getValue() == null) { showErr(lblError, "Role is required."); event.consume(); return; }
            if (StringUtil.safe(tfPhone.getText()).isBlank()) { showErr(lblError, "Phone is required."); event.consume(); return; }
            if (StringUtil.safe(tfEmail.getText()).isBlank()) { showErr(lblError, "Email is required."); event.consume(); return; }
            if (isNew && (cbUserNew == null || !(cbUserNew.getValue() instanceof UserOption))) {
                showErr(lblError, "Pick a user account from the list.");
                event.consume();
                return;
            }
            if (cbBakery.getValue() == null) { showErr(lblError, "Bakery is required."); event.consume(); return; }
            String addr = AddressInputHelper.getTypedText(cbAddress);
            if (cbAddress.getValue() == null && addr.isBlank()) { showErr(lblError, "Address is required."); event.consume(); }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
            try {
                AddressOption addr = resolveAddress(cbAddress);
                int addressId = addr != null ? addr.getAddressId() : (isNew ? 0 : existing.getAddressId());
                String userId = isNew ? cbUserNew.getValue().getUserId() : existing.getUserId();
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
                loadAllAsync();
            } catch (Exception ex) {
                LogData.handleException(isNew ? "CREATE_EMPLOYEE" : "UPDATE_EMPLOYEE", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save employee.", ex);
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
            loadAllAsync();
            lblStatus.setText("Employee deleted.");
        } catch (Exception ex) {
            LogData.handleException("DELETE_EMPLOYEE", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete employee.", ex);
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