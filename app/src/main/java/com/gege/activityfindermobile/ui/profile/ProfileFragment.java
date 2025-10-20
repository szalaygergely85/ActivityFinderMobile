package com.gege.activityfindermobile.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    SharedPreferencesManager prefsManager;

    @Inject
    UserRepository userRepository;

    private CircleImageView ivProfileAvatar;
    private TextView tvName, tvEmail, tvBio, tvRatingValue, tvActivitiesCount;
    private Chip chipBadge;
    private ChipGroup chipGroupInterests;
    private MaterialButton btnLogout, btnEditProfile;
    private CircularProgressIndicator progressLoading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        loadUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when returning from edit screen
        loadUserProfile();
    }

    private void initViews(View view) {
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvBio = view.findViewById(R.id.tv_bio);
        tvRatingValue = view.findViewById(R.id.tv_rating_value);
        tvActivitiesCount = view.findViewById(R.id.tv_activities_count);
        chipBadge = view.findViewById(R.id.chip_badge);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        progressLoading = view.findViewById(R.id.progress_loading);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
    }

    private void navigateToEditProfile() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_profile_to_editProfileFragment);
    }

    private void loadUserProfile() {
        Long userId = prefsManager.getUserId();

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Show loading
        progressLoading.setVisibility(View.VISIBLE);

        // Fetch user profile
        userRepository.getUserById(userId, new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                progressLoading.setVisibility(View.GONE);
                displayUserProfile(user);
            }

            @Override
            public void onError(String errorMessage) {
                progressLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load profile: " + errorMessage, Toast.LENGTH_LONG).show();

                // Show placeholder data if API fails
                displayPlaceholderData();
            }
        });
    }

    private void displayUserProfile(User user) {
        // Basic info
        tvName.setText(user.getFullName());
        tvEmail.setText(user.getEmail());

        // Bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setText(user.getBio());
        } else {
            tvBio.setText("No bio added yet");
            tvBio.setTextColor(getResources().getColor(R.color.text_hint, null));
        }

        // Stats
        if (user.getRating() != null) {
            tvRatingValue.setText(String.format("%.1f", user.getRating()));
        } else {
            tvRatingValue.setText("N/A");
        }

        if (user.getCompletedActivities() != null) {
            tvActivitiesCount.setText(String.valueOf(user.getCompletedActivities()));
        } else {
            tvActivitiesCount.setText("0");
        }

        // Badge
        if (user.getBadge() != null && !user.getBadge().isEmpty()) {
            chipBadge.setText(user.getBadge());
            chipBadge.setVisibility(View.VISIBLE);
        } else {
            chipBadge.setVisibility(View.GONE);
        }

        // Interests
        chipGroupInterests.removeAllViews();
        List<String> interests = user.getInterests();
        if (interests != null && !interests.isEmpty()) {
            for (String interest : interests) {
                Chip chip = new Chip(requireContext());
                chip.setText(interest);
                chip.setChipBackgroundColorResource(R.color.primary_light);
                chip.setClickable(false);
                chipGroupInterests.addView(chip);
            }
        } else {
            // Show "No interests" message
            Chip chip = new Chip(requireContext());
            chip.setText("No interests added");
            chip.setChipBackgroundColorResource(R.color.gray_light);
            chip.setClickable(false);
            chipGroupInterests.addView(chip);
        }

        // TODO: Load profile image with Glide if profileImageUrl exists
        // For now, keep default avatar
    }

    private void displayPlaceholderData() {
        tvName.setText("User");
        tvEmail.setText("user@example.com");
        tvBio.setText("Welcome to Activity Finder!");
        tvRatingValue.setText("0.0");
        tvActivitiesCount.setText("0");
        chipBadge.setVisibility(View.GONE);
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
        // Clear user session
        prefsManager.clearUserSession();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        navigateToLogin();
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_profile_to_loginFragment);
    }
}
