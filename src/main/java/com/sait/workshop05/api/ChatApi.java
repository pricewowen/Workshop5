package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChatApi {

    private ChatApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadJson {
        public Integer id;
        public String customerUserId;
        public String customerDisplayName;
        public String customerUsername;
        public String customerEmail;
        public String customerProfilePhotoPath;
        public String employeeUserId;
        public String status;
        public String category;
        public String createdAt;
        public String updatedAt;
        public String closedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageJson {
        public Integer id;
        public Integer threadId;
        public String senderUserId;
        public String text;
        public String sentAt;
        public boolean read;
        public boolean isSystem;
        public boolean staffOnly;
    }

    public static List<ThreadJson> openThreads(String category) throws Exception {
        String path = "/api/v1/chat/threads";
        if (category != null && !category.isBlank()) {
            path += "?category=" + URLEncoder.encode(category, StandardCharsets.UTF_8);
        }
        HttpResponse<String> res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET chat threads failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<ThreadJson>>() {});
    }

    public static List<ThreadJson> archivedThreads(String category) throws Exception {
        String path = "/api/v1/chat/threads/archived";
        if (category != null && !category.isBlank()) {
            path += "?category=" + URLEncoder.encode(category, StandardCharsets.UTF_8);
        }
        HttpResponse<String> res = ApiClient.getInstance().get(path);
        if (res.statusCode() == 403) {
            return Collections.emptyList();
        }
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET archived threads failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<ThreadJson>>() {});
    }

    public static List<MessageJson> messages(int threadId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/chat/threads/" + threadId + "/messages");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET chat messages failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<MessageJson>>() {});
    }

    public static MessageJson postMessage(int threadId, String text) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated(
                "/api/v1/chat/threads/" + threadId + "/messages", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST chat message failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), MessageJson.class);
    }

    public static ThreadJson assign(int threadId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated(
                "/api/v1/chat/threads/" + threadId + "/assign", Collections.emptyMap());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST assign failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ThreadJson.class);
    }

    public static ThreadJson close(int threadId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated(
                "/api/v1/chat/threads/" + threadId + "/close", Collections.emptyMap());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST close failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ThreadJson.class);
    }

    public static void markRead(int threadId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated(
                "/api/v1/chat/threads/" + threadId + "/read", Collections.emptyMap());
        if (res.statusCode() >= 400 && res.statusCode() != 204) {
            throw new RuntimeException("POST read failed: " + res.statusCode() + " " + res.body());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StaffJson {
        public String userId;
        public String username;
        public String role;

        @Override
        public String toString() {
            String roleLabel = role != null && !role.isBlank()
                    ? " (" + role.toLowerCase() + ")"
                    : "";
            return (username != null ? username : "unknown") + roleLabel;
        }
    }

    public static List<StaffJson> staffRecipients() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/messages/recipients");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET staff recipients failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), new TypeReference<List<StaffJson>>() {});
    }

    public static ThreadJson transfer(int threadId, String employeeUserId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("employeeUserId", employeeUserId);
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated(
                "/api/v1/chat/threads/" + threadId + "/transfer", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST transfer failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ThreadJson.class);
    }

    public static ThreadJson reopen(int threadId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated(
                "/api/v1/chat/threads/" + threadId + "/reopen", Collections.emptyMap());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST reopen failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ThreadJson.class);
    }
}
