// Contributor(s): Samantha
// Main: Samantha - Multipart image upload for product and bakery photos via the API.

package com.sait.workshop05.api;

import java.io.File;
import java.net.http.HttpResponse;

/**
 * Uploads images through admin routes. The server stores files in object storage and saves public URLs.
 * Base URL for API calls still comes from ApiClient env resolution.
 */
public final class ImageUploadApi {

    private ImageUploadApi() {}

    /**
     * POST multipart image for one product. Server validates size and type then updates the product row.
     *
     * @param productId product primary key
     * @param image JPG or PNG file within server limits
     * @throws Exception when HTTP status is an error
     */
    public static void uploadProductImage(int productId, File image) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance()
                .postMultipart("/api/v1/products/" + productId + "/image", "image", image);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Upload product image failed: " + res.statusCode() + " " + res.body());
        }
    }

    /**
     * Uploads one bakery image through the admin bakery image route.
     */
    public static void uploadBakeryImage(int bakeryId, File image) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance()
                .postMultipart("/api/v1/bakeries/" + bakeryId + "/image", "image", image);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Upload bakery image failed: " + res.statusCode() + " " + res.body());
        }
    }
}
