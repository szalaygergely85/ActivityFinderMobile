package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.ActivityPhotoApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.model.ActivityGalleryAccess;
import com.gege.activityfindermobile.data.model.ActivityPhoto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ActivityPhotoRepository {
    private static final String TAG = "ActivityPhotoRepository";
    private final ActivityPhotoApiService activityPhotoApiService;

    @Inject
    public ActivityPhotoRepository(ActivityPhotoApiService activityPhotoApiService) {
        this.activityPhotoApiService = activityPhotoApiService;
    }

    /**
     * Check if current user has access to activity gallery
     */
    public void checkGalleryAccess(Long activityId, ApiCallback<ActivityGalleryAccess> callback) {
        activityPhotoApiService
                .checkGalleryAccess(activityId)
                .enqueue(
                        new Callback<ActivityGalleryAccess>() {
                            @Override
                            public void onResponse(
                                    Call<ActivityGalleryAccess> call,
                                    Response<ActivityGalleryAccess> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Gallery access checked successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to check gallery access: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<ActivityGalleryAccess> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /**
     * Upload multiple photos to activity gallery
     */
    public void uploadPhotos(
            Long activityId, List<File> imageFiles, ApiCallback<List<ActivityPhoto>> callback) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            callback.onError("No images selected");
            return;
        }

        // Create multipart body parts for each file
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (File imageFile : imageFiles) {
            if (imageFile.exists()) {
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("image/*"), imageFile);
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("files", imageFile.getName(), requestFile);
                parts.add(body);
            }
        }

        if (parts.isEmpty()) {
            callback.onError("No valid image files found");
            return;
        }

        activityPhotoApiService
                .uploadPhotos(activityId, parts)
                .enqueue(
                        new Callback<List<ActivityPhoto>>() {
                            @Override
                            public void onResponse(
                                    Call<List<ActivityPhoto>> call,
                                    Response<List<ActivityPhoto>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Photos uploaded successfully: "
                                                    + response.body().size());
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to upload photos: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<ActivityPhoto>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /**
     * Get all photos for an activity
     */
    public void getActivityPhotos(Long activityId, ApiCallback<List<ActivityPhoto>> callback) {
        activityPhotoApiService
                .getActivityPhotos(activityId)
                .enqueue(
                        new Callback<List<ActivityPhoto>>() {
                            @Override
                            public void onResponse(
                                    Call<List<ActivityPhoto>> call,
                                    Response<List<ActivityPhoto>> response) {
                                if (response.isSuccessful()) {
                                    List<ActivityPhoto> photos = response.body();
                                    if (photos != null) {
                                        Log.d(TAG, "Fetched " + photos.size() + " photos");
                                        callback.onSuccess(photos);
                                    } else {
                                        Log.d(TAG, "No photos found");
                                        callback.onSuccess(new ArrayList<>());
                                    }
                                } else {
                                    String errorMsg = "Failed to fetch photos: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<ActivityPhoto>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /**
     * Delete a photo from the gallery
     */
    public void deletePhoto(Long activityId, Long photoId, ApiCallbackVoid callback) {
        activityPhotoApiService
                .deletePhoto(activityId, photoId)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Photo deleted successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg = "Failed to delete photo: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /**
     * Get photo count for an activity
     */
    public void getPhotoCount(Long activityId, ApiCallback<Long> callback) {
        activityPhotoApiService
                .getPhotoCount(activityId)
                .enqueue(
                        new Callback<Long>() {
                            @Override
                            public void onResponse(Call<Long> call, Response<Long> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Photo count: " + response.body());
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to get photo count: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Long> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }
}
