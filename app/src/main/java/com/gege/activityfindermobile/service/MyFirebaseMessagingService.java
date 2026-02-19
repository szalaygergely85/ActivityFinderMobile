package com.gege.activityfindermobile.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.repository.NotificationRepository;
import com.gege.activityfindermobile.ui.main.MainActivity;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    public static final String CHANNEL_ID = "vivento_notifications";
    public static final String CHANNEL_NAME = "Vivento Notifications";

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
            showNotification(title, body, data);
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

    private void showNotification(String title, String body, Map<String, String> data) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for Vivento activities and updates");
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Create intent to open app when notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add navigation data if present
        if (data != null) {
            if (data.containsKey("screen")) {
                intent.putExtra("navigate_to", data.get("screen"));
            }
            if (data.containsKey("activityId")) {
                intent.putExtra("activityId", data.get("activityId"));
            }
            if (data.containsKey("notificationId")) {
                intent.putExtra("notificationId", data.get("notificationId"));
            }
        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.vivento)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        // Show notification with unique ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
