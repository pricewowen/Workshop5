package com.sait.workshop05.database;

import com.sait.workshop05.models.Address;
import com.sait.workshop05.models.Bakery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BakeryDAO {
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
}
