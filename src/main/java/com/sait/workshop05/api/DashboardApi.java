package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.List;

import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderItem;

/**
 * Dashboard summary from {@code GET /api/v1/admin/dashboard/summary}.
 */
public final class DashboardApi {

    private DashboardApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SummaryResponse {
        public BigDecimal totalRevenue;
        public long totalOrders;
        public long totalCustomers;
        public long totalProducts;
        public List<OrderApi.OrderJson> recentOrders;
    }

    public static SummaryResponse fetchSummary() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/dashboard/summary");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("dashboard summary failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), SummaryResponse.class);
    }

    public static Order toOrder(OrderApi.OrderJson j) {
        return OrderApi.toOrder(j);
    }

    public static List<OrderItem> itemsFromSummaryOrder(OrderApi.OrderJson j) {
        return OrderApi.itemsFromOrder(j);
    }
}
