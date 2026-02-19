package com.gege.activityfindermobile.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gege.activityfindermobile.R;

public class ImageLoader {

    /**
     * Load profile image from URL into ImageView
     *
     * @param context Context
     * @param imageUrl URL or path to the image
     * @param imageView Target ImageView
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Load default placeholder
            imageView.setImageResource(R.drawable.ic_person);
            return;
        }

        // Build full URL if it's a relative path
        String fullUrl = imageUrl;
        if (!imageUrl.startsWith("http")) {
            // Assuming relative paths from backend, prepend base URL
            String baseUrl = Constants.BASE_URL;
            // Remove trailing slash from base URL if present
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            // Handle leading slash in image URL
            if (!imageUrl.startsWith("/")) {
                fullUrl = baseUrl + "/" + imageUrl;
            } else {
                fullUrl = baseUrl + imageUrl;
            }
        }

        RequestOptions options =
                new RequestOptions()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop();

        // Create GlideUrl with Authorization header
        GlideUrl glideUrl = buildGlideUrlWithAuth(context, fullUrl);

        Glide.with(context).load(glideUrl).apply(options).into(imageView);
    }

    /**
     * Load circular profile image (for profile pictures)
     *
     * @param context Context
     * @param imageUrl URL or path to the image
     * @param imageView Target ImageView (should be CircleImageView)
     */
    public static void loadCircularProfileImage(
            Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_person);
            return;
        }

        String fullUrl = imageUrl;
        if (!imageUrl.startsWith("http")) {
            String baseUrl = Constants.BASE_URL;
            // Remove trailing slash from base URL if present
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            // Handle leading slash in image URL
            if (!imageUrl.startsWith("/")) {
                fullUrl = baseUrl + "/" + imageUrl;
            } else {
                fullUrl = baseUrl + imageUrl;
            }
        }

        RequestOptions options =
                new RequestOptions()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop();

        // Create GlideUrl with Authorization header
        GlideUrl glideUrl = buildGlideUrlWithAuth(context, fullUrl);

        Glide.with(context).load(glideUrl).apply(options).into(imageView);
    }

    /**
     * Build GlideUrl with Authorization header
     *
     * @param context Context
     * @param url Image URL
     * @return GlideUrl with auth headers
     */
    private static GlideUrl buildGlideUrlWithAuth(Context context, String url) {
        SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);
        String token = prefsManager.getUserToken();

        if (token != null && !token.isEmpty()) {
            return new GlideUrl(
                    url,
                    new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build());
        } else {
            // No token available, return plain GlideUrl
            return new GlideUrl(url);
        }
    }

    /**
     * Load an activity cover image into an ImageView.
     * Cover images are public (no auth header needed) and cached to disk.
     *
     * @param context  Context
     * @param imageUrl Full or relative URL of the cover image
     * @param imageView Target ImageView
     */
    public static void loadCoverImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.activity_default);
            return;
        }

        String fullUrl = imageUrl;
        if (!imageUrl.startsWith("http")) {
            String baseUrl = Constants.BASE_URL;
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            fullUrl = baseUrl + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
        }

        Glide.with(context)
                .load(fullUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.activity_default)
                .error(R.drawable.activity_default)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(imageView);
    }

    /**
     * Load gallery photo from URL into ImageView with authorization
     *
     * @param context Context
     * @param imageUrl URL or path to the image
     * @param imageView Target ImageView
     */
    public static void loadGalleryPhoto(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        // Build full URL if it's a relative path
        String fullUrl = imageUrl;
        if (!imageUrl.startsWith("http")) {
            String baseUrl = Constants.BASE_URL;
            // Remove trailing slash from base URL if present
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            // Handle leading slash in image URL
            if (!imageUrl.startsWith("/")) {
                fullUrl = baseUrl + "/" + imageUrl;
            } else {
                fullUrl = baseUrl + imageUrl;
            }
        }

        RequestOptions options =
                new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop();

        // Create GlideUrl with Authorization header
        GlideUrl glideUrl = buildGlideUrlWithAuth(context, fullUrl);

        Glide.with(context).load(glideUrl).apply(options).into(imageView);
    }

    /**
     * Clear Glide cache
     *
     * @param context Context
     */
    public static void clearCache(Context context) {
        Glide.get(context).clearMemory();
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
    }
}
