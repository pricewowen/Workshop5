package com.sait.workshop05.controllers;

import java.time.LocalDateTime;
import java.util.List;

import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.api.RewardApi;
import com.sait.workshop05.models.Order;
import com.sait.workshop05.models.OrderOption;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.CustomerOption;
import com.sait.workshop05.models.Reward;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.UiPrivacy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    @FXML private TableView<Reward> tblRewards;
    @FXML private TableColumn<Reward, String> colCustomer;
    @FXML private TableColumn<Reward, String> colOrder;
    @FXML private TableColumn<Reward, Integer> colPoints;
    @FXML private TableColumn<Reward, LocalDateTime> colTransactionDate;

    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;

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
            if (selected == null) {
                txtPoints.clear();
                dtpTransactionDate.setValue(null);
                txtTransactionTime.clear();
                cboCustomer.getSelectionModel().clearSelection();
                cboOrder.getSelectionModel().clearSelection();
                return;
            }
            txtPoints.setText(String.valueOf(selected.getRewardPointsEarned()));

            if (selected.getRewardTransactionDate() != null) {
                LocalDateTime dt = selected.getRewardTransactionDate();
                dtpTransactionDate.setValue(dt.toLocalDate());
                txtTransactionTime.setText(String.format("%02d:%02d", dt.getHour(), dt.getMinute()));
            } else {
                dtpTransactionDate.setValue(null);
                txtTransactionTime.clear();
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
                        || contains(UiPrivacy.compactRef(reward.getRewardId()), q)
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
                String num = o.getOrderNumber();
                if (num == null || num.isBlank()) {
                    num = "Order";
                }
                String cust = o.getCustomerDisplay();
                if (cust == null || cust.isBlank()) {
                    cust = "Customer";
                }
                orders.add(new OrderOption(o.getOrderId(), num + " — " + cust));
            }
            cboOrder.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            LogData.handleException("LOAD_REWARD_COMBOS", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load Customer/Order lists.", e);
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
            ErrorHandler.showErrorDialog("API Error", "Could not load rewards.", e);
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
        txtPoints.clear();
        dtpTransactionDate.setValue(null);
        txtTransactionTime.clear();
        cboCustomer.getSelectionModel().clearSelection();
        cboOrder.getSelectionModel().clearSelection();
        lblStatus.setText("Cleared");
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

    private static boolean contains(String field, String q) {
        if (field == null) return false;
        return field.toLowerCase().contains(q);
    }

}