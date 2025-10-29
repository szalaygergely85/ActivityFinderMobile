package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.ActivityApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.model.Activity;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ActivityRepository {
    private static final String TAG = "ActivityRepository";
    private final ActivityApiService activityApiService;

    @Inject
    public ActivityRepository(ActivityApiService activityApiService) {
        this.activityApiService = activityApiService;
    }

    /** Create a new activity */
    public void createActivity(
            Long userId, ActivityCreateRequest request, ApiCallback<Activity> callback) {
        activityApiService
                .createActivity(userId, request)
                .enqueue(
                        new Callback<Activity>() {
                            @Override
                            public void onResponse(
                                    Call<Activity> call, Response<Activity> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Activity created successfully: "
                                                    + response.body().getTitle());
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to create activity: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Activity> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get activity by ID */
    public void getActivityById(Long activityId, ApiCallback<Activity> callback) {
        activityApiService
                .getActivityById(activityId)
                .enqueue(
                        new Callback<Activity>() {
                            @Override
                            public void onResponse(
                                    Call<Activity> call, Response<Activity> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Activity fetched successfully: "
                                                    + response.body().getId());
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch activity: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Activity> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get all open activities */
    public void getAllOpenActivities(ApiCallback<List<Activity>> callback) {
        activityApiService
                .getAllOpenActivities()
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " open activities");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch open activities: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get upcoming activities */
    public void getUpcomingActivities(ApiCallback<List<Activity>> callback) {
        activityApiService
                .getUpcomingActivities()
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " upcoming activities");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch upcoming activities: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get trending activities */
    public void getTrendingActivities(ApiCallback<List<Activity>> callback) {
        activityApiService
                .getTrendingActivities()
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " trending activities");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch trending activities: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get activities by category */
    public void getActivitiesByCategory(String category, ApiCallback<List<Activity>> callback) {
        activityApiService
                .getActivitiesByCategory(category)
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched activities for category: " + category);
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch activities by category: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get activities by location */
    public void getActivitiesByLocation(String location, ApiCallback<List<Activity>> callback) {
        activityApiService
                .getActivitiesByLocation(location)
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched activities for location: " + location);
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch activities by location: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Search activities by keyword */
    public void searchActivities(String keyword, ApiCallback<List<Activity>> callback) {
        activityApiService
                .searchActivities(keyword)
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Search returned "
                                                    + response.body().size()
                                                    + " activities");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to search activities: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get user's own activities (all statuses including expired) */
    public void getMyActivities(Long userId, ApiCallback<List<Activity>> callback) {
        activityApiService
                .getMyActivitiesAll()
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched user's activities");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch user activities: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get activities with available spots */
    public void getActivitiesWithSpots(ApiCallback<List<Activity>> callback) {
        activityApiService
                .getActivitiesWithSpots()
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched activities with available spots");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch activities with spots: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Update activity */
    public void updateActivity(
            Long activityId,
            Long userId,
            ActivityCreateRequest request,
            ApiCallback<Activity> callback) {
        activityApiService
                .updateActivity(activityId, userId, request)
                .enqueue(
                        new Callback<Activity>() {
                            @Override
                            public void onResponse(
                                    Call<Activity> call, Response<Activity> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Activity updated successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to update activity: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Activity> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Cancel activity */
    public void cancelActivity(Long activityId, Long userId, ApiCallbackVoid callback) {
        activityApiService
                .cancelActivity(activityId, userId)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Activity cancelled successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to cancel activity: " + response.code();
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

    /** Get nearby activities with custom radius */
    public void getNearbyActivities(
            double latitude,
            double longitude,
            float radiusKm,
            ApiCallback<List<Activity>> callback) {
        activityApiService
                .getNearbyActivities(latitude, longitude, radiusKm)
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " nearby activities within "
                                                    + radiusKm
                                                    + "km");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch nearby activities: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get nearby activities with default radius (10 km) */
    public void getNearbyActivitiesDefaultRadius(
            double latitude, double longitude, ApiCallback<List<Activity>> callback) {
        activityApiService
                .getNearbyActivitiesDefaultRadius(latitude, longitude)
                .enqueue(
                        new Callback<List<Activity>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Activity>> call, Response<List<Activity>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " nearby activities");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch nearby activities: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Activity>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }
}
