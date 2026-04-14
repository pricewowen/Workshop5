package com.sait.workshop05.models;

import javafx.beans.property.*;

/**
 * A special-product entry from the {@code product_special} table.
 * Carries the internal primary key and product ID internally so the controller can
 * issue PUT / DELETE requests, but neither field is shown as a table column.
 */
public class ProductSpecial {

    private final IntegerProperty productSpecialId;  // PK — kept for API calls, not displayed
    private final IntegerProperty productId;          // FK — kept for edit pre-selection, not displayed
    private final StringProperty  productName;
    private final StringProperty  productDescription;
    private final DoubleProperty  productBasePrice;
    private final DoubleProperty  discountPercent;
    private final StringProperty  featuredOn;
    private final StringProperty  imageUrl;

    /** Default constructor — initialises all JavaFX properties. */
    public ProductSpecial() {
        this.productSpecialId   = new SimpleIntegerProperty();
        this.productId          = new SimpleIntegerProperty();
        this.productName        = new SimpleStringProperty();
        this.productDescription = new SimpleStringProperty();
        this.productBasePrice   = new SimpleDoubleProperty();
        this.discountPercent    = new SimpleDoubleProperty();
        this.featuredOn         = new SimpleStringProperty();
        this.imageUrl           = new SimpleStringProperty();
    }

    // ── productSpecialId (PK, not shown in table) ────────────────

    public int getProductSpecialId() { return productSpecialId.get(); }
    public IntegerProperty productSpecialIdProperty() { return productSpecialId; }
    public void setProductSpecialId(int value) { productSpecialId.set(value); }

    // ── productId (FK, not shown in table) ───────────────────────

    public int getProductId() { return productId.get(); }
    public IntegerProperty productIdProperty() { return productId; }
    public void setProductId(int value) { productId.set(value); }

    // ── productName ──────────────────────────────────────────────

    public String getProductName() { return productName.get(); }
    public StringProperty productNameProperty() { return productName; }
    public void setProductName(String value) { productName.set(value != null ? value : ""); }

    // ── productDescription ───────────────────────────────────────

    public String getProductDescription() { return productDescription.get(); }
    public StringProperty productDescriptionProperty() { return productDescription; }
    public void setProductDescription(String value) { productDescription.set(value != null ? value : ""); }

    // ── productBasePrice ─────────────────────────────────────────

    public double getProductBasePrice() { return productBasePrice.get(); }
    public DoubleProperty productBasePriceProperty() { return productBasePrice; }
    public void setProductBasePrice(double value) { productBasePrice.set(value); }

    // ── discountPercent ──────────────────────────────────────────

    public double getDiscountPercent() { return discountPercent.get(); }
    public DoubleProperty discountPercentProperty() { return discountPercent; }
    public void setDiscountPercent(double value) { discountPercent.set(value); }

    // ── featuredOn ───────────────────────────────────────────────

    public String getFeaturedOn() { return featuredOn.get(); }
    public StringProperty featuredOnProperty() { return featuredOn; }
    public void setFeaturedOn(String value) { featuredOn.set(value != null ? value : ""); }

    // ── imageUrl ─────────────────────────────────────────────────

    public String getImageUrl() { return imageUrl.get(); }
    public StringProperty imageUrlProperty() { return imageUrl; }
    public void setImageUrl(String value) { imageUrl.set(value != null ? value : ""); }

    public boolean hasImage() {
        String url = imageUrl.get();
        return url != null && !url.isBlank();
    }

    @Override
    public String toString() {
        return "ProductSpecial{id=" + productSpecialId.get() +
                ", productName='" + productName.get() + '\'' +
                ", featuredOn='" + featuredOn.get() + '\'' +
                ", discountPercent=" + discountPercent.get() + '}';
    }
}
