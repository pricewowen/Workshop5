package com.sait.workshop05.controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.sait.workshop05.database.CustomerOption;
import com.sait.workshop05.database.OrderOption;
import com.sait.workshop05.database.RewardDAO;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Reward;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for the reward view
 */
public class RewardController {

    private static final String LOG_USER = "REWARD_VIEW";

    @FXML private TableView<Reward> tblRewards;
    @FXML private TableColumn<Reward, Integer> colRewardId;
    @FXML private TableColumn<Reward, String> colCustomer;
    @FXML private TableColumn<Reward, String> colOrder;
    @FXML private TableColumn<Reward, Integer> colPoints;
    @FXML private TableColumn<Reward, LocalDateTime> colTransactionDate;

    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;

    @FXML private TextField txtRewardId;
    @FXML private ComboBox<CustomerOption> cboCustomer;
    @FXML private ComboBox<OrderOption> cboOrder;
    @FXML private TextField txtPoints;
    @FXML private DatePicker dtpTransactionDate;
    @FXML private TextField txtTransactionTime;

    @FXML private Button btnRefresh;

    private final RewardDAO dao = new RewardDAO();
    private final ObservableList<Reward> master = FXCollections.observableArrayList();
    private FilteredList<Reward> filtered;

    @FXML
    void initialize() {
        setColumns();
        setSelectionBinding();
        setSearchFiltering();
        loadCombos();
        refreshTable();
    }

    private void setColumns() {
        colRewardId.setCellValueFactory(new PropertyValueFactory<>("rewardId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerDisplay"));
        colOrder.setCellValueFactory(new PropertyValueFactory<>("orderDisplay"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("rewardPointsEarned"));
        colTransactionDate.setCellValueFactory(new PropertyValueFactory<>("rewardTransactionDate"));

        // Format date column
        colTransactionDate.setCellFactory(column -> new TableCell<Reward, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString().replace("T", " "));
                }
            }
        });
        tblRewards.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setSelectionBinding() {
        tblRewards.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) return;
            txtRewardId.setText(String.valueOf(selected.getRewardId()));
            txtPoints.setText(String.valueOf(selected.getRewardPointsEarned()));

            if (selected.getRewardTransactionDate() != null) {
                LocalDateTime dt = selected.getRewardTransactionDate();
                dtpTransactionDate.setValue(dt.toLocalDate());
                txtTransactionTime.setText(String.format("%02d:%02d", dt.getHour(), dt.getMinute()));
            }
            selectCustomerById(selected.getCustomerId());
            selectOrderById(selected.getOrderId());
        });
    }

    private void setSearchFiltering() {
        filtered = new FilteredList<>(master, e -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();

            filtered.setPredicate(reward -> {
                if (q.isEmpty()) return true;

                return contains(reward.getCustomerDisplay(), q)
                        || contains(reward.getOrderDisplay(), q)
                        || String.valueOf(reward.getRewardId()).contains(q)
                        || String.valueOf(reward.getCustomerId()).contains(q)
                        || String.valueOf(reward.getOrderId()).contains(q)
                        || String.valueOf(reward.getRewardPointsEarned()).contains(q)
                        || (reward.getRewardTransactionDate() != null &&
                        reward.getRewardTransactionDate().toString().toLowerCase().contains(q));
            });
            lblStatus.setText(filtered.size() + " reward(s) shown");
        });
        SortedList<Reward> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblRewards.comparatorProperty());
        tblRewards.setItems(sorted);
    }

    private void loadCombos() {
        try {
            List<CustomerOption> customers = dao.getCustomerOptions();
            cboCustomer.setItems(FXCollections.observableArrayList(customers));

            List<OrderOption> orders = dao.getOrderOptions();
            cboOrder.setItems(FXCollections.observableArrayList(orders));
        } catch (SQLException e) {
            LogData.handleException("LOAD_REWARD_COMBOS", e);
            showError("Database Error", "Could not load Customer/Order lists.", e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            master.clear();
            master.addAll(dao.getAllRewards());
            lblStatus.setText(master.size() + " reward(s) loaded");
            LogData.logAction("READ", "Reward");
        } catch (SQLException e) {
            LogData.handleException("READ_REWARDS", e);
            showError("Database Error", "Could not load rewards.", e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        loadCombos();
        refreshTable();
    }

    @FXML
    private void onClear() {
        tblRewards.getSelectionModel().clearSelection();
        txtRewardId.clear();
        txtPoints.clear();
        dtpTransactionDate.setValue(null);
        txtTransactionTime.clear();
        cboCustomer.getSelectionModel().clearSelection();
        cboOrder.getSelectionModel().clearSelection();
        lblStatus.setText("Cleared");
    }

    @FXML
    private void onCreate() {
        ValidationResult vr = validateForm(false);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "Reward");
            showWarning("Validation", vr.message);
            return;
        }

        Reward r = buildFromForm(false);

        try {
            int newId = dao.insertReward(r);
            LogData.logAction("CREATE", "Reward");
            refreshTable();

            if (newId > 0) {
                selectRewardById(newId);
                lblStatus.setText("Created reward #" + newId);
            } else {
                lblStatus.setText("Created reward");
            }

        } catch (SQLException ex) {
            LogData.handleException("CREATE_REWARD", ex);

            String friendly = friendlyDbMessage(ex);
            showError("Create Failed", "Could not create reward.", friendly);
        }
    }

    @FXML
    private void onUpdate() {
        if (txtRewardId.getText() == null || txtRewardId.getText().trim().isEmpty()) {
            showWarning("Update", "Select a reward row to update.");
            return;
        }

        ValidationResult vr = validateForm(true);
        if (!vr.ok) {
            LogData.logAction("VALIDATION_FAILED", "Reward");
            showWarning("Validation", vr.message);
            return;
        }

        Reward r = buildFromForm(true);

        try {
            boolean ok = dao.updateReward(r);
            LogData.logAction("UPDATE", "Reward");
            refreshTable();
            selectRewardById(r.getRewardId());
            lblStatus.setText(ok ? "Updated reward #" + r.getRewardId() : "No update applied");
        } catch (SQLException ex) {
            LogData.handleException("UPDATE_REWARD", ex);

            String friendly = friendlyDbMessage(ex);
            showError("Update Failed", "Could not update reward.", friendly);
        }
    }

    @FXML
    private void onDelete() {
        Reward selected = tblRewards.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Delete", "Select a reward row to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete reward #" + selected.getRewardId() + "?");
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            dao.deleteReward(selected.getRewardId());
            LogData.logAction("DELETE", "Reward");
            refreshTable();
            onClear();
            lblStatus.setText("Deleted reward #" + selected.getRewardId());
        } catch (SQLException ex) {
            LogData.handleException("DELETE_REWARD", ex);

            String friendly = friendlyDbMessage(ex);
            showError("Delete Failed", "Could not delete reward.", friendly);
        }
    }

    private Reward buildFromForm(boolean includeId) {
        Reward r = new Reward();

        if (includeId) {
            r.setRewardId(Integer.parseInt(txtRewardId.getText().trim()));
        }

        r.setRewardPointsEarned(Integer.parseInt(txtPoints.getText().trim()));

        // Build LocalDateTime from date picker and time field
        if (dtpTransactionDate.getValue() != null) {
            String timeStr = txtTransactionTime.getText().trim();
            if (timeStr.isEmpty()) {
                timeStr = "00:00";
            }
            String[] timeParts = timeStr.split(":");
            int hour = timeParts.length > 0 ? Integer.parseInt(timeParts[0]) : 0;
            int minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;

            LocalDateTime transactionDateTime = dtpTransactionDate.getValue()
                    .atTime(hour, minute);
            r.setRewardTransactionDate(transactionDateTime);
        } else {
            r.setRewardTransactionDate(LocalDateTime.now());
        }

        CustomerOption cust = cboCustomer.getValue();
        OrderOption ord = cboOrder.getValue();

        r.setCustomerId(cust.getCustomerId());
        r.setOrderId(ord.getOrderId());

        return r;
    }

    private ValidationResult validateForm(boolean isUpdate) {
        String points = safe(txtPoints.getText());
        CustomerOption customer = cboCustomer.getValue();
        OrderOption order = cboOrder.getValue();

        if (isUpdate) {
            String id = safe(txtRewardId.getText());
            if (id.isBlank()) return ValidationResult.fail("Reward ID is missing (select a row first).");
            try {
                Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                return ValidationResult.fail("Reward ID is invalid.");
            }
        }

        if (points.isBlank()) return ValidationResult.fail("Points earned is required.");
        try {
            int pointsValue = Integer.parseInt(points);
            if (pointsValue < 0) return ValidationResult.fail("Points must be positive.");
            if (pointsValue > 1000000) return ValidationResult.fail("Points value too large.");
        } catch (NumberFormatException ex) {
            return ValidationResult.fail("Points must be a valid integer.");
        }

        if (customer == null) return ValidationResult.fail("Customer is required.");
        if (order == null) return ValidationResult.fail("Order is required.");

        // Validate time format if provided
        String timeStr = safe(txtTransactionTime.getText());
        if (!timeStr.isEmpty()) {
            if (!timeStr.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                return ValidationResult.fail("Time format must be HH:MM (24-hour format).");
            }
        }

        return ValidationResult.ok();
    }

    private String friendlyDbMessage(SQLException ex) {
        String sqlState = ex.getSQLState();
        String msg = (ex.getMessage() == null) ? "" : ex.getMessage();

        if (sqlState != null && sqlState.startsWith("23")) {
            if (msg.toLowerCase().contains("foreign key constraint")) {
                if (msg.toLowerCase().contains("fk_reward_customer")) {
                    return "The selected customer does not exist.";
                }
                if (msg.toLowerCase().contains("fk_reward_order")) {
                    return "The selected order does not exist.";
                }
                return "Foreign key constraint violation.";
            }
            return "This operation violates a database constraint.";
        }

        return msg.isBlank() ? "Unknown database error." : msg;
    }

    private void selectRewardById(int id) {
        for (Reward r : master) {
            if (r.getRewardId() == id) {
                tblRewards.getSelectionModel().select(r);
                tblRewards.scrollTo(r);
                return;
            }
        }
    }

    private void selectCustomerById(int customerId) {
        if (cboCustomer.getItems() == null) return;
        for (CustomerOption c : cboCustomer.getItems()) {
            if (c.getCustomerId() == customerId) {
                cboCustomer.getSelectionModel().select(c);
                return;
            }
        }
        cboCustomer.getSelectionModel().clearSelection();
    }

    private void selectOrderById(int orderId) {
        if (cboOrder.getItems() == null) return;
        for (OrderOption o : cboOrder.getItems()) {
            if (o.getOrderId() == orderId) {
                cboOrder.getSelectionModel().select(o);
                return;
            }
        }
        cboOrder.getSelectionModel().clearSelection();
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

        static ValidationResult ok() {
            return new ValidationResult(true, "");
        }

        static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg);
        }
    }
}