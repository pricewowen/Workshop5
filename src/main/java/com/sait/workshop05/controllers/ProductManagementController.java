package com.sait.workshop05.controllers;

import com.sait.workshop05.api.CatalogApi;
import com.sait.workshop05.api.ImageUploadApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Product;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product, Integer> colProductId;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, Double> colBasePrice;
    @FXML private TableColumn<Product, String> colTags;
    @FXML private TableColumn<Product, String> colImage;
    @FXML private TableColumn<Product, Void> colActions;

    // ── Toolbar ────────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnNewProduct;

    private final ObservableList<Product> master = FXCollections.observableArrayList();
    private FilteredList<Product> filtered;

    // Cached tag name→id map for dialogs
    private Map<String, Integer> tagNameToId = Map.of();
    private List<String> allTagNames = new ArrayList<>();

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupActionsColumn();
        setupSearchFiltering();
        loadTagOptions();
        refreshTable();
    }

    private void setupColumns() {
        colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        colBasePrice.setCellValueFactory(new PropertyValueFactory<>("productBasePrice"));
        colTags.setCellValueFactory(new PropertyValueFactory<>("tagsDisplay"));

        colBasePrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

        colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        colImage.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty) { setText(""); return; }
                setText(url != null && !url.isBlank() ? "Yes" : "No");
            }
        });

        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-icon-edit");
                deleteBtn.getStyleClass().add("btn-icon-delete");
                editBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    showProductDialog(p);
                });
                deleteBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
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

    private void loadTagOptions() {
        try {
            List<CatalogApi.TagResponse> tags = CatalogApi.fetchTags();
            allTagNames = tags.stream().map(t -> t.name).collect(Collectors.toList());
            tagNameToId = tags.stream().collect(Collectors.toMap(t -> t.name, t -> t.id));
        } catch (Exception e) {
            LogData.handleException("LOAD_TAG_OPTIONS", e);
        }
    }

    // ────────────────────────────────────────────────────────────
    // Refresh
    // ────────────────────────────────────────────────────────────

    private void refreshTable() {
        try {
            List<CatalogApi.ProductResponse> rows = CatalogApi.fetchProducts(null, null);
            Map<Integer, String> idToName = CatalogApi.fetchTags().stream()
                    .collect(Collectors.toMap(t -> t.id, t -> t.name));
            master.clear();
            for (CatalogApi.ProductResponse p : rows) {
                master.add(toProduct(p, idToName));
            }
            lblStatus.setText(master.size() + " product(s) loaded");
            LogData.logAction("READ", "Product");
        } catch (Exception e) {
            LogData.handleException("READ_PRODUCTS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load products.", e.getMessage());
        }
    }

    private Product toProduct(CatalogApi.ProductResponse p, Map<Integer, String> idToName) {
        Product pr = new Product();
        if (p.id != null) pr.setProductId(p.id);
        pr.setProductName(p.name != null ? p.name : "");
        pr.setProductDescription(p.description != null ? p.description : "");
        pr.setProductBasePrice(p.basePrice != null ? p.basePrice.doubleValue() : 0);
        if (p.tagIds != null && !p.tagIds.isEmpty()) {
            pr.setTagsDisplay(p.tagIds.stream().map(idToName::get).filter(Objects::nonNull).collect(Collectors.joining(", ")));
        } else {
            pr.setTagsDisplay("");
        }
        pr.setImageUrl(p.imageUrl);
        return pr;
    }

    @FXML
    private void onRefresh() {
        loadTagOptions();
        refreshTable();
    }

    // ────────────────────────────────────────────────────────────
    // Create / Edit Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onNewProduct() {
        showProductDialog(null);
    }

    private void showProductDialog(Product existing) {
        boolean isNew = existing == null;

        // Load existing tags for edit
        ObservableList<String> assignedTags = FXCollections.observableArrayList();
        if (!isNew) {
            try {
                CatalogApi.ProductResponse p = CatalogApi.fetchProduct(existing.getProductId());
                Map<Integer, String> idToName = CatalogApi.fetchTags().stream()
                        .collect(Collectors.toMap(t -> t.id, t -> t.name));
                if (p.tagIds != null) {
                    p.tagIds.stream().map(idToName::get).filter(Objects::nonNull).forEach(assignedTags::add);
                }
            } catch (Exception e) {
                LogData.handleException("LOAD_PRODUCT_TAGS", e);
            }
        }

        // Form fields
        TextField tfName = new TextField(isNew ? "" : StringUtil.nz(existing.getProductName()));
        TextArea taDescription = new TextArea(isNew ? "" : StringUtil.nz(existing.getProductDescription()));
        taDescription.setPrefRowCount(3);
        taDescription.setWrapText(true);
        TextField tfPrice = new TextField(isNew ? "" : String.format("%.2f", existing.getProductBasePrice()));

        // Tag management
        ComboBox<String> cbTag = new ComboBox<>(FXCollections.observableArrayList(allTagNames));
        cbTag.setMaxWidth(Double.MAX_VALUE);
        ListView<String> lstTags = new ListView<>(assignedTags);
        lstTags.setPrefHeight(100);

        Button btnAdd = new Button("Add Tag");
        btnAdd.getStyleClass().add("btn-muted");
        btnAdd.setOnAction(e -> {
            String sel = cbTag.getValue();
            if (sel != null && !sel.isBlank() && !assignedTags.contains(sel)) assignedTags.add(sel);
        });

        Button btnRemove = new Button("Remove Selected");
        btnRemove.getStyleClass().add("btn-muted");
        btnRemove.setOnAction(e -> {
            String sel = lstTags.getSelectionModel().getSelectedItem();
            if (sel != null) assignedTags.remove(sel);
        });

        HBox tagControls = new HBox(6, cbTag, btnAdd, btnRemove);
        HBox.setHgrow(cbTag, Priority.ALWAYS);

        // Image picker
        File[] selectedImageFile = {null};
        boolean existingHasImage = !isNew && existing.hasImage();
        Label lblImage = new Label(isNew ? "No file selected"
                : existingHasImage ? "Current image set (browse to replace)" : "No image set (browse to add)");
        Button btnBrowse = new Button("Browse Image...");
        btnBrowse.getStyleClass().add("btn-muted");
        HBox imageRow = new HBox(8, btnBrowse, lblImage);
        imageRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #B85C4C; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        // Build layout
        GridPane grid = buildFormGrid();
        int row = 0;
        addRow(grid, row++, "Product Name *", tfName);
        addRow(grid, row++, "Description *", taDescription);
        addRow(grid, row++, "Base Price *", tfPrice);

        Label tagsLabel = new Label("Tags");
        tagsLabel.getStyleClass().add("form-label");
        VBox tagsBox = new VBox(6, tagControls, lstTags);
        grid.add(tagsLabel, 0, row);
        grid.add(tagsBox, 1, row);
        row++;

        Label imgLabel = new Label("Image");
        imgLabel.getStyleClass().add("form-label");
        grid.add(imgLabel, 0, row);
        grid.add(imageRow, 1, row);

        VBox content = new VBox(12, grid, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "New Product" : "Edit Product");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modal-dialog-pane");
        dialog.setResizable(true);

        // Wire image browse after dialog is shown (needs window reference)
        dialog.getDialogPane().sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            btnBrowse.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select Product Image");
                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Image files (JPG, PNG)", "*.jpg", "*.jpeg", "*.png"));
                File file = chooser.showOpenDialog(scene.getWindow());
                if (file != null) {
                    selectedImageFile[0] = file;
                    lblImage.setText(file.getName());
                }
            });
        });

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String err = validateDialog(tfName, taDescription, tfPrice);
            if (err != null) {
                lblError.setText(err);
                lblError.setVisible(true);
                lblError.setManaged(true);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            String name = tfName.getText().trim();
            String desc = taDescription.getText().trim();
            double price = Double.parseDouble(tfPrice.getText().trim());
            List<Integer> tagIds = assignedTags.stream()
                    .map(tagNameToId::get).filter(Objects::nonNull).collect(Collectors.toList());

            try {
                int productId;
                if (isNew) {
                    CatalogApi.ProductResponse created = CatalogApi.createProduct(name, desc, price, tagIds, "");
                    LogData.logAction("CREATE", "Product");
                    productId = created.id != null ? created.id : -1;
                    refreshTable();
                    if (productId > 0) { selectProductById(productId); lblStatus.setText("Created product #" + productId); }
                } else {
                    CatalogApi.updateProduct(existing.getProductId(), name, desc, price, tagIds, "");
                    LogData.logAction("UPDATE", "Product");
                    productId = existing.getProductId();
                    refreshTable();
                    selectProductById(productId);
                    lblStatus.setText("Updated product #" + productId);
                }

                if (selectedImageFile[0] != null && productId > 0) {
                    try {
                        ImageUploadApi.uploadProductImage(productId, selectedImageFile[0]);
                        LogData.logAction("UPLOAD_IMAGE", "Product #" + productId);
                    } catch (Exception uploadEx) {
                        LogData.handleException("UPLOAD_PRODUCT_IMAGE", uploadEx);
                        ErrorHandler.showErrorDialog("Upload Failed", "Product saved but image upload failed.", uploadEx.getMessage());
                    }
                }
            } catch (Exception ex) {
                LogData.handleException(isNew ? "CREATE_PRODUCT" : "UPDATE_PRODUCT", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save product.", ex.getMessage());
            }
        });
    }

    private String validateDialog(TextField tfName, TextArea taDesc, TextField tfPrice) {
        String name = StringUtil.safe(tfName.getText());
        String desc = StringUtil.safe(taDesc.getText());
        String price = StringUtil.safe(tfPrice.getText());
        if (name.isBlank()) return "Product name is required.";
        if (name.length() > 120) return "Product name must be 120 characters or less.";
        if (desc.isBlank()) return "Description is required.";
        if (desc.length() > 1000) return "Description must be 1000 characters or less.";
        if (price.isBlank()) return "Base price is required.";
        try {
            double p = Double.parseDouble(price);
            if (p < 0) return "Base price cannot be negative.";
        } catch (NumberFormatException e) {
            return "Base price must be a valid number (e.g., 4.99).";
        }
        return null;
    }

    // ────────────────────────────────────────────────────────────
    // Delete
    // ────────────────────────────────────────────────────────────

    private void handleDeleteProduct(Product p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete product #" + p.getProductId() + " (" + p.getProductName() + ")?");
        confirm.setContentText("This will also remove all tag associations. This cannot be undone.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            CatalogApi.deleteProduct(p.getProductId());
            LogData.logAction("DELETE", "Product");
            Sentry.withScope(scope -> {
                scope.setTag("action", "DELETE");
                scope.setTag("entity", "product");
                Sentry.captureMessage("Deleted product #" + p.getProductId() + " (" + p.getProductName() + ")", SentryLevel.WARNING);
            });
            refreshTable();
            lblStatus.setText("Deleted product #" + p.getProductId());
        } catch (Exception ex) {
            LogData.handleException("DELETE_PRODUCT", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete product.", ex.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private GridPane buildFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(120);
        c0.setHgrow(Priority.NEVER);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setMaxWidth(Double.MAX_VALUE);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, Control control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        control.setMaxWidth(Double.MAX_VALUE);
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
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
