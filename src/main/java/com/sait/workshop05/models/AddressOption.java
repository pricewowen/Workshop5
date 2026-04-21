// Contributor(s): Owen
// Main: Owen - Address pick list row for admin screens.

package com.sait.workshop05.models;

/**
 * Address row for searchable ComboBox pickers with formatted labels in the editor.
 */
public class AddressOption {
    private final int addressId;
    private final String line1;
    private final String city;
    private final String province;
    private final String postalCode;

    /**
     * Creates one address option row for searchable selectors.
     */
    public AddressOption(int addressId, String line1, String city, String province, String postalCode) {
        this.addressId = addressId;
        this.line1 = line1;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
    }

    /**
     * Returns address id.
     */
    public int getAddressId() {
        return addressId;
    }

    /**
     * Returns first address line.
     */
    public String getLine1() {
        return line1;
    }

    /**
     * Returns city value.
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns province code.
     */
    public String getProvince() {
        return province;
    }

    /**
     * Returns postal code value.
     */
    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public String toString() {
        String c = (city == null) ? "" : (city.trim() + ", ");
        return addressId + " - " + line1 + " (" + c + province + " " + postalCode + ")";
    }
}
