package com.gege.activityfindermobile.logging;

import android.util.Log;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;

/**
 * Timber tree for production (release) builds.
 * Logs only warnings and errors to reduce overhead while maintaining visibility.
 */
public class ReleaseTree extends Timber.Tree {

    private static final int MIN_LOG_LEVEL = Log.WARN;

    @Override
    protected void log(
            int priority,
            @Nullable String tag,
            @Nullable String message,
            @Nullable Throwable t) {
        if (priority < MIN_LOG_LEVEL) {
            return;
        }

        String formattedMessage = formatMessage(message, t);
        Log.println(priority, tag != null ? tag : "Timber", formattedMessage);
    }

    private String formatMessage(@Nullable String message, @Nullable Throwable t) {
        if (t == null) {
            return message != null ? message : "";
        }
        if (message == null) {
            return Log.getStackTraceString(t);
        }
        return message + "\n" + Log.getStackTraceString(t);
    }
}
