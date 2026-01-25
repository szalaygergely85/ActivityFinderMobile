package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.model.CoverImage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CoverImageApiService {

    @GET("api/covers")
    Call<List<CoverImage>> getAllCoverImages();
}
