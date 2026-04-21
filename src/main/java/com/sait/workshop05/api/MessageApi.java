// Contributor(s): Robbie
// Main: Robbie - Legacy staff messaging REST helpers for /api/v1/messages.

package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sait.workshop05.models.Message;

/**
 * Staff messaging via /api/v1/messages. Prefer ChatApi for the new support chat flow.
 */
public final class MessageApi {

    private MessageApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegacyMessageJson {
        public String id;
        public String senderId;
        public String receiverId;
        public String subject;
        public String content;
        public String sentAt;
        public boolean read;
    }

    /**
     * Returns inbox rows for the authenticated staff member.
     */
    public static List<LegacyMessageJson> myMessages() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/messages");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET messages failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<LegacyMessageJson>>() {});
    }

    /**
     * Returns a conversation thread with one partner id.
     */
    public static List<LegacyMessageJson> conversation(String otherUserId) throws Exception {
        String path = "/api/v1/messages/with/" + otherUserId;
        HttpResponse<String> res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET conversation failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<LegacyMessageJson>>() {});
    }

    /**
     * Sends one legacy message and returns the created payload.
     */
    public static LegacyMessageJson send(String receiverId, String subject, String content) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("receiverId", receiverId);
        body.put("subject", subject);
        body.put("content", content);
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/messages", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST messages failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), LegacyMessageJson.class);
    }

    /**
     * Marks one legacy message row as read.
     */
    public static void markRead(String messageId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().patch("/api/v1/messages/" + messageId + "/read", Map.of());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("PATCH message read failed: " + res.statusCode() + " " + res.body());
        }
    }

    /**
     * Maps legacy message JSON into the desktop Message model.
     */
    public static Message toModel(LegacyMessageJson j) {
        Message m = new Message();
        m.setMessageId(j.id != null ? j.id : "");
        m.setSenderId(j.senderId != null ? j.senderId : "");
        m.setReceiverId(j.receiverId != null ? j.receiverId : "");
        m.setMessageSubject(j.subject);
        m.setMessageContent(j.content != null ? j.content : "");
        m.setMessageSentDateTime(parseSent(j.sentAt));
        m.setMessageIsRead(j.read);
        return m;
    }

    private static LocalDateTime parseSent(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return OffsetDateTime.parse(s).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(s);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
