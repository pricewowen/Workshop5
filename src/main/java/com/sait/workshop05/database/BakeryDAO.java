package com.sait.workshop05.database;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Address;
import com.sait.workshop05.models.Bakery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BakeryDAO {
    /**
     * Gets all bakeries
     * @return an ArrayList of all bakeries
     * @throws SQLException if errors occur
     */
    public ArrayList<Bakery> getAllBakeries() throws SQLException {
        String sql = "SELECT b.bakeryId, b.bakeryName, b.bakeryPhone, b.bakeryEmail, " +
                "a.addressLine1, a.addressLine2, a.addressCity, a.addressProvince, " +
                "a.addressPostalCode, a.addressId " +
                "FROM Bakery b JOIN Address a ON b.addressId = a.addressId";

        ArrayList<Bakery> bakeries = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // get address
                Address a = new Address(
                        rs.getInt("addressId"),
                        rs.getString("addressLine1"),
                        rs.getString("addressLine2"),
                        rs.getString("addressCity"),
                        rs.getString("addressProvince"),
                        rs.getString("addressPostalCode")
                );

                // get bakery
                Bakery b = new Bakery(
                        rs.getInt("bakeryId"),
                        a,
                        rs.getString("bakeryName"),
                        rs.getString("bakeryPhone"),
                        rs.getString("bakeryEmail")
                );

                // add to bakery arrayList
                bakeries.add(b);
            }

            return bakeries;
        }
    }

    public void updateBakery(Bakery bakery) throws SQLException {
        String sql = "UPDATE Bakery " +
                "SET bakeryName = ?, " +
                "bakeryPhone = ?, " +
                "bakeryEmail = ? " +
                "WHERE bakeryId = ?";

        String sqlQuery2 = "UPDATE Address " +
                "SET addressLine1 = ?, " +
                "addressLine2 = ?, " +
                "addressCity = ?, " +
                "addressProvince = ?," +
                "addressPostalCode = ? " +
                "WHERE addressId = ?";

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // so doesn't auto update the database util the end

            // update the bakery
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, bakery.getBakeryName());
                stmt.setString(2, bakery.getBakeryPhone());
                stmt.setString(3, bakery.getBakeryEmail());
                stmt.setInt(4, bakery.getBakeryId());
                stmt.executeUpdate();
            }

            // update the address
            Address addr = bakery.getAddress();
            try (PreparedStatement stmt = conn.prepareStatement(sqlQuery2)) {
                stmt.setString(1, addr.getAddressLine1());
                stmt.setString(2, addr.getAddressLine2());
                stmt.setString(3, addr.getAddressCity());
                stmt.setString(4, addr.getAddressProvince());
                stmt.setString(5, addr.getAddressPostalCode());
                stmt.setInt(6, addr.getAddressId());
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // rollback if something fails
                } catch (SQLException rollbackE) {
                    LogData.handleException("Rollback", rollbackE);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LogData.handleException("Closing_Connection", e);
                }
            }
        }
    }
}
