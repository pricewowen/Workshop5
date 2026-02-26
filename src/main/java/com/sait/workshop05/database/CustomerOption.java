package com.sait.workshop05.database;

public class CustomerOption {
    private final int customerId;
    private final String customerName;

    public CustomerOption(int customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public int getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }

    @Override
    public String toString() {
        return customerId + " - " + customerName;
    }
}
