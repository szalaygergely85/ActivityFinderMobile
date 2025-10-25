package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.ReviewApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.ReviewRequest;
import com.gege.activityfindermobile.data.model.Review;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ReviewRepository {
    private static final String TAG = "ReviewRepository";
    private final ReviewApiService reviewApiService;

    @Inject
    public ReviewRepository(ReviewApiService reviewApiService) {
        this.reviewApiService = reviewApiService;
    }

    /** Create a review */
    public void createReview(
            Long userId, ReviewRequest request, ApiCallback<Review> callback) {
        reviewApiService
                .createReview(userId, request)
                .enqueue(
                        new Callback<Review>() {
                            @Override
                            public void onResponse(Call<Review> call, Response<Review> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Review created successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to create review: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Review> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get reviews for a user */
    public void getReviewsForUser(Long userId, ApiCallback<List<Review>> callback) {
        reviewApiService
                .getReviewsForUser(userId)
                .enqueue(
                        new Callback<List<Review>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Review>> call, Response<List<Review>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " reviews for user");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to fetch reviews: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Review>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get reviews for an activity */
    public void getReviewsForActivity(Long activityId, ApiCallback<List<Review>> callback) {
        reviewApiService
                .getReviewsForActivity(activityId)
                .enqueue(
                        new Callback<List<Review>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Review>> call, Response<List<Review>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " reviews for activity");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch activity reviews: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Review>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Delete a review */
    public void deleteReview(Long reviewId, Long userId, ApiCallbackVoid callback) {
        reviewApiService
                .deleteReview(reviewId, userId)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Review deleted successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg = "Failed to delete review: " + response.code();
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
}
