package com.sait.workshop05.api;

import java.io.File;
import java.net.http.HttpResponse;

/**
 * Uploads product images to DigitalOcean Spaces via the admin API.
 * Images land in the {@code products/} folder and are publicly accessible at
 * {@code https://peelin-good-storage.tor1.digitaloceanspaces.com}.
 * The API URL is read from {@code .env.local} by {@link ApiClient}.
 */
public final class ImageUploadApi {

    private ImageUploadApi() {}

    /**
     * Uploads an image for a product to the {@code products/} folder.
     * The backend validates the file, stores it in DigitalOcean Spaces, and
     * persists the resulting public URL on the product record.
     *
     * @param productId numeric product ID
     * @param image     JPG or PNG file (max 5 MB)
     * @throws Exception if the upload fails or the server returns an error status
     */
    public static void uploadProductImage(int productId, File image) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance()
                .postMultipart("/api/v1/products/" + productId + "/image", "image", image);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Upload product image failed: " + res.statusCode() + " " + res.body());
        }
    }

    public static void uploadBakeryImage(int bakeryId, File image) throws Exception {
        HttpResponse<String> res = ApiClient.getInstance()
                .postMultipart("/api/v1/bakeries/" + bakeryId + "/image", "image", image);
        if (res.statusCode() >= 400) {
            throw new RuntimeException("Upload bakery image failed: " + res.statusCode() + " " + res.body());
        }
    }
}
