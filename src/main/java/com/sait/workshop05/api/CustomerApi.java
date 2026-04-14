package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin customer operations ({@code /api/v1/admin/customers}).
 */
public final class CustomerApi {

    private CustomerApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerRow {
        public String id;
        public String userId;
        /** Login username for the linked account, when present. */
        public String username;
        public Integer rewardTierId;
        public String firstName;
        public String middleInitial;
        public String lastName;
        public String phone;
        public String email;
        public int rewardBalance;
        public Integer addressId;
    }

    public static List<CustomerRow> list() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/customers");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET admin/customers failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<CustomerRow>>() {});
    }

    public static void patch(String customerId, Map<String, Object> body) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().patch("/api/v1/admin/customers/" + customerId, body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("PATCH admin/customers failed: " + res.statusCode() + " " + res.body());
        }
    }

    /** Admin-only: create a linked or guest customer; response body is parsed as {@link CustomerRow}. */
    public static CustomerRow create(Map<String, Object> body) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/admin/customers", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST admin/customers failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), CustomerRow.class);
    }

    public static Map<String, Object> patchBodyForProfile(
            String firstName,
            String middleInitial,
            String lastName,
            String phone,
            String businessPhone,
            String email,
            Integer addressId,
            Integer rewardTierId
    ) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("firstName", firstName);
        m.put("middleInitial", middleInitial);
        m.put("lastName", lastName);
        m.put("phone", phone);
        m.put("businessPhone", businessPhone);
        m.put("email", email);
        m.put("addressId", addressId);
        m.put("rewardTierId", rewardTierId);
        return m;
    }

    public static Map<String, Object> patchRewardBalance(int newBalance) {
        return Map.of("rewardBalance", newBalance);
    }
}
