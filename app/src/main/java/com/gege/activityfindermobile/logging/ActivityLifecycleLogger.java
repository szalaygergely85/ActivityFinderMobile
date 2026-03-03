package com.gege.activityfindermobile.logging;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import timber.log.Timber;

/**
 * Logs all activity lifecycle events for debugging and crash context.
 * Helps track which activities were open and their state at crash time.
 */
public class ActivityLifecycleLogger implements Application.ActivityLifecycleCallbacks {

    private final BreadcrumbLogger breadcrumbLogger = BreadcrumbLogger.getInstance();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String name = activity.getClass().getSimpleName();
        Timber.d("[LIFECYCLE] %s created", name);
        breadcrumbLogger.log("LIFECYCLE", name + " created");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        String name = activity.getClass().getSimpleName();
        Timber.d("[LIFECYCLE] %s started", name);
        breadcrumbLogger.log("LIFECYCLE", name + " started");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        String name = activity.getClass().getSimpleName();
        Timber.i("[LIFECYCLE] %s resumed", name);
        breadcrumbLogger.log("LIFECYCLE", name + " resumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        String name = activity.getClass().getSimpleName();
        Timber.d("[LIFECYCLE] %s paused", name);
        breadcrumbLogger.log("LIFECYCLE", name + " paused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Timber.d("[LIFECYCLE] %s stopped", activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Timber.d("[LIFECYCLE] %s save instance state", activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        String name = activity.getClass().getSimpleName();
        Timber.i("[LIFECYCLE] %s destroyed", name);
        breadcrumbLogger.log("LIFECYCLE", name + " destroyed");
    }
}
