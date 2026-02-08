package com.sait.workshop05.controllers;

import com.sait.workshop05.database.ProductDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Product;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
import com.sait.workshop05.util.ValidationResult;
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
            ErrorHandler.showErrorDialog("Database Error", "Could not load tag options.", e.getMessage());
        }
    }

    private void setupSelectionBinding() {
        tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) return;

            txtProductId.setText(String.valueOf(selected.getProductId()));
            txtProductName.setText(StringUtil.nz(selected.getProductName()));
            txtDescription.setText(StringUtil.nz(selected.getProductDescription()));
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

                return StringUtil.containsIgnoreCase(prod.getProductName(), q)
                        || StringUtil.containsIgnoreCase(prod.getProductDescription(), q)
                        || StringUtil.containsIgnoreCase(prod.getTagsDisplay(), q)
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
            ErrorHandler.showErrorDialog("Database Error", "Could not load products.", e.getMessage());
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
            ErrorHandler.showWarning("Tag", "Select a tag from the dropdown first.");
            return;
        }
        if (assignedTags.contains(selectedTag)) {
            ErrorHandler.showWarning("Tag", "This tag is already assigned.");
            return;
        }
        assignedTags.add(selectedTag);
    }

    @FXML
    private void onRemoveTag() {
        String selected = lstAssignedTags.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Tag", "Select a tag from the list to remove.");
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
        if (!vr.isOk()) {
            LogData.logAction("VALIDATION_FAILED", "Product");
            ErrorHandler.showWarning("Validation", vr.getMessage());
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
            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Create Failed", "Could not create product.", friendly);
        }
    }

    @FXML
    private void onUpdate() {
        if (txtProductId.getText() == null || txtProductId.getText().trim().isEmpty()) {
            ErrorHandler.showWarning("Update", "Select a product row to update.");
            return;
        }

        ValidationResult vr = validateForm(true);
        if (!vr.isOk()) {
            LogData.logAction("VALIDATION_FAILED", "Product");
            ErrorHandler.showWarning("Validation", vr.getMessage());
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
            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Update Failed", "Could not update product.", friendly);
        }
    }

    @FXML
    private void onDelete() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showWarning("Delete", "Select a product row to delete.");
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
            String friendly = ErrorHandler.friendlyDbMessage(ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete product.", friendly);
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
        String name = StringUtil.safe(txtProductName.getText());
        String description = StringUtil.safe(txtDescription.getText());
        String priceStr = StringUtil.safe(txtBasePrice.getText());

        if (isUpdate) {
            String id = StringUtil.safe(txtProductId.getText());
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

    private void selectProductById(int id) {
        for (Product p : master) {
            if (p.getProductId() == id) {
                tblProducts.getSelectionModel().select(p);
                tblProducts.scrollTo(p);
                return;
            }
        }
    }
}
