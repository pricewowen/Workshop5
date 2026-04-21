// Contributor(s): Robbie
// Main: Robbie - Customer combo row for orders and references.

package com.sait.workshop05.models;

/**
 * Customer selector row used by order and reference pickers.
 */
public class CustomerOption {
    private final String customerId;
    private final String fullName;
    private final int rewardBalance;
    private final Integer addressId;

    /**
     * Creates one customer option row with display and reward metadata.
     */
    public CustomerOption(String customerId, String fullName, int rewardBalance, Integer addressId) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.rewardBalance = rewardBalance;
        this.addressId = addressId;
    }

    /**
     * Returns customer id value.
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Returns customer full name label.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns current reward balance value.
     */
    public int getRewardBalance() {
        return rewardBalance;
    }

    /** Address id linked to this customer profile or null when none exists. */
    public Integer getAddressId() {
        return addressId;
    }

    @Override
    public String toString() {
        return customerId + " \u2014 " + fullName;
    }
}
