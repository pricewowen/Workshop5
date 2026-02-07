//package com.sait.workshop05.database;
//
//import com.sait.workshop05.models.Address;
//import com.sait.workshop05.models.Bakery;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class BakeryDAO {
//    public List<Bakery> getAllBakeries() throws SQLException {
//        String sql = "SELECT b.bakeryId, b.bakeryName, b.bakeryPhone, b.bakeryEmail" +
//                " a.addressLine1, a.addressLine2, a.addressCity, a.addressProvince" +
//                " a.addressPostalCode" +
//                " FROM bakery b JOIN Address a ON e.addressId = a.addressId";
//
//        ArrayList<Bakery> bakeries = new ArrayList<Bakery>();
//
////        try {
////            Connection conn = DBUtil.getConnection();
////            PreparedStatement stmt = conn.prepareStatement(sql);
////            ResultSet rs = stmt.executeQuery();
////
////            while (rs.next()) {
////                Address a = new Address();
////                a.getAddressLine1(rs.getString("addressLine1")),
////                a.getAddressLine2(rs.getString("addressLine2")),
////
////            }
////        }
//    }
//}
