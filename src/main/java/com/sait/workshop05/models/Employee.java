package com.sait.workshop05.models;

import javafx.beans.property.*;

public class Employee {

    private final StringProperty employeeId = new SimpleStringProperty();
    private final StringProperty userId = new SimpleStringProperty();
    private final IntegerProperty bakeryId = new SimpleIntegerProperty();
    private final IntegerProperty addressId = new SimpleIntegerProperty();

    private final StringProperty employeeFirstName = new SimpleStringProperty();
    private final StringProperty employeeMiddleInitial = new SimpleStringProperty();
    private final StringProperty employeeLastName = new SimpleStringProperty();
    private final StringProperty employeeRole = new SimpleStringProperty();
    private final StringProperty employeePhone = new SimpleStringProperty();
    private final StringProperty employeeBusinessPhone = new SimpleStringProperty();
    private final StringProperty employeeEmail = new SimpleStringProperty();

    private final StringProperty userDisplay = new SimpleStringProperty();
    private final StringProperty addressDisplay = new SimpleStringProperty();
    private final StringProperty bakeryDisplay = new SimpleStringProperty();

    public Employee() { }

    public String getEmployeeId() { return employeeId.get(); }
    public void setEmployeeId(String value) { employeeId.set(value != null ? value : ""); }
    public StringProperty employeeIdProperty() { return employeeId; }

    public String getUserId() { return userId.get(); }
    public void setUserId(String value) { userId.set(value != null ? value : ""); }
    public StringProperty userIdProperty() { return userId; }

    public int getBakeryId() { return bakeryId.get(); }
    public void setBakeryId(int value) { bakeryId.set(value); }
    public IntegerProperty bakeryIdProperty() { return bakeryId; }

    public int getAddressId() { return addressId.get(); }
    public void setAddressId(int value) { addressId.set(value); }
    public IntegerProperty addressIdProperty() { return addressId; }

    public String getEmployeeFirstName() { return employeeFirstName.get(); }
    public void setEmployeeFirstName(String value) { employeeFirstName.set(value); }
    public StringProperty employeeFirstNameProperty() { return employeeFirstName; }

    public String getEmployeeMiddleInitial() { return employeeMiddleInitial.get(); }
    public void setEmployeeMiddleInitial(String value) { employeeMiddleInitial.set(value); }
    public StringProperty employeeMiddleInitialProperty() { return employeeMiddleInitial; }

    public String getEmployeeLastName() { return employeeLastName.get(); }
    public void setEmployeeLastName(String value) { employeeLastName.set(value); }
    public StringProperty employeeLastNameProperty() { return employeeLastName; }

    public String getEmployeeRole() { return employeeRole.get(); }
    public void setEmployeeRole(String value) { employeeRole.set(value); }
    public StringProperty employeeRoleProperty() { return employeeRole; }

    public String getEmployeePhone() { return employeePhone.get(); }
    public void setEmployeePhone(String value) { employeePhone.set(value); }
    public StringProperty employeePhoneProperty() { return employeePhone; }

    public String getEmployeeBusinessPhone() { return employeeBusinessPhone.get(); }
    public void setEmployeeBusinessPhone(String value) { employeeBusinessPhone.set(value); }
    public StringProperty employeeBusinessPhoneProperty() { return employeeBusinessPhone; }

    public String getEmployeeEmail() { return employeeEmail.get(); }
    public void setEmployeeEmail(String value) { employeeEmail.set(value); }
    public StringProperty employeeEmailProperty() { return employeeEmail; }

    public String getUserDisplay() { return userDisplay.get(); }
    public void setUserDisplay(String value) { userDisplay.set(value); }
    public StringProperty userDisplayProperty() { return userDisplay; }

    public String getAddressDisplay() { return addressDisplay.get(); }
    public void setAddressDisplay(String value) { addressDisplay.set(value); }
    public StringProperty addressDisplayProperty() { return addressDisplay; }

    public String getBakeryDisplay() { return bakeryDisplay.get(); }
    public void setBakeryDisplay(String value) { bakeryDisplay.set(value != null ? value : ""); }
    public StringProperty bakeryDisplayProperty() { return bakeryDisplay; }
}
