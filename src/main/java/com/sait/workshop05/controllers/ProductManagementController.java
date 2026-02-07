package com.sait.workshop05.controllers;

import com.sait.workshop05.database.ProductDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductManagementController {

    private static final String LOG_USER = "PRODUCT_VIEW";

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product, Integer> colProductId;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, Double> colBasePrice;
    @FXML private TableColumn<Product, String> colTags;

    // ── Search & status ────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;

    // ── Form fields ────────────────────────────────────────────
    @FXML private TextField txtProductId;
    @FXML private TextField txtProductName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtBasePrice;
    @FXML private ComboBox<String> cboTag;

    // ── Tag assignment ─────────────────────────────────────────
    @FXML private ListView<String> lstAssignedTags;
    @FXML private Button btnAddTag;
    @FXML private Button btnRemoveTag;

    // ── CRUD buttons ───────────────────────────────────────────
    @FXML private Button btnCreate;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private final ProductDAO dao = new ProductDAO();
    private final ObservableList<Product> master = FXCollections.observableArrayList();
    private FilteredList<Product> filtered;

    // Tags currently assigned in the form
    private final ObservableList<String> assignedTags = FXCollections.observableArrayList();

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupTagOptions();
        setupSelectionBinding();
        setupSearchFiltering();
        lstAssignedTags.setItems(assignedTags);
        refreshTable();
    }

    private void setupColumns() {
        colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        colBasePrice.setCellValueFactory(new PropertyValueFactory<>("productBasePrice"));
        colTags.setCellValueFactory(new PropertyValueFactory<>("tagsDisplay"));

        // Format price column to show 2 decimal places
        colBasePrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupTagOptions() {
        try {
            List<String> tags = dao.getTagOptions();
            cboTag.setItems(FXCollections.observableArrayList(tags));
        } catch (SQLException e) {
            LogData.handleException("LOAD_TAG_OPTIONS", e);
            showError("Database Error", "Could not load tag options.", e.getMessage());
        }
    }

    private void setupSelectionBinding() {
        tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) return;

            txtProductId.setText(String.valueOf(selected.getProductId()));
            txtProductName.setText(nz(selected.getProductName()));
            txtDescription.setText(nz(selected.getProductDescription()));
            txtBasePrice.setText(String.format("%.2f", selected.getProductBasePrice()));

            // Load assigned tags for this product
            loadAssignedTags(selected.getProductId());
        });
    }

    private void loadAssignedTags(int productId) {
        try {
            List<String> tags = dao.getTagsForProduct(productId);
            assignedTags.setAll(tags);
        } catch (SQLException e) {
            LogData.handleException("LOAD_PRODUCT_TAGS", e);
            assignedTags.clear();
        }
    }

    private void setupSearchFiltering() {
        filtered = new FilteredList<>(master, p -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();

            filtered.setPredicate(prod -> {
                if (q.isEmpty()) return true;

                return contains(prod.getProductName(), q)
                        || contains(prod.getProductDescription(), q)
                        || contains(prod.getTagsDisplay(), q)
                        || String.valueOf(prod.getProductId()).contains(q)
                        || String.format("%.2f", prod.getProductBasePrice()).contains(q);
            });

            lblStatus.setText(filtered.size() + " product(s) shown");
        });

        SortedList<Product> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblProducts.comparatorProperty());
        tblProducts.setItems(sorted);
    }

    // ────────────────────────────────────────────────────────────
    // Refresh
    // ────────────────────────────────────────────────────────────

    private void refreshTable() {
        try {
            master.clear();
            master.addAll(dao.getAllProducts());
            lblStatus.setText(master.size() + " product(s) loaded");
            LogData.logAction("READ", "Product");
        } catch (SQLException e) {
            LogData.handleException("READ_PRODUCTS", e);
            showError("Database Error", "Could not load products.", e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        setupTagOptions();
        refreshTable();
    }

    // ────────────────────────────────────────────────────────────
    // Tag management (Add / Remove in form)
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onAddTag() {
        String selectedTag = cboTag.getValue();
        if (selectedTag == null || selectedTag.isBlank()) {
            showWarning("Tag", "Select a tag from the dropdown first.");
            return;
        }
        if (assignedTags.contains(selectedTag)) {
            showWarning("Tag", "This tag is already assigned.");
            return;
        }
        assignedTags.add(selectedTag);
    }

    @FXML
    private void onRemoveTag() {
        String selected = lstAssignedTags.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Tag", "Select a tag from the list to remove.");
            return;
        }
        assignedTags.remove(selected);
    }

    // ────────────────────────────────────────────────────────────
    // CRUD operations
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onCreate() {
        ValidationResult vr = validateForm(false);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "Product");
            showWarning("Validation", vr.message);
            return;
        }

        Product p = buildFromForm(false);

        try {
            int newId = dao.insertProduct(p);

            // Save tags
            if (newId > 0) {
                dao.setTagsForProduct(newId, new ArrayList<>(assignedTags));
            }

            LogData.logAction("CREATE", "Product");
            refreshTable();

            if (newId > 0) {
                selectProductById(newId);
                lblStatus.setText("Created product #" + newId);
            } else {
                lblStatus.setText("Created product");
            }

        } catch (SQLException ex) {
            LogData.handleException("CREATE_PRODUCT", ex);
            String friendly = friendlyDbMessage(ex);
            showError("Create Failed", "Could not create product.", friendly);
        }
    }

    @FXML
    private void onUpdate() {
        if (txtProductId.getText() == null || txtProductId.getText().trim().isEmpty()) {
            showWarning("Update", "Select a product row to update.");
            return;
        }

        ValidationResult vr = validateForm(true);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "Product");
            showWarning("Validation", vr.message);
            return;
        }

        Product p = buildFromForm(true);

        try {
            boolean ok = dao.updateProduct(p);

            // Update tags
            dao.setTagsForProduct(p.getProductId(), new ArrayList<>(assignedTags));

            LogData.logAction("UPDATE", "Product");
            refreshTable();
            selectProductById(p.getProductId());
            lblStatus.setText(ok ? "Updated product #" + p.getProductId() : "No update applied");
        } catch (SQLException ex) {
            LogData.handleException("UPDATE_PRODUCT", ex);
            String friendly = friendlyDbMessage(ex);
            showError("Update Failed", "Could not update product.", friendly);
        }
    }

    @FXML
    private void onDelete() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Delete", "Select a product row to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete product #" + selected.getProductId() + " (" + selected.getProductName() + ")?");
        confirm.setContentText("This will also remove all tag associations. This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            dao.deleteProduct(selected.getProductId());
            LogData.logAction("DELETE", "Product");
            refreshTable();
            onClear();
            lblStatus.setText("Deleted product #" + selected.getProductId());
        } catch (SQLException ex) {
            LogData.handleException("DELETE_PRODUCT", ex);
            String friendly = friendlyDbMessage(ex);
            showError("Delete Failed", "Could not delete product.", friendly);
        }
    }

    @FXML
    private void onClear() {
        tblProducts.getSelectionModel().clearSelection();
        txtProductId.clear();
        txtProductName.clear();
        txtDescription.clear();
        txtBasePrice.clear();
        cboTag.setValue(null);
        assignedTags.clear();
        lblStatus.setText("Cleared");
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private Product buildFromForm(boolean includeId) {
        Product p = new Product();

        if (includeId) {
            p.setProductId(Integer.parseInt(txtProductId.getText().trim()));
        }

        p.setProductName(txtProductName.getText().trim());
        p.setProductDescription(txtDescription.getText().trim());
        p.setProductBasePrice(Double.parseDouble(txtBasePrice.getText().trim()));

        return p;
    }

    private ValidationResult validateForm(boolean isUpdate) {
        String name = safe(txtProductName.getText());
        String description = safe(txtDescription.getText());
        String priceStr = safe(txtBasePrice.getText());

        if (isUpdate) {
            String id = safe(txtProductId.getText());
            if (id.isBlank()) return ValidationResult.fail("Product ID is missing (select a row first).");
            try {
                Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                return ValidationResult.fail("Product ID is invalid.");
            }
        }

        // Name validation
        if (name.isBlank()) return ValidationResult.fail("Product name is required.");
        if (name.length() > 120) return ValidationResult.fail("Product name must be 120 characters or less.");

        // Description validation
        if (description.isBlank()) return ValidationResult.fail("Product description is required.");
        if (description.length() > 1000) return ValidationResult.fail("Description must be 1000 characters or less.");

        // Price validation
        if (priceStr.isBlank()) return ValidationResult.fail("Base price is required.");
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ex) {
            return ValidationResult.fail("Base price must be a valid number (e.g., 4.99).");
        }
        if (price < 0) return ValidationResult.fail("Base price cannot be negative.");

        return ValidationResult.ok();
    }

    private String friendlyDbMessage(SQLException ex) {
        String sqlState = ex.getSQLState();
        String msg = (ex.getMessage() == null) ? "" : ex.getMessage();

        if (sqlState != null && sqlState.startsWith("23")) {
            if (msg.toLowerCase().contains("duplicate")) {
                return "A product with that name may already exist.";
            }
            if (msg.toLowerCase().contains("chk_product_price") || msg.toLowerCase().contains("check constraint")) {
                return "Price cannot be negative (database constraint).";
            }
            if (msg.toLowerCase().contains("foreign key constraint")) {
                return "This product is referenced by other records (e.g., orders). Remove those references first.";
            }
            return "This operation violates a database constraint.";
        }

        return msg.isBlank() ? "Unknown database error." : msg;
    }

    private void selectProductById(int id) {
        for (Product p : master) {
            if (p.getProductId() == id) {
                tblProducts.getSelectionModel().select(p);
                tblProducts.scrollTo(p);
                return;
            }
        }
    }

    private void showWarning(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showError(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private static boolean contains(String field, String q) {
        if (field == null) return false;
        return field.toLowerCase().contains(q);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // ── Validation result ──────────────────────────────────────

    private static class ValidationResult {
        final boolean ok;
        final String message;

        private ValidationResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        static ValidationResult ok() {
            return new ValidationResult(true, "");
        }

        static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg);
        }
    }
}
