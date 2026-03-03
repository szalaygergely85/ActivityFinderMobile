package com.gege.activityfindermobile.logging;

import timber.log.Timber;

/**
 * Helper class for common logging patterns across the app.
 * Provides easy-to-use methods for logging with consistent formatting.
 */
public class LoggingHelper {

    private final String tag;
    private final BreadcrumbLogger breadcrumbLogger = BreadcrumbLogger.getInstance();

    public LoggingHelper(Class<?> clazz) {
        this.tag = clazz.getSimpleName();
    }

    public LoggingHelper(String tag) {
        this.tag = tag;
    }

    // Database operations
    public void logDbQuery(String query, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 100) {
            Timber.w("%s [DB] Slow query detected (%dms): %s", tag, duration, query);
            breadcrumbLogger.log("DB_SLOW", tag + " - " + duration + "ms");
        } else {
            Timber.d("%s [DB] Query executed in %dms", tag, duration);
        }
    }

    public void logDbError(String operation, Exception e) {
        Timber.e(e, "%s [DB] Error during %s", tag, operation);
        breadcrumbLogger.log("DB_ERROR", tag + " - " + operation + ": " + e.getMessage());
    }

    // Network operations
    public void logNetworkRequest(String method, String url, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        Timber.d("%s [NET] %s %s (%dms)", tag, method, url, duration);
    }

    public void logNetworkError(String method, String url, int statusCode, String error) {
        Timber.e("%s [NET] %s %s - Status: %d, Error: %s", tag, method, url, statusCode, error);
        breadcrumbLogger.log("NET_ERROR", statusCode + " - " + error);
    }

    public void logNetworkSuccess(String method, String url, int statusCode, long duration) {
        Timber.d("%s [NET] %s %s - Status: %d (%dms)", tag, method, url, statusCode, duration);
    }

    // Activity/UI operations
    public void logActivityCreated(String activityName) {
        Timber.i("%s [UI] Activity created: %s", tag, activityName);
        breadcrumbLogger.log("UI_ACTIVITY", activityName + " created");
    }

    public void logActivityDestroyed(String activityName) {
        Timber.i("%s [UI] Activity destroyed: %s", tag, activityName);
        breadcrumbLogger.log("UI_ACTIVITY", activityName + " destroyed");
    }

    public void logUserAction(String action, String details) {
        Timber.i("%s [UI] User action: %s - %s", tag, action, details);
        breadcrumbLogger.log("USER_ACTION", action + ": " + details);
    }

    // Generic methods
    public void info(String message) {
        Timber.i("%s: %s", tag, message);
    }

    public void info(String format, Object... args) {
        Timber.i("%s: %s", tag, String.format(format, args));
    }

    public void warn(String message) {
        Timber.w("%s: %s", tag, message);
        breadcrumbLogger.log("WARN", message);
    }

    public void warn(String format, Object... args) {
        String message = String.format(format, args);
        Timber.w("%s: %s", tag, message);
        breadcrumbLogger.log("WARN", message);
    }

    public void error(String message, Exception e) {
        Timber.e(e, "%s: %s", tag, message);
        breadcrumbLogger.log("ERROR", message + ": " + e.getMessage());
    }

    public void error(String message) {
        Timber.e("%s: %s", tag, message);
        breadcrumbLogger.log("ERROR", message);
    }

    public void debug(String message) {
        Timber.d("%s: %s", tag, message);
    }

    public void debug(String format, Object... args) {
        Timber.d("%s: %s", tag, String.format(format, args));
    }
}
