package com.sait.workshop05.controllers;

import com.sait.workshop05.api.CustomerApi;
import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.api.RewardTierApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.*;
import com.sait.workshop05.util.AddressInputHelper;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.util.OrderStatus;
import com.sait.workshop05.util.StringUtil;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerManagementController {

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, String> colFirstName;
    @FXML private TableColumn<Customer, String> colLastName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colRewardTier;
    @FXML private TableColumn<Customer, Integer> colRewardBalance;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private TableColumn<Customer, Void> colActions;

    // ── Toolbar ────────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnNewCustomer;
    @FXML private Button btnOrderHistory;
    @FXML private Button btnAdjustPoints;

    private final ObservableList<Customer> master = FXCollections.observableArrayList();
    private FilteredList<Customer> filtered;
    private List<RewardTierApi.RewardTierJson> tierData = new ArrayList<>();

    // Cached options for dialogs
    private List<RewardTierOption> tierOptions = new ArrayList<>();
    private List<AddressOption> addressOptions = new ArrayList<>();
    private boolean isLoading = false;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────

    @FXML
    void initialize() {
        setupColumns();
        setupActionsColumn();
        setupSearchFiltering();
        setupSelectionButtons();
        if (tblCustomers != null) {
            tblCustomers.setPlaceholder(new Label("Loading customers…"));
        }
        loadAllAsync();
    }

    private void setupColumns() {
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRewardTier.setCellValueFactory(new PropertyValueFactory<>("rewardTierDisplay"));
        colRewardBalance.setCellValueFactory(new PropertyValueFactory<>("rewardBalance"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("addressDisplay"));
        tblCustomers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final HBox box = new HBox(editBtn);

            {
                editBtn.getStyleClass().add("btn-icon-edit");
                editBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    showCustomerDialog(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupSelectionButtons() {
        if (btnOrderHistory != null) btnOrderHistory.setDisable(true);
        if (btnAdjustPoints != null) btnAdjustPoints.setDisable(true);
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean has = selected != null;
            if (btnOrderHistory != null) btnOrderHistory.setDisable(!has);
            if (btnAdjustPoints != null) btnAdjustPoints.setDisable(!has);
        });
    }

    private void setupSearchFiltering() {
        filtered = new FilteredList<>(master, c -> true);

        txtSearch.textProperty().addListener((obs, oldText, newText) -> {
            String q = (newText == null) ? "" : newText.trim().toLowerCase();
            filtered.setPredicate(cust -> {
                if (q.isEmpty()) return true;
                return StringUtil.containsIgnoreCase(cust.getFirstName(), q)
                        || StringUtil.containsIgnoreCase(cust.getLastName(), q)
                        || StringUtil.containsIgnoreCase(cust.getEmail(), q)
                        || StringUtil.containsIgnoreCase(cust.getPhone(), q)
                        || StringUtil.containsIgnoreCase(cust.getRewardTierDisplay(), q)
                        || StringUtil.containsIgnoreCase(cust.getAddressDisplay(), q)
                        || String.valueOf(cust.getRewardBalance()).contains(q);
            });
            lblStatus.setText(filtered.size() + " customer(s) shown");
            updateCustomerTablePlaceholder();
        });

        SortedList<Customer> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblCustomers.comparatorProperty());
        tblCustomers.setItems(sorted);
    }

    private void updateCustomerTablePlaceholder() {
        if (tblCustomers == null || filtered == null) {
            return;
        }
        if (isLoading) {
            tblCustomers.setPlaceholder(new Label("Loading customers…"));
            return;
        }
        if (filtered.isEmpty()) {
            tblCustomers.setPlaceholder(new Label(
                    master.isEmpty()
                            ? "No customers to display."
                            : "No customers match the current filter."));
        }
    }

    // ────────────────────────────────────────────────────────────
    // Async data loading
    // ────────────────────────────────────────────────────────────

    /**
     * Fetches all reference data and customer rows in a single background task.
     * Replaces the old loadCombos() + refreshTable() pair that ran 5 blocking
     * HTTP calls on the FX thread (2 of which were duplicates).
     */
    private void loadAllAsync() {
        isLoading = true;
        lblStatus.setText("Loading...");
        updateCustomerTablePlaceholder();
        Task<CombinedData> task = new Task<>() {
            @Override
            protected CombinedData call() throws Exception {
                List<RewardTierApi.RewardTierJson> tiers = RewardTierApi.list();
                List<AddressOption> addrs = ReferenceApi.loadAddresses();
                List<CustomerApi.CustomerRow> customers = CustomerApi.list();
                return new CombinedData(tiers, addrs, customers);
            }
        };
        task.setOnSucceeded(e -> applyData(task.getValue()));
        task.setOnFailed(e -> {
            Throwable t = task.getException();
            LogData.handleException("LOAD_CUSTOMERS", new RuntimeException(t));
            isLoading = false;
            updateCustomerTablePlaceholder();
            ErrorHandler.showErrorDialog("API Error", "Could not load customers.", t);
        });
        new Thread(task).start();
    }

    private void applyData(CombinedData d) {
        isLoading = false;
        tierData = new ArrayList<>(d.tiers);
        tierOptions = d.tiers.stream()
                .filter(t -> t.id != null)
                .map(t -> new RewardTierOption(t.id, t.name != null ? t.name : ""))
                .collect(Collectors.toList());
        addressOptions = d.addrs;

        Map<Integer, String> tierMap = new HashMap<>();
        for (RewardTierApi.RewardTierJson t : d.tiers) {
            if (t.id != null) tierMap.put(t.id, t.name != null ? t.name : "");
        }
        Map<Integer, String> addrMap = new HashMap<>();
        for (AddressOption a : d.addrs) {
            addrMap.put(a.getAddressId(), a.getLine1() + ", " + a.getCity());
        }
        master.clear();
        for (CustomerApi.CustomerRow row : d.customers) {
            master.add(fromCustomerRow(row, tierMap, addrMap));
        }
        lblStatus.setText(master.size() + " customer(s) loaded");
        updateCustomerTablePlaceholder();
        LogData.logAction("READ", "Customer");
    }

    /**
     * After a mutation (edit/adjust points), re-fetches customers and reward tiers
     * so tier names and point thresholds match the server (e.g. after tiers were
     * edited on the Reward Tiers screen). Addresses stay cached from the last full load.
     */
    private void refreshCustomersOnlyAsync(Runnable afterRefresh) {
        ReferenceApi.invalidateCustomersCache();
        Task<CustomerRefreshData> task = new Task<>() {
            @Override
            protected CustomerRefreshData call() throws Exception {
                List<RewardTierApi.RewardTierJson> tiers = RewardTierApi.list();
                List<CustomerApi.CustomerRow> customers = CustomerApi.list();
                return new CustomerRefreshData(tiers, customers);
            }
        };
        task.setOnSucceeded(e -> {
            CustomerRefreshData data = task.getValue();
            tierData = new ArrayList<>(data.tiers);
            tierOptions = data.tiers.stream()
                    .filter(t -> t.id != null)
                    .map(t -> new RewardTierOption(t.id, t.name != null ? t.name : ""))
                    .collect(Collectors.toList());

            Map<Integer, String> tierMap = new HashMap<>();
            for (RewardTierApi.RewardTierJson t : data.tiers) {
                if (t.id != null) tierMap.put(t.id, t.name != null ? t.name : "");
            }
            Map<Integer, String> addrMap = new HashMap<>();
            for (AddressOption a : addressOptions) {
                addrMap.put(a.getAddressId(), a.getLine1() + ", " + a.getCity());
            }
            master.clear();
            for (CustomerApi.CustomerRow row : data.customers) {
                master.add(fromCustomerRow(row, tierMap, addrMap));
            }
            lblStatus.setText(master.size() + " customer(s) loaded");
            LogData.logAction("READ", "Customer");
            if (afterRefresh != null) afterRefresh.run();
        });
        task.setOnFailed(e -> {
            Throwable t = task.getException();
            LogData.handleException("READ_CUSTOMERS", new RuntimeException(t));
            ErrorHandler.showErrorDialog("API Error", "Could not load customers.", t);
        });
        new Thread(task).start();
    }

    private Customer fromCustomerRow(CustomerApi.CustomerRow row, Map<Integer, String> tierMap, Map<Integer, String> addrMap) {
        Customer c = new Customer();
        c.setCustomerId(row.id != null ? row.id : "");
        c.setUserId(row.userId != null ? row.userId : "");
        c.setUserDisplay(row.username != null && !row.username.isBlank() ? row.username : "");
        c.setFirstName(row.firstName != null ? row.firstName : "");
        c.setMiddleInitial(row.middleInitial != null ? row.middleInitial : "");
        c.setLastName(row.lastName != null ? row.lastName : "");
        c.setEmail(row.email != null ? row.email : "");
        c.setPhone(row.phone != null ? row.phone : "");
        c.setRewardBalance(row.rewardBalance);
        if (row.rewardTierId != null) {
            c.setRewardTierId(row.rewardTierId);
            c.setRewardTierDisplay(tierMap.getOrDefault(row.rewardTierId, ""));
        }
        if (row.addressId != null) {
            c.setAddressId(row.addressId);
            c.setAddressDisplay(addrMap.getOrDefault(row.addressId, ""));
        }
        return c;
    }

    @FXML
    private void onRefresh() {
        loadAllAsync();
    }

    // ────────────────────────────────────────────────────────────
    // Create / Edit Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onNewCustomer() {
        showCustomerDialog(null);
    }

    private void showCustomerDialog(Customer existing) {
        boolean isNew = existing == null;

        TextField tfFirstName = new TextField(isNew ? "" : StringUtil.nz(existing.getFirstName()));
        TextField tfMiddleInitial = new TextField(isNew ? "" : StringUtil.nz(existing.getMiddleInitial()));
        tfMiddleInitial.setMaxWidth(70);
        TextField tfLastName = new TextField(isNew ? "" : StringUtil.nz(existing.getLastName()));
        TextField tfPhone = new TextField(isNew ? "" : StringUtil.nz(existing.getPhone()));
        TextField tfBusinessPhone = new TextField(isNew ? "" : StringUtil.nz(existing.getBusinessPhone()));
        TextField tfEmail = new TextField(isNew ? "" : StringUtil.nz(existing.getEmail()));
        TextField tfRewardBalance = new TextField(isNew ? "0" : String.valueOf(existing.getRewardBalance()));
        tfRewardBalance.setEditable(false);
        tfRewardBalance.setFocusTraversable(false);
        tfRewardBalance.setStyle("-fx-opacity: 1; -fx-control-inner-background: #f0ebe4;");

        TextField tfTierDisplay = new TextField(isNew ? tierDisplayForBalance(0) : StringUtil.nz(existing.getRewardTierDisplay()));
        tfTierDisplay.setEditable(false);
        tfTierDisplay.setFocusTraversable(false);
        tfTierDisplay.setStyle("-fx-opacity: 1; -fx-control-inner-background: #f0ebe4;");
        tfTierDisplay.setPromptText("—");

        final ComboBox<UserOption> cbUserNew;
        final TextField tfLinkedUser;

        if (isNew) {
            List<UserOption> assignablePool;
            try {
                assignablePool = ReferenceApi.loadUnlinkedCustomerRoleUsersForNewCustomer();
            } catch (Exception ex) {
                LogData.handleException("LOAD_ASSIGNABLE_CUSTOMER_USERS", ex);
                assignablePool = new ArrayList<>();
            }
            final List<UserOption> assignableForFilter = new ArrayList<>(assignablePool);
            cbUserNew = new ComboBox<>(FXCollections.observableArrayList(assignablePool));
            cbUserNew.setMaxWidth(Double.MAX_VALUE);
            cbUserNew.setEditable(true);
            cbUserNew.setConverter(new javafx.util.StringConverter<UserOption>() {
                @Override
                public String toString(UserOption option) {
                    return option == null ? "" : option.getUsername();
                }
                @Override
                public UserOption fromString(String s) {
                    if (s == null || s.isBlank()) return null;
                    String norm = s.trim();
                    return assignableForFilter.stream()
                            .filter(u -> u.getUsername().equalsIgnoreCase(norm))
                            .findFirst()
                            .orElse(null);
                }
            });
            cbUserNew.setOnKeyReleased(evt -> {
                javafx.scene.input.KeyCode code = evt.getCode();
                if (code == javafx.scene.input.KeyCode.ENTER || code == javafx.scene.input.KeyCode.ESCAPE
                        || code == javafx.scene.input.KeyCode.UP || code == javafx.scene.input.KeyCode.DOWN) {
                    return;
                }
                String typed = cbUserNew.getEditor().getText();
                cbUserNew.getSelectionModel().clearSelection();
                String lc = typed == null ? "" : typed.toLowerCase();
                List<UserOption> match = lc.isEmpty()
                        ? new ArrayList<>(assignableForFilter)
                        : assignableForFilter.stream()
                        .filter(u -> u.getUsername().toLowerCase().contains(lc))
                        .collect(Collectors.toList());
                cbUserNew.setItems(FXCollections.observableArrayList(match));
                if (typed != null) {
                    cbUserNew.getEditor().setText(typed);
                    cbUserNew.getEditor().positionCaret(typed.length());
                }
                if (!match.isEmpty()) {
                    cbUserNew.show();
                }
            });
            tfLinkedUser = null;
        } else {
            cbUserNew = null;
            tfLinkedUser = new TextField(StringUtil.nz(existing.getUserDisplay()));
            tfLinkedUser.setEditable(false);
            tfLinkedUser.setFocusTraversable(false);
            tfLinkedUser.setStyle("-fx-opacity: 1; -fx-control-inner-background: #f0ebe4;");
            if (tfLinkedUser.getText().isBlank()) {
                tfLinkedUser.setText("(no linked login)");
            }
        }

        ComboBox<AddressOption> cbAddress = new ComboBox<>();
        AddressInputHelper.configureEditableAddressCombo(cbAddress);
        AddressInputHelper.setAddressItems(cbAddress, addressOptions);
        if (!isNew && existing.getAddressId() > 0) {
            AddressInputHelper.selectAddressById(cbAddress, existing.getAddressId());
        }
        cbAddress.setMaxWidth(Double.MAX_VALUE);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #B85C4C; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        GridPane grid = buildFormGrid();
        int row = 0;
        addRow(grid, row++, "First Name *", tfFirstName);
        addRow(grid, row++, "Middle Initial", tfMiddleInitial);
        addRow(grid, row++, "Last Name *", tfLastName);
        addRow(grid, row++, "Email *", tfEmail);
        addRow(grid, row++, "Phone *", tfPhone);
        addRow(grid, row++, "Business Phone", tfBusinessPhone);
        if (isNew) {
            addRow(grid, row++, "User Account *", cbUserNew);
        } else {
            addRow(grid, row++, "Linked user (read-only)", tfLinkedUser);
        }
        addRow(grid, row++, "Reward tier (from points)", tfTierDisplay);
        addRow(grid, row++, "Reward points (read-only)", tfRewardBalance);
        addRow(grid, row, "Address *", cbAddress);

        Label lblTierHint = new Label("Tier updates automatically when points cross a threshold (including after Save).");
        lblTierHint.setStyle("-fx-text-fill: #8A8178; -fx-font-size: 11px; -fx-wrap-text: true;");
        lblTierHint.setMaxWidth(480);
        Label lblPointsHint = new Label(
                isNew
                        ? "New customers start at 0 points. After saving, use Adjust Points on the list to add or remove points."
                        : "Use Adjust Points on the customer list to change points; they cannot be edited here.");
        lblPointsHint.setStyle("-fx-text-fill: #8A8178; -fx-font-size: 11px; -fx-wrap-text: true;");
        lblPointsHint.setMaxWidth(480);

        VBox content = new VBox(12, grid, lblTierHint, lblPointsHint, lblError);
        content.setPadding(new Insets(20, 24, 8, 24));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "New Customer" : "Edit Customer");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modal-dialog-pane");

        ButtonType saveType = new ButtonType(
                isNew ? "Create Customer" : "Save Changes",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (isNew && cbUserNew != null && cbUserNew.isEditable() && cbUserNew.getConverter() != null) {
                String typed = cbUserNew.getEditor().getText();
                UserOption match = cbUserNew.getConverter().fromString(typed);
                cbUserNew.setValue(match);
            }
            if (StringUtil.safe(tfFirstName.getText()).isBlank()) { showErr(lblError, "First name is required."); event.consume(); return; }
            if (StringUtil.safe(tfLastName.getText()).isBlank()) { showErr(lblError, "Last name is required."); event.consume(); return; }
            if (StringUtil.safe(tfPhone.getText()).isBlank()) { showErr(lblError, "Phone is required."); event.consume(); return; }
            if (StringUtil.safe(tfEmail.getText()).isBlank()) { showErr(lblError, "Email is required."); event.consume(); return; }
            if (isNew && (cbUserNew == null || !(cbUserNew.getValue() instanceof UserOption))) {
                showErr(lblError, "Pick a user account from the list (create a customer login first if the list is empty).");
                event.consume();
                return;
            }
            String addrTyped = AddressInputHelper.getTypedText(cbAddress);
            if (cbAddress.getValue() == null && addrTyped.isBlank()) {
                showErr(lblError, "Address is required.");
                event.consume();
                return;
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
            try {
                AddressOption addr = resolveAddress(cbAddress);
                if (isNew) {
                    int addressId = addr != null ? addr.getAddressId() : 0;
                    if (addressId <= 0) {
                        ErrorHandler.showWarning("Create Failed", "Address is required.");
                        return;
                    }
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("userId", cbUserNew.getValue().getUserId());
                    body.put("addressId", addressId);
                    body.put("firstName", tfFirstName.getText().trim());
                    body.put("middleInitial", tfMiddleInitial.getText().trim().isEmpty() ? null : tfMiddleInitial.getText().trim());
                    body.put("lastName", tfLastName.getText().trim());
                    body.put("phone", tfPhone.getText().trim());
                    body.put("businessPhone", tfBusinessPhone.getText().trim().isEmpty() ? null : tfBusinessPhone.getText().trim());
                    body.put("email", tfEmail.getText().trim());
                    body.put("rewardBalance", 0);
                    CustomerApi.CustomerRow created = CustomerApi.create(body);
                    LogData.logAction("CREATE", "Customer");
                    String newId = created.id != null ? created.id : "";
                    refreshCustomersOnlyAsync(() -> {
                        if (!newId.isBlank()) {
                            selectCustomerById(newId);
                        }
                        lblStatus.setText("Customer created.");
                    });
                } else {
                    int currentBalance = existing.getRewardBalance();
                    Integer newTierId = findTierForBalance(currentBalance);
                    if (newTierId == null) {
                        newTierId = existing.getRewardTierId();
                    }
                    Map<String, Object> body = CustomerApi.patchBodyForProfile(
                            tfFirstName.getText().trim(),
                            tfMiddleInitial.getText().trim().isEmpty() ? null : tfMiddleInitial.getText().trim(),
                            tfLastName.getText().trim(),
                            tfPhone.getText().trim(),
                            tfBusinessPhone.getText().trim().isEmpty() ? null : tfBusinessPhone.getText().trim(),
                            tfEmail.getText().trim(),
                            addr != null ? addr.getAddressId() : existing.getAddressId(),
                            newTierId
                    );
                    CustomerApi.patch(existing.getCustomerId(), body);
                    LogData.logAction("UPDATE", "Customer");
                    String cid = existing.getCustomerId();
                    String displayName = (tfFirstName.getText().trim() + " " + tfLastName.getText().trim()).trim();
                    refreshCustomersOnlyAsync(() -> {
                        selectCustomerById(cid);
                        lblStatus.setText("Updated customer " + displayName);
                    });
                }
            } catch (Exception ex) {
                LogData.handleException(isNew ? "CREATE_CUSTOMER" : "UPDATE_CUSTOMER", ex);
                ErrorHandler.showErrorDialog(isNew ? "Create Failed" : "Update Failed",
                        "Could not save customer.", ex);
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    // Order History Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onViewOrderHistory() {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) { ErrorHandler.showWarning("Order History", "Select a customer row first."); return; }

        try {
            List<Order> orders = OrderApi.listOrdersForCustomer(selected.getCustomerId());
            LogData.logAction("VIEW_ORDER_HISTORY", "Customer " + selected.getCustomerId());
            showOrderHistoryDialog(selected, orders);
        } catch (Exception e) {
            LogData.handleException("VIEW_ORDER_HISTORY", e);
            ErrorHandler.showErrorDialog("API Error", "Could not load order history.", e);
        }
    }

    private void showOrderHistoryDialog(Customer customer, List<Order> orders) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order History — " + customer.getFirstName() + " " + customer.getLastName());
        dialog.setHeaderText(orders.size() + " order(s) found");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        TableView<Order> tblOrders = new TableView<>();
        tblOrders.setPrefHeight(260);

        TableColumn<Order, String> colOrderNum = new TableColumn<>("Order #");
        colOrderNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colOrderNum.setPrefWidth(110);

        TableColumn<Order, LocalDateTime> colDate = new TableColumn<>("Placed");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderPlacedDateTime"));
        colDate.setPrefWidth(140);
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DT_FMT));
            }
        });

        TableColumn<Order, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("orderMethod"));
        colMethod.setPrefWidth(90);

        TableColumn<Order, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("orderTotal"));
        colTotal.setPrefWidth(90);
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });

        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        colStatus.setPrefWidth(100);

        tblOrders.getColumns().addAll(colOrderNum, colDate, colMethod, colTotal, colStatus);
        tblOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label lblItems = new Label("Order Items (select an order above)");
        lblItems.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        TableView<OrderItem> tblItems = new TableView<>();
        tblItems.setPrefHeight(180);

        TableColumn<OrderItem, String> colProduct = new TableColumn<>("Product");
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productDisplay"));
        colProduct.setPrefWidth(200);
        TableColumn<OrderItem, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("orderItemQuantity"));
        colQty.setPrefWidth(60);
        TableColumn<OrderItem, Double> colLineTotal = new TableColumn<>("Line Total");
        colLineTotal.setCellValueFactory(new PropertyValueFactory<>("orderItemLineTotal"));
        colLineTotal.setPrefWidth(100);
        colLineTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });
        tblItems.getColumns().addAll(colProduct, colQty, colLineTotal);
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ComboBox<String> cboFilter = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.FILTER_STATUSES));
        cboFilter.setValue("All");
        FilteredList<Order> filteredOrders = new FilteredList<>(FXCollections.observableArrayList(orders), o -> true);
        cboFilter.valueProperty().addListener((obs, old, val) ->
                filteredOrders.setPredicate(o -> val == null || "All".equals(val) || val.equalsIgnoreCase(o.getOrderStatus())));
        SortedList<Order> sortedOrders = new SortedList<>(filteredOrders);
        sortedOrders.comparatorProperty().bind(tblOrders.comparatorProperty());
        tblOrders.setItems(sortedOrders);

        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { tblItems.getItems().clear(); return; }
            try {
                List<OrderItem> items = OrderApi.getOrderItems(sel.getOrderId());
                tblItems.setItems(FXCollections.observableArrayList(items));
                lblItems.setText("Order " + sel.getOrderNumber() + " — " + items.size() + " item(s)");
            } catch (Exception e) {
                LogData.handleException("LOAD_ORDER_ITEMS", e);
                tblItems.getItems().clear();
            }
        });

        HBox filterBar = new HBox(8, new Label("Filter by status:"), cboFilter);
        VBox content = new VBox(10, filterBar, tblOrders, lblItems, tblItems);
        content.setPadding(new Insets(10));
        VBox.setVgrow(tblOrders, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(720, 560);
        dialog.showAndWait();
    }

    // ────────────────────────────────────────────────────────────
    // Adjust Points Dialog
    // ────────────────────────────────────────────────────────────

    @FXML
    private void onAdjustPoints() {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) { ErrorHandler.showWarning("Adjust Points", "Select a customer row first."); return; }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Adjust Loyalty Points");
        dialog.setHeaderText("Customer: " + selected.getFirstName() + " " + selected.getLastName()
                + "\nCurrent Balance: " + selected.getRewardBalance() + " points");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/sait/workshop05/styles.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtAdjust = new TextField("0");
        txtAdjust.setPromptText("Positive to add, negative to subtract");
        grid.add(new Label("Adjustment:"), 0, 0);
        grid.add(txtAdjust, 1, 0);

        Label lblPreview = new Label("New Balance: " + selected.getRewardBalance());
        grid.add(lblPreview, 1, 1);

        txtAdjust.textProperty().addListener((obs, old, val) -> {
            try {
                int adj = Integer.parseInt(val.trim());
                int newBal = selected.getRewardBalance() + adj;
                lblPreview.setText("New Balance: " + newBal + (newBal < 0 ? " (invalid!)" : ""));
            } catch (NumberFormatException e) {
                lblPreview.setText("New Balance: (invalid input)");
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try { return Integer.parseInt(txtAdjust.getText().trim()); }
                catch (NumberFormatException e) { return null; }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(adjustment -> {
            int newBalance = selected.getRewardBalance() + adjustment;
            if (newBalance < 0) { ErrorHandler.showWarning("Invalid", "Reward balance cannot go below 0."); return; }
            try {
                Map<String, Object> patchBody = new LinkedHashMap<>(CustomerApi.patchRewardBalance(newBalance));
                Integer newTierId = findTierForBalance(newBalance);
                if (newTierId != null) {
                    patchBody.put("rewardTierId", newTierId);
                }
                CustomerApi.patch(selected.getCustomerId(), patchBody);
                LogData.logAction("ADJUST_POINTS", "Customer " + selected.getCustomerId()
                        + " adjusted by " + adjustment + " -> " + newBalance);
                String cid = selected.getCustomerId();
                String firstName = selected.getFirstName();
                int finalAdjustment = adjustment;
                int finalBalance = newBalance;
                refreshCustomersOnlyAsync(() -> {
                    selectCustomerById(cid);
                    lblStatus.setText("Adjusted points for " + firstName + " by " + finalAdjustment + " -> " + finalBalance);
                });
            } catch (Exception e) {
                LogData.handleException("ADJUST_POINTS", e);
                ErrorHandler.showErrorDialog("Error", "Could not update reward balance.", e);
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private GridPane buildFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(130);
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

    private void showErr(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private AddressOption resolveAddress(ComboBox<AddressOption> cbAddress) throws Exception {
        AddressOption selected = cbAddress.getValue();
        if (selected != null) return selected;
        String typed = AddressInputHelper.getTypedText(cbAddress);
        if (typed.isBlank()) return null;
        return ReferenceApi.createAddressFromTyped(typed);
    }

    private void selectCustomerById(String id) {
        for (Customer c : master) {
            if (id.equals(c.getCustomerId())) {
                tblCustomers.getSelectionModel().select(c);
                tblCustomers.scrollTo(c);
                return;
            }
        }
    }

    private Integer findTierForBalance(int balance) {
        for (RewardTierApi.RewardTierJson tier : tierData) {
            if (tier.id == null) continue;
            boolean meetsMin = balance >= tier.minPoints;
            boolean meetsMax = tier.maxPoints == null || balance <= tier.maxPoints;
            if (meetsMin && meetsMax) return tier.id;
        }
        return null;
    }

    private String tierDisplayForBalance(int balance) {
        Integer id = findTierForBalance(balance);
        if (id == null) {
            return "";
        }
        for (RewardTierApi.RewardTierJson t : tierData) {
            if (id.equals(t.id)) {
                return t.name != null ? t.name : "";
            }
        }
        return "";
    }

    // ────────────────────────────────────────────────────────────
    // Inner types
    // ────────────────────────────────────────────────────────────

    private static final class CombinedData {
        final List<RewardTierApi.RewardTierJson> tiers;
        final List<AddressOption> addrs;
        final List<CustomerApi.CustomerRow> customers;

        CombinedData(List<RewardTierApi.RewardTierJson> tiers,
                     List<AddressOption> addrs, List<CustomerApi.CustomerRow> customers) {
            this.tiers = tiers;
            this.addrs = addrs;
            this.customers = customers;
        }
    }

    private static final class CustomerRefreshData {
        final List<RewardTierApi.RewardTierJson> tiers;
        final List<CustomerApi.CustomerRow> customers;

        CustomerRefreshData(List<RewardTierApi.RewardTierJson> tiers,
                            List<CustomerApi.CustomerRow> customers) {
            this.tiers = tiers;
            this.customers = customers;
        }
    }
}