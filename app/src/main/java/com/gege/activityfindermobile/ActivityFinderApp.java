package com.gege.activityfindermobile;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.gege.activityfindermobile.service.MyFirebaseMessagingService;
import com.gege.activityfindermobile.utils.CategoryManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ActivityFinderApp extends Application {

    @Inject CategoryManager categoryManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize any app-wide services or configurations here

        // Create notification channels
        createNotificationChannels();

        // Refresh category cache from backend
        if (categoryManager != null) {
            categoryManager.refreshCategories();
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            MyFirebaseMessagingService.CHANNEL_ID,
                            MyFirebaseMessagingService.CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for Vivento activities and updates");
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
