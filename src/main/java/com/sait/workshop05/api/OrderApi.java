package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sait.workshop05.models.CustomerOption;
import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderItem;
import com.sait.workshop05.util.UiPrivacy;

/**
 * Orders API: list, items, status patch, checkout (staff).
 */
public final class OrderApi {

    private OrderApi() {}

    /** Maps Workshop 5 UI status labels to API {@code order_status} values. */
    public static String workshopStatusToApi(String w5) {
        if (w5 == null) return "placed";
        return switch (w5.trim()) {
            case "Pending" -> "placed";
            case "Processing" -> "preparing";
            case "Ready" -> "ready";
            case "Out for Delivery" -> "picked_up";
            case "Completed" -> "completed";
            case "Delivered" -> "delivered";
            case "Cancelled" -> "cancelled";
            default -> "placed";
        };
    }

    /** Human-readable label for table display. */
    public static String statusToDisplay(String api) {
        if (api == null || api.isBlank()) return "";
        return switch (api.trim().toLowerCase()) {
            case "placed", "pending_payment" -> "Pending";
            case "paid" -> "Paid";
            case "preparing" -> "Processing";
            case "ready" -> "Ready";
            case "scheduled" -> "Scheduled";
            case "picked_up" -> "Out for Delivery";
            case "delivered" -> "Delivered";
            case "completed" -> "Completed";
            case "cancelled" -> "Cancelled";
            default -> api;
        };
    }

    public static List<Order> listOrdersForCustomer(String customerId) throws Exception {
        if (customerId == null || customerId.isBlank()) {
            return List.of();
        }
        return listOrders().stream()
                .filter(o -> customerId.equals(o.getCustomerId()))
                .collect(Collectors.toList());
    }

    public static List<Order> listOrders() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/orders");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("list orders failed: " + res.statusCode() + " " + res.body());
        }
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        List<OrderJson> rows = mapper.readValue(res.body(), new TypeReference<List<OrderJson>>() {});
        List<Order> out = new ArrayList<>();
        for (OrderJson j : rows) {
            out.add(toOrder(j));
        }
        enrichCustomerDisplays(out);
        return out;
    }

    /**
     * Fills in customer names from the admin customer directory when the order list JSON omits
     * {@code customerName} (or leaves it blank). No-op if every row already has a display name.
     */
    static void enrichCustomerDisplays(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        boolean needEnrichment = false;
        for (Order o : orders) {
            if (!orderHasResolvedCustomerDisplay(o)) {
                needEnrichment = true;
                break;
            }
        }
        if (!needEnrichment) {
            return;
        }
        Map<String, String> nameByCustomerId;
        try {
            nameByCustomerId = loadCustomerDisplayNameMap();
        } catch (Exception e) {
            nameByCustomerId = Map.of();
        }
        for (Order o : orders) {
            if (orderHasResolvedCustomerDisplay(o)) {
                continue;
            }
            String id = o.getCustomerId();
            if (id == null || id.isBlank()) {
                o.setCustomerDisplay(UiPrivacy.customerDisplayFallback(null));
                continue;
            }
            String resolved = nameByCustomerId.get(id);
            if (resolved != null && !resolved.isBlank()) {
                o.setCustomerDisplay(resolved);
            } else {
                o.setCustomerDisplay(UiPrivacy.customerDisplayFallback(id));
            }
        }
    }

    /**
     * {@code true} when the label is fit to show as-is (including {@code Guest} with no customer id).
     * {@code false} when we should try the customer directory or keep a generic fallback.
     */
    private static boolean orderHasResolvedCustomerDisplay(Order o) {
        String d = o.getCustomerDisplay();
        if (d == null || d.isBlank() || "—".equals(d)) {
            return false;
        }
        String id = o.getCustomerId();
        if (id == null || id.isBlank()) {
            return true;
        }
        return !d.equals(UiPrivacy.customerDisplayFallback(id));
    }

    private static Map<String, String> loadCustomerDisplayNameMap() throws Exception {
        List<CustomerOption> customers = ReferenceApi.loadCustomers();
        Map<String, String> map = new HashMap<>();
        for (CustomerOption c : customers) {
            if (c.getCustomerId() == null || c.getCustomerId().isBlank()) {
                continue;
            }
            String name = c.getFullName() != null ? c.getFullName().trim() : "";
            if (name.isEmpty() || "(customer)".equalsIgnoreCase(name)) {
                map.put(c.getCustomerId(), "Customer");
            } else {
                map.put(c.getCustomerId(), name);
            }
        }
        return map;
    }

    public static List<OrderItem> getOrderItems(String orderId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/orders/" + orderId);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("get order failed: " + res.statusCode() + " " + res.body());
        }
        ObjectMapper mapper = ApiClient.getInstance().getMapper();
        OrderJson j = mapper.readValue(res.body(), OrderJson.class);
        return itemsFromOrder(j);
    }

    public static void updateOrderStatus(String orderId, String workshopStatusLabel) throws Exception {
        String apiStatus = workshopStatusToApi(workshopStatusLabel);
        Map<String, String> body = new HashMap<>();
        body.put("status", apiStatus);
        HttpResponse<String> res = ApiClient.getInstance().patch("/api/v1/orders/" + orderId + "/status", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("patch status failed: " + res.statusCode() + " " + res.body());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderJson {
        public String id;
        public String orderNumber;
        public String customerId;
        public Integer bakeryId;
        public String bakeryName;
        public Integer addressId;
        public String orderMethod;
        public String status;
        public BigDecimal orderTotal;
        public BigDecimal orderDiscount;
        public String placedAt;
        public String scheduledAt;
        public String deliveredAt;
        public String comment;
        public List<ItemJson> items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemJson {
        public Integer id;
        public Integer productId;
        public String productName;
        public Integer batchId;
        public int quantity;
        public BigDecimal unitPrice;
        public BigDecimal lineTotal;
    }

    public static Order toOrder(OrderJson j) {
        Order o = new Order();
        o.setOrderId(j.id);
        o.setOrderNumber(UiPrivacy.friendlyOrderNumber(j.orderNumber, j.id));
        o.setCustomerId(j.customerId != null ? j.customerId : "");
        o.setBakeryId(j.bakeryId != null ? j.bakeryId : 0);
        o.setAddressId(j.addressId != null ? j.addressId : 0);
        o.setOrderPlacedDateTime(parseDt(j.placedAt));
        o.setOrderScheduledDateTime(parseDt(j.scheduledAt));
        o.setOrderDeliveredDateTime(parseDt(j.deliveredAt));
        o.setOrderMethod(j.orderMethod != null ? j.orderMethod : "");
        o.setOrderComment(j.comment);
        o.setOrderTotal(j.orderTotal != null ? j.orderTotal.doubleValue() : 0);
        o.setOrderDiscount(j.orderDiscount != null ? j.orderDiscount.doubleValue() : 0);
        o.setOrderStatus(statusToDisplay(j.status));

        String custName = j.customerName != null ? j.customerName.trim() : "";
        if (!custName.isEmpty()) {
            o.setCustomerDisplay(custName);
        } else {
            o.setCustomerDisplay("");
        }

        o.setBakeryDisplay(j.bakeryName != null ? j.bakeryName : "");
        o.setAddressDisplay("");
        return o;
    }

    public static List<OrderItem> itemsFromOrder(OrderJson j) {
        List<OrderItem> list = new ArrayList<>();
        if (j.items == null) return list;
        for (ItemJson row : j.items) {
            OrderItem it = new OrderItem();
            if (row.id != null) {
                it.setOrderItemId(row.id);
            }
            it.setProductId(row.productId != null ? row.productId : 0);
            it.setProductDisplay(row.productName != null ? row.productName : "");
            it.setBatchId(row.batchId != null ? row.batchId : 0);
            it.setOrderItemQuantity(row.quantity);
            it.setOrderItemUnitPriceAtTime(row.unitPrice != null ? row.unitPrice.doubleValue() : 0);
            it.setOrderItemLineTotal(row.lineTotal != null ? row.lineTotal.doubleValue() : 0);
            it.setOrderId(j.id);
            list.add(it);
        }
        return list;
    }

    /**
     * Staff checkout (admin/employee) — {@code POST /api/v1/orders} with {@code customerId} set.
     */
    public static String placeStaffOrder(
            String customerId,
            int bakeryId,
            String methodUi,
            Integer addressId,
            String comment,
            LocalDateTime scheduledLocal,
            double manualDiscount,
            List<OrderItem> cart
    ) throws Exception {
        String methodApi;
        if (methodUi != null && methodUi.equalsIgnoreCase("Delivery")) {
            methodApi = "delivery";
        } else {
            methodApi = "pickup";
        }

        java.time.OffsetDateTime scheduledAt = scheduledLocal == null
                ? null
                : scheduledLocal.atZone(ZoneId.systemDefault()).toOffsetDateTime();

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem it : cart) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("productId", it.getProductId());
            line.put("quantity", it.getOrderItemQuantity());
            if (it.getBatchId() > 0) {
                line.put("batchId", it.getBatchId());
            }
            items.add(line);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerId", customerId);
        body.put("manualDiscount", BigDecimal.valueOf(manualDiscount));
        body.put("bakeryId", bakeryId);
        body.put("orderMethod", methodApi);
        body.put("addressId", addressId);
        body.put("comment", comment);
        body.put("scheduledAt", scheduledAt == null ? null : scheduledAt.toString());
        body.put("paymentMethod", "cash");
        body.put("items", items);

        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/orders", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("checkout failed: " + res.statusCode() + " " + res.body());
        }
        OrderJson created = ApiClient.getInstance().getMapper().readValue(res.body(), OrderJson.class);
        return checkoutResultLabel(created);
    }

    /** Visible order ref for success messages — never returns a raw UUID. */
    private static String checkoutResultLabel(OrderJson created) {
        return UiPrivacy.friendlyOrderNumber(created.orderNumber, created.id);
    }

    private static LocalDateTime parseDt(String s) {
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
