// Contributor(s): Samantha
// Main: Samantha - Reward tier entity for loyalty bands.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.math.BigDecimal;

/**
 * Loyalty tier with point bounds and discount rate for staff CRUD.
 */
public class RewardTier {
    private final IntegerProperty rewardTierId = new SimpleIntegerProperty();
    private final StringProperty rewardTierName = new SimpleStringProperty();
    private final IntegerProperty rewardTierMinPoints = new SimpleIntegerProperty();
    private final IntegerProperty rewardTierMaxPoints = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> rewardTierDiscountRate = new SimpleObjectProperty<>();

    /**
     * Creates an empty reward tier row for JavaFX binding.
     */
    public RewardTier() { }

    /**
     * @return reward tier id value.
     */
    public int getRewardTierId() { return rewardTierId.get(); }
    /**
     * @param value reward tier id value.
     */
    public void setRewardTierId(int value) { rewardTierId.set(value); }
    /**
     * @return JavaFX property wrapper for reward tier id.
     */
    public IntegerProperty rewardTierIdProperty() { return rewardTierId; }

    /**
     * @return reward tier name value.
     */
    public String getRewardTierName() { return rewardTierName.get(); }
    /**
     * @param value reward tier name value.
     */
    public void setRewardTierName(String value) {rewardTierName.set(value);}
    /**
     * @return JavaFX property wrapper for reward tier name.
     */
    public StringProperty rewardTierNameProperty() { return rewardTierName; }

    /**
     * @return minimum points threshold.
     */
    public int getRewardTierMinPoints() { return rewardTierMinPoints.get(); }
    /**
     * @param value minimum points threshold.
     */
    public void setRewardTierMinPoints(int value) {rewardTierMinPoints.set(value);}
    /**
     * @return JavaFX property wrapper for minimum points threshold.
     */
    public IntegerProperty rewardTierMinPointsProperty() { return rewardTierMinPoints; }

    /**
     * @return maximum points threshold or {@code 0} when unlimited.
     */
    public int getRewardTierMaxPoints() { return rewardTierMaxPoints.get(); }
    /**
     * @param value maximum points threshold or {@code null} for unlimited.
     */
    public void setRewardTierMaxPoints(Integer value) {rewardTierMaxPoints.set(value == null ? 0 : value);}
    /**
     * @return JavaFX property wrapper for maximum points threshold.
     */
    public IntegerProperty rewardTierMaxPointsProperty() { return rewardTierMaxPoints; }

    /**
     * @return discount rate percentage value.
     */
    public BigDecimal getRewardTierDiscountRate() { return rewardTierDiscountRate.get(); }
    /**
     * @param value discount rate percentage value.
     */
    public void setRewardTierDiscountRate(BigDecimal value) {rewardTierDiscountRate.set(value);}
    /**
     * @return JavaFX property wrapper for discount rate percentage.
     */
    public ObjectProperty<BigDecimal> rewardTierDiscountRateProperty() { return rewardTierDiscountRate; }

    /**
     * @return debug-friendly summary string for the reward tier row.
     */
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