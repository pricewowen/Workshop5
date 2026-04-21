// Contributor(s): Samantha
// Main: Samantha - Review placeholder model for future staff views.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * JavaFX review row for moderation and reporting views.
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

    // Display labels make related ids readable in staff tables.
    private final StringProperty customerDisplay;
    private final StringProperty productDisplay;
    private final StringProperty employeeDisplay;

    /**
     * Initializes JavaFX properties for table and form binding.
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

    /** @return review id value. */
    public int getReviewId() {
        return reviewId.get();
    }

    /** @return JavaFX property wrapper for review id. */
    public IntegerProperty reviewIdProperty() {
        return reviewId;
    }

    /** @param reviewId review id value. */
    public void setReviewId(int reviewId) {
        this.reviewId.set(reviewId);
    }

    /** @return customer id value. */
    public int getCustomerId() {
        return customerId.get();
    }

    /** @return JavaFX property wrapper for customer id. */
    public IntegerProperty customerIdProperty() {
        return customerId;
    }

    /** @param customerId customer id value. */
    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    /** @return product id value. */
    public int getProductId() {
        return productId.get();
    }

    /** @return JavaFX property wrapper for product id. */
    public IntegerProperty productIdProperty() {
        return productId;
    }

    /** @param productId product id value. */
    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    /** @return employee id value. */
    public int getEmployeeId() {
        return employeeId.get();
    }

    /** @return JavaFX property wrapper for employee id. */
    public IntegerProperty employeeIdProperty() {
        return employeeId;
    }

    /** @param employeeId employee id value. */
    public void setEmployeeId(int employeeId) {
        this.employeeId.set(employeeId);
    }

    /** @return review rating value. */
    public int getReviewRating() {
        return reviewRating.get();
    }

    /** @return JavaFX property wrapper for review rating. */
    public IntegerProperty reviewRatingProperty() {
        return reviewRating;
    }

    /** @param reviewRating review rating value. */
    public void setReviewRating(int reviewRating) {
        this.reviewRating.set(reviewRating);
    }

    /** @return review comment value. */
    public String getReviewComment() {
        return reviewComment.get();
    }

    /** @return JavaFX property wrapper for review comment. */
    public StringProperty reviewCommentProperty() {
        return reviewComment;
    }

    /** @param reviewComment review comment value. */
    public void setReviewComment(String reviewComment) {
        this.reviewComment.set(reviewComment);
    }

    /** @return review submitted date value. */
    public LocalDateTime getReviewSubmittedDate() {
        return reviewSubmittedDate.get();
    }

    /** @return JavaFX property wrapper for submitted date. */
    public ObjectProperty<LocalDateTime> reviewSubmittedDateProperty() {
        return reviewSubmittedDate;
    }

    /** @param reviewSubmittedDate review submitted date value. */
    public void setReviewSubmittedDate(LocalDateTime reviewSubmittedDate) {
        this.reviewSubmittedDate.set(reviewSubmittedDate);
    }

    /** @return review status value. */
    public String getReviewStatus() {
        return reviewStatus.get();
    }

    /** @return JavaFX property wrapper for review status. */
    public StringProperty reviewStatusProperty() {
        return reviewStatus;
    }

    /** @param reviewStatus review status value. */
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus.set(reviewStatus);
    }

    /** @return review approval date value. */
    public LocalDateTime getReviewApprovalDate() {
        return reviewApprovalDate.get();
    }

    /** @return JavaFX property wrapper for approval date. */
    public ObjectProperty<LocalDateTime> reviewApprovalDateProperty() {
        return reviewApprovalDate;
    }

    /** @param reviewApprovalDate review approval date value. */
    public void setReviewApprovalDate(LocalDateTime reviewApprovalDate) {
        this.reviewApprovalDate.set(reviewApprovalDate);
    }

    /** @return customer display label. */
    public String getCustomerDisplay() {
        return customerDisplay.get();
    }

    /** @return JavaFX property wrapper for customer display label. */
    public StringProperty customerDisplayProperty() {
        return customerDisplay;
    }

    /** @param customerDisplay customer display label. */
    public void setCustomerDisplay(String customerDisplay) {
        this.customerDisplay.set(customerDisplay);
    }

    /** @return product display label. */
    public String getProductDisplay() {
        return productDisplay.get();
    }

    /** @return JavaFX property wrapper for product display label. */
    public StringProperty productDisplayProperty() {
        return productDisplay;
    }

    /** @param productDisplay product display label. */
    public void setProductDisplay(String productDisplay) {
        this.productDisplay.set(productDisplay);
    }

    /** @return employee display label. */
    public String getEmployeeDisplay() {
        return employeeDisplay.get();
    }

    /** @return JavaFX property wrapper for employee display label. */
    public StringProperty employeeDisplayProperty() {
        return employeeDisplay;
    }

    /** @param employeeDisplay employee display label. */
    public void setEmployeeDisplay(String employeeDisplay) {
        this.employeeDisplay.set(employeeDisplay);
    }

    /**
     * @return debug-friendly summary string for the review row.
     */
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

