// Contributor(s): Samantha
// Main: Samantha - Product special row for featured discounts.

package com.sait.workshop05.models;

import javafx.beans.property.*;

/**
 * Featured discount row for catalog specials. Primary key and product id stay in the model for REST calls and
 * are not shown as their own columns.
 */
public class ProductSpecial {

    private final IntegerProperty productSpecialId;  // PK for API calls not displayed
    private final IntegerProperty productId;          // FK for edit pre-selection not displayed
    private final StringProperty  productName;
    private final StringProperty  productDescription;
    private final DoubleProperty  productBasePrice;
    private final DoubleProperty  discountPercent;
    private final StringProperty  featuredOn;
    private final StringProperty  imageUrl;

    /**
     * Initializes all JavaFX properties.
     */
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

    // productSpecialId is kept for update and delete API calls.

    /** @return product special id value. */
    public int getProductSpecialId() { return productSpecialId.get(); }
    /** @return JavaFX property wrapper for product special id. */
    public IntegerProperty productSpecialIdProperty() { return productSpecialId; }
    /** @param value product special id value. */
    public void setProductSpecialId(int value) { productSpecialId.set(value); }

    // productId links this special to its product row.

    /** @return product id value. */
    public int getProductId() { return productId.get(); }
    /** @return JavaFX property wrapper for product id. */
    public IntegerProperty productIdProperty() { return productId; }
    /** @param value product id value. */
    public void setProductId(int value) { productId.set(value); }

    // Product name shown in specials tables.

    /** @return product name value. */
    public String getProductName() { return productName.get(); }
    /** @return JavaFX property wrapper for product name. */
    public StringProperty productNameProperty() { return productName; }
    /** @param value product name value. */
    public void setProductName(String value) { productName.set(value != null ? value : ""); }

    // Product description shown in specials tables.

    /** @return product description value. */
    public String getProductDescription() { return productDescription.get(); }
    /** @return JavaFX property wrapper for product description. */
    public StringProperty productDescriptionProperty() { return productDescription; }
    /** @param value product description value. */
    public void setProductDescription(String value) { productDescription.set(value != null ? value : ""); }

    // Base price used before discount formatting.

    /** @return product base price value. */
    public double getProductBasePrice() { return productBasePrice.get(); }
    /** @return JavaFX property wrapper for product base price. */
    public DoubleProperty productBasePriceProperty() { return productBasePrice; }
    /** @param value product base price value. */
    public void setProductBasePrice(double value) { productBasePrice.set(value); }

    // Discount percent used for special pricing.

    /** @return discount percentage value. */
    public double getDiscountPercent() { return discountPercent.get(); }
    /** @return JavaFX property wrapper for discount percentage. */
    public DoubleProperty discountPercentProperty() { return discountPercent; }
    /** @param value discount percentage value. */
    public void setDiscountPercent(double value) { discountPercent.set(value); }

    // Featured date shown in the specials list.

    /** @return featured date string value. */
    public String getFeaturedOn() { return featuredOn.get(); }
    /** @return JavaFX property wrapper for featured date. */
    public StringProperty featuredOnProperty() { return featuredOn; }
    /** @param value featured date string value. */
    public void setFeaturedOn(String value) { featuredOn.set(value != null ? value : ""); }

    // Optional image URL used by table thumbnail cells.

    /** @return image URL value. */
    public String getImageUrl() { return imageUrl.get(); }
    /** @return JavaFX property wrapper for image URL. */
    public StringProperty imageUrlProperty() { return imageUrl; }
    /** @param value image URL value. */
    public void setImageUrl(String value) { imageUrl.set(value != null ? value : ""); }

    /**
     * @return {@code true} when image URL is present.
     */
    public boolean hasImage() {
        String url = imageUrl.get();
        return url != null && !url.isBlank();
    }

    /**
     * @return debug-friendly summary string for the product special row.
     */
    @Override
    public String toString() {
        return "ProductSpecial{id=" + productSpecialId.get() +
                ", productName='" + productName.get() + '\'' +
                ", featuredOn='" + featuredOn.get() + '\'' +
                ", discountPercent=" + discountPercent.get() + '}';
    }
}
