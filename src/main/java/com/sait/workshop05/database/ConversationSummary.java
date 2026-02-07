package com.sait.workshop05.database;

import java.time.LocalDateTime;

/**
 * Helper class for conversation list display.
 * Summarises a conversation between the current user and a partner.
 */
public class ConversationSummary {
    private final int partnerId;
    private final String partnerUsername;
    private final LocalDateTime lastMessageTime;
    private final int unreadCount;

    public ConversationSummary(int partnerId, String partnerUsername,
                               LocalDateTime lastMessageTime, int unreadCount) {
        this.partnerId = partnerId;
        this.partnerUsername = partnerUsername;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public int getPartnerId() {
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
