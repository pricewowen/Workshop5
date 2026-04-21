// Contributor(s): Robbie
// Main: Robbie - Customer profile model for admin customer screens.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * JavaFX customer row used by profile and customer admin screens.
 */
public class Customer {
    /** Customer row id (UUID string from API). */
    private final StringProperty customerId;
    /** Auth user id (UUID string). */
    private final StringProperty userId;
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

    // Display values keep linked ids readable in the table layer.
    private final StringProperty userDisplay;
    private final StringProperty addressDisplay;
    private final StringProperty rewardTierDisplay;

    /**
     * Initializes JavaFX properties for binding and null safe display updates.
     */
    public Customer() {
        this.customerId = new SimpleStringProperty();
        this.userId = new SimpleStringProperty();
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

    /** @return customer id value. */
    public String getCustomerId() {
        return customerId.get();
    }

    /** @return JavaFX property wrapper for customer id. */
    public StringProperty customerIdProperty() {
        return customerId;
    }

    /** @param customerId customer id value or {@code null}. */
    public void setCustomerId(String customerId) {
        this.customerId.set(customerId != null ? customerId : "");
    }

    /** @return linked auth user id value. */
    public String getUserId() {
        return userId.get();
    }

    /** @return JavaFX property wrapper for linked auth user id. */
    public StringProperty userIdProperty() {
        return userId;
    }

    /** @param userId linked auth user id value or {@code null}. */
    public void setUserId(String userId) {
        this.userId.set(userId != null ? userId : "");
    }

    /** @return linked address id value. */
    public int getAddressId() {
        return addressId.get();
    }

    /** @return JavaFX property wrapper for linked address id. */
    public IntegerProperty addressIdProperty() {
        return addressId;
    }

    /** @param addressId linked address id value. */
    public void setAddressId(int addressId) {
        this.addressId.set(addressId);
    }

    /** @return linked reward tier id value. */
    public int getRewardTierId() {
        return rewardTierId.get();
    }

    /** @return JavaFX property wrapper for reward tier id. */
    public IntegerProperty rewardTierIdProperty() {
        return rewardTierId;
    }

    /** @param rewardTierId linked reward tier id value. */
    public void setRewardTierId(int rewardTierId) {
        this.rewardTierId.set(rewardTierId);
    }

    /** @return first name value. */
    public String getFirstName() {
        return firstName.get();
    }

    /** @return JavaFX property wrapper for first name. */
    public StringProperty firstNameProperty() {
        return firstName;
    }

    /** @param firstName first name value. */
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    /** @return middle initial value. */
    public String getMiddleInitial() {
        return middleInitial.get();
    }

    /** @return JavaFX property wrapper for middle initial. */
    public StringProperty middleInitialProperty() {
        return middleInitial;
    }

    /** @param middleInitial middle initial value. */
    public void setMiddleInitial(String middleInitial) {
        this.middleInitial.set(middleInitial);
    }

    /** @return last name value. */
    public String getLastName() {
        return lastName.get();
    }

    /** @return JavaFX property wrapper for last name. */
    public StringProperty lastNameProperty() {
        return lastName;
    }

    /** @param lastName last name value. */
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    /** @return role value. */
    public String getRole() {
        return role.get();
    }

    /** @return JavaFX property wrapper for role. */
    public StringProperty roleProperty() {
        return role;
    }

    /** @param role role value. */
    public void setRole(String role) {
        this.role.set(role);
    }

    /** @return phone value. */
    public String getPhone() {
        return phone.get();
    }

    /** @return JavaFX property wrapper for phone. */
    public StringProperty phoneProperty() {
        return phone;
    }

    /** @param phone phone value. */
    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    /** @return business phone value. */
    public String getBusinessPhone() {
        return businessPhone.get();
    }

    /** @return JavaFX property wrapper for business phone. */
    public StringProperty businessPhoneProperty() {
        return businessPhone;
    }

    /** @param businessPhone business phone value. */
    public void setBusinessPhone(String businessPhone) {
        this.businessPhone.set(businessPhone);
    }

    /** @return email value. */
    public String getEmail() {
        return email.get();
    }

    /** @return JavaFX property wrapper for email. */
    public StringProperty emailProperty() {
        return email;
    }

    /** @param email email value. */
    public void setEmail(String email) {
        this.email.set(email);
    }

    /** @return reward balance value. */
    public int getRewardBalance() {
        return rewardBalance.get();
    }

    /** @return JavaFX property wrapper for reward balance. */
    public IntegerProperty rewardBalanceProperty() {
        return rewardBalance;
    }

    /** @param rewardBalance reward balance value. */
    public void setRewardBalance(int rewardBalance) {
        this.rewardBalance.set(rewardBalance);
    }

    /** @return tier assigned date value. */
    public LocalDateTime getTierAssignedDate() {
        return tierAssignedDate.get();
    }

    /** @return JavaFX property wrapper for tier assigned date. */
    public ObjectProperty<LocalDateTime> tierAssignedDateProperty() {
        return tierAssignedDate;
    }

    /** @param tierAssignedDate tier assigned date value. */
    public void setTierAssignedDate(LocalDateTime tierAssignedDate) {
        this.tierAssignedDate.set(tierAssignedDate);
    }

    /** @return linked user display label. */
    public String getUserDisplay() {
        return userDisplay.get();
    }

    /** @return JavaFX property wrapper for linked user display label. */
    public StringProperty userDisplayProperty() {
        return userDisplay;
    }

    /** @param userDisplay linked user display label. */
    public void setUserDisplay(String userDisplay) {
        this.userDisplay.set(userDisplay);
    }

    /** @return linked address display label. */
    public String getAddressDisplay() {
        return addressDisplay.get();
    }

    /** @return JavaFX property wrapper for linked address display label. */
    public StringProperty addressDisplayProperty() {
        return addressDisplay;
    }

    /** @param addressDisplay linked address display label. */
    public void setAddressDisplay(String addressDisplay) {
        this.addressDisplay.set(addressDisplay);
    }

    /** @return linked reward tier display label. */
    public String getRewardTierDisplay() {
        return rewardTierDisplay.get();
    }

    /** @return JavaFX property wrapper for linked reward tier display label. */
    public StringProperty rewardTierDisplayProperty() {
        return rewardTierDisplay;
    }

    /** @param rewardTierDisplay linked reward tier display label. */
    public void setRewardTierDisplay(String rewardTierDisplay) {
        this.rewardTierDisplay.set(rewardTierDisplay);
    }

    /**
     * Returns display-ready full name including middle initial when present.
     *
     * @return full name for table and form display.
     */
    public String getFullName() {
        String middle = middleInitial.get() != null && !middleInitial.get().isEmpty()
                ? middleInitial.get() + " "
                : "";
        return firstName.get() + " " + middle + lastName.get();
    }

    /**
     * @return debug-friendly summary string for the customer row.
     */
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

