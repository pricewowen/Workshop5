// Contributor(s): Mason
// Main: Mason - Bakery combo option for pick lists.

package com.sait.workshop05.models;

/**
 * Bakery id and display name for ComboBox value types.
 */
public class BakeryOption {
    private final int bakeryId;
    private final String bakeryName;

    /**
     * Creates one bakery option row for pickers.
     */
    public BakeryOption(int bakeryId, String bakeryName) {
        this.bakeryId = bakeryId;
        this.bakeryName = bakeryName;
    }

    /**
     * Returns bakery id.
     */
    public int getBakeryId() {
        return bakeryId;
    }

    /**
     * Returns bakery display name.
     */
    public String getBakeryName() {
        return bakeryName;
    }

    @Override
    public String toString() {
        return bakeryId + " - " + bakeryName;
    }
}
