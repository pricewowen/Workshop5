package com.sait.workshop05.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /** Short-lived cache: order enrichment and combo boxes hit this often in one session. */
    private static final long CUSTOMER_CACHE_TTL_MS = 25_000L;
    private static volatile List<CustomerOption> customerCacheSnapshot;
    private static volatile long customerCacheExpiresAtEpochMs;

    private static final long ADDRESS_CACHE_TTL_MS = 60_000L;
    private static volatile List<AddressOption> addressCacheSnapshot;
    private static volatile long addressCacheExpiresAtEpochMs;

    /**
     * Clears the in-memory customer list cache. Call after customer create/update so other screens
     * see fresh names and balances on their next load.
     */
    public static void invalidateCustomersCache() {
        customerCacheSnapshot = null;
        customerCacheExpiresAtEpochMs = 0L;
    }

    /** Clears cached address rows (e.g. after creating an address). */
    public static void invalidateAddressesCache() {
        addressCacheSnapshot = null;
        addressCacheExpiresAtEpochMs = 0L;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserSummaryJson {
        public String id;
        public String username;
        public String email;
        /** API role: {@code admin}, {@code employee}, or {@code customer}. */
        public String role;
    }

    public static List<UserOption> loadAdminUsers() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/users/staff");
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

    /**
     * All admin-visible users (admin sees staff + customers; employees see customers only).
     */
    public static List<UserSummaryJson> fetchAdminUserSummaries() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/users");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("admin users list failed: " + res.statusCode() + " " + res.body());
        }
        return ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<UserSummaryJson>>() {});
    }

    /**
     * User ids that already have an employee or customer profile (two cheap indexed queries on the server,
     * not a full customer list).
     */
    private static Set<String> fetchProfileLinkedUserIds() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/users/profile-linked-ids");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("profile-linked-ids failed: " + res.statusCode() + " " + res.body());
        }
        List<String> ids = ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<String>>() {});
        return new HashSet<>(ids);
    }

    /**
     * Staff logins ({@code admin} or {@code employee}) not already tied to an employee or customer profile.
     */
    public static List<UserOption> loadUnlinkedStaffUsersForNewEmployee() throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().get("/api/v1/admin/users/staff");
        if (res.statusCode() >= 400) {
            throw new RuntimeException("admin users/staff failed: " + res.statusCode() + " " + res.body());
        }
        List<UserSummaryJson> rows = ApiClient.getInstance().getMapper()
                .readValue(res.body(), new TypeReference<List<UserSummaryJson>>() {});
        Set<String> taken = fetchProfileLinkedUserIds();
        List<UserOption> out = new ArrayList<>();
        for (UserSummaryJson u : rows) {
            if (u.id == null || u.username == null) {
                continue;
            }
            if (u.role == null
                    || (!"employee".equalsIgnoreCase(u.role) && !"admin".equalsIgnoreCase(u.role))) {
                continue;
            }
            if (taken.contains(u.id)) {
                continue;
            }
            out.add(new UserOption(u.id, u.username));
        }
        return out;
    }

    /**
     * {@code customer}-role logins that are not linked to any employee or customer profile yet.
     */
    public static List<UserOption> loadUnlinkedCustomerRoleUsersForNewCustomer() throws Exception {
        Set<String> taken = fetchProfileLinkedUserIds();
        List<UserOption> out = new ArrayList<>();
        for (UserSummaryJson u : fetchAdminUserSummaries()) {
            if (u.id == null || u.username == null) {
                continue;
            }
            if (u.role == null || !"customer".equalsIgnoreCase(u.role)) {
                continue;
            }
            if (taken.contains(u.id)) {
                continue;
            }
            out.add(new UserOption(u.id, u.username));
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
        long now = System.currentTimeMillis();
        List<CustomerOption> snap = customerCacheSnapshot;
        long exp = customerCacheExpiresAtEpochMs;
        if (snap != null && now < exp) {
            return new ArrayList<>(snap);
        }
        synchronized (ReferenceApi.class) {
            now = System.currentTimeMillis();
            snap = customerCacheSnapshot;
            exp = customerCacheExpiresAtEpochMs;
            if (snap != null && now < exp) {
                return new ArrayList<>(snap);
            }
            List<CustomerOption> fresh = loadCustomersFromNetwork();
            customerCacheSnapshot = List.copyOf(fresh);
            customerCacheExpiresAtEpochMs = now + CUSTOMER_CACHE_TTL_MS;
            return new ArrayList<>(fresh);
        }
    }

    private static List<CustomerOption> loadCustomersFromNetwork() throws Exception {
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
        long now = System.currentTimeMillis();
        List<AddressOption> snap = addressCacheSnapshot;
        long exp = addressCacheExpiresAtEpochMs;
        if (snap != null && now < exp) {
            return new ArrayList<>(snap);
        }
        synchronized (ReferenceApi.class) {
            now = System.currentTimeMillis();
            snap = addressCacheSnapshot;
            exp = addressCacheExpiresAtEpochMs;
            if (snap != null && now < exp) {
                return new ArrayList<>(snap);
            }
            List<AddressOption> fresh = loadAddressesFromNetwork();
            addressCacheSnapshot = List.copyOf(fresh);
            addressCacheExpiresAtEpochMs = now + ADDRESS_CACHE_TTL_MS;
            return new ArrayList<>(fresh);
        }
    }

    private static List<AddressOption> loadAddressesFromNetwork() throws Exception {
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
                "province", "AB",
                "postalCode", "X0X0X0"
        );
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/admin/addresses", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("create address failed: " + res.statusCode() + " " + res.body());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = ApiClient.getInstance().getMapper().readValue(res.body(), Map.class);
        int id = ((Number) map.get("id")).intValue();
        invalidateAddressesCache();
        return new AddressOption(id, t, "Unknown", "AB", "X0X0X0");
    }
}
