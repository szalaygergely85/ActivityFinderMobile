package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.dto.MessageCountResponse;
import com.gege.activityfindermobile.data.dto.MessageRequest;
import com.gege.activityfindermobile.data.model.ActivityMessage;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** API Service for activity messaging */
public interface MessageApiService {

    /** Send a message in an activity chat */
    @POST("api/activities/{activityId}/messages")
    Call<ActivityMessage> sendMessage(
            @Path("activityId") Long activityId, @Body MessageRequest request);

    /** Get all messages for an activity */
    @GET("api/activities/{activityId}/messages")
    Call<List<ActivityMessage>> getMessages(@Path("activityId") Long activityId);

    /** Get messages since a specific timestamp */
    @GET("api/activities/{activityId}/messages/since")
    Call<List<ActivityMessage>> getMessagesSince(
            @Path("activityId") Long activityId, @Query("timestamp") String timestamp);

    /** Get message count for an activity */
    @GET("api/activities/{activityId}/messages/count")
    Call<MessageCountResponse> getMessageCount(@Path("activityId") Long activityId);

    /** Delete a message */
    @DELETE("api/activities/{activityId}/messages/{messageId}")
    Call<Map<String, String>> deleteMessage(
            @Path("activityId") Long activityId, @Path("messageId") Long messageId);
}
