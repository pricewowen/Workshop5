package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Message model representing a message between users.
 * This is a preliminary model - table may need to be created in the database.
 */
public class Message {
    private final IntegerProperty messageId;
    private final IntegerProperty senderId;
    private final IntegerProperty receiverId;
    private final StringProperty messageSubject;
    private final StringProperty messageContent;
    private final ObjectProperty<LocalDateTime> messageSentDateTime;
    private final BooleanProperty messageIsRead;

    // Display properties
    private final StringProperty senderDisplay;
    private final StringProperty receiverDisplay;

    /**
     * Default constructor - initializes all properties
     */
    public Message() {
        this.messageId = new SimpleIntegerProperty();
        this.senderId = new SimpleIntegerProperty();
        this.receiverId = new SimpleIntegerProperty();
        this.messageSubject = new SimpleStringProperty();
        this.messageContent = new SimpleStringProperty();
        this.messageSentDateTime = new SimpleObjectProperty<>();
        this.messageIsRead = new SimpleBooleanProperty(false);
        this.senderDisplay = new SimpleStringProperty();
        this.receiverDisplay = new SimpleStringProperty();
    }

    // Getters and Setters
    public int getMessageId() {
        return messageId.get();
    }

    public IntegerProperty messageIdProperty() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId.set(messageId);
    }

    public int getSenderId() {
        return senderId.get();
    }

    public IntegerProperty senderIdProperty() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId.set(senderId);
    }

    public int getReceiverId() {
        return receiverId.get();
    }

    public IntegerProperty receiverIdProperty() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId.set(receiverId);
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

