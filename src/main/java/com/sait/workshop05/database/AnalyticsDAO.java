// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.database;

import com.sait.workshop05.analytics.DataPoint;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDAO {

    /* =========================================================
       Shared Helper
       ========================================================= */

    private void applyFilters(StringBuilder sql,
                              List<Object> params,
                              LocalDate start,
                              LocalDate end,
                              String bakery) {

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
    }

    /* =========================================================
       Revenue
       ========================================================= */

    public double getTotalRevenue(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT IFNULL(SUM(o.orderTotal - o.orderDiscount),0) AS revenue
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE o.orderStatus = 'Completed'
        """);

        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, start, end, bakery);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("revenue") : 0.0;
        }
    }

    public List<DataPoint> getRevenueOverTime(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT DATE(o.orderPlacedDateTime) AS day,
                   SUM(o.orderTotal - o.orderDiscount) AS revenue
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE o.orderStatus = 'Completed'
        """);

        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, start, end, bakery);

        sql.append("""
            GROUP BY DATE(o.orderPlacedDateTime)
            ORDER BY day
        """);

        return executeDataPointQuery(sql, params, "day", "revenue");
    }

    public List<DataPoint> getRevenueByBakery(LocalDate start, LocalDate end) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT b.bakeryName,
                   SUM(o.orderTotal - o.orderDiscount) AS revenue
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE o.orderStatus = 'Completed'
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

        sql.append("""
            GROUP BY b.bakeryName
            ORDER BY revenue DESC
        """);

        return executeDataPointQuery(sql, params, "bakeryName", "revenue");
    }

    /* =========================================================
       AOV
       ========================================================= */

    public double getAverageOrderValue(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT IFNULL(AVG(o.orderTotal - o.orderDiscount),0) AS aov
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE o.orderStatus = 'Completed'
        """);

        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, start, end, bakery);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("aov") : 0.0;
        }
    }

    public List<DataPoint> getAverageOrderValueOverTime(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT DATE(o.orderPlacedDateTime) AS day,
                   AVG(o.orderTotal - o.orderDiscount) AS aov
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE o.orderStatus = 'Completed'
        """);

        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, start, end, bakery);

        sql.append("""
            GROUP BY DATE(o.orderPlacedDateTime)
            ORDER BY day
        """);

        return executeDataPointQuery(sql, params, "day", "aov");
    }

    /* =========================================================
       Completion Rate
       ========================================================= */

    public double getCompletionRate(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT IFNULL(
                (SUM(CASE WHEN o.orderStatus = 'Completed' THEN 1 ELSE 0 END)
                 / COUNT(*) * 100.0)
            ,0) AS completionRate
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, start, end, bakery);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("completionRate") : 0.0;
        }
    }

    public List<DataPoint> getCompletionRateOverTime(LocalDate start, LocalDate end, String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT DATE(o.orderPlacedDateTime) AS day,
                   (SUM(CASE WHEN o.orderStatus = 'Completed' THEN 1 ELSE 0 END)
                    / COUNT(*) * 100.0) AS completionRate
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, start, end, bakery);

        sql.append("""
            GROUP BY DATE(o.orderPlacedDateTime)
            ORDER BY day
        """);

        return executeDataPointQuery(sql, params, "day", "completionRate");
    }

    /* =========================================================
       Top Products
       ========================================================= */

    public List<DataPoint> getTopProducts(LocalDate start,
                                          LocalDate end,
                                          String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT p.productName,
                   SUM(oi.orderItemQuantity) AS totalSold
            FROM OrderItem oi
            JOIN Product p ON oi.productId = p.productId
            JOIN `Order` o ON oi.orderId = o.orderId
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE o.orderStatus = 'Completed'
        """);

        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, start, end, bakery);

        sql.append("""
            GROUP BY p.productName
            ORDER BY totalSold DESC
            LIMIT 10
        """);

        return executeDataPointQuery(sql, params, "productName", "totalSold");
    }

    /* =========================================================
       Shared DataPoint Executor
       ========================================================= */

    private List<DataPoint> executeDataPointQuery(StringBuilder sql,
                                                  List<Object> params,
                                                  String labelColumn,
                                                  String valueColumn) throws SQLException {

        List<DataPoint> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new DataPoint(
                        rs.getString(labelColumn),
                        rs.getDouble(valueColumn)
                ));
            }
        }

        return list;
    }

    /* =========================================================
       Bakery Names
       ========================================================= */

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
    
    public List<LocalDate> getAvailableOrderDates(String bakery) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT DATE(o.orderPlacedDateTime) AS orderDate
            FROM `Order` o
            JOIN Bakery b ON o.bakeryId = b.bakeryId
            WHERE o.orderStatus = 'Completed'
        """);

        List<Object> params = new ArrayList<>();

        if (bakery != null && !bakery.equals("All Bakeries")) {
            sql.append(" AND b.bakeryName = ?");
            params.add(bakery);
        }

        sql.append(" ORDER BY orderDate");

        List<LocalDate> dates = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                dates.add(rs.getDate("orderDate").toLocalDate());
            }
        }

        return dates;
    }
}
