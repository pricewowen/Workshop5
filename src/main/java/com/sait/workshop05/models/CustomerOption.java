package com.sait.workshop05.models;

public class CustomerOption {
    private final int customerId;
    private final String fullName;
    private final int rewardBalance;

    public CustomerOption(int customerId, String fullName, int rewardBalance) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.rewardBalance = rewardBalance;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public int getRewardBalance() {
        return rewardBalance;
    }

    @Override
    public String toString() {
        return customerId + " - " + fullName;
    }
}
