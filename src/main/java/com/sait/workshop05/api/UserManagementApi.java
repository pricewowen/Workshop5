package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desktop client for /api/v1/admin/users — list, create, and toggle active state.
 */
public final class UserManagementApi {

    private UserManagementApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserRow {
        public String id;
        public String username;
        public String email;
        public String role;
        public boolean active;
    }

    public static List<UserRow> listUsers() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/users");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("list users failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<UserRow>>() {});
    }

    public static UserRow createUser(String username, String email, String password, String role) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);

        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/admin/users", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("create user failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), UserRow.class);
    }

    public static void setActive(String userId, boolean active) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("active", active);
        HttpResponse<String> res = ApiClient.getInstance().patch(
                "/api/v1/admin/users/" + userId + "/active", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("set active failed: " + res.statusCode() + " " + res.body());
        }
    }
}
