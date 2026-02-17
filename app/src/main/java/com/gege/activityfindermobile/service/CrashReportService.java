package com.gege.activityfindermobile.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gege.activityfindermobile.data.api.CrashLogApiService;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service that catches uncaught exceptions and sends crash reports to the backend.
 * Implements Thread.UncaughtExceptionHandler to intercept all crashes.
 */
public class CrashReportService implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashReportService";
    private static final String PREFS_NAME = "crash_reports";
    private static final String KEY_PENDING_CRASH = "pending_crash";

    private final Context context;
    private final CrashLogApiService crashLogApiService;
    private final SharedPreferencesManager prefsManager;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public CrashReportService(
            Context context,
            CrashLogApiService crashLogApiService,
            SharedPreferencesManager prefsManager) {
        this.context = context.getApplicationContext();
        this.crashLogApiService = crashLogApiService;
        this.prefsManager = prefsManager;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /** Initialize the crash handler - call this from Application.onCreate() */
    public void initialize() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        Log.d(TAG, "Crash report service initialized");

        // Check for and send any pending crash reports from previous session
        sendPendingCrashReport();
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        Log.e(TAG, "Uncaught exception in thread: " + thread.getName(), throwable);

        try {
            // Build crash report
            Map<String, Object> crashReport = buildCrashReport(throwable);

            // Save crash report locally (in case network fails)
            saveCrashReportLocally(crashReport);

            // Try to send immediately (best effort, may not complete before app dies)
            sendCrashReportSync(crashReport);

        } catch (Exception e) {
            Log.e(TAG, "Error handling crash", e);
        }

        // Call the default handler to let the app crash normally
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        }
    }

    private Map<String, Object> buildCrashReport(Throwable throwable) {
        Map<String, Object> report = new HashMap<>();

        // App info
        report.put("appVersion", getAppVersion());
        report.put("platform", "android");

        // Device info
        report.put("deviceModel", Build.MANUFACTURER + " " + Build.MODEL);
        report.put("osVersion", "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");

        // Error info
        report.put("errorMessage", throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName());
        report.put("stackTrace", getStackTraceString(throwable));

        // Timestamp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            report.put("crashedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        // User ID (if logged in)
        if (prefsManager != null && prefsManager.isLoggedIn()) {
            Long userId = prefsManager.getUserId();
            if (userId != null) {
                report.put("userId", userId);
            }
        }

        return report;
    }

    private String getAppVersion() {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private void saveCrashReportLocally(Map<String, Object> crashReport) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            // Convert to simple string format for storage
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : crashReport.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n---\n");
            }
            prefs.edit().putString(KEY_PENDING_CRASH, sb.toString()).apply();
            Log.d(TAG, "Crash report saved locally");
        } catch (Exception e) {
            Log.e(TAG, "Failed to save crash report locally", e);
        }
    }

    private void sendCrashReportSync(Map<String, Object> crashReport) {
        try {
            // This is a synchronous call - we try our best to send before the app dies
            Call<Map<String, Object>> call = crashLogApiService.submitCrashLog(crashReport);
            Response<Map<String, Object>> response = call.execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Crash report sent successfully");
                clearPendingCrashReport();
            } else {
                Log.e(TAG, "Failed to send crash report: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending crash report", e);
            // Report will be sent on next app launch
        }
    }

    /** Send any crash reports that were saved but not sent */
    private void sendPendingCrashReport() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String pendingCrash = prefs.getString(KEY_PENDING_CRASH, null);

            if (pendingCrash == null || pendingCrash.isEmpty()) {
                return;
            }

            Log.d(TAG, "Found pending crash report, attempting to send...");

            // Parse the saved crash report
            Map<String, Object> crashReport = new HashMap<>();
            String[] parts = pendingCrash.split("\n---\n");
            for (String part : parts) {
                int eqIndex = part.indexOf('=');
                if (eqIndex > 0) {
                    String key = part.substring(0, eqIndex);
                    String value = part.substring(eqIndex + 1);
                    crashReport.put(key, value);
                }
            }

            // Send asynchronously
            crashLogApiService.submitCrashLog(crashReport).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Pending crash report sent successfully");
                        clearPendingCrashReport();
                    } else {
                        Log.e(TAG, "Failed to send pending crash report: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "Network error sending pending crash report", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error processing pending crash report", e);
        }
    }

    private void clearPendingCrashReport() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_PENDING_CRASH).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing pending crash report", e);
        }
    }
}
