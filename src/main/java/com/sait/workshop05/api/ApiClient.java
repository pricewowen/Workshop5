package com.sait.workshop05.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton HTTP client for the Spring Boot API.
 * Reads API_URL from .env.local and attaches the JWT on authenticated requests.
 */
public class ApiClient {

    private static ApiClient instance;

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private String jwtToken;

    private ApiClient() {
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.baseUrl = loadBaseUrl();
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    private String loadBaseUrl() {
        String envPath = System.getProperty("user.dir") + "/.env.local";
        Map<String, String> env = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(envPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) env.put(parts[0].trim(), parts[1].trim());
            }
        } catch (Exception e) {
            System.err.println("ApiClient: could not read .env.local — " + e.getMessage());
        }

        String url = env.get("API_URL");
        if (url == null || url.isBlank()) {
            throw new RuntimeException("API_URL is missing from .env.local");
        }
        return url.replaceAll("/+$", ""); // strip trailing slash
    }

    public void setToken(String token) {
        this.jwtToken = token;
    }

    public void clearToken() {
        this.jwtToken = null;
    }

    /**
     * POST with a body object serialized to JSON. No auth header (used for login).
     */
    public HttpResponse<String> post(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        String url = baseUrl + path;

        System.out.println("[DEBUG_LOG] API POST Request: " + url);
        // System.out.println("[DEBUG_LOG] Request Body: " + json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[DEBUG_LOG] API POST Response Status: " + response.statusCode());
            if (response.statusCode() >= 400) {
                System.err.println("[DEBUG_LOG] API Error Response Body: " + response.body());
            }
            return response;
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] API Request Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * POST with JWT authorization header.
     */
    public HttpResponse<String> postAuthenticated(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        String url = baseUrl + path;

        System.out.println("[DEBUG_LOG] API POST Authenticated: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[DEBUG_LOG] API POST Authenticated Response Status: " + response.statusCode());
            if (response.statusCode() >= 400) {
                System.err.println("[DEBUG_LOG] API Error Response Body: " + response.body());
            }
            return response;
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] API Request Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * GET with JWT authorization header.
     */
    public HttpResponse<String> get(String path) throws Exception {
        String url = baseUrl + path;
        System.out.println("[DEBUG_LOG] API GET Request: " + url);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        try {
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("[DEBUG_LOG] API GET Response Status: " + response.statusCode());
            if (response.statusCode() >= 400) {
                System.err.println("[DEBUG_LOG] API Error Response Body: " + response.body());
            }
            return response;
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] API Request Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * PUT with JWT authorization header.
     */
    public HttpResponse<String> put(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        String url = baseUrl + path;

        System.out.println("[DEBUG_LOG] API PUT Request: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[DEBUG_LOG] API PUT Response Status: " + response.statusCode());
            if (response.statusCode() >= 400) {
                System.err.println("[DEBUG_LOG] API Error Response Body: " + response.body());
            }
            return response;
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] API Request Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * DELETE with JWT authorization header.
     */
    public HttpResponse<String> delete(String path) throws Exception {
        String url = baseUrl + path;
        System.out.println("[DEBUG_LOG] API DELETE Request: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[DEBUG_LOG] API DELETE Response Status: " + response.statusCode());
            if (response.statusCode() >= 400) {
                System.err.println("[DEBUG_LOG] API Error Response Body: " + response.body());
            }
            return response;
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] API Request Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
