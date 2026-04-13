package com.sait.workshop05.models;

public class CustomerOption {
    private final String customerId;
    private final String fullName;
    private final int rewardBalance;

    public CustomerOption(String customerId, String fullName, int rewardBalance) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.rewardBalance = rewardBalance;
    }

    public String getCustomerId() {
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
        return customerId + " \u2014 " + fullName;
    }
}
