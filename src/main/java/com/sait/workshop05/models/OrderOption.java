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

    @Override
    public String toString() {
        return orderId + " - " + orderInfo;
    }
}
