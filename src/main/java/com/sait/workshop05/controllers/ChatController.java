package com.sait.workshop05.controllers;

import com.sait.workshop05.api.ChatApi;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private ToggleButton tabAll;
    @FXML private ToggleButton tabGeneral;
    @FXML private ToggleButton tabOrderIssue;
    @FXML private ToggleButton tabAccountHelp;
    @FXML private ToggleButton tabFeedback;
    @FXML private ToggleButton tabArchived;

    @FXML private ListView<ChatApi.ThreadJson> lstThreads;
    @FXML private Label lblThreadCount;
    @FXML private Button btnRefreshThreads;

    @FXML private Label lblHeaderName;
    @FXML private Label lblHeaderUsername;
    @FXML private Label lblHeaderCategory;
    @FXML private Label lblHeaderStatus;
    @FXML private StackPane paneHeaderAvatar;
    @FXML private Circle circleHeaderAvatarBg;
    @FXML private Label lblHeaderAvatarInitials;
    @FXML private ImageView imgHeaderAvatar;
    @FXML private Button btnAssign;
    @FXML private Button btnTransfer;
    @FXML private Button btnReopen;
    @FXML private Button btnClose;
    @FXML private Label lblActionError;

    @FXML private ScrollPane scrollMessages;
    @FXML private VBox vboxMessages;
    @FXML private TextArea txtComposer;
    @FXML private Button btnSend;
    @FXML private HBox composerBox;
    @FXML private Label lblClosedBanner;

    private final ObservableList<ChatApi.ThreadJson> threads = FXCollections.observableArrayList();
    private final List<Integer> loadedMessageIds = new ArrayList<>();

    private ChatApi.ThreadJson selectedThread;
    private String activeCategory = "";
    private boolean viewingArchived = false;

    private Timeline pollTimer;
    private String currentUserId;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MMM d, HH:mm");
    private static final int POLL_SECONDS = 3;

    @FXML
    void initialize() {
        currentUserId = UserSession.getInstance().getApiUserId();

        setupCategoryTabs();
        setupThreadList();
        setupComposer();

        boolean isAdmin = UserSession.getInstance().isAdmin();
        tabArchived.setVisible(isAdmin);
        tabArchived.setManaged(isAdmin);

        showEmptyState();
        refreshThreadsAsync();
        startPolling();
    }

    public void shutdown() {
        if (pollTimer != null) {
            pollTimer.stop();
            pollTimer = null;
        }
    }

    private void setupCategoryTabs() {
        ToggleGroup group = new ToggleGroup();
        List<ToggleButton> tabs = List.of(tabAll, tabGeneral, tabOrderIssue, tabAccountHelp, tabFeedback, tabArchived);
        for (ToggleButton tb : tabs) {
            tb.setToggleGroup(group);
        }
        tabAll.setSelected(true);

        tabAll.setOnAction(e -> switchCategory("", false));
        tabGeneral.setOnAction(e -> switchCategory("general", false));
        tabOrderIssue.setOnAction(e -> switchCategory("order_issue", false));
        tabAccountHelp.setOnAction(e -> switchCategory("account_help", false));
        tabFeedback.setOnAction(e -> switchCategory("feedback", false));
        tabArchived.setOnAction(e -> switchCategory("", true));

        group.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == null) o.setSelected(true);
        });
    }

    private void switchCategory(String category, boolean archived) {
        activeCategory = category;
        viewingArchived = archived;
        selectedThread = null;
        showEmptyState();
        refreshThreadsAsync();
    }

    private void setupThreadList() {
        lstThreads.setItems(threads);
        lstThreads.setCellFactory(lv -> new ListCell<>() {
            private final Circle avatarBg = new Circle(18, javafx.scene.paint.Color.web("#8A9E7F"));
            private final Label avatarInitials = new Label();
            private final ImageView avatarImage = new ImageView();
            private final StackPane avatar = new StackPane(avatarBg, avatarInitials, avatarImage);
            private final Label nameLabel = new Label();
            private final Label usernameLabel = new Label();
            private final Label categoryLabel = new Label();
            private final VBox textBox = new VBox(1, nameLabel, usernameLabel, categoryLabel);
            private final HBox row = new HBox(10, avatar, textBox);

            {
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 10, 8, 10));
                avatar.setPrefSize(36, 36);
                avatar.setMinSize(36, 36);
                avatarInitials.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
                avatarImage.setFitWidth(36);
                avatarImage.setFitHeight(36);
                avatarImage.setPreserveRatio(true);
                Rectangle clip = new Rectangle(36, 36);
                clip.setArcWidth(36);
                clip.setArcHeight(36);
                avatarImage.setClip(clip);
                avatarImage.setVisible(false);
                avatarImage.setManaged(false);
                nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C1A0E;");
                usernameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A8178;");
                categoryLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8A8178;");
            }

            @Override
            protected void updateItem(ChatApi.ThreadJson t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                String displayName = t.customerDisplayName != null && !t.customerDisplayName.isBlank()
                        ? t.customerDisplayName
                        : (t.customerUsername != null ? t.customerUsername : "Customer");
                nameLabel.setText(displayName);
                usernameLabel.setText(t.customerUsername != null ? "@" + t.customerUsername : "");
                categoryLabel.setText(prettifyCategory(t.category) + " · " + formatTimestamp(t.updatedAt));
                avatarInitials.setText(initialsFor(displayName));
                loadAvatarInto(avatarImage, avatarInitials, t.customerProfilePhotoPath);
                setGraphic(row);
                setStyle(isSelected() ? "-fx-background-color: #F0E6DC;" : "");
            }
        });

        lstThreads.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal != selectedThread) {
                selectThread(newVal);
            }
        });
    }

    private void setupComposer() {
        btnSend.setOnAction(e -> sendMessage());
        btnAssign.setOnAction(e -> assignToMe());
        btnTransfer.setOnAction(e -> openTransferPicker());
        btnReopen.setOnAction(e -> reopenThread());
        btnClose.setOnAction(e -> closeThread());
        btnRefreshThreads.setOnAction(e -> refreshThreadsAsync());

        txtComposer.setOnKeyPressed(ev -> {
            if (ev.getCode().toString().equals("ENTER") && !ev.isShiftDown()) {
                ev.consume();
                sendMessage();
            }
        });
    }

    private void startPolling() {
        pollTimer = new Timeline(new KeyFrame(Duration.seconds(POLL_SECONDS), e -> pollTick()));
        pollTimer.setCycleCount(Animation.INDEFINITE);
        pollTimer.play();
    }

    private void pollTick() {
        refreshThreadsAsync();
        if (selectedThread != null) {
            loadMessagesAsync(selectedThread.id, false);
        }
    }

    // ─── Thread list ──────────────────────────────────────────

    private void refreshThreadsAsync() {
        final String cat = activeCategory;
        final boolean archived = viewingArchived;

        Task<List<ChatApi.ThreadJson>> task = new Task<>() {
            @Override
            protected List<ChatApi.ThreadJson> call() throws Exception {
                return archived ? ChatApi.archivedThreads(cat.isBlank() ? null : cat)
                                : ChatApi.openThreads(cat.isBlank() ? null : cat);
            }
        };
        task.setOnSucceeded(ev -> {
            if (!cat.equals(activeCategory) || archived != viewingArchived) return;
            List<ChatApi.ThreadJson> fetched = task.getValue();
            mergeThreads(fetched);
            lblThreadCount.setText(threads.size() + (threads.size() == 1 ? " thread" : " threads"));
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            LogData.handleException("CHAT_THREADS", ex instanceof Exception ? (Exception) ex : new Exception(ex));
        });
        Thread th = new Thread(task, "chat-threads-fetch");
        th.setDaemon(true);
        th.start();
    }

    private void mergeThreads(List<ChatApi.ThreadJson> fetched) {
        threads.setAll(fetched);
        if (selectedThread != null) {
            fetched.stream()
                    .filter(t -> t.id.equals(selectedThread.id))
                    .findFirst()
                    .ifPresent(this::updateHeader);
        }
    }

    // ─── Selection / messages ─────────────────────────────────

    private void selectThread(ChatApi.ThreadJson t) {
        selectedThread = t;
        loadedMessageIds.clear();
        vboxMessages.getChildren().clear();
        updateHeader(t);
        lblActionError.setText("");
        composerBox.setVisible(true);
        composerBox.setManaged(true);
        loadMessagesAsync(t.id, true);
        if (!"closed".equalsIgnoreCase(t.status)) {
            markReadAsync(t.id);
        }
    }

    private void updateHeader(ChatApi.ThreadJson t) {
        String displayName = t.customerDisplayName != null && !t.customerDisplayName.isBlank()
                ? t.customerDisplayName
                : (t.customerUsername != null ? t.customerUsername : "Customer");
        lblHeaderName.setText(displayName);
        lblHeaderUsername.setText(t.customerUsername != null ? "@" + t.customerUsername : "");
        lblHeaderCategory.setText(prettifyCategory(t.category));
        applyHeaderAvatar(displayName, t.customerProfilePhotoPath);
        boolean closed = "closed".equalsIgnoreCase(t.status);
        lblHeaderStatus.setText(closed ? "Closed" : (t.employeeUserId == null ? "Unassigned" : "Assigned"));

        boolean mine = currentUserId != null
                && t.employeeUserId != null
                && currentUserId.equalsIgnoreCase(t.employeeUserId);
        boolean isAdmin = UserSession.getInstance().isAdmin();
        btnAssign.setVisible(!closed && t.employeeUserId == null);
        btnAssign.setManaged(!closed && t.employeeUserId == null);
        btnTransfer.setVisible(!closed && mine);
        btnTransfer.setManaged(!closed && mine);
        btnReopen.setVisible(closed && isAdmin);
        btnReopen.setManaged(closed && isAdmin);
        btnClose.setVisible(!closed && mine);
        btnClose.setManaged(!closed && mine);

        boolean showBanner = closed;
        lblClosedBanner.setVisible(showBanner);
        lblClosedBanner.setManaged(showBanner);
        composerBox.setVisible(!showBanner);
        composerBox.setManaged(!showBanner);
    }

    private void loadMessagesAsync(int threadId, boolean replaceAll) {
        Task<List<ChatApi.MessageJson>> task = new Task<>() {
            @Override
            protected List<ChatApi.MessageJson> call() throws Exception {
                return ChatApi.messages(threadId);
            }
        };
        task.setOnSucceeded(ev -> {
            if (selectedThread == null || selectedThread.id != threadId) return;
            List<ChatApi.MessageJson> msgs = task.getValue();
            if (replaceAll) {
                vboxMessages.getChildren().clear();
                loadedMessageIds.clear();
            }
            for (ChatApi.MessageJson m : msgs) {
                if (loadedMessageIds.contains(m.id)) continue;
                vboxMessages.getChildren().add(buildBubble(m));
                loadedMessageIds.add(m.id);
            }
            Platform.runLater(() -> scrollMessages.setVvalue(1.0));
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            LogData.handleException("CHAT_MESSAGES", ex instanceof Exception ? (Exception) ex : new Exception(ex));
        });
        Thread th = new Thread(task, "chat-messages-fetch");
        th.setDaemon(true);
        th.start();
    }

    private HBox buildBubble(ChatApi.MessageJson m) {
        boolean system = m.isSystem || m.staffOnly;
        if (system) {
            String prefix = m.staffOnly ? "AUDIT · " : "";
            Label pill = new Label(prefix + (m.text == null ? "" : m.text) + "  ·  " + formatTimestamp(m.sentAt));
            pill.setWrapText(true);
            pill.setMaxWidth(520);
            pill.setPadding(new Insets(6, 12, 6, 12));
            pill.setStyle(m.staffOnly
                    ? "-fx-background-color: rgba(138,158,127,0.08); -fx-text-fill: #6B8268; -fx-background-radius: 12; -fx-border-color: rgba(138,158,127,0.45); -fx-border-radius: 12; -fx-border-style: dashed; -fx-font-size: 11px;"
                    : "-fx-background-color: rgba(44,26,14,0.05); -fx-text-fill: #5C4A3E; -fx-background-radius: 12; -fx-font-size: 11px;");
            HBox row = new HBox(pill);
            row.setAlignment(Pos.CENTER);
            row.setPadding(new Insets(2, 12, 2, 12));
            return row;
        }

        boolean mine = currentUserId != null && currentUserId.equalsIgnoreCase(m.senderUserId);

        Label text = new Label(m.text == null ? "" : m.text);
        text.setWrapText(true);
        text.setMaxWidth(520);
        text.setPadding(new Insets(8, 12, 8, 12));
        text.setStyle(mine
                ? "-fx-background-color: #C4714A; -fx-text-fill: white; -fx-background-radius: 14;"
                : "-fx-background-color: #F0E6DC; -fx-text-fill: #2C1A0E; -fx-background-radius: 14;");

        Label time = new Label(formatTimestamp(m.sentAt));
        time.setStyle("-fx-font-size: 10px; -fx-text-fill: #8A8178;");

        VBox col = new VBox(2, text, time);
        col.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox row = new HBox(col);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 12, 2, 12));
        return row;
    }

    // ─── Actions ──────────────────────────────────────────────

    private void sendMessage() {
        if (selectedThread == null) return;
        String text = txtComposer.getText();
        if (text == null || text.trim().isEmpty()) return;
        final int threadId = selectedThread.id;
        final String payload = text.trim();
        txtComposer.clear();
        btnSend.setDisable(true);

        Task<ChatApi.MessageJson> task = new Task<>() {
            @Override
            protected ChatApi.MessageJson call() throws Exception {
                return ChatApi.postMessage(threadId, payload);
            }
        };
        task.setOnSucceeded(ev -> {
            btnSend.setDisable(false);
            if (selectedThread != null && selectedThread.id == threadId) {
                ChatApi.MessageJson m = task.getValue();
                if (!loadedMessageIds.contains(m.id)) {
                    vboxMessages.getChildren().add(buildBubble(m));
                    loadedMessageIds.add(m.id);
                    Platform.runLater(() -> scrollMessages.setVvalue(1.0));
                }
            }
            refreshThreadsAsync();
        });
        task.setOnFailed(ev -> {
            btnSend.setDisable(false);
            txtComposer.setText(payload);
            lblActionError.setText("Failed to send.");
            LogData.handleException("CHAT_SEND", new Exception(task.getException()));
        });
        Thread th = new Thread(task, "chat-send");
        th.setDaemon(true);
        th.start();
    }

    private void assignToMe() {
        if (selectedThread == null) return;
        final int threadId = selectedThread.id;
        Task<ChatApi.ThreadJson> task = new Task<>() {
            @Override
            protected ChatApi.ThreadJson call() throws Exception {
                return ChatApi.assign(threadId);
            }
        };
        task.setOnSucceeded(ev -> {
            selectedThread = task.getValue();
            updateHeader(selectedThread);
            refreshThreadsAsync();
        });
        task.setOnFailed(ev -> {
            lblActionError.setText("Failed to assign.");
            LogData.handleException("CHAT_ASSIGN", new Exception(task.getException()));
        });
        Thread th = new Thread(task, "chat-assign");
        th.setDaemon(true);
        th.start();
    }

    private void closeThread() {
        if (selectedThread == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Close this conversation?", ButtonType.YES, ButtonType.CANCEL);
        confirm.setHeaderText("Close thread");
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> doClose());
    }

    private void doClose() {
        final int threadId = selectedThread.id;
        Task<ChatApi.ThreadJson> task = new Task<>() {
            @Override
            protected ChatApi.ThreadJson call() throws Exception {
                return ChatApi.close(threadId);
            }
        };
        task.setOnSucceeded(ev -> {
            selectedThread = task.getValue();
            updateHeader(selectedThread);
            refreshThreadsAsync();
        });
        task.setOnFailed(ev -> {
            lblActionError.setText("Failed to close.");
            LogData.handleException("CHAT_CLOSE", new Exception(task.getException()));
        });
        Thread th = new Thread(task, "chat-close");
        th.setDaemon(true);
        th.start();
    }

    private void openTransferPicker() {
        if (selectedThread == null) return;
        final int threadId = selectedThread.id;

        Task<List<ChatApi.StaffJson>> loadTask = new Task<>() {
            @Override
            protected List<ChatApi.StaffJson> call() throws Exception {
                return ChatApi.staffRecipients();
            }
        };
        loadTask.setOnSucceeded(ev -> {
            List<ChatApi.StaffJson> staff = loadTask.getValue();
            if (staff.isEmpty()) {
                lblActionError.setText("No other staff available.");
                return;
            }
            ChoiceDialog<ChatApi.StaffJson> dialog = new ChoiceDialog<>(staff.get(0), staff);
            dialog.setTitle("Transfer conversation");
            dialog.setHeaderText("Transfer to which staff member?");
            dialog.setContentText("Staff:");
            dialog.setGraphic(null);
            // Render username (role)
            dialog.getItems().setAll(staff);
            Platform.runLater(() -> {
                // Custom converter via a ComboBox<StaffJson> in content — simplest: rely on toString
            });
            dialog.showAndWait().ifPresent(target -> doTransfer(threadId, target));
        });
        loadTask.setOnFailed(ev -> {
            lblActionError.setText("Could not load staff list.");
            LogData.handleException("CHAT_TRANSFER_LIST", new Exception(loadTask.getException()));
        });
        Thread th = new Thread(loadTask, "chat-staff-list");
        th.setDaemon(true);
        th.start();
    }

    private void doTransfer(int threadId, ChatApi.StaffJson target) {
        Task<ChatApi.ThreadJson> task = new Task<>() {
            @Override
            protected ChatApi.ThreadJson call() throws Exception {
                return ChatApi.transfer(threadId, target.userId);
            }
        };
        task.setOnSucceeded(ev -> {
            selectedThread = null;
            showEmptyState();
            refreshThreadsAsync();
        });
        task.setOnFailed(ev -> {
            lblActionError.setText("Failed to transfer.");
            LogData.handleException("CHAT_TRANSFER", new Exception(task.getException()));
        });
        Thread th = new Thread(task, "chat-transfer");
        th.setDaemon(true);
        th.start();
    }

    private void reopenThread() {
        if (selectedThread == null) return;
        final int threadId = selectedThread.id;
        Task<ChatApi.ThreadJson> task = new Task<>() {
            @Override
            protected ChatApi.ThreadJson call() throws Exception {
                return ChatApi.reopen(threadId);
            }
        };
        task.setOnSucceeded(ev -> {
            selectedThread = task.getValue();
            updateHeader(selectedThread);
            loadMessagesAsync(threadId, true);
            refreshThreadsAsync();
        });
        task.setOnFailed(ev -> {
            lblActionError.setText("Failed to reopen.");
            LogData.handleException("CHAT_REOPEN", new Exception(task.getException()));
        });
        Thread th = new Thread(task, "chat-reopen");
        th.setDaemon(true);
        th.start();
    }

    private void markReadAsync(int threadId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ChatApi.markRead(threadId);
                return null;
            }
        };
        Thread th = new Thread(task, "chat-read");
        th.setDaemon(true);
        th.start();
    }

    // ─── Helpers ──────────────────────────────────────────────

    private void showEmptyState() {
        vboxMessages.getChildren().clear();
        loadedMessageIds.clear();
        lblHeaderName.setText("Select a conversation");
        if (lblHeaderUsername != null) lblHeaderUsername.setText("");
        applyHeaderAvatar(null, null);
        lblHeaderCategory.setText("");
        lblHeaderStatus.setText("");
        lblActionError.setText("");
        btnAssign.setVisible(false);
        btnAssign.setManaged(false);
        btnTransfer.setVisible(false);
        btnTransfer.setManaged(false);
        btnReopen.setVisible(false);
        btnReopen.setManaged(false);
        btnClose.setVisible(false);
        btnClose.setManaged(false);
        lblClosedBanner.setVisible(false);
        lblClosedBanner.setManaged(false);
        composerBox.setVisible(false);
        composerBox.setManaged(false);
    }

    private String prettifyCategory(String raw) {
        if (raw == null || raw.isBlank()) return "General";
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private void applyHeaderAvatar(String displayName, String photoPath) {
        if (lblHeaderAvatarInitials == null || imgHeaderAvatar == null) return;
        lblHeaderAvatarInitials.setText(initialsFor(displayName));
        if (imgHeaderAvatar.getClip() == null) {
            Rectangle clip = new Rectangle(44, 44);
            clip.setArcWidth(44);
            clip.setArcHeight(44);
            imgHeaderAvatar.setClip(clip);
        }
        loadAvatarInto(imgHeaderAvatar, lblHeaderAvatarInitials, photoPath);
    }

    private void loadAvatarInto(ImageView target, Label initialsLabel, String photoPath) {
        target.setImage(null);
        target.setVisible(false);
        target.setManaged(false);
        if (initialsLabel != null) {
            initialsLabel.setVisible(true);
            initialsLabel.setManaged(true);
        }
        if (photoPath == null || photoPath.isBlank()) return;
        Image image = new Image(photoPath.trim(), 88, 88, true, true, true);
        Runnable apply = () -> {
            if (image.isError()) return;
            target.setImage(image);
            target.setVisible(true);
            target.setManaged(true);
            if (initialsLabel != null) {
                initialsLabel.setVisible(false);
                initialsLabel.setManaged(false);
            }
        };
        if (image.getProgress() >= 1.0) {
            Platform.runLater(apply);
        } else {
            image.progressProperty().addListener((obs, o, n) -> {
                if (n != null && n.doubleValue() >= 1.0) Platform.runLater(apply);
            });
        }
    }

    private String initialsFor(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String p = parts[0];
            return p.length() >= 2 ? p.substring(0, 2).toUpperCase() : p.toUpperCase();
        }
        String first = parts[0];
        String last = parts[parts.length - 1];
        return ("" + first.charAt(0) + last.charAt(0)).toUpperCase();
    }

    private String formatTimestamp(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return OffsetDateTime.parse(iso).format(DT_FMT);
        } catch (Exception e) {
            return iso;
        }
    }
}
