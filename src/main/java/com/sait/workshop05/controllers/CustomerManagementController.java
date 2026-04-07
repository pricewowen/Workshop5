package com.sait.workshop05.controllers;

import com.sait.workshop05.database.*;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.*;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.OrderStatus;
import com.sait.workshop05.util.StringUtil;
import com.sait.workshop05.util.AddressInputHelper;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, Integer> colCustomerId;
    @FXML private TableColumn<Customer, String> colFirstName;
    @FXML private TableColumn<Customer, String> colLastName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colRewardTier;
    @FXML private TableColumn<Customer, Integer> colRewardBalance;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private TableColumn<Customer, Void> colActions;

    // ── Toolbar ────────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnNewCustomer;
    @FXML private Button btnOrderHistory;
    @FXML private Button btnAdjustPoints;

    private final CustomerDAO dao = new CustomerDAO();
    private final OrderDAO orderDao = new OrderDAO();
    private final ObservableList<Customer> master = FXCollections.observableArrayList();
    private FilteredList<Customer> filtered;

    // Cached options for dialogs
    private List<RewardTierOption> tierOptions = new ArrayList<>();
    private List<UserOption> userOptions = new ArrayList<>();
    private List<AddressOption> addressOptions = new ArrayList<>();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupActionsColumn();
        setupSearchFiltering();
        setupSelectionButtons();
        loadCombos();
        refreshTable();
    }

    private void setupColumns() {
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRewardTier.setCellValueFactory(new PropertyValueFactory<>("rewardTierDisplay"));
        colRewardBalance.setCellValueFactory(new PropertyValueFactory<>("rewardBalance"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("addressDisplay"));
        tblCustomers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                    Customer c = getTableView().getItems().get(getIndex());
                    showCustomerDialog(c);
                });
                deleteBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    handleDeleteCustomer(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupSelectionButtons() {
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean hasSelection = selected != null;
            btnOrderHistory.setDisable(!hasSelection);
            btnAdjustPoints.setDisable(!hasSelection);
        });
    }

    private void setupSearchFiltering() {
        filtered = new FilteredList<>(master, c -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();

            filtered.setPredicate(cust -> {
                if (q.isEmpty()) return true;
                return StringUtil.containsIgnoreCase(cust.getFirstName(), q)
                        || StringUtil.containsIgnoreCase(cust.getMiddleInitial(), q)
                        || StringUtil.containsIgnoreCase(cust.getLastName(), q)
                        || StringUtil.containsIgnoreCase(cust.getEmail(), q)
                        || StringUtil.containsIgnoreCase(cust.getPhone(), q)
                        || StringUtil.containsIgnoreCase(cust.getBusinessPhone(), q)
                        || StringUtil.containsIgnoreCase(cust.getRole(), q)
                        || StringUtil.containsIgnoreCase(cust.getRewardTierDisplay(), q)
                        || StringUtil.containsIgnoreCase(cust.getAddressDisplay(), q)
                        || String.valueOf(cust.getCustomerId()).contains(q)
                        || String.valueOf(cust.getRewardBalance()).contains(q);
            });

            lblStatus.setText(filtered.size() + " customer(s) shown");
        });

        SortedList<Customer> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblCustomers.comparatorProperty());
        tblCustomers.setItems(sorted);
    }

    private void loadCombos() {
        try {
            tierOptions = dao.getRewardTierOptions();
            userOptions = dao.getUserOptions();
            addressOptions = dao.getAddressOptions();
        } catch (SQLException e) {
            LogData.handleException("LOAD_CUSTOMER_COMBOS", e);
            ErrorHandler.showErrorDialog("Database Error", "Could not load dropdown lists.", e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    // Refresh
    // ────────────────────────────────────────────────────────────

    private void refreshTable() {
        try {
            master.clear();
            master.addAll(dao.getAllCustomers());
            lblStatus.setText(master.size() + " customer(s) loaded");
            LogData.logAction("READ", "Customer");
        } catch (SQLException e) {
            LogData.handleException("READ_CUSTOMERS", e);
            ErrorHandler.showErrorDialog("Database Error", "Could not load customers.", e.getMessage());
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
    private void onNewCustomer() {
        showCustomerDialog(null);
    }

    private void showCustomerDialog(Customer existing) {
        boolean isNew = existing == null;

        // Form fields
        TextField tfFirstName = new TextField(isNew ? "" : StringUtil.nz(existing.getFirstName()));
        TextField tfMiddleInitial = new TextField(isNew ? "" : StringUtil.nz(existing.getMiddleInitial()));
        tfMiddleInitial.setMaxWidth(70);
        TextField tfLastName = new TextField(isNew ? "" : StringUtil.nz(existing.getLastName()));
        TextField tfPhone = new TextField(isNew ? "" : StringUtil.nz(existing.getPhone()));
        TextField tfBusinessPhone = new TextField(isNew ? "" : StringUtil.nz(existing.getBusinessPhone()));
        TextField tfEmail = new TextField(isNew ? "" : StringUtil.nz(existing.getEmail()));
        TextField tfRewardBalance = new TextField(isNew ? "0" : String.valueOf(existing.getRewardBalance()));

        ComboBox<String> cbRole = new ComboBox<>(FXCollections.observableArrayList(
                "Regular", "VIP", "Wholesale", "Corporate"));
        if (!isNew) cbRole.setValue(existing.getRole());

        ComboBox<RewardTierOption> cbTier = new ComboBox<>(FXCollections.observableArrayList(tierOptions));
        cbTier.setMaxWidth(Double.MAX_VALUE);
        if (!isNew && existing.getRewardTierId() > 0) {
            tierOptions.stream().filter(t -> t.getRewardTierId() == existing.getRewardTierId())
                    .findFirst().ifPresent(cbTier::setValue);
        }

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
        addRow(grid, row++, "Email *", tfEmail);
        addRow(grid, row++, "Phone *", tfPhone);
        addRow(grid, row++, "Business Phone", tfBusinessPhone);
        addRow(grid, row++, "Role", cbRole);
        addRow(grid, row++, "Reward Tier *", cbTier);
        addRow(grid, row++, "Reward Points", tfRewardBalance);
        addRow(grid, row++, "User Account", cbUser);
        addRow(grid, row, "Address *", cbAddress);

        VBox content = new VBox(12, grid, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "New Customer" : "Edit Customer");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modal-dialog-pane");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        // Intercept OK to validate before closing
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String err = validateDialog(tfFirstName, tfLastName, tfPhone, tfEmail,
                    tfMiddleInitial, tfRewardBalance, cbTier, cbAddress);
            if (err != null) {
                lblError.setText(err);
                lblError.setVisible(true);
                lblError.setManaged(true);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            Customer c = isNew ? new Customer() : existing;
            c.setFirstName(tfFirstName.getText().trim());
            c.setMiddleInitial(StringUtil.trimToNull(tfMiddleInitial.getText()));
            c.setLastName(tfLastName.getText().trim());
            c.setEmail(tfEmail.getText().trim());
            c.setPhone(tfPhone.getText().trim());
            c.setBusinessPhone(StringUtil.trimToNull(tfBusinessPhone.getText()));
            c.setRole(cbRole.getValue());
            if (cbTier.getValue() != null) c.setRewardTierId(cbTier.getValue().getRewardTierId());
            try {
                c.setRewardBalance(Integer.parseInt(tfRewardBalance.getText().trim()));
            } catch (NumberFormatException ignored) {
                c.setRewardBalance(0);
            }
            if (cbUser.getValue() != null) c.setUserId(cbUser.getValue().getUserId());
            c.setTierAssignedDate(LocalDateTime.now());

            try {
                AddressOption addr = resolveAddress(cbAddress);
                if (addr != null) c.setAddressId(addr.getAddressId());

                if (isNew) {
                    int newId = dao.insertCustomer(c);
                    LogData.logAction("CREATE", "Customer");
                    refreshTable();
                    if (newId > 0) { selectCustomerById(newId); lblStatus.setText("Created customer #" + newId); }
                } else {
                    dao.updateCustomer(c);
                    LogData.logAction("UPDATE", "Customer");
                    refreshTable();
                    selectCustomerById(c.getCustomerId());
                    lblStatus.setText("Updated customer #" + c.getCustomerId());
                }
            } catch (SQLException ex) {
                LogData.handleException(isNew ? "CREATE_CUSTOMER" : "UPDATE_CUSTOMER", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save customer.", ErrorHandler.friendlyDbMessage(ex));
            }
        });
    }

    private String validateDialog(TextField tfFirst, TextField tfLast, TextField tfPhone,
                                   TextField tfEmail, TextField tfMI, TextField tfBalance,
                                   ComboBox<RewardTierOption> cbTier,
                                   ComboBox<AddressOption> cbAddress) {
        if (StringUtil.safe(tfFirst.getText()).isBlank()) return "First name is required.";
        if (StringUtil.safe(tfLast.getText()).isBlank()) return "Last name is required.";
        if (StringUtil.safe(tfPhone.getText()).isBlank()) return "Phone is required.";
        if (!StringUtil.PHONE_RX.matcher(StringUtil.safe(tfPhone.getText())).matches()) return "Phone format looks invalid.";
        if (StringUtil.safe(tfEmail.getText()).isBlank()) return "Email is required.";
        if (!StringUtil.EMAIL_RX.matcher(StringUtil.safe(tfEmail.getText())).matches()) return "Email format looks invalid.";
        String mi = StringUtil.safe(tfMI.getText());
        if (!mi.isBlank() && mi.trim().length() > 2) return "Middle initial must be 1-2 characters.";
        if (cbTier.getValue() == null) return "Reward tier is required.";
        String addr = AddressInputHelper.getTypedText(cbAddress);
        if (cbAddress.getValue() == null && addr.isBlank()) return "Address is required.";
        String balStr = StringUtil.safe(tfBalance.getText());
        if (!balStr.isEmpty()) {
            try {
                int bal = Integer.parseInt(balStr);
                if (bal < 0) return "Reward balance cannot be negative.";
            } catch (NumberFormatException e) {
                return "Reward balance must be a whole number.";
            }
        }
        return null;
    }

    // ────────────────────────────────────────────────────────────
    // Delete
    // ────────────────────────────────────────────────────────────

    private void handleDeleteCustomer(Customer c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete customer #" + c.getCustomerId() + " (" + c.getFullName() + ")?");
        confirm.setContentText("This cannot be undone. Orders linked to this customer may also be affected.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            dao.deleteCustomer(c.getCustomerId());
            LogData.logAction("DELETE", "Customer");
            Sentry.withScope(scope -> {
                scope.setTag("action", "DELETE");
                scope.setTag("entity", "customer");
                Sentry.captureMessage("Deleted customer #" + c.getCustomerId() + " (" + c.getFullName() + ")", SentryLevel.WARNING);
            });
            refreshTable();
            lblStatus.setText("Deleted customer #" + c.getCustomerId());
        } catch (SQLException ex) {
            LogData.handleException("DELETE_CUSTOMER", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete customer.", ErrorHandler.friendlyDbMessage(ex));
        }
    }

    // ────────────────────────────────────────────────────────────
    // Order History Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onViewOrderHistory() {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Order History", "Select a customer row first.");
            return;
        }

        try {
            List<Order> orders = orderDao.getOrdersByCustomerId(selected.getCustomerId());
            LogData.logAction("VIEW_ORDER_HISTORY", "Customer #" + selected.getCustomerId());
            showOrderHistoryDialog(selected, orders);
        } catch (SQLException e) {
            LogData.handleException("VIEW_ORDER_HISTORY", e);
            ErrorHandler.showErrorDialog("Database Error", "Could not load order history.", e.getMessage());
        }
    }

    private void showOrderHistoryDialog(Customer customer, List<Order> orders) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order History \u2014 " + customer.getFullName());
        dialog.setHeaderText(orders.size() + " order(s) found for " + customer.getFullName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        TableView<Order> tblOrders = new TableView<>();
        tblOrders.setPrefHeight(250);

        TableColumn<Order, Integer> colOrdId = new TableColumn<>("Order ID");
        colOrdId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrdId.setPrefWidth(70);

        TableColumn<Order, String> colOrdDate = new TableColumn<>("Placed");
        colOrdDate.setCellValueFactory(new PropertyValueFactory<>("orderPlacedDateTime"));
        colOrdDate.setPrefWidth(140);
        colOrdDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(""); return; }
                Order o = getTableView().getItems().get(getIndex());
                setText(o.getOrderPlacedDateTime() != null ? o.getOrderPlacedDateTime().format(DT_FMT) : "");
            }
        });

        TableColumn<Order, String> colOrdMethod = new TableColumn<>("Method");
        colOrdMethod.setCellValueFactory(new PropertyValueFactory<>("orderMethod"));
        colOrdMethod.setPrefWidth(90);

        TableColumn<Order, Double> colOrdTotal = new TableColumn<>("Total");
        colOrdTotal.setCellValueFactory(new PropertyValueFactory<>("orderTotal"));
        colOrdTotal.setPrefWidth(90);
        colOrdTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

        TableColumn<Order, String> colOrdStatus = new TableColumn<>("Status");
        colOrdStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        colOrdStatus.setPrefWidth(100);

        TableColumn<Order, String> colOrdBakery = new TableColumn<>("Bakery");
        colOrdBakery.setCellValueFactory(new PropertyValueFactory<>("bakeryDisplay"));
        colOrdBakery.setPrefWidth(120);

        tblOrders.getColumns().addAll(colOrdId, colOrdDate, colOrdMethod, colOrdTotal, colOrdStatus, colOrdBakery);
        tblOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label lblItems = new Label("Order Items (select an order above)");
        lblItems.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        TableView<OrderItem> tblItems = new TableView<>();
        tblItems.setPrefHeight(180);

        TableColumn<OrderItem, String> colItemProduct = new TableColumn<>("Product");
        colItemProduct.setCellValueFactory(new PropertyValueFactory<>("productDisplay"));
        colItemProduct.setPrefWidth(200);

        TableColumn<OrderItem, Integer> colItemQty = new TableColumn<>("Qty");
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("orderItemQuantity"));
        colItemQty.setPrefWidth(60);

        TableColumn<OrderItem, Double> colItemTotal = new TableColumn<>("Line Total");
        colItemTotal.setCellValueFactory(new PropertyValueFactory<>("orderItemLineTotal"));
        colItemTotal.setPrefWidth(100);
        colItemTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

        tblItems.getColumns().addAll(colItemProduct, colItemQty, colItemTotal);
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ComboBox<String> cboStatusFilter = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.FILTER_STATUSES));
        cboStatusFilter.setValue("All");

        FilteredList<Order> filteredOrders = new FilteredList<>(FXCollections.observableArrayList(orders), o -> true);
        cboStatusFilter.valueProperty().addListener((obs, old, val) ->
                filteredOrders.setPredicate(o -> val == null || "All".equals(val) || val.equalsIgnoreCase(o.getOrderStatus())));

        SortedList<Order> sortedOrders = new SortedList<>(filteredOrders);
        sortedOrders.comparatorProperty().bind(tblOrders.comparatorProperty());
        tblOrders.setItems(sortedOrders);

        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, old, selOrder) -> {
            if (selOrder == null) { tblItems.getItems().clear(); return; }
            try {
                List<OrderItem> items = orderDao.getOrderItems(selOrder.getOrderId());
                tblItems.setItems(FXCollections.observableArrayList(items));
                lblItems.setText("Order #" + selOrder.getOrderId() + " \u2014 " + items.size() + " item(s)");
            } catch (SQLException e) {
                LogData.handleException("LOAD_ORDER_ITEMS", e);
                tblItems.getItems().clear();
            }
        });

        HBox filterBar = new HBox(8, new Label("Filter by status:"), cboStatusFilter);
        VBox content = new VBox(10, filterBar, tblOrders, lblItems, tblItems);
        content.setPadding(new Insets(10));
        VBox.setVgrow(tblOrders, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(750, 580);
        dialog.showAndWait();
    }

    // ────────────────────────────────────────────────────────────
    // Adjust Points Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onAdjustPoints() {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Adjust Points", "Select a customer row first.");
            return;
        }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Adjust Loyalty Points");
        dialog.setHeaderText("Customer: " + selected.getFullName()
                + "\nCurrent Balance: " + selected.getRewardBalance() + " points");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtAdjust = new TextField("0");
        txtAdjust.setPromptText("Positive to add, negative to subtract");
        grid.add(new Label("Adjustment:"), 0, 0);
        grid.add(txtAdjust, 1, 0);

        Label lblPreview = new Label("New Balance: " + selected.getRewardBalance());
        grid.add(lblPreview, 1, 1);

        txtAdjust.textProperty().addListener((obs, old, val) -> {
            try {
                int adj = Integer.parseInt(val.trim());
                int newBal = selected.getRewardBalance() + adj;
                lblPreview.setText("New Balance: " + newBal + (newBal < 0 ? " (invalid!)" : ""));
            } catch (NumberFormatException e) {
                lblPreview.setText("New Balance: (invalid input)");
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try { return Integer.parseInt(txtAdjust.getText().trim()); }
                catch (NumberFormatException e) { return null; }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(adjustment -> {
            int newBalance = selected.getRewardBalance() + adjustment;
            if (newBalance < 0) { ErrorHandler.showWarning("Invalid", "Reward balance cannot go below 0."); return; }
            try {
                dao.updateRewardBalance(selected.getCustomerId(), newBalance);
                LogData.logAction("ADJUST_POINTS", "Customer #" + selected.getCustomerId()
                        + " adjusted by " + adjustment + " (new: " + newBalance + ")");
                refreshTable();
                selectCustomerById(selected.getCustomerId());
                lblStatus.setText("Adjusted points for " + selected.getFullName() + " by " + adjustment + " -> " + newBalance);
            } catch (SQLException e) {
                LogData.handleException("ADJUST_POINTS", e);
                ErrorHandler.showErrorDialog("Error", "Could not update reward balance.", e.getMessage());
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private GridPane buildFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        javafx.scene.layout.ColumnConstraints c0 = new javafx.scene.layout.ColumnConstraints();
        c0.setMinWidth(130);
        c0.setHgrow(Priority.NEVER);
        javafx.scene.layout.ColumnConstraints c1 = new javafx.scene.layout.ColumnConstraints();
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

    private void selectCustomerById(int id) {
        for (Customer c : master) {
            if (c.getCustomerId() == id) {
                tblCustomers.getSelectionModel().select(c);
                tblCustomers.scrollTo(c);
                return;
            }
        }
    }
}
