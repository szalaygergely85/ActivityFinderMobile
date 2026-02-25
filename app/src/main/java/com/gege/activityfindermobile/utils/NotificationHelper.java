package com.gege.activityfindermobile.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.ui.main.MainActivity;

import java.util.Map;

public class NotificationHelper {

    public static final String CHANNEL_ID = "vivento_notifications";
    public static final String CHANNEL_NAME = "Vivento Notifications";

    public static void show(Context context, String title, String body, Map<String, String> data) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for Vivento activities and updates");
            channel.enableVibration(true);
            channel.enableLights(true);
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (data != null) {
            if (data.containsKey("screen")) intent.putExtra("navigate_to", data.get("screen"));
            if (data.containsKey("activityId")) intent.putExtra("activityId", data.get("activityId"));
            if (data.containsKey("notificationId")) intent.putExtra("notificationId", data.get("notificationId"));
        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        (int) System.currentTimeMillis(),
                        intent,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.vivento)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void sendAllTestNotifications(Context context) {
        String[][] tests = {
            {"NEW_MESSAGE",           "New Message",          "Alex sent a message in 'Morning Run'"},
            {"PARTICIPANT_ACCEPTED",  "Request Accepted",     "Your join request was accepted!"},
            {"PARTICIPANT_DECLINED",  "Request Declined",     "Your join request was declined"},
            {"PARTICIPANT_INTERESTED","New Join Request",     "Someone wants to join your activity"},
            {"PARTICIPANT_LEFT",      "Participant Left",     "A participant left your activity"},
            {"ACTIVITY_CREATED",      "New Activity",         "A new activity was created near you"},
            {"ACTIVITY_UPDATED",      "Activity Updated",     "An activity you joined has been updated"},
            {"ACTIVITY_CANCELLED",    "Activity Cancelled",   "An activity you joined has been cancelled"},
            {"ACTIVITY_COMPLETED",    "Activity Completed",   "Congratulations! Activity is now complete"},
            {"ACTIVITY_REMINDER",     "Activity Reminder",    "Your activity starts in 1 hour"},
            {"REVIEW_RECEIVED",       "New Review",           "You received a new 5-star review"},
        };

        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < tests.length; i++) {
            final String[] test = tests[i];
            handler.postDelayed(
                    () -> show(context, test[1], test[2], null),
                    i * 1500L);
        }
    }
}
