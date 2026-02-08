package com.sait.workshop05.database;

import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.models.UserOption;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared DAO utilities to eliminate duplication across individual DAOs.
 */
public class SharedDAO {

    /** Get all users for ComboBox dropdowns. Used by Employee and Customer DAOs. */
    public static List<UserOption> getUserOptions() throws SQLException {
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

    /** Get all addresses for ComboBox dropdowns. Used by Employee, Customer, and Order DAOs. */
    public static List<AddressOption> getAddressOptions() throws SQLException {
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

    /** Convert empty/whitespace string to null for DB insertion. */
    public static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
