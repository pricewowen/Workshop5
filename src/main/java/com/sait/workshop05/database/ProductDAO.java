package com.sait.workshop05.database;

import com.sait.workshop05.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product entity.
 * Handles all database operations related to products.
 */
public class ProductDAO {

    /**
     * Retrieve all products from the database
     */
    public List<Product> getAllProducts() throws SQLException {
        String sql =
                "SELECT productId, productName, productDescription, productBasePrice " +
                "FROM Product " +
                "ORDER BY productName ASC";

        List<Product> products = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("productId"));
                p.setProductName(rs.getString("productName"));
                p.setProductDescription(rs.getString("productDescription"));
                p.setProductBasePrice(rs.getDouble("productBasePrice"));
                products.add(p);
            }
        }

        return products;
    }

    /**
     * Get products by category (using tags)
     */
    public List<Product> getProductsByCategory(String category) throws SQLException {
        String sql =
                "SELECT DISTINCT p.productId, p.productName, p.productDescription, p.productBasePrice " +
                "FROM Product p " +
                "INNER JOIN ProductTag pt ON p.productId = pt.productId " +
                "INNER JOIN Tag t ON pt.tagId = t.tagId " +
                "WHERE t.tagName = ? " +
                "ORDER BY p.productName ASC";

        List<Product> products = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("productId"));
                    p.setProductName(rs.getString("productName"));
                    p.setProductDescription(rs.getString("productDescription"));
                    p.setProductBasePrice(rs.getDouble("productBasePrice"));
                    products.add(p);
                }
            }
        }

        return products;
    }

    /**
     * Get product by ID
     */
    public Product getProductById(int productId) throws SQLException {
        String sql =
                "SELECT productId, productName, productDescription, productBasePrice " +
                "FROM Product " +
                "WHERE productId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("productId"));
                    p.setProductName(rs.getString("productName"));
                    p.setProductDescription(rs.getString("productDescription"));
                    p.setProductBasePrice(rs.getDouble("productBasePrice"));
                    return p;
                }
            }
        }

        return null;
    }

    /**
     * Search products by name or description
     */
    public List<Product> searchProducts(String searchTerm) throws SQLException {
        String sql =
                "SELECT productId, productName, productDescription, productBasePrice " +
                "FROM Product " +
                "WHERE productName LIKE ? OR productDescription LIKE ? " +
                "ORDER BY productName ASC";

        List<Product> products = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("productId"));
                    p.setProductName(rs.getString("productName"));
                    p.setProductDescription(rs.getString("productDescription"));
                    p.setProductBasePrice(rs.getDouble("productBasePrice"));
                    products.add(p);
                }
            }
        }

        return products;
    }

    /**
     * Update product price
     */
    public boolean updateProductPrice(int productId, double price) throws SQLException {
        String sql = "UPDATE Product SET productBasePrice = ? WHERE productId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, productId);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Insert a new product
     */
    public int insertProduct(Product p) throws SQLException {
        String sql =
                "INSERT INTO Product (productName, productDescription, productBasePrice) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getProductName());
            ps.setString(2, p.getProductDescription());
            ps.setDouble(3, p.getProductBasePrice());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * Update an existing product
     */
    public boolean updateProduct(Product p) throws SQLException {
        String sql =
                "UPDATE Product SET productName = ?, productDescription = ?, productBasePrice = ? " +
                "WHERE productId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getProductName());
            ps.setString(2, p.getProductDescription());
            ps.setDouble(3, p.getProductBasePrice());
            ps.setInt(4, p.getProductId());

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Delete a product by ID
     */
    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM Product WHERE productId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Get all available categories (tags)
     */
    public List<String> getAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT tagName FROM Tag ORDER BY tagName ASC";

        List<String> categories = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("tagName"));
            }
        }

        return categories;
    }
}

