package com.sait.workshop05.database;

import com.sait.workshop05.models.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public List<Employee> getAllEmployees() throws SQLException {
        String sql =
                "SELECT e.employeeId, e.userId, e.addressId, " +
                        "       e.employeeFirstName, e.employeeMiddleInitial, e.employeeLastName, " +
                        "       e.employeeRole, e.employeePhone, e.employeeBusinessPhone, e.employeeEmail, " +
                        "       u.userUsername AS userDisplay, " +
                        "       CONCAT(a.addressLine1, ', ', IFNULL(a.addressCity,''), ' ', a.addressProvince, ' ', a.addressPostalCode) AS addressDisplay " +
                        "FROM Employee e " +
                        "JOIN `User` u ON e.userId = u.userId " +
                        "JOIN Address a ON e.addressId = a.addressId " +
                        "ORDER BY e.employeeId DESC";

        List<Employee> employees = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Employee e = new Employee();
                e.setEmployeeId(rs.getInt("employeeId"));
                e.setUserId(rs.getInt("userId"));
                e.setAddressId(rs.getInt("addressId"));
                e.setEmployeeFirstName(rs.getString("employeeFirstName"));
                e.setEmployeeMiddleInitial(rs.getString("employeeMiddleInitial"));
                e.setEmployeeLastName(rs.getString("employeeLastName"));
                e.setEmployeeRole(rs.getString("employeeRole"));
                e.setEmployeePhone(rs.getString("employeePhone"));
                e.setEmployeeBusinessPhone(rs.getString("employeeBusinessPhone"));
                e.setEmployeeEmail(rs.getString("employeeEmail"));
                e.setUserDisplay(rs.getInt("userId") + " - " + rs.getString("userDisplay"));
                e.setAddressDisplay(rs.getString("addressDisplay"));
                employees.add(e);
            }
        }

        return employees;
    }

    public int insertEmployee(Employee e) throws SQLException {
        String sql =
                "INSERT INTO Employee " +
                        "(userId, addressId, employeeFirstName, employeeMiddleInitial, employeeLastName, employeeRole, employeePhone, employeeBusinessPhone, employeeEmail) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, e.getUserId());
            ps.setInt(2, e.getAddressId());
            ps.setString(3, e.getEmployeeFirstName());
            ps.setString(4, emptyToNull(e.getEmployeeMiddleInitial()));
            ps.setString(5, e.getEmployeeLastName());
            ps.setString(6, e.getEmployeeRole());
            ps.setString(7, e.getEmployeePhone());
            ps.setString(8, emptyToNull(e.getEmployeeBusinessPhone()));
            ps.setString(9, e.getEmployeeEmail());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    public boolean updateEmployee(Employee e) throws SQLException {
        String sql =
                "UPDATE Employee SET " +
                        "userId=?, addressId=?, employeeFirstName=?, employeeMiddleInitial=?, employeeLastName=?, " +
                        "employeeRole=?, employeePhone=?, employeeBusinessPhone=?, employeeEmail=? " +
                        "WHERE employeeId=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, e.getUserId());
            ps.setInt(2, e.getAddressId());
            ps.setString(3, e.getEmployeeFirstName());
            ps.setString(4, emptyToNull(e.getEmployeeMiddleInitial()));
            ps.setString(5, e.getEmployeeLastName());
            ps.setString(6, e.getEmployeeRole());
            ps.setString(7, e.getEmployeePhone());
            ps.setString(8, emptyToNull(e.getEmployeeBusinessPhone()));
            ps.setString(9, e.getEmployeeEmail());
            ps.setInt(10, e.getEmployeeId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM Employee WHERE employeeId=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            return ps.executeUpdate() == 1;
        }
    }

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

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
