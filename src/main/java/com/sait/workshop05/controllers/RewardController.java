package com.sait.workshop05.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.api.RewardApi;
import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderOption;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.CustomerOption;
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
    @FXML private TableColumn<Reward, String> colRewardId;
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
            List<CustomerOption> customers = ReferenceApi.loadCustomers();
            cboCustomer.setItems(FXCollections.observableArrayList(customers));

            List<OrderOption> orders = new java.util.ArrayList<>();
            for (Order o : OrderApi.listOrders()) {
                orders.add(new OrderOption(o.getOrderId(),
                        o.getOrderId() + " — " + o.getCustomerDisplay()));
            }
            cboOrder.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            LogData.handleException("LOAD_REWARD_COMBOS", e);
            showError("API Error", "Could not load Customer/Order lists.", e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            master.clear();
            master.addAll(RewardApi.listAll());
            lblStatus.setText(master.size() + " reward(s) loaded");
            LogData.logAction("READ", "Reward");
        } catch (Exception e) {
            LogData.handleException("READ_REWARDS", e);
            showError("API Error", "Could not load rewards.", e.getMessage());
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
        showWarning("Not available", "Reward creation is not exposed via the API. Points are created when orders are placed.");
    }

    @FXML
    private void onUpdate() {
        showWarning("Not available", "Reward updates are not exposed via the API.");
    }

    @FXML
    private void onDelete() {
        showWarning("Not available", "Reward deletion is not exposed via the API.");
    }

    private Reward buildFromForm(boolean includeId) {
        return new Reward();
    }

    private ValidationResult validateForm(boolean isUpdate) {
        String points = safe(txtPoints.getText());
        CustomerOption customer = cboCustomer.getValue();
        OrderOption order = cboOrder.getValue();

        if (isUpdate) {
            String id = safe(txtRewardId.getText());
            if (id.isBlank()) return ValidationResult.fail("Reward ID is missing (select a row first).");
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

    private void selectRewardById(String id) {
        for (Reward r : master) {
            if (r.getRewardId() != null && r.getRewardId().equals(id)) {
                tblRewards.getSelectionModel().select(r);
                tblRewards.scrollTo(r);
                return;
            }
        }
    }

    private void selectCustomerById(String customerId) {
        if (cboCustomer.getItems() == null) return;
        for (CustomerOption c : cboCustomer.getItems()) {
            if (c.getCustomerId().equals(customerId)) {
                cboCustomer.getSelectionModel().select(c);
                return;
            }
        }
        cboCustomer.getSelectionModel().clearSelection();
    }

    private void selectOrderById(String orderId) {
        if (cboOrder.getItems() == null) return;
        for (OrderOption o : cboOrder.getItems()) {
            if (o.getOrderId().equals(orderId)) {
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