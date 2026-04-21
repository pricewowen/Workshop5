// Contributor(s): Owen
// Main: Owen - Employee staff list and admin employee create update delete.

package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Staff list from /api/v1/employee/staff and admin writes under /api/v1/admin/employees.
 */
public final class EmployeeApi {

    private EmployeeApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmployeeRow {
        public String id;
        public String userId;
        public Integer bakeryId;
        public String firstName;
        public String middleInitial;
        public String lastName;
        public String position;
        public String phone;
        public String workEmail;
        public Integer addressId;
    }

    /**
     * Returns staff rows used by employee management tables.
     */
    public static List<EmployeeRow> listStaff() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/employee/staff");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET employee/staff failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<EmployeeRow>>() {});
    }

    /**
     * Creates one employee profile linked to an existing user account.
     */
    public static void create(
            String userId,
            int bakeryId,
            int addressId,
            String firstName,
            String middleInitial,
            String lastName,
            String position,
            String phone,
            String businessPhone,
            String workEmail
    ) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", userId);
        body.put("bakeryId", bakeryId);
        body.put("addressId", addressId);
        body.put("firstName", firstName);
        body.put("middleInitial", middleInitial != null && !middleInitial.isBlank() ? middleInitial : null);
        body.put("lastName", lastName);
        body.put("position", position);
        body.put("phone", phone);
        body.put("businessPhone", businessPhone != null && !businessPhone.isBlank() ? businessPhone : null);
        body.put("workEmail", workEmail);
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/admin/employees", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST admin/employees failed: " + res.statusCode() + " " + res.body());
        }
    }

    /**
     * Updates one employee profile by employee id.
     */
    public static void update(
            String employeeId,
            String userId,
            int bakeryId,
            int addressId,
            String firstName,
            String middleInitial,
            String lastName,
            String position,
            String phone,
            String businessPhone,
            String workEmail
    ) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", userId);
        body.put("bakeryId", bakeryId);
        body.put("addressId", addressId);
        body.put("firstName", firstName);
        body.put("middleInitial", middleInitial != null && !middleInitial.isBlank() ? middleInitial : null);
        body.put("lastName", lastName);
        body.put("position", position);
        body.put("phone", phone);
        body.put("businessPhone", businessPhone != null && !businessPhone.isBlank() ? businessPhone : null);
        body.put("workEmail", workEmail);
        HttpResponse<String> res = ApiClient.getInstance().put("/api/v1/admin/employees/" + employeeId, body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("PUT admin/employees failed: " + res.statusCode() + " " + res.body());
        }
    }

    /**
     * Deletes one employee profile by id.
     */
    public static void delete(String employeeId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().delete("/api/v1/admin/employees/" + employeeId);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("DELETE admin/employees failed: " + res.statusCode() + " " + res.body());
        }
    }
}
