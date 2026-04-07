package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.models.BakeryOption;
import com.sait.workshop05.models.CustomerOption;
import com.sait.workshop05.models.Product;
import com.sait.workshop05.models.UserOption;

/**
 * Loads reference data for admin screens (customers, bakeries, addresses, catalog).
 */
public final class ReferenceApi {

    private ReferenceApi() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserSummaryJson {
        public String id;
        public String username;
        public String email;
    }

    public static List<UserOption> loadAdminUsers() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/users");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("admin users failed: " + res.statusCode() + " " + res.body());
        }
        List<UserSummaryJson> rows = ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<UserSummaryJson>>() {});
        List<UserOption> out = new ArrayList<>();
        for (UserSummaryJson u : rows) {
            if (u.id != null && u.username != null) {
                out.add(new UserOption(u.id, u.username));
            }
        }
        return out;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerJson {
        public String id;
        public String firstName;
        public String lastName;
        public int rewardBalance;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressJson {
        public int id;
        public String line1;
        public String line2;
        public String city;
        public String province;
        public String postalCode;
    }

    public static List<CustomerOption> loadCustomers() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/customers");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("customers failed: " + res.statusCode() + " " + res.body());
        }
        List<CustomerJson> rows = ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<CustomerJson>>() {});
        List<CustomerOption> out = new ArrayList<>();
        for (CustomerJson c : rows) {
            String name = ((c.firstName != null ? c.firstName : "") + " " + (c.lastName != null ? c.lastName : "")).trim();
            out.add(new CustomerOption(c.id, name.isEmpty() ? "(customer)" : name, c.rewardBalance));
        }
        return out;
    }

    public static List<BakeryOption> loadBakeries() throws Exception {
        List<CatalogApi.BakeryResponse> rows = CatalogApi.fetchBakeries(null);
        List<BakeryOption> out = new ArrayList<>();
        for (CatalogApi.BakeryResponse b : rows) {
            if (b.id != null && b.name != null) {
                out.add(new BakeryOption(b.id, b.name));
            }
        }
        return out;
    }

    public static List<AddressOption> loadAddresses() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/addresses");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("addresses failed: " + res.statusCode() + " " + res.body());
        }
        List<AddressJson> rows = ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<AddressJson>>() {});
        List<AddressOption> out = new ArrayList<>();
        for (AddressJson a : rows) {
            out.add(new AddressOption(a.id, a.line1, a.city, a.province, a.postalCode));
        }
        return out;
    }

    public static List<Product> loadProducts() throws Exception {
        List<CatalogApi.ProductResponse> rows = CatalogApi.fetchProducts(null, null);
        List<Product> out = new ArrayList<>();
        for (CatalogApi.ProductResponse p : rows) {
            if (p.id == null) continue;
            Product pr = new Product();
            pr.setProductId(p.id);
            pr.setProductName(p.name != null ? p.name : "");
            pr.setProductDescription(p.description != null ? p.description : "");
            pr.setProductBasePrice(p.basePrice != null ? p.basePrice.doubleValue() : 0);
            out.add(pr);
        }
        return out;
    }

    /**
     * Creates an address via the API from free-form text (line1 holds full text; minimal placeholders elsewhere).
     */
    public static AddressOption createAddressFromTyped(String typed) throws Exception {
        String t = typed.trim();
        Map<String, String> body = Map.of(
                "line1", t,
                "line2", "",
                "city", "Unknown",
                "province", "Unknown",
                "postalCode", "X0X0X0"
        );
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/admin/addresses", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("create address failed: " + res.statusCode() + " " + res.body());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = ApiClient.getInstance().getMapper().readValue(res.body(), Map.class);
        int id = ((Number) map.get("id")).intValue();
        return new AddressOption(id, t, "Unknown", "Unknown", "X0X0X0");
    }
}
