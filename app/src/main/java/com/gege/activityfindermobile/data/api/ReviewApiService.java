package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.dto.ReviewCreateRequest;
import com.gege.activityfindermobile.data.model.Review;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReviewApiService {

    @POST("api/reviews")
    Call<Review> createReview(@Header("User-Id") Long userId, @Body ReviewCreateRequest request);

    @GET("api/reviews/user/{userId}")
    Call<List<Review>> getReviewsForUser(@Path("userId") Long userId);

    @GET("api/reviews/activity/{activityId}")
    Call<List<Review>> getReviewsForActivity(@Path("activityId") Long activityId);

    @DELETE("api/reviews/{id}")
    Call<Void> deleteReview(@Path("id") Long id, @Header("User-Id") Long userId);
}
