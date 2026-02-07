package com.sait.workshop05.database;

import com.sait.workshop05.models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer entity.
 * Handles all database operations related to customers.
 */
public class CustomerDAO {

    /**
     * Retrieve all customers from the database
     */
    public List<Customer> getAllCustomers() throws SQLException {
        String sql =
                "SELECT c.customerId, c.userId, c.addressId, c.rewardTierId, " +
                "       c.customerFirstName, c.customerMiddleInitial, c.customerLastName, " +
                "       c.customerRole, c.customerPhone, c.customerBusinessPhone, " +
                "       c.customerEmail, c.customerRewardBalance, c.customerTierAssignedDate, " +
                "       u.userUsername AS userDisplay, " +
                "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince, ' ', a.addressPostalCode) AS addressDisplay, " +
                "       rt.rewardTierName AS rewardTierDisplay " +
                "FROM Customer c " +
                "LEFT JOIN `User` u ON c.userId = u.userId " +
                "JOIN Address a ON c.addressId = a.addressId " +
                "JOIN RewardTier rt ON c.rewardTierId = rt.rewardTierId " +
                "ORDER BY c.customerId DESC";

        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer c = new Customer();
                c.setCustomerId(rs.getInt("customerId"));
                c.setUserId(rs.getInt("userId"));
                c.setAddressId(rs.getInt("addressId"));
                c.setRewardTierId(rs.getInt("rewardTierId"));
                c.setFirstName(rs.getString("customerFirstName"));
                c.setMiddleInitial(rs.getString("customerMiddleInitial"));
                c.setLastName(rs.getString("customerLastName"));
                c.setRole(rs.getString("customerRole"));
                c.setPhone(rs.getString("customerPhone"));
                c.setBusinessPhone(rs.getString("customerBusinessPhone"));
                c.setEmail(rs.getString("customerEmail"));
                c.setRewardBalance(rs.getInt("customerRewardBalance"));

                Timestamp tierDate = rs.getTimestamp("customerTierAssignedDate");
                if (tierDate != null) {
                    c.setTierAssignedDate(tierDate.toLocalDateTime());
                }

                String userDisplay = rs.getString("userDisplay");
                c.setUserDisplay(userDisplay != null ? rs.getInt("userId") + " - " + userDisplay : "No User");
                c.setAddressDisplay(rs.getString("addressDisplay"));
                c.setRewardTierDisplay(rs.getString("rewardTierDisplay"));

                customers.add(c);
            }
        }

        return customers;
    }

    /**
     * Get customer by customer ID
     */
    public Customer getCustomerById(int customerId) throws SQLException {
        String sql =
                "SELECT c.customerId, c.userId, c.addressId, c.rewardTierId, " +
                "       c.customerFirstName, c.customerMiddleInitial, c.customerLastName, " +
                "       c.customerRole, c.customerPhone, c.customerBusinessPhone, " +
                "       c.customerEmail, c.customerRewardBalance, c.customerTierAssignedDate, " +
                "       u.userUsername AS userDisplay, " +
                "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince, ' ', a.addressPostalCode) AS addressDisplay, " +
                "       rt.rewardTierName AS rewardTierDisplay " +
                "FROM Customer c " +
                "LEFT JOIN `User` u ON c.userId = u.userId " +
                "JOIN Address a ON c.addressId = a.addressId " +
                "JOIN RewardTier rt ON c.rewardTierId = rt.rewardTierId " +
                "WHERE c.customerId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setCustomerId(rs.getInt("customerId"));
                    c.setUserId(rs.getInt("userId"));
                    c.setAddressId(rs.getInt("addressId"));
                    c.setRewardTierId(rs.getInt("rewardTierId"));
                    c.setFirstName(rs.getString("customerFirstName"));
                    c.setMiddleInitial(rs.getString("customerMiddleInitial"));
                    c.setLastName(rs.getString("customerLastName"));
                    c.setRole(rs.getString("customerRole"));
                    c.setPhone(rs.getString("customerPhone"));
                    c.setBusinessPhone(rs.getString("customerBusinessPhone"));
                    c.setEmail(rs.getString("customerEmail"));
                    c.setRewardBalance(rs.getInt("customerRewardBalance"));

                    Timestamp tierDate = rs.getTimestamp("customerTierAssignedDate");
                    if (tierDate != null) {
                        c.setTierAssignedDate(tierDate.toLocalDateTime());
                    }

                    String userDisplay = rs.getString("userDisplay");
                    c.setUserDisplay(userDisplay != null ? rs.getInt("userId") + " - " + userDisplay : "No User");
                    c.setAddressDisplay(rs.getString("addressDisplay"));
                    c.setRewardTierDisplay(rs.getString("rewardTierDisplay"));

                    return c;
                }
            }
        }

        return null;
    }

    /**
     * Get customer by user ID
     */
    public Customer getCustomerByUserId(int userId) throws SQLException {
        String sql =
                "SELECT c.customerId, c.userId, c.addressId, c.rewardTierId, " +
                "       c.customerFirstName, c.customerMiddleInitial, c.customerLastName, " +
                "       c.customerRole, c.customerPhone, c.customerBusinessPhone, " +
                "       c.customerEmail, c.customerRewardBalance, c.customerTierAssignedDate, " +
                "       u.userUsername AS userDisplay, " +
                "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince, ' ', a.addressPostalCode) AS addressDisplay, " +
                "       rt.rewardTierName AS rewardTierDisplay " +
                "FROM Customer c " +
                "LEFT JOIN `User` u ON c.userId = u.userId " +
                "JOIN Address a ON c.addressId = a.addressId " +
                "JOIN RewardTier rt ON c.rewardTierId = rt.rewardTierId " +
                "WHERE c.userId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setCustomerId(rs.getInt("customerId"));
                    c.setUserId(rs.getInt("userId"));
                    c.setAddressId(rs.getInt("addressId"));
                    c.setRewardTierId(rs.getInt("rewardTierId"));
                    c.setFirstName(rs.getString("customerFirstName"));
                    c.setMiddleInitial(rs.getString("customerMiddleInitial"));
                    c.setLastName(rs.getString("customerLastName"));
                    c.setRole(rs.getString("customerRole"));
                    c.setPhone(rs.getString("customerPhone"));
                    c.setBusinessPhone(rs.getString("customerBusinessPhone"));
                    c.setEmail(rs.getString("customerEmail"));
                    c.setRewardBalance(rs.getInt("customerRewardBalance"));

                    Timestamp tierDate = rs.getTimestamp("customerTierAssignedDate");
                    if (tierDate != null) {
                        c.setTierAssignedDate(tierDate.toLocalDateTime());
                    }

                    String userDisplay = rs.getString("userDisplay");
                    c.setUserDisplay(userDisplay != null ? rs.getInt("userId") + " - " + userDisplay : "No User");
                    c.setAddressDisplay(rs.getString("addressDisplay"));
                    c.setRewardTierDisplay(rs.getString("rewardTierDisplay"));

                    return c;
                }
            }
        }

        return null;
    }

    /**
     * Insert a new customer
     */
    public int insertCustomer(Customer c) throws SQLException {
        String sql =
                "INSERT INTO Customer " +
                "(userId, addressId, rewardTierId, customerFirstName, customerMiddleInitial, customerLastName, " +
                "customerRole, customerPhone, customerBusinessPhone, customerEmail, customerRewardBalance, customerTierAssignedDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, c.getUserId() > 0 ? c.getUserId() : null);
            ps.setInt(2, c.getAddressId());
            ps.setInt(3, c.getRewardTierId());
            ps.setString(4, c.getFirstName());
            ps.setString(5, emptyToNull(c.getMiddleInitial()));
            ps.setString(6, c.getLastName());
            ps.setString(7, c.getRole());
            ps.setString(8, c.getPhone());
            ps.setString(9, emptyToNull(c.getBusinessPhone()));
            ps.setString(10, c.getEmail());
            ps.setInt(11, c.getRewardBalance());

            if (c.getTierAssignedDate() != null) {
                ps.setTimestamp(12, Timestamp.valueOf(c.getTierAssignedDate()));
            } else {
                ps.setNull(12, Types.TIMESTAMP);
            }

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
     * Update an existing customer
     */
    public boolean updateCustomer(Customer c) throws SQLException {
        String sql =
                "UPDATE Customer SET " +
                "userId=?, addressId=?, rewardTierId=?, customerFirstName=?, customerMiddleInitial=?, customerLastName=?, " +
                "customerRole=?, customerPhone=?, customerBusinessPhone=?, customerEmail=?, customerRewardBalance=?, customerTierAssignedDate=? " +
                "WHERE customerId=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, c.getUserId() > 0 ? c.getUserId() : null);
            ps.setInt(2, c.getAddressId());
            ps.setInt(3, c.getRewardTierId());
            ps.setString(4, c.getFirstName());
            ps.setString(5, emptyToNull(c.getMiddleInitial()));
            ps.setString(6, c.getLastName());
            ps.setString(7, c.getRole());
            ps.setString(8, c.getPhone());
            ps.setString(9, emptyToNull(c.getBusinessPhone()));
            ps.setString(10, c.getEmail());
            ps.setInt(11, c.getRewardBalance());

            if (c.getTierAssignedDate() != null) {
                ps.setTimestamp(12, Timestamp.valueOf(c.getTierAssignedDate()));
            } else {
                ps.setNull(12, Types.TIMESTAMP);
            }

            ps.setInt(13, c.getCustomerId());

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Delete a customer by ID
     */
    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM Customer WHERE customerId=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Search customers by name or email
     */
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        String sql =
                "SELECT c.customerId, c.userId, c.addressId, c.rewardTierId, " +
                "       c.customerFirstName, c.customerMiddleInitial, c.customerLastName, " +
                "       c.customerRole, c.customerPhone, c.customerBusinessPhone, " +
                "       c.customerEmail, c.customerRewardBalance, c.customerTierAssignedDate, " +
                "       u.userUsername AS userDisplay, " +
                "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince, ' ', a.addressPostalCode) AS addressDisplay, " +
                "       rt.rewardTierName AS rewardTierDisplay " +
                "FROM Customer c " +
                "LEFT JOIN `User` u ON c.userId = u.userId " +
                "JOIN Address a ON c.addressId = a.addressId " +
                "JOIN RewardTier rt ON c.rewardTierId = rt.rewardTierId " +
                "WHERE c.customerFirstName LIKE ? OR c.customerLastName LIKE ? OR c.customerEmail LIKE ? " +
                "ORDER BY c.customerId DESC";

        List<Customer> customers = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer c = new Customer();
                    c.setCustomerId(rs.getInt("customerId"));
                    c.setUserId(rs.getInt("userId"));
                    c.setAddressId(rs.getInt("addressId"));
                    c.setRewardTierId(rs.getInt("rewardTierId"));
                    c.setFirstName(rs.getString("customerFirstName"));
                    c.setMiddleInitial(rs.getString("customerMiddleInitial"));
                    c.setLastName(rs.getString("customerLastName"));
                    c.setRole(rs.getString("customerRole"));
                    c.setPhone(rs.getString("customerPhone"));
                    c.setBusinessPhone(rs.getString("customerBusinessPhone"));
                    c.setEmail(rs.getString("customerEmail"));
                    c.setRewardBalance(rs.getInt("customerRewardBalance"));

                    Timestamp tierDate = rs.getTimestamp("customerTierAssignedDate");
                    if (tierDate != null) {
                        c.setTierAssignedDate(tierDate.toLocalDateTime());
                    }

                    String userDisplay = rs.getString("userDisplay");
                    c.setUserDisplay(userDisplay != null ? rs.getInt("userId") + " - " + userDisplay : "No User");
                    c.setAddressDisplay(rs.getString("addressDisplay"));
                    c.setRewardTierDisplay(rs.getString("rewardTierDisplay"));

                    customers.add(c);
                }
            }
        }

        return customers;
    }

    /**
     * Get all reward tier options for ComboBox population.
     */
    public List<RewardTierOption> getRewardTierOptions() throws SQLException {
        String sql = "SELECT rewardTierId, rewardTierName FROM RewardTier ORDER BY rewardTierId ASC";
        List<RewardTierOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new RewardTierOption(
                        rs.getInt("rewardTierId"),
                        rs.getString("rewardTierName")
                ));
            }
        }
        return options;
    }

    /**
     * Get all user options for ComboBox population.
     */
    public List<UserOption> getUserOptions() throws SQLException {
        String sql = "SELECT userId, userUsername FROM `User` ORDER BY userId DESC";
        List<UserOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new UserOption(rs.getInt("userId"), rs.getString("userUsername")));
            }
        }
        return options;
    }

    /**
     * Get all address options for ComboBox population.
     */
    public List<AddressOption> getAddressOptions() throws SQLException {
        String sql =
                "SELECT addressId, addressLine1, addressCity, addressProvince, addressPostalCode " +
                "FROM Address ORDER BY addressId DESC";

        List<AddressOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new AddressOption(
                        rs.getInt("addressId"),
                        rs.getString("addressLine1"),
                        rs.getString("addressCity"),
                        rs.getString("addressProvince"),
                        rs.getString("addressPostalCode")
                ));
            }
        }
        return options;
    }

    /**
     * Update only the reward balance for a customer.
     */
    public boolean updateRewardBalance(int customerId, int newBalance) throws SQLException {
        String sql = "UPDATE Customer SET customerRewardBalance = ? WHERE customerId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newBalance);
            ps.setInt(2, customerId);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Helper method to convert empty strings to null
     */
    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

