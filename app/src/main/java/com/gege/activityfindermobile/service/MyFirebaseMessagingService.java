package com.gege.activityfindermobile.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.repository.NotificationRepository;
import com.gege.activityfindermobile.utils.NotificationHelper;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    public static final String CHANNEL_ID = NotificationHelper.CHANNEL_ID;

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    SharedPreferencesManager prefsManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // Send token to backend if user is logged in
        if (prefsManager != null && prefsManager.isLoggedIn()) {
            sendTokenToServer(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        String title = "";
        String body = "";

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification - Title: " + title + ", Body: " + body);
        }

        // Check if message contains a data payload
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            Log.d(TAG, "Data payload: " + data);

            // Override title/body if present in data
            if (data.containsKey("title")) {
                title = data.get("title");
            }
            if (data.containsKey("body")) {
                body = data.get("body");
            }
        }

        // Show notification if we have content and user has not disabled this type
        if (title != null && !title.isEmpty() && shouldShowNotification(data)) {
            NotificationHelper.show(this, title, body, data);
        }
    }

    private boolean shouldShowNotification(Map<String, String> data) {
        if (prefsManager == null) return true;
        String type = data != null ? data.get("type") : null;
        if (type == null) return true;

        if ("ACTIVITY_REMINDER".equals(type)) {
            return prefsManager.getBoolean("notify_reminders", true);
        }

        switch (type) {
            case "ACTIVITY_CREATED":
            case "ACTIVITY_UPDATED":
            case "ACTIVITY_CANCELLED":
            case "ACTIVITY_COMPLETED":
            case "PARTICIPANT_INTERESTED":
            case "PARTICIPANT_ACCEPTED":
            case "PARTICIPANT_DECLINED":
            case "PARTICIPANT_JOINED":
            case "PARTICIPANT_LEFT":
            case "REVIEW_RECEIVED":
            case "NEW_MESSAGE":
                return prefsManager.getBoolean("notify_activity_updates", true);
            default:
                return true; // System notifications always shown
        }
    }

    private void sendTokenToServer(String token) {
        if (notificationRepository == null) {
            Log.e(TAG, "NotificationRepository is null, cannot send token");
            return;
        }

        notificationRepository.registerDeviceToken(
                token,
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "FCM token registered with server successfully");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to register FCM token: " + errorMessage);
                    }
                });
    }

}
