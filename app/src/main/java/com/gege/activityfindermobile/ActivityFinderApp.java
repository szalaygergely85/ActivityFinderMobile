package com.gege.activityfindermobile;

import android.app.Application;

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

        // Refresh category cache from backend
        if (categoryManager != null) {
            categoryManager.refreshCategories();
        }
    }
}
