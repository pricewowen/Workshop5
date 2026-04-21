// Contributor(s): Mason
// Main: Mason - Bakery entity for location CRUD screens.

package com.sait.workshop05.models;

/**
 * Bakery branch with nested address for location CRUD screens.
 */
public class Bakery {
    private int bakeryId;
    private Address address;
    private String bakeryName;
    private String bakeryPhone;
    private String bakeryEmail;

    /**
     * Creates one bakery model row for CRUD screens.
     */
    public Bakery(int bakeryId, Address address, String bakeryName, String bakeryPhone, String bakeryEmail) {
        this.bakeryId = bakeryId;
        this.address = address;
        this.bakeryName = bakeryName;
        this.bakeryPhone = bakeryPhone;
        this.bakeryEmail = bakeryEmail;
    }

    /**
     * Returns bakery id.
     */
    public int getBakeryId() {
        return bakeryId;
    }

    /**
     * Sets bakery id.
     */
    public void setBakeryId(int bakeryId) {
        this.bakeryId = bakeryId;
    }

    /**
     * Returns linked bakery address value.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets linked bakery address value.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Returns bakery display name.
     */
    public String getBakeryName() {
        return bakeryName;
    }

    /**
     * Sets bakery display name.
     */
    public void setBakeryName(String bakeryName) {
        this.bakeryName = bakeryName;
    }

    /**
     * Returns bakery phone value.
     */
    public String getBakeryPhone() {
        return bakeryPhone;
    }

    /**
     * Sets bakery phone value.
     */
    public void setBakeryPhone(String bakeryPhone) {
        this.bakeryPhone = bakeryPhone;
    }

    /**
     * Returns bakery email value.
     */
    public String getBakeryEmail() {
        return bakeryEmail;
    }

    /**
     * Sets bakery email value.
     */
    public void setBakeryEmail(String bakeryEmail) {
        this.bakeryEmail = bakeryEmail;
    }
}
