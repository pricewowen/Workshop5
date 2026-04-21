// Contributor(s): Owen
// Main: Owen - Address value type for forms and API mapping.

package com.sait.workshop05.models;

/**
 * Mutable address value for forms and REST payloads.
 */
public class Address {
    private int addressId;
    private String addressLine1;
    private String addressLine2;
    private String addressCity;
    private String addressProvince;
    private String addressPostalCode;

    /**
     * Creates one mutable address value object.
     */
    public Address(int addressId, String addressLine1, String addressLine2, String addressCity, String addressProvince, String addressPostalCode) {
        this.addressId = addressId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressCity = addressCity;
        this.addressProvince = addressProvince;
        this.addressPostalCode = addressPostalCode;
    }

    /**
     * Returns address id.
     */
    public int getAddressId() {
        return addressId;
    }

    /**
     * Sets address id.
     */
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    /**
     * Returns first address line.
     */
    public String getAddressLine1() {
        return addressLine1;
    }

    /**
     * Sets first address line.
     */
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * Returns second address line.
     */
    public String getAddressLine2() {
        return addressLine2;
    }

    /**
     * Sets second address line.
     */
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    /**
     * Returns city value.
     */
    public String getAddressCity() {
        return addressCity;
    }

    /**
     * Sets city value.
     */
    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    /**
     * Returns province code.
     */
    public String getAddressProvince() {
        return addressProvince;
    }

    /**
     * Sets province code.
     */
    public void setAddressProvince(String addressProvince) {
        this.addressProvince = addressProvince;
    }

    /**
     * Returns postal code value.
     */
    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    /**
     * Sets postal code value.
     */
    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }
}
