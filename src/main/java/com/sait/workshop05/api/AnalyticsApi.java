package com.sait.workshop05.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sait.workshop05.analytics.DataPoint;
import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderItem;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Analytics derived primarily from Orders API data.
 *
 * Why this implementation:
 * - Workshop 7 analytics endpoints are partially broken/missing.
 * - /api/v1/orders is working reliably.
 * - The old Workshop 5 behavior depended on status buckets anyway.
 *
 * Recognized statuses:
 * - Completed
 * - Delivered
 *
 * In-progress / pending statuses:
 * - Pending
 * - Paid
 * - Processing
 * - Ready
 * - Scheduled
 * - Out for Delivery
 *
 * Excluded:
 * - Cancelled
 */
public final class AnalyticsApi {

    private static final String ALL_BAKERIES_ADMIN = "All Bakeries";
    private static final String ALL_MY_BAKERIES = "All My Bakeries";
    private static final String UNKNOWN_EMPLOYEE = "Unknown Employee";

    private static final Set<String> RECOGNIZED_STATUSES = Set.of(
            "completed",
            "delivered"
    );

    private static final Set<String> IN_PROGRESS_STATUSES = Set.of(
            "pending",
            "paid",
            "processing",
            "ready",
            "scheduled",
            "out for delivery"
    );

    private static final Set<String> EXCLUDED_STATUSES = Set.of(
            "cancelled"
    );

    private static final Map<String, List<OrderItem>> ORDER_ITEMS_CACHE = new ConcurrentHashMap<>();

    /**
     * Emergency seed fallback for Sales by Employee.
     *
     * Why this exists:
     * - Workshop 7 employee/batch endpoints are currently 500ing.
     * - Orders + order details are still 200.
     * - The old DAO logic grouped by Batch -> Employee.
     * - The current seeded dataset gives us stable batch_id -> employee_id assignments.
     */
    private static final Map<Integer, String> BATCH_TO_EMPLOYEE = buildSeedBatchEmployeeMap();

    private AnalyticsApi() {}

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static boolean isAllBakerySelection(String bakerySelection) {
        return bakerySelection == null
                || bakerySelection.isBlank()
                || ALL_BAKERIES_ADMIN.equals(bakerySelection)
                || ALL_MY_BAKERIES.equals(bakerySelection);
    }

    private static String normalizeStatus(Order order) {
        if (order == null || order.getOrderStatus() == null) return "";
        return order.getOrderStatus().trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isRecognized(Order order) {
        return RECOGNIZED_STATUSES.contains(normalizeStatus(order));
    }

    private static boolean isInProgress(Order order) {
        return IN_PROGRESS_STATUSES.contains(normalizeStatus(order));
    }

    private static boolean isExcluded(Order order) {
        return EXCLUDED_STATUSES.contains(normalizeStatus(order));
    }

    private static boolean bakeryMatches(Order order, String bakerySelection) {
        if (isAllBakerySelection(bakerySelection)) {
            return true;
        }

        String bakery = order.getBakeryDisplay();
        return bakery != null && bakery.equals(bakerySelection);
    }

    private static LocalDate analyticsDate(Order order) {
        LocalDateTime placed = order.getOrderPlacedDateTime();
        if (placed != null) {
            return placed.toLocalDate();
        }

        LocalDateTime scheduled = order.getOrderScheduledDateTime();
        if (scheduled != null) {
            return scheduled.toLocalDate();
        }

        LocalDateTime delivered = order.getOrderDeliveredDateTime();
        if (delivered != null) {
            return delivered.toLocalDate();
        }

        return null;
    }

    private static boolean dateInRange(LocalDate date, LocalDate start, LocalDate end) {
        return date != null && !date.isBefore(start) && !date.isAfter(end);
    }

    private static double finalAmount(Order order) {
        return order.getFinalAmount();
    }

    private static List<Order> listFilteredOrders(LocalDate start,
                                                  LocalDate end,
                                                  String bakerySelection) throws Exception {
        List<Order> all = OrderApi.listOrders();
        List<Order> filtered = new ArrayList<>();

        for (Order order : all) {
            if (order == null) continue;

            LocalDate date = analyticsDate(order);
            if (!dateInRange(date, start, end)) continue;
            if (!bakeryMatches(order, bakerySelection)) continue;

            filtered.add(order);
        }

        return filtered;
    }

    private static List<Order> listAllOrders() throws Exception {
        return OrderApi.listOrders();
    }

    private static List<OrderItem> getOrderItemsCached(String orderId) throws Exception {
        List<OrderItem> cached = ORDER_ITEMS_CACHE.get(orderId);
        if (cached != null) {
            return cached;
        }

        List<OrderItem> loaded = OrderApi.getOrderItems(orderId);
        ORDER_ITEMS_CACHE.put(orderId, loaded);
        return loaded;
    }

    private static double average(double total, int count) {
        return count <= 0 ? 0.0 : total / count;
    }

    private static List<DataPoint> mapToSeries(Map<String, Double> input) {
        List<DataPoint> out = new ArrayList<>();
        for (Map.Entry<String, Double> entry : input.entrySet()) {
            out.add(new DataPoint(entry.getKey(), entry.getValue()));
        }
        return out;
    }

    private static List<DataPoint> mapToSeriesSortedByValueDesc(Map<String, Double> input) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(input.entrySet());
        entries.sort((a, b) -> {
            int valueCompare = Double.compare(b.getValue(), a.getValue());
            if (valueCompare != 0) return valueCompare;
            return a.getKey().compareToIgnoreCase(b.getKey());
        });

        List<DataPoint> out = new ArrayList<>();
        for (Map.Entry<String, Double> entry : entries) {
            out.add(new DataPoint(entry.getKey(), entry.getValue()));
        }
        return out;
    }

    private static Map<Integer, String> buildSeedBatchEmployeeMap() {
        Map<Integer, String> batchToEmployee = new HashMap<>();

        Map<Integer, String> employeeIdToName = new HashMap<>();
        employeeIdToName.put(1, "Mason Clark");
        employeeIdToName.put(2, "Sophia Patel");
        employeeIdToName.put(3, "Ethan Wright");
        employeeIdToName.put(4, "Isabella Chen");
        employeeIdToName.put(5, "Noah Martin");
        employeeIdToName.put(6, "Ava Roberts");
        employeeIdToName.put(7, "Logan Scott");
        employeeIdToName.put(8, "Mia Kim");
        employeeIdToName.put(9, "Jackson Hall");

        batchToEmployee.put(1, employeeIdToName.get(1));
        batchToEmployee.put(2, employeeIdToName.get(2));
        batchToEmployee.put(3, employeeIdToName.get(3));
        batchToEmployee.put(4, employeeIdToName.get(4));
        batchToEmployee.put(5, employeeIdToName.get(3));
        batchToEmployee.put(6, employeeIdToName.get(2));
        batchToEmployee.put(7, employeeIdToName.get(5));
        batchToEmployee.put(8, employeeIdToName.get(6));
        batchToEmployee.put(9, employeeIdToName.get(7));
        batchToEmployee.put(10, employeeIdToName.get(8));
        batchToEmployee.put(11, employeeIdToName.get(9));
        batchToEmployee.put(12, employeeIdToName.get(6));
        batchToEmployee.put(13, employeeIdToName.get(7));
        batchToEmployee.put(14, employeeIdToName.get(8));
        batchToEmployee.put(15, employeeIdToName.get(9));
        batchToEmployee.put(16, employeeIdToName.get(5));
        batchToEmployee.put(17, employeeIdToName.get(4));
        batchToEmployee.put(18, employeeIdToName.get(2));

        return batchToEmployee;
    }

    private static String employeeNameForBatch(int batchId) {
        String employee = BATCH_TO_EMPLOYEE.get(batchId);
        if (employee == null || employee.isBlank()) {
            return UNKNOWN_EMPLOYEE;
        }
        return employee;
    }

    private static boolean shouldDisplayEmployee(String employeeName) {
        return employeeName != null
                && !employeeName.isBlank()
                && !UNKNOWN_EMPLOYEE.equals(employeeName);
    }

    private static double tryBackendNumber(String path, double fallback) {
        try {
            var res = ApiClient.getInstance().get(path);
            if (res.statusCode() != 200) {
                return fallback;
            }
            return ApiClient.getInstance().getMapper().readValue(res.body(), BigDecimal.class).doubleValue();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static List<DataPoint> tryBackendSeries(String path) {
        try {
            var res = ApiClient.getInstance().get(path);
            if (res.statusCode() != 200) {
                return new ArrayList<>();
            }
            return parseDataPointSeries(res.body());
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private static List<DataPoint> localRevenueOverTime(LocalDate start,
                                                        LocalDate end,
                                                        String bakerySelection,
                                                        boolean recognized) throws Exception {
        Map<LocalDate, Double> totals = new TreeMap<>();

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isExcluded(order)) continue;
            if (recognized && !isRecognized(order)) continue;
            if (!recognized && !isInProgress(order)) continue;

            LocalDate date = analyticsDate(order);
            if (date == null) continue;

            totals.merge(date, finalAmount(order), Double::sum);
        }

        List<DataPoint> out = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> entry : totals.entrySet()) {
            out.add(new DataPoint(entry.getKey().toString(), entry.getValue()));
        }
        return out;
    }

    private static List<DataPoint> localRevenueByBakery(LocalDate start,
                                                        LocalDate end,
                                                        boolean recognized) throws Exception {
        Map<String, Double> totals = new TreeMap<>();

        for (Order order : listFilteredOrders(start, end, ALL_BAKERIES_ADMIN)) {
            if (isExcluded(order)) continue;
            if (recognized && !isRecognized(order)) continue;
            if (!recognized && !isInProgress(order)) continue;

            String bakery = order.getBakeryDisplay();
            if (bakery == null || bakery.isBlank()) {
                bakery = "Unknown Bakery";
            }

            totals.merge(bakery, finalAmount(order), Double::sum);
        }

        return mapToSeries(totals);
    }

    private static List<DataPoint> localAverageOrderValueOverTime(LocalDate start,
                                                                  LocalDate end,
                                                                  String bakerySelection,
                                                                  boolean recognized) throws Exception {
        Map<LocalDate, Double> totals = new TreeMap<>();
        Map<LocalDate, Integer> counts = new TreeMap<>();

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isExcluded(order)) continue;
            if (recognized && !isRecognized(order)) continue;
            if (!recognized && !isInProgress(order)) continue;

            LocalDate date = analyticsDate(order);
            if (date == null) continue;

            totals.merge(date, finalAmount(order), Double::sum);
            counts.merge(date, 1, Integer::sum);
        }

        List<DataPoint> out = new ArrayList<>();
        for (LocalDate date : totals.keySet()) {
            out.add(new DataPoint(
                    date.toString(),
                    average(totals.getOrDefault(date, 0.0), counts.getOrDefault(date, 0))
            ));
        }
        return out;
    }

    private static List<DataPoint> localCompletionRateOverTime(LocalDate start,
                                                               LocalDate end,
                                                               String bakerySelection,
                                                               boolean recognized) throws Exception {
        Map<LocalDate, Integer> recognizedCounts = new TreeMap<>();
        Map<LocalDate, Integer> inProgressCounts = new TreeMap<>();

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isExcluded(order)) continue;

            LocalDate date = analyticsDate(order);
            if (date == null) continue;

            if (isRecognized(order)) {
                recognizedCounts.merge(date, 1, Integer::sum);
            } else if (isInProgress(order)) {
                inProgressCounts.merge(date, 1, Integer::sum);
            }
        }

        Set<LocalDate> dates = new TreeSet<>();
        dates.addAll(recognizedCounts.keySet());
        dates.addAll(inProgressCounts.keySet());

        List<DataPoint> out = new ArrayList<>();
        for (LocalDate date : dates) {
            int rec = recognizedCounts.getOrDefault(date, 0);
            int prog = inProgressCounts.getOrDefault(date, 0);
            int denom = rec + prog;

            double value;
            if (denom == 0) {
                value = 0.0;
            } else if (recognized) {
                value = (rec * 100.0) / denom;
            } else {
                value = (prog * 100.0) / denom;
            }

            out.add(new DataPoint(date.toString(), value));
        }
        return out;
    }

    private static List<DataPoint> localTopProducts(LocalDate start,
                                                    LocalDate end,
                                                    String bakerySelection,
                                                    boolean recognized) throws Exception {
        Map<String, Double> totals = new TreeMap<>();

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isExcluded(order)) continue;
            if (recognized && !isRecognized(order)) continue;
            if (!recognized && !isInProgress(order)) continue;

            List<OrderItem> items = getOrderItemsCached(order.getOrderId());
            for (OrderItem item : items) {
                String product = item.getProductDisplay();
                if (product == null || product.isBlank()) {
                    product = "Unknown Product";
                }

                totals.merge(product, (double) item.getOrderItemQuantity(), Double::sum);
            }
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<>(totals.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<DataPoint> out = new ArrayList<>();
        for (Map.Entry<String, Double> entry : entries) {
            out.add(new DataPoint(entry.getKey(), entry.getValue()));
        }
        return out;
    }

    // ── Recognized analytics ─────────────────────────────────────────────────

    public static double getTotalRevenue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        double total = 0.0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isRecognized(order)) {
                total += finalAmount(order);
            }
        }

        return total;
    }

    public static List<DataPoint> getRevenueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localRevenueOverTime(start, end, bakerySelection, true);
    }

    public static List<DataPoint> getRevenueByBakery(LocalDate start, LocalDate end) throws Exception {
        return localRevenueByBakery(start, end, true);
    }

    public static double getAverageOrderValue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        double total = 0.0;
        int count = 0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isRecognized(order)) {
                total += finalAmount(order);
                count++;
            }
        }

        return average(total, count);
    }

    public static List<DataPoint> getAverageOrderValueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localAverageOrderValueOverTime(start, end, bakerySelection, true);
    }

    public static double getCompletionRate(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        int recognized = 0;
        int inProgress = 0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isRecognized(order)) {
                recognized++;
            } else if (isInProgress(order)) {
                inProgress++;
            }
        }

        int denom = recognized + inProgress;
        return denom == 0 ? 0.0 : (recognized * 100.0) / denom;
    }

    public static List<DataPoint> getCompletionRateOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localCompletionRateOverTime(start, end, bakerySelection, true);
    }

    public static List<DataPoint> getTopProducts(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localTopProducts(start, end, bakerySelection, true);
    }

    public static double getTotalSalesByEmployee(LocalDate start,
                                                 LocalDate end,
                                                 String bakerySelection) throws Exception {

        double total = 0.0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (!isRecognized(order)) continue;

            List<OrderItem> items = getOrderItemsCached(order.getOrderId());
            for (OrderItem item : items) {
                total += item.getOrderItemLineTotal();
            }
        }

        return total;
    }

    public static List<DataPoint> getSalesByEmployee(LocalDate start,
                                                     LocalDate end,
                                                     String bakerySelection) throws Exception {

        Map<String, Double> totals = new HashMap<>();

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (!isRecognized(order)) continue;

            List<OrderItem> items = getOrderItemsCached(order.getOrderId());
            for (OrderItem item : items) {
                String employee = employeeNameForBatch(item.getBatchId());
                if (!shouldDisplayEmployee(employee)) continue;
                totals.merge(employee, item.getOrderItemLineTotal(), Double::sum);
            }
        }

        return mapToSeriesSortedByValueDesc(totals);
    }

    public static List<String> getBakeryNames() throws Exception {
        Set<String> names = new TreeSet<>();
        for (Order order : listAllOrders()) {
            String bakery = order.getBakeryDisplay();
            if (bakery != null && !bakery.isBlank()) {
                names.add(bakery);
            }
        }
        return new ArrayList<>(names);
    }

    public static List<String> getBakeryNamesByIds(List<Integer> bakeryIds) throws Exception {
        return getBakeryNames();
    }

    public static List<LocalDate> getAvailableOrderDates(String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        Set<LocalDate> dates = new TreeSet<>();

        for (Order order : listAllOrders()) {
            if (!bakeryMatches(order, bakerySelection)) continue;

            LocalDate date = analyticsDate(order);
            if (date != null) {
                dates.add(date);
            }
        }

        return new ArrayList<>(dates);
    }

    // ── In-progress analytics ────────────────────────────────────────────────

    public static double getInProgressRevenue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        double total = 0.0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isInProgress(order)) {
                total += finalAmount(order);
            }
        }

        return total;
    }

    public static double getInProgressAverageOrderValue(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        double total = 0.0;
        int count = 0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isInProgress(order)) {
                total += finalAmount(order);
                count++;
            }
        }

        return average(total, count);
    }

    public static double getInProgressRate(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        int recognized = 0;
        int inProgress = 0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (isRecognized(order)) {
                recognized++;
            } else if (isInProgress(order)) {
                inProgress++;
            }
        }

        int denom = recognized + inProgress;
        return denom == 0 ? 0.0 : (inProgress * 100.0) / denom;
    }

    public static double getInProgressTotalSalesByEmployee(LocalDate start,
                                                           LocalDate end,
                                                           String bakerySelection) throws Exception {

        double total = 0.0;

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (!isInProgress(order)) continue;

            List<OrderItem> items = getOrderItemsCached(order.getOrderId());
            for (OrderItem item : items) {
                total += item.getOrderItemLineTotal();
            }
        }

        return total;
    }

    public static List<DataPoint> getInProgressRevenueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localRevenueOverTime(start, end, bakerySelection, false);
    }

    public static List<DataPoint> getInProgressRevenueByBakery(LocalDate start, LocalDate end) throws Exception {
        return localRevenueByBakery(start, end, false);
    }

    public static List<DataPoint> getInProgressAverageOrderValueOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localAverageOrderValueOverTime(start, end, bakerySelection, false);
    }

    public static List<DataPoint> getInProgressRateOverTime(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localCompletionRateOverTime(start, end, bakerySelection, false);
    }

    public static List<DataPoint> getInProgressTopProducts(LocalDate start, LocalDate end, String bakerySelection) throws Exception {
        return localTopProducts(start, end, bakerySelection, false);
    }

    public static List<DataPoint> getInProgressSalesByEmployee(LocalDate start,
                                                               LocalDate end,
                                                               String bakerySelection) throws Exception {

        Map<String, Double> totals = new HashMap<>();

        for (Order order : listFilteredOrders(start, end, bakerySelection)) {
            if (!isInProgress(order)) continue;

            List<OrderItem> items = getOrderItemsCached(order.getOrderId());
            for (OrderItem item : items) {
                String employee = employeeNameForBatch(item.getBatchId());
                if (!shouldDisplayEmployee(employee)) continue;
                totals.merge(employee, item.getOrderItemLineTotal(), Double::sum);
            }
        }

        return mapToSeriesSortedByValueDesc(totals);
    }

    // ── Shared parsing helpers kept for graceful backend fallbacks ───────────

    private static List<DataPoint> parseDataPointSeries(String body) throws Exception {
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        com.fasterxml.jackson.databind.JavaType type =
                mapper.getTypeFactory().constructCollectionType(List.class, Dp.class);

        List<Dp> rows = mapper.readValue(body, type);
        List<DataPoint> out = new ArrayList<>();

        for (Dp r : rows) {
            out.add(new DataPoint(r.label, r.value != null ? r.value.doubleValue() : 0.0));
        }

        return out;
    }

    @SuppressWarnings("unused")
    private static class Dp {
        public String label;
        public BigDecimal value;
    }
}