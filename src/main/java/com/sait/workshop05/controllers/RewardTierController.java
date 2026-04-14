package com.sait.workshop05;

import com.sait.workshop05.api.RewardTierApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.RewardTier;
import com.sait.workshop05.util.ErrorHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class RewardTierController {

    private static final String LOG_USER = "REWARD_TIER_VIEW";

    @FXML private TableView<RewardTier> tblRewardTiers;
    @FXML private TableColumn<RewardTier, Integer> colTierId;
    @FXML private TableColumn<RewardTier, String> colTierName;
    @FXML private TableColumn<RewardTier, Integer> colMinPoints;
    @FXML private TableColumn<RewardTier, Integer> colMaxPoints;
    @FXML private TableColumn<RewardTier, BigDecimal> colDiscountRate;
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private TextField txtTierId;
    @FXML private TextField txtTierName;
    @FXML private TextField txtMinPoints;
    @FXML private TextField txtMaxPoints;
    @FXML private CheckBox chkUnlimited;
    @FXML private TextField txtDiscountRate;
    @FXML private Button btnCreate;
    @FXML private Button btnUpdate;

    private final ObservableList<RewardTier> master = FXCollections.observableArrayList();
    private FilteredList<RewardTier> filtered;

    @FXML
    void initialize() {
        setColumns();
        setSelectionBinding();
        setSearchFiltering();
        setupListeners();
        refreshTable();
        updateButtonState(false);
    }

    /**
     * Create and Update stay visible; one is disabled when it does not apply (mirror Products-style UX).
     */
    private void updateButtonState(boolean hasSelection) {
        if (btnCreate != null) {
            btnCreate.setVisible(true);
            btnCreate.setManaged(true);
            btnCreate.setDisable(hasSelection);
        }
        if (btnUpdate != null) {
            btnUpdate.setVisible(true);
            btnUpdate.setManaged(true);
            btnUpdate.setDisable(!hasSelection);
        }
    }

    private void setColumns() {
        colTierId.setCellValueFactory(new PropertyValueFactory<>("rewardTierId"));
        colTierName.setCellValueFactory(new PropertyValueFactory<>("rewardTierName"));
        colMinPoints.setCellValueFactory(new PropertyValueFactory<>("rewardTierMinPoints"));
        colMaxPoints.setCellValueFactory(new PropertyValueFactory<>("rewardTierMaxPoints"));
        colDiscountRate.setCellValueFactory(new PropertyValueFactory<>("rewardTierDiscountRate"));

        // Format discount rate col
        colDiscountRate.setCellFactory(column -> new TableCell<RewardTier, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f%%", item));
                }
            }
        });

        // Format max points col
        colMaxPoints.setCellFactory(column -> new TableCell<RewardTier, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null || item == 0) {
                    setText("Unlimited");
                } else {
                    setText(String.format("%,d", item));
                }
            }
        });

        // Format min points col
        colMinPoints.setCellFactory(column -> new TableCell<RewardTier, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", item));
                }
            }
        });

        tblRewardTiers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setSelectionBinding() {
        tblRewardTiers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                updateButtonState(false);
                return;
            }
            updateButtonState(true);

            txtTierId.setText(String.valueOf(selected.getRewardTierId()));
            txtTierName.setText(selected.getRewardTierName());
            txtMinPoints.setText(String.valueOf(selected.getRewardTierMinPoints()));

            // Handle max points
            if (selected.getRewardTierMaxPoints() == 0) {
                txtMaxPoints.clear();
                chkUnlimited.setSelected(true);
                txtMaxPoints.setDisable(true);
            } else {
                txtMaxPoints.setText(String.valueOf(selected.getRewardTierMaxPoints()));
                chkUnlimited.setSelected(false);
                txtMaxPoints.setDisable(false);
            }

            // Handle discount rate
            if (selected.getRewardTierDiscountRate() != null) {
                txtDiscountRate.setText(selected.getRewardTierDiscountRate().toString());
            } else {
                txtDiscountRate.clear();
            }
        });
    }

    private void setSearchFiltering() {
        filtered = new FilteredList<>(master, e -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();

            filtered.setPredicate(tier -> {
                if (q.isEmpty()) return true;

                return contains(tier.getRewardTierName(), q)
                        || String.valueOf(tier.getRewardTierId()).contains(q)
                        || String.valueOf(tier.getRewardTierMinPoints()).contains(q)
                        || (tier.getRewardTierMaxPoints() > 0 &&
                        String.valueOf(tier.getRewardTierMaxPoints()).contains(q))
                        || (tier.getRewardTierDiscountRate() != null &&
                        tier.getRewardTierDiscountRate().toString().contains(q));
            });
            lblStatus.setText(filtered.size() + " tier(s) shown");
            updateRewardTierTablePlaceholder();
        });

        SortedList<RewardTier> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblRewardTiers.comparatorProperty());
        tblRewardTiers.setItems(sorted);
        tblRewardTiers.setPlaceholder(new Label("Loading reward tiers…"));
    }

    private void updateRewardTierTablePlaceholder() {
        if (tblRewardTiers == null || filtered == null) {
            return;
        }
        if (filtered.isEmpty()) {
            tblRewardTiers.setPlaceholder(new Label(
                    master.isEmpty()
                            ? "No reward tiers to display."
                            : "No tiers match the search filter."));
        }
    }

    private void setupListeners() {
        chkUnlimited.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtMaxPoints.setDisable(newVal);
            if (newVal) {
                txtMaxPoints.clear();
            }
        });

        // Add input validation
        txtMinPoints.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtMinPoints.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        txtMaxPoints.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!chkUnlimited.isSelected() && !newVal.matches("\\d*")) {
                txtMaxPoints.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        txtDiscountRate.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtDiscountRate.setText(oldVal);
            }
        });
    }

    private void refreshTable() {
        tblRewardTiers.setPlaceholder(new Label("Loading reward tiers…"));
        try {
            master.clear();
            for (RewardTierApi.RewardTierJson j : RewardTierApi.list()) {
                master.add(fromJson(j));
            }
            lblStatus.setText(master.size() + " tier(s) loaded");
            updateRewardTierTablePlaceholder();
            LogData.logAction("READ", "RewardTier");
        } catch (Exception e) {
            LogData.handleException("READ_REWARD_TIERS", e);
            tblRewardTiers.setPlaceholder(new Label("Could not load reward tiers."));
            ErrorHandler.showErrorDialog("API Error", "Could not load reward tiers.", e);
        }
    }

    private RewardTier fromJson(RewardTierApi.RewardTierJson j) {
        RewardTier t = new RewardTier();
        t.setRewardTierId(j.id != null ? j.id : 0);
        t.setRewardTierName(j.name != null ? j.name : "");
        t.setRewardTierMinPoints(j.minPoints);
        t.setRewardTierMaxPoints(j.maxPoints == null ? 0 : j.maxPoints);
        t.setRewardTierDiscountRate(j.discountRatePercent);
        return t;
    }

    @FXML
    private void onRefresh() {
        refreshTable();
    }

    @FXML
    private void onClear() {
        tblRewardTiers.getSelectionModel().clearSelection();
        txtTierId.clear();
        txtTierName.clear();
        txtMinPoints.clear();
        txtMaxPoints.clear();
        txtDiscountRate.clear();
        chkUnlimited.setSelected(false);
        txtMaxPoints.setDisable(false);
        lblStatus.setText("Cleared");
        updateButtonState(false);
    }

    @FXML
    private void onCreate() {
        ValidationResult vr = validateForm(false);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "RewardTier");
            showWarning("Validation", vr.message);
            return;
        }

        RewardTier tier = buildFromForm(false);

        try {
            Integer maxPoints = chkUnlimited.isSelected() ? null :
                    (txtMaxPoints.getText().isEmpty() ? null : Integer.parseInt(txtMaxPoints.getText()));

            RewardTierApi.RewardTierJson created = RewardTierApi.create(
                    tier.getRewardTierName(),
                    tier.getRewardTierMinPoints(),
                    maxPoints,
                    tier.getRewardTierDiscountRate()
            );
            LogData.logAction("CREATE", "RewardTier");
            refreshTable();

            if (created.id != null && created.id > 0) {
                selectTierById(created.id);
                lblStatus.setText("Created tier #" + created.id);
            }

        } catch (Exception ex) {
            LogData.handleException("CREATE_REWARD_TIER", ex);
            ErrorHandler.showErrorDialog("Create Failed", "Could not create reward tier.", ex);
        }
    }

    @FXML
    private void onUpdate() {
        if (txtTierId.getText() == null || txtTierId.getText().trim().isEmpty()) {
            showWarning("Update", "Select a tier row to update.");
            return;
        }

        ValidationResult vr = validateForm(true);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "RewardTier");
            showWarning("Validation", vr.message);
            return;
        }

        RewardTier tier = buildFromForm(true);

        try {
            Integer maxPoints = chkUnlimited.isSelected() ? null :
                    (txtMaxPoints.getText().isEmpty() ? null : Integer.parseInt(txtMaxPoints.getText()));

            RewardTierApi.update(
                    tier.getRewardTierId(),
                    tier.getRewardTierName(),
                    tier.getRewardTierMinPoints(),
                    maxPoints,
                    tier.getRewardTierDiscountRate()
            );
            LogData.logAction("UPDATE", "RewardTier");
            refreshTable();
            selectTierById(tier.getRewardTierId());
            lblStatus.setText("Updated tier #" + tier.getRewardTierId());
        } catch (Exception ex) {
            LogData.handleException("UPDATE_REWARD_TIER", ex);
            ErrorHandler.showErrorDialog("Update Failed", "Could not update reward tier.", ex);
        }
    }

    @FXML
    private void onDelete() {
        RewardTier selected = tblRewardTiers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Delete", "Select a tier row to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        confirm.getDialogPane().getStyleClass().add("modal-dialog-pane");
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete tier: " + selected.getRewardTierName());
        confirm.setContentText("This cannot be undone. Customers assigned to this tier will be affected.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            RewardTierApi.delete(selected.getRewardTierId());
            LogData.logAction("DELETE", "RewardTier");
            refreshTable();
            onClear();
            lblStatus.setText("Deleted tier #" + selected.getRewardTierId());
        } catch (Exception ex) {
            LogData.handleException("DELETE_REWARD_TIER", ex);
            ErrorHandler.showErrorDialog("Delete Failed", "Could not delete reward tier.", ex);
        }
    }

    private RewardTier buildFromForm(boolean includeId) {
        RewardTier tier = new RewardTier();

        if (includeId) {
            tier.setRewardTierId(Integer.parseInt(txtTierId.getText().trim()));
        }

        tier.setRewardTierName(txtTierName.getText().trim());
        tier.setRewardTierMinPoints(Integer.parseInt(txtMinPoints.getText().trim()));

        // Handle max points
        if (chkUnlimited.isSelected() || txtMaxPoints.getText().trim().isEmpty()) {
            tier.setRewardTierMaxPoints(null);
        } else {
            tier.setRewardTierMaxPoints(Integer.parseInt(txtMaxPoints.getText().trim()));
        }

        // Handle discount rate
        String discountStr = txtDiscountRate.getText().trim();
        if (!discountStr.isEmpty()) {
            tier.setRewardTierDiscountRate(new BigDecimal(discountStr));
        } else {
            tier.setRewardTierDiscountRate(null);
        }

        return tier;
    }

    private ValidationResult validateForm(boolean isUpdate) {
        String name = safe(txtTierName.getText());
        String minPoints = safe(txtMinPoints.getText());

        if (isUpdate) {
            String id = safe(txtTierId.getText());
            if (id.isBlank()) return ValidationResult.fail("Tier ID is missing (select a row first).");
            try {
                Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                return ValidationResult.fail("Tier ID is invalid.");
            }
        }

        if (name.isBlank()) return ValidationResult.fail("Tier name is required.");
        if (name.length() > 30) return ValidationResult.fail("Tier name must be 30 characters or less.");

        if (minPoints.isBlank()) return ValidationResult.fail("Minimum points is required.");
        try {
            int minPointsValue = Integer.parseInt(minPoints);
            if (minPointsValue < 0) return ValidationResult.fail("Minimum points must be 0 or greater.");
        } catch (NumberFormatException ex) {
            return ValidationResult.fail("Minimum points must be a valid integer.");
        }

        // Validate max points if provided
        if (!chkUnlimited.isSelected() && !txtMaxPoints.getText().trim().isEmpty()) {
            try {
                int maxPointsValue = Integer.parseInt(txtMaxPoints.getText().trim());
                int minPointsValue = Integer.parseInt(minPoints);
                if (maxPointsValue <= minPointsValue) {
                    return ValidationResult.fail("Maximum points must be greater than minimum points.");
                }
            } catch (NumberFormatException ex) {
                return ValidationResult.fail("Maximum points must be a valid integer.");
            }
        }

        // Validate discount rate if provided
        String discountStr = safe(txtDiscountRate.getText());
        if (!discountStr.isEmpty()) {
            try {
                BigDecimal discount = new BigDecimal(discountStr);
                if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(new BigDecimal("100")) > 0) {
                    return ValidationResult.fail("Discount rate must be between 0 and 100.");
                }
            } catch (NumberFormatException ex) {
                return ValidationResult.fail("Discount rate must be a valid number.");
            }
        }

        return ValidationResult.ok();
    }

    private void selectTierById(int id) {
        for (RewardTier t : master) {
            if (t.getRewardTierId() == id) {
                tblRewardTiers.getSelectionModel().select(t);
                tblRewardTiers.scrollTo(t);
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

    private static boolean contains(String field, String q) {
        if (field == null) return false;
        return field.toLowerCase().contains(q);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static class ValidationResult {
        final boolean ok;
        final String message;

        private ValidationResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }
        static ValidationResult ok() {return new ValidationResult(true, "");}
        static ValidationResult fail(String msg) {return new ValidationResult(false, msg);}
    }
}