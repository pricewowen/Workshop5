package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.sait.workshop05.models.Reward;

public final class RewardApi {

    private RewardApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RewardJson {
        public String id;
        public String customerId;
        public String orderId;
        public int pointsEarned;
        public String transactionDate;
    }

    public static List<Reward> listAll() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/rewards");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("rewards failed: " + res.statusCode() + " " + res.body());
        }
        List<RewardJson> rows = ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<RewardJson>>() {});
        List<Reward> out = new ArrayList<>();
        for (RewardJson j : rows) {
            Reward r = new Reward();
            r.setRewardId(j.id);
            r.setCustomerId(j.customerId != null ? j.customerId : "");
            r.setOrderId(j.orderId != null ? j.orderId : "");
            r.setRewardPointsEarned(j.pointsEarned);
            r.setRewardTransactionDate(parseTs(j.transactionDate));
            r.setCustomerDisplay(j.customerId != null ? j.customerId : "");
            r.setOrderDisplay(j.orderId != null ? j.orderId : "");
            out.add(r);
        }
        return out;
    }

    private static LocalDateTime parseTs(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return OffsetDateTime.parse(s).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(s);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
