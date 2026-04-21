// Contributor(s): Robbie
// Main: Robbie - Order line item model for checkout and detail.

package com.sait.workshop05.models;

import javafx.beans.property.*;

/**
 * JavaFX order line item row for checkout and detail screens.
 */
public class OrderItem {
    private final IntegerProperty orderItemId;
    private final StringProperty orderId;
    private final IntegerProperty productId;
    private final IntegerProperty batchId;
    private final IntegerProperty orderItemQuantity;
    private final DoubleProperty orderItemUnitPriceAtTime;
    private final DoubleProperty orderItemLineTotal;

    // Display labels keep ids readable in staff tables.
    private final StringProperty productDisplay;
    private final StringProperty batchDisplay;

    /**
     * Initializes JavaFX properties for binding and live row refresh.
     */
    public OrderItem() {
        this.orderItemId = new SimpleIntegerProperty();
        this.orderId = new SimpleStringProperty();
        this.productId = new SimpleIntegerProperty();
        this.batchId = new SimpleIntegerProperty();
        this.orderItemQuantity = new SimpleIntegerProperty();
        this.orderItemUnitPriceAtTime = new SimpleDoubleProperty();
        this.orderItemLineTotal = new SimpleDoubleProperty();
        this.productDisplay = new SimpleStringProperty();
        this.batchDisplay = new SimpleStringProperty();
    }

    /** Returns order item id value. */
    public int getOrderItemId() {
        return orderItemId.get();
    }

    /** Returns JavaFX property wrapper for order item id. */
    public IntegerProperty orderItemIdProperty() {
        return orderItemId;
    }

    /** Sets order item id value. */
    public void setOrderItemId(int orderItemId) {
        this.orderItemId.set(orderItemId);
    }

    /** Returns linked order id value. */
    public String getOrderId() {
        return orderId.get();
    }

    /** Returns JavaFX property wrapper for linked order id. */
    public StringProperty orderIdProperty() {
        return orderId;
    }

    /** Sets linked order id value. */
    public void setOrderId(String orderId) {
        this.orderId.set(orderId);
    }

    /** Returns linked product id value. */
    public int getProductId() {
        return productId.get();
    }

    /** Returns JavaFX property wrapper for linked product id. */
    public IntegerProperty productIdProperty() {
        return productId;
    }

    /** Sets linked product id value. */
    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    /** Returns linked batch id value. */
    public int getBatchId() {
        return batchId.get();
    }

    /** Returns JavaFX property wrapper for linked batch id. */
    public IntegerProperty batchIdProperty() {
        return batchId;
    }

    /** Sets linked batch id value. */
    public void setBatchId(int batchId) {
        this.batchId.set(batchId);
    }

    /** Returns quantity value for the line item. */
    public int getOrderItemQuantity() {
        return orderItemQuantity.get();
    }

    /** Returns JavaFX property wrapper for quantity. */
    public IntegerProperty orderItemQuantityProperty() {
        return orderItemQuantity;
    }

    /** Sets quantity value for the line item. */
    public void setOrderItemQuantity(int orderItemQuantity) {
        this.orderItemQuantity.set(orderItemQuantity);
    }

    /** Returns unit price value captured at order time. */
    public double getOrderItemUnitPriceAtTime() {
        return orderItemUnitPriceAtTime.get();
    }

    /** Returns JavaFX property wrapper for unit price at order time. */
    public DoubleProperty orderItemUnitPriceAtTimeProperty() {
        return orderItemUnitPriceAtTime;
    }

    /** Sets unit price value captured at order time. */
    public void setOrderItemUnitPriceAtTime(double orderItemUnitPriceAtTime) {
        this.orderItemUnitPriceAtTime.set(orderItemUnitPriceAtTime);
    }

    /** Returns computed line total value. */
    public double getOrderItemLineTotal() {
        return orderItemLineTotal.get();
    }

    /** Returns JavaFX property wrapper for line total. */
    public DoubleProperty orderItemLineTotalProperty() {
        return orderItemLineTotal;
    }

    /** Sets computed line total value. */
    public void setOrderItemLineTotal(double orderItemLineTotal) {
        this.orderItemLineTotal.set(orderItemLineTotal);
    }

    /** Returns display label for product value. */
    public String getProductDisplay() {
        return productDisplay.get();
    }

    /** Returns JavaFX property wrapper for product display label. */
    public StringProperty productDisplayProperty() {
        return productDisplay;
    }

    /** Sets display label for product value. */
    public void setProductDisplay(String productDisplay) {
        this.productDisplay.set(productDisplay);
    }

    /** Returns display label for batch value. */
    public String getBatchDisplay() {
        return batchDisplay.get();
    }

    /** Returns JavaFX property wrapper for batch display label. */
    public StringProperty batchDisplayProperty() {
        return batchDisplay;
    }

    /** Sets display label for batch value. */
    public void setBatchDisplay(String batchDisplay) {
        this.batchDisplay.set(batchDisplay);
    }

    /**
     * @return debug-friendly summary string for the order item row.
     */
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

