package com.sait.workshop05.models;

public class Bakery {
    private int bakeryId;
    private Address address;
    private String bakeryName;
    private String bakeryPhone;
    private String bakeryEmail;

    public Bakery(int bakeryId, Address address, String bakeryName, String bakeryPhone, String bakeryEmail) {
        this.bakeryId = bakeryId;
        this.address = address;
        this.bakeryName = bakeryName;
        this.bakeryPhone = bakeryPhone;
        this.bakeryEmail = bakeryEmail;
    }

    public int getBakeryId() {
        return bakeryId;
    }

    public void setBakeryId(int bakeryId) {
        this.bakeryId = bakeryId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getBakeryName() {
        return bakeryName;
    }

    public void setBakeryName(String bakeryName) {
        this.bakeryName = bakeryName;
    }

    public String getBakeryPhone() {
        return bakeryPhone;
    }

    public void setBakeryPhone(String bakeryPhone) {
        this.bakeryPhone = bakeryPhone;
    }

    public String getBakeryEmail() {
        return bakeryEmail;
    }

    public void setBakeryEmail(String bakeryEmail) {
        this.bakeryEmail = bakeryEmail;
    }
}
