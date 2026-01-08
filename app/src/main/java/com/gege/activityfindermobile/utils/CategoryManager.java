package com.gege.activityfindermobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gege.activityfindermobile.data.api.CategoryApiService;
import com.gege.activityfindermobile.data.model.Category;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class CategoryManager {

    private static final String TAG = "CategoryManager";
    private static final String PREFS_NAME = "category_cache";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_LAST_UPDATED = "last_updated";
    private static final long CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000; // 24 hours

    private final CategoryApiService categoryApiService;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private Map<String, String> categoryImageMap; // category name -> image resource name

    @Inject
    public CategoryManager(
            @dagger.hilt.android.qualifiers.ApplicationContext Context context,
            CategoryApiService categoryApiService,
            Gson gson) {
        this.categoryApiService = categoryApiService;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = gson;
        this.categoryImageMap = new HashMap<>();
        loadCategoriesFromCache();
    }

    /** Fetch categories from backend and cache them locally */
    public void refreshCategories() {
        categoryApiService
                .getAllCategories()
                .enqueue(
                        new Callback<List<Category>>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<Category>> call,
                                    @NonNull Response<List<Category>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Category> categories = response.body();
                                    cacheCategories(categories);
                                    updateCategoryImageMap(categories);
                                    Log.d(
                                            TAG,
                                            "Categories refreshed successfully: "
                                                    + categories.size());
                                } else {
                                    Log.e(TAG, "Failed to refresh categories: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<List<Category>> call, @NonNull Throwable t) {
                                Log.e(TAG, "Error refreshing categories", t);
                            }
                        });
    }

    /**
     * Get image resource name for a category
     *
     * @param categoryName The category name (e.g., "Sports", "Music")
     * @return The image resource name (e.g., "activity_sports") or "activity_default" if not found
     */
    public String getImageResourceName(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return "activity_default";
        }

        // Check cache first
        String imageResourceName = categoryImageMap.get(categoryName.toLowerCase());

        // If not found, try to refresh cache (async) and return default for now
        if (imageResourceName == null) {
            if (shouldRefreshCache()) {
                refreshCategories();
            }
            // Fallback to convention-based naming
            return "activity_" + categoryName.toLowerCase().replaceAll("\\s+", "_");
        }

        return imageResourceName;
    }

    /** Get all cached categories */
    public List<Category> getCachedCategories() {
        String json = sharedPreferences.getString(KEY_CATEGORIES, null);
        if (json != null) {
            Type listType = new TypeToken<List<Category>>() {}.getType();
            return gson.fromJson(json, listType);
        }
        return new ArrayList<>();
    }

    /** Check if cache needs refresh */
    private boolean shouldRefreshCache() {
        long lastUpdated = sharedPreferences.getLong(KEY_LAST_UPDATED, 0);
        return System.currentTimeMillis() - lastUpdated > CACHE_VALIDITY_MS;
    }

    /** Load categories from SharedPreferences cache */
    private void loadCategoriesFromCache() {
        List<Category> categories = getCachedCategories();
        updateCategoryImageMap(categories);

        // Refresh if cache is stale
        if (shouldRefreshCache()) {
            refreshCategories();
        }
    }

    /** Cache categories to SharedPreferences */
    private void cacheCategories(List<Category> categories) {
        String json = gson.toJson(categories);
        sharedPreferences
                .edit()
                .putString(KEY_CATEGORIES, json)
                .putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                .apply();
    }

    /** Update the in-memory category image map */
    private void updateCategoryImageMap(List<Category> categories) {
        categoryImageMap.clear();
        for (Category category : categories) {
            if (category.getName() != null && category.getImageResourceName() != null) {
                categoryImageMap.put(
                        category.getName().toLowerCase(), category.getImageResourceName());
            }
        }
    }
}
