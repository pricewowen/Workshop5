package com.sait.workshop05.models;

public class OrderOption {
    private final int orderId;
    private final String orderInfo;

    public OrderOption(int orderId, String orderInfo) {
        this.orderId = orderId;
        this.orderInfo = orderInfo;
    }

    public int getOrderId() { return orderId; }
    public String getOrderInfo() { return orderInfo; }

    @Override
    public String toString() {
        return orderId + " - " + orderInfo;
    }
}
