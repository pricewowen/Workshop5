// Contributor(s): Robbie
// Main: Robbie - Legacy staff messaging inbox UI.

package com.sait.workshop05.controllers;

import com.sait.workshop05.api.MessageApi;
import com.sait.workshop05.api.ReferenceApi;
import com.sait.workshop05.models.ConversationSummary;
import com.sait.workshop05.models.Message;
import com.sait.workshop05.models.UserOption;
import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.util.ErrorHandler;
import com.sait.workshop05.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Staff messaging inbox and thread view backed by the messages API.
 */
public class MessagingController {

    @FXML private ListView<ConversationSummary> lstConversations;
    @FXML private TextField txtConversationSearch;
    @FXML private Button btnNewMessage;
    @FXML private Button btnRefreshConversations;
    @FXML private Label lblUnreadCount;

    @FXML private Label lblThreadTitle;
    @FXML private ScrollPane scrollMessages;
    @FXML private VBox vboxMessages;
    @FXML private TextField txtSubject;
    @FXML private TextArea txtMessageContent;
    @FXML private Button btnSend;
    @FXML private Label lblComposeStatus;

    private final ObservableList<ConversationSummary> conversationList = FXCollections.observableArrayList();
    private FilteredList<ConversationSummary> filteredConversations;

    private String currentUserUuid;
    private String selectedPartnerId = "";
    private String selectedPartnerName = "";

    private boolean isLoadingThread = false;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    void initialize() {
        currentUserUuid = UserSession.getInstance().getApiUserId();
        if (currentUserUuid == null || currentUserUuid.isBlank()) {
            ErrorHandler.showErrorDialog("Session", "Missing user id from login. Please log in again.");
        }

        setupConversationList();
        setupSearchFilter();
        setupSelectionBinding();
        refreshConversationsAsync();

        btnSend.setDisable(true);
        txtSubject.setDisable(true);
        txtMessageContent.setDisable(true);
    }

    private void setupConversationList() {
        lstConversations.setCellFactory(lv -> new ListCell<>() {
            private final Label nameLabel = new Label();
            private final Label timeLabel = new Label();
            private final HBox top = new HBox(8);
            private final VBox cellBox = new VBox(2);

            {
                top.setAlignment(Pos.CENTER_LEFT);
                cellBox.setPadding(new Insets(4, 6, 4, 6));
                cellBox.getChildren().addAll(top, timeLabel);
                // Reapply styles on selection changes to avoid stale row state.
                selectedProperty().addListener((obs, wasSelected, selected) -> applyStyles(selected));
            }

            private void applyStyles(boolean selected) {
                ConversationSummary item = getItem();
                if (selected) {
                    nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.75);");
                    setStyle("-fx-background-color: #C4714A;");
                } else {
                    nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C1A0E;");
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A8178;");
                    boolean unread = item != null && item.getUnreadCount() > 0;
                    setStyle(unread ? "-fx-background-color: #F8EDD5;" : "");
                }
            }

            @Override
            protected void updateItem(ConversationSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                nameLabel.setText(item.getPartnerUsername());
                timeLabel.setText(item.getLastMessageTime() != null
                        ? item.getLastMessageTime().format(DT_FMT) : "");

                top.getChildren().setAll(nameLabel);
                if (item.getUnreadCount() > 0) {
                    Label badge = new Label(String.valueOf(item.getUnreadCount()));
                    badge.setStyle("-fx-background-color: #C75B52; -fx-text-fill: white; " +
                            "-fx-padding: 1 6 1 6; -fx-background-radius: 10; -fx-font-size: 11px;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    top.getChildren().addAll(spacer, badge);
                }

                setGraphic(cellBox);
                setText(null);
                applyStyles(isSelected());
            }
        });
    }

    private void setupSearchFilter() {
        filteredConversations = new FilteredList<>(conversationList, p -> true);
        lstConversations.setItems(filteredConversations);
        lstConversations.setPlaceholder(new Label("Loading conversations…"));

        txtConversationSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredConversations.setPredicate(cs -> {
                if (filter.isEmpty()) return true;
                return cs.getPartnerUsername().toLowerCase().contains(filter);
            });
            updateConversationListPlaceholder();
        });
    }

    private void updateConversationListPlaceholder() {
        if (lstConversations == null || filteredConversations == null) {
            return;
        }
        if (filteredConversations.isEmpty()) {
            lstConversations.setPlaceholder(new Label(
                    conversationList.isEmpty()
                            ? "No conversations yet."
                            : "No conversations match the search filter."));
        }
    }

    private void setupSelectionBinding() {
        lstConversations.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null && !isLoadingThread) {
                        selectedPartnerId = newVal.getPartnerId();
                        selectedPartnerName = newVal.getPartnerUsername();
                        loadConversationThreadAsync();
                        enableComposeArea();
                    }
                }
        );
    }

    private static LocalDateTime parseSent(String s) {
        if (s == null || s.isBlank()) return LocalDateTime.MIN;
        try {
            return OffsetDateTime.parse(s).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(s);
            } catch (Exception e2) {
                return LocalDateTime.MIN;
            }
        }
    }

    // Async conversation loading.

    private void refreshConversationsAsync() {
        if (currentUserUuid == null || currentUserUuid.isBlank()) {
            lstConversations.setPlaceholder(new Label("Sign in again to load conversations."));
            return;
        }

        lstConversations.setPlaceholder(new Label("Loading conversations…"));

        Task<ConversationData> task = new Task<>() {
            @Override
            protected ConversationData call() throws Exception {
                List<MessageApi.LegacyMessageJson> rows = MessageApi.myMessages();
                Map<String, String> userNames = new HashMap<>();
                for (UserOption u : ReferenceApi.loadAdminUsers()) {
                    userNames.put(u.getUserId(), u.getUsername());
                }
                return new ConversationData(rows, userNames);
            }
        };
        task.setOnSucceeded(e -> applyConversations(task.getValue()));
        task.setOnFailed(e -> {
            Throwable t = task.getException();
            lstConversations.setPlaceholder(new Label("Could not load conversations."));
            ErrorHandler.showErrorDialog("Load Error", "Could not load conversations", t);
            LogData.handleException("LOAD_CONVERSATIONS", new RuntimeException(t));
        });
        new Thread(task).start();
    }

    private void applyConversations(ConversationData d) {
        Map<String, LocalDateTime> lastByPartner = new HashMap<>();
        Map<String, Integer> unreadByPartner = new HashMap<>();

        for (MessageApi.LegacyMessageJson m : d.messages) {
            String other = otherParty(m, currentUserUuid);
            if (other == null || other.isBlank()) continue;

            LocalDateTime t = parseSent(m.sentAt);
            lastByPartner.merge(other, t, (a, b) -> a.isAfter(b) ? a : b);

            if (m.receiverId != null && m.receiverId.equals(currentUserUuid) && !m.read) {
                unreadByPartner.merge(other, 1, Integer::sum);
            }
        }

        List<ConversationSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, LocalDateTime> entry : lastByPartner.entrySet()) {
            String pid = entry.getKey();
            String name = d.userNames.getOrDefault(pid, pid.substring(0, Math.min(8, pid.length())) + "…");
            int unread = unreadByPartner.getOrDefault(pid, 0);
            summaries.add(new ConversationSummary(pid, name, entry.getValue(), unread));
        }
        summaries.sort(Comparator.comparing(ConversationSummary::getLastMessageTime).reversed());

        conversationList.setAll(summaries);

        int totalUnread = summaries.stream().mapToInt(ConversationSummary::getUnreadCount).sum();
        lblUnreadCount.setText(totalUnread > 0 ? totalUnread + " unread" : "No unread messages");
        updateConversationListPlaceholder();
    }

    /**
     * Loads a conversation thread in a background task.
     * Eliminates the old double-fetch pattern: previously the conversation was
     * fetched once to mark messages read, then fetched again immediately after.
     * Now we mark-read in the background and use the same response for rendering.
     */
    private void loadConversationThreadAsync() {
        if (selectedPartnerId == null || selectedPartnerId.isBlank()) return;

        isLoadingThread = true;
        String partnerId = selectedPartnerId;
        String me = currentUserUuid;

        Task<List<MessageApi.LegacyMessageJson>> task = new Task<>() {
            @Override
            protected List<MessageApi.LegacyMessageJson> call() throws Exception {
                List<MessageApi.LegacyMessageJson> rows = MessageApi.conversation(partnerId);
                // Mark unread messages as read in the same pass to avoid a second fetch.
                for (MessageApi.LegacyMessageJson m : rows) {
                    if (m.receiverId != null && m.receiverId.equals(me) && !m.read && m.id != null) {
                        MessageApi.markRead(m.id);
                    }
                }
                return rows;
            }
        };
        task.setOnSucceeded(e -> {
            try {
                List<MessageApi.LegacyMessageJson> rows = task.getValue();
                List<Message> messages = new ArrayList<>();
                for (MessageApi.LegacyMessageJson m : rows) {
                    messages.add(MessageApi.toModel(m));
                }

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

                scrollMessages.applyCss();
                scrollMessages.layout();
                scrollMessages.setVvalue(1.0);

                // Refresh sidebar counts so unread badges clear after open.
                refreshConversationsAsync();

                for (ConversationSummary cs : conversationList) {
                    if (cs.getPartnerId().equals(selectedPartnerId)) {
                        lstConversations.getSelectionModel().select(cs);
                        break;
                    }
                }

                if (!messages.isEmpty()) {
                    Message lastMsg = messages.get(messages.size() - 1);
                    String lastSubject = lastMsg.getMessageSubject();
                    if (lastSubject != null && !lastSubject.startsWith("Re: ")) {
                        txtSubject.setText("Re: " + lastSubject);
                    } else if (lastSubject != null) {
                        txtSubject.setText(lastSubject);
                    }
                }
            } finally {
                isLoadingThread = false;
            }
        });
        task.setOnFailed(e -> {
            isLoadingThread = false;
            Throwable t = task.getException();
            ErrorHandler.showErrorDialog("Load Error", "Could not load message thread", t);
            LogData.handleException("LOAD_THREAD", new RuntimeException(t));
        });
        new Thread(task).start();
    }

    private VBox createMessageBubble(Message msg) {
        boolean isSentByMe = currentUserUuid != null && currentUserUuid.equals(msg.getSenderId());

        VBox bubble = new VBox(2);
        bubble.setPadding(new Insets(6, 10, 6, 10));
        bubble.setMaxWidth(500);

        if (msg.getMessageSubject() != null && !msg.getMessageSubject().isBlank()) {
            Label subjectLabel = new Label(msg.getMessageSubject());
            subjectLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4e342e;");
            subjectLabel.setWrapText(true);
            bubble.getChildren().add(subjectLabel);
        }

        Label contentLabel = new Label(msg.getMessageContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px;");
        bubble.getChildren().add(contentLabel);

        Label timeLabel = new Label(
                msg.getMessageSentDateTime() != null
                        ? msg.getMessageSentDateTime().format(DT_FMT)
                        : ""
        );
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8A8178;");
        bubble.getChildren().add(timeLabel);

        if (isSentByMe) {
            bubble.setStyle("-fx-background-color: #E8F0E5; -fx-background-radius: 12; " +
                    "-fx-border-color: #C5D9C8; -fx-border-radius: 12;");
        } else {
            bubble.setStyle("-fx-background-color: #F0EBE3; -fx-background-radius: 12; " +
                    "-fx-border-color: #E8E2DA; -fx-border-radius: 12;");
        }

        HBox wrapper = new HBox();
        wrapper.setPadding(new Insets(2, 0, 2, 0));

        if (isSentByMe) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            wrapper.getChildren().addAll(spacer, bubble);
        } else {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            wrapper.getChildren().addAll(bubble, spacer);
        }

        return new VBox(wrapper);
    }

    @FXML
    void onNewMessage() {
        try {
            List<UserOption> allUsers = ReferenceApi.loadAdminUsers();
            allUsers.removeIf(u -> currentUserUuid != null && u.getUserId().equals(currentUserUuid));

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

                try {
                    List<MessageApi.LegacyMessageJson> existing = MessageApi.conversation(selectedPartnerId);
                    vboxMessages.getChildren().clear();

                    if (existing.isEmpty()) {
                        Label empty = new Label("No messages yet. Send the first message below!");
                        empty.setStyle("-fx-text-fill: #8A8178; -fx-font-size: 13px;");
                        vboxMessages.getChildren().add(empty);
                    } else {
                        for (MessageApi.LegacyMessageJson m : existing) {
                            vboxMessages.getChildren().add(createMessageBubble(MessageApi.toModel(m)));
                        }
                    }
                } catch (Exception e) {
                    LogData.handleException("LOAD_CONVERSATION", e);
                }

                isLoadingThread = true;
                try {
                    for (ConversationSummary cs : conversationList) {
                        if (cs.getPartnerId().equals(selectedPartnerId)) {
                            lstConversations.getSelectionModel().select(cs);
                            break;
                        }
                    }
                } finally {
                    isLoadingThread = false;
                }
            });

        } catch (Exception e) {
            ErrorHandler.showErrorDialog("Error", "Could not load staff users", e);
            LogData.handleException("LOAD_STAFF_USERS", e);
        }
    }

    @FXML
    void onSendMessage() {
        if (selectedPartnerId == null || selectedPartnerId.isBlank()) {
            ErrorHandler.showErrorDialog("No Recipient", "Please select a conversation or start a new message.");
            return;
        }

        String subject = txtSubject.getText() != null ? txtSubject.getText().trim() : "";
        String content = txtMessageContent.getText() != null ? txtMessageContent.getText().trim() : "";

        if (content.isEmpty()) {
            ErrorHandler.showErrorDialog("Validation Error", "Message content cannot be empty.");
            txtMessageContent.requestFocus();
            return;
        }

        if (subject.isEmpty()) {
            subject = "(No Subject)";
        }

        if (subject.length() > 255) {
            ErrorHandler.showErrorDialog("Validation Error", "Subject must be 255 characters or less.");
            return;
        }

        if (content.length() > 2000) {
            ErrorHandler.showErrorDialog("Validation Error", "Message content must be 2000 characters or less.");
            return;
        }

        try {
            MessageApi.send(selectedPartnerId, subject, content);

            LogData.logAction("SEND_MESSAGE", "Message to " + selectedPartnerName);

            txtMessageContent.clear();
            lblComposeStatus.setText("Message sent!");

            loadConversationThreadAsync();

        } catch (Exception e) {
            ErrorHandler.showErrorDialog("Send Error", "Failed to send message", e);
            LogData.handleException("SEND_MESSAGE", e);
        }
    }

    @FXML
    void onRefreshConversations() {
        String previousPartnerId = selectedPartnerId;
        refreshConversationsAsync();

        if (previousPartnerId != null && !previousPartnerId.isBlank()) {
            isLoadingThread = true;
            try {
                for (ConversationSummary cs : conversationList) {
                    if (cs.getPartnerId().equals(previousPartnerId)) {
                        lstConversations.getSelectionModel().select(cs);
                        break;
                    }
                }
            } finally {
                isLoadingThread = false;
            }
        }
    }

    private void enableComposeArea() {
        btnSend.setDisable(false);
        txtSubject.setDisable(false);
        txtMessageContent.setDisable(false);
    }

    private static String otherParty(MessageApi.LegacyMessageJson m, String me) {
        if (m.senderId != null && m.senderId.equals(me)) {
            return m.receiverId;
        }
        return m.senderId;
    }

    // Inner types.

    private static final class ConversationData {
        final List<MessageApi.LegacyMessageJson> messages;
        final Map<String, String> userNames;

        ConversationData(List<MessageApi.LegacyMessageJson> messages, Map<String, String> userNames) {
            this.messages = messages;
            this.userNames = userNames;
        }
    }
}
