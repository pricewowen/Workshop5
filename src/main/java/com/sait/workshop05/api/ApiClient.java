// Contributor(s): Robbie
// Main: Robbie - Shared HTTP client for Workshop 7 with JWT bearer on protected routes.

package com.sait.workshop05.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

/**
 * Singleton HTTP client for the Workshop 7 Spring Boot API (springdoc OpenAPI on the server).
 * Base URL comes from env file JVM property or built-in default.
 * Sends JSON and optional {@code Authorization: Bearer} token on protected routes.
 */
public class ApiClient {

    private static ApiClient instance;

    // Switch this base URL to the deployed API host when desktop testing needs production data.
    private static final String API_BASE_URL = "http://localhost:8080";

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
        this.baseUrl = resolveBaseUrl().replaceAll("/+$", "");
    }

    // Order is .env.local API_URL then JVM or OS env then the compile-time default.
    private static String resolveBaseUrl() {
        String envPath = System.getProperty("user.dir") + "/.env.local";
        try (BufferedReader reader = new BufferedReader(new FileReader(envPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2 && "API_URL".equals(parts[0].trim())) {
                    String value = parts[1].trim();
                    if (!value.isEmpty()) return value;
                }
            }
        } catch (Exception ignored) {
        }
        String sys = System.getProperty("API_URL", System.getenv("API_URL"));
        if (sys != null && !sys.isEmpty()) return sys;
        return API_BASE_URL;
    }

    /** Returns the shared instance. */
    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    /** Stores JWT for authenticated requests. */
    public void setToken(String token) {
        this.jwtToken = token;
    }

    /** Clears JWT so only public endpoints work until login again. */
    public void clearToken() {
        this.jwtToken = null;
    }

    /** Base URL with no trailing slash. */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * POST JSON without Authorization. Used for login and other public POST routes.
     */
    public HttpResponse<String> post(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        String url = baseUrl + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /** POST JSON with Bearer token. */
    public HttpResponse<String> postAuthenticated(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        String url = baseUrl + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * GET with Accept JSON. Sends Bearer when a token is set so some routes work before login.
     */
    public HttpResponse<String> get(String path) throws Exception {
        String url = baseUrl + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        try {
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /** PUT JSON with Bearer token. */
    public HttpResponse<String> put(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        String url = baseUrl + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /** DELETE with Bearer token. */
    public HttpResponse<String> delete(String path) throws Exception {
        String url = baseUrl + path;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /** PATCH JSON with Bearer token. */
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
     * POST multipart form data with one file part and Bearer auth.
     * Field name must match what the server expects for that path.
     */
    public HttpResponse<String> postMultipart(String path, String fieldName, File file) throws Exception {
        String boundary = "----JavaFXBoundary" + Long.toHexString(System.currentTimeMillis());
        String contentType = probeContentType(file.getName());

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Manual multipart so java.net.http can send bytes without extra deps.
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


    /** Shared Jackson mapper for DTO parsing. */
    public ObjectMapper getMapper() {
        return mapper;
    }
}
