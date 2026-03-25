package com.sait.workshop05.models;

public class AddressOption {
    private final int addressId;
    private final String line1;
    private final String city;
    private final String province;
    private final String postalCode;

    public AddressOption(int addressId, String line1, String city, String province, String postalCode) {
        this.addressId = addressId;
        this.line1 = line1;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
    }

    public int getAddressId() {
        return addressId;
    }

    public String getLine1() {
        return line1;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public String toString() {
        String c = (city == null) ? "" : (city.trim() + ", ");
        return addressId + " - " + line1 + " (" + c + province + " " + postalCode + ")";
    }
}
