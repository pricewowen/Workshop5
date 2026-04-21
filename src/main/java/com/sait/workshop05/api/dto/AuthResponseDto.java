// Contributor(s): Robbie
// Main: Robbie - Login response mapping for Workshop 7 auth JSON.

package com.sait.workshop05.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps fields returned after successful login. Extra JSON fields are ignored.
 */
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

    /**
     * Creates an empty auth response DTO for JSON binding.
     */
    public AuthResponseDto() {}

    /** Returns auth response row id. */
    public String getId() { return id; }
    /** Sets auth response row id. */
    public void setId(String id) { this.id = id; }

    /** Returns API user id. */
    public String getUserId() { return userId; }
    /** Sets API user id. */
    public void setUserId(String userId) { this.userId = userId; }

    /** Returns bakery id when present in login payload. */
    public int getBakeryId() { return bakeryId; }
    /** Sets bakery id when present in login payload. */
    public void setBakeryId(int bakeryId) { this.bakeryId = bakeryId; }

    /** Returns username field. */
    public String getUsername() { return username; }
    /** Sets username field. */
    public void setUsername(String username) { this.username = username; }

    /** Returns first name value. */
    public String getFirstName() { return firstName; }
    /** Sets first name value. */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** Returns middle initial value. */
    public String getMiddleInitial() { return middleInitial; }
    /** Sets middle initial value. */
    public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }

    /** Returns last name value. */
    public String getLastName() { return lastName; }
    /** Sets last name value. */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** Returns staff position value. */
    public String getPosition() { return position; }
    /** Sets staff position value. */
    public void setPosition(String position) { this.position = position; }

    /** Returns phone value. */
    public String getPhone() { return phone; }
    /** Sets phone value. */
    public void setPhone(String phone) { this.phone = phone; }

    /** Returns business phone value. */
    public String getBusinessPhone() { return businessPhone; }
    /** Sets business phone value. */
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    /** Returns work email value. */
    public String getWorkEmail() { return workEmail; }
    /** Sets work email value. */
    public void setWorkEmail(String workEmail) { this.workEmail = workEmail; }

    /** Returns linked address id. */
    public int getAddressId() { return addressId; }
    /** Sets linked address id. */
    public void setAddressId(int addressId) { this.addressId = addressId; }

    /** Returns profile photo path value. */
    public String getProfilePhotoPath() { return profilePhotoPath; }
    /** Sets profile photo path value. */
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }

    /** Returns whether profile photo approval is pending. */
    public boolean isPhotoApprovalPending() { return photoApprovalPending; }
    /** Sets whether profile photo approval is pending. */
    public void setPhotoApprovalPending(boolean photoApprovalPending) { this.photoApprovalPending = photoApprovalPending; }

    /** Returns JWT token value. */
    public String getToken() { return token; }
    /** Sets JWT token value. */
    public void setToken(String token) { this.token = token; }

    /** Returns role value. */
    public String getRole() { return role; }
    /** Sets role value. */
    public void setRole(String role) { this.role = role; }

    /** Returns sign-in email value. */
    public String getEmail() { return email; }
    /** Sets sign-in email value. */
    public void setEmail(String email) { this.email = email; }

}
