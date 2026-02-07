package com.sait.workshop05.controllers;

import com.sait.workshop05.database.*;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.*;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.OrderStatus;
import com.sait.workshop05.util.StringUtil;
import com.sait.workshop05.util.ValidationResult;
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

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CustomerManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, Integer> colCustomerId;
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

    private final CustomerDAO dao = new CustomerDAO();
    private final OrderDAO orderDao = new OrderDAO();
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
        setupSelectionBinding();
        setupSearchFiltering();
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

            selectAddressById(selected.getAddressId());
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
            List<RewardTierOption> tiers = dao.getRewardTierOptions();
            cboRewardTier.setItems(FXCollections.observableArrayList(tiers));

            List<UserOption> users = dao.getUserOptions();
            cboUser.setItems(FXCollections.observableArrayList(users));

            List<AddressOption> addresses = dao.getAddressOptions();
            cboAddress.setItems(FXCollections.observableArrayList(addresses));
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
        cboAddress.getSelectionModel().clearSelection();
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
        ValidationResult vr = validateForm(false);
        if (!vr.isOk()) {
            LogData.logAction("VALIDATION_FAILED", "Customer");
            ErrorHandler.showWarning("Validation", vr.getMessage());
            return;
        }

        Customer c = buildFromForm(false);

        try {
            int newId = dao.insertCustomer(c);
            LogData.logAction("CREATE", "Customer");
            refreshTable();

            if (newId > 0) {
                selectCustomerById(newId);
                lblStatus.setText("Created customer #" + newId);
            } else {
                lblStatus.setText("Created customer");
            }
        } catch (SQLException ex) {
            LogData.handleException("CREATE_CUSTOMER", ex);
            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Create Failed", "Could not create customer.", friendly);
        }
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

        Customer c = buildFromForm(true);

        try {
            boolean ok = dao.updateCustomer(c);
            LogData.logAction("UPDATE", "Customer");
            refreshTable();
            selectCustomerById(c.getCustomerId());
            lblStatus.setText(ok ? "Updated customer #" + c.getCustomerId() : "No update applied");
        } catch (SQLException ex) {
            LogData.handleException("UPDATE_CUSTOMER", ex);
            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Update Failed", "Could not update customer.", friendly);
        }
    }

    @FXML
    private void onDelete() {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Delete", "Select a customer row to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete customer #" + selected.getCustomerId()
                + " (" + selected.getFullName() + ")?");
        confirm.setContentText("This cannot be undone. Orders linked to this customer may also be affected.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            dao.deleteCustomer(selected.getCustomerId());
            LogData.logAction("DELETE", "Customer");
            refreshTable();
            onClear();
            lblStatus.setText("Deleted customer #" + selected.getCustomerId());
        } catch (SQLException ex) {
            LogData.handleException("DELETE_CUSTOMER", ex);
            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete customer.", friendly);
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

        // ── Orders table ───────────────────────────────────────
        TableView<Order> tblOrders = new TableView<>();
        tblOrders.setPrefHeight(250);
        tblOrders.setStyle("-fx-font-size: 13px;");

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
                List<OrderItem> items = orderDao.getOrderItems(selOrder.getOrderId());
                tblItems.setItems(FXCollections.observableArrayList(items));
                lblItems.setText("Order #" + selOrder.getOrderId() + " \u2014 "
                        + items.size() + " item(s)");
            } catch (SQLException e) {
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
                dao.updateRewardBalance(selected.getCustomerId(), newBalance);
                LogData.logAction("ADJUST_POINTS",
                        "Customer #" + selected.getCustomerId()
                                + " adjusted by " + adjustment
                                + " (new balance: " + newBalance + ")");
                refreshTable();
                selectCustomerById(selected.getCustomerId());
                lblStatus.setText("Adjusted points for " + selected.getFullName()
                        + " by " + adjustment + " -> " + newBalance);
            } catch (SQLException e) {
                LogData.handleException("ADJUST_POINTS", e);
                ErrorHandler.showErrorDialog("Error", "Could not update reward balance.", e.getMessage());
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private Customer buildFromForm(boolean includeId) {
        Customer c = new Customer();

        if (includeId) {
            c.setCustomerId(Integer.parseInt(txtCustomerId.getText().trim()));
        }

        c.setFirstName(txtFirstName.getText().trim());
        c.setMiddleInitial(StringUtil.trimToNull(txtMiddleInitial.getText()));
        c.setLastName(txtLastName.getText().trim());
        c.setRole(cboRole.getValue());
        c.setPhone(txtPhone.getText().trim());
        c.setBusinessPhone(StringUtil.trimToNull(txtBusinessPhone.getText()));
        c.setEmail(txtEmail.getText().trim());

        AddressOption addr = cboAddress.getValue();
        c.setAddressId(addr.getAddressId());

        UserOption user = cboUser.getValue();
        c.setUserId(user != null ? user.getUserId() : 0);

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
        RewardTierOption tier = cboRewardTier.getValue();

        if (isUpdate) {
            String id = StringUtil.safe(txtCustomerId.getText());
            if (id.isBlank()) return ValidationResult.fail("Customer ID is missing (select a row first).");
            try {
                Integer.parseInt(id);
            } catch (NumberFormatException ex) {
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

        if (addr == null) return ValidationResult.fail("Address is required.");
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

    private void selectCustomerById(int id) {
        for (Customer c : master) {
            if (c.getCustomerId() == id) {
                tblCustomers.getSelectionModel().select(c);
                tblCustomers.scrollTo(c);
                return;
            }
        }
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

    private void selectUserById(int userId) {
        if (cboUser.getItems() == null) return;
        if (userId <= 0) {
            cboUser.getSelectionModel().clearSelection();
            return;
        }
        for (UserOption u : cboUser.getItems()) {
            if (u.getUserId() == userId) {
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
