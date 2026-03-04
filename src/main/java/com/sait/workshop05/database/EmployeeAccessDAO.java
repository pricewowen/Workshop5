// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes EMPLOYEE analytics eligibility and scope.
 *
 * In THIS schema, there is no EmployeeBakery mapping table.
 * So "where an employee works" is inferred from Batch rows:
 *   Batch.employeeId + Batch.bakeryId
 */
public class EmployeeAccessDAO {

    /**
     * Get employeeId for a given userId.
     * Returns null if this user is not linked to an Employee row.
     */
    public static Integer getEmployeeIdByUserId(int userId) {
        String sql = """
                SELECT e.employeeId
                FROM Employee e
                WHERE e.userId = ?
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("employeeId");
                }
            }

        } catch (Exception e) {
            System.err.println("Error getEmployeeIdByUserId: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Accessible bakery scope for analytics, inferred from production history.
     *
     * Returns distinct bakeryIds from Batch rows for the employee.
     * Empty list means "no bakery scope" (generic employee / no production yet).
     */
    public static List<Integer> getAccessibleBakeryIdsByUserId(int userId) {
        List<Integer> bakeryIds = new ArrayList<>();

        Integer employeeId = getEmployeeIdByUserId(userId);
        if (employeeId == null) {
            return bakeryIds;
        }

        String sql = """
                SELECT DISTINCT b.bakeryId
                FROM Batch b
                WHERE b.employeeId = ?
                ORDER BY b.bakeryId
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bakeryIds.add(rs.getInt("bakeryId"));
                }
            }

        } catch (Exception e) {
            System.err.println("Error getAccessibleBakeryIdsByUserId: " + e.getMessage());
            e.printStackTrace();
        }

        return bakeryIds;
    }
}