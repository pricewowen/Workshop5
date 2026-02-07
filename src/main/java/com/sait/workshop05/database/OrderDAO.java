package com.sait.workshop05.database;

import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Order entity.
 * Handles all database operations related to orders.
 */
public class OrderDAO {

    /**
     * Retrieve all orders from the database
     */
    public List<Order> getAllOrders() throws SQLException {
        String sql =
                "SELECT o.orderId, o.customerId, o.bakeryId, o.addressId, " +
                "       o.orderPlacedDateTime, o.orderScheduledDateTime, o.orderDeliveredDateTime, " +
                "       o.orderMethod, o.orderComment, o.orderTotal, o.orderDiscount, o.orderStatus, " +
                "       CONCAT(c.customerFirstName, ' ', c.customerLastName) AS customerDisplay, " +
                "       b.bakeryName AS bakeryDisplay, " +
                "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince) AS addressDisplay " +
                "FROM `Order` o " +
                "JOIN Customer c ON o.customerId = c.customerId " +
                "JOIN Bakery b ON o.bakeryId = b.bakeryId " +
                "LEFT JOIN Address a ON o.addressId = a.addressId " +
                "ORDER BY o.orderPlacedDateTime DESC";

        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }
        }

        return orders;
    }

    /**
     * Get orders by customer ID
     */
    public List<Order> getOrdersByCustomerId(int customerId) throws SQLException {
        String sql =
                "SELECT o.orderId, o.customerId, o.bakeryId, o.addressId, " +
                "       o.orderPlacedDateTime, o.orderScheduledDateTime, o.orderDeliveredDateTime, " +
                "       o.orderMethod, o.orderComment, o.orderTotal, o.orderDiscount, o.orderStatus, " +
                "       CONCAT(c.customerFirstName, ' ', c.customerLastName) AS customerDisplay, " +
                "       b.bakeryName AS bakeryDisplay, " +
                "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince) AS addressDisplay " +
                "FROM `Order` o " +
                "JOIN Customer c ON o.customerId = c.customerId " +
                "JOIN Bakery b ON o.bakeryId = b.bakeryId " +
                "LEFT JOIN Address a ON o.addressId = a.addressId " +
                "WHERE o.customerId = ? " +
                "ORDER BY o.orderPlacedDateTime DESC";

        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    orders.add(order);
                }
            }
        }

        return orders;
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(int orderId) throws SQLException {
        String sql =
                "SELECT o.orderId, o.customerId, o.bakeryId, o.addressId, " +
                "       o.orderPlacedDateTime, o.orderScheduledDateTime, o.orderDeliveredDateTime, " +
                "       o.orderMethod, o.orderComment, o.orderTotal, o.orderDiscount, o.orderStatus, " +
                "       CONCAT(c.customerFirstName, ' ', c.customerLastName) AS customerDisplay, " +
                "       b.bakeryName AS bakeryDisplay, " +
                "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince) AS addressDisplay " +
                "FROM `Order` o " +
                "JOIN Customer c ON o.customerId = c.customerId " +
                "JOIN Bakery b ON o.bakeryId = b.bakeryId " +
                "LEFT JOIN Address a ON o.addressId = a.addressId " +
                "WHERE o.orderId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        }

        return null;
    }

    /**
     * Create a new order
     */
    public int createOrder(Order order) throws SQLException {
        String sql =
                "INSERT INTO `Order` " +
                "(customerId, bakeryId, addressId, orderPlacedDateTime, orderScheduledDateTime, " +
                "orderMethod, orderComment, orderTotal, orderDiscount, orderStatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, order.getCustomerId());
            ps.setInt(2, order.getBakeryId());
            ps.setObject(3, order.getAddressId() > 0 ? order.getAddressId() : null);
            ps.setTimestamp(4, Timestamp.valueOf(order.getOrderPlacedDateTime()));

            if (order.getOrderScheduledDateTime() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(order.getOrderScheduledDateTime()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.setString(6, order.getOrderMethod());
            ps.setString(7, order.getOrderComment());
            ps.setDouble(8, order.getOrderTotal());
            ps.setDouble(9, order.getOrderDiscount());
            ps.setString(10, order.getOrderStatus());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE `Order` SET orderStatus = ? WHERE orderId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Update order delivered date/time
     */
    public boolean updateOrderDelivered(int orderId, Timestamp deliveredDateTime) throws SQLException {
        String sql = "UPDATE `Order` SET orderDeliveredDateTime = ?, orderStatus = 'Delivered' WHERE orderId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, deliveredDateTime);
            ps.setInt(2, orderId);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Get order items for a specific order
     */
    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        String sql =
                "SELECT oi.orderItemId, oi.orderId, oi.productId, oi.batchId, " +
                "       oi.orderItemQuantity, oi.orderItemUnitPriceAtTime, oi.orderItemLineTotal, " +
                "       p.productName AS productDisplay " +
                "FROM OrderItem oi " +
                "JOIN Product p ON oi.productId = p.productId " +
                "WHERE oi.orderId = ?";

        List<OrderItem> items = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("orderItemId"));
                    item.setOrderId(rs.getInt("orderId"));
                    item.setProductId(rs.getInt("productId"));
                    item.setBatchId(rs.getInt("batchId"));
                    item.setOrderItemQuantity(rs.getInt("orderItemQuantity"));
                    item.setOrderItemUnitPriceAtTime(rs.getDouble("orderItemUnitPriceAtTime"));
                    item.setOrderItemLineTotal(rs.getDouble("orderItemLineTotal"));
                    item.setProductDisplay(rs.getString("productDisplay"));
                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * Add an item to an order
     */
    public int addOrderItem(OrderItem item) throws SQLException {
        String sql =
                "INSERT INTO OrderItem " +
                "(orderId, productId, batchId, orderItemQuantity, orderItemUnitPriceAtTime, orderItemLineTotal) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setObject(3, item.getBatchId() > 0 ? item.getBatchId() : null);
            ps.setInt(4, item.getOrderItemQuantity());
            ps.setDouble(5, item.getOrderItemUnitPriceAtTime());
            ps.setDouble(6, item.getOrderItemLineTotal());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * Calculate order total from order items
     */
    public double calculateOrderTotal(int orderId) throws SQLException {
        String sql = "SELECT SUM(orderItemLineTotal) AS total FROM OrderItem WHERE orderId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }

        return 0.0;
    }

    /**
     * Create an order with all its items in a single transaction.
     */
    public int createOrderWithItems(Order order, List<OrderItem> items) throws SQLException {
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // Insert the Order
            String orderSql =
                    "INSERT INTO `Order` " +
                    "(customerId, bakeryId, addressId, orderPlacedDateTime, orderScheduledDateTime, " +
                    "orderMethod, orderComment, orderTotal, orderDiscount, orderStatus) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getCustomerId());
                ps.setInt(2, order.getBakeryId());
                ps.setObject(3, order.getAddressId() > 0 ? order.getAddressId() : null);
                ps.setTimestamp(4, Timestamp.valueOf(order.getOrderPlacedDateTime()));

                if (order.getOrderScheduledDateTime() != null) {
                    ps.setTimestamp(5, Timestamp.valueOf(order.getOrderScheduledDateTime()));
                } else {
                    ps.setNull(5, Types.TIMESTAMP);
                }

                ps.setString(6, order.getOrderMethod());
                ps.setString(7, order.getOrderComment());
                ps.setDouble(8, order.getOrderTotal());
                ps.setDouble(9, order.getOrderDiscount());
                ps.setString(10, order.getOrderStatus());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated order ID");
                    }
                }
            }

            // Insert all OrderItems
            String itemSql =
                    "INSERT INTO OrderItem " +
                    "(orderId, productId, batchId, orderItemQuantity, orderItemUnitPriceAtTime, orderItemLineTotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (OrderItem item : items) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getProductId());
                    ps.setObject(3, item.getBatchId() > 0 ? item.getBatchId() : null);
                    ps.setInt(4, item.getOrderItemQuantity());
                    ps.setDouble(5, item.getOrderItemUnitPriceAtTime());
                    ps.setDouble(6, item.getOrderItemLineTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Get all bakery locations for ComboBox population.
     */
    public List<BakeryOption> getBakeryOptions() throws SQLException {
        String sql = "SELECT bakeryId, bakeryName FROM Bakery ORDER BY bakeryName ASC";
        List<BakeryOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new BakeryOption(
                        rs.getInt("bakeryId"),
                        rs.getString("bakeryName")
                ));
            }
        }
        return options;
    }

    /**
     * Get all customers for ComboBox population in New Order tab.
     */
    public List<CustomerOption> getCustomerOptions() throws SQLException {
        String sql =
                "SELECT customerId, customerFirstName, customerLastName, customerRewardBalance " +
                "FROM Customer ORDER BY customerLastName, customerFirstName ASC";

        List<CustomerOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new CustomerOption(
                        rs.getInt("customerId"),
                        rs.getString("customerFirstName") + " " + rs.getString("customerLastName"),
                        rs.getInt("customerRewardBalance")
                ));
            }
        }
        return options;
    }

    /**
     * Get address options for ComboBox population.
     */
    public List<AddressOption> getAddressOptions() throws SQLException {
        return SharedDAO.getAddressOptions();
    }

    /**
     * Helper method to map ResultSet to Order object
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("orderId"));
        order.setCustomerId(rs.getInt("customerId"));
        order.setBakeryId(rs.getInt("bakeryId"));
        order.setAddressId(rs.getInt("addressId"));

        Timestamp placedTime = rs.getTimestamp("orderPlacedDateTime");
        if (placedTime != null) {
            order.setOrderPlacedDateTime(placedTime.toLocalDateTime());
        }

        Timestamp scheduledTime = rs.getTimestamp("orderScheduledDateTime");
        if (scheduledTime != null) {
            order.setOrderScheduledDateTime(scheduledTime.toLocalDateTime());
        }

        Timestamp deliveredTime = rs.getTimestamp("orderDeliveredDateTime");
        if (deliveredTime != null) {
            order.setOrderDeliveredDateTime(deliveredTime.toLocalDateTime());
        }

        order.setOrderMethod(rs.getString("orderMethod"));
        order.setOrderComment(rs.getString("orderComment"));
        order.setOrderTotal(rs.getDouble("orderTotal"));
        order.setOrderDiscount(rs.getDouble("orderDiscount"));
        order.setOrderStatus(rs.getString("orderStatus"));
        order.setCustomerDisplay(rs.getString("customerDisplay"));
        order.setBakeryDisplay(rs.getString("bakeryDisplay"));
        order.setAddressDisplay(rs.getString("addressDisplay"));

        return order;
    }
}

