package com.sait.workshop05.controllers;

import com.sait.workshop05.database.*;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderItem;
import com.sait.workshop05.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OrderManagementController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final List<String> STATUS_FLOW = Arrays.asList(
            "Pending", "Processing", "Ready", "Out for Delivery", "Completed", "Cancelled"
    );

    // ════════════════════════════════════════════════════════════
    // TAB 1: All Orders — FXML bindings
    // ════════════════════════════════════════════════════════════

    @FXML private TabPane tabPane;

    @FXML private TableView<Order> tblOrders;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, String> colOrderCustomer;
    @FXML private TableColumn<Order, String> colOrderBakery;
    @FXML private TableColumn<Order, LocalDateTime> colOrderPlaced;
    @FXML private TableColumn<Order, LocalDateTime> colOrderScheduled;
    @FXML private TableColumn<Order, String> colOrderMethod;
    @FXML private TableColumn<Order, Double> colOrderTotal;
    @FXML private TableColumn<Order, Double> colOrderDiscount;
    @FXML private TableColumn<Order, String> colOrderStatus;

    @FXML private TextField txtOrderSearch;
    @FXML private ComboBox<String> cboStatusFilter;
    @FXML private Label lblOrderStatus;
    @FXML private Button btnRefreshOrders;

    // Order detail / items
    @FXML private Label lblOrderDetailTitle;
    @FXML private TableView<OrderItem> tblOrderItems;
    @FXML private TableColumn<OrderItem, String> colItemProduct;
    @FXML private TableColumn<OrderItem, Integer> colItemQty;
    @FXML private TableColumn<OrderItem, Double> colItemUnitPrice;
    @FXML private TableColumn<OrderItem, Double> colItemLineTotal;

    @FXML private ComboBox<String> cboNewStatus;
    @FXML private Button btnUpdateStatus;

    // ════════════════════════════════════════════════════════════
    // TAB 2: New Order — FXML bindings
    // ════════════════════════════════════════════════════════════

    @FXML private ComboBox<CustomerOption> cboNewCustomer;
    @FXML private ComboBox<BakeryOption> cboNewBakery;
    @FXML private ComboBox<String> cboNewMethod;
    @FXML private DatePicker dpScheduledDate;
    @FXML private TextField txtScheduledTime;
    @FXML private ComboBox<AddressOption> cboNewAddress;
    @FXML private TextField txtNewComment;

    // Product catalog
    @FXML private TextField txtProductSearch;
    @FXML private TableView<Product> tblCatalog;
    @FXML private TableColumn<Product, String> colCatName;
    @FXML private TableColumn<Product, String> colCatDesc;
    @FXML private TableColumn<Product, Double> colCatPrice;
    @FXML private Spinner<Integer> spnQuantity;
    @FXML private Button btnAddToOrder;

    // Cart
    @FXML private TableView<OrderItem> tblCart;
    @FXML private TableColumn<OrderItem, String> colCartProduct;
    @FXML private TableColumn<OrderItem, Integer> colCartQty;
    @FXML private TableColumn<OrderItem, Double> colCartUnitPrice;
    @FXML private TableColumn<OrderItem, Double> colCartLineTotal;
    @FXML private Button btnRemoveFromOrder;

    // Summary
    @FXML private Label lblSubtotal;
    @FXML private TextField txtDiscount;
    @FXML private Label lblRewardInfo;
    @FXML private Label lblTotal;
    @FXML private Button btnPlaceOrder;
    @FXML private Label lblNewOrderStatus;

    // ════════════════════════════════════════════════════════════
    // State
    // ════════════════════════════════════════════════════════════

    private final OrderDAO orderDao = new OrderDAO();
    private final ProductDAO productDao = new ProductDAO();

    // Tab 1
    private final ObservableList<Order> orderMaster = FXCollections.observableArrayList();
    private FilteredList<Order> orderFiltered;

    // Tab 2 catalog
    private final ObservableList<Product> catalogMaster = FXCollections.observableArrayList();
    private FilteredList<Product> catalogFiltered;

    // Tab 2 cart
    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();

    // ════════════════════════════════════════════════════════════
    // Initialization
    // ════════════════════════════════════════════════════════════

    @FXML
    void initialize() {
        setupTab1();
        setupTab2();
        loadOrderData();
    }

    // ────────────────────────────────────────────────────────────
    // TAB 1 Setup
    // ────────────────────────────────────────────────────────────

    private void setupTab1() {
        // Column bindings
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrderCustomer.setCellValueFactory(new PropertyValueFactory<>("customerDisplay"));
        colOrderBakery.setCellValueFactory(new PropertyValueFactory<>("bakeryDisplay"));
        colOrderMethod.setCellValueFactory(new PropertyValueFactory<>("orderMethod"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

        // Date columns with custom formatting
        colOrderPlaced.setCellValueFactory(new PropertyValueFactory<>("orderPlacedDateTime"));
        colOrderPlaced.setCellFactory(col -> new DateTimeCell<>());
        colOrderScheduled.setCellValueFactory(new PropertyValueFactory<>("orderScheduledDateTime"));
        colOrderScheduled.setCellFactory(col -> new DateTimeCell<>());

        // Currency columns
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("orderTotal"));
        colOrderTotal.setCellFactory(col -> new CurrencyCell<>());
        colOrderDiscount.setCellValueFactory(new PropertyValueFactory<>("orderDiscount"));
        colOrderDiscount.setCellFactory(col -> new CurrencyCell<>());

        tblOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Order Items columns
        colItemProduct.setCellValueFactory(new PropertyValueFactory<>("productDisplay"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("orderItemQuantity"));
        colItemUnitPrice.setCellValueFactory(new PropertyValueFactory<>("orderItemUnitPriceAtTime"));
        colItemUnitPrice.setCellFactory(col -> new CurrencyCell<>());
        colItemLineTotal.setCellValueFactory(new PropertyValueFactory<>("orderItemLineTotal"));
        colItemLineTotal.setCellFactory(col -> new CurrencyCell<>());
        tblOrderItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Status filter ComboBox
        ObservableList<String> statusOptions = FXCollections.observableArrayList("All");
        statusOptions.addAll(STATUS_FLOW);
        cboStatusFilter.setItems(statusOptions);
        cboStatusFilter.setValue("All");

        // Status update ComboBox
        cboNewStatus.setItems(FXCollections.observableArrayList(STATUS_FLOW));

        // Filtering
        orderFiltered = new FilteredList<>(orderMaster, o -> true);

        txtOrderSearch.textProperty().addListener((obs, o, n) -> applyOrderFilter());
        cboStatusFilter.valueProperty().addListener((obs, o, n) -> applyOrderFilter());

        SortedList<Order> sorted = new SortedList<>(orderFiltered);
        sorted.comparatorProperty().bind(tblOrders.comparatorProperty());
        tblOrders.setItems(sorted);

        // Selection binding -> load items
        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                tblOrderItems.getItems().clear();
                lblOrderDetailTitle.setText("Select an order above to view details");
                cboNewStatus.setValue(null);
                return;
            }

            lblOrderDetailTitle.setText("Order #" + selected.getOrderId()
                    + " — " + selected.getCustomerDisplay());
            cboNewStatus.setValue(selected.getOrderStatus());

            try {
                List<OrderItem> items = orderDao.getOrderItems(selected.getOrderId());
                tblOrderItems.setItems(FXCollections.observableArrayList(items));
            } catch (SQLException e) {
                LogData.handleException("LOAD_ORDER_ITEMS", e);
                tblOrderItems.getItems().clear();
            }
        });
    }

    private void applyOrderFilter() {
        String q = txtOrderSearch.getText() == null ? "" : txtOrderSearch.getText().trim().toLowerCase();
        String statusVal = cboStatusFilter.getValue();

        orderFiltered.setPredicate(order -> {
            boolean matchesStatus = statusVal == null || "All".equals(statusVal)
                    || statusVal.equalsIgnoreCase(order.getOrderStatus());

            if (!matchesStatus) return false;
            if (q.isEmpty()) return true;

            return contains(order.getCustomerDisplay(), q)
                    || contains(order.getBakeryDisplay(), q)
                    || contains(order.getOrderMethod(), q)
                    || contains(order.getOrderStatus(), q)
                    || contains(order.getAddressDisplay(), q)
                    || String.valueOf(order.getOrderId()).contains(q)
                    || String.format("%.2f", order.getOrderTotal()).contains(q);
        });

        lblOrderStatus.setText(orderFiltered.size() + " order(s) shown");
    }

    // ────────────────────────────────────────────────────────────
    // TAB 2 Setup
    // ────────────────────────────────────────────────────────────

    private void setupTab2() {
        // Method ComboBox
        cboNewMethod.setItems(FXCollections.observableArrayList("Pickup", "Delivery", "Dine-in"));

        // Spinner for quantity (1-99)
        spnQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        spnQuantity.setEditable(true);

        // Catalog columns
        colCatName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCatDesc.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        colCatPrice.setCellValueFactory(new PropertyValueFactory<>("productBasePrice"));
        colCatPrice.setCellFactory(col -> new CurrencyCell<>());
        tblCatalog.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cart columns
        colCartProduct.setCellValueFactory(new PropertyValueFactory<>("productDisplay"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("orderItemQuantity"));
        colCartUnitPrice.setCellValueFactory(new PropertyValueFactory<>("orderItemUnitPriceAtTime"));
        colCartUnitPrice.setCellFactory(col -> new CurrencyCell<>());
        colCartLineTotal.setCellValueFactory(new PropertyValueFactory<>("orderItemLineTotal"));
        colCartLineTotal.setCellFactory(col -> new CurrencyCell<>());
        tblCart.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCart.setItems(cartItems);

        // Product search filtering
        catalogFiltered = new FilteredList<>(catalogMaster, p -> true);

        txtProductSearch.textProperty().addListener((obs, o, n) -> {
            String q = (n == null) ? "" : n.trim().toLowerCase();
            catalogFiltered.setPredicate(p -> {
                if (q.isEmpty()) return true;
                return contains(p.getProductName(), q)
                        || contains(p.getProductDescription(), q);
            });
        });

        SortedList<Product> sortedCatalog = new SortedList<>(catalogFiltered);
        sortedCatalog.comparatorProperty().bind(tblCatalog.comparatorProperty());
        tblCatalog.setItems(sortedCatalog);

        // Customer selection -> show reward balance
        cboNewCustomer.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblRewardInfo.setText("Balance: " + newVal.getRewardBalance() + " pts");
            } else {
                lblRewardInfo.setText("Select a customer");
            }
        });

        // Discount field -> recalculate total
        txtDiscount.textProperty().addListener((obs, o, n) -> recalculateTotals());

        // Load ComboBox data
        loadTab2Combos();
        loadCatalog();
    }

    private void loadTab2Combos() {
        try {
            cboNewCustomer.setItems(FXCollections.observableArrayList(orderDao.getCustomerOptions()));
            cboNewBakery.setItems(FXCollections.observableArrayList(orderDao.getBakeryOptions()));
            cboNewAddress.setItems(FXCollections.observableArrayList(orderDao.getAddressOptions()));
        } catch (SQLException e) {
            LogData.handleException("LOAD_ORDER_COMBOS", e);
            showError("Database Error", "Could not load dropdown lists.", e.getMessage());
        }
    }

    private void loadCatalog() {
        try {
            catalogMaster.clear();
            catalogMaster.addAll(productDao.getAllProducts());
        } catch (SQLException e) {
            LogData.handleException("LOAD_PRODUCT_CATALOG", e);
            showError("Database Error", "Could not load product catalog.", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    // TAB 1 Actions
    // ════════════════════════════════════════════════════════════

    private void loadOrderData() {
        try {
            orderMaster.clear();
            orderMaster.addAll(orderDao.getAllOrders());
            lblOrderStatus.setText(orderMaster.size() + " order(s) loaded");
            LogData.logAction("READ", "Order");
        } catch (SQLException e) {
            LogData.handleException("READ_ORDERS", e);
            showError("Database Error", "Could not load orders.", e.getMessage());
        }
    }

    @FXML
    private void onRefreshOrders() {
        loadOrderData();
        loadTab2Combos();
        loadCatalog();
    }

    @FXML
    private void onUpdateStatus() {
        Order selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Update Status", "Select an order first.");
            return;
        }

        String newStatus = cboNewStatus.getValue();
        if (newStatus == null || newStatus.isBlank()) {
            showWarning("Update Status", "Select a new status.");
            return;
        }

        // Validate status transition
        String currentStatus = selected.getOrderStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            showWarning("Invalid Transition",
                    "Cannot change status from '" + currentStatus + "' to '" + newStatus + "'.\n"
                    + "Status can only move forward (except Cancelled which is always allowed).");
            return;
        }

        try {
            orderDao.updateOrderStatus(selected.getOrderId(), newStatus);
            LogData.logAction("UPDATE_STATUS",
                    "Order #" + selected.getOrderId() + ": " + currentStatus + " -> " + newStatus);
            loadOrderData();
            lblOrderStatus.setText("Updated order #" + selected.getOrderId() + " to " + newStatus);
        } catch (SQLException e) {
            LogData.handleException("UPDATE_ORDER_STATUS", e);
            showError("Update Failed", "Could not update order status.", e.getMessage());
        }
    }

    private boolean isValidStatusTransition(String current, String next) {
        if (current == null || next == null) return false;
        if (current.equalsIgnoreCase(next)) return false;

        // Cancelled is always allowed (from any state)
        if ("Cancelled".equalsIgnoreCase(next)) return true;

        // Cannot transition FROM Completed or Cancelled
        if ("Completed".equalsIgnoreCase(current) || "Cancelled".equalsIgnoreCase(current)) return false;

        // Must move forward in the flow
        int currentIdx = getStatusIndex(current);
        int nextIdx = getStatusIndex(next);
        return nextIdx > currentIdx;
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < STATUS_FLOW.size(); i++) {
            if (STATUS_FLOW.get(i).equalsIgnoreCase(status)) return i;
        }
        return -1;
    }

    // ════════════════════════════════════════════════════════════
    // TAB 2 Actions
    // ════════════════════════════════════════════════════════════

    @FXML
    private void onAddToOrder() {
        Product selected = tblCatalog.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Add Item", "Select a product from the catalog first.");
            return;
        }

        int qty = spnQuantity.getValue();

        // Check if product already in cart -> increase quantity
        for (OrderItem item : cartItems) {
            if (item.getProductId() == selected.getProductId()) {
                int newQty = item.getOrderItemQuantity() + qty;
                item.setOrderItemQuantity(newQty);
                item.setOrderItemLineTotal(newQty * item.getOrderItemUnitPriceAtTime());
                tblCart.refresh();
                recalculateTotals();
                return;
            }
        }

        // New item
        OrderItem item = new OrderItem();
        item.setProductId(selected.getProductId());
        item.setProductDisplay(selected.getProductName());
        item.setOrderItemQuantity(qty);
        item.setOrderItemUnitPriceAtTime(selected.getProductBasePrice());
        item.setOrderItemLineTotal(qty * selected.getProductBasePrice());

        cartItems.add(item);
        recalculateTotals();
    }

    @FXML
    private void onRemoveFromOrder() {
        OrderItem selected = tblCart.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Remove Item", "Select an item from the cart to remove.");
            return;
        }

        cartItems.remove(selected);
        recalculateTotals();
    }

    private void recalculateTotals() {
        double subtotal = 0;
        for (OrderItem item : cartItems) {
            subtotal += item.getOrderItemLineTotal();
        }

        double discount = 0;
        try {
            discount = Double.parseDouble(txtDiscount.getText().trim());
            if (discount < 0) discount = 0;
        } catch (NumberFormatException ignored) {}

        double total = subtotal - discount;
        if (total < 0) total = 0;

        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    private void onPlaceOrder() {
        // Validate
        CustomerOption customer = cboNewCustomer.getValue();
        BakeryOption bakery = cboNewBakery.getValue();
        String method = cboNewMethod.getValue();

        if (customer == null) {
            showWarning("Validation", "Please select a customer.");
            return;
        }
        if (bakery == null) {
            showWarning("Validation", "Please select a bakery location.");
            return;
        }
        if (method == null || method.isBlank()) {
            showWarning("Validation", "Please select an order method.");
            return;
        }
        if (cartItems.isEmpty()) {
            showWarning("Validation", "Add at least one product to the order.");
            return;
        }

        // Calculate totals
        double subtotal = 0;
        for (OrderItem item : cartItems) {
            subtotal += item.getOrderItemLineTotal();
        }

        double discount = 0;
        try {
            discount = Double.parseDouble(txtDiscount.getText().trim());
            if (discount < 0) discount = 0;
        } catch (NumberFormatException ignored) {}

        // Validation: discount cannot exceed subtotal (proposal rule)
        if (discount > subtotal) {
            showWarning("Validation", "Discount ($" + String.format("%.2f", discount)
                    + ") cannot exceed subtotal ($" + String.format("%.2f", subtotal) + ").");
            return;
        }

        double total = subtotal - discount;

        // Parse scheduled date/time
        LocalDateTime scheduledDateTime = null;
        LocalDate schedDate = dpScheduledDate.getValue();
        String timeStr = txtScheduledTime.getText() != null ? txtScheduledTime.getText().trim() : "";

        if (schedDate != null) {
            LocalTime schedTime = LocalTime.of(12, 0); // default noon
            if (!timeStr.isEmpty()) {
                try {
                    schedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                } catch (DateTimeParseException e) {
                    showWarning("Validation", "Scheduled time format must be HH:mm (e.g., 14:30).");
                    return;
                }
            }
            scheduledDateTime = LocalDateTime.of(schedDate, schedTime);

            // Validation: scheduled date must be after now (proposal rule)
            if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                showWarning("Validation", "Scheduled date/time must be in the future.");
                return;
            }
        }

        // Delivery address
        AddressOption address = cboNewAddress.getValue();

        // Build Order
        Order order = new Order();
        order.setCustomerId(customer.getCustomerId());
        order.setBakeryId(bakery.getBakeryId());
        order.setAddressId(address != null ? address.getAddressId() : 0);
        order.setOrderPlacedDateTime(LocalDateTime.now());
        order.setOrderScheduledDateTime(scheduledDateTime);
        order.setOrderMethod(method);
        order.setOrderComment(txtNewComment.getText() != null ? txtNewComment.getText().trim() : null);
        order.setOrderTotal(total);
        order.setOrderDiscount(discount);
        order.setOrderStatus("Pending");

        // Build items list
        List<OrderItem> items = new ArrayList<>(cartItems);

        // Confirm
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Order");
        confirm.setHeaderText("Place order for " + customer.getFullName() + "?");
        confirm.setContentText(items.size() + " item(s), Total: $" + String.format("%.2f", total));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            int newOrderId = orderDao.createOrderWithItems(order, items);
            LogData.logAction("CREATE_ORDER",
                    "Order #" + newOrderId + " for " + customer.getFullName()
                    + " ($" + String.format("%.2f", total) + ")");

            // Clear the form
            clearNewOrderForm();
            lblNewOrderStatus.setText("Order #" + newOrderId + " placed successfully!");

            // Refresh orders tab
            loadOrderData();

        } catch (SQLException e) {
            LogData.handleException("CREATE_ORDER", e);
            showError("Order Failed", "Could not place order.", e.getMessage());
        }
    }

    private void clearNewOrderForm() {
        cboNewCustomer.getSelectionModel().clearSelection();
        cboNewBakery.getSelectionModel().clearSelection();
        cboNewMethod.getSelectionModel().clearSelection();
        dpScheduledDate.setValue(null);
        txtScheduledTime.clear();
        cboNewAddress.getSelectionModel().clearSelection();
        txtNewComment.clear();
        cartItems.clear();
        txtDiscount.setText("0.00");
        spnQuantity.getValueFactory().setValue(1);
        recalculateTotals();
    }

    // ════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════

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

    // ── Reusable cell factories ────────────────────────────────

    private static class CurrencyCell<S> extends TableCell<S, Double> {
        @Override
        protected void updateItem(Double price, boolean empty) {
            super.updateItem(price, empty);
            setText(empty || price == null ? "" : String.format("$%.2f", price));
        }
    }

    private static class DateTimeCell<S> extends TableCell<S, LocalDateTime> {
        private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        protected void updateItem(LocalDateTime item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText("");
            } else {
                setText(item.format(DT_FMT));
            }
        }
    }
}
