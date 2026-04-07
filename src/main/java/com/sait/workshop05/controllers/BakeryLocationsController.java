package com.sait.workshop05.controllers;

import com.sait.workshop05.database.BakeryDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Address;
import com.sait.workshop05.models.Bakery;
import com.sait.workshop05.models.Province;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
import com.sait.workshop05.util.Validator;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public class BakeryLocationsController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Bakery> tblBakeryLocations;
    @FXML private TableColumn<Bakery, Integer> colBakeryId;
    @FXML private TableColumn<Bakery, String> colBakeryName;
    @FXML private TableColumn<Bakery, String> colBakeryPhone;
    @FXML private TableColumn<Bakery, String> colBakeryEmail;
    @FXML private TableColumn<Bakery, String> colBakeryAddress;
    @FXML private TableColumn<Bakery, String> colBakeryCity;
    @FXML private TableColumn<Bakery, String> colBakeryProvince;
    @FXML private TableColumn<Bakery, Void> colActions;

    // ── Toolbar ────────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnNewBakery;

    private final BakeryDAO dao = new BakeryDAO();
    private final ObservableList<Bakery> bakeryList = FXCollections.observableArrayList();
    private FilteredList<Bakery> filtered;

    private static final ObservableList<Province> PROVINCES = FXCollections.observableArrayList(
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

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupTableColumns();
        setupActionsColumn();
        setupSearchFiltering();
        refreshTable();
    }

    private void setupTableColumns() {
        colBakeryId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getBakeryId()));
        colBakeryName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getBakeryName()));
        colBakeryPhone.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getBakeryPhone()));
        colBakeryEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getBakeryEmail()));
        colBakeryAddress.setCellValueFactory(cd -> {
            Address a = cd.getValue().getAddress();
            if (a == null) return new ReadOnlyStringWrapper("");
            String full = a.getAddressLine1();
            if (a.getAddressLine2() != null && !a.getAddressLine2().isBlank())
                full += ", " + a.getAddressLine2();
            return new ReadOnlyStringWrapper(full);
        });
        colBakeryCity.setCellValueFactory(cd -> {
            Address a = cd.getValue().getAddress();
            return new ReadOnlyStringWrapper(a != null ? a.getAddressCity() : "");
        });
        colBakeryProvince.setCellValueFactory(cd -> {
            Address a = cd.getValue().getAddress();
            return new ReadOnlyStringWrapper(a != null ? a.getAddressProvince() : "");
        });
        tblBakeryLocations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                    Bakery b = getTableView().getItems().get(getIndex());
                    showBakeryDialog(b);
                });
                deleteBtn.setOnAction(e -> {
                    Bakery b = getTableView().getItems().get(getIndex());
                    handleDeleteBakery(b);
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
        filtered = new FilteredList<>(bakeryList, b -> true);

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
                                StringUtil.containsIgnoreCase(addr.getAddressLine1(), q)
                                || StringUtil.containsIgnoreCase(addr.getAddressLine2(), q)
                                || StringUtil.containsIgnoreCase(addr.getAddressCity(), q)
                                || StringUtil.containsIgnoreCase(addr.getAddressProvince(), q)
                                || StringUtil.containsIgnoreCase(addr.getAddressPostalCode(), q)
                        ));
            });
        });

        SortedList<Bakery> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblBakeryLocations.comparatorProperty());
        tblBakeryLocations.setItems(sorted);
    }

    // ────────────────────────────────────────────────────────────
    // Refresh
    // ────────────────────────────────────────────────────────────

    private void refreshTable() {
        try {
            bakeryList.clear();
            bakeryList.addAll(dao.getAllBakeries());
            if (lblStatus != null) lblStatus.setText(bakeryList.size() + " location(s) loaded");
            LogData.logAction("READ", "Bakeries");
        } catch (SQLException e) {
            LogData.handleException("READ_BAKERIES", e);
            ErrorHandler.showErrorDialog("Database Error", "Could not load bakeries.", e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        txtSearch.clear();
        refreshTable();
    }

    // ────────────────────────────────────────────────────────────
    // Create / Edit Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onNewBakery() {
        showBakeryDialog(null);
    }

    private void showBakeryDialog(Bakery existing) {
        boolean isNew = existing == null;
        Address existingAddr = isNew ? null : existing.getAddress();

        TextField tfName = new TextField(isNew ? "" : StringUtil.nz(existing.getBakeryName()));
        TextField tfPhone = new TextField(isNew ? "" : StringUtil.nz(existing.getBakeryPhone()));
        TextField tfEmail = new TextField(isNew ? "" : StringUtil.nz(existing.getBakeryEmail()));
        TextField tfLine1 = new TextField(existingAddr != null ? StringUtil.nz(existingAddr.getAddressLine1()) : "");
        TextField tfLine2 = new TextField(existingAddr != null ? StringUtil.nz(existingAddr.getAddressLine2()) : "");
        TextField tfCity = new TextField(existingAddr != null ? StringUtil.nz(existingAddr.getAddressCity()) : "");
        TextField tfPostal = new TextField(existingAddr != null ? StringUtil.nz(existingAddr.getAddressPostalCode()) : "");

        ComboBox<Province> cbProvince = new ComboBox<>(PROVINCES);
        cbProvince.setMaxWidth(Double.MAX_VALUE);
        cbProvince.setConverter(new StringConverter<>() {
            @Override public String toString(Province p) { return p != null ? p.getName() : ""; }
            @Override public Province fromString(String s) { return null; }
        });
        cbProvince.setValue(PROVINCES.getFirst());
        if (existingAddr != null) {
            PROVINCES.stream().filter(p -> p.getCode().equals(existingAddr.getAddressProvince()))
                    .findFirst().ifPresent(cbProvince::setValue);
        }

        // Phone formatter
        applyPhoneFormatter(tfPhone);
        applyCapFirstLetter(tfCity);
        applyPostalFormatter(tfPostal);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #B85C4C; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        GridPane grid = buildFormGrid();
        int row = 0;
        addRow(grid, row++, "Name *", tfName);
        addRow(grid, row++, "Phone *", tfPhone);
        addRow(grid, row++, "Email *", tfEmail);
        addRow(grid, row++, "Address Line 1 *", tfLine1);
        addRow(grid, row++, "Address Line 2", tfLine2);
        addRow(grid, row++, "City *", tfCity);
        addRow(grid, row++, "Province *", cbProvince);
        addRow(grid, row, "Postal Code *", tfPostal);

        VBox content = new VBox(12, grid, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "New Location" : "Edit Location");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modal-dialog-pane");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Province prov = cbProvince.getValue();
            String err = validateDialog(tfName, tfPhone, tfEmail, tfLine1, tfLine2,
                    tfCity, prov, tfPostal);
            if (err != null) {
                lblError.setText(err);
                lblError.setVisible(true);
                lblError.setManaged(true);
                event.consume();
                return;
            }
            // Duplicate check only for new bakeries
            if (isNew) {
                String dupErr = duplicateCheckDialog(tfName, tfEmail, tfPhone, tfLine1, tfLine2);
                if (dupErr != null) {
                    lblError.setText(dupErr);
                    lblError.setVisible(true);
                    lblError.setManaged(true);
                    event.consume();
                }
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            Address addr;
            Province prov = cbProvince.getValue();
            if (isNew) {
                addr = new Address(0, tfLine1.getText(), tfLine2.getText(), tfCity.getText(),
                        prov.getCode(), tfPostal.getText());
            } else {
                int addrId = existingAddr != null ? existingAddr.getAddressId() : 0;
                addr = new Address(addrId, tfLine1.getText(), tfLine2.getText(), tfCity.getText(),
                        prov.getCode(), tfPostal.getText());
            }

            try {
                if (isNew) {
                    Bakery b = new Bakery(0, addr, tfName.getText(), tfPhone.getText(), tfEmail.getText());
                    dao.insertBakery(b);
                    LogData.logAction("CREATE", "Bakery");
                    lblStatus.setText("Location created.");
                } else {
                    existing.setBakeryName(tfName.getText());
                    existing.setBakeryPhone(tfPhone.getText());
                    existing.setBakeryEmail(tfEmail.getText());
                    existing.setAddress(addr);
                    dao.updateBakery(existing);
                    LogData.logAction("UPDATE", "Bakery");
                    lblStatus.setText("Location updated.");
                }
                refreshTable();
            } catch (SQLException ex) {
                LogData.handleException(isNew ? "CREATE_BAKERY" : "UPDATE_BAKERY", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save location.", ex.getMessage());
            }
        });
    }

    private String validateDialog(TextField tfName, TextField tfPhone, TextField tfEmail,
                                   TextField tfLine1, TextField tfLine2, TextField tfCity,
                                   Province prov, TextField tfPostal) {
        String nameErr = Validator.isValidName(tfName.getText(), "Name");
        if (nameErr != null) return nameErr;
        String phoneErr = Validator.isValidPhoneNumber(tfPhone.getText());
        if (phoneErr != null) return phoneErr;
        String emailErr = Validator.isValidEmail(tfEmail.getText());
        if (emailErr != null) return emailErr;
        String addr1Err = Validator.isValidAddress(tfLine1.getText(), 1);
        if (addr1Err != null) return addr1Err;
        String addr2Err = Validator.isValidAddress(tfLine2.getText(), 2);
        if (addr2Err != null) return addr2Err;
        String cityErr = Validator.isValidName(tfCity.getText(), "City");
        if (cityErr != null) return cityErr;
        if (prov == null) return "Province is required.";
        String provErr = Validator.isValidProvince(prov.getCode());
        if (provErr != null) return provErr;
        String postalErr = Validator.isValidPostalCode(tfPostal.getText());
        if (postalErr != null) return postalErr;
        return null;
    }

    private String duplicateCheckDialog(TextField tfName, TextField tfEmail, TextField tfPhone,
                                         TextField tfLine1, TextField tfLine2) {
        try {
            ArrayList<Bakery> bakeries = dao.getAllBakeries();
            String name = tfName.getText().toLowerCase();
            String email = tfEmail.getText().toLowerCase();
            String phone = tfPhone.getText().toLowerCase();
            String line1 = tfLine1.getText().toLowerCase();
            String line2 = tfLine2.getText().toLowerCase();
            for (Bakery b : bakeries) {
                if (b.getBakeryName().trim().toLowerCase().equals(name)) return "Bakery name already exists.";
                if (b.getBakeryEmail().trim().toLowerCase().equals(email)) return "Bakery email already exists.";
                if (b.getBakeryPhone().trim().toLowerCase().equals(phone)) return "Bakery phone already exists.";
                Address a = b.getAddress();
                if (a != null) {
                    if (a.getAddressLine1().trim().toLowerCase().equals(line1)) return "Bakery address line 1 already exists.";
                    String bl2 = a.getAddressLine2() == null ? "" : a.getAddressLine2().trim().toLowerCase();
                    if (!bl2.isEmpty() && bl2.equals(line2)) return "Bakery address line 2 already exists.";
                }
            }
        } catch (SQLException e) {
            LogData.handleException("DUPLICATE_CHECK_BAKERY", e);
        }
        return null;
    }

    // ────────────────────────────────────────────────────────────
    // Delete
    // ────────────────────────────────────────────────────────────

    private void handleDeleteBakery(Bakery b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + b.getBakeryName() + "?");
        confirm.setContentText("This cannot be undone. Employees or products linked to this bakery may also be affected.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            dao.deleteBakery(b);
            LogData.logAction("DELETE", "Bakery");
            Sentry.withScope(scope -> {
                scope.setTag("action", "DELETE");
                scope.setTag("entity", "bakery");
                Sentry.captureMessage("Deleted bakery #" + b.getBakeryId() + " (" + b.getBakeryName() + ")", SentryLevel.WARNING);
            });
            refreshTable();
            lblStatus.setText("Deleted location #" + b.getBakeryId());
        } catch (SQLException ex) {
            LogData.handleException("DELETE_BAKERY", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "This bakery cannot be deleted as it is referenced by other records.", ex.getMessage());
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

    private void applyPhoneFormatter(TextField tf) {
        tf.textProperty().addListener((obs, oldText, newText) -> {
            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 10) digits = digits.substring(0, 10);
            StringBuilder sb = new StringBuilder();
            int len = digits.length();
            if (len > 0) sb.append("(");
            if (len >= 1) sb.append(digits.substring(0, Math.min(3, len)));
            if (len >= 4) { sb.append(") "); sb.append(digits.substring(3, Math.min(6, len))); }
            if (len >= 7) { sb.append("-"); sb.append(digits.substring(6)); }
            if (!sb.toString().equals(newText)) tf.setText(sb.toString());
        });
    }

    private void applyCapFirstLetter(TextField tf) {
        tf.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) return;
            String cap = newText.substring(0, 1).toUpperCase() + newText.substring(1).toLowerCase();
            if (!newText.equals(cap)) tf.setText(cap);
        });
    }

    private void applyPostalFormatter(TextField tf) {
        tf.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;
            String text = newText.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length() && i < 6; i++) {
                if (i == 3) sb.append(" ");
                sb.append(text.charAt(i));
            }
            if (!sb.toString().equals(newText)) tf.setText(sb.toString());
        });
    }
}
