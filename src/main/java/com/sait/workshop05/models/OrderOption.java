package com.sait.workshop05.models;

public class OrderOption {
    private final String orderId;
    private final String orderInfo;

    public OrderOption(String orderId, String orderInfo) {
        this.orderId = orderId;
        this.orderInfo = orderInfo;
    }

    public String getOrderId() { return orderId; }
    public String getOrderInfo() { return orderInfo; }

    /**
     * ComboBox / list display only — never expose internal {@code orderId} (often a UUID) in the UI.
     */
    @Override
    public String toString() {
        return orderInfo != null ? orderInfo : "";
    }
}
