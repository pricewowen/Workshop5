package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Payment model representing a payment transaction for an order.
 * Maps to the Payment table in the database.
 */
public class Payment {
    private final IntegerProperty paymentId;
    private final IntegerProperty orderId;
    private final DoubleProperty paymentAmount;
    private final StringProperty paymentMethod;
    private final StringProperty paymentTransactionId;
    private final StringProperty paymentStatus;
    private final ObjectProperty<LocalDateTime> paymentPaidAt;

    /**
     * Default constructor - initializes all properties
     */
    public Payment() {
        this.paymentId = new SimpleIntegerProperty();
        this.orderId = new SimpleIntegerProperty();
        this.paymentAmount = new SimpleDoubleProperty();
        this.paymentMethod = new SimpleStringProperty();
        this.paymentTransactionId = new SimpleStringProperty();
        this.paymentStatus = new SimpleStringProperty();
        this.paymentPaidAt = new SimpleObjectProperty<>();
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId.get();
    }

    public IntegerProperty paymentIdProperty() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId.set(paymentId);
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

    public double getPaymentAmount() {
        return paymentAmount.get();
    }

    public DoubleProperty paymentAmountProperty() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount.set(paymentAmount);
    }

    public String getPaymentMethod() {
        return paymentMethod.get();
    }

    public StringProperty paymentMethodProperty() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod.set(paymentMethod);
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId.get();
    }

    public StringProperty paymentTransactionIdProperty() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId.set(paymentTransactionId);
    }

    public String getPaymentStatus() {
        return paymentStatus.get();
    }

    public StringProperty paymentStatusProperty() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus.set(paymentStatus);
    }

    public LocalDateTime getPaymentPaidAt() {
        return paymentPaidAt.get();
    }

    public ObjectProperty<LocalDateTime> paymentPaidAtProperty() {
        return paymentPaidAt;
    }

    public void setPaymentPaidAt(LocalDateTime paymentPaidAt) {
        this.paymentPaidAt.set(paymentPaidAt);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId.get() +
                ", orderId=" + orderId.get() +
                ", paymentAmount=" + paymentAmount.get() +
                ", paymentMethod='" + paymentMethod.get() + '\'' +
                ", paymentStatus='" + paymentStatus.get() + '\'' +
                ", paymentPaidAt=" + paymentPaidAt.get() +
                '}';
    }
}

