package com.sait.workshop05.controllers;

import com.sait.workshop05.api.CustomerApi;
import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.api.RewardTierApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.*;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.OrderStatus;
import com.sait.workshop05.util.StringUtil;
import com.sait.workshop05.util.ValidationResult;
import com.sait.workshop05.util.AddressInputHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CustomerManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, String> colCustomerId;
    @FXML private TableColumn<Customer, String> colFirstName;
    @FXML private TableColumn<Customer, String> colMiddleInitial;
    @FXML private TableColumn<Customer, String> colLastName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colRewardTier;
    @FXML private TableColumn<Customer, Integer> colRewardBalance;
    @FXML private TableColumn<Customer, String> colAddress;

    // ── Search & status ────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;

    // ── Form fields ────────────────────────────────────────────
    @FXML private TextField txtCustomerId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtMiddleInitial;
    @FXML private TextField txtLastName;
    @FXML private ComboBox<String> cboRole;
    @FXML private TextField txtPhone;
    @FXML private TextField txtBusinessPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<AddressOption> cboAddress;
    @FXML private ComboBox<UserOption> cboUser;
    @FXML private ComboBox<RewardTierOption> cboRewardTier;
    @FXML private TextField txtRewardBalance;

    // ── CRUD + extra buttons ───────────────────────────────────
    @FXML private Button btnCreate;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnOrderHistory;
    @FXML private Button btnAdjustPoints;

    private final ObservableList<Customer> master = FXCollections.observableArrayList();
    private FilteredList<Customer> filtered;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupRoleOptions();
        AddressInputHelper.configureEditableAddressCombo(cboAddress);
        setupSelectionBinding();
        setupSearchFiltering();
        if (btnCreate != null) {
            btnCreate.setDisable(true);
            btnCreate.setTooltip(new Tooltip("Customers are created via customer registration (API)."));
        }
        if (btnDelete != null) {
            btnDelete.setDisable(true);
            btnDelete.setTooltip(new Tooltip("Deleting customers is not exposed on the API."));
        }
        loadCombos();
        refreshTable();
    }

    private void setupColumns() {
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleInitial.setCellValueFactory(new PropertyValueFactory<>("middleInitial"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRewardTier.setCellValueFactory(new PropertyValueFactory<>("rewardTierDisplay"));
        colRewardBalance.setCellValueFactory(new PropertyValueFactory<>("rewardBalance"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("addressDisplay"));

        tblCustomers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupRoleOptions() {
        cboRole.setItems(FXCollections.observableArrayList(
                "Regular",
                "VIP",
                "Wholesale",
                "Corporate"
        ));
    }

    private void setupSelectionBinding() {
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) return;

            txtCustomerId.setText(String.valueOf(selected.getCustomerId()));
            txtFirstName.setText(StringUtil.nz(selected.getFirstName()));
            txtMiddleInitial.setText(StringUtil.nz(selected.getMiddleInitial()));
            txtLastName.setText(StringUtil.nz(selected.getLastName()));
            cboRole.setValue(selected.getRole());
            txtPhone.setText(StringUtil.nz(selected.getPhone()));
            txtBusinessPhone.setText(StringUtil.nz(selected.getBusinessPhone()));
            txtEmail.setText(StringUtil.nz(selected.getEmail()));
            txtRewardBalance.setText(String.valueOf(selected.getRewardBalance()));

            AddressInputHelper.selectAddressById(cboAddress, selected.getAddressId());
            selectUserById(selected.getUserId());
            selectRewardTierById(selected.getRewardTierId());
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
            List<RewardTierOption> tiers = FXCollections.observableArrayList();
            for (RewardTierApi.RewardTierJson t : RewardTierApi.list()) {
                if (t.id != null && t.name != null) {
                    tiers.add(new RewardTierOption(t.id, t.name));
                }
            }
            cboRewardTier.setItems(FXCollections.observableArrayList(tiers));

            cboUser.setItems(FXCollections.observableArrayList(ReferenceApi.loadAdminUsers()));

            List<AddressOption> addresses = ReferenceApi.loadAddresses();
            AddressInputHelper.setAddressItems(cboAddress, addresses);
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
            java.util.Map<Integer, String> tierNames = new java.util.HashMap<>();
            for (RewardTierApi.RewardTierJson t : RewardTierApi.list()) {
                if (t.id != null) {
                    tierNames.put(t.id, t.name != null ? t.name : "");
                }
            }
            java.util.Map<Integer, String> addrDisp = new java.util.HashMap<>();
            for (AddressOption a : ReferenceApi.loadAddresses()) {
                addrDisp.put(a.getAddressId(), a.getLine1() + ", " + a.getCity());
            }

            master.clear();
            for (CustomerApi.CustomerRow row : CustomerApi.list()) {
                master.add(fromCustomerRow(row, tierNames, addrDisp));
            }
            lblStatus.setText(master.size() + " customer(s) loaded");
            LogData.logAction("READ", "Customer");
        } catch (Exception e) {
            LogData.handleException("READ_CUSTOMERS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load customers.", e.getMessage());
        }
    }

    private Customer fromCustomerRow(CustomerApi.CustomerRow row,
                                     java.util.Map<Integer, String> tierNames,
                                     java.util.Map<Integer, String> addrDisp) {
        Customer c = new Customer();
        c.setCustomerId(row.id != null ? row.id : "");
        c.setUserId(row.userId != null ? row.userId : "");
        c.setAddressId(row.addressId != null ? row.addressId : 0);
        c.setRewardTierId(row.rewardTierId != null ? row.rewardTierId : 0);
        c.setFirstName(row.firstName != null ? row.firstName : "");
        c.setMiddleInitial(row.middleInitial);
        c.setLastName(row.lastName != null ? row.lastName : "");
        c.setRole("Regular");
        c.setPhone(row.phone != null ? row.phone : "");
        c.setBusinessPhone("");
        c.setEmail(row.email != null ? row.email : "");
        c.setRewardBalance(row.rewardBalance);
        c.setTierAssignedDate(LocalDateTime.now());
        c.setUserDisplay(row.userId != null ? row.userId : "");
        String ad = row.addressId != null ? addrDisp.get(row.addressId) : null;
        c.setAddressDisplay(ad != null ? ad : "");
        String tn = row.rewardTierId != null ? tierNames.get(row.rewardTierId) : null;
        c.setRewardTierDisplay(tn != null ? tn : "");
        return c;
    }

    @FXML
    private void onRefresh() {
        loadCombos();
        refreshTable();
    }

    @FXML
    private void onClear() {
        tblCustomers.getSelectionModel().clearSelection();
        txtCustomerId.clear();
        txtFirstName.clear();
        txtMiddleInitial.clear();
        txtLastName.clear();
        cboRole.setValue(null);
        txtPhone.clear();
        txtBusinessPhone.clear();
        txtEmail.clear();
        AddressInputHelper.clearAddressField(cboAddress);
        cboUser.getSelectionModel().clearSelection();
        cboRewardTier.getSelectionModel().clearSelection();
        txtRewardBalance.clear();
        lblStatus.setText("Cleared");
    }

    // ────────────────────────────────────────────────────────────
    // CRUD operations
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onCreate() {
        ErrorHandler.showInfo("Not available", "New customers are created through the public registration flow (API), not this screen.");
    }

    @FXML
    private void onUpdate() {
        if (txtCustomerId.getText() == null || txtCustomerId.getText().trim().isEmpty()) {
            ErrorHandler.showWarning("Update", "Select a customer row to update.");
            return;
        }

        ValidationResult vr = validateForm(true);
        if (!vr.isOk()) {
            LogData.logAction("VALIDATION_FAILED", "Customer");
            ErrorHandler.showWarning("Validation", vr.getMessage());
            return;
        }

        try {
            Customer c = buildFromForm(true);
            java.util.Map<String, Object> body = CustomerApi.patchBodyForProfile(
                    c.getFirstName(),
                    c.getMiddleInitial(),
                    c.getLastName(),
                    c.getPhone(),
                    c.getBusinessPhone(),
                    c.getEmail(),
                    c.getAddressId() > 0 ? c.getAddressId() : null,
                    c.getRewardTierId() > 0 ? c.getRewardTierId() : null
            );
            CustomerApi.patch(c.getCustomerId(), body);
            LogData.logAction("UPDATE", "Customer");
            refreshTable();
            selectCustomerById(c.getCustomerId());
            lblStatus.setText("Updated customer " + c.getCustomerId());
        } catch (IllegalArgumentException ex) {
            ErrorHandler.showWarning("Validation", ex.getMessage());
        } catch (Exception ex) {
            LogData.handleException("UPDATE_CUSTOMER", ex);
            ErrorHandler.showErrorDialog("Update Failed", "Could not update customer.", ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        ErrorHandler.showInfo("Not available", "Deleting customers is not supported through the API from this app.");
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
        dialog.setTitle("Order History \u2014 " + customer.getFullName());
        dialog.setHeaderText(orders.size() + " order(s) found for " + customer.getFullName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);

        // ── Orders table ───────────────────────────────────────
        TableView<Order> tblOrders = new TableView<>();
        tblOrders.setPrefHeight(250);
        tblOrders.setStyle("-fx-font-size: 13px;");

        TableColumn<Order, String> colOrdId = new TableColumn<>("Order ID");
        colOrdId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrdId.setPrefWidth(70);

        TableColumn<Order, String> colOrdDate = new TableColumn<>("Placed");
        colOrdDate.setCellValueFactory(new PropertyValueFactory<>("orderPlacedDateTime"));
        colOrdDate.setPrefWidth(140);
        colOrdDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    Order o = getTableView().getItems().get(getIndex());
                    setText(o.getOrderPlacedDateTime() != null
                            ? o.getOrderPlacedDateTime().format(DT_FMT) : "");
                }
            }
        });

        TableColumn<Order, String> colOrdScheduled = new TableColumn<>("Scheduled");
        colOrdScheduled.setCellValueFactory(new PropertyValueFactory<>("orderScheduledDateTime"));
        colOrdScheduled.setPrefWidth(140);
        colOrdScheduled.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    Order o = getTableView().getItems().get(getIndex());
                    setText(o.getOrderScheduledDateTime() != null
                            ? o.getOrderScheduledDateTime().format(DT_FMT) : "\u2014");
                }
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

        TableColumn<Order, Double> colOrdDiscount = new TableColumn<>("Discount");
        colOrdDiscount.setCellValueFactory(new PropertyValueFactory<>("orderDiscount"));
        colOrdDiscount.setPrefWidth(80);
        colOrdDiscount.setCellFactory(col -> new TableCell<>() {
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

        tblOrders.getColumns().addAll(colOrdId, colOrdDate, colOrdScheduled,
                colOrdMethod, colOrdTotal, colOrdDiscount, colOrdStatus, colOrdBakery);
        tblOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblOrders.setItems(FXCollections.observableArrayList(orders));

        // ── Order Items table (bottom) ─────────────────────────
        Label lblItems = new Label("Order Items (select an order above)");
        lblItems.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<OrderItem> tblItems = new TableView<>();
        tblItems.setPrefHeight(180);
        tblItems.setStyle("-fx-font-size: 13px;");

        TableColumn<OrderItem, String> colItemProduct = new TableColumn<>("Product");
        colItemProduct.setCellValueFactory(new PropertyValueFactory<>("productDisplay"));
        colItemProduct.setPrefWidth(200);

        TableColumn<OrderItem, Integer> colItemQty = new TableColumn<>("Qty");
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("orderItemQuantity"));
        colItemQty.setPrefWidth(60);

        TableColumn<OrderItem, Double> colItemPrice = new TableColumn<>("Unit Price");
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("orderItemUnitPriceAtTime"));
        colItemPrice.setPrefWidth(100);
        colItemPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

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

        tblItems.getColumns().addAll(colItemProduct, colItemQty, colItemPrice, colItemTotal);
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Wire: select order -> load items
        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selOrder) -> {
            if (selOrder == null) {
                tblItems.getItems().clear();
                return;
            }
            try {
                List<OrderItem> items = OrderApi.getOrderItems(selOrder.getOrderId());
                tblItems.setItems(FXCollections.observableArrayList(items));
                lblItems.setText("Order #" + selOrder.getOrderId() + " \u2014 "
                        + items.size() + " item(s)");
            } catch (Exception e) {
                LogData.handleException("LOAD_ORDER_ITEMS", e);
                tblItems.getItems().clear();
            }
        });

        // ── Status filter ──────────────────────────────────────
        ComboBox<String> cboStatusFilter = new ComboBox<>(FXCollections.observableArrayList(
                OrderStatus.FILTER_STATUSES
        ));
        cboStatusFilter.setValue("All");
        cboStatusFilter.setStyle("-fx-font-size: 13px;");

        FilteredList<Order> filteredOrders = new FilteredList<>(
                FXCollections.observableArrayList(orders), o -> true);

        cboStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredOrders.setPredicate(o -> {
                if (newVal == null || "All".equals(newVal)) return true;
                return newVal.equalsIgnoreCase(o.getOrderStatus());
            });
        });

        SortedList<Order> sortedOrders = new SortedList<>(filteredOrders);
        sortedOrders.comparatorProperty().bind(tblOrders.comparatorProperty());
        tblOrders.setItems(sortedOrders);

        // ── Layout ─────────────────────────────────────────────
        Label lblFilter = new Label("Filter by status:");
        lblFilter.setStyle("-fx-font-size: 13px;");

        VBox content = new VBox(10,
                new javafx.scene.layout.HBox(8, lblFilter, cboStatusFilter),
                tblOrders,
                lblItems,
                tblItems
        );
        content.setPadding(new Insets(10));
        VBox.setVgrow(tblOrders, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(850, 600);
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

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtAdjust = new TextField("0");
        txtAdjust.setPromptText("Enter amount (positive to add, negative to subtract)");

        grid.add(new Label("Adjustment:"), 0, 0);
        grid.add(txtAdjust, 1, 0);

        Label lblPreview = new Label("New Balance: " + selected.getRewardBalance());
        grid.add(lblPreview, 1, 1);

        txtAdjust.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int adj = Integer.parseInt(newVal.trim());
                int newBalance = selected.getRewardBalance() + adj;
                lblPreview.setText("New Balance: " + newBalance + (newBalance < 0 ? " (invalid!)" : ""));
            } catch (NumberFormatException e) {
                lblPreview.setText("New Balance: (invalid input)");
            }
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    return Integer.parseInt(txtAdjust.getText().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();

        result.ifPresent(adjustment -> {
            int newBalance = selected.getRewardBalance() + adjustment;
            if (newBalance < 0) {
                ErrorHandler.showWarning("Invalid", "Reward balance cannot go below 0.");
                return;
            }

            try {
                CustomerApi.patch(selected.getCustomerId(), CustomerApi.patchRewardBalance(newBalance));
                LogData.logAction("ADJUST_POINTS",
                        "Customer " + selected.getCustomerId()
                                + " adjusted by " + adjustment
                                + " (new balance: " + newBalance + ")");
                refreshTable();
                selectCustomerById(selected.getCustomerId());
                lblStatus.setText("Adjusted points for " + selected.getFullName()
                        + " by " + adjustment + " -> " + newBalance);
            } catch (Exception e) {
                LogData.handleException("ADJUST_POINTS", e);
                ErrorHandler.showErrorDialog("Error", "Could not update reward balance.", e.getMessage());
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private Customer buildFromForm(boolean includeId) throws Exception {
        Customer c = new Customer();

        if (includeId) {
            c.setCustomerId(txtCustomerId.getText().trim());
        }

        c.setFirstName(txtFirstName.getText().trim());
        c.setMiddleInitial(StringUtil.trimToNull(txtMiddleInitial.getText()));
        c.setLastName(txtLastName.getText().trim());
        c.setRole(cboRole.getValue());
        c.setPhone(txtPhone.getText().trim());
        c.setBusinessPhone(StringUtil.trimToNull(txtBusinessPhone.getText()));
        c.setEmail(txtEmail.getText().trim());

        AddressOption addr = resolveAddressSelection(true);
        c.setAddressId(addr.getAddressId());

        UserOption user = cboUser.getValue();
        c.setUserId(user != null ? user.getUserId() : "");

        RewardTierOption tier = cboRewardTier.getValue();
        c.setRewardTierId(tier.getRewardTierId());

        String balStr = StringUtil.safe(txtRewardBalance.getText());
        c.setRewardBalance(balStr.isEmpty() ? 0 : Integer.parseInt(balStr));

        c.setTierAssignedDate(LocalDateTime.now());

        return c;
    }

    private ValidationResult validateForm(boolean isUpdate) {
        String first = StringUtil.safe(txtFirstName.getText());
        String last = StringUtil.safe(txtLastName.getText());
        String phone = StringUtil.safe(txtPhone.getText());
        String email = StringUtil.safe(txtEmail.getText());
        String mi = StringUtil.safe(txtMiddleInitial.getText());
        AddressOption addr = cboAddress.getValue();
        String typedAddress = AddressInputHelper.getTypedText(cboAddress);
        RewardTierOption tier = cboRewardTier.getValue();

        if (isUpdate) {
            String id = StringUtil.safe(txtCustomerId.getText());
            if (id.isBlank()) return ValidationResult.fail("Customer ID is missing (select a row first).");
            try {
                java.util.UUID.fromString(id);
            } catch (IllegalArgumentException ex) {
                return ValidationResult.fail("Customer ID is invalid.");
            }
        }

        if (first.isBlank()) return ValidationResult.fail("First name is required.");
        if (last.isBlank()) return ValidationResult.fail("Last name is required.");

        if (phone.isBlank()) return ValidationResult.fail("Phone is required.");
        if (!StringUtil.PHONE_RX.matcher(phone).matches()) return ValidationResult.fail("Phone format looks invalid.");

        if (email.isBlank()) return ValidationResult.fail("Email is required.");
        if (!StringUtil.EMAIL_RX.matcher(email).matches()) return ValidationResult.fail("Email format looks invalid.");

        if (!mi.isBlank() && mi.trim().length() > 2) return ValidationResult.fail("Middle initial must be 1-2 characters.");

        if (addr == null && typedAddress.isBlank()) return ValidationResult.fail("Address is required.");
        if (tier == null) return ValidationResult.fail("Reward tier is required.");

        // Length limits matching SQL columns
        if (first.length() > 50) return ValidationResult.fail("First name must be 50 characters or less.");
        if (last.length() > 50) return ValidationResult.fail("Last name must be 50 characters or less.");
        if (phone.length() > 20) return ValidationResult.fail("Phone must be 20 characters or less.");
        String biz = StringUtil.safe(txtBusinessPhone.getText());
        if (!biz.isBlank() && biz.length() > 20) return ValidationResult.fail("Business phone must be 20 characters or less.");
        if (email.length() > 254) return ValidationResult.fail("Email must be 254 characters or less.");

        // Reward balance validation
        String balStr = StringUtil.safe(txtRewardBalance.getText());
        if (!balStr.isEmpty()) {
            try {
                int bal = Integer.parseInt(balStr);
                if (bal < 0) return ValidationResult.fail("Reward balance cannot be negative.");
            } catch (NumberFormatException e) {
                return ValidationResult.fail("Reward balance must be a whole number.");
            }
        }

        return ValidationResult.ok();
    }

    private void selectCustomerById(String id) {
        for (Customer c : master) {
            if (c.getCustomerId().equals(id)) {
                tblCustomers.getSelectionModel().select(c);
                tblCustomers.scrollTo(c);
                return;
            }
        }
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

    private void selectRewardTierById(int tierId) {
        if (cboRewardTier.getItems() == null) return;
        for (RewardTierOption t : cboRewardTier.getItems()) {
            if (t.getRewardTierId() == tierId) {
                cboRewardTier.getSelectionModel().select(t);
                return;
            }
        }
        cboRewardTier.getSelectionModel().clearSelection();
    }
}
