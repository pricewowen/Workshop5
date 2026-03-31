package com.sait.workshop05.database;

import com.sait.workshop05.models.CustomerOption;
import com.sait.workshop05.models.OrderOption;
import com.sait.workshop05.models.Reward;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RewardDAO {
    /**
     * Gets all the reward transactions from the database
     */
    public List<Reward> getAllRewards() throws SQLException {
        String sql =
                "SELECT r.rewardId, r.customerId, r.orderId, r.rewardPointsEarned, r.rewardTransactionDate, " +
                        "       CONCAT(c.customerFirstName, ' ', c.customerLastName, ' (#', c.customerId, ')') AS customerDisplay, " +
                        "       CONCAT('Order #', o.orderId, ' - $', FORMAT(o.orderTotal, 2)) AS orderDisplay " +
                        "FROM Reward r " +
                        "JOIN Customer c ON r.customerId = c.customerId " +
                        "JOIN `Order` o ON r.orderId = o.orderId " +
                        "ORDER BY r.rewardTransactionDate DESC";

        List<Reward> rewards = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reward r = new Reward();
                r.setRewardId(rs.getInt("rewardId"));
                r.setCustomerId(rs.getInt("customerId"));
                r.setOrderId(rs.getInt("orderId"));
                r.setRewardPointsEarned(rs.getInt("rewardPointsEarned"));
                r.setRewardTransactionDate(rs.getTimestamp("rewardTransactionDate").toLocalDateTime());
                r.setCustomerDisplay(rs.getString("customerDisplay"));
                r.setOrderDisplay(rs.getString("orderDisplay"));
                rewards.add(r);
            }
        }
        return rewards;
    }
    /**
     * Inserts a new reward
     */
    public int insertReward(Reward r) throws SQLException {
        String sql =
                "INSERT INTO Reward (customerId, orderId, rewardPointsEarned, rewardTransactionDate) " +
                        "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getCustomerId());
            ps.setInt(2, r.getOrderId());
            ps.setInt(3, r.getRewardPointsEarned());
            ps.setTimestamp(4, Timestamp.valueOf(r.getRewardTransactionDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }
    /**
     * Updates and existing reward
     */
    public boolean updateReward(Reward r) throws SQLException {
        String sql =
                "UPDATE Reward SET (customerId=?, orderId=?, rewardPointsEarned=?, rewardTransactionDate=?) WHERE rewardId=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getCustomerId());
            ps.setInt(2, r.getOrderId());
            ps.setInt(3, r.getRewardPointsEarned());
            ps.setTimestamp(4, Timestamp.valueOf(r.getRewardTransactionDate()));
            ps.setInt(5, r.getRewardId());
            return ps.executeUpdate() == 1;
        }
    }
    /**
     * Deletes a reward
     */
    public boolean deleteReward(int rewardId) throws SQLException {
        String sql = "DELETE FROM Reward WHERE rewardId=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rewardId);
            return ps.executeUpdate() == 1;
        }
    }
    /**
     * Gets a list of customers who have made an order
     */
    public List<CustomerOption> getCustomerOptions() throws SQLException {
        String sql =
                "SELECT customerId, customerFirstName, customerLastName, customerRewardBalance " +
                        "FROM Customer ORDER BY customerId DESC";
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
     * Gets a list of all recorded orders
     */
    public List<OrderOption> getOrderOptions() throws SQLException {
        String sql =
                "SELECT orderId, CONCAT('Order #', orderId, ' - $', FORMAT(orderTotal, 2), ' - ', orderStatus) AS orderInfo " +
                        "FROM `Order` ORDER BY orderId DESC";
        List<OrderOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                options.add(new OrderOption(rs.getInt("orderId"), rs.getString("orderInfo")));
            }
        }
        return options;
    }
    /*
     * TODO
     * Implement updating to combo boxes depending on the customer or order selected
     */
}