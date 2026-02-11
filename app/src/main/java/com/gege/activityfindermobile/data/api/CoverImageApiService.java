package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.model.CoverImage;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CoverImageApiService {

    @GET("api/covers")
    Call<List<CoverImage>> getAllCoverImages();

    @Multipart
    @POST("api/covers/v2/upload")
    Call<CoverImage> uploadCoverImage(
            @Part MultipartBody.Part file,
            @Part("displayName") RequestBody displayName);
}
