package com.gege.activityfindermobile.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.repository.NotificationRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private static final String PREF_DISTANCE_UNIT = "distance_unit";
    private static final String PREF_NOTIFY_ACTIVITY = "notify_activity_updates";
    private static final String PREF_NOTIFY_REMINDERS = "notify_reminders";

    @Inject SharedPreferencesManager prefsManager;
    @Inject NotificationRepository notificationRepository;

    private TextView tvEmail;
    private TextView tvDistanceUnit;
    private TextView tvVersion;
    private SwitchMaterial switchActivityUpdates;
    private SwitchMaterial switchReminders;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAppBarPadding(view);
        loadSettings();
        setupListeners(view);
    }

    private void setupAppBarPadding(View view) {
        View appBar = view.findViewById(R.id.app_bar);
        if (appBar == null) return;

        final int originalPaddingTop = appBar.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    appBar.setPadding(
                            appBar.getPaddingLeft(),
                            systemBars.top + originalPaddingTop,
                            appBar.getPaddingRight(),
                            appBar.getPaddingBottom());
                    return insets;
                });
    }



    private void initViews(View view) {
        tvEmail = view.findViewById(R.id.tv_email);
        tvDistanceUnit = view.findViewById(R.id.tv_distance_unit);
        tvVersion = view.findViewById(R.id.tv_version);
        switchActivityUpdates = view.findViewById(R.id.switch_activity_updates);
        switchReminders = view.findViewById(R.id.switch_reminders);
    }

    private void loadSettings() {
        // Load email
        String email = prefsManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            tvEmail.setText("Not set");
        } else {
            tvEmail.setText(email);
        }

        // Load distance unit
        boolean useKilometers = prefsManager.getBoolean(PREF_DISTANCE_UNIT, true);
        tvDistanceUnit.setText(useKilometers ? "Kilometers" : "Miles");

        // Load notification preferences
        switchActivityUpdates.setChecked(prefsManager.getBoolean(PREF_NOTIFY_ACTIVITY, true));
        switchReminders.setChecked(prefsManager.getBoolean(PREF_NOTIFY_REMINDERS, true));

        // Load version
        try {
            PackageInfo pInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            tvVersion.setText("Version " + pInfo.versionName + " (" + pInfo.versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Version unknown");
        }
    }

    private void setupListeners(View view) {
        // Back button
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        // Change password
        view.findViewById(R.id.item_password).setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Distance units
        view.findViewById(R.id.item_distance_units).setOnClickListener(v -> {
            showDistanceUnitsDialog();
        });

        // Notification switches
        switchActivityUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.putBoolean(PREF_NOTIFY_ACTIVITY, isChecked);
            syncNotificationPreferences();
        });

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.putBoolean(PREF_NOTIFY_REMINDERS, isChecked);
            syncNotificationPreferences();
        });

        // Help Center
        view.findViewById(R.id.item_help).setOnClickListener(v -> {
            openUrl("https://vivento.fun/support");
        });

        // Privacy Policy
        view.findViewById(R.id.item_privacy).setOnClickListener(v -> {
            openUrl("https://vivento.fun/privacy");
        });

        // Terms of Service
        view.findViewById(R.id.item_terms).setOnClickListener(v -> {
            openUrl("https://vivento.fun/terms");
        });

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            showLogoutDialog();
        });

        // Delete Account
        view.findViewById(R.id.btn_delete_account).setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
    }

    private void syncNotificationPreferences() {
        boolean activityUpdates = prefsManager.getBoolean(PREF_NOTIFY_ACTIVITY, true);
        boolean reminders = prefsManager.getBoolean(PREF_NOTIFY_REMINDERS, true);
        notificationRepository.updateNotificationPreferences(
                activityUpdates,
                reminders,
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        Log.d("SettingsFragment", "Notification preferences synced");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e("SettingsFragment", "Failed to sync preferences: " + errorMessage);
                    }
                });
    }

    private void showChangePasswordDialog() {
        // Navigate to a password change screen or show a dialog
        Toast.makeText(requireContext(), "Password change coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showDistanceUnitsDialog() {
        boolean currentUseKm = prefsManager.getBoolean(PREF_DISTANCE_UNIT, true);
        int selectedIndex = currentUseKm ? 0 : 1;

        String[] options = {"Kilometers", "Miles"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Distance Units")
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    boolean useKm = (which == 0);
                    prefsManager.putBoolean(PREF_DISTANCE_UNIT, useKm);
                    tvDistanceUnit.setText(useKm ? "Kilometers" : "Miles");
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        prefsManager.clearUserSession();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_settingsFragment_to_loginFragment);
    }

    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone. All your data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    openUrl("https://vivento.fun/delete-account");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
