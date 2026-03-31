package com.sait.workshop05.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes EMPLOYEE analytics eligibility and scope.
 *
 * TODO: Replace with API calls once the Spring Boot API exposes an employee-scope endpoint.
 *       Until then these return null/empty so analytics are disabled for employees.
 */
public class EmployeeAccessDAO {

    public static Integer getEmployeeIdByUserId(int userId) {
        // TODO: GET /api/v1/employee/me or similar
        return null;
    }

    public static List<Integer> getAccessibleBakeryIdsByUserId(int userId) {
        // TODO: GET /api/v1/employee/me/bakeries or similar
        return new ArrayList<>();
    }
}
