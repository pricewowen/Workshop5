package com.sait.workshop05.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponseDto {
    private String id;
    private String userId;
    private int bakeryId;
    private String username;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String position;
    private String phone;
    private String businessPhone;
    private String workEmail;
    private int addressId;
    private String profilePhotoPath;
    private boolean photoApprovalPending;
    private String token;
    private String role;
    /** Sign-in email from API when present. */
    private String email;

    public AuthResponseDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getBakeryId() { return bakeryId; }
    public void setBakeryId(int bakeryId) { this.bakeryId = bakeryId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleInitial() { return middleInitial; }
    public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    public String getWorkEmail() { return workEmail; }
    public void setWorkEmail(String workEmail) { this.workEmail = workEmail; }

    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public String getProfilePhotoPath() { return profilePhotoPath; }
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }

    public boolean isPhotoApprovalPending() { return photoApprovalPending; }
    public void setPhotoApprovalPending(boolean photoApprovalPending) { this.photoApprovalPending = photoApprovalPending; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}
