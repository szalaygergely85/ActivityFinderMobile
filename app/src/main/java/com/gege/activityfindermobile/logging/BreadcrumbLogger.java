package com.gege.activityfindermobile.logging;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;

/**
 * Tracks breadcrumbs (app actions) for crash reports.
 * Keeps a circular buffer of the last N log entries.
 */
public class BreadcrumbLogger {

    private static final int MAX_BREADCRUMBS = 100;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private final Deque<String> breadcrumbs = new ArrayDeque<>(MAX_BREADCRUMBS);
    private static BreadcrumbLogger instance;

    private BreadcrumbLogger() {}

    public static synchronized BreadcrumbLogger getInstance() {
        if (instance == null) {
            instance = new BreadcrumbLogger();
        }
        return instance;
    }

    /** Add a breadcrumb entry */
    public synchronized void log(String category, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String breadcrumb = String.format("[%s] %s: %s", timestamp, category, message);

        breadcrumbs.addLast(breadcrumb);

        if (breadcrumbs.size() > MAX_BREADCRUMBS) {
            breadcrumbs.removeFirst();
        }
    }

    /** Get all breadcrumbs as a formatted string */
    public synchronized String getBreadcrumbsAsString() {
        if (breadcrumbs.isEmpty()) {
            return "No breadcrumbs recorded";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== BREADCRUMB LOG (Last ").append(breadcrumbs.size()).append(" actions) ===\n");
        for (String breadcrumb : breadcrumbs) {
            sb.append(breadcrumb).append("\n");
        }
        return sb.toString();
    }

    /** Clear all breadcrumbs */
    public synchronized void clear() {
        breadcrumbs.clear();
    }

    /** Get breadcrumb count */
    public synchronized int size() {
        return breadcrumbs.size();
    }
}
