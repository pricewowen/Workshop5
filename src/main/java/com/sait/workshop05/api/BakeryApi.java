package com.sait.workshop05.api;

import com.sait.workshop05.models.Address;
import com.sait.workshop05.models.Bakery;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin bakery CRUD via {@code /api/v1/bakeries}.
 */
public final class BakeryApi {

    private BakeryApi() {}

    public static List<Bakery> listAll() throws Exception {
        List<CatalogApi.BakeryResponse> rows = CatalogApi.fetchBakeries(null);
        return rows.stream().map(BakeryApi::toBakery).collect(Collectors.toList());
    }

    public static Bakery toBakery(CatalogApi.BakeryResponse b) {
        Address addr;
        if (b.address != null) {
            addr = new Address(
                    b.address.id != null ? b.address.id : 0,
                    nz(b.address.line1),
                    nz(b.address.line2),
                    nz(b.address.city),
                    nz(b.address.province),
                    nz(b.address.postalCode)
            );
        } else {
            addr = new Address(0, "", "", "", "", "");
        }
        Bakery bakery = new Bakery(b.id != null ? b.id : 0, addr, nz(b.name), nz(b.phone), nz(b.email));
        return bakery;
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    public static void create(Bakery bakery) throws Exception {
        Map<String, Object> body = upsertBody(bakery);
        HttpResponse<String> res = ApiClient.getInstance().postAuthenticated("/api/v1/bakeries", body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("POST bakeries failed: " + res.statusCode() + " " + res.body());
        }
    }

    public static void update(int id, Bakery bakery) throws Exception {
        Map<String, Object> body = upsertBody(bakery);
        HttpResponse<String> res = ApiClient.getInstance().put("/api/v1/bakeries/" + id, body);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("PUT bakeries failed: " + res.statusCode() + " " + res.body());
        }
    }

    public static void delete(int id) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance().delete("/api/v1/bakeries/" + id);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("DELETE bakeries failed: " + res.statusCode() + " " + res.body());
        }
    }

    private static Map<String, Object> upsertBody(Bakery bakery) {
        Address a = bakery.getAddress();
        Map<String, Object> addr = new LinkedHashMap<>();
        addr.put("line1", a != null ? a.getAddressLine1() : "");
        addr.put("line2", a != null && a.getAddressLine2() != null ? a.getAddressLine2() : "");
        addr.put("city", a != null ? a.getAddressCity() : "");
        addr.put("province", a != null ? a.getAddressProvince() : "");
        addr.put("postalCode", a != null ? a.getAddressPostalCode() : "");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", bakery.getBakeryName());
        body.put("phone", bakery.getBakeryPhone());
        body.put("email", bakery.getBakeryEmail());
        body.put("status", "open");
        body.put("address", addr);
        return body;
    }
}
