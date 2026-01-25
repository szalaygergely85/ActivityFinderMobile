package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.CoverImageApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.CoverImage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class CoverImageRepository {
    private static final String TAG = "CoverImageRepository";
    private final CoverImageApiService coverImageApiService;

    @Inject
    public CoverImageRepository(CoverImageApiService coverImageApiService) {
        this.coverImageApiService = coverImageApiService;
    }

    public void getCoverImages(ApiCallback<List<CoverImage>> callback) {
        coverImageApiService
                .getAllCoverImages()
                .enqueue(
                        new Callback<List<CoverImage>>() {
                            @Override
                            public void onResponse(
                                    Call<List<CoverImage>> call,
                                    Response<List<CoverImage>> response) {
                                Log.d(TAG, "Response code: " + response.code());
                                Log.d(TAG, "Request URL: " + call.request().url());
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Loaded " + response.body().size() + " cover images");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorBody = "";
                                    try {
                                        if (response.errorBody() != null) {
                                            errorBody = response.errorBody().string();
                                        }
                                    } catch (Exception e) {
                                        errorBody = "Could not read error body";
                                    }
                                    String errorMsg = "Failed to load cover images: " + response.code() + " - " + errorBody;
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<CoverImage>> call, Throwable t) {
                                Log.e(TAG, "Request URL: " + call.request().url());
                                String errorMsg = "Network error: " + t.getClass().getSimpleName() + " - " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }
}
