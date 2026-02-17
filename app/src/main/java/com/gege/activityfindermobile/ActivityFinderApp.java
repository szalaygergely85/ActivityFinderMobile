package com.gege.activityfindermobile;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.gege.activityfindermobile.data.api.CrashLogApiService;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.repository.NotificationRepository;
import com.gege.activityfindermobile.service.CrashReportService;
import com.gege.activityfindermobile.service.MyFirebaseMessagingService;
import com.gege.activityfindermobile.utils.CategoryManager;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ActivityFinderApp extends Application {

    private static final String TAG = "ActivityFinderApp";

    @Inject CategoryManager categoryManager;
    @Inject CrashLogApiService crashLogApiService;
    @Inject SharedPreferencesManager prefsManager;
    @Inject NotificationRepository notificationRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize crash reporting
        initializeCrashReporting();

        // Create notification channels
        createNotificationChannels();

        // Refresh category cache from backend
        if (categoryManager != null) {
            categoryManager.refreshCategories();
        }

        // Register FCM token if user is already logged in
        registerFcmTokenIfLoggedIn();
    }

    private void initializeCrashReporting() {
        if (crashLogApiService != null && prefsManager != null) {
            CrashReportService crashReportService = new CrashReportService(
                    this, crashLogApiService, prefsManager);
            crashReportService.initialize();
            Log.d(TAG, "Crash reporting initialized");
        }
    }

    private void registerFcmTokenIfLoggedIn() {
        if (prefsManager != null && prefsManager.isLoggedIn() && notificationRepository != null) {
            FirebaseMessaging.getInstance()
                    .getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Failed to get FCM token", task.getException());
                            return;
                        }

                        String token = task.getResult();
                        Log.d(TAG, "FCM Token on startup: " + token);

                        // Register with backend
                        notificationRepository.registerDeviceToken(
                                token,
                                new ApiCallbackVoid() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "FCM token registered on app startup");
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Log.e(TAG, "Failed to register FCM token on startup: " + errorMessage);
                                    }
                                });
                    });
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
