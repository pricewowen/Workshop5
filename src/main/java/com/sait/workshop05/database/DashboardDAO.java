package com.sait.workshop05.database;

import com.sait.workshop05.models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Dashboard summary queries (Phase 11).
 * Provides aggregate statistics and recent order data.
 */
public class DashboardDAO {

    /**
     * Canonical sales status filter.
     * Defines what counts as recognized revenue.
     */
    private static final String SALES_STATUS_FILTER =
            "orderStatus IN ('Completed', 'Delivered')";

    /**
     * Get total revenue (sum of completed/delivered order totals)
     */
    public double getTotalRevenue() throws SQLException {
        String sql =
                "SELECT IFNULL(SUM(orderTotal), 0) AS revenue " +
                "FROM `Order` " +
                "WHERE " + SALES_STATUS_FILTER;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("revenue");
            }
        }
        return 0.0;
    }

    /**
     * Get total number of completed/delivered orders
     */
    public int getTotalOrders() throws SQLException {
        String sql =
                "SELECT COUNT(*) AS cnt " +
                "FROM `Order` " +
                "WHERE " + SALES_STATUS_FILTER;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("cnt");
            }
        }
        return 0;
    }

    /**
     * Get total number of customers
     */
    public int getTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM Customer";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("cnt");
            }
        }
        return 0;
    }

    /**
     * Get total number of products
     */
    public int getTotalProducts() throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM Product";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("cnt");
            }
        }
        return 0;
    }

    /**
     * Get recent orders (most recent N) with customer and bakery names.
     */
    public List<Order> getRecentOrders(int limit) throws SQLException {
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
                "ORDER BY o.orderPlacedDateTime DESC " +
                "LIMIT ?";

        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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

                    orders.add(order);
                }
            }
        }

        return orders;
    }
}
