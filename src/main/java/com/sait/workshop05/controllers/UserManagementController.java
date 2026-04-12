package com.sait.workshop05.controllers;

import com.sait.workshop05.api.UserManagementApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class UserManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<UserRow> tblUsers;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colEmail;
    @FXML private TableColumn<UserRow, String> colRole;
    @FXML private TableColumn<UserRow, String> colStatus;
    @FXML private TableColumn<UserRow, Void>   colActions;

    // ── Toolbar ────────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label     lblStatus;
    @FXML private Button    btnRefresh;
    @FXML private Button    btnNewUser;

    private final ObservableList<UserRow> master = FXCollections.observableArrayList();
    private FilteredList<UserRow> filtered;

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupActionsColumn();
        setupSearchFiltering();
        loadUsers();
    }

    private void setupColumns() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleDisplay"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText(item);
                setStyle("Active".equals(item)
                        ? "-fx-text-fill: #3A7A3A; -fx-font-weight: bold;"
                        : "-fx-text-fill: #B85C4C; -fx-font-weight: bold;");
            }
        });
        tblUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();

            {
                toggleBtn.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    handleToggleActive(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                UserRow row = getTableView().getItems().get(getIndex());
                toggleBtn.setText(row.isActive() ? "Deactivate" : "Activate");
                toggleBtn.getStyleClass().setAll(row.isActive() ? "btn-icon-delete" : "btn-icon-edit");
                setGraphic(toggleBtn);
            }
        });
    }

    private void setupSearchFiltering() {
        filtered = new FilteredList<>(master, u -> true);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(u -> {
                if (q.isEmpty()) return true;
                return StringUtil.containsIgnoreCase(u.getUsername(), q)
                        || StringUtil.containsIgnoreCase(u.getEmail(), q)
                        || StringUtil.containsIgnoreCase(u.getRoleDisplay(), q);
            });
            lblStatus.setText(filtered.size() + " user(s) shown");
        });
        SortedList<UserRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblUsers.comparatorProperty());
        tblUsers.setItems(sorted);
    }

    // ────────────────────────────────────────────────────────────
    // Data
    // ────────────────────────────────────────────────────────────

    private void loadUsers() {
        try {
            master.clear();
            for (UserManagementApi.UserRow u : UserManagementApi.listUsers()) {
                master.add(new UserRow(u));
            }
            lblStatus.setText(master.size() + " user(s) loaded");
            LogData.logAction("READ", "User");
        } catch (Exception e) {
            LogData.handleException("READ_USERS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load users.", e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        loadUsers();
    }

    // ────────────────────────────────────────────────────────────
    // Create User Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onNewUser() {
        TextField tfUsername = new TextField();
        tfUsername.setPromptText("e.g., jsmith");
        TextField tfEmail = new TextField();
        tfEmail.setPromptText("e.g., jane@peelin.ca");
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Min 6 characters");
        ComboBox<String> cbRole = new ComboBox<>(
                FXCollections.observableArrayList("Employee", "Customer"));
        cbRole.setMaxWidth(Double.MAX_VALUE);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #B85C4C; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        GridPane grid = buildFormGrid();
        addRow(grid, 0, "Username *", tfUsername);
        addRow(grid, 1, "Email *", tfEmail);
        addRow(grid, 2, "Password *", pfPassword);
        addRow(grid, 3, "Role *", cbRole);

        VBox content = new VBox(12, grid, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New User Account");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(440);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modal-dialog-pane");

        ButtonType saveType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String err = validateCreate(tfUsername, tfEmail, pfPassword, cbRole);
            if (err != null) {
                lblError.setText(err);
                lblError.setVisible(true);
                lblError.setManaged(true);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
            String roleApi = "Employee".equals(cbRole.getValue()) ? "employee" : "customer";
            try {
                UserManagementApi.createUser(
                        tfUsername.getText().trim(),
                        tfEmail.getText().trim(),
                        pfPassword.getText(),
                        roleApi);
                LogData.logAction("CREATE", "User: " + tfUsername.getText().trim());
                loadUsers();
                lblStatus.setText("User '" + tfUsername.getText().trim() + "' created.");
            } catch (Exception ex) {
                LogData.handleException("CREATE_USER", ex);
                ErrorHandler.showErrorDialog("Create Failed", "Could not create user account.", ex.getMessage());
            }
        });
    }

    private String validateCreate(TextField tfUser, TextField tfEmail,
                                  PasswordField pfPw, ComboBox<String> cbRole) {
        if (StringUtil.safe(tfUser.getText()).isBlank()) return "Username is required.";
        if (tfUser.getText().trim().length() < 3) return "Username must be at least 3 characters.";
        if (StringUtil.safe(tfEmail.getText()).isBlank()) return "Email is required.";
        if (!tfEmail.getText().trim().contains("@")) return "Enter a valid email address.";
        if (StringUtil.safe(pfPw.getText()).isBlank()) return "Password is required.";
        if (pfPw.getText().length() < 6) return "Password must be at least 6 characters.";
        if (cbRole.getValue() == null) return "Role is required.";
        return null;
    }

    // ────────────────────────────────────────────────────────────
    // Toggle Active
    // ────────────────────────────────────────────────────────────

    private void handleToggleActive(UserRow row) {
        boolean newActive = !row.isActive();
        String action = newActive ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText((newActive ? "Activate" : "Deactivate") + " user: " + row.getUsername());
        confirm.setContentText(newActive
                ? "This user will be able to log in again."
                : "This user will no longer be able to log in.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            UserManagementApi.setActive(row.getId(), newActive);
            LogData.logAction(newActive ? "ACTIVATE" : "DEACTIVATE", "User: " + row.getUsername());
            loadUsers();
            lblStatus.setText("User '" + row.getUsername() + "' " + action + "d.");
        } catch (Exception ex) {
            LogData.handleException("TOGGLE_USER_ACTIVE", ex);
            ErrorHandler.showErrorDialog("Update Failed", "Could not update user status.", ex.getMessage());
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
        c0.setMinWidth(110);
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

    // ────────────────────────────────────────────────────────────
    // View model
    // ────────────────────────────────────────────────────────────

    public static final class UserRow {
        private final String id;
        private final String username;
        private final String email;
        private final String roleDisplay;
        private final String statusDisplay;
        private final boolean active;

        public UserRow(UserManagementApi.UserRow u) {
            this.id = u.id;
            this.username = u.username;
            this.email = u.email;
            this.roleDisplay = capitalize(u.role);
            this.active = u.active;
            this.statusDisplay = u.active ? "Active" : "Inactive";
        }

        public String getId()            { return id; }
        public String getUsername()      { return username; }
        public String getEmail()         { return email; }
        public String getRoleDisplay()   { return roleDisplay; }
        public String getStatusDisplay() { return statusDisplay; }
        public boolean isActive()        { return active; }

        private static String capitalize(String s) {
            if (s == null || s.isEmpty()) return "";
            return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
        }
    }
}
