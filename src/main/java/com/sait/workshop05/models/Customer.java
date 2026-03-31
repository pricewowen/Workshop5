package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Customer model representing a customer in the system.
 * Maps to the Customer table in the database.
 */
public class Customer {
    private final IntegerProperty customerId;
    private final IntegerProperty userId;
    private final IntegerProperty addressId;
    private final IntegerProperty rewardTierId;
    private final StringProperty firstName;
    private final StringProperty middleInitial;
    private final StringProperty lastName;
    private final StringProperty role;
    private final StringProperty phone;
    private final StringProperty businessPhone;
    private final StringProperty email;
    private final IntegerProperty rewardBalance;
    private final ObjectProperty<LocalDateTime> tierAssignedDate;

    // Display properties for foreign keys
    private final StringProperty userDisplay;
    private final StringProperty addressDisplay;
    private final StringProperty rewardTierDisplay;

    /**
     * Default constructor - initializes all properties
     */
    public Customer() {
        this.customerId = new SimpleIntegerProperty();
        this.userId = new SimpleIntegerProperty();
        this.addressId = new SimpleIntegerProperty();
        this.rewardTierId = new SimpleIntegerProperty();
        this.firstName = new SimpleStringProperty();
        this.middleInitial = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();
        this.role = new SimpleStringProperty();
        this.phone = new SimpleStringProperty();
        this.businessPhone = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.rewardBalance = new SimpleIntegerProperty();
        this.tierAssignedDate = new SimpleObjectProperty<>();
        this.userDisplay = new SimpleStringProperty();
        this.addressDisplay = new SimpleStringProperty();
        this.rewardTierDisplay = new SimpleStringProperty();
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId.get();
    }

    public IntegerProperty customerIdProperty() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public int getAddressId() {
        return addressId.get();
    }

    public IntegerProperty addressIdProperty() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId.set(addressId);
    }

    public int getRewardTierId() {
        return rewardTierId.get();
    }

    public IntegerProperty rewardTierIdProperty() {
        return rewardTierId;
    }

    public void setRewardTierId(int rewardTierId) {
        this.rewardTierId.set(rewardTierId);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getMiddleInitial() {
        return middleInitial.get();
    }

    public StringProperty middleInitialProperty() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial.set(middleInitial);
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public String getPhone() {
        return phone.get();
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public String getBusinessPhone() {
        return businessPhone.get();
    }

    public StringProperty businessPhoneProperty() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone.set(businessPhone);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public int getRewardBalance() {
        return rewardBalance.get();
    }

    public IntegerProperty rewardBalanceProperty() {
        return rewardBalance;
    }

    public void setRewardBalance(int rewardBalance) {
        this.rewardBalance.set(rewardBalance);
    }

    public LocalDateTime getTierAssignedDate() {
        return tierAssignedDate.get();
    }

    public ObjectProperty<LocalDateTime> tierAssignedDateProperty() {
        return tierAssignedDate;
    }

    public void setTierAssignedDate(LocalDateTime tierAssignedDate) {
        this.tierAssignedDate.set(tierAssignedDate);
    }

    public String getUserDisplay() {
        return userDisplay.get();
    }

    public StringProperty userDisplayProperty() {
        return userDisplay;
    }

    public void setUserDisplay(String userDisplay) {
        this.userDisplay.set(userDisplay);
    }

    public String getAddressDisplay() {
        return addressDisplay.get();
    }

    public StringProperty addressDisplayProperty() {
        return addressDisplay;
    }

    public void setAddressDisplay(String addressDisplay) {
        this.addressDisplay.set(addressDisplay);
    }

    public String getRewardTierDisplay() {
        return rewardTierDisplay.get();
    }

    public StringProperty rewardTierDisplayProperty() {
        return rewardTierDisplay;
    }

    public void setRewardTierDisplay(String rewardTierDisplay) {
        this.rewardTierDisplay.set(rewardTierDisplay);
    }

    /**
     * Get full name of customer
     */
    public String getFullName() {
        String middle = middleInitial.get() != null && !middleInitial.get().isEmpty()
                ? middleInitial.get() + " "
                : "";
        return firstName.get() + " " + middle + lastName.get();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId.get() +
                ", userId=" + userId.get() +
                ", name='" + getFullName() + '\'' +
                ", email='" + email.get() + '\'' +
                ", rewardBalance=" + rewardBalance.get() +
                '}';
    }
}

