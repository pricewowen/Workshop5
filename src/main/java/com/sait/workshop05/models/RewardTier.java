package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.math.BigDecimal;

public class RewardTier {
    private final IntegerProperty rewardTierId = new SimpleIntegerProperty();
    private final StringProperty rewardTierName = new SimpleStringProperty();
    private final IntegerProperty rewardTierMinPoints = new SimpleIntegerProperty();
    private final IntegerProperty rewardTierMaxPoints = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> rewardTierDiscountRate = new SimpleObjectProperty<>();

    public RewardTier() { }

    public int getRewardTierId() { return rewardTierId.get(); }
    public void setRewardTierId(int value) { rewardTierId.set(value); }
    public IntegerProperty rewardTierIdProperty() { return rewardTierId; }

    public String getRewardTierName() { return rewardTierName.get(); }
    public void setRewardTierName(String value) {rewardTierName.set(value);}
    public StringProperty rewardTierNameProperty() { return rewardTierName; }

    public int getRewardTierMinPoints() { return rewardTierMinPoints.get(); }
    public void setRewardTierMinPoints(int value) {rewardTierMinPoints.set(value);}
    public IntegerProperty rewardTierMinPointsProperty() { return rewardTierMinPoints; }

    public int getRewardTierMaxPoints() { return rewardTierMaxPoints.get(); }
    public void setRewardTierMaxPoints(Integer value) {rewardTierMaxPoints.set(value == null ? 0 : value);}
    public IntegerProperty rewardTierMaxPointsProperty() { return rewardTierMaxPoints; }

    public BigDecimal getRewardTierDiscountRate() { return rewardTierDiscountRate.get(); }
    public void setRewardTierDiscountRate(BigDecimal value) {rewardTierDiscountRate.set(value);}
    public ObjectProperty<BigDecimal> rewardTierDiscountRateProperty() { return rewardTierDiscountRate; }

    @Override
    public String toString() {
        return "Loyalty Tier{" +
                "rewardTierId=" + rewardTierId.get() +
                ", rewardTierName=" + rewardTierName.get() +
                ", rewardTierMinPoints=" + rewardTierMinPoints.get() +
                ", rewardTierMaxPoints=" + rewardTierMaxPoints.get() +
                ", rewardTierDiscountRate='" + rewardTierDiscountRate.get() +
                '}';
    }
}