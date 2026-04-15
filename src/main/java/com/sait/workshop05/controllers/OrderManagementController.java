package com.sait.workshop05.controllers;

import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.*;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.OrderStatus;
import com.sait.workshop05.util.AddressInputHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderManagementController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final List<String> STATUS_FLOW = OrderStatus.ALL_STATUSES;

    /**
     * Staff checkout with manual discount — must match {@code OrderService} (tax on net after discount;
     * delivery fee when method is delivery and net is under threshold).
     */
    private static final BigDecimal STAFF_CHECKOUT_TAX_PERCENT = new BigDecimal("5");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("7.00");
    private static final BigDecimal DELIVERY_FREE_THRESHOLD = new BigDecimal("50.00");

    /** Maximum manual discount as a percent of cart subtotal (matches validation and UI hint). */
    private static final double MAX_STAFF_DISCOUNT_PERCENT = 50.0;

    // ════════════════════════════════════════════════════════════
    // TAB 1: All Orders — FXML bindings
    // ════════════════════════════════════════════════════════════

    @FXML private TabPane tabPane;

    @FXML private TableView<Order> tblOrders;
    @FXML private TableColumn<Order, String> colOrderId;
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
    @FXML private Spinner<Integer> spnDiscountPercent;
    @FXML private TextField txtDiscount;
    @FXML private Label lblDiscountCapHint;
    @FXML private Label lblTax;
    @FXML private Label lblDelivery;
    @FXML private Label lblRewardInfo;
    @FXML private Label lblTotal;
    @FXML private Button btnPlaceOrder;
    @FXML private Label lblNewOrderStatus;

    // ════════════════════════════════════════════════════════════
    // State
    // ════════════════════════════════════════════════════════════

    // Tab 1
    private final ObservableList<Order> orderMaster = FXCollections.observableArrayList();
    private FilteredList<Order> orderFiltered;

    // Tab 2 catalog
    private final ObservableList<Product> catalogMaster = FXCollections.observableArrayList();
    private FilteredList<Product> catalogFiltered;

    // Tab 2 cart
    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();

    /** Prevents discount $ field and % spinner from ping-ponging while syncing. */
    private boolean suppressDiscountSync;

    /** Ignores stale order-line responses when the table selection changes quickly. */
    private final AtomicInteger orderItemsLoadGeneration = new AtomicInteger();

    /** Invalidates in-flight order-list loads (rapid refresh / leave page). */
    private final AtomicInteger orderListLoadGeneration = new AtomicInteger();

    /** Invalidates in-flight New Order reference loads (tab switch away from New Order). */
    private final AtomicInteger tab2RefsLoadGeneration = new AtomicInteger();

    /** Invalidates in-flight combined refresh tasks. */
    private final AtomicInteger refreshAllLoadGeneration = new AtomicInteger();

    private static final int TAB_ALL_ORDERS = 0;
    private static final int TAB_NEW_ORDER = 1;

    // ════════════════════════════════════════════════════════════
    // Initialization
    // ════════════════════════════════════════════════════════════

    @FXML
    void initialize() {
        setupTab1();
        setupTab2();
        applyOrderPageButtonStyles();
        setupTabPaneLoadingBehavior();
        loadOrderDataAsync();
        if (tabPane != null && tabPane.getSelectionModel().getSelectedIndex() == TAB_NEW_ORDER) {
            ensureTab2ReferencesLoadedAsync();
        }
    }

    /**
     * Loads catalog / combo data only when the New Order tab is shown, and cancels in-flight
     * tab-2 loads when switching away so the FX thread is not buried applying huge lists off-tab.
     */
    private void setupTabPaneLoadingBehavior() {
        if (tabPane == null) {
            return;
        }
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            int oldI = oldIdx != null ? oldIdx.intValue() : -1;
            int newI = newIdx != null ? newIdx.intValue() : -1;
            if (oldI == TAB_NEW_ORDER && newI != TAB_NEW_ORDER) {
                tab2RefsLoadGeneration.incrementAndGet();
            }
            if (newI == TAB_NEW_ORDER) {
                ensureTab2ReferencesLoadedAsync();
            }
        });
    }

    private void ensureTab2ReferencesLoadedAsync() {
        if (!catalogMaster.isEmpty()) {
            return;
        }
        loadTab2ReferencesAsync();
    }

    /** False after this controller's root is removed from the scene (user navigated to another page). */
    private boolean isOrderViewAttached() {
        return tblOrders != null && tblOrders.getScene() != null;
    }

    /**
     * Ensures toolbar / action buttons pick up app styles (same gray {@code btn-muted} as Products).
     * TabPane content can miss scene styles; the FXML root also loads {@code styles.css}.
     */
    private void applyOrderPageButtonStyles() {
        styleMutedToolbar(btnRefreshOrders);
        styleMutedToolbar(btnUpdateStatus);
        styleMutedToolbar(btnAddToOrder);
        styleMutedToolbar(btnRemoveFromOrder);
        styleMutedToolbar(btnPlaceOrder);
    }

    private static void styleMutedToolbar(Button b) {
        if (b == null) {
            return;
        }
        b.getStyleClass().removeAll("btn-cta", "btn-accent-lg", "btn-accent", "btn-muted", "toolbar-action");
        b.getStyleClass().addAll("btn-muted", "toolbar-action");
    }

    // ────────────────────────────────────────────────────────────
    // TAB 1 Setup
    // ────────────────────────────────────────────────────────────

    private void setupTab1() {
        // Column bindings
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
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
        cboStatusFilter.setItems(FXCollections.observableArrayList(OrderStatus.FILTER_STATUSES));
        cboStatusFilter.setValue("All");

        // Status update ComboBox
        cboNewStatus.setItems(FXCollections.observableArrayList(OrderStatus.STAFF_ASSIGNABLE_STATUSES));

        // Filtering
        orderFiltered = new FilteredList<>(orderMaster, o -> true);

        txtOrderSearch.textProperty().addListener((obs, o, n) -> applyOrderFilter());
        cboStatusFilter.valueProperty().addListener((obs, o, n) -> applyOrderFilter());

        SortedList<Order> sorted = new SortedList<>(orderFiltered);
        sorted.comparatorProperty().bind(tblOrders.comparatorProperty());
        tblOrders.setItems(sorted);
        tblOrders.setPlaceholder(new Label("Loading orders…"));
        tblOrderItems.setPlaceholder(new Label("Select an order above to view line items."));

        // Selection binding -> load items
        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                tblOrderItems.getItems().clear();
                tblOrderItems.setPlaceholder(new Label("Select an order above to view line items."));
                lblOrderDetailTitle.setText("Select an order above to view details");
                cboNewStatus.setValue(null);
                return;
            }

            lblOrderDetailTitle.setText("Order " + selected.getOrderNumber()
                    + " — " + selected.getCustomerDisplay());
            String rowStatus = selected.getOrderStatus();
            cboNewStatus.setValue(resolveStaffAssignableComboValue(rowStatus));

            String orderId = selected.getOrderId();
            int gen = orderItemsLoadGeneration.incrementAndGet();
            tblOrderItems.setPlaceholder(new Label("Loading line items…"));
            tblOrderItems.setItems(FXCollections.observableArrayList());
            Task<List<OrderItem>> itemsTask = new Task<>() {
                @Override
                protected List<OrderItem> call() throws Exception {
                    return OrderApi.getOrderItems(orderId);
                }
            };
            itemsTask.setOnSucceeded(e -> {
                if (gen != orderItemsLoadGeneration.get()) {
                    return;
                }
                if (!isOrderViewAttached()) {
                    return;
                }
                Order still = tblOrders.getSelectionModel().getSelectedItem();
                if (still == null || !orderId.equals(still.getOrderId())) {
                    return;
                }
                List<OrderItem> rows = itemsTask.getValue();
                tblOrderItems.setItems(FXCollections.observableArrayList(rows));
                if (rows == null || rows.isEmpty()) {
                    tblOrderItems.setPlaceholder(new Label("No line items for this order."));
                } else {
                    tblOrderItems.setPlaceholder(null);
                }
            });
            itemsTask.setOnFailed(e -> {
                if (gen != orderItemsLoadGeneration.get()) {
                    return;
                }
                if (!isOrderViewAttached()) {
                    return;
                }
                Order still = tblOrders.getSelectionModel().getSelectedItem();
                if (still == null || !orderId.equals(still.getOrderId())) {
                    return;
                }
                tblOrderItems.getItems().clear();
                tblOrderItems.setPlaceholder(new Label("Could not load line items."));
                Throwable t = itemsTask.getException();
                LogData.handleException("LOAD_ORDER_ITEMS", new RuntimeException(t));
                ErrorHandler.showErrorDialog(
                        "API Error",
                        "Could not load order line items.",
                        t);
            });
            new Thread(itemsTask, "order-items-" + orderId).start();
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
                    || contains(order.getOrderNumber(), q)
                    || String.format("%.2f", order.getOrderTotal()).contains(q);
        });

        lblOrderStatus.setText(orderFiltered.size() + " order(s) shown");
        if (orderFiltered.isEmpty()) {
            tblOrders.setPlaceholder(new Label(
                    orderMaster.isEmpty()
                            ? "No orders to display."
                            : "No orders match the current filter."));
        }
    }

    // ────────────────────────────────────────────────────────────
    // TAB 2 Setup
    // ────────────────────────────────────────────────────────────

    private void setupTab2() {
        // Method ComboBox
        cboNewMethod.setItems(FXCollections.observableArrayList("Pickup", "Delivery", "Dine-in"));
        cboNewMethod.valueProperty().addListener((obs, o, n) -> recalculateTotals());

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
        tblCatalog.setPlaceholder(new Label("Loading catalog…"));

        // Customer selection -> show reward balance and auto-populate their address
        cboNewCustomer.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblRewardInfo.setText("Balance: " + newVal.getRewardBalance() + " pts");
                if (newVal.getAddressId() != null) {
                    AddressInputHelper.selectAddressById(cboNewAddress, newVal.getAddressId());
                } else {
                    AddressInputHelper.clearAddressField(cboNewAddress);
                }
            } else {
                lblRewardInfo.setText("Select a customer");
                AddressInputHelper.clearAddressField(cboNewAddress);
            }
        });

        IntegerSpinnerValueFactory pctFactory =
                new IntegerSpinnerValueFactory(0, (int) MAX_STAFF_DISCOUNT_PERCENT, 0);
        spnDiscountPercent.setValueFactory(pctFactory);
        spnDiscountPercent.setEditable(true);
        pctFactory.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressDiscountSync) {
                return;
            }
            refreshDollarDiscountFromPercentOnly();
        });

        // Discount $ field (sync % spinner when user types dollars)
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*\\.?\\d{0,2}")) {
                txtDiscount.setText(oldVal);
            } else {
                if (!suppressDiscountSync) {
                    syncSpinnerFromDollarField();
                }
                recalculateTotals();
            }
        });

        // Load ComboBox data
        AddressInputHelper.configureEditableAddressCombo(cboNewAddress);
        configureNewOrderCustomerCombo();
        refreshDollarDiscountFromPercentOnly();
    }

    /** Show customer name only in the New Order picker (never the internal customer UUID). */
    private void configureNewOrderCustomerCombo() {
        Callback<ListView<CustomerOption>, ListCell<CustomerOption>> cellFactory =
                lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(CustomerOption item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getFullName());
                    }
                };
        cboNewCustomer.setCellFactory(cellFactory);
        cboNewCustomer.setButtonCell(cellFactory.call(null));
    }

    private record Tab2References(
            List<CustomerOption> customers,
            List<BakeryOption> bakeries,
            List<AddressOption> addresses,
            List<Product> products
    ) {}

    private Tab2References fetchTab2References() throws Exception {
        return new Tab2References(
                ReferenceApi.loadCustomers(),
                ReferenceApi.loadBakeries(),
                ReferenceApi.loadAddresses(),
                ReferenceApi.loadProducts()
        );
    }

    private void applyTab2References(Tab2References d) {
        cboNewCustomer.setItems(FXCollections.observableArrayList(
                d.customers() != null ? d.customers() : List.of()));
        cboNewBakery.setItems(FXCollections.observableArrayList(
                d.bakeries() != null ? d.bakeries() : List.of()));
        AddressInputHelper.setAddressItems(cboNewAddress,
                d.addresses() != null ? d.addresses() : List.of());
        List<Product> products = d.products() != null ? d.products() : List.of();
        catalogMaster.setAll(products);
        if (products.isEmpty()) {
            tblCatalog.setPlaceholder(new Label("No products available."));
        } else {
            tblCatalog.setPlaceholder(new Label("No products match the search filter."));
        }
    }

    private void loadTab2ReferencesAsync() {
        int myGen = tab2RefsLoadGeneration.incrementAndGet();
        tblCatalog.setPlaceholder(new Label("Loading catalog…"));
        Task<Tab2References> task = new Task<>() {
            @Override
            protected Tab2References call() throws Exception {
                return fetchTab2References();
            }
        };
        task.setOnSucceeded(e -> {
            if (myGen != tab2RefsLoadGeneration.get()) {
                return;
            }
            if (!isOrderViewAttached()) {
                return;
            }
            applyTab2References(task.getValue());
        });
        task.setOnFailed(e -> {
            if (myGen != tab2RefsLoadGeneration.get()) {
                return;
            }
            if (!isOrderViewAttached()) {
                return;
            }
            Throwable t = task.getException();
            LogData.handleException("LOAD_ORDER_TAB2_REF", new RuntimeException(t));
            tblCatalog.setPlaceholder(new Label("Could not load catalog."));
            ErrorHandler.showErrorDialog(
                    "API Error",
                    "Could not load new-order lists or catalog.",
                    t);
        });
        new Thread(task, "orders-tab2-ref").start();
    }

    /**
     * Used after creating an address from typed text so the new row appears before placing the order.
     */
    private void reloadTab2ReferencesSync() throws Exception {
        applyTab2References(fetchTab2References());
    }

    // ════════════════════════════════════════════════════════════
    // TAB 1 Actions
    // ════════════════════════════════════════════════════════════

    private record OrdersAndTab2(List<Order> orders, Tab2References tab2) {}

    private void loadOrderDataAsync() {
        int myGen = orderListLoadGeneration.incrementAndGet();
        lblOrderStatus.setText("Loading orders…");
        tblOrders.setPlaceholder(new Label("Loading orders…"));
        if (btnRefreshOrders != null) {
            btnRefreshOrders.setDisable(true);
        }
        Task<List<Order>> task = new Task<>() {
            @Override
            protected List<Order> call() throws Exception {
                return OrderApi.listOrders();
            }
        };
        task.setOnSucceeded(e -> {
            if (myGen != orderListLoadGeneration.get()) {
                return;
            }
            if (!isOrderViewAttached()) {
                return;
            }
            if (btnRefreshOrders != null) {
                btnRefreshOrders.setDisable(false);
            }
            List<Order> rows = task.getValue();
            orderMaster.setAll(rows != null ? rows : List.of());
            lblOrderStatus.setText(orderMaster.size() + " order(s) loaded");
            tblOrders.setPlaceholder(new Label("No orders to display."));
            LogData.logAction("READ", "Order");
        });
        task.setOnFailed(e -> {
            if (myGen != orderListLoadGeneration.get()) {
                return;
            }
            if (!isOrderViewAttached()) {
                return;
            }
            if (btnRefreshOrders != null) {
                btnRefreshOrders.setDisable(false);
            }
            Throwable t = task.getException();
            LogData.handleException("READ_ORDERS", new RuntimeException(t));
            lblOrderStatus.setText("Could not load orders.");
            tblOrders.setPlaceholder(new Label("Could not load orders."));
            ErrorHandler.showErrorDialog(
                    "API Error",
                    "Could not load orders.",
                    t);
        });
        new Thread(task, "orders-list").start();
    }

    @FXML
    private void onRefreshOrders() {
        orderListLoadGeneration.incrementAndGet();
        tab2RefsLoadGeneration.incrementAndGet();
        int myGen = refreshAllLoadGeneration.incrementAndGet();

        lblOrderStatus.setText("Refreshing…");
        tblOrders.setPlaceholder(new Label("Loading orders…"));
        if (btnRefreshOrders != null) {
            btnRefreshOrders.setDisable(true);
        }
        Task<OrdersAndTab2> task = new Task<>() {
            @Override
            protected OrdersAndTab2 call() throws Exception {
                return new OrdersAndTab2(OrderApi.listOrders(), fetchTab2References());
            }
        };
        task.setOnSucceeded(e -> {
            if (myGen != refreshAllLoadGeneration.get()) {
                return;
            }
            if (!isOrderViewAttached()) {
                return;
            }
            if (btnRefreshOrders != null) {
                btnRefreshOrders.setDisable(false);
            }
            OrdersAndTab2 pack = task.getValue();
            List<Order> orders = pack.orders() != null ? pack.orders() : List.of();
            orderMaster.setAll(orders);
            applyTab2References(pack.tab2());
            lblOrderStatus.setText(orderMaster.size() + " order(s) loaded");
            tblOrders.setPlaceholder(new Label("No orders to display."));
            LogData.logAction("READ", "Order");
        });
        task.setOnFailed(e -> {
            if (myGen != refreshAllLoadGeneration.get()) {
                return;
            }
            if (!isOrderViewAttached()) {
                return;
            }
            if (btnRefreshOrders != null) {
                btnRefreshOrders.setDisable(false);
            }
            Throwable t = task.getException();
            LogData.handleException("REFRESH_ORDERS_PAGE", new RuntimeException(t));
            lblOrderStatus.setText("Refresh failed.");
            tblOrders.setPlaceholder(new Label("Could not load orders."));
            ErrorHandler.showErrorDialog(
                    "API Error",
                    "Could not refresh orders or new-order data.",
                    t);
        });
        new Thread(task, "orders-refresh-all").start();
    }

    @FXML
    private void onUpdateStatus() {
        Order selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Update Status", "Select an order first.");
            return;
        }

        String newStatus = cboNewStatus.getValue();
        if (newStatus == null || newStatus.isBlank()) {
            ErrorHandler.showWarning("Update Status", "Select a new status.");
            return;
        }

        // Validate status transition
        String currentStatus = selected.getOrderStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            ErrorHandler.showWarning("Invalid Transition",
                    "Cannot change status from '" + currentStatus + "' to '" + newStatus + "'.\n"
                    + "Status can only move forward (except Cancelled which is always allowed).");
            return;
        }

        String orderNumber = selected.getOrderNumber();
        String orderId = selected.getOrderId();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                OrderApi.updateOrderStatus(orderId, newStatus);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            LogData.logAction("UPDATE_STATUS",
                    "Order " + orderNumber + ": " + currentStatus + " -> " + newStatus);
            loadOrderDataAsync();
            lblOrderStatus.setText("Updated " + orderNumber + " to " + newStatus);
        });
        task.setOnFailed(e -> {
            Throwable t = task.getException();
            LogData.handleException("UPDATE_ORDER_STATUS", new RuntimeException(t));
            ErrorHandler.showErrorDialog(
                    "Update Failed",
                    "Could not update order status.",
                    t);
        });
        new Thread(task, "order-status-update").start();
    }

    /**
     * Binds the status combo only to values present in {@link OrderStatus#STAFF_ASSIGNABLE_STATUSES}
     * (e.g. Completed/Cancelled rows leave the combo cleared).
     */
    private static String resolveStaffAssignableComboValue(String rowStatus) {
        if (rowStatus == null || rowStatus.isBlank()) {
            return null;
        }
        for (String allowed : OrderStatus.STAFF_ASSIGNABLE_STATUSES) {
            if (allowed.equalsIgnoreCase(rowStatus)) {
                return allowed;
            }
        }
        return null;
    }

    private boolean isValidStatusTransition(String current, String next) {
        if (current == null || next == null) return false;
        if (current.equalsIgnoreCase(next)) return false;

        // Cancelled is always allowed (from any state)
        if (OrderStatus.CANCELLED.equalsIgnoreCase(next)) return true;

        // Match mobile admin: staff never PATCHes "completed" (customer / system completes the order).
        if (OrderStatus.COMPLETED.equalsIgnoreCase(next)) return false;

        // Cannot transition FROM Completed or Cancelled
        if (OrderStatus.COMPLETED.equalsIgnoreCase(current) || OrderStatus.CANCELLED.equalsIgnoreCase(current)) return false;

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
            ErrorHandler.showWarning("Add Item", "Select a product from the catalog first.");
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
                refreshDollarDiscountFromPercentOnly();
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
        refreshDollarDiscountFromPercentOnly();
    }

    @FXML
    private void onRemoveFromOrder() {
        OrderItem selected = tblCart.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Remove Item", "Select an item from the cart to remove.");
            return;
        }

        cartItems.remove(selected);
        refreshDollarDiscountFromPercentOnly();
    }

    private double cartSubtotalRaw() {
        double subtotal = 0;
        for (OrderItem item : cartItems) {
            subtotal += item.getOrderItemLineTotal();
        }
        return subtotal;
    }

    /**
     * Sets the dollar discount from the current % spinner and cart subtotal (max {@value #MAX_STAFF_DISCOUNT_PERCENT}%),
     * then refreshes tax/total labels.
     */
    private void refreshDollarDiscountFromPercentOnly() {
        if (spnDiscountPercent == null || txtDiscount == null) {
            recalculateTotals();
            return;
        }
        suppressDiscountSync = true;
        try {
            int pct = spnDiscountPercent.getValue();
            BigDecimal sub = BigDecimal.valueOf(cartSubtotalRaw()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal dollars = sub.multiply(BigDecimal.valueOf(pct))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            txtDiscount.setText(String.format("%.2f", dollars.doubleValue()));
        } finally {
            suppressDiscountSync = false;
        }
        recalculateTotals();
    }

    /** Keeps the % spinner in range when the user edits the dollar discount field. */
    private void syncSpinnerFromDollarField() {
        if (spnDiscountPercent == null) {
            return;
        }
        double sub = cartSubtotalRaw();
        double disc = parseDiscountFieldRaw();
        int pct;
        if (sub <= 1e-9) {
            pct = 0;
        } else {
            pct = (int) Math.round(disc / sub * 100.0);
            if (pct < 0) {
                pct = 0;
            }
            if (pct > (int) MAX_STAFF_DISCOUNT_PERCENT) {
                pct = (int) MAX_STAFF_DISCOUNT_PERCENT;
            }
        }
        IntegerSpinnerValueFactory f = (IntegerSpinnerValueFactory) spnDiscountPercent.getValueFactory();
        if (f.getValue() != pct) {
            suppressDiscountSync = true;
            try {
                f.setValue(pct);
            } finally {
                suppressDiscountSync = false;
            }
        }
    }

    private void recalculateTotals() {
        NewOrderEstimate e = estimateNewOrderCheckout();
        lblSubtotal.setText(String.format("$%.2f", e.subtotal));
        lblTax.setText(String.format("$%.2f", e.tax));

        boolean isDelivery = cboNewMethod.getValue() != null
                && "Delivery".equalsIgnoreCase(cboNewMethod.getValue());
        if (!isDelivery) {
            lblDelivery.setText("—");
        } else if (e.deliveryFee <= 0.001) {
            lblDelivery.setText("$0.00 (free over $50)");
        } else {
            lblDelivery.setText(String.format("$%.2f", e.deliveryFee));
        }

        lblTotal.setText(String.format("$%.2f", e.grandTotal));

        updateDiscountPercentHint(e.subtotal);
    }

    private void updateDiscountPercentHint(double subtotal) {
        if (lblDiscountCapHint == null) {
            return;
        }
        if (subtotal <= 0.001) {
            lblDiscountCapHint.setText("");
            lblDiscountCapHint.setManaged(false);
            lblDiscountCapHint.setVisible(false);
            return;
        }
        lblDiscountCapHint.setManaged(true);
        lblDiscountCapHint.setVisible(true);
        double raw = parseDiscountFieldRaw();
        double maxD = subtotal * (MAX_STAFF_DISCOUNT_PERCENT / 100.0);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Max %.0f%% off (≈ $%.2f)", MAX_STAFF_DISCOUNT_PERCENT, maxD));
        if (raw > 0.001) {
            sb.append(String.format(" · entered ≈ %.1f%% off subtotal", raw / subtotal * 100.0));
        }
        lblDiscountCapHint.setText(sb.toString());
        if (raw > maxD + 0.001) {
            lblDiscountCapHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #B85C4C;");
        } else {
            lblDiscountCapHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A8178;");
        }
    }

    private static double parseDiscountFieldRaw(TextField field) {
        if (field == null || field.getText() == null) {
            return 0;
        }
        try {
            double d = Double.parseDouble(field.getText().trim());
            return d < 0 ? 0 : d;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDiscountFieldRaw() {
        return parseDiscountFieldRaw(txtDiscount);
    }

    /**
     * Mirrors backend staff order: list subtotal, manual discount capped at subtotal, tax 5% on net,
     * delivery $7 when method is Delivery and net &lt; $50 (Pickup/Dine-in → no delivery line).
     */
    private NewOrderEstimate estimateNewOrderCheckout() {
        double rawSubtotal = cartSubtotalRaw();
        double discount = 0;
        try {
            discount = Double.parseDouble(txtDiscount.getText().trim());
            if (discount < 0) {
                discount = 0;
            }
        } catch (NumberFormatException e) {
            discount = 0;
        }

        BigDecimal listSubtotal = BigDecimal.valueOf(rawSubtotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal manual = BigDecimal.valueOf(discount).setScale(2, RoundingMode.HALF_UP);
        if (manual.compareTo(listSubtotal) > 0) {
            manual = listSubtotal;
        }
        BigDecimal afterDiscount = listSubtotal.subtract(manual).max(BigDecimal.ZERO);

        boolean isDelivery = cboNewMethod.getValue() != null
                && "Delivery".equalsIgnoreCase(cboNewMethod.getValue());
        BigDecimal delivery = BigDecimal.ZERO;
        if (isDelivery && afterDiscount.compareTo(DELIVERY_FREE_THRESHOLD) < 0) {
            delivery = DELIVERY_FEE;
        }

        BigDecimal tax = afterDiscount.multiply(STAFF_CHECKOUT_TAX_PERCENT)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = afterDiscount.add(tax).add(delivery).setScale(2, RoundingMode.HALF_UP);

        return new NewOrderEstimate(
                listSubtotal.doubleValue(),
                manual.doubleValue(),
                afterDiscount.doubleValue(),
                tax.doubleValue(),
                delivery.doubleValue(),
                grandTotal.doubleValue());
    }

    private record NewOrderEstimate(double subtotal, double discount, double afterDiscount,
                                    double tax, double deliveryFee, double grandTotal) {}

    @FXML
    private void onPlaceOrder() {
        // Validate
        CustomerOption customer = cboNewCustomer.getValue();
        BakeryOption bakery = cboNewBakery.getValue();
        String method = cboNewMethod.getValue();

        if (customer == null) {
            ErrorHandler.showWarning("Validation", "Please select a customer.");
            return;
        }
        if (bakery == null) {
            ErrorHandler.showWarning("Validation", "Please select a bakery location.");
            return;
        }
        if (method == null || method.isBlank()) {
            ErrorHandler.showWarning("Validation", "Please select an order method.");
            return;
        }
        if (cartItems.isEmpty()) {
            ErrorHandler.showWarning("Validation", "Add at least one product to the order.");
            return;
        }

        NewOrderEstimate est = estimateNewOrderCheckout();

        // Validation: discount cannot exceed MAX_STAFF_DISCOUNT_PERCENT of subtotal (0–50% inclusive)
        double maxDiscount = est.subtotal * (MAX_STAFF_DISCOUNT_PERCENT / 100.0);
        // Round both sides to 2 decimal places before comparing to avoid floating-point false positives
        double discountRounded  = Math.round(est.discount  * 100.0) / 100.0;
        double maxDiscountRounded = Math.round(maxDiscount * 100.0) / 100.0;
        if (discountRounded > maxDiscountRounded) {
            double pctEntered = est.subtotal > 1e-9 ? (est.discount / est.subtotal) * 100.0 : 0;
            ErrorHandler.showWarning("Validation", String.format(
                    "Discount cannot exceed %.0f%% of the subtotal (about $%.2f max). You entered about %.1f%% ($%.2f).",
                    MAX_STAFF_DISCOUNT_PERCENT, maxDiscount, pctEntered, est.discount));
            return;
        }

        // Parse scheduled date/time (both fields required)
        LocalDate schedDate = dpScheduledDate.getValue();
        String timeStr = txtScheduledTime.getText() != null ? txtScheduledTime.getText().trim() : "";

        if (schedDate == null) {
            ErrorHandler.showWarning("Validation", "Please select a scheduled date.");
            return;
        }
        if (timeStr.isEmpty()) {
            ErrorHandler.showWarning("Validation", "Please enter a scheduled time (HH:mm).");
            return;
        }

        LocalTime schedTime;
        try {
            schedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            ErrorHandler.showWarning("Validation", "Scheduled time format must be HH:mm (e.g., 14:30).");
            return;
        }

        LocalDateTime scheduledDateTime = LocalDateTime.of(schedDate, schedTime);

        // Validation: scheduled date must be after now
        if (scheduledDateTime.isBefore(LocalDateTime.now())) {
            ErrorHandler.showWarning("Validation", "Scheduled date/time must be in the future.");
            return;
        }

        // Validation: scheduled time must fall within the selected bakery's hours of operation
        try {
            String hoursError = validateScheduledTimeAgainstBakeryHours(
                    scheduledDateTime, bakery.getBakeryId());
            if (hoursError != null) {
                ErrorHandler.showWarning("Validation", hoursError);
                return;
            }
        } catch (Exception ex) {
            LogData.handleException("BAKERY_HOURS_CHECK", ex);
            ErrorHandler.showWarning("Validation",
                    "Could not verify bakery hours. Please confirm the scheduled time is correct.");
        }

        // Delivery address — required when method is Delivery
        boolean isDeliveryMethod = "Delivery".equalsIgnoreCase(method);
        AddressOption address;
        try {
            address = resolveAddressSelection(isDeliveryMethod);
        } catch (IllegalArgumentException ex) {
            ErrorHandler.showWarning("Validation",
                    isDeliveryMethod
                            ? "A delivery address is required. Please select or enter an address."
                            : "Check your address and order details.");
            return;
        } catch (Exception ex) {
            LogData.handleException("CREATE_ORDER_ADDRESS", ex);
            ErrorHandler.showErrorDialog("API Error", "Could not resolve address.", ex);
            return;
        }

        List<OrderItem> items = new ArrayList<>(cartItems);

        String deliveryLine = !isDeliveryMethod
                ? "Delivery: —"
                : (est.deliveryFee <= 0.001
                        ? "Delivery: $0.00 (free over $50)"
                        : String.format("Delivery: $%.2f", est.deliveryFee));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        confirm.getDialogPane().getStyleClass().add("modal-dialog-pane");
        confirm.setTitle("Confirm Order");
        confirm.setHeaderText("Place order for " + customer.getFullName() + "?");
        confirm.setContentText(String.format(
                "%d item(s)%nSubtotal $%.2f · Discount $%.2f · Tax (5%%) $%.2f%n%s%nEstimated total: $%.2f",
                items.size(), est.subtotal, est.discount, est.tax, deliveryLine, est.grandTotal));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        if (btnPlaceOrder != null) {
            btnPlaceOrder.setDisable(true);
        }
        lblNewOrderStatus.setText("Placing order…");

        String customerId = customer.getCustomerId();
        int bakeryId = bakery.getBakeryId();
        Integer addressId = address != null ? address.getAddressId() : null;
        String comment = txtNewComment.getText() != null ? txtNewComment.getText().trim() : null;
        final LocalDateTime scheduledForApi = scheduledDateTime;
        final double discountForApi = est.discount;

        Task<String> placeTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return OrderApi.placeStaffOrder(
                        customerId,
                        bakeryId,
                        method,
                        addressId,
                        comment,
                        scheduledForApi,
                        discountForApi,
                        items
                );
            }
        };
        placeTask.setOnSucceeded(e -> {
            if (btnPlaceOrder != null) {
                btnPlaceOrder.setDisable(false);
            }
            String newOrderId = placeTask.getValue();
            String statusRef = "placed".equals(newOrderId)
                    ? "order placed"
                    : "Order #" + newOrderId;
            LogData.logAction("CREATE_ORDER",
                    statusRef + " for " + customer.getFullName()
                            + " (est. total $" + String.format("%.2f", est.grandTotal) + ")");

            clearNewOrderForm();
            lblNewOrderStatus.setText("placed".equals(newOrderId)
                    ? "Order placed successfully!"
                    : "Order #" + newOrderId + " placed successfully!");

            ReferenceApi.invalidateCustomersCache();
            loadOrderDataAsync();
        });
        placeTask.setOnFailed(e -> {
            if (btnPlaceOrder != null) {
                btnPlaceOrder.setDisable(false);
            }
            lblNewOrderStatus.setText("");
            Throwable t = placeTask.getException();
            LogData.handleException("CREATE_ORDER", new RuntimeException(t));
            ErrorHandler.showErrorDialog(
                    "Order Failed",
                    "Could not place order.",
                    t);
        });
        new Thread(placeTask, "order-place-staff").start();
    }

    private void clearNewOrderForm() {
        cboNewCustomer.getSelectionModel().clearSelection();
        cboNewBakery.getSelectionModel().clearSelection();
        cboNewMethod.getSelectionModel().clearSelection();
        dpScheduledDate.setValue(null);
        txtScheduledTime.clear();
        AddressInputHelper.clearAddressField(cboNewAddress);
        txtNewComment.clear();
        cartItems.clear();
        txtDiscount.setText("0.00");
        if (spnDiscountPercent != null && spnDiscountPercent.getValueFactory() instanceof IntegerSpinnerValueFactory f) {
            suppressDiscountSync = true;
            try {
                f.setValue(0);
            } finally {
                suppressDiscountSync = false;
            }
        }
        spnQuantity.getValueFactory().setValue(1);
        recalculateTotals();
    }

    // ════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════

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

    /**
     * Checks that {@code scheduledDateTime} falls within the given bakery's operating hours for
     * that day of week. Returns {@code null} when the time is valid (or no hours are configured),
     * or a user-facing error string when it is not.
     */
    private String validateScheduledTimeAgainstBakeryHours(
            LocalDateTime scheduledDateTime, int bakeryId) throws Exception {

        List<com.sait.workshop05.api.CatalogApi.BakeryHourJson> hours =
                com.sait.workshop05.api.CatalogApi.fetchBakeryHours(bakeryId);

        if (hours == null || hours.isEmpty()) {
            return null; // no hours configured — skip validation
        }

        int dow = scheduledDateTime.getDayOfWeek().getValue(); // 1=Mon … 7=Sun (ISO)
        LocalTime schedTime = scheduledDateTime.toLocalTime();

        com.sait.workshop05.api.CatalogApi.BakeryHourJson dayHour = hours.stream()
                .filter(h -> h.dayOfWeek == dow)
                .findFirst()
                .orElse(null);

        if (dayHour == null) {
            return "The bakery has no hours configured for " +
                    scheduledDateTime.getDayOfWeek().getDisplayName(
                            java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) + ".";
        }
        if (dayHour.closed) {
            return "The bakery is closed on " +
                    scheduledDateTime.getDayOfWeek().getDisplayName(
                            java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) + ".";
        }

        if (dayHour.openTime != null && dayHour.closeTime != null) {
            LocalTime open  = LocalTime.parse(dayHour.openTime.substring(0, 5));
            LocalTime close = LocalTime.parse(dayHour.closeTime.substring(0, 5));
            if (schedTime.isBefore(open) || schedTime.isAfter(close)) {
                return String.format(
                        "Scheduled time (%s) is outside bakery hours (%s – %s).",
                        schedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        dayHour.openTime.substring(0, 5),
                        dayHour.closeTime.substring(0, 5));
            }
        }

        return null; // valid
    }

    private AddressOption resolveAddressSelection(boolean required) throws Exception {
        AddressOption selected = cboNewAddress.getValue();
        if (selected != null) return selected;

        String typed = AddressInputHelper.getTypedText(cboNewAddress);
        if (typed.isBlank()) {
            if (required) throw new IllegalArgumentException("Address is required.");
            return null;
        }

        AddressOption resolved = ReferenceApi.createAddressFromTyped(typed);
        reloadTab2ReferencesSync();
        AddressInputHelper.selectAddressById(cboNewAddress, resolved.getAddressId());
        return resolved;
    }
}
