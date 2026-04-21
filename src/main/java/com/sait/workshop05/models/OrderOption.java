// Contributor(s): Robbie
// Main: Robbie - Order pick list row for staff tools.

package com.sait.workshop05.models;

/**
 * Order pick list entry. toString returns the friendly label only and keeps the internal order id off the UI.
 */
public class OrderOption {
    private final String orderId;
    private final String orderInfo;

    /**
     * Creates one order option with internal id and friendly label.
     */
    public OrderOption(String orderId, String orderInfo) {
        this.orderId = orderId;
        this.orderInfo = orderInfo;
    }

    /**
     * Returns internal order id.
     */
    public String getOrderId() { return orderId; }
    /**
     * Returns friendly order label text.
     */
    public String getOrderInfo() { return orderInfo; }

    /**
     * ComboBox and list display string. The raw order id stays internal and is often a UUID.
     */
    @Override
    public String toString() {
        return orderInfo != null ? orderInfo : "";
    }
}
