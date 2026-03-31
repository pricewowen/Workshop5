package com.sait.workshop05.controllers;

import com.sait.workshop05.models.ConversationSummary;
import com.sait.workshop05.database.MessageDAO;
import com.sait.workshop05.models.UserOption;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.models.Message;
import com.sait.workshop05.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the internal staff messaging view (Phase 10).
 * Supports conversation list, message thread display, and sending new messages.
 */
public class MessagingController {

    // ── Left panel: Conversations ──
    @FXML private ListView<ConversationSummary> lstConversations;
    @FXML private TextField txtConversationSearch;
    @FXML private Button btnNewMessage;
    @FXML private Button btnRefreshConversations;
    @FXML private Label lblUnreadCount;

    // ── Right panel: Thread ──
    @FXML private Label lblThreadTitle;
    @FXML private ScrollPane scrollMessages;
    @FXML private VBox vboxMessages;
    @FXML private TextField txtSubject;
    @FXML private TextArea txtMessageContent;
    @FXML private Button btnSend;
    @FXML private Label lblComposeStatus;

    private final MessageDAO messageDAO = new MessageDAO();
    private final ObservableList<ConversationSummary> conversationList = FXCollections.observableArrayList();
    private FilteredList<ConversationSummary> filteredConversations;

    private int currentUserId;
    private int selectedPartnerId = -1;
    private String selectedPartnerName = "";

    /** Guard flag to prevent infinite recursion between selection listener and loadConversationThread(). */
    private boolean isLoadingThread = false;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ───────────────────────────────────────────────
    // Initialise
    // ───────────────────────────────────────────────

    @FXML
    void initialize() {
        currentUserId = UserSession.getInstance().getCurrentUser().getUserId();

        setupConversationList();
        setupSearchFilter();
        setupSelectionBinding();
        refreshConversations();

        // Disable send controls until a conversation is selected
        btnSend.setDisable(true);
        txtSubject.setDisable(true);
        txtMessageContent.setDisable(true);
    }

    // ───────────────────────────────────────────────
    // Conversation list setup
    // ───────────────────────────────────────────────

    private void setupConversationList() {
        lstConversations.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ConversationSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cell = new VBox(2);
                    cell.setPadding(new Insets(4, 6, 4, 6));

                    Label nameLabel = new Label(item.getPartnerUsername());
                    nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

                    Label timeLabel = new Label(item.getLastMessageTime().format(DT_FMT));
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A8178;");

                    HBox top = new HBox(8);
                    top.setAlignment(Pos.CENTER_LEFT);
                    top.getChildren().add(nameLabel);

                    if (item.getUnreadCount() > 0) {
                        Label badge = new Label(String.valueOf(item.getUnreadCount()));
                        badge.setStyle("-fx-background-color: #C75B52; -fx-text-fill: white; " +
                                "-fx-padding: 1 6 1 6; -fx-background-radius: 10; -fx-font-size: 11px;");
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        top.getChildren().addAll(spacer, badge);
                    }

                    cell.getChildren().addAll(top, timeLabel);
                    setGraphic(cell);
                    setText(null);

                    // Highlight rows with unread messages
                    if (item.getUnreadCount() > 0) {
                        setStyle("-fx-background-color: #F8EDD5;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupSearchFilter() {
        filteredConversations = new FilteredList<>(conversationList, p -> true);
        lstConversations.setItems(filteredConversations);

        txtConversationSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredConversations.setPredicate(cs -> {
                if (filter.isEmpty()) return true;
                return cs.getPartnerUsername().toLowerCase().contains(filter);
            });
        });
    }

    private void setupSelectionBinding() {
        lstConversations.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null && !isLoadingThread) {
                        selectedPartnerId = newVal.getPartnerId();
                        selectedPartnerName = newVal.getPartnerUsername();
                        loadConversationThread();
                        enableComposeArea();
                    }
                }
        );
    }

    // ───────────────────────────────────────────────
    // Load conversation data
    // ───────────────────────────────────────────────

    private void refreshConversations() {
        try {
            List<ConversationSummary> partners = messageDAO.getConversationPartners(currentUserId);
            conversationList.setAll(partners);

            int totalUnread = partners.stream().mapToInt(ConversationSummary::getUnreadCount).sum();
            lblUnreadCount.setText(totalUnread > 0 ? totalUnread + " unread" : "No unread messages");

        } catch (SQLException e) {
            ErrorHandler.showErrorDialog("Load Error", "Could not load conversations", e.getMessage());
            LogData.handleException("LOAD_CONVERSATIONS", e);
        }
    }

    private void loadConversationThread() {
        if (selectedPartnerId < 0) return;

        isLoadingThread = true;
        try {
            // Mark messages from this partner as read
            messageDAO.markConversationAsRead(currentUserId, selectedPartnerId);

            // Load the conversation
            List<Message> messages = messageDAO.getConversation(currentUserId, selectedPartnerId);

            lblThreadTitle.setText("Conversation with " + selectedPartnerName);
            vboxMessages.getChildren().clear();

            if (messages.isEmpty()) {
                Label empty = new Label("No messages yet. Send the first message below!");
                empty.setStyle("-fx-text-fill: #8A8178; -fx-font-size: 13px;");
                vboxMessages.getChildren().add(empty);
            } else {
                for (Message msg : messages) {
                    vboxMessages.getChildren().add(createMessageBubble(msg));
                }
            }

            // Scroll to bottom after messages load
            scrollMessages.applyCss();
            scrollMessages.layout();
            scrollMessages.setVvalue(1.0);

            // Refresh the conversation list to update unread counts
            refreshConversations();

            // Re-select the current partner (guard flag prevents recursion)
            for (ConversationSummary cs : conversationList) {
                if (cs.getPartnerId() == selectedPartnerId) {
                    lstConversations.getSelectionModel().select(cs);
                    break;
                }
            }

            // Pre-fill subject for replies
            if (!messages.isEmpty()) {
                Message lastMsg = messages.get(messages.size() - 1);
                String lastSubject = lastMsg.getMessageSubject();
                if (lastSubject != null && !lastSubject.startsWith("Re: ")) {
                    txtSubject.setText("Re: " + lastSubject);
                } else if (lastSubject != null) {
                    txtSubject.setText(lastSubject);
                }
            }

        } catch (SQLException e) {
            ErrorHandler.showErrorDialog("Load Error", "Could not load message thread", e.getMessage());
            LogData.handleException("LOAD_THREAD", e);
        } finally {
            isLoadingThread = false;
        }
    }

    // ───────────────────────────────────────────────
    // Message bubble factory
    // ───────────────────────────────────────────────

    private VBox createMessageBubble(Message msg) {
        boolean isSentByMe = msg.getSenderId() == currentUserId;

        VBox bubble = new VBox(2);
        bubble.setPadding(new Insets(6, 10, 6, 10));
        bubble.setMaxWidth(500);

        // Subject line (if present)
        if (msg.getMessageSubject() != null && !msg.getMessageSubject().isBlank()) {
            Label subjectLabel = new Label(msg.getMessageSubject());
            subjectLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4e342e;");
            subjectLabel.setWrapText(true);
            bubble.getChildren().add(subjectLabel);
        }

        // Message content
        Label contentLabel = new Label(msg.getMessageContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px;");
        bubble.getChildren().add(contentLabel);

        // Timestamp
        Label timeLabel = new Label(
                msg.getMessageSentDateTime() != null
                        ? msg.getMessageSentDateTime().format(DT_FMT)
                        : ""
        );
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8A8178;");
        bubble.getChildren().add(timeLabel);

        // Styling
        if (isSentByMe) {
            bubble.setStyle("-fx-background-color: #E8F0E5; -fx-background-radius: 12; " +
                    "-fx-border-color: #C5D9C8; -fx-border-radius: 12;");
        } else {
            bubble.setStyle("-fx-background-color: #F0EBE3; -fx-background-radius: 12; " +
                    "-fx-border-color: #E8E2DA; -fx-border-radius: 12;");
        }

        // Wrap in HBox for alignment
        HBox wrapper = new HBox();
        wrapper.setPadding(new Insets(2, 0, 2, 0));

        if (isSentByMe) {
            // Sent messages align right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            wrapper.getChildren().addAll(spacer, bubble);
        } else {
            // Received messages align left
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            wrapper.getChildren().addAll(bubble, spacer);
        }

        VBox container = new VBox(wrapper);
        return container;
    }

    // ───────────────────────────────────────────────
    // Actions
    // ───────────────────────────────────────────────

    @FXML
    void onNewMessage() {
        try {
            List<UserOption> allUsers = messageDAO.getAllStaffUsers();

            // Remove current user from list
            allUsers.removeIf(u -> u.getUserId() == currentUserId);

            if (allUsers.isEmpty()) {
                ErrorHandler.showInfo("No Recipients", "There are no other staff users to message.");
                return;
            }

            ChoiceDialog<UserOption> dialog = new ChoiceDialog<>(allUsers.get(0), allUsers);
            dialog.setTitle("New Message");
            dialog.setHeaderText("Start a new conversation");
            dialog.setContentText("Select recipient:");

            Optional<UserOption> result = dialog.showAndWait();
            result.ifPresent(user -> {
                selectedPartnerId = user.getUserId();
                selectedPartnerName = user.getUsername();
                lblThreadTitle.setText("Conversation with " + selectedPartnerName);
                enableComposeArea();
                txtSubject.setText("");
                txtSubject.requestFocus();

                // Load existing conversation if any
                try {
                    List<Message> existing = messageDAO.getConversation(currentUserId, selectedPartnerId);
                    vboxMessages.getChildren().clear();

                    if (existing.isEmpty()) {
                        Label empty = new Label("No messages yet. Send the first message below!");
                        empty.setStyle("-fx-text-fill: #8A8178; -fx-font-size: 13px;");
                        vboxMessages.getChildren().add(empty);
                    } else {
                        for (Message msg : existing) {
                            vboxMessages.getChildren().add(createMessageBubble(msg));
                        }
                    }
                } catch (SQLException e) {
                    LogData.handleException("LOAD_CONVERSATION", e);
                }

                // Select existing conversation if it exists (guarded to prevent recursion)
                isLoadingThread = true;
                try {
                    for (ConversationSummary cs : conversationList) {
                        if (cs.getPartnerId() == selectedPartnerId) {
                            lstConversations.getSelectionModel().select(cs);
                            break;
                        }
                    }
                } finally {
                    isLoadingThread = false;
                }
            });

        } catch (SQLException e) {
            ErrorHandler.showErrorDialog("Error", "Could not load staff users", e.getMessage());
            LogData.handleException("LOAD_STAFF_USERS", e);
        }
    }

    @FXML
    void onSendMessage() {
        if (selectedPartnerId < 0) {
            ErrorHandler.showErrorDialog("No Recipient", "Please select a conversation or start a new message.", null);
            return;
        }

        String subject = txtSubject.getText() != null ? txtSubject.getText().trim() : "";
        String content = txtMessageContent.getText() != null ? txtMessageContent.getText().trim() : "";

        // Validation
        if (content.isEmpty()) {
            ErrorHandler.showErrorDialog("Validation Error", "Message content cannot be empty.", null);
            txtMessageContent.requestFocus();
            return;
        }

        if (subject.isEmpty()) {
            subject = "(No Subject)";
        }

        if (subject.length() > 255) {
            ErrorHandler.showErrorDialog("Validation Error", "Subject must be 255 characters or less.", null);
            return;
        }

        if (content.length() > 2000) {
            ErrorHandler.showErrorDialog("Validation Error", "Message content must be 2000 characters or less.", null);
            return;
        }

        try {
            Message message = new Message();
            message.setSenderId(currentUserId);
            message.setReceiverId(selectedPartnerId);
            message.setMessageSubject(subject);
            message.setMessageContent(content);
            message.setMessageSentDateTime(LocalDateTime.now());
            message.setMessageIsRead(false);

            int newId = messageDAO.sendMessage(message);

            if (newId > 0) {
                LogData.logAction("SEND_MESSAGE",
                        "Message #" + newId + " to " + selectedPartnerName);

                // Clear compose area
                txtMessageContent.clear();
                lblComposeStatus.setText("Message sent!");

                // Reload conversation
                loadConversationThread();
            } else {
                ErrorHandler.showErrorDialog("Send Error", "Message could not be sent.", null);
            }

        } catch (SQLException e) {
            ErrorHandler.showErrorDialog("Send Error", "Failed to send message", ErrorHandler.friendlyDbMessage(e));
            LogData.handleException("SEND_MESSAGE", e);
        }
    }

    @FXML
    void onRefreshConversations() {
        int previousPartnerId = selectedPartnerId;
        refreshConversations();

        // Re-select previous conversation (guarded to prevent recursion)
        if (previousPartnerId > 0) {
            isLoadingThread = true;
            try {
                for (ConversationSummary cs : conversationList) {
                    if (cs.getPartnerId() == previousPartnerId) {
                        lstConversations.getSelectionModel().select(cs);
                        break;
                    }
                }
            } finally {
                isLoadingThread = false;
            }
        }
    }

    // ───────────────────────────────────────────────
    // Helpers
    // ───────────────────────────────────────────────

    private void enableComposeArea() {
        btnSend.setDisable(false);
        txtSubject.setDisable(false);
        txtMessageContent.setDisable(false);
    }

}
