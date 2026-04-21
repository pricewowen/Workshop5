// Contributor(s): Robbie
// Main: Robbie - Dashboard summary endpoint with fallback aggregation.

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
 * Dashboard summary from GET /api/v1/admin/dashboard/summary.
 * On non-success status builds totals from orders customers and products calls instead.
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

    /**
     * Loads dashboard summary metrics and falls back to local aggregation on failure.
     */
    public static SummaryResponse fetchSummary() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/dashboard/summary");
        if (res.statusCode() == 200) {
            return ApiClient.getInstance().getMapper().readValue(res.body(), SummaryResponse.class);
        }
        // Fallback keeps dashboard cards populated when summary aggregation is unavailable.
        System.err.println("[DashboardApi] summary endpoint returned " + res.statusCode() + ", using fallback");
        return buildFallbackSummary();
    }

    private static SummaryResponse buildFallbackSummary() throws Exception {
        SummaryResponse s = new SummaryResponse();

        List<OrderApi.OrderJson> allOrderJson = fetchAllOrderJson();
        s.totalOrders = allOrderJson.size();

        s.recentOrders = allOrderJson.stream()
                .filter(j -> j.placedAt != null)
                .sorted(Comparator.comparing((OrderApi.OrderJson j) -> j.placedAt).reversed())
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        s.totalRevenue = allOrderJson.stream()
                .map(j -> {
                    BigDecimal total = j.orderTotal != null ? j.orderTotal : BigDecimal.ZERO;
                    BigDecimal discount = j.orderDiscount != null ? j.orderDiscount : BigDecimal.ZERO;
                    return total.subtract(discount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Customer total uses admin list size to match what staff can access.
        try {
            HttpResponse<String> cr = ApiClient.getInstance().get("/api/v1/admin/customers");
            if (cr.statusCode() == 200) {
                List<?> list = ApiClient.getInstance().getMapper().readValue(cr.body(), List.class);
                s.totalCustomers = list.size();
            }
        } catch (Exception e) {
            s.totalCustomers = 0;
        }

        // Product total is derived from catalog rows shown in management flows.
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

    /**
     * Maps one dashboard order payload into the shared Order model.
     */
    public static Order toOrder(OrderApi.OrderJson j) {
        return OrderApi.toOrder(j);
    }

    /**
     * Maps embedded dashboard line items into OrderItem rows.
     */
    public static List<OrderItem> itemsFromSummaryOrder(OrderApi.OrderJson j) {
        return OrderApi.itemsFromOrder(j);
    }
}
