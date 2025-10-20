package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.NotificationApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.DeviceTokenRequest;
import com.gege.activityfindermobile.data.dto.NotificationPreferencesRequest;
import com.gege.activityfindermobile.data.dto.UnreadCountResponse;
import com.gege.activityfindermobile.data.model.Notification;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private final NotificationApiService notificationApiService;

    @Inject
    public NotificationRepository(NotificationApiService notificationApiService) {
        this.notificationApiService = notificationApiService;
    }

    /** Register FCM device token */
    public void registerDeviceToken(String fcmToken, ApiCallbackVoid callback) {
        DeviceTokenRequest request = new DeviceTokenRequest(fcmToken);
        notificationApiService
                .registerDeviceToken(request)
                .enqueue(
                        new Callback<Map<String, String>>() {
                            @Override
                            public void onResponse(
                                    Call<Map<String, String>> call,
                                    Response<Map<String, String>> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Device token registered successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to register device token: " + response.code();
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

    /** Update notification preferences */
    public void updateNotificationPreferences(boolean enabled, ApiCallbackVoid callback) {
        NotificationPreferencesRequest request = new NotificationPreferencesRequest(enabled);
        notificationApiService
                .updateNotificationPreferences(request)
                .enqueue(
                        new Callback<Map<String, String>>() {
                            @Override
                            public void onResponse(
                                    Call<Map<String, String>> call,
                                    Response<Map<String, String>> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Notification preferences updated successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to update notification preferences: "
                                                    + response.code();
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

    /** Get all notifications */
    public void getAllNotifications(ApiCallback<List<Notification>> callback) {
        notificationApiService
                .getAllNotifications()
                .enqueue(
                        new Callback<List<Notification>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Notification>> call,
                                    Response<List<Notification>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched " + response.body().size() + " notifications");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch notifications: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Notification>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get unread notifications */
    public void getUnreadNotifications(ApiCallback<List<Notification>> callback) {
        notificationApiService
                .getUnreadNotifications()
                .enqueue(
                        new Callback<List<Notification>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Notification>> call,
                                    Response<List<Notification>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " unread notifications");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch unread notifications: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Notification>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get unread count */
    public void getUnreadCount(ApiCallback<Integer> callback) {
        notificationApiService
                .getUnreadCount()
                .enqueue(
                        new Callback<UnreadCountResponse>() {
                            @Override
                            public void onResponse(
                                    Call<UnreadCountResponse> call,
                                    Response<UnreadCountResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer count = response.body().getUnreadCount();
                                    Log.d(TAG, "Unread count: " + count);
                                    callback.onSuccess(count);
                                } else {
                                    String errorMsg =
                                            "Failed to fetch unread count: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Mark notification as read */
    public void markAsRead(Long notificationId, ApiCallbackVoid callback) {
        notificationApiService
                .markAsRead(notificationId)
                .enqueue(
                        new Callback<Map<String, String>>() {
                            @Override
                            public void onResponse(
                                    Call<Map<String, String>> call,
                                    Response<Map<String, String>> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Notification marked as read");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg = "Failed to mark as read: " + response.code();
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

    /** Mark all notifications as read */
    public void markAllAsRead(ApiCallbackVoid callback) {
        notificationApiService
                .markAllAsRead()
                .enqueue(
                        new Callback<Map<String, String>>() {
                            @Override
                            public void onResponse(
                                    Call<Map<String, String>> call,
                                    Response<Map<String, String>> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "All notifications marked as read");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to mark all as read: " + response.code();
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

    /** Delete a notification */
    public void deleteNotification(Long notificationId, ApiCallbackVoid callback) {
        notificationApiService
                .deleteNotification(notificationId)
                .enqueue(
                        new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Notification deleted successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorMsg =
                                            "Failed to delete notification: " + response.code();
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
