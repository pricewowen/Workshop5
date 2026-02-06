package com.sait.workshop05.models;

import javafx.beans.property.*;

/**
 * Product model representing a bakery product.
 * Maps to the Product table in the database.
 */
public class Product {
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final StringProperty productDescription;
    private final DoubleProperty productBasePrice;

    /**
     * Default constructor - initializes all properties
     */
    public Product() {
        this.productId = new SimpleIntegerProperty();
        this.productName = new SimpleStringProperty();
        this.productDescription = new SimpleStringProperty();
        this.productBasePrice = new SimpleDoubleProperty();
    }

    /**
     * Full constructor
     */
    public Product(int productId, String productName, String productDescription, double productBasePrice) {
        this();
        setProductId(productId);
        setProductName(productName);
        setProductDescription(productDescription);
        setProductBasePrice(productBasePrice);
    }

    // Getters and Setters
    public int getProductId() {
        return productId.get();
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public String getProductName() {
        return productName.get();
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public String getProductDescription() {
        return productDescription.get();
    }

    public StringProperty productDescriptionProperty() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription.set(productDescription);
    }

    public double getProductBasePrice() {
        return productBasePrice.get();
    }

    public DoubleProperty productBasePriceProperty() {
        return productBasePrice;
    }

    public void setProductBasePrice(double productBasePrice) {
        this.productBasePrice.set(productBasePrice);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId.get() +
                ", productName='" + productName.get() + '\'' +
                ", productBasePrice=" + productBasePrice.get() +
                '}';
    }
}

