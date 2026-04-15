package com.sait.workshop05.models;

public class CustomerOption {
    private final String customerId;
    private final String fullName;
    private final int rewardBalance;
    private final Integer addressId;

    public CustomerOption(String customerId, String fullName, int rewardBalance, Integer addressId) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.rewardBalance = rewardBalance;
        this.addressId = addressId;
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

    /** Address ID linked to this customer's profile, or {@code null} if none. */
    public Integer getAddressId() {
        return addressId;
    }

    @Override
    public String toString() {
        return customerId + " \u2014 " + fullName;
    }
}
