package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Message model representing a message between users (API uses UUID strings for user and message ids).
 */
public class Message {
    private final StringProperty messageId;
    private final StringProperty senderId;
    private final StringProperty receiverId;
    private final StringProperty messageSubject;
    private final StringProperty messageContent;
    private final ObjectProperty<LocalDateTime> messageSentDateTime;
    private final BooleanProperty messageIsRead;

    private final StringProperty senderDisplay;
    private final StringProperty receiverDisplay;

    public Message() {
        this.messageId = new SimpleStringProperty();
        this.senderId = new SimpleStringProperty();
        this.receiverId = new SimpleStringProperty();
        this.messageSubject = new SimpleStringProperty();
        this.messageContent = new SimpleStringProperty();
        this.messageSentDateTime = new SimpleObjectProperty<>();
        this.messageIsRead = new SimpleBooleanProperty(false);
        this.senderDisplay = new SimpleStringProperty();
        this.receiverDisplay = new SimpleStringProperty();
    }

    public String getMessageId() {
        return messageId.get();
    }

    public StringProperty messageIdProperty() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId.set(messageId != null ? messageId : "");
    }

    public String getSenderId() {
        return senderId.get();
    }

    public StringProperty senderIdProperty() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId.set(senderId != null ? senderId : "");
    }

    public String getReceiverId() {
        return receiverId.get();
    }

    public StringProperty receiverIdProperty() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId.set(receiverId != null ? receiverId : "");
    }

    public String getMessageSubject() {
        return messageSubject.get();
    }

    public StringProperty messageSubjectProperty() {
        return messageSubject;
    }

    public void setMessageSubject(String messageSubject) {
        this.messageSubject.set(messageSubject);
    }

    public String getMessageContent() {
        return messageContent.get();
    }

    public StringProperty messageContentProperty() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent.set(messageContent);
    }

    public LocalDateTime getMessageSentDateTime() {
        return messageSentDateTime.get();
    }

    public ObjectProperty<LocalDateTime> messageSentDateTimeProperty() {
        return messageSentDateTime;
    }

    public void setMessageSentDateTime(LocalDateTime messageSentDateTime) {
        this.messageSentDateTime.set(messageSentDateTime);
    }

    public boolean isMessageIsRead() {
        return messageIsRead.get();
    }

    public BooleanProperty messageIsReadProperty() {
        return messageIsRead;
    }

    public void setMessageIsRead(boolean messageIsRead) {
        this.messageIsRead.set(messageIsRead);
    }

    public String getSenderDisplay() {
        return senderDisplay.get();
    }

    public StringProperty senderDisplayProperty() {
        return senderDisplay;
    }

    public void setSenderDisplay(String senderDisplay) {
        this.senderDisplay.set(senderDisplay);
    }

    public String getReceiverDisplay() {
        return receiverDisplay.get();
    }

    public StringProperty receiverDisplayProperty() {
        return receiverDisplay;
    }

    public void setReceiverDisplay(String receiverDisplay) {
        this.receiverDisplay.set(receiverDisplay);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId.get() +
                ", senderId=" + senderId.get() +
                ", receiverId=" + receiverId.get() +
                ", messageSubject='" + messageSubject.get() + '\'' +
                ", messageSentDateTime=" + messageSentDateTime.get() +
                ", messageIsRead=" + messageIsRead.get() +
                '}';
    }
}
