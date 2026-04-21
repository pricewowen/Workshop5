// Contributor(s): Samantha
// Main: Samantha - Admin CRUD for reward tiers under /api/v1/reward-tiers.

package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin CRUD for reward tier rows at /api/v1/reward-tiers.
 */
public final class RewardTierApi {

    private RewardTierApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RewardTierJson {
        public Integer id;
        public String name;
        public int minPoints;
        public Integer maxPoints;
        public BigDecimal discountRatePercent;
    }

    /**
     * Returns reward tier rows used by loyalty administration screens.
     */
    public static List<RewardTierJson> list() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/reward-tiers");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET reward-tiers failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<RewardTierJson>>() {});
    }

    /**
     * Creates one reward tier and returns the stored row.
     */
    public static RewardTierJson create(String name, int minPoints, Integer maxPoints, BigDecimal discountPct) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("minPoints", minPoints);
        body.put("maxPoints", maxPoints);
        body.put("discountRatePercent", discountPct);
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/reward-tiers", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST reward-tiers failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), RewardTierJson.class);
    }

    /**
     * Replaces one reward tier by id and returns the updated row.
     */
    public static RewardTierJson update(int id, String name, int minPoints, Integer maxPoints, BigDecimal discountPct) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("minPoints", minPoints);
        body.put("maxPoints", maxPoints);
        body.put("discountRatePercent", discountPct);
        HttpResponse<String> res = ApiClient.getInstance().put("/api/v1/reward-tiers/" + id, body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("PUT reward-tiers failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), RewardTierJson.class);
    }

    /**
     * Deletes one reward tier by id.
     */
    public static void delete(int id) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().delete("/api/v1/reward-tiers/" + id);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("DELETE reward-tiers failed: " + res.statusCode() + " " + res.body());
        }
    }
}
