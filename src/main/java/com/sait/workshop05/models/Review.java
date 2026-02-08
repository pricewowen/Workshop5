package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Review model representing a customer review for a product.
 * Maps to the Review table in the database.
 */
public class Review {
    private final IntegerProperty reviewId;
    private final IntegerProperty customerId;
    private final IntegerProperty productId;
    private final IntegerProperty employeeId;
    private final IntegerProperty reviewRating;
    private final StringProperty reviewComment;
    private final ObjectProperty<LocalDateTime> reviewSubmittedDate;
    private final StringProperty reviewStatus;
    private final ObjectProperty<LocalDateTime> reviewApprovalDate;

    // Display properties
    private final StringProperty customerDisplay;
    private final StringProperty productDisplay;
    private final StringProperty employeeDisplay;

    /**
     * Default constructor - initializes all properties
     */
    public Review() {
        this.reviewId = new SimpleIntegerProperty();
        this.customerId = new SimpleIntegerProperty();
        this.productId = new SimpleIntegerProperty();
        this.employeeId = new SimpleIntegerProperty();
        this.reviewRating = new SimpleIntegerProperty();
        this.reviewComment = new SimpleStringProperty();
        this.reviewSubmittedDate = new SimpleObjectProperty<>();
        this.reviewStatus = new SimpleStringProperty();
        this.reviewApprovalDate = new SimpleObjectProperty<>();
        this.customerDisplay = new SimpleStringProperty();
        this.productDisplay = new SimpleStringProperty();
        this.employeeDisplay = new SimpleStringProperty();
    }

    // Getters and Setters
    public int getReviewId() {
        return reviewId.get();
    }

    public IntegerProperty reviewIdProperty() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId.set(reviewId);
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public IntegerProperty customerIdProperty() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public int getProductId() {
        return productId.get();
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public int getEmployeeId() {
        return employeeId.get();
    }

    public IntegerProperty employeeIdProperty() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId.set(employeeId);
    }

    public int getReviewRating() {
        return reviewRating.get();
    }

    public IntegerProperty reviewRatingProperty() {
        return reviewRating;
    }

    public void setReviewRating(int reviewRating) {
        this.reviewRating.set(reviewRating);
    }

    public String getReviewComment() {
        return reviewComment.get();
    }

    public StringProperty reviewCommentProperty() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment.set(reviewComment);
    }

    public LocalDateTime getReviewSubmittedDate() {
        return reviewSubmittedDate.get();
    }

    public ObjectProperty<LocalDateTime> reviewSubmittedDateProperty() {
        return reviewSubmittedDate;
    }

    public void setReviewSubmittedDate(LocalDateTime reviewSubmittedDate) {
        this.reviewSubmittedDate.set(reviewSubmittedDate);
    }

    public String getReviewStatus() {
        return reviewStatus.get();
    }

    public StringProperty reviewStatusProperty() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus.set(reviewStatus);
    }

    public LocalDateTime getReviewApprovalDate() {
        return reviewApprovalDate.get();
    }

    public ObjectProperty<LocalDateTime> reviewApprovalDateProperty() {
        return reviewApprovalDate;
    }

    public void setReviewApprovalDate(LocalDateTime reviewApprovalDate) {
        this.reviewApprovalDate.set(reviewApprovalDate);
    }

    public String getCustomerDisplay() {
        return customerDisplay.get();
    }

    public StringProperty customerDisplayProperty() {
        return customerDisplay;
    }

    public void setCustomerDisplay(String customerDisplay) {
        this.customerDisplay.set(customerDisplay);
    }

    public String getProductDisplay() {
        return productDisplay.get();
    }

    public StringProperty productDisplayProperty() {
        return productDisplay;
    }

    public void setProductDisplay(String productDisplay) {
        this.productDisplay.set(productDisplay);
    }

    public String getEmployeeDisplay() {
        return employeeDisplay.get();
    }

    public StringProperty employeeDisplayProperty() {
        return employeeDisplay;
    }

    public void setEmployeeDisplay(String employeeDisplay) {
        this.employeeDisplay.set(employeeDisplay);
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId.get() +
                ", customerId=" + customerId.get() +
                ", productId=" + productId.get() +
                ", reviewRating=" + reviewRating.get() +
                ", reviewStatus='" + reviewStatus.get() + '\'' +
                ", reviewSubmittedDate=" + reviewSubmittedDate.get() +
                '}';
    }
}

