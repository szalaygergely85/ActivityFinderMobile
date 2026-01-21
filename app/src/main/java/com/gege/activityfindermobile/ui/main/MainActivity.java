package com.gege.activityfindermobile.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.repository.NotificationRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject SharedPreferencesManager prefsManager;
    @Inject NotificationRepository notificationRepository;

    private static final String TAG = "MainActivity";

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private Handler notificationPollingHandler;
    private Runnable notificationPollingRunnable;

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, "Notification permission granted");
                        } else {
                            Log.w(TAG, "Notification permission denied");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
        checkLoginStatus();
        setupNotificationPolling();
        requestNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notification badge when returning to the app
        if (prefsManager.isLoggedIn()) {
            updateNotificationBadge();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop polling when activity is destroyed
        if (notificationPollingHandler != null && notificationPollingRunnable != null) {
            notificationPollingHandler.removeCallbacks(notificationPollingRunnable);
        }
    }

    private void checkLoginStatus() {
        // If user is not logged in, navigate to login screen
        if (!prefsManager.isLoggedIn()) {
            navController.navigate(R.id.loginFragment);
        }
    }

    private void setupNavigation() {
        // Get NavHostFragment
        NavHostFragment navHostFragment =
                (NavHostFragment)
                        getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Hide bottom nav on certain screens
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    if (destination.getId() == R.id.loginFragment
                            || destination.getId() == R.id.registerFragment
                            || destination.getId() == R.id.profileSetupFragment
                            || destination.getId() == R.id.activityDetailFragment
                            || destination.getId() == R.id.createActivityFragment
                            || destination.getId() == R.id.userProfileFragment) {
                        bottomNavigationView.setVisibility(View.GONE);
                    } else {
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void setupNotificationPolling() {
        notificationPollingHandler = new Handler(Looper.getMainLooper());
        notificationPollingRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        if (prefsManager.isLoggedIn()) {
                            updateNotificationBadge();
                        }
                        // Poll every 30 seconds
                        notificationPollingHandler.postDelayed(this, 30000);
                    }
                };
        // Start polling immediately
        notificationPollingHandler.post(notificationPollingRunnable);
    }

    private void updateNotificationBadge() {
        notificationRepository.getUnreadCount(
                new ApiCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer count) {
                        BadgeDrawable badge =
                                bottomNavigationView.getOrCreateBadge(R.id.notificationsFragment);
                        if (count != null && count > 0) {
                            badge.setVisible(true);
                            badge.setNumber(count);
                            // Set badge appearance
                            badge.setBackgroundColor(getResources().getColor(R.color.error, null));
                            badge.setBadgeTextColor(getResources().getColor(R.color.white, null));
                        } else {
                            badge.setVisible(false);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Silently fail - don't show error to user for background polling
                    }
                });
    }

    public void refreshNotificationBadge() {
        updateNotificationBadge();
    }

    private void requestNotificationPermission() {
        // Only needed for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
