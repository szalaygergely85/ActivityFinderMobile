package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.model.ImageUploadResponse;
import com.gege.activityfindermobile.data.model.UserPhoto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserPhotoApiService {

    // Upload a new photo
    @Multipart
    @POST("/api/users/photos/upload")
    Call<ImageUploadResponse> uploadPhoto(@Part MultipartBody.Part file);

    // Get current user's photos
    @GET("/api/users/photos")
    Call<List<UserPhoto>> getMyPhotos();

    // Get another user's photos (public)
    @GET("/api/users/photos/user/{userId}")
    Call<List<UserPhoto>> getUserPhotos(@Path("userId") Long userId);

    // Set a photo as profile picture
    @PUT("/api/users/photos/{photoId}/set-as-profile")
    Call<UserPhoto> setPhotoAsProfile(@Path("photoId") Long photoId);

    // Delete a photo
    @DELETE("/api/users/photos/{photoId}")
    Call<Void> deletePhoto(@Path("photoId") Long photoId);

    // Reorder photos
    @PUT("/api/users/photos/reorder")
    Call<Void> reorderPhotos(@Body PhotoReorderRequest request);

    // DTO for reorder request
    class PhotoReorderRequest {
        public List<Long> photoIds;

        public PhotoReorderRequest(List<Long> photoIds) {
            this.photoIds = photoIds;
        }
    }
}
