package com.sait.workshop05.util;

import java.util.List;

/**
 * Standardised order status constants used across the application.
 */
public final class OrderStatus {
    public static final String PENDING = "Pending";
    public static final String PROCESSING = "Processing";
    public static final String READY = "Ready";
    public static final String OUT_FOR_DELIVERY = "Out for Delivery";
    public static final String COMPLETED = "Completed";
    public static final String DELIVERED = "Delivered";
    public static final String CANCELLED = "Cancelled";

    /** All statuses for ComboBox dropdowns (does not include "All"). */
    public static final List<String> ALL_STATUSES = List.of(
            PENDING, PROCESSING, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED
    );

    /** Statuses for filter dropdowns (includes "All" option). */
    public static final List<String> FILTER_STATUSES = List.of(
            "All", PENDING, PROCESSING, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED
    );

    private OrderStatus() {} // utility class
}
