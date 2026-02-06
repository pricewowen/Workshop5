package com.sait.workshop05.database;

import com.sait.workshop05.models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Message entity.
 * Handles all database operations related to messages.
 *
 * Note: This is a preliminary implementation. The Message table
 * may need to be created in the database schema.
 */
public class MessageDAO {

    /**
     * Get all messages for a specific user (as sender or receiver)
     */
    public List<Message> getMessagesByUserId(int userId) throws SQLException {
        String sql =
                "SELECT m.messageId, m.senderId, m.receiverId, m.messageSubject, " +
                "       m.messageContent, m.messageSentDateTime, m.messageIsRead, " +
                "       sender.userUsername AS senderDisplay, " +
                "       receiver.userUsername AS receiverDisplay " +
                "FROM Message m " +
                "JOIN `User` sender ON m.senderId = sender.userId " +
                "JOIN `User` receiver ON m.receiverId = receiver.userId " +
                "WHERE m.senderId = ? OR m.receiverId = ? " +
                "ORDER BY m.messageSentDateTime DESC";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message message = mapResultSetToMessage(rs);
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    /**
     * Get messages received by a user
     */
    public List<Message> getReceivedMessages(int userId) throws SQLException {
        String sql =
                "SELECT m.messageId, m.senderId, m.receiverId, m.messageSubject, " +
                "       m.messageContent, m.messageSentDateTime, m.messageIsRead, " +
                "       sender.userUsername AS senderDisplay, " +
                "       receiver.userUsername AS receiverDisplay " +
                "FROM Message m " +
                "JOIN `User` sender ON m.senderId = sender.userId " +
                "JOIN `User` receiver ON m.receiverId = receiver.userId " +
                "WHERE m.receiverId = ? " +
                "ORDER BY m.messageSentDateTime DESC";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message message = mapResultSetToMessage(rs);
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    /**
     * Get messages sent by a user
     */
    public List<Message> getSentMessages(int userId) throws SQLException {
        String sql =
                "SELECT m.messageId, m.senderId, m.receiverId, m.messageSubject, " +
                "       m.messageContent, m.messageSentDateTime, m.messageIsRead, " +
                "       sender.userUsername AS senderDisplay, " +
                "       receiver.userUsername AS receiverDisplay " +
                "FROM Message m " +
                "JOIN `User` sender ON m.senderId = sender.userId " +
                "JOIN `User` receiver ON m.receiverId = receiver.userId " +
                "WHERE m.senderId = ? " +
                "ORDER BY m.messageSentDateTime DESC";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message message = mapResultSetToMessage(rs);
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    /**
     * Get unread message count for a user
     */
    public int getUnreadMessageCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Message WHERE receiverId = ? AND messageIsRead = 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }

        return 0;
    }

    /**
     * Send a new message
     */
    public int sendMessage(Message message) throws SQLException {
        String sql =
                "INSERT INTO Message " +
                "(senderId, receiverId, messageSubject, messageContent, messageSentDateTime, messageIsRead) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            ps.setString(3, message.getMessageSubject());
            ps.setString(4, message.getMessageContent());
            ps.setTimestamp(5, Timestamp.valueOf(message.getMessageSentDateTime()));
            ps.setBoolean(6, message.isMessageIsRead());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * Mark a message as read
     */
    public boolean markAsRead(int messageId) throws SQLException {
        String sql = "UPDATE Message SET messageIsRead = 1 WHERE messageId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, messageId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Delete a message
     */
    public boolean deleteMessage(int messageId) throws SQLException {
        String sql = "DELETE FROM Message WHERE messageId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, messageId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Get message by ID
     */
    public Message getMessageById(int messageId) throws SQLException {
        String sql =
                "SELECT m.messageId, m.senderId, m.receiverId, m.messageSubject, " +
                "       m.messageContent, m.messageSentDateTime, m.messageIsRead, " +
                "       sender.userUsername AS senderDisplay, " +
                "       receiver.userUsername AS receiverDisplay " +
                "FROM Message m " +
                "JOIN `User` sender ON m.senderId = sender.userId " +
                "JOIN `User` receiver ON m.receiverId = receiver.userId " +
                "WHERE m.messageId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, messageId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMessage(rs);
                }
            }
        }

        return null;
    }

    /**
     * Helper method to map ResultSet to Message object
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("messageId"));
        message.setSenderId(rs.getInt("senderId"));
        message.setReceiverId(rs.getInt("receiverId"));
        message.setMessageSubject(rs.getString("messageSubject"));
        message.setMessageContent(rs.getString("messageContent"));

        Timestamp sentTime = rs.getTimestamp("messageSentDateTime");
        if (sentTime != null) {
            message.setMessageSentDateTime(sentTime.toLocalDateTime());
        }

        message.setMessageIsRead(rs.getBoolean("messageIsRead"));
        message.setSenderDisplay(rs.getString("senderDisplay"));
        message.setReceiverDisplay(rs.getString("receiverDisplay"));

        return message;
    }
}

