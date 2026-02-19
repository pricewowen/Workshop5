package com.sait.workshop05.database;

import com.sait.workshop05.analytics.DataPoint;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDAO {

    private static final String VALID_STATUS = "('Completed','Delivered')";

    public double getTotalRevenue(LocalDate start, LocalDate end, String bakery) {

        StringBuilder sql = new StringBuilder(
                "SELECT IFNULL(SUM(orderTotal),0) AS revenue " +
                "FROM `Order` WHERE orderStatus IN " + VALID_STATUS
        );

        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND DATE(orderPlacedDateTime) >= ?");
            params.add(Date.valueOf(start));
        }

        if (end != null) {
            sql.append(" AND DATE(orderPlacedDateTime) <= ?");
            params.add(Date.valueOf(end));
        }

        if (bakery != null && !bakery.equals("All Bakeries")) {
            sql.append(" AND bakeryId = ?");
            params.add(Integer.parseInt(bakery));
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("revenue");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public List<DataPoint> getRevenueOverTime(LocalDate start, LocalDate end, String bakery) {

        StringBuilder sql = new StringBuilder(
                "SELECT DATE(orderPlacedDateTime) AS day, " +
                "       SUM(orderTotal) AS revenue " +
                "FROM `Order` WHERE orderStatus IN " + VALID_STATUS
        );

        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append(" AND DATE(orderPlacedDateTime) >= ?");
            params.add(Date.valueOf(start));
        }

        if (end != null) {
            sql.append(" AND DATE(orderPlacedDateTime) <= ?");
            params.add(Date.valueOf(end));
        }

        if (bakery != null && !bakery.equals("All Bakeries")) {
            sql.append(" AND bakeryId = ?");
            params.add(Integer.parseInt(bakery));
        }

        sql.append(" GROUP BY DATE(orderPlacedDateTime) ");
        sql.append(" ORDER BY day ASC ");

        List<DataPoint> results = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getDate("day").toString();
                    double value = rs.getDouble("revenue");
                    results.add(new DataPoint(label, value));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
}
