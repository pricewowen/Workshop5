// Contributor(s): Robbie
// Main: Robbie - Payment summary fields for order views.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * JavaFX payment row used in order payment summaries.
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
     * Initializes JavaFX properties for UI binding and status refresh.
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

    /** Returns payment id value. */
    public int getPaymentId() {
        return paymentId.get();
    }

    /** Returns JavaFX property wrapper for payment id. */
    public IntegerProperty paymentIdProperty() {
        return paymentId;
    }

    /** Sets payment id value. */
    public void setPaymentId(int paymentId) {
        this.paymentId.set(paymentId);
    }

    /** Returns linked order id value. */
    public int getOrderId() {
        return orderId.get();
    }

    /** Returns JavaFX property wrapper for linked order id. */
    public IntegerProperty orderIdProperty() {
        return orderId;
    }

    /** Sets linked order id value. */
    public void setOrderId(int orderId) {
        this.orderId.set(orderId);
    }

    /** Returns payment amount value. */
    public double getPaymentAmount() {
        return paymentAmount.get();
    }

    /** Returns JavaFX property wrapper for payment amount. */
    public DoubleProperty paymentAmountProperty() {
        return paymentAmount;
    }

    /** Sets payment amount value. */
    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount.set(paymentAmount);
    }

    /** Returns payment method label. */
    public String getPaymentMethod() {
        return paymentMethod.get();
    }

    /** Returns JavaFX property wrapper for payment method. */
    public StringProperty paymentMethodProperty() {
        return paymentMethod;
    }

    /** Sets payment method label. */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod.set(paymentMethod);
    }

    /** Returns payment transaction id value. */
    public String getPaymentTransactionId() {
        return paymentTransactionId.get();
    }

    /** Returns JavaFX property wrapper for transaction id. */
    public StringProperty paymentTransactionIdProperty() {
        return paymentTransactionId;
    }

    /** Sets payment transaction id value. */
    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId.set(paymentTransactionId);
    }

    /** Returns payment status label. */
    public String getPaymentStatus() {
        return paymentStatus.get();
    }

    /** Returns JavaFX property wrapper for payment status. */
    public StringProperty paymentStatusProperty() {
        return paymentStatus;
    }

    /** Sets payment status label. */
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus.set(paymentStatus);
    }

    /** Returns paid-at timestamp value. */
    public LocalDateTime getPaymentPaidAt() {
        return paymentPaidAt.get();
    }

    /** Returns JavaFX property wrapper for paid-at timestamp. */
    public ObjectProperty<LocalDateTime> paymentPaidAtProperty() {
        return paymentPaidAt;
    }

    /** Sets paid-at timestamp value. */
    public void setPaymentPaidAt(LocalDateTime paymentPaidAt) {
        this.paymentPaidAt.set(paymentPaidAt);
    }

    /**
     * @return debug-friendly summary string for the payment row.
     */
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

