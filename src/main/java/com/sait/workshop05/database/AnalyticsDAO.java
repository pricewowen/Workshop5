package com.sait.workshop05.database;

import com.sait.workshop05.analytics.DataPoint;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDAO {

    /* ==============================
       Revenue Over Time
       ============================== */

    public double getTotalRevenue(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT IFNULL(SUM(orderTotal - orderDiscount),0) AS revenue
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND DATE(o.orderPlacedDateTime) >= ?");
            params.add(Date.valueOf(start));
        }

        if (end != null) {
            sql.append(" AND DATE(o.orderPlacedDateTime) <= ?");
            params.add(Date.valueOf(end));
        }

        if (bakery != null && !bakery.equals("All Bakeries")) {
            sql.append(" AND b.bakeryName = ?");
            params.add(bakery);
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("revenue");
            }
        }

        return 0.0;
    }

    public List<DataPoint> getRevenueOverTime(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT DATE(o.orderPlacedDateTime) AS day,
                   SUM(o.orderTotal - o.orderDiscount) AS revenue
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND DATE(o.orderPlacedDateTime) >= ?");
            params.add(Date.valueOf(start));
        }

        if (end != null) {
            sql.append(" AND DATE(o.orderPlacedDateTime) <= ?");
            params.add(Date.valueOf(end));
        }

        if (bakery != null && !bakery.equals("All Bakeries")) {
            sql.append(" AND b.bakeryName = ?");
            params.add(bakery);
        }

        sql.append(" GROUP BY DATE(o.orderPlacedDateTime) ORDER BY day");

        List<DataPoint> data = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                data.add(new DataPoint(
                        rs.getString("day"),
                        rs.getDouble("revenue")
                ));
            }
        }

        return data;
    }

    /* ==============================
       Revenue By Bakery
       ============================== */

    public List<DataPoint> getRevenueByBakery(LocalDate start, LocalDate end) throws SQLException {

        String sql = """
            SELECT b.bakeryName,
                   SUM(o.orderTotal - o.orderDiscount) AS revenue
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            GROUP BY b.bakeryName
            ORDER BY revenue DESC
        """;

        List<DataPoint> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new DataPoint(
                        rs.getString("bakeryName"),
                        rs.getDouble("revenue")
                ));
            }
        }

        return list;
    }

    /* ==============================
       Average Order Value (AOV)
       ============================== */

    public double getAverageOrderValue(LocalDate start, LocalDate end, String bakery) throws SQLException {

        String sql = """
            SELECT IFNULL(AVG(orderTotal - orderDiscount),0) AS aov
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("aov");
            }
        }

        return 0.0;
    }

    public List<DataPoint> getAverageOrderValueOverTime(LocalDate start, LocalDate end, String bakery) throws SQLException {

        String sql = """
            SELECT DATE(o.orderPlacedDateTime) AS day,
                   AVG(o.orderTotal - o.orderDiscount) AS aov
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            GROUP BY DATE(o.orderPlacedDateTime)
            ORDER BY day
        """;

        List<DataPoint> data = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.add(new DataPoint(
                        rs.getString("day"),
                        rs.getDouble("aov")
                ));
            }
        }

        return data;
    }

    /* ==============================
       Completion Rate
       ============================== */

    public double getCompletionRate(LocalDate start, LocalDate end, String bakery) throws SQLException {

        String sql = """
            SELECT 
                IFNULL(
                    SUM(CASE WHEN orderStatus = 'Completed' THEN 1 ELSE 0 END) 
                    / COUNT(*) * 100
                ,0) AS completionRate
            FROM `Order`
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("completionRate");
            }
        }

        return 0.0;
    }

    public List<DataPoint> getCompletionRateOverTime(LocalDate start, LocalDate end, String bakery) throws SQLException {

        String sql = """
            SELECT DATE(orderPlacedDateTime) AS day,
                   SUM(CASE WHEN orderStatus = 'Completed' THEN 1 ELSE 0 END)
                   / COUNT(*) * 100 AS completionRate
            FROM `Order`
            GROUP BY DATE(orderPlacedDateTime)
            ORDER BY day
        """;

        List<DataPoint> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new DataPoint(
                        rs.getString("day"),
                        rs.getDouble("completionRate")
                ));
            }
        }

        return list;
    }

    /* ==============================
       Top Products
       ============================== */

    public List<DataPoint> getTopProducts(LocalDate start, LocalDate end, String bakery) throws SQLException {

        String sql = """
            SELECT p.productName,
                   SUM(oi.quantity) AS totalSold
            FROM OrderItem oi
            JOIN Product p ON oi.productId = p.productId
            JOIN `Order` o ON oi.orderId = o.orderId
            GROUP BY p.productName
            ORDER BY totalSold DESC
            LIMIT 10
        """;

        List<DataPoint> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new DataPoint(
                        rs.getString("productName"),
                        rs.getDouble("totalSold")
                ));
            }
        }

        return list;
    }

    /* ==============================
       Bakery Names
       ============================== */

    public List<String> getBakeryNames() throws SQLException {

        String sql = "SELECT bakeryName FROM Bakery ORDER BY bakeryName";

        List<String> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("bakeryName"));
            }
        }

        return list;
    }
}
