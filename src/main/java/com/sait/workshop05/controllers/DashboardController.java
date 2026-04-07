package com.sait.workshop05.controllers;

import com.sait.workshop05.api.DashboardApi;
import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderItem;
import com.sait.workshop05.util.OrderStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the Dashboard view (Phase 11).
 * Wires summary cards and recent orders table to live DB data.
 */
public class DashboardController {

    @FXML private Button btnNewOrder;
    @FXML private Button btnRefresh;

    @FXML private TableColumn<Order, String> colOrderId;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colProducts;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, LocalDateTime> colDate;
    @FXML private TableColumn<Order, Void> colActions;

    @FXML private Label lblActiveProducts;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblTotalOrders;
    @FXML private Label lblTotalRevenu;
    @FXML private Label lblStatus;

    @FXML private TableView<Order> tbvRecentOrders;

    /** Pre-loaded product display strings keyed by order id. */
    private final Map<String, String> orderProductsMap = new HashMap<>();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    void initialize() {
        setupColumns();
        loadDashboard();
        if (btnNewOrder != null) btnNewOrder.setOnAction(e -> onNewOrder());
    }

    // ───────────────────────────────────────────────
    // Column setup
    // ───────────────────────────────────────────────

    private void setupColumns() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerDisplay"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("orderTotal"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

        // Format total as currency
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });

        // Date column with formatter
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderPlacedDateTime"));
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DT_FMT));
            }
        });

        // Status column with colour coding
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {

                        // 🟡 IN PROGRESS GROUP (same as Pending)
                        case OrderStatus.PENDING,
                             OrderStatus.PROCESSING,
                             OrderStatus.READY,
                             OrderStatus.OUT_FOR_DELIVERY,
                             "Scheduled",
                             "Placed" ->
                                setStyle("-fx-text-fill: #C48A1A; -fx-font-weight: bold;");

                        // 🟢 RECOGNIZED GROUP (same as Completed)
                        case OrderStatus.COMPLETED,
                             OrderStatus.DELIVERED ->
                                setStyle("-fx-text-fill: #5A9E6F; -fx-font-weight: bold;");

                        // 🔴 CANCELLED stays distinct
                        case OrderStatus.CANCELLED ->
                                setStyle("-fx-text-fill: #C75B52; -fx-font-weight: bold;");

                        default -> setStyle("");
                    }
                }
            }
        });

        // Products column — uses pre-loaded map (no per-row DB calls)
        colProducts.setCellValueFactory(new PropertyValueFactory<>("orderComment")); // placeholder binding
        colProducts.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                    return;
                }
                Order order = getTableView().getItems().get(getIndex());
                String products = orderProductsMap.getOrDefault(order.getOrderId(), "-");
                setText(products);
            }
        });

        // Actions column — View button
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("View");

            {
                btnView.getStyleClass().add("btn-muted");
                btnView.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnView);
            }
        });

        tbvRecentOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ───────────────────────────────────────────────
    // Load data
    // ───────────────────────────────────────────────

    private void loadDashboard() {
        try {
            DashboardApi.SummaryResponse s = DashboardApi.fetchSummary();

            // Summary cards
            lblTotalRevenu.setText(String.format("$%,.2f", s.totalRevenue != null ? s.totalRevenue.doubleValue() : 0));
            lblTotalOrders.setText(String.valueOf(s.totalOrders));
            lblTotalCustomers.setText(String.valueOf(s.totalCustomers));
            lblActiveProducts.setText(String.valueOf(s.totalProducts));

            // Recent orders table
            orderProductsMap.clear();
            List<Order> recent = new java.util.ArrayList<>();
            if (s.recentOrders != null) {
                for (OrderApi.OrderJson j : s.recentOrders) {
                    Order o = DashboardApi.toOrder(j);
                    List<OrderItem> items = DashboardApi.itemsFromSummaryOrder(j);
                    String products = items.stream()
                            .map(OrderItem::getProductDisplay)
                            .filter(name -> name != null && !name.isEmpty())
                            .collect(Collectors.joining(", "));
                    orderProductsMap.put(o.getOrderId(), products.isEmpty() ? "-" : products);
                    recent.add(o);
                }
            }
            tbvRecentOrders.setItems(FXCollections.observableArrayList(recent));
            lblStatus.setText(recent.size() + " recent order(s)");

        } catch (Exception e) {
            lblTotalRevenu.setText("$0.00");
            lblTotalOrders.setText("0");
            lblTotalCustomers.setText("0");
            lblActiveProducts.setText("0");
            tbvRecentOrders.setItems(FXCollections.observableArrayList());
            lblStatus.setText("Could not load dashboard");
            LogData.handleException("LOAD_DASHBOARD", e);
        }
    }

    // ───────────────────────────────────────────────
    // Actions
    // ───────────────────────────────────────────────

    @FXML
    private void onRefresh() {
        loadDashboard();
    }

    private void onNewOrder() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/sait/workshop05/order-management-view.fxml"));
            javafx.scene.Node page = loader.load();

            javafx.scene.layout.AnchorPane contentArea =
                    (javafx.scene.layout.AnchorPane) btnNewOrder.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(page);
                javafx.scene.layout.AnchorPane.setTopAnchor(page, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(page, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(page, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(page, 0.0);
            }
        } catch (Exception e) {
            LogData.handleException("NAV_NEW_ORDER", e);
            ErrorHandler.showErrorDialog("Navigation Error", "Could not open Order Management", e.getMessage());
        }
    }

    private void showOrderDetails(Order order) {
        try {
            List<OrderItem> items = OrderApi.getOrderItems(order.getOrderId());

            StringBuilder sb = new StringBuilder();
            sb.append("Order #").append(order.getOrderId()).append("\n");
            sb.append("Customer: ").append(order.getCustomerDisplay()).append("\n");
            sb.append("Bakery: ").append(order.getBakeryDisplay()).append("\n");
            sb.append("Method: ").append(order.getOrderMethod()).append("\n");
            sb.append("Status: ").append(order.getOrderStatus()).append("\n");
            sb.append("Placed: ").append(
                    order.getOrderPlacedDateTime() != null
                            ? order.getOrderPlacedDateTime().format(DT_FMT) : "-"
            ).append("\n");
            sb.append("Scheduled: ").append(
                    order.getOrderScheduledDateTime() != null
                            ? order.getOrderScheduledDateTime().format(DT_FMT) : "-"
            ).append("\n\n");

            sb.append("Items:\n");
            for (OrderItem item : items) {
                sb.append("  - ").append(item.getProductDisplay())
                        .append("  x").append(item.getOrderItemQuantity())
                        .append("  $").append(String.format("%.2f", item.getOrderItemLineTotal()))
                        .append("\n");
            }

            sb.append("\nSubtotal: $").append(String.format("%.2f", order.getOrderTotal()));
            sb.append("\nDiscount: $").append(String.format("%.2f", order.getOrderDiscount()));
            sb.append("\nTotal: $").append(String.format("%.2f", order.getFinalAmount()));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Details");
            alert.setHeaderText("Order #" + order.getOrderId());

            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(18);
            textArea.setPrefColumnCount(40);
            textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
            alert.showAndWait();

        } catch (Exception e) {
            LogData.handleException("VIEW_ORDER_DETAILS", e);
        }
    }
}
