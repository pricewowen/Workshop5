package com.sait.workshop05.models;

public class Address {
    private int addressId;
    private String addressLine1;
    private String addressLine2;
    private String addressCity;
    private String addressProvince;
    private String addressPostalCode;

    public Address(int addressId, String addressLine1, String addressLine2, String addressCity, String addressProvince, String addressPostalCode) {
        this.addressId = addressId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressCity = addressCity;
        this.addressProvince = addressProvince;
        this.addressPostalCode = addressPostalCode;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressProvince() {
        return addressProvince;
    }

    public void setAddressProvince(String addressProvince) {
        this.addressProvince = addressProvince;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }
}
