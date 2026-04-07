package com.sait.workshop05.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sait.workshop05.analytics.DataPoint;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin analytics backed by the Spring API (JWT scope is enforced server-side).
 */
public final class AnalyticsApi {

    private AnalyticsApi() {}

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String baseQuery(LocalDate start, LocalDate end, String bakerySelection) {
        String q = "?start=" + start + "&end=" + end;
        if (bakerySelection != null && !bakerySelection.isBlank()) {
            q += "&bakerySelection=" + enc(bakerySelection);
        }
        return q;
    }

    private static double readBigDecimalNumber(String body, ObjectMapper mapper) throws Exception {
        return mapper.readValue(body, BigDecimal.class).doubleValue();
    }

    public static double getTotalRevenue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        String path = "/api/v1/admin/analytics/metrics/total-revenue" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("total-revenue failed: " + res.statusCode() + " " + res.body());
        }
        return readBigDecimalNumber(res.body(), mapper);
    }

    public static List<DataPoint> getRevenueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return getDataPointSeries("/api/v1/admin/analytics/revenue-over-time" + baseQuery(start, end, bakerySelection));
    }

    public static List<DataPoint> getRevenueByBakery(LocalDate start, LocalDate end) throws Exception {
        String path = "/api/v1/admin/analytics/revenue-by-bakery?start=" + start + "&end=" + end;
        return getDataPointSeries(path);
    }

    public static double getAverageOrderValue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        String path = "/api/v1/admin/analytics/metrics/average-order-value" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("average-order-value failed: " + res.statusCode() + " " + res.body());
        }
        return readBigDecimalNumber(res.body(), mapper);
    }

    public static List<DataPoint> getAverageOrderValueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return getDataPointSeries("/api/v1/admin/analytics/series/average-order-value-over-time" + baseQuery(start, end, bakerySelection));
    }

    public static double getCompletionRate(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        String path = "/api/v1/admin/analytics/metrics/completion-rate" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("completion-rate failed: " + res.statusCode() + " " + res.body());
        }
        return readBigDecimalNumber(res.body(), mapper);
    }

    public static List<DataPoint> getCompletionRateOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return getDataPointSeries("/api/v1/admin/analytics/series/completion-rate-over-time" + baseQuery(start, end, bakerySelection));
    }

    public static List<DataPoint> getTopProducts(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return getDataPointSeries("/api/v1/admin/analytics/series/top-products" + baseQuery(start, end, bakerySelection));
    }

    public static double getTotalSalesByEmployee(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        String path = "/api/v1/admin/analytics/metrics/sales-by-employee-total" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("sales-by-employee-total failed: " + res.statusCode() + " " + res.body());
        }
        return readBigDecimalNumber(res.body(), mapper);
    }

    public static List<DataPoint> getSalesByEmployee(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return getDataPointSeries("/api/v1/admin/analytics/series/sales-by-employee" + baseQuery(start, end, bakerySelection));
    }

    public static List<String> getBakeryNames() throws Exception {
        var res = ApiClient.getInstance().get("/api/v1/admin/analytics/meta/bakery-names");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("bakery-names failed: " + res.statusCode() + " " + res.body());
        }
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        return mapper.readValue(res.body(), type);
    }

    public static List<String> getBakeryNamesByIds(List<Integer> bakeryIds) throws Exception {
        // Server already scopes bakery names for employees; admin sees all.
        return getBakeryNames();
    }

    public static List<LocalDate> getAvailableOrderDates(String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        LocalDate start = LocalDate.of(1970, 1, 1);
        LocalDate end = LocalDate.of(2100, 1, 1);
        String path = "/api/v1/admin/analytics/meta/order-dates?start=" + start + "&end=" + end;
        if (bakerySelection != null && !bakerySelection.isBlank()) {
            path += "&bakerySelection=" + enc(bakerySelection);
        }
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("order-dates failed: " + res.statusCode() + " " + res.body());
        }
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        List<String> raw = mapper.readValue(res.body(), new TypeReference<List<String>>() {});
        List<LocalDate> out = new ArrayList<>();
        for (String s : raw) {
            if (s != null && !s.isBlank()) {
                out.add(LocalDate.parse(s));
            }
        }
        return out;
    }

    // ── In-progress equivalents ──────────────────────────────────────────────
    // These hit /in-progress-* endpoints on the API; gracefully return 0 / empty
    // if the server returns anything other than 200 (endpoint not yet implemented).

    public static double getInProgressRevenue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/metrics/in-progress-revenue" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return 0.0;
        return readBigDecimalNumber(res.body(), ApiClient.getInstance().getMapper());
    }

    public static double getInProgressAverageOrderValue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/metrics/in-progress-average-order-value" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return 0.0;
        return readBigDecimalNumber(res.body(), ApiClient.getInstance().getMapper());
    }

    public static double getInProgressRate(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/metrics/in-progress-rate" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return 0.0;
        return readBigDecimalNumber(res.body(), ApiClient.getInstance().getMapper());
    }

    public static double getInProgressTotalSalesByEmployee(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/metrics/in-progress-sales-by-employee-total" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return 0.0;
        return readBigDecimalNumber(res.body(), ApiClient.getInstance().getMapper());
    }

    public static List<DataPoint> getInProgressRevenueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/series/in-progress-revenue-over-time" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return new ArrayList<>();
        return parseDataPointSeries(res.body());
    }

    public static List<DataPoint> getInProgressRevenueByBakery(LocalDate start, LocalDate end) throws Exception {
        String path = "/api/v1/admin/analytics/series/in-progress-revenue-by-bakery?start=" + start + "&end=" + end;
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return new ArrayList<>();
        return parseDataPointSeries(res.body());
    }

    public static List<DataPoint> getInProgressAverageOrderValueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/series/in-progress-average-order-value-over-time" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return new ArrayList<>();
        return parseDataPointSeries(res.body());
    }

    public static List<DataPoint> getInProgressRateOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/series/in-progress-rate-over-time" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return new ArrayList<>();
        return parseDataPointSeries(res.body());
    }

    public static List<DataPoint> getInProgressTopProducts(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/series/in-progress-top-products" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return new ArrayList<>();
        return parseDataPointSeries(res.body());
    }

    public static List<DataPoint> getInProgressSalesByEmployee(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        String path = "/api/v1/admin/analytics/series/in-progress-sales-by-employee" + baseQuery(start, end, bakerySelection);
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() != 200) return new ArrayList<>();
        return parseDataPointSeries(res.body());
    }

    private static List<DataPoint> parseDataPointSeries(String body) throws Exception {
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Dp.class);
        List<Dp> rows = mapper.readValue(body, type);
        List<DataPoint> out = new ArrayList<>();
        for (Dp r : rows) {
            out.add(new DataPoint(r.label, r.value != null ? r.value.doubleValue() : 0.0));
        }
        return out;
    }

    private static List<DataPoint> getDataPointSeries(String path) throws Exception {
        var res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET " + path + " failed: " + res.statusCode() + " " + res.body());
        }
        return parseDataPointSeries(res.body());
    }

    @SuppressWarnings("unused")
    private static class Dp {
        public String label;
        public BigDecimal value;
    }
}
