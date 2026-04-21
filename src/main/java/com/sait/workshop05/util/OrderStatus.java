// Contributor(s): Robbie
// Main: Robbie - Order status constants aligned with Workshop 7 values.

package com.sait.workshop05.util;

import java.util.List;

/**
 * Shared status constants keep desktop labels aligned with API values.
 */
public final class OrderStatus {
    public static final String PENDING = "Pending";
    public static final String PROCESSING = "Processing";
    public static final String READY = "Ready";
    public static final String OUT_FOR_DELIVERY = "Out for Delivery";
    public static final String COMPLETED = "Completed";
    public static final String DELIVERED = "Delivered";
    public static final String CANCELLED = "Cancelled";

    /** Full status list for editable dropdowns without the filter-only All option. */
    public static final List<String> ALL_STATUSES = List.of(
            PENDING, PROCESSING, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED
    );

    /**
     * Status values staff may PATCH directly.
     * Completed remains server-driven through customer confirmation or automatic flows.
     */
    public static final List<String> STAFF_ASSIGNABLE_STATUSES = List.of(
            PENDING, PROCESSING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    );

    /** Statuses for filter dropdowns including the All option. */
    public static final List<String> FILTER_STATUSES = List.of(
            "All", PENDING, PROCESSING, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED
    );

    private OrderStatus() {} // Prevent instantiation of constants holder.
}
