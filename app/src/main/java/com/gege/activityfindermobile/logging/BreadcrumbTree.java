package com.gege.activityfindermobile.logging;

import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

/**
 * Timber tree that captures WARN/ERROR logs as breadcrumbs for crash reporting.
 * Provides context about what happened before a crash.
 */
public class BreadcrumbTree extends Timber.Tree {

    private final BreadcrumbLogger breadcrumbLogger = BreadcrumbLogger.getInstance();

    @Override
    protected void log(
            int priority,
            @Nullable String tag,
            @Nullable String message,
            @Nullable Throwable t) {
        if (message == null) {
            return;
        }

        if (priority >= android.util.Log.WARN) {
            String breadcrumbMessage = message;
            if (t != null) {
                breadcrumbMessage =
                        message + " - " + t.getClass().getSimpleName() + ": " + t.getMessage();
            }
            breadcrumbLogger.log(getPriorityName(priority), breadcrumbMessage);
        }
    }

    private String getPriorityName(int priority) {
        switch (priority) {
            case android.util.Log.VERBOSE:
                return "V";
            case android.util.Log.DEBUG:
                return "D";
            case android.util.Log.INFO:
                return "I";
            case android.util.Log.WARN:
                return "W";
            case android.util.Log.ERROR:
                return "E";
            case android.util.Log.ASSERT:
                return "A";
            default:
                return "?";
        }
    }
}
