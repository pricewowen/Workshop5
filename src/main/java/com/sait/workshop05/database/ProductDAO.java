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
     * Retrieve all products from the database with tags displayed as comma-separated string.
     */
    public List<Product> getAllProducts() throws SQLException {
        String sql =
                "SELECT p.productId, p.productName, p.productDescription, p.productBasePrice, " +
                "       GROUP_CONCAT(t.tagName ORDER BY t.tagName SEPARATOR ', ') AS tagsDisplay " +
                "FROM Product p " +
                "LEFT JOIN ProductTag pt ON p.productId = pt.productId " +
                "LEFT JOIN Tag t ON pt.tagId = t.tagId " +
                "GROUP BY p.productId, p.productName, p.productDescription, p.productBasePrice " +
                "ORDER BY p.productName ASC";

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
                p.setTagsDisplay(rs.getString("tagsDisplay") != null ? rs.getString("tagsDisplay") : "");
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
                    products.add(mapResultSetToProduct(rs));
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
                    return mapResultSetToProduct(rs);
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
                    products.add(mapResultSetToProduct(rs));
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
     * Get all available tag names from the Tag table (for ComboBox / CheckComboBox).
     */
    public List<String> getTagOptions() throws SQLException {
        String sql = "SELECT tagName FROM Tag ORDER BY tagName ASC";

        List<String> tags = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tags.add(rs.getString("tagName"));
            }
        }

        return tags;
    }

    /**
     * Get tags currently assigned to a specific product.
     */
    public List<String> getTagsForProduct(int productId) throws SQLException {
        String sql =
                "SELECT t.tagName " +
                "FROM ProductTag pt " +
                "JOIN Tag t ON pt.tagId = t.tagId " +
                "WHERE pt.productId = ? " +
                "ORDER BY t.tagName ASC";

        List<String> tags = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("tagName"));
                }
            }
        }

        return tags;
    }

    /**
     * Set the tags for a product (delete old, insert new — transactional).
     * @param productId the product ID
     * @param tagNames list of tag names to assign
     */
    public void setTagsForProduct(int productId, List<String> tagNames) throws SQLException {
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // Delete existing tags for this product
            String deleteSql = "DELETE FROM ProductTag WHERE productId = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            // Insert new tags
            if (tagNames != null && !tagNames.isEmpty()) {
                String insertSql =
                        "INSERT INTO ProductTag (productId, tagId) " +
                        "SELECT ?, tagId FROM Tag WHERE tagName = ?";

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (String tagName : tagNames) {
                        ps.setInt(1, productId);
                        ps.setString(2, tagName);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Get all available categories (tags) — alias for getTagOptions()
     */
    public List<String> getAllCategories() throws SQLException {
        return getTagOptions();
    }

    /**
     * Helper method to map ResultSet to Product object.
     * Maps the core product fields (productId, productName, productDescription, productBasePrice).
     * Note: Does not set tagsDisplay — getAllProducts() handles that separately.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("productId"));
        p.setProductName(rs.getString("productName"));
        p.setProductDescription(rs.getString("productDescription"));
        p.setProductBasePrice(rs.getDouble("productBasePrice"));
        return p;
    }
}
