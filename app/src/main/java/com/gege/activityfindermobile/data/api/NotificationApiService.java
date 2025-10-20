package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.dto.DeviceTokenRequest;
import com.gege.activityfindermobile.data.dto.NotificationPreferencesRequest;
import com.gege.activityfindermobile.data.dto.UnreadCountResponse;
import com.gege.activityfindermobile.data.model.Notification;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** API Service for notifications */
public interface NotificationApiService {

    /** Register FCM device token */
    @POST("api/notifications/device-token")
    Call<Map<String, String>> registerDeviceToken(@Body DeviceTokenRequest request);

    /** Update notification preferences */
    @PUT("api/notifications/preferences")
    Call<Map<String, String>> updateNotificationPreferences(
            @Body NotificationPreferencesRequest request);

    /** Get all notifications for current user */
    @GET("api/notifications")
    Call<List<Notification>> getAllNotifications();

    /** Get unread notifications */
    @GET("api/notifications/unread")
    Call<List<Notification>> getUnreadNotifications();

    /** Get unread notification count */
    @GET("api/notifications/unread/count")
    Call<UnreadCountResponse> getUnreadCount();

    /** Mark notification as read */
    @PATCH("api/notifications/{id}/read")
    Call<Map<String, String>> markAsRead(@Path("id") Long notificationId);

    /** Mark all notifications as read */
    @PATCH("api/notifications/read-all")
    Call<Map<String, String>> markAllAsRead();

    /** Delete a notification */
    @DELETE("api/notifications/{id}")
    Call<Void> deleteNotification(@Path("id") Long notificationId);
}
