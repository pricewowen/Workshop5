package com.sait.workshop05.models;

public class BakeryOption {
    private final int bakeryId;
    private final String bakeryName;

    public BakeryOption(int bakeryId, String bakeryName) {
        this.bakeryId = bakeryId;
        this.bakeryName = bakeryName;
    }

    public int getBakeryId() {
        return bakeryId;
    }

    public String getBakeryName() {
        return bakeryName;
    }

    @Override
    public String toString() {
        return bakeryId + " - " + bakeryName;
    }
}
