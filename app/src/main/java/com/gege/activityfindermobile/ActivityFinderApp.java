package com.gege.activityfindermobile;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ActivityFinderApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize any app-wide services or configurations here
    }
}
