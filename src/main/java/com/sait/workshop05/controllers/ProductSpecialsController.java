// Contributor(s): Samantha
// Main: Samantha - Product specials featured dates and discounts.

package com.sait.workshop05.controllers;

import com.sait.workshop05.api.ApiClient;
import com.sait.workshop05.api.CatalogApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.ProductSpecial;
import com.sait.workshop05.util.DialogHelper;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.StringUtil;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Product specials CRUD for featured discounts and dates. The product special id stays in the model for API
 * calls and does not appear as its own column.
 */
public class ProductSpecialsController {

    // Product specials table controls.
    @FXML private TableView<ProductSpecial>            tblProductSpecials;
    @FXML private TableColumn<ProductSpecial, String>  colProductName;
    @FXML private TableColumn<ProductSpecial, String>  colDescription;
    @FXML private TableColumn<ProductSpecial, Double>  colBasePrice;
    @FXML private TableColumn<ProductSpecial, Double>  colDiscount;
    @FXML private TableColumn<ProductSpecial, String>  colFeaturedOn;
    @FXML private TableColumn<ProductSpecial, String>  colImage;
    @FXML private TableColumn<ProductSpecial, Void>    colActions;

    // Toolbar controls.
    @FXML private TextField txtSearch;
    @FXML private Label     lblStatus;
    @FXML private Button    btnRefresh;
    @FXML private Button    btnNewSpecial;

    private final ObservableList<ProductSpecial> master = FXCollections.observableArrayList();
    private FilteredList<ProductSpecial> filtered;

    /** Full product list loaded once for the create and edit ComboBox. */
    private List<CatalogApi.ProductResponse> allProducts = List.of();
    /** productId to ProductResponse lookup for auto-populating read-only fields in the dialog. */
    private Map<Integer, CatalogApi.ProductResponse> productById = Map.of();

    // Initialization.

    @FXML
    void initialize() {
        setupColumns();
        setupActionsColumn();
        setupSearchFiltering();
        loadAllAsync();
    }

    private void setupColumns() {
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        colFeaturedOn.setCellValueFactory(new PropertyValueFactory<>("featuredOn"));

        // Base price is formatted as currency for quick scan in staff tables.
        colBasePrice.setCellValueFactory(new PropertyValueFactory<>("productBasePrice"));
        colBasePrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

        // Discount is formatted as a percentage and shows an em dash when no discount is set.
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercent"));
        colDiscount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double pct, boolean empty) {
                super.updateItem(pct, empty);
                if (empty || pct == null) {
                    setText("");
                } else if (pct == 0.0) {
                    setText("—");
                } else {
                    setText(String.format("%.1f%%", pct));
                }
            }
        });

        // Image thumbnails use async loading so large lists do not block table rendering.
        colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        final int thumb = 40;
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(thumb);
                imageView.setFitHeight(thumb);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
                imageView.setImage(null);
                if (empty) { setGraphic(null); return; }
                if (url == null || url.isBlank()) {
                    Label none = new Label("No image");
                    none.setStyle("-fx-text-fill: #8A8178; -fx-font-size: 11px;");
                    setGraphic(none);
                    return;
                }
                String resolved = resolveImageUrl(url);
                Label pending = new Label("…");
                pending.setStyle("-fx-text-fill: #8A8178; -fx-font-size: 12px;");
                setGraphic(pending);

                Image img = new Image(resolved, thumb, thumb, true, true, true);
                Runnable apply = () -> {
                    if (!url.equals(getItem())) return;
                    if (img.isError()) {
                        Label err = new Label("Can't load");
                        err.setStyle("-fx-text-fill: #B85C4C; -fx-font-size: 10px; -fx-wrap-text: true;");
                        err.setMaxWidth(thumb + 24);
                        setGraphic(err);
                    } else {
                        imageView.setImage(img);
                        setGraphic(imageView);
                    }
                };
                if (img.getProgress() >= 1.0) {
                    Platform.runLater(apply);
                } else {
                    img.progressProperty().addListener((obs, o, n) -> {
                        if (n != null && n.doubleValue() >= 1.0) Platform.runLater(apply);
                    });
                    img.errorProperty().addListener((obs, o, isErr) -> {
                        if (Boolean.TRUE.equals(isErr)) Platform.runLater(apply);
                    });
                }
            }
        });

        tblProductSpecials.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblProductSpecials.setFixedCellSize(52);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-icon-edit");
                deleteBtn.getStyleClass().add("btn-icon-delete");
                editBtn.setOnAction(e -> {
                    ProductSpecial ps = getTableView().getItems().get(getIndex());
                    showSpecialDialog(ps);
                });
                deleteBtn.setOnAction(e -> {
                    ProductSpecial ps = getTableView().getItems().get(getIndex());
                    handleDeleteSpecial(ps);
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
            filtered.setPredicate(ps -> {
                if (q.isEmpty()) return true;
                return StringUtil.containsIgnoreCase(ps.getProductName(), q)
                        || StringUtil.containsIgnoreCase(ps.getProductDescription(), q)
                        || StringUtil.containsIgnoreCase(ps.getFeaturedOn(), q)
                        || String.format("%.2f", ps.getProductBasePrice()).contains(q);
            });
            lblStatus.setText(filtered.size() + " product special(s) shown");
            updatePlaceholder();
        });

        SortedList<ProductSpecial> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblProductSpecials.comparatorProperty());
        tblProductSpecials.setItems(sorted);
        tblProductSpecials.setPlaceholder(new Label("Loading product specials…"));
    }

    private void updatePlaceholder() {
        if (tblProductSpecials == null || filtered == null) return;
        if (filtered.isEmpty()) {
            tblProductSpecials.setPlaceholder(new Label(
                    master.isEmpty()
                            ? "No product specials to display."
                            : "No product specials match the search filter."));
        }
    }

    // Async data loading.

    /**
     * Fetches product specials and the product list in parallel on a background thread.
     * The product list is stored for use in the create / edit dialog ComboBox.
     */
    private void loadAllAsync() {
        lblStatus.setText("Loading...");
        tblProductSpecials.setPlaceholder(new Label("Loading product specials…"));

        Task<SpecialLoadData> task = new Task<>() {
            @Override
            protected SpecialLoadData call() throws Exception {
                List<CatalogApi.ProductSpecialResponse> specials = CatalogApi.fetchProductSpecials();
                List<CatalogApi.ProductResponse>        products = CatalogApi.fetchProducts(null, null);
                return new SpecialLoadData(specials, products);
            }
        };

        task.setOnSucceeded(e -> applyData(task.getValue()));
        task.setOnFailed(e -> {
            Throwable t = task.getException();
            LogData.handleException("LOAD_PRODUCT_SPECIALS", new RuntimeException(t));
            tblProductSpecials.setPlaceholder(new Label("Could not load product specials."));
            ErrorHandler.showErrorDialog("API Error", "Could not load product specials.", t);
        });

        new Thread(task).start();
    }

    private void applyData(SpecialLoadData d) {
        allProducts = d.products;
        productById = d.products.stream()
                .filter(p -> p.id != null)
                .collect(Collectors.toMap(p -> p.id, p -> p));

        master.clear();
        for (CatalogApi.ProductSpecialResponse r : d.specials) {
            master.add(toProductSpecial(r));
        }
        lblStatus.setText(master.size() + " product special(s) loaded");
        updatePlaceholder();
        LogData.logAction("READ", "ProductSpecial");
    }

    /**
     * Re-fetches only the specials list after a mutation, reusing the already-loaded
     * product cache so the product ComboBox stays populated without an extra round trip.
     */
    private void refreshSpecialsOnlyAsync(Runnable afterRefresh) {
        tblProductSpecials.setPlaceholder(new Label("Loading product specials…"));

        Task<List<CatalogApi.ProductSpecialResponse>> task = new Task<>() {
            @Override
            protected List<CatalogApi.ProductSpecialResponse> call() throws Exception {
                return CatalogApi.fetchProductSpecials();
            }
        };

        task.setOnSucceeded(e -> {
            master.clear();
            for (CatalogApi.ProductSpecialResponse r : task.getValue()) {
                master.add(toProductSpecial(r));
            }
            lblStatus.setText(master.size() + " product special(s) loaded");
            updatePlaceholder();
            LogData.logAction("READ", "ProductSpecial");
            if (afterRefresh != null) afterRefresh.run();
        });

        task.setOnFailed(e -> {
            Throwable t = task.getException();
            LogData.handleException("READ_PRODUCT_SPECIALS", new RuntimeException(t));
            tblProductSpecials.setPlaceholder(new Label("Could not load product specials."));
            ErrorHandler.showErrorDialog("API Error", "Could not load product specials.", t);
        });

        new Thread(task).start();
    }

    private ProductSpecial toProductSpecial(CatalogApi.ProductSpecialResponse r) {
        ProductSpecial ps = new ProductSpecial();
        if (r.productSpecialId != null) ps.setProductSpecialId(r.productSpecialId);
        if (r.productId        != null) ps.setProductId(r.productId);
        ps.setProductName(r.productName != null ? r.productName : "");
        ps.setProductDescription(r.productDescription != null ? r.productDescription : "");
        ps.setProductBasePrice(r.productBasePrice != null ? r.productBasePrice.doubleValue() : 0.0);
        ps.setDiscountPercent(r.discountPercent != null ? r.discountPercent.doubleValue() : 0.0);
        ps.setFeaturedOn(r.featuredOn != null ? r.featuredOn : "");
        ps.setImageUrl(r.productImageUrl);
        return ps;
    }

    @FXML
    private void onRefresh() { loadAllAsync(); }

    // Create and edit dialog.

    @FXML
    private void onNewSpecial() { showSpecialDialog(null); }

    /**
     * Opens create or edit dialog. Editable fields are product ComboBox featured date and discount percent from
     * zero to one hundred. Description and base price fill from the selected product.
     */
    private void showSpecialDialog(ProductSpecial existing) {
        boolean isNew = existing == null;

        // Editable fields.
        // Product ComboBox  -  display names, keyed by ProductResponse
        ComboBox<CatalogApi.ProductResponse> cbProduct = new ComboBox<>(
                FXCollections.observableArrayList(allProducts));
        cbProduct.setMaxWidth(Double.MAX_VALUE);
        cbProduct.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CatalogApi.ProductResponse p) {
                return p == null ? "" : (p.name != null ? p.name : "(unnamed)");
            }
            @Override public CatalogApi.ProductResponse fromString(String s) { return null; }
        });

        DatePicker dpFeaturedOn = new DatePicker();
        dpFeaturedOn.setMaxWidth(Double.MAX_VALUE);
        if (!isNew && !existing.getFeaturedOn().isBlank()) {
            try {
                dpFeaturedOn.setValue(LocalDate.parse(existing.getFeaturedOn()));
            } catch (Exception ignored) {}
        }

        TextField tfDiscount = new TextField(
                isNew ? "0.00" : String.format("%.2f", existing.getDiscountPercent()));
        tfDiscount.setPromptText("0.00 – 50.00");

        // Read-only product detail fields.
        Label lblDescValue  = new Label();
        lblDescValue.setWrapText(true);
        lblDescValue.setStyle("-fx-text-fill: #5A534E;");

        Label lblPriceValue = new Label();
        lblPriceValue.setStyle("-fx-text-fill: #5A534E;");

        // Auto-populate read-only fields when product selection changes
        cbProduct.valueProperty().addListener((obs, oldP, newP) -> {
            if (newP == null) {
                lblDescValue.setText("");
                lblPriceValue.setText("");
            } else {
                lblDescValue.setText(newP.description != null ? newP.description : "");
                lblPriceValue.setText(newP.basePrice != null
                        ? String.format("$%.2f", newP.basePrice.doubleValue()) : "");
            }
        });

        // Pre-select the existing product when editing
        if (!isNew && existing.getProductId() > 0) {
            CatalogApi.ProductResponse pre = productById.get(existing.getProductId());
            if (pre != null) cbProduct.setValue(pre);
        }

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #B85C4C; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        // Discount cap hint shown below the discount field
        Label lblDiscountHint = new Label("Max 50%");
        lblDiscountHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A8178;");

        // Layout.
        GridPane grid = buildFormGrid();
        int row = 0;
        addRow(grid, row++, "Product *",     cbProduct);
        addRow(grid, row++, "Featured On *", dpFeaturedOn);
        addRow(grid, row++, "Discount %",    tfDiscount);
        grid.add(lblDiscountHint, 1, row++);

        // Separator between editable and read-only sections
        Label sep = new Label("Product Details  (read-only)");
        sep.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A8178; -fx-padding: 8 0 2 0;");
        grid.add(sep, 0, row++, 2, 1);

        addRow(grid, row++, "Description", lblDescValue);
        addRow(grid, row++, "Base Price",  lblPriceValue);

        VBox content = new VBox(12, grid, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        // Dialog setup.
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "New Product Special" : "Edit Product Special");
        dialog.getDialogPane().setContent(content);
        DialogHelper.configureResponsive(dialog, 520);
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(
                        "/com/sait/workshop05/styles.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modal-dialog-pane");
        dialog.setResizable(true);

        ButtonType saveType = new ButtonType(
                isNew ? "Create Special" : "Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String err = validateDialog(cbProduct, dpFeaturedOn, tfDiscount);
            if (err != null) {
                lblError.setText(err);
                lblError.setVisible(true);
                lblError.setManaged(true);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            int     selectedProductId = cbProduct.getValue().id;
            String  featuredOn        = dpFeaturedOn.getValue().toString();
            double  discount          = Double.parseDouble(tfDiscount.getText().trim());

            try {
                if (isNew) {
                    CatalogApi.ProductSpecialResponse created =
                            CatalogApi.createProductSpecial(selectedProductId, featuredOn, discount);
                    LogData.logAction("CREATE", "ProductSpecial");
                    int newId = created.productSpecialId != null ? created.productSpecialId : -1;
                    refreshSpecialsOnlyAsync(() -> {
                        lblStatus.setText("Created product special #" + newId);
                        ErrorHandler.showInfo("Special created",
                                "The product special was saved successfully.");
                    });
                } else {
                    CatalogApi.updateProductSpecial(existing.getProductSpecialId(),
                            selectedProductId, featuredOn, discount);
                    LogData.logAction("UPDATE", "ProductSpecial");
                    refreshSpecialsOnlyAsync(() ->
                            lblStatus.setText("Updated product special #"
                                    + existing.getProductSpecialId()));
                }
            } catch (Exception ex) {
                LogData.handleException(isNew ? "CREATE_PRODUCT_SPECIAL" : "UPDATE_PRODUCT_SPECIAL", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save product special.", ex);
            }
        });
    }

    private String validateDialog(ComboBox<CatalogApi.ProductResponse> cbProduct,
                                   DatePicker dpFeaturedOn, TextField tfDiscount) {
        if (cbProduct.getValue() == null)
            return "Please select a product.";
        if (dpFeaturedOn.getValue() == null)
            return "Featured On date is required.";
        String discStr = StringUtil.safe(tfDiscount.getText());
        if (!discStr.isBlank()) {
            try {
                double d = Double.parseDouble(discStr);
                if (d < 0 || d > 50) return "Discount must be between 0 and 50.";
            } catch (NumberFormatException e) {
                return "Discount must be a valid number (e.g. 10.00).";
            }
        }
        return null;
    }

    // Delete.

    private void handleDeleteSpecial(ProductSpecial ps) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete special for \"" + ps.getProductName() + "\""
                + " (featured on " + ps.getFeaturedOn() + ")?");
        confirm.setContentText("This cannot be undone.");
        confirm.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(
                        "/com/sait/workshop05/styles.css")).toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            CatalogApi.deleteProductSpecial(ps.getProductSpecialId());
            LogData.logAction("DELETE", "ProductSpecial");
            Sentry.withScope(scope -> {
                scope.setTag("action", "DELETE");
                scope.setTag("entity", "product-special");
                Sentry.captureMessage("Deleted product special #" + ps.getProductSpecialId()
                        + " (" + ps.getProductName() + ")", SentryLevel.WARNING);
            });
            int deletedId = ps.getProductSpecialId();
            refreshSpecialsOnlyAsync(() ->
                    lblStatus.setText("Deleted product special #" + deletedId));
        } catch (Exception ex) {
            LogData.handleException("DELETE_PRODUCT_SPECIAL", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete product special.", ex);
        }
    }

    // Helpers.

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

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        if (control instanceof Control c) c.setMaxWidth(Double.MAX_VALUE);
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
    }

    /** Resolves absolute image URLs; prepends the API base URL for site-relative paths. */
    private static String resolveImageUrl(String url) {
        if (url == null || url.isBlank()) return "";
        String u = url.trim();
        if (u.startsWith("http://") || u.startsWith("https://")) return u;
        String base = ApiClient.getInstance().getBaseUrl();
        return u.startsWith("/") ? base + u : base + "/" + u;
    }

    // Inner types.

    private static final class SpecialLoadData {
        final List<CatalogApi.ProductSpecialResponse> specials;
        final List<CatalogApi.ProductResponse>        products;

        SpecialLoadData(List<CatalogApi.ProductSpecialResponse> specials,
                        List<CatalogApi.ProductResponse>        products) {
            this.specials  = specials;
            this.products  = products;
        }
    }
}
