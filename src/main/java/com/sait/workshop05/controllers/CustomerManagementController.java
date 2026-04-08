package com.sait.workshop05.controllers;

import com.sait.workshop05.api.CustomerApi;
import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.api.RewardTierApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.*;
import com.sait.workshop05.util.AddressInputHelper;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.OrderStatus;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomerManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, String> colCustomerId;
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
    @FXML private Button btnOrderHistory;
    @FXML private Button btnAdjustPoints;

    private final ObservableList<Customer> master = FXCollections.observableArrayList();
    private FilteredList<Customer> filtered;
    private List<RewardTierApi.RewardTierJson> tierData = new ArrayList<>();

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
            private final HBox box = new HBox(editBtn);

            {
                editBtn.getStyleClass().add("btn-icon-edit");
                editBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    showCustomerDialog(c);
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
        if (btnOrderHistory != null) btnOrderHistory.setDisable(true);
        if (btnAdjustPoints != null) btnAdjustPoints.setDisable(true);
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean has = selected != null;
            if (btnOrderHistory != null) btnOrderHistory.setDisable(!has);
            if (btnAdjustPoints != null) btnAdjustPoints.setDisable(!has);
        });
    }

    private void setupSearchFiltering() {
        filtered = new FilteredList<>(master, c -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();
            filtered.setPredicate(cust -> {
                if (q.isEmpty()) return true;
                return StringUtil.containsIgnoreCase(cust.getFirstName(), q)
                        || StringUtil.containsIgnoreCase(cust.getLastName(), q)
                        || StringUtil.containsIgnoreCase(cust.getEmail(), q)
                        || StringUtil.containsIgnoreCase(cust.getPhone(), q)
                        || StringUtil.containsIgnoreCase(cust.getRewardTierDisplay(), q)
                        || StringUtil.containsIgnoreCase(cust.getAddressDisplay(), q)
                        || StringUtil.containsIgnoreCase(cust.getCustomerId(), q)
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
            List<RewardTierApi.RewardTierJson> tiers = RewardTierApi.list();
            tierData = new ArrayList<>(tiers);
            List<RewardTierOption> tierOptionsList = new ArrayList<>();
            for (RewardTierApi.RewardTierJson t : tiers) {
                if (t.id != null) tierOptionsList.add(new RewardTierOption(t.id, t.name != null ? t.name : ""));
            }
            tierOptions = tierOptionsList;
            userOptions = ReferenceApi.loadAdminUsers();
            addressOptions = ReferenceApi.loadAddresses();
        } catch (Exception e) {
            LogData.handleException("LOAD_CUSTOMER_COMBOS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load dropdown lists.", e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    // Refresh
    // ────────────────────────────────────────────────────────────

    private void refreshTable() {
        try {
            Map<Integer, String> tierMap = new HashMap<>();
            for (RewardTierApi.RewardTierJson t : RewardTierApi.list()) {
                if (t.id != null) tierMap.put(t.id, t.name != null ? t.name : "");
            }
            Map<Integer, String> addrMap = new HashMap<>();
            for (AddressOption a : ReferenceApi.loadAddresses()) {
                addrMap.put(a.getAddressId(), a.getLine1() + ", " + a.getCity());
            }
            master.clear();
            for (CustomerApi.CustomerRow row : CustomerApi.list()) {
                master.add(fromCustomerRow(row, tierMap, addrMap));
            }
            lblStatus.setText(master.size() + " customer(s) loaded");
            LogData.logAction("READ", "Customer");
        } catch (Exception e) {
            LogData.handleException("READ_CUSTOMERS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load customers.", e.getMessage());
        }
    }

    private Customer fromCustomerRow(CustomerApi.CustomerRow row, Map<Integer, String> tierMap, Map<Integer, String> addrMap) {
        Customer c = new Customer();
        c.setCustomerId(row.id != null ? row.id : "");
        c.setUserId(row.userId != null ? row.userId : "");
        c.setFirstName(row.firstName != null ? row.firstName : "");
        c.setMiddleInitial(row.middleInitial != null ? row.middleInitial : "");
        c.setLastName(row.lastName != null ? row.lastName : "");
        c.setEmail(row.email != null ? row.email : "");
        c.setPhone(row.phone != null ? row.phone : "");
        c.setRewardBalance(row.rewardBalance);
        if (row.rewardTierId != null) {
            c.setRewardTierId(row.rewardTierId);
            c.setRewardTierDisplay(tierMap.getOrDefault(row.rewardTierId, ""));
        }
        if (row.addressId != null) {
            c.setAddressId(row.addressId);
            c.setAddressDisplay(addrMap.getOrDefault(row.addressId, ""));
        }
        return c;
    }

    @FXML
    private void onRefresh() {
        loadCombos();
        refreshTable();
    }

    // ────────────────────────────────────────────────────────────
    // Edit Dialog (no create/delete — not in API)
    // ────────────────────────────────────────────────────────────

    private void showCustomerDialog(Customer existing) {
        TextField tfFirstName = new TextField(StringUtil.nz(existing.getFirstName()));
        TextField tfMiddleInitial = new TextField(StringUtil.nz(existing.getMiddleInitial()));
        tfMiddleInitial.setMaxWidth(70);
        TextField tfLastName = new TextField(StringUtil.nz(existing.getLastName()));
        TextField tfPhone = new TextField(StringUtil.nz(existing.getPhone()));
        TextField tfBusinessPhone = new TextField(StringUtil.nz(existing.getBusinessPhone()));
        TextField tfEmail = new TextField(StringUtil.nz(existing.getEmail()));
        TextField tfRewardBalance = new TextField(String.valueOf(existing.getRewardBalance()));

        ComboBox<RewardTierOption> cbTier = new ComboBox<>(FXCollections.observableArrayList(tierOptions));
        cbTier.setMaxWidth(Double.MAX_VALUE);
        tierOptions.stream().filter(t -> t.getRewardTierId() == existing.getRewardTierId())
                .findFirst().ifPresent(cbTier::setValue);

        ComboBox<AddressOption> cbAddress = new ComboBox<>();
        AddressInputHelper.configureEditableAddressCombo(cbAddress);
        AddressInputHelper.setAddressItems(cbAddress, addressOptions);
        if (existing.getAddressId() > 0) AddressInputHelper.selectAddressById(cbAddress, existing.getAddressId());
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
        addRow(grid, row++, "Reward Tier *", cbTier);
        addRow(grid, row++, "Reward Points", tfRewardBalance);
        addRow(grid, row, "Address *", cbAddress);

        VBox content = new VBox(12, grid, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Customer");
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
            if (StringUtil.safe(tfPhone.getText()).isBlank()) { showErr(lblError, "Phone is required."); event.consume(); return; }
            if (StringUtil.safe(tfEmail.getText()).isBlank()) { showErr(lblError, "Email is required."); event.consume(); return; }
            if (cbTier.getValue() == null) { showErr(lblError, "Reward tier is required."); event.consume(); }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
            try {
                AddressOption addr = resolveAddress(cbAddress);
                Map<String, Object> body = CustomerApi.patchBodyForProfile(
                        tfFirstName.getText().trim(),
                        tfMiddleInitial.getText().trim().isEmpty() ? null : tfMiddleInitial.getText().trim(),
                        tfLastName.getText().trim(),
                        tfPhone.getText().trim(),
                        tfBusinessPhone.getText().trim().isEmpty() ? null : tfBusinessPhone.getText().trim(),
                        tfEmail.getText().trim(),
                        addr != null ? addr.getAddressId() : existing.getAddressId(),
                        cbTier.getValue() != null ? cbTier.getValue().getRewardTierId() : existing.getRewardTierId()
                );
                CustomerApi.patch(existing.getCustomerId(), body);
                LogData.logAction("UPDATE", "Customer");
                refreshTable();
                selectCustomerById(existing.getCustomerId());
                lblStatus.setText("Updated customer " + existing.getCustomerId());
            } catch (Exception ex) {
                LogData.handleException("UPDATE_CUSTOMER", ex);
                ErrorHandler.showErrorDialog("Update Failed", "Could not update customer.", ex.getMessage());
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    // Order History Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onViewOrderHistory() {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) { ErrorHandler.showWarning("Order History", "Select a customer row first."); return; }

        try {
            List<Order> orders = OrderApi.listOrdersForCustomer(selected.getCustomerId());
            LogData.logAction("VIEW_ORDER_HISTORY", "Customer " + selected.getCustomerId());
            showOrderHistoryDialog(selected, orders);
        } catch (Exception e) {
            LogData.handleException("VIEW_ORDER_HISTORY", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load order history.", e.getMessage());
        }
    }

    private void showOrderHistoryDialog(Customer customer, List<Order> orders) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order History — " + customer.getFirstName() + " " + customer.getLastName());
        dialog.setHeaderText(orders.size() + " order(s) found");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        TableView<Order> tblOrders = new TableView<>();
        tblOrders.setPrefHeight(260);

        TableColumn<Order, String> colId = new TableColumn<>("Order ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colId.setPrefWidth(90);

        TableColumn<Order, String> colDate = new TableColumn<>("Placed");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderPlacedDateTime"));
        colDate.setPrefWidth(140);
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(""); return; }
                Order o = getTableView().getItems().get(getIndex());
                setText(o.getOrderPlacedDateTime() != null ? o.getOrderPlacedDateTime().format(DT_FMT) : "");
            }
        });

        TableColumn<Order, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("orderMethod"));
        colMethod.setPrefWidth(90);

        TableColumn<Order, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("orderTotal"));
        colTotal.setPrefWidth(90);
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        colStatus.setPrefWidth(100);

        tblOrders.getColumns().addAll(colId, colDate, colMethod, colTotal, colStatus);
        tblOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label lblItems = new Label("Order Items (select an order above)");
        lblItems.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        TableView<OrderItem> tblItems = new TableView<>();
        tblItems.setPrefHeight(180);

        TableColumn<OrderItem, String> colProduct = new TableColumn<>("Product");
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productDisplay"));
        colProduct.setPrefWidth(200);
        TableColumn<OrderItem, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("orderItemQuantity"));
        colQty.setPrefWidth(60);
        TableColumn<OrderItem, Double> colLineTotal = new TableColumn<>("Line Total");
        colLineTotal.setCellValueFactory(new PropertyValueFactory<>("orderItemLineTotal"));
        colLineTotal.setPrefWidth(100);
        colLineTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });
        tblItems.getColumns().addAll(colProduct, colQty, colLineTotal);
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ComboBox<String> cboFilter = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.FILTER_STATUSES));
        cboFilter.setValue("All");
        FilteredList<Order> filteredOrders = new FilteredList<>(FXCollections.observableArrayList(orders), o -> true);
        cboFilter.valueProperty().addListener((obs, old, val) ->
                filteredOrders.setPredicate(o -> val == null || "All".equals(val) || val.equalsIgnoreCase(o.getOrderStatus())));
        SortedList<Order> sortedOrders = new SortedList<>(filteredOrders);
        sortedOrders.comparatorProperty().bind(tblOrders.comparatorProperty());
        tblOrders.setItems(sortedOrders);

        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { tblItems.getItems().clear(); return; }
            try {
                List<OrderItem> items = OrderApi.getOrderItems(sel.getOrderId());
                tblItems.setItems(FXCollections.observableArrayList(items));
                lblItems.setText("Order " + sel.getOrderId() + " — " + items.size() + " item(s)");
            } catch (Exception e) {
                LogData.handleException("LOAD_ORDER_ITEMS", e);
                tblItems.getItems().clear();
            }
        });

        HBox filterBar = new HBox(8, new Label("Filter by status:"), cboFilter);
        VBox content = new VBox(10, filterBar, tblOrders, lblItems, tblItems);
        content.setPadding(new Insets(10));
        VBox.setVgrow(tblOrders, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(720, 560);
        dialog.showAndWait();
    }

    // ────────────────────────────────────────────────────────────
    // Adjust Points Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onAdjustPoints() {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) { ErrorHandler.showWarning("Adjust Points", "Select a customer row first."); return; }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Adjust Loyalty Points");
        dialog.setHeaderText("Customer: " + selected.getFirstName() + " " + selected.getLastName()
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
                java.util.Map<String, Object> patchBody = new LinkedHashMap<>(CustomerApi.patchRewardBalance(newBalance));
                Integer newTierId = findTierForBalance(newBalance);
                if (newTierId != null) {
                    patchBody.put("rewardTierId", newTierId);
                }
                CustomerApi.patch(selected.getCustomerId(), patchBody);
                LogData.logAction("ADJUST_POINTS", "Customer " + selected.getCustomerId()
                        + " adjusted by " + adjustment + " -> " + newBalance);
                refreshTable();
                selectCustomerById(selected.getCustomerId());
                lblStatus.setText("Adjusted points for " + selected.getFirstName() + " by " + adjustment + " -> " + newBalance);
            } catch (Exception e) {
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

    private void selectCustomerById(String id) {
        for (Customer c : master) {
            if (id.equals(c.getCustomerId())) {
                tblCustomers.getSelectionModel().select(c);
                tblCustomers.scrollTo(c);
                return;
            }
        }
    }

    private Integer findTierForBalance(int balance) {
        for (RewardTierApi.RewardTierJson tier : tierData) {
            if (tier.id == null) continue;
            boolean meetsMin = balance >= tier.minPoints;
            boolean meetsMax = tier.maxPoints == null || balance <= tier.maxPoints;
            if (meetsMin && meetsMax) return tier.id;
        }
        return null;
    }
}
