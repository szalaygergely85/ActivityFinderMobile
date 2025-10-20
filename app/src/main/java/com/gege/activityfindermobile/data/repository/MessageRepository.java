package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.MessageApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.MessageCountResponse;
import com.gege.activityfindermobile.data.dto.MessageRequest;
import com.gege.activityfindermobile.data.model.ActivityMessage;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class MessageRepository {
    private static final String TAG = "MessageRepository";
    private final MessageApiService messageApiService;

    @Inject
    public MessageRepository(MessageApiService messageApiService) {
        this.messageApiService = messageApiService;
    }

    /** Send a message in an activity chat */
    public void sendMessage(
            Long activityId, String messageText, ApiCallback<ActivityMessage> callback) {
        MessageRequest request = new MessageRequest(messageText);
        messageApiService
                .sendMessage(activityId, request)
                .enqueue(
                        new Callback<ActivityMessage>() {
                            @Override
                            public void onResponse(
                                    Call<ActivityMessage> call,
                                    Response<ActivityMessage> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Message sent successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to send message: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<ActivityMessage> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get all messages for an activity */
    public void getMessages(Long activityId, ApiCallback<List<ActivityMessage>> callback) {
        messageApiService
                .getMessages(activityId)
                .enqueue(
                        new Callback<List<ActivityMessage>>() {
                            @Override
                            public void onResponse(
                                    Call<List<ActivityMessage>> call,
                                    Response<List<ActivityMessage>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched " + response.body().size() + " messages");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch messages: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<ActivityMessage>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get messages since a specific timestamp (for polling) */
    public void getMessagesSince(
            Long activityId, String timestamp, ApiCallback<List<ActivityMessage>> callback) {
        messageApiService
                .getMessagesSince(activityId, timestamp)
                .enqueue(
                        new Callback<List<ActivityMessage>>() {
                            @Override
                            public void onResponse(
                                    Call<List<ActivityMessage>> call,
                                    Response<List<ActivityMessage>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched " + response.body().size() + " new messages");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch new messages: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<ActivityMessage>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get message count for an activity */
    public void getMessageCount(Long activityId, ApiCallback<Integer> callback) {
        messageApiService
                .getMessageCount(activityId)
                .enqueue(
                        new Callback<MessageCountResponse>() {
                            @Override
                            public void onResponse(
                                    Call<MessageCountResponse> call,
                                    Response<MessageCountResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer count = response.body().getMessageCount();
                                    Log.d(TAG, "Message count: " + count);
                                    callback.onSuccess(count);
                                } else {
                                    String errorMsg =
                                            "Failed to fetch message count: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<MessageCountResponse> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Delete a message */
    public void deleteMessage(Long activityId, Long messageId, ApiCallbackVoid callback) {
        messageApiService
                .deleteMessage(activityId, messageId)
                .enqueue(
                        new Callback<Map<String, String>>() {
                            @Override
                            public void onResponse(
                                    Call<Map<String, String>> call,
                                    Response<Map<String, String>> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Message deleted successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to delete message: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }
}
