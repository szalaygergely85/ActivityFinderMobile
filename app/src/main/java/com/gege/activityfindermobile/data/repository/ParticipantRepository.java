package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.ParticipantApiService;
import com.gege.activityfindermobile.data.api.ParticipantStatusUpdateRequest;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.ExpressInterestRequest;
import com.gege.activityfindermobile.data.model.Participant;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ParticipantRepository {
    private static final String TAG = "ParticipantRepository";
    private final ParticipantApiService participantApiService;

    @Inject
    public ParticipantRepository(ParticipantApiService participantApiService) {
        this.participantApiService = participantApiService;
    }

    /** Express interest in an activity */
    public void expressInterest(
            Long activityId,
            Long userId,
            ExpressInterestRequest request,
            ApiCallback<Participant> callback) {
        participantApiService
                .expressInterest(activityId, userId, request)
                .enqueue(
                        new Callback<Participant>() {
                            @Override
                            public void onResponse(
                                    Call<Participant> call, Response<Participant> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Interest expressed successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg;
                                    try {
                                        if (response.errorBody() != null) {
                                            String errorBody = response.errorBody().string();
                                            // Try to extract the message from error body
                                            if (errorBody.contains("already expressed interest")) {
                                                errorMsg = "You have already joined this activity";
                                            } else if (errorBody.contains("message")) {
                                                // Try to parse JSON error message
                                                int msgStart =
                                                        errorBody.indexOf("\"message\":\"") + 11;
                                                int msgEnd = errorBody.indexOf("\"", msgStart);
                                                if (msgStart > 10 && msgEnd > msgStart) {
                                                    errorMsg =
                                                            errorBody.substring(msgStart, msgEnd);
                                                } else {
                                                    errorMsg = "Failed to join activity";
                                                }
                                            } else {
                                                errorMsg = "Failed to join activity";
                                            }
                                        } else {
                                            errorMsg =
                                                    "Failed to express interest: "
                                                            + response.code();
                                        }
                                    } catch (Exception e) {
                                        errorMsg = "Failed to join activity";
                                        Log.e(TAG, "Error parsing error body", e);
                                    }
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Participant> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get all participants for an activity */
    public void getActivityParticipants(Long activityId, ApiCallback<List<Participant>> callback) {
        participantApiService
                .getActivityParticipants(activityId)
                .enqueue(
                        new Callback<List<Participant>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Participant>> call,
                                    Response<List<Participant>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched " + response.body().size() + " participants");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch participants: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Participant>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get interested users for an activity (creator only) */
    public void getInterestedUsers(
            Long activityId, Long creatorId, ApiCallback<List<Participant>> callback) {
        participantApiService
                .getInterestedUsers(activityId, creatorId)
                .enqueue(
                        new Callback<List<Participant>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Participant>> call,
                                    Response<List<Participant>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " interested users");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch interested users: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Participant>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get user's participations */
    public void getMyParticipations(Long userId, ApiCallback<List<Participant>> callback) {
        participantApiService
                .getMyParticipations(userId)
                .enqueue(
                        new Callback<List<Participant>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Participant>> call,
                                    Response<List<Participant>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched user's participations");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch participations: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Participant>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Update participant status (Accept/Decline) */
    public void updateParticipantStatus(
            Long participantId, Long creatorId, String status, ApiCallback<Participant> callback) {
        ParticipantStatusUpdateRequest request = new ParticipantStatusUpdateRequest(status);
        participantApiService
                .updateParticipantStatus(participantId, creatorId, request)
                .enqueue(
                        new Callback<Participant>() {
                            @Override
                            public void onResponse(
                                    Call<Participant> call, Response<Participant> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Participant status updated to: " + status);
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to update status: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Participant> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Leave an activity */
    public void leaveActivity(Long activityId, Long userId, ApiCallbackVoid callback) {
        participantApiService
                .leaveActivity(activityId, userId)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Left activity successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to leave activity: " + response.code();
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
