// Contributor(s): Samantha
// Main: Samantha - Reward transaction row for loyalty ledger.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Loyalty ledger row for points earned on an order with display strings for tables.
 */
public class Reward {
    private final StringProperty rewardId = new SimpleStringProperty();
    private final StringProperty customerId = new SimpleStringProperty();
    private final StringProperty orderId = new SimpleStringProperty();
    private final IntegerProperty rewardPointsEarned = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> rewardTransactionDate = new SimpleObjectProperty<>();

    private final StringProperty customerDisplay = new SimpleStringProperty();
    private final StringProperty orderDisplay = new SimpleStringProperty();

    /**
     * Creates an empty reward row for JavaFX binding.
     */
    public Reward() { }

    /**
     * @return reward id value.
     */
    public String getRewardId() { return rewardId.get(); }
    /**
     * @param value reward id value.
     */
    public void setRewardId(String value) { rewardId.set(value); }
    /**
     * @return JavaFX property wrapper for reward id.
     */
    public StringProperty rewardIdProperty() { return rewardId; }

    /**
     * @return customer id value.
     */
    public String getCustomerId() { return customerId.get(); }
    /**
     * @param value customer id value.
     */
    public void setCustomerId(String value) { customerId.set(value); }
    /**
     * @return JavaFX property wrapper for customer id.
     */
    public StringProperty customerIdProperty() { return customerId; }

    /**
     * @return order id value.
     */
    public String getOrderId() { return orderId.get(); }
    /**
     * @param value order id value.
     */
    public void setOrderId(String value) { orderId.set(value); }
    /**
     * @return JavaFX property wrapper for order id.
     */
    public StringProperty orderIdProperty() { return orderId; }

    /**
     * @return points earned value.
     */
    public int getRewardPointsEarned() { return rewardPointsEarned.get(); }
    /**
     * @param value points earned value.
     */
    public void setRewardPointsEarned(int value) { rewardPointsEarned.set(value); }
    /**
     * @return JavaFX property wrapper for points earned.
     */
    public IntegerProperty rewardPointsEarnedProperty() { return rewardPointsEarned; }

    /**
     * @return transaction date value.
     */
    public LocalDateTime getRewardTransactionDate() { return rewardTransactionDate.get(); }
    /**
     * @param value transaction date value.
     */
    public void setRewardTransactionDate(LocalDateTime value) { rewardTransactionDate.set(value); }
    /**
     * @return JavaFX property wrapper for transaction date.
     */
    public ObjectProperty<LocalDateTime> rewardTransactionDateProperty() { return rewardTransactionDate; }

    /**
     * @return customer display label.
     */
    public String getCustomerDisplay() { return customerDisplay.get(); }
    /**
     * @param value customer display label.
     */
    public void setCustomerDisplay(String value) { customerDisplay.set(value); }
    /**
     * @return JavaFX property wrapper for customer display label.
     */
    public StringProperty customerDisplayProperty() { return customerDisplay; }

    /**
     * @return order display label.
     */
    public String getOrderDisplay() { return orderDisplay.get(); }
    /**
     * @param value order display label.
     */
    public void setOrderDisplay(String value) { orderDisplay.set(value); }
    /**
     * @return JavaFX property wrapper for order display label.
     */
    public StringProperty orderDisplayProperty() { return orderDisplay; }

    /**
     * @return debug-friendly summary string for the reward row.
     */
    @Override
    public String toString() {
        return "Reward{" +
                "rewardId=" + rewardId.get() +
                ", customerId=" + customerId.get() +
                ", orderId=" + orderId.get() +
                ", rewardPointsEarned=" + rewardPointsEarned.get() +
                ", rewardTransactionDate='" + rewardTransactionDate.get() +
                '}';
    }
}