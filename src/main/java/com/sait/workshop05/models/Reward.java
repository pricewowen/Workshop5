package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Reward {
    private final IntegerProperty rewardId = new SimpleIntegerProperty();
    private final IntegerProperty customerId = new SimpleIntegerProperty();
    private final IntegerProperty orderId = new SimpleIntegerProperty();
    private final IntegerProperty rewardPointsEarned = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> rewardTransactionDate = new SimpleObjectProperty<>();

    private final StringProperty customerDisplay = new SimpleStringProperty();
    private final StringProperty orderDisplay = new SimpleStringProperty();

    public Reward() { }

    public int getRewardId() { return rewardId.get(); }
    public void setRewardId(int value) { rewardId.set(value); }
    public IntegerProperty rewardIdProperty() { return rewardId; }

    public int getCustomerId() { return customerId.get(); }
    public void setCustomerId(int value) { customerId.set(value); }
    public IntegerProperty customerIdProperty() { return customerId; }

    public int getOrderId() { return orderId.get(); }
    public void setOrderId(int value) { orderId.set(value); }
    public IntegerProperty orderIdProperty() { return orderId; }

    public int getRewardPointsEarned() { return rewardPointsEarned.get(); }
    public void setRewardPointsEarned(int value) { rewardPointsEarned.set(value); }
    public IntegerProperty rewardPointsEarnedProperty() { return rewardPointsEarned; }

    public LocalDateTime getRewardTransactionDate() { return rewardTransactionDate.get(); }
    public void setRewardTransactionDate(LocalDateTime value) { rewardTransactionDate.set(value); }
    public ObjectProperty<LocalDateTime> rewardTransactionDateProperty() { return rewardTransactionDate; }

    public String getCustomerDisplay() { return customerDisplay.get(); }
    public void setCustomerDisplay(String value) { customerDisplay.set(value); }
    public StringProperty customerDisplayProperty() { return customerDisplay; }

    public String getOrderDisplay() { return orderDisplay.get(); }
    public void setOrderDisplay(String value) { orderDisplay.set(value); }
    public StringProperty orderDisplayProperty() { return orderDisplay; }

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