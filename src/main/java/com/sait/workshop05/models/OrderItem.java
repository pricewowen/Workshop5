package com.sait.workshop05.models;

import javafx.beans.property.*;

/**
 * OrderItem model representing an item in an order.
 * Maps to the OrderItem table in the database.
 */
public class OrderItem {
    private final IntegerProperty orderItemId;
    private final IntegerProperty orderId;
    private final IntegerProperty productId;
    private final IntegerProperty batchId;
    private final IntegerProperty orderItemQuantity;
    private final DoubleProperty orderItemUnitPriceAtTime;
    private final DoubleProperty orderItemLineTotal;

    // Display properties
    private final StringProperty productDisplay;
    private final StringProperty batchDisplay;

    /**
     * Default constructor - initializes all properties
     */
    public OrderItem() {
        this.orderItemId = new SimpleIntegerProperty();
        this.orderId = new SimpleIntegerProperty();
        this.productId = new SimpleIntegerProperty();
        this.batchId = new SimpleIntegerProperty();
        this.orderItemQuantity = new SimpleIntegerProperty();
        this.orderItemUnitPriceAtTime = new SimpleDoubleProperty();
        this.orderItemLineTotal = new SimpleDoubleProperty();
        this.productDisplay = new SimpleStringProperty();
        this.batchDisplay = new SimpleStringProperty();
    }

    // Getters and Setters
    public int getOrderItemId() {
        return orderItemId.get();
    }

    public IntegerProperty orderItemIdProperty() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId.set(orderItemId);
    }

    public int getOrderId() {
        return orderId.get();
    }

    public IntegerProperty orderIdProperty() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId.set(orderId);
    }

    public int getProductId() {
        return productId.get();
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public int getBatchId() {
        return batchId.get();
    }

    public IntegerProperty batchIdProperty() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId.set(batchId);
    }

    public int getOrderItemQuantity() {
        return orderItemQuantity.get();
    }

    public IntegerProperty orderItemQuantityProperty() {
        return orderItemQuantity;
    }

    public void setOrderItemQuantity(int orderItemQuantity) {
        this.orderItemQuantity.set(orderItemQuantity);
    }

    public double getOrderItemUnitPriceAtTime() {
        return orderItemUnitPriceAtTime.get();
    }

    public DoubleProperty orderItemUnitPriceAtTimeProperty() {
        return orderItemUnitPriceAtTime;
    }

    public void setOrderItemUnitPriceAtTime(double orderItemUnitPriceAtTime) {
        this.orderItemUnitPriceAtTime.set(orderItemUnitPriceAtTime);
    }

    public double getOrderItemLineTotal() {
        return orderItemLineTotal.get();
    }

    public DoubleProperty orderItemLineTotalProperty() {
        return orderItemLineTotal;
    }

    public void setOrderItemLineTotal(double orderItemLineTotal) {
        this.orderItemLineTotal.set(orderItemLineTotal);
    }

    public String getProductDisplay() {
        return productDisplay.get();
    }

    public StringProperty productDisplayProperty() {
        return productDisplay;
    }

    public void setProductDisplay(String productDisplay) {
        this.productDisplay.set(productDisplay);
    }

    public String getBatchDisplay() {
        return batchDisplay.get();
    }

    public StringProperty batchDisplayProperty() {
        return batchDisplay;
    }

    public void setBatchDisplay(String batchDisplay) {
        this.batchDisplay.set(batchDisplay);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId.get() +
                ", orderId=" + orderId.get() +
                ", productId=" + productId.get() +
                ", quantity=" + orderItemQuantity.get() +
                ", unitPrice=" + orderItemUnitPriceAtTime.get() +
                ", lineTotal=" + orderItemLineTotal.get() +
                '}';
    }
}

