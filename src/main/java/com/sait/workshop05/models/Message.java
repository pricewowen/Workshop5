// Contributor(s): Robbie
// Main: Robbie - Legacy staff message model for messaging UI.

package com.sait.workshop05.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * JavaFX staff message row mapped from API UUID based payloads.
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

    /**
     * Creates an empty message row for JavaFX binding.
     */
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

    /** @return message id value. */
    public String getMessageId() {
        return messageId.get();
    }

    /** @return JavaFX property wrapper for message id. */
    public StringProperty messageIdProperty() {
        return messageId;
    }

    /** @param messageId message id value. */
    public void setMessageId(String messageId) {
        this.messageId.set(messageId != null ? messageId : "");
    }

    /** @return sender id value. */
    public String getSenderId() {
        return senderId.get();
    }

    /** @return JavaFX property wrapper for sender id. */
    public StringProperty senderIdProperty() {
        return senderId;
    }

    /** @param senderId sender id value. */
    public void setSenderId(String senderId) {
        this.senderId.set(senderId != null ? senderId : "");
    }

    /** @return receiver id value. */
    public String getReceiverId() {
        return receiverId.get();
    }

    /** @return JavaFX property wrapper for receiver id. */
    public StringProperty receiverIdProperty() {
        return receiverId;
    }

    /** @param receiverId receiver id value. */
    public void setReceiverId(String receiverId) {
        this.receiverId.set(receiverId != null ? receiverId : "");
    }

    /** @return message subject value. */
    public String getMessageSubject() {
        return messageSubject.get();
    }

    /** @return JavaFX property wrapper for message subject. */
    public StringProperty messageSubjectProperty() {
        return messageSubject;
    }

    /** @param messageSubject message subject value. */
    public void setMessageSubject(String messageSubject) {
        this.messageSubject.set(messageSubject);
    }

    /** @return message content value. */
    public String getMessageContent() {
        return messageContent.get();
    }

    /** @return JavaFX property wrapper for message content. */
    public StringProperty messageContentProperty() {
        return messageContent;
    }

    /** @param messageContent message content value. */
    public void setMessageContent(String messageContent) {
        this.messageContent.set(messageContent);
    }

    /** @return message sent timestamp value. */
    public LocalDateTime getMessageSentDateTime() {
        return messageSentDateTime.get();
    }

    /** @return JavaFX property wrapper for sent timestamp. */
    public ObjectProperty<LocalDateTime> messageSentDateTimeProperty() {
        return messageSentDateTime;
    }

    /** @param messageSentDateTime message sent timestamp value. */
    public void setMessageSentDateTime(LocalDateTime messageSentDateTime) {
        this.messageSentDateTime.set(messageSentDateTime);
    }

    /** @return read flag value. */
    public boolean isMessageIsRead() {
        return messageIsRead.get();
    }

    /** @return JavaFX property wrapper for read flag. */
    public BooleanProperty messageIsReadProperty() {
        return messageIsRead;
    }

    /** @param messageIsRead read flag value. */
    public void setMessageIsRead(boolean messageIsRead) {
        this.messageIsRead.set(messageIsRead);
    }

    /** @return sender display label. */
    public String getSenderDisplay() {
        return senderDisplay.get();
    }

    /** @return JavaFX property wrapper for sender display label. */
    public StringProperty senderDisplayProperty() {
        return senderDisplay;
    }

    /** @param senderDisplay sender display label. */
    public void setSenderDisplay(String senderDisplay) {
        this.senderDisplay.set(senderDisplay);
    }

    /** @return receiver display label. */
    public String getReceiverDisplay() {
        return receiverDisplay.get();
    }

    /** @return JavaFX property wrapper for receiver display label. */
    public StringProperty receiverDisplayProperty() {
        return receiverDisplay;
    }

    /** @param receiverDisplay receiver display label. */
    public void setReceiverDisplay(String receiverDisplay) {
        this.receiverDisplay.set(receiverDisplay);
    }

    /**
     * @return debug-friendly summary string for the message row.
     */
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
