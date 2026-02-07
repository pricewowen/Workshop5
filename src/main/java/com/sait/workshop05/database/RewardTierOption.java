package com.sait.workshop05.database;

public class RewardTierOption {
    private final int rewardTierId;
    private final String tierName;

    public RewardTierOption(int rewardTierId, String tierName) {
        this.rewardTierId = rewardTierId;
        this.tierName = tierName;
    }

    public int getRewardTierId() {
        return rewardTierId;
    }

    public String getTierName() {
        return tierName;
    }

    @Override
    public String toString() {
        return rewardTierId + " - " + tierName;
    }
}
