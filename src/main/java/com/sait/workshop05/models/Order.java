// Contributor(s): Robbie
// Main: Robbie - Order header model for management views.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * JavaFX order row used by order list and checkout admin screens.
 */
public class Order {
    private final StringProperty orderId;
    private final StringProperty orderNumber;
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

    // Display values resolved from related ids for table presentation.
    private final StringProperty customerDisplay;
    private final StringProperty bakeryDisplay;
    private final StringProperty addressDisplay;

    /**
     * Initializes JavaFX properties for binding and table updates.
     */
    public Order() {
        this.orderId = new SimpleStringProperty();
        this.orderNumber = new SimpleStringProperty();
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

    /** @return order id value. */
    public String getOrderId() {
        return orderId.get();
    }

    /** @return JavaFX property wrapper for order id. */
    public StringProperty orderIdProperty() {
        return orderId;
    }

    /** @param orderId order id value. */
    public void setOrderId(String orderId) {
        this.orderId.set(orderId);
    }

    /** @return order number display value. */
    public String getOrderNumber() {
        return orderNumber.get();
    }

    /** @return JavaFX property wrapper for order number. */
    public StringProperty orderNumberProperty() {
        return orderNumber;
    }

    /** @param orderNumber order number display value. */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber.set(orderNumber);
    }

    /** @return customer id value. */
    public String getCustomerId() {
        return customerId.get();
    }

    /** @return JavaFX property wrapper for customer id. */
    public StringProperty customerIdProperty() {
        return customerId;
    }

    /** @param customerId customer id value. */
    public void setCustomerId(String customerId) {
        this.customerId.set(customerId);
    }

    /** @return bakery id value. */
    public int getBakeryId() {
        return bakeryId.get();
    }

    /** @return JavaFX property wrapper for bakery id. */
    public IntegerProperty bakeryIdProperty() {
        return bakeryId;
    }

    /** @param bakeryId bakery id value. */
    public void setBakeryId(int bakeryId) {
        this.bakeryId.set(bakeryId);
    }

    /** @return address id value. */
    public int getAddressId() {
        return addressId.get();
    }

    /** @return JavaFX property wrapper for address id. */
    public IntegerProperty addressIdProperty() {
        return addressId;
    }

    /** @param addressId address id value. */
    public void setAddressId(int addressId) {
        this.addressId.set(addressId);
    }

    /** @return order placed timestamp value. */
    public LocalDateTime getOrderPlacedDateTime() {
        return orderPlacedDateTime.get();
    }

    /** @return JavaFX property wrapper for placed timestamp. */
    public ObjectProperty<LocalDateTime> orderPlacedDateTimeProperty() {
        return orderPlacedDateTime;
    }

    /** @param orderPlacedDateTime order placed timestamp value. */
    public void setOrderPlacedDateTime(LocalDateTime orderPlacedDateTime) {
        this.orderPlacedDateTime.set(orderPlacedDateTime);
    }

    /** @return scheduled timestamp value. */
    public LocalDateTime getOrderScheduledDateTime() {
        return orderScheduledDateTime.get();
    }

    /** @return JavaFX property wrapper for scheduled timestamp. */
    public ObjectProperty<LocalDateTime> orderScheduledDateTimeProperty() {
        return orderScheduledDateTime;
    }

    /** @param orderScheduledDateTime scheduled timestamp value. */
    public void setOrderScheduledDateTime(LocalDateTime orderScheduledDateTime) {
        this.orderScheduledDateTime.set(orderScheduledDateTime);
    }

    /** @return delivered timestamp value. */
    public LocalDateTime getOrderDeliveredDateTime() {
        return orderDeliveredDateTime.get();
    }

    /** @return JavaFX property wrapper for delivered timestamp. */
    public ObjectProperty<LocalDateTime> orderDeliveredDateTimeProperty() {
        return orderDeliveredDateTime;
    }

    /** @param orderDeliveredDateTime delivered timestamp value. */
    public void setOrderDeliveredDateTime(LocalDateTime orderDeliveredDateTime) {
        this.orderDeliveredDateTime.set(orderDeliveredDateTime);
    }

    /** @return order method value. */
    public String getOrderMethod() {
        return orderMethod.get();
    }

    /** @return JavaFX property wrapper for order method. */
    public StringProperty orderMethodProperty() {
        return orderMethod;
    }

    /** @param orderMethod order method value. */
    public void setOrderMethod(String orderMethod) {
        this.orderMethod.set(orderMethod);
    }

    /** @return order comment value. */
    public String getOrderComment() {
        return orderComment.get();
    }

    /** @return JavaFX property wrapper for order comment. */
    public StringProperty orderCommentProperty() {
        return orderComment;
    }

    /** @param orderComment order comment value. */
    public void setOrderComment(String orderComment) {
        this.orderComment.set(orderComment);
    }

    /** @return order total value. */
    public double getOrderTotal() {
        return orderTotal.get();
    }

    /** @return JavaFX property wrapper for order total. */
    public DoubleProperty orderTotalProperty() {
        return orderTotal;
    }

    /** @param orderTotal order total value. */
    public void setOrderTotal(double orderTotal) {
        this.orderTotal.set(orderTotal);
    }

    /** @return order discount value. */
    public double getOrderDiscount() {
        return orderDiscount.get();
    }

    /** @return JavaFX property wrapper for order discount. */
    public DoubleProperty orderDiscountProperty() {
        return orderDiscount;
    }

    /** @param orderDiscount order discount value. */
    public void setOrderDiscount(double orderDiscount) {
        this.orderDiscount.set(orderDiscount);
    }

    /** @return order status value. */
    public String getOrderStatus() {
        return orderStatus.get();
    }

    /** @return JavaFX property wrapper for order status. */
    public StringProperty orderStatusProperty() {
        return orderStatus;
    }

    /** @param orderStatus order status value. */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus.set(orderStatus);
    }

    /** @return customer display label. */
    public String getCustomerDisplay() {
        return customerDisplay.get();
    }

    /** @return JavaFX property wrapper for customer display label. */
    public StringProperty customerDisplayProperty() {
        return customerDisplay;
    }

    /** @param customerDisplay customer display label. */
    public void setCustomerDisplay(String customerDisplay) {
        this.customerDisplay.set(customerDisplay);
    }

    /** @return bakery display label. */
    public String getBakeryDisplay() {
        return bakeryDisplay.get();
    }

    /** @return JavaFX property wrapper for bakery display label. */
    public StringProperty bakeryDisplayProperty() {
        return bakeryDisplay;
    }

    /** @param bakeryDisplay bakery display label. */
    public void setBakeryDisplay(String bakeryDisplay) {
        this.bakeryDisplay.set(bakeryDisplay);
    }

    /** @return address display label. */
    public String getAddressDisplay() {
        return addressDisplay.get();
    }

    /** @return JavaFX property wrapper for address display label. */
    public StringProperty addressDisplayProperty() {
        return addressDisplay;
    }

    /** @param addressDisplay address display label. */
    public void setAddressDisplay(String addressDisplay) {
        this.addressDisplay.set(addressDisplay);
    }

    /**
     * Returns net total after subtracting manual discount.
     *
     * @return net order amount shown to staff.
     */
    public double getFinalAmount() {
        return orderTotal.get() - orderDiscount.get();
    }

    /**
     * @return debug-friendly summary string for the order row.
     */
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

