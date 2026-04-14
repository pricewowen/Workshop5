package com.sait.workshop05.api;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thin wrappers around the Peelin Good REST API for catalog reads.
 * Uses {@link ApiClient} with JWT when set (public GETs work without a token).
 */
public final class CatalogApi {

    private CatalogApi() {}

    public static List<ProductResponse> fetchProducts(String search, Integer tagId) throws Exception {
        StringBuilder path = new StringBuilder("/api/v1/products");
        boolean q = false;
        if (search != null && !search.isBlank()) {
            path.append(q ? "&" : "?");
            path.append("search=").append(java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8));
            q = true;
        }
        if (tagId != null) {
            path.append(q ? "&" : "?");
            path.append("tagId=").append(tagId);
        }
        HttpResponse<String> res = ApiClient.getInstance().get(path.toString());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET /api/v1/products failed: " + res.statusCode() + " " + res.body());
        }
        return readList(ApiClient.getInstance().getMapper(), res.body(), ProductResponse.class);
    }

    public static ProductResponse fetchProduct(int id) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/products/" + id);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET /api/v1/products/" + id + " failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ProductResponse.class);
    }

    public static List<BakeryResponse> fetchBakeries(String search) throws Exception {
        String path = "/api/v1/bakeries";
        if (search != null && !search.isBlank()) {
            path += "?search=" + java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8);
        }
        HttpResponse<String> res = ApiClient.getInstance().get(path);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET /api/v1/bakeries failed: " + res.statusCode() + " " + res.body());
        }
        return readList(ApiClient.getInstance().getMapper(), res.body(), BakeryResponse.class);
    }

    public static List<TagResponse> fetchTags() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/tags");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET /api/v1/tags failed: " + res.statusCode() + " " + res.body());
        }
        return readList(ApiClient.getInstance().getMapper(), res.body(), TagResponse.class);
    }

    public static ProductResponse createProduct(String name, String description, double basePrice,
                                                List<Integer> tagIds, String imageUrl) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("description", description != null ? description : "");
        body.put("basePrice", BigDecimal.valueOf(basePrice));
        body.put("imageUrl", imageUrl != null && !imageUrl.isBlank() ? imageUrl : "");
        body.put("tagIds", tagIds != null ? tagIds : new ArrayList<>());
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/products", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST /api/v1/products failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ProductResponse.class);
    }

    public static ProductResponse updateProduct(int id, String name, String description, double basePrice,
                                                List<Integer> tagIds, String imageUrl) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("description", description != null ? description : "");
        body.put("basePrice", BigDecimal.valueOf(basePrice));
        body.put("imageUrl", imageUrl != null && !imageUrl.isBlank() ? imageUrl : "");
        body.put("tagIds", tagIds != null ? tagIds : new ArrayList<>());
        HttpResponse<String> res = ApiClient.getInstance().put("/api/v1/products/" + id, body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("PUT /api/v1/products/" + id + " failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ProductResponse.class);
    }

    public static void deleteProduct(int id) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().delete("/api/v1/products/" + id);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("DELETE /api/v1/products/" + id + " failed: " + res.statusCode() + " " + res.body());
        }
    }

    /**
     * Returns all rows from the product specials API ({@code GET /api/v1/product-specials/all}).
     * Each entry contains full product details plus the date it is featured and its discount
     * percentage. The internal primary key is present in the raw response but is not surfaced
     * in the desktop UI.
     */
    public static List<ProductSpecialResponse> fetchProductSpecials() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/product-specials/all");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("GET /api/v1/product-specials/all failed: "
                    + res.statusCode() + " " + res.body());
        }
        return readList(ApiClient.getInstance().getMapper(), res.body(), ProductSpecialResponse.class);
    }

    /**
     * Creates a new product special ({@code POST /api/v1/product-specials}).
     * Returns the created entry with its server-assigned primary key.
     */
    public static ProductSpecialResponse createProductSpecial(int productId, String featuredOn,
                                                              double discountPercent) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("productId", productId);
        body.put("featuredOn", featuredOn);
        body.put("discountPercent", java.math.BigDecimal.valueOf(discountPercent));
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/product-specials", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST /api/v1/product-specials failed: "
                    + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ProductSpecialResponse.class);
    }

    /**
     * Replaces all mutable fields on an existing product special
     * ({@code PUT /api/v1/product-specials/{id}}).
     */
    public static ProductSpecialResponse updateProductSpecial(int productSpecialId, int productId,
                                                              String featuredOn,
                                                              double discountPercent) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("productId", productId);
        body.put("featuredOn", featuredOn);
        body.put("discountPercent", java.math.BigDecimal.valueOf(discountPercent));
        HttpResponse<String> res = ApiClient.getInstance().put(
                "/api/v1/product-specials/" + productSpecialId, body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("PUT /api/v1/product-specials/" + productSpecialId
                    + " failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper().readValue(res.body(), ProductSpecialResponse.class);
    }

    /**
     * Permanently removes a product special ({@code DELETE /api/v1/product-specials/{id}}).
     */
    public static void deleteProductSpecial(int productSpecialId) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().delete(
                "/api/v1/product-specials/" + productSpecialId);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("DELETE /api/v1/product-specials/" + productSpecialId
                    + " failed: " + res.statusCode() + " " + res.body());
        }
    }

    /** tag name → id for admin product form */
    public static Map<String, Integer> tagNameToIdMap() throws Exception {
        List<TagResponse> tags = fetchTags();
        return tags.stream().collect(Collectors.toMap(t -> t.name, t -> t.id, (a, b) -> a));
    }

    private static <T> List<T> readList(ObjectMapper mapper, String json, Class<T> clazz) throws Exception {
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return mapper.readValue(json, type);
    }

    /** JSON shape for GET /api/v1/products */
    public static class ProductResponse {
        public Integer id;
        public String name;
        public String description;
        public java.math.BigDecimal basePrice;
        public String imageUrl;
        public List<Integer> tagIds;
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class BakeryResponse {
        public Integer id;
        public String name;
        public String phone;
        public String email;
        public String status;
        public java.math.BigDecimal latitude;
        public java.math.BigDecimal longitude;
        public AddressResponse address;
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressResponse {
        public Integer id;
        public String line1;
        public String line2;
        public String city;
        public String province;
        public String postalCode;
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class TagResponse {
        public Integer id;
        public String name;
    }

    /** JSON shape for GET /api/v1/product-specials/all. */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductSpecialResponse {
        public Integer productSpecialId;   // present in JSON but not shown in the UI
        public String featuredOn;
        public java.math.BigDecimal discountPercent;
        public Integer productId;
        public String productName;
        public String productDescription;
        public java.math.BigDecimal productBasePrice;
        public String productImageUrl;
    }
}
