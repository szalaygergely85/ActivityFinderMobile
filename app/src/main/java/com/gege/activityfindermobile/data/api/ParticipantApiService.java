package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.dto.ExpressInterestRequest;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.api.ParticipantStatusUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ParticipantApiService {

    @POST("api/participants/activities/{activityId}/interest")
    Call<Participant> expressInterest(
            @Path("activityId") Long activityId,
            @Query("userId") Long userId,
            @Body ExpressInterestRequest request
    );

    @GET("api/participants/activities/{activityId}")
    Call<List<Participant>> getActivityParticipants(@Path("activityId") Long activityId);

    @GET("api/participants/activities/{activityId}/interested")
    Call<List<Participant>> getInterestedUsers(
            @Path("activityId") Long activityId,
            @Query("creatorId") Long creatorId
    );

    @GET("api/participants/my-participations")
    Call<List<Participant>> getMyParticipations(@Query("userId") Long userId);

    @PATCH("api/participants/{participantId}/status")
    Call<Participant> updateParticipantStatus(
            @Path("participantId") Long participantId,
            @Query("creatorId") Long creatorId,
            @Body ParticipantStatusUpdateRequest request
    );

    @DELETE("api/participants/activities/{activityId}/leave")
    Call<Void> leaveActivity(
            @Path("activityId") Long activityId,
            @Query("userId") Long userId
    );
}
