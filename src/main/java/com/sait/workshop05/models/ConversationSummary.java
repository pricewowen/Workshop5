package com.sait.workshop05.models;

import java.time.LocalDateTime;

/**
 * Helper class for conversation list display.
 * Summarises a conversation between the current user and a partner.
 */
public class ConversationSummary {
    /** Partner user id (UUID string). */
    private final String partnerId;
    private final String partnerUsername;
    private final LocalDateTime lastMessageTime;
    private final int unreadCount;

    public ConversationSummary(String partnerId, String partnerUsername,
                               LocalDateTime lastMessageTime, int unreadCount) {
        this.partnerId = partnerId;
        this.partnerUsername = partnerUsername;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getPartnerUsername() {
        return partnerUsername;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    @Override
    public String toString() {
        String display = partnerUsername;
        if (unreadCount > 0) {
            display += " (" + unreadCount + ")";
        }
        return display;
    }
}
