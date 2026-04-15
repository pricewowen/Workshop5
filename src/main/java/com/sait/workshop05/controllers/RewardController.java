package com.sait.workshop05.controllers;

import java.time.LocalDateTime;

import com.sait.workshop05.api.RewardApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Reward;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.UiPrivacy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    @FXML private TextField txtCustomer;
    @FXML private TextField txtOrder;
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
                txtCustomer.clear();
                txtOrder.clear();
                txtPoints.clear();
                dtpTransactionDate.setValue(null);
                txtTransactionTime.clear();
                return;
            }
            txtCustomer.setText(selected.getCustomerDisplay() != null ? selected.getCustomerDisplay() : "");
            txtOrder.setText(selected.getOrderDisplay() != null ? selected.getOrderDisplay() : "");
            txtPoints.setText(String.valueOf(selected.getRewardPointsEarned()));

            if (selected.getRewardTransactionDate() != null) {
                LocalDateTime dt = selected.getRewardTransactionDate();
                dtpTransactionDate.setValue(dt.toLocalDate());
                txtTransactionTime.setText(String.format("%02d:%02d", dt.getHour(), dt.getMinute()));
            } else {
                dtpTransactionDate.setValue(null);
                txtTransactionTime.clear();
            }
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
            updateRewardsTablePlaceholder();
        });
        SortedList<Reward> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblRewards.comparatorProperty());
        tblRewards.setItems(sorted);
        tblRewards.setPlaceholder(new Label("Loading rewards…"));
    }

    private void updateRewardsTablePlaceholder() {
        if (tblRewards == null || filtered == null) {
            return;
        }
        if (filtered.isEmpty()) {
            tblRewards.setPlaceholder(new Label(
                    master.isEmpty()
                            ? "No reward transactions to display."
                            : "No rewards match the search filter."));
        }
    }

    private void refreshTable() {
        tblRewards.setPlaceholder(new Label("Loading rewards…"));
        try {
            master.clear();
            master.addAll(RewardApi.listAll());
            lblStatus.setText(master.size() + " reward(s) loaded");
            updateRewardsTablePlaceholder();
            LogData.logAction("READ", "Reward");
        } catch (Exception e) {
            LogData.handleException("READ_REWARDS", e);
            tblRewards.setPlaceholder(new Label("Could not load rewards."));
            ErrorHandler.showErrorDialog("API Error", "Could not load rewards.", e);
        }
    }

    @FXML
    private void onRefresh() {
        refreshTable();
    }

    @FXML
    private void onClear() {
        tblRewards.getSelectionModel().clearSelection();
        txtCustomer.clear();
        txtOrder.clear();
        txtPoints.clear();
        dtpTransactionDate.setValue(null);
        txtTransactionTime.clear();
        lblStatus.setText("Cleared");
    }

    private static boolean contains(String field, String q) {
        if (field == null) return false;
        return field.toLowerCase().contains(q);
    }

}