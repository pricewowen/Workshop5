package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderItem;

/**
 * Dashboard summary from {@code GET /api/v1/admin/dashboard/summary}.
 * Falls back to individual API calls if the summary endpoint returns 5xx.
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
        if (res.statusCode() == 200) {
            return ApiClient.getInstance().getMapper().readValue(res.body(), SummaryResponse.class);
        }
        // 5xx or unexpected status — build summary from individual endpoints
        System.err.println("[DashboardApi] summary endpoint returned " + res.statusCode() + ", using fallback");
        return buildFallbackSummary();
    }

    private static SummaryResponse buildFallbackSummary() throws Exception {
        SummaryResponse s = new SummaryResponse();

        // Orders — list endpoint
        List<OrderApi.OrderJson> allOrderJson = fetchAllOrderJson();
        s.totalOrders = allOrderJson.size();

        // Recent orders — last 10 by placedAt descending
        s.recentOrders = allOrderJson.stream()
                .filter(j -> j.placedAt != null)
                .sorted(Comparator.comparing((OrderApi.OrderJson j) -> j.placedAt).reversed())
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        // Total revenue — sum completed order totals from the already-fetched order list
        s.totalRevenue = allOrderJson.stream()
                .map(j -> {
                    BigDecimal total = j.orderTotal != null ? j.orderTotal : BigDecimal.ZERO;
                    BigDecimal discount = j.orderDiscount != null ? j.orderDiscount : BigDecimal.ZERO;
                    return total.subtract(discount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Customer count
        try {
            HttpResponse<String> cr = ApiClient.getInstance().get("/api/v1/admin/customers");
            if (cr.statusCode() == 200) {
                List<?> list = ApiClient.getInstance().getMapper().readValue(cr.body(), List.class);
                s.totalCustomers = list.size();
            }
        } catch (Exception e) {
            s.totalCustomers = 0;
        }

        // Product count
        try {
            List<CatalogApi.ProductResponse> products = CatalogApi.fetchProducts(null, null);
            s.totalProducts = products.size();
        } catch (Exception e) {
            s.totalProducts = 0;
        }

        return s;
    }

    private static List<OrderApi.OrderJson> fetchAllOrderJson() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/orders");
        if (res.statusCode() >= 400) {
            return new ArrayList<>();
        }
        return ApiClient.getInstance().getMapper().readValue(
                res.body(), new TypeReference<List<OrderApi.OrderJson>>() {});
    }

    public static Order toOrder(OrderApi.OrderJson j) {
        return OrderApi.toOrder(j);
    }

    public static List<OrderItem> itemsFromSummaryOrder(OrderApi.OrderJson j) {
        return OrderApi.itemsFromOrder(j);
    }
}
