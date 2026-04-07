package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Order model representing a customer order.
 * Maps to the Order table in the database.
 */
public class Order {
    private final StringProperty orderId;
    private final StringProperty customerId;
    private final IntegerProperty bakeryId;
    private final IntegerProperty addressId;
    private final ObjectProperty<LocalDateTime> orderPlacedDateTime;
    private final ObjectProperty<LocalDateTime> orderScheduledDateTime;
    private final ObjectProperty<LocalDateTime> orderDeliveredDateTime;
    private final StringProperty orderMethod;
    private final StringProperty orderComment;
    private final DoubleProperty orderTotal;
    private final DoubleProperty orderDiscount;
    private final StringProperty orderStatus;

    // Display properties for foreign keys
    private final StringProperty customerDisplay;
    private final StringProperty bakeryDisplay;
    private final StringProperty addressDisplay;

    /**
     * Default constructor - initializes all properties
     */
    public Order() {
        this.orderId = new SimpleStringProperty();
        this.customerId = new SimpleStringProperty();
        this.bakeryId = new SimpleIntegerProperty();
        this.addressId = new SimpleIntegerProperty();
        this.orderPlacedDateTime = new SimpleObjectProperty<>();
        this.orderScheduledDateTime = new SimpleObjectProperty<>();
        this.orderDeliveredDateTime = new SimpleObjectProperty<>();
        this.orderMethod = new SimpleStringProperty();
        this.orderComment = new SimpleStringProperty();
        this.orderTotal = new SimpleDoubleProperty();
        this.orderDiscount = new SimpleDoubleProperty();
        this.orderStatus = new SimpleStringProperty();
        this.customerDisplay = new SimpleStringProperty();
        this.bakeryDisplay = new SimpleStringProperty();
        this.addressDisplay = new SimpleStringProperty();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId.get();
    }

    public StringProperty orderIdProperty() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId.set(orderId);
    }

    public String getCustomerId() {
        return customerId.get();
    }

    public StringProperty customerIdProperty() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId.set(customerId);
    }

    public int getBakeryId() {
        return bakeryId.get();
    }

    public IntegerProperty bakeryIdProperty() {
        return bakeryId;
    }

    public void setBakeryId(int bakeryId) {
        this.bakeryId.set(bakeryId);
    }

    public int getAddressId() {
        return addressId.get();
    }

    public IntegerProperty addressIdProperty() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId.set(addressId);
    }

    public LocalDateTime getOrderPlacedDateTime() {
        return orderPlacedDateTime.get();
    }

    public ObjectProperty<LocalDateTime> orderPlacedDateTimeProperty() {
        return orderPlacedDateTime;
    }

    public void setOrderPlacedDateTime(LocalDateTime orderPlacedDateTime) {
        this.orderPlacedDateTime.set(orderPlacedDateTime);
    }

    public LocalDateTime getOrderScheduledDateTime() {
        return orderScheduledDateTime.get();
    }

    public ObjectProperty<LocalDateTime> orderScheduledDateTimeProperty() {
        return orderScheduledDateTime;
    }

    public void setOrderScheduledDateTime(LocalDateTime orderScheduledDateTime) {
        this.orderScheduledDateTime.set(orderScheduledDateTime);
    }

    public LocalDateTime getOrderDeliveredDateTime() {
        return orderDeliveredDateTime.get();
    }

    public ObjectProperty<LocalDateTime> orderDeliveredDateTimeProperty() {
        return orderDeliveredDateTime;
    }

    public void setOrderDeliveredDateTime(LocalDateTime orderDeliveredDateTime) {
        this.orderDeliveredDateTime.set(orderDeliveredDateTime);
    }

    public String getOrderMethod() {
        return orderMethod.get();
    }

    public StringProperty orderMethodProperty() {
        return orderMethod;
    }

    public void setOrderMethod(String orderMethod) {
        this.orderMethod.set(orderMethod);
    }

    public String getOrderComment() {
        return orderComment.get();
    }

    public StringProperty orderCommentProperty() {
        return orderComment;
    }

    public void setOrderComment(String orderComment) {
        this.orderComment.set(orderComment);
    }

    public double getOrderTotal() {
        return orderTotal.get();
    }

    public DoubleProperty orderTotalProperty() {
        return orderTotal;
    }

    public void setOrderTotal(double orderTotal) {
        this.orderTotal.set(orderTotal);
    }

    public double getOrderDiscount() {
        return orderDiscount.get();
    }

    public DoubleProperty orderDiscountProperty() {
        return orderDiscount;
    }

    public void setOrderDiscount(double orderDiscount) {
        this.orderDiscount.set(orderDiscount);
    }

    public String getOrderStatus() {
        return orderStatus.get();
    }

    public StringProperty orderStatusProperty() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus.set(orderStatus);
    }

    public String getCustomerDisplay() {
        return customerDisplay.get();
    }

    public StringProperty customerDisplayProperty() {
        return customerDisplay;
    }

    public void setCustomerDisplay(String customerDisplay) {
        this.customerDisplay.set(customerDisplay);
    }

    public String getBakeryDisplay() {
        return bakeryDisplay.get();
    }

    public StringProperty bakeryDisplayProperty() {
        return bakeryDisplay;
    }

    public void setBakeryDisplay(String bakeryDisplay) {
        this.bakeryDisplay.set(bakeryDisplay);
    }

    public String getAddressDisplay() {
        return addressDisplay.get();
    }

    public StringProperty addressDisplayProperty() {
        return addressDisplay;
    }

    public void setAddressDisplay(String addressDisplay) {
        this.addressDisplay.set(addressDisplay);
    }

    /**
     * Get the final amount after discount
     */
    public double getFinalAmount() {
        return orderTotal.get() - orderDiscount.get();
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId.get() +
                ", customerId=" + customerId.get() +
                ", orderTotal=" + orderTotal.get() +
                ", orderDiscount=" + orderDiscount.get() +
                ", orderStatus='" + orderStatus.get() + '\'' +
                ", orderPlacedDateTime=" + orderPlacedDateTime.get() +
                '}';
    }
}

