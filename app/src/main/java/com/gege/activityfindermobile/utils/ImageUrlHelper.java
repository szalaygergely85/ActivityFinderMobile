package com.gege.activityfindermobile.utils;

/**
 * Helper class to handle image URLs from backend. Backend returns relative paths, we need to
 * convert them to absolute URLs for Glide.
 */
public class ImageUrlHelper {
    // Base API URL - matches NetworkModule configuration
    private static final String BASE_URL = "http://10.0.2.2:8080";

    /**
     * Convert relative image path to absolute URL for loading with Glide. If the URL is already
     * absolute, return as-is. If it's a relative path, prepend the base URL.
     *
     * @param imageUrl The image URL/path from backend
     * @return The absolute URL for Glide to load
     */
    public static String getAbsoluteImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        // If already an absolute URL, return as-is
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        // If it's a relative path, prepend base URL
        if (imageUrl.startsWith("/")) {
            return BASE_URL + imageUrl;
        }

        // Otherwise assume it needs the base URL
        return BASE_URL + "/" + imageUrl;
    }
}
