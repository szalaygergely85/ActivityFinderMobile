package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.model.ActivityGalleryAccess;
import com.gege.activityfindermobile.data.model.ActivityPhoto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ActivityPhotoApiService {

    // Check if user has access to activity gallery
    @GET("/api/activities/{activityId}/gallery/access")
    Call<ActivityGalleryAccess> checkGalleryAccess(@Path("activityId") Long activityId);

    // Upload multiple photos to activity gallery
    @Multipart
    @POST("/api/activities/{activityId}/gallery/upload")
    Call<List<ActivityPhoto>> uploadPhotos(
            @Path("activityId") Long activityId, @Part List<MultipartBody.Part> files);

    // Get all photos for an activity
    @GET("/api/activities/{activityId}/gallery")
    Call<List<ActivityPhoto>> getActivityPhotos(@Path("activityId") Long activityId);

    // Delete a photo
    @DELETE("/api/activities/{activityId}/gallery/{photoId}")
    Call<Void> deletePhoto(@Path("activityId") Long activityId, @Path("photoId") Long photoId);

    // Get photo count for an activity
    @GET("/api/activities/{activityId}/gallery/count")
    Call<Long> getPhotoCount(@Path("activityId") Long activityId);
}
