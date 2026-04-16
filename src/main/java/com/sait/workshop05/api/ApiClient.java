package com.sait.workshop05.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

/**
 * Singleton HTTP client for the Spring Boot API.
 * Uses the deployed API URL and attaches the JWT on authenticated requests.
 */
public class ApiClient {

    private static ApiClient instance;
    private static final String DEPLOYED_API_BASE_URL = "https://peelin-good-kdeft.ondigitalocean.app";

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private String jwtToken;

    private ApiClient() {
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
        this.baseUrl = DEPLOYED_API_BASE_URL.replaceAll("/+$", "");
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public void setToken(String token) {
        this.jwtToken = token;
    }

    public void clearToken() {
        this.jwtToken = null;
    }

    /** Base URL for the deployed API (no trailing slash). */
    public String getBaseUrl() {
        return baseUrl;
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

    /**
     * PATCH with JWT authorization header.
     */
    public HttpResponse<String> patch(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();

        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * POST a single file as multipart/form-data with JWT authorization header.
     *
     * @param path      API path (e.g. {@code /api/v1/products/1/image})
     * @param fieldName multipart field name expected by the server
     * @param file      file to upload (JPG or PNG)
     * @return raw HTTP response
     */
    public HttpResponse<String> postMultipart(String path, String fieldName, File file) throws Exception {
        String boundary = "----JavaFXBoundary" + Long.toHexString(System.currentTimeMillis());
        String contentType = probeContentType(file.getName());

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Build the multipart body: opening part header, file bytes, and closing boundary
        String partHeader = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + file.getName() + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n";
        String closing = "\r\n--" + boundary + "--\r\n";

        byte[] headerBytes = partHeader.getBytes(StandardCharsets.UTF_8);
        byte[] closingBytes = closing.getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[headerBytes.length + fileBytes.length + closingBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(closingBytes, 0, body, headerBytes.length + fileBytes.length, closingBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String probeContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }


    public ObjectMapper getMapper() {
        return mapper;
    }
}
