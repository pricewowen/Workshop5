// Contributor(s): Robbie
// Main: Robbie - Conversation summary row for inbox lists.

package com.sait.workshop05.models;

import java.time.LocalDateTime;

/**
 * One inbox row for the legacy messaging UI with partner id and last activity and unread count.
 */
public class ConversationSummary {
    /** Partner user id (UUID string). */
    private final String partnerId;
    private final String partnerUsername;
    private final LocalDateTime lastMessageTime;
    private final int unreadCount;

    /**
     * Creates one conversation summary row for inbox rendering.
     */
    public ConversationSummary(String partnerId, String partnerUsername,
                               LocalDateTime lastMessageTime, int unreadCount) {
        this.partnerId = partnerId;
        this.partnerUsername = partnerUsername;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    /**
     * Returns partner user id.
     */
    public String getPartnerId() {
        return partnerId;
    }

    /**
     * Returns partner username label.
     */
    public String getPartnerUsername() {
        return partnerUsername;
    }

    /**
     * Returns timestamp of latest message in the thread.
     */
    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    /**
     * Returns unread message count for the thread.
     */
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
