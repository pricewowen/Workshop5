// Contributor(s): Owen
// Main: Owen - Employee profile model for staff CRUD.

package com.sait.workshop05.models;

import javafx.beans.property.*;

/**
 * Employee profile with JavaFX properties for table views and search filters and edit dialogs.
 */
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

    /**
     * Creates an empty employee row for JavaFX binding.
     */
    public Employee() { }

    /** @return employee id value. */
    public String getEmployeeId() { return employeeId.get(); }
    /** @param value employee id value. */
    public void setEmployeeId(String value) { employeeId.set(value != null ? value : ""); }
    /** @return JavaFX property wrapper for employee id. */
    public StringProperty employeeIdProperty() { return employeeId; }

    /** @return linked user id value. */
    public String getUserId() { return userId.get(); }
    /** @param value linked user id value. */
    public void setUserId(String value) { userId.set(value != null ? value : ""); }
    /** @return JavaFX property wrapper for linked user id. */
    public StringProperty userIdProperty() { return userId; }

    /** @return bakery id value. */
    public int getBakeryId() { return bakeryId.get(); }
    /** @param value bakery id value. */
    public void setBakeryId(int value) { bakeryId.set(value); }
    /** @return JavaFX property wrapper for bakery id. */
    public IntegerProperty bakeryIdProperty() { return bakeryId; }

    /** @return address id value. */
    public int getAddressId() { return addressId.get(); }
    /** @param value address id value. */
    public void setAddressId(int value) { addressId.set(value); }
    /** @return JavaFX property wrapper for address id. */
    public IntegerProperty addressIdProperty() { return addressId; }

    /** @return first name value. */
    public String getEmployeeFirstName() { return employeeFirstName.get(); }
    /** @param value first name value. */
    public void setEmployeeFirstName(String value) { employeeFirstName.set(value); }
    /** @return JavaFX property wrapper for first name. */
    public StringProperty employeeFirstNameProperty() { return employeeFirstName; }

    /** @return middle initial value. */
    public String getEmployeeMiddleInitial() { return employeeMiddleInitial.get(); }
    /** @param value middle initial value. */
    public void setEmployeeMiddleInitial(String value) { employeeMiddleInitial.set(value); }
    /** @return JavaFX property wrapper for middle initial. */
    public StringProperty employeeMiddleInitialProperty() { return employeeMiddleInitial; }

    /** @return last name value. */
    public String getEmployeeLastName() { return employeeLastName.get(); }
    /** @param value last name value. */
    public void setEmployeeLastName(String value) { employeeLastName.set(value); }
    /** @return JavaFX property wrapper for last name. */
    public StringProperty employeeLastNameProperty() { return employeeLastName; }

    /** @return role value. */
    public String getEmployeeRole() { return employeeRole.get(); }
    /** @param value role value. */
    public void setEmployeeRole(String value) { employeeRole.set(value); }
    /** @return JavaFX property wrapper for role. */
    public StringProperty employeeRoleProperty() { return employeeRole; }

    /** @return phone value. */
    public String getEmployeePhone() { return employeePhone.get(); }
    /** @param value phone value. */
    public void setEmployeePhone(String value) { employeePhone.set(value); }
    /** @return JavaFX property wrapper for phone. */
    public StringProperty employeePhoneProperty() { return employeePhone; }

    /** @return business phone value. */
    public String getEmployeeBusinessPhone() { return employeeBusinessPhone.get(); }
    /** @param value business phone value. */
    public void setEmployeeBusinessPhone(String value) { employeeBusinessPhone.set(value); }
    /** @return JavaFX property wrapper for business phone. */
    public StringProperty employeeBusinessPhoneProperty() { return employeeBusinessPhone; }

    /** @return email value. */
    public String getEmployeeEmail() { return employeeEmail.get(); }
    /** @param value email value. */
    public void setEmployeeEmail(String value) { employeeEmail.set(value); }
    /** @return JavaFX property wrapper for email. */
    public StringProperty employeeEmailProperty() { return employeeEmail; }

    /** @return linked user display label. */
    public String getUserDisplay() { return userDisplay.get(); }
    /** @param value linked user display label. */
    public void setUserDisplay(String value) { userDisplay.set(value); }
    /** @return JavaFX property wrapper for linked user display label. */
    public StringProperty userDisplayProperty() { return userDisplay; }

    /** @return linked address display label. */
    public String getAddressDisplay() { return addressDisplay.get(); }
    /** @param value linked address display label. */
    public void setAddressDisplay(String value) { addressDisplay.set(value); }
    /** @return JavaFX property wrapper for linked address display label. */
    public StringProperty addressDisplayProperty() { return addressDisplay; }

    /** @return linked bakery display label. */
    public String getBakeryDisplay() { return bakeryDisplay.get(); }
    /** @param value linked bakery display label. */
    public void setBakeryDisplay(String value) { bakeryDisplay.set(value != null ? value : ""); }
    /** @return JavaFX property wrapper for linked bakery display label. */
    public StringProperty bakeryDisplayProperty() { return bakeryDisplay; }
}
