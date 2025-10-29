package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.model.Activity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ActivityApiService {

    @POST("api/activities")
    Call<Activity> createActivity(
            @Query("creatorId") Long creatorId, @Body ActivityCreateRequest request);

    @GET("api/activities/{id}")
    Call<Activity> getActivityById(@Path("id") Long id);

    @GET("api/activities")
    Call<List<Activity>> getAllOpenActivities();

    @GET("api/activities/upcoming")
    Call<List<Activity>> getUpcomingActivities();

    @GET("api/activities/trending")
    Call<List<Activity>> getTrendingActivities();

    @GET("api/activities/category/{category}")
    Call<List<Activity>> getActivitiesByCategory(@Path("category") String category);

    @GET("api/activities/location")
    Call<List<Activity>> getActivitiesByLocation(@Query("location") String location);

    @GET("api/activities/nearby")
    Call<List<Activity>> getNearbyActivities(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radiusKm") float radiusKm);

    @GET("api/activities/nearby")
    Call<List<Activity>> getNearbyActivitiesDefaultRadius(
            @Query("latitude") double latitude, @Query("longitude") double longitude);

    @GET("api/activities/search")
    Call<List<Activity>> searchActivities(@Query("keyword") String keyword);

    @GET("api/activities/creator/{creatorId}")
    Call<List<Activity>> getMyActivities(@Path("creatorId") Long creatorId);

    @GET("api/activities/my-activities")
    Call<List<Activity>> getMyActivitiesWithStatus(@Query("status") String status);

    @GET("api/activities/my-activities")
    Call<List<Activity>> getMyActivitiesAll();

    @GET("api/activities/available")
    Call<List<Activity>> getActivitiesWithSpots();

    @PUT("api/activities/{id}")
    Call<Activity> updateActivity(
            @Path("id") Long id,
            @Header("User-Id") Long userId,
            @Body ActivityCreateRequest request);

    @DELETE("api/activities/{id}")
    Call<Void> cancelActivity(@Path("id") Long id, @Header("User-Id") Long userId);
}
