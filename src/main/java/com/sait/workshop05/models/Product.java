// Contributor(s): Samantha
// Main: Samantha - Product entity for catalog management.

package com.sait.workshop05.models;

import javafx.beans.property.*;

/**
 * JavaFX product row used by catalog and specials screens.
 */
public class Product {
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final StringProperty productDescription;
    private final DoubleProperty productBasePrice;
    private final StringProperty tagsDisplay;
    private final StringProperty imageUrl;

    /**
     * Initializes JavaFX properties used by form and table bindings.
     */
    public Product() {
        this.productId = new SimpleIntegerProperty();
        this.productName = new SimpleStringProperty();
        this.productDescription = new SimpleStringProperty();
        this.productBasePrice = new SimpleDoubleProperty();
        this.tagsDisplay = new SimpleStringProperty();
        this.imageUrl = new SimpleStringProperty();
    }

    /**
     * Convenience constructor for seeded or mapped product rows.
     */
    public Product(int productId, String productName, String productDescription, double productBasePrice) {
        this();
        setProductId(productId);
        setProductName(productName);
        setProductDescription(productDescription);
        setProductBasePrice(productBasePrice);
    }

    /** Returns product id value. */
    public int getProductId() {
        return productId.get();
    }

    /** Returns JavaFX property wrapper for product id. */
    public IntegerProperty productIdProperty() {
        return productId;
    }

    /** Sets product id value. */
    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    /** Returns product name value. */
    public String getProductName() {
        return productName.get();
    }

    /** Returns JavaFX property wrapper for product name. */
    public StringProperty productNameProperty() {
        return productName;
    }

    /** Sets product name value. */
    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    /** Returns product description value. */
    public String getProductDescription() {
        return productDescription.get();
    }

    /** Returns JavaFX property wrapper for description. */
    public StringProperty productDescriptionProperty() {
        return productDescription;
    }

    /** Sets product description value. */
    public void setProductDescription(String productDescription) {
        this.productDescription.set(productDescription);
    }

    /** Returns base price value. */
    public double getProductBasePrice() {
        return productBasePrice.get();
    }

    /** Returns JavaFX property wrapper for base price. */
    public DoubleProperty productBasePriceProperty() {
        return productBasePrice;
    }

    /** Sets base price value. */
    public void setProductBasePrice(double productBasePrice) {
        this.productBasePrice.set(productBasePrice);
    }

    /** Returns derived tag display text for UI tables. */
    public String getTagsDisplay() {
        return tagsDisplay.get();
    }

    /** Returns JavaFX property wrapper for tag display text. */
    public StringProperty tagsDisplayProperty() {
        return tagsDisplay;
    }

    /** Sets derived tag display text for UI tables. */
    public void setTagsDisplay(String tagsDisplay) {
        this.tagsDisplay.set(tagsDisplay);
    }

    /** Returns product image URL value. */
    public String getImageUrl() {
        return imageUrl.get();
    }

    /** Returns JavaFX property wrapper for image URL. */
    public StringProperty imageUrlProperty() {
        return imageUrl;
    }

    /** Sets product image URL value. */
    public void setImageUrl(String imageUrl) {
        this.imageUrl.set(imageUrl);
    }

    /** Returns true when image URL is present. */
    public boolean hasImage() {
        return imageUrl.get() != null && !imageUrl.get().isEmpty();
    }

    /** Returns description alias used by older UI bindings. */
    public String getDescription() {
        return getProductDescription();
    }

    /** Sets description alias used by older UI bindings. */
    public void setDescription(String description) {
        setProductDescription(description);
    }

    /** Returns base price alias used by older UI bindings. */
    public double getPrice() {
        return getProductBasePrice();
    }

    /** Sets base price alias used by older UI bindings. */
    public void setPrice(double price) {
        setProductBasePrice(price);
    }

    /**
     * @return debug-friendly summary string for the product row.
     */
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId.get() +
                ", productName='" + productName.get() + '\'' +
                ", productBasePrice=" + productBasePrice.get() +
                '}';
    }
}

