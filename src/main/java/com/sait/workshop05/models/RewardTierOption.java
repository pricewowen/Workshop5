// Contributor(s): Samantha
// Main: Samantha - Reward tier combo row for forms.

package com.sait.workshop05.models;

/**
 * Pick list row mapping tier id to label for reward tier ComboBox UIs.
 */
public class RewardTierOption {
    private final int rewardTierId;
    private final String tierName;

    /**
     * Creates one reward tier option row for picker controls.
     */
    public RewardTierOption(int rewardTierId, String tierName) {
        this.rewardTierId = rewardTierId;
        this.tierName = tierName;
    }

    /**
     * Returns reward tier id.
     */
    public int getRewardTierId() {
        return rewardTierId;
    }

    /**
     * Returns reward tier display name.
     */
    public String getTierName() {
        return tierName;
    }

    @Override
    public String toString() {
        return rewardTierId + " - " + tierName;
    }
}
