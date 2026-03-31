package com.sait.workshop05.database;

import com.sait.workshop05.models.RewardTier;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RewardTierDAO {

    /**
     * Gets all reward tiers from the database
     */
    public List<RewardTier> getAllRewardTiers() throws SQLException {
        String sql = "SELECT rewardTierId, rewardTierName, rewardTierMinPoints, " +
                "rewardTierMaxPoints, rewardTierDiscountRate " +
                "FROM RewardTier " +
                "ORDER BY rewardTierMinPoints ASC";

        List<RewardTier> tiers = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RewardTier tier = new RewardTier();
                tier.setRewardTierId(rs.getInt("rewardTierId"));
                tier.setRewardTierName(rs.getString("rewardTierName"));
                tier.setRewardTierMinPoints(rs.getInt("rewardTierMinPoints"));

                // Handle NULL for max points
                int maxPoints = rs.getInt("rewardTierMaxPoints");
                if (rs.wasNull()) {
                    tier.setRewardTierMaxPoints(null);
                } else {
                    tier.setRewardTierMaxPoints(maxPoints);
                }

                // Handle NULL for discount rate
                BigDecimal discount = rs.getBigDecimal("rewardTierDiscountRate");
                tier.setRewardTierDiscountRate(discount);

                tiers.add(tier);
            }
        }
        return tiers;
    }

    /**
     * Gets a single reward tier by its id
     *
     */
    public RewardTier getRewardTierById(int id) throws SQLException {
        String sql = "SELECT rewardTierId, rewardTierName, rewardTierMinPoints, " +
                "rewardTierMaxPoints, rewardTierDiscountRate " +
                "FROM RewardTier WHERE rewardTierId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RewardTier tier = new RewardTier();
                    tier.setRewardTierId(rs.getInt("rewardTierId"));
                    tier.setRewardTierName(rs.getString("rewardTierName"));
                    tier.setRewardTierMinPoints(rs.getInt("rewardTierMinPoints"));
                    int maxPoints = rs.getInt("rewardTierMaxPoints");
                    tier.setRewardTierMaxPoints(rs.wasNull() ? null : maxPoints);
                    tier.setRewardTierDiscountRate(rs.getBigDecimal("rewardTierDiscountRate"));
                    return tier;
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new reward tier
     */
    public int insertRewardTier(RewardTier tier) throws SQLException {
        String sql = "INSERT INTO RewardTier (rewardTierName, rewardTierMinPoints, " +
                "rewardTierMaxPoints, rewardTierDiscountRate) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tier.getRewardTierName());
            ps.setInt(2, tier.getRewardTierMinPoints());

            // Handle NULL for max points
            if (tier.getRewardTierMaxPoints() == 0) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, tier.getRewardTierMaxPoints());
            }

            // Handle NULL for discount rate
            if (tier.getRewardTierDiscountRate() == null) {
                ps.setNull(4, Types.DECIMAL);
            } else {
                ps.setBigDecimal(4, tier.getRewardTierDiscountRate());
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Updates an existing reward tier
     */
    public boolean updateRewardTier(RewardTier tier) throws SQLException {
        String sql = "UPDATE RewardTier SET rewardTierName = ?, " +
                "rewardTierMinPoints = ?, rewardTierMaxPoints = ?, " +
                "rewardTierDiscountRate = ? WHERE rewardTierId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tier.getRewardTierName());
            ps.setInt(2, tier.getRewardTierMinPoints());

            // Handle NULL for max points
            if (tier.getRewardTierMaxPoints() == 0) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, tier.getRewardTierMaxPoints());
            }

            // Handle NULL for discount rate
            if (tier.getRewardTierDiscountRate() == null) {
                ps.setNull(4, Types.DECIMAL);
            } else {
                ps.setBigDecimal(4, tier.getRewardTierDiscountRate());
            }

            ps.setInt(5, tier.getRewardTierId());

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Deletes a reward tier
     */
    public boolean deleteRewardTier(int rewardTierId) throws SQLException {
        // First check if any customers are using this tier
        String checkSql = "SELECT COUNT(*) FROM Customer WHERE rewardTierId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, rewardTierId);

            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete a tier that is assigned to customers", "23000");
                }
            }
        }

        String deleteSql = "DELETE FROM RewardTier WHERE rewardTierId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {

            ps.setInt(1, rewardTierId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Get tier options for combo box
     */
    public List<RewardTier> getTierOptions() throws SQLException {
        String sql = "SELECT rewardTierId, rewardTierName, rewardTierMinPoints, " +
                "rewardTierMaxPoints, rewardTierDiscountRate " +
                "FROM RewardTier ORDER BY rewardTierMinPoints ASC";

        List<RewardTier> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RewardTier tier = new RewardTier();
                tier.setRewardTierId(rs.getInt("rewardTierId"));
                tier.setRewardTierName(rs.getString("rewardTierName"));
                tier.setRewardTierMinPoints(rs.getInt("rewardTierMinPoints"));

                int maxPoints = rs.getInt("rewardTierMaxPoints");
                tier.setRewardTierMaxPoints(rs.wasNull() ? null : maxPoints);

                tier.setRewardTierDiscountRate(rs.getBigDecimal("rewardTierDiscountRate"));

                options.add(tier);
            }
        }
        return options;
    }

    /**
     * Validates mix and max range of points for tiers
     */
    public boolean validatePointsRange(int minPoints, Integer maxPoints, Integer excludeId) throws SQLException {
        String sql = "SELECT rewardTierId, rewardTierMinPoints, rewardTierMaxPoints " +
                "FROM RewardTier " +
                "WHERE (rewardTierMinPoints <= ? AND (rewardTierMaxPoints >= ? OR rewardTierMaxPoints IS NULL)) " +
                "OR (rewardTierMinPoints <= ? AND (rewardTierMaxPoints >= ? OR rewardTierMaxPoints IS NULL))";

        if (excludeId != null) {
            sql += " AND rewardTierId != ?";
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int maxValue = (maxPoints == null) ? Integer.MAX_VALUE : maxPoints;

            ps.setInt(1, maxValue);
            ps.setInt(2, minPoints);
            ps.setInt(3, maxValue);
            ps.setInt(4, minPoints);

            if (excludeId != null) {
                ps.setInt(5, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next(); // Returns true if no overlap
            }
        }
    }
}