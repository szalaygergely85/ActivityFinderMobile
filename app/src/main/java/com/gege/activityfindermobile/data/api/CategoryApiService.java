package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CategoryApiService {

    @GET("api/categories")
    Call<List<Category>> getAllCategories();

    @GET("api/categories/popular")
    Call<List<Category>> getPopularCategories();

    @GET("api/categories/{id}")
    Call<Category> getCategoryById(@Path("id") Long id);
}
