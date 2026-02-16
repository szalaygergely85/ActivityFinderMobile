package com.gege.activityfindermobile.ui.profile;

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
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.ui.adapters.PhotoGalleryAdapter;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject SharedPreferencesManager prefsManager;

    @Inject UserRepository userRepository;

    private CircleImageView ivProfileAvatar;
    private TextView tvName, tvEmail, tvBio, tvRatingValue, tvActivitiesCount, tvPhotoCount;
    private Chip chipBadge;
    private ChipGroup chipGroupInterests;
    private MaterialButton btnEditProfile;
    private CircularProgressIndicator progressLoading;
    private View cardPhotos, layoutPhotosEmpty;
    private RecyclerView rvUserPhotos;
    private PhotoGalleryAdapter photoGalleryAdapter;

    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAppBarPadding(view);
        initViews(view);
        setupListeners();
        loadUserProfile();
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
        // Location Feature
        // tvUserLocation = view.findViewById(R.id.tv_user_location);
        chipBadge = view.findViewById(R.id.chip_badge);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        progressLoading = view.findViewById(R.id.progress_loading);
        cardPhotos = view.findViewById(R.id.card_photos);
        rvUserPhotos = view.findViewById(R.id.rv_user_photos);
        layoutPhotosEmpty = view.findViewById(R.id.layout_photos_empty);
        tvPhotoCount = view.findViewById(R.id.tv_photo_count);
    }

    private void setupListeners() {
        View btnBack = requireView().findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            });
        }

        View btnSettings = requireView().findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> navigateToSettings());
        }

        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
    }

    private void navigateToSettings() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_profile_to_settingsFragment);
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
        userRepository.getUserById(
                userId,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        progressLoading.setVisibility(View.GONE);
                        currentUser = user;
                        displayUserProfile(user);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load profile: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
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
                Chip chip =
                        (Chip)
                                getLayoutInflater()
                                        .inflate(
                                                R.layout.chip_display_item,
                                                chipGroupInterests,
                                                false);
                chip.setText(interest);
                chipGroupInterests.addView(chip);
            }
        } else {
            // Show "No interests" message
            Chip chip =
                    (Chip)
                            getLayoutInflater()
                                    .inflate(R.layout.chip_display_item, chipGroupInterests, false);
            chip.setText("No interests added");
            chipGroupInterests.addView(chip);
        }

        // Load profile image
        ImageLoader.loadCircularProfileImage(
                requireContext(), user.getProfileImageUrl(), ivProfileAvatar);

        // Display user location
        // displayUserLocation();

        // Set photos
        List<UserPhoto> photos = user.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            setupPhotosAdapter(photos);
            cardPhotos.setVisibility(View.VISIBLE);
        } else {
            layoutPhotosEmpty.setVisibility(View.VISIBLE);
            cardPhotos.setVisibility(View.VISIBLE);
        }
    }

    /*
        private void displayUserLocation() {
            if (currentUser != null
                    && currentUser.getCity() != null
                    && !currentUser.getCity().isEmpty()) {
                String locationText = "📍 " + currentUser.getCity();

                tvUserLocation.setText(locationText);
                tvUserLocation.setVisibility(View.VISIBLE);
            } else {
                tvUserLocation.setText("No city set");
                tvUserLocation.setVisibility(View.VISIBLE);
            }
        }
    */
    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_profile_to_loginFragment);
    }

    private void setupPhotosAdapter(List<UserPhoto> photos) {
        photoGalleryAdapter =
                new PhotoGalleryAdapter(
                        photos,
                        new PhotoGalleryAdapter.OnPhotoActionListener() {
                            @Override
                            public void onSetAsProfile(UserPhoto photo) {
                                // Not available in profile view
                            }

                            @Override
                            public void onDeletePhoto(UserPhoto photo) {
                                // Not available in profile view
                            }

                            @Override
                            public void onPhotoClick(UserPhoto photo) {
                                // Open full-screen photo viewer
                                openPhotoViewer(photos, photos.indexOf(photo));
                            }

                            @Override
                            public void onAddPhotoClick() {
                                // Not available in profile view
                            }
                        });
        photoGalleryAdapter.setEditMode(false);
        rvUserPhotos.setAdapter(photoGalleryAdapter);
        tvPhotoCount.setText(photos.size() + "/6");
        layoutPhotosEmpty.setVisibility(View.GONE);
    }

    private void openPhotoViewer(List<UserPhoto> photos, int position) {
        // First, check if the navigation action exists
        try {
            Bundle bundle = new Bundle();
            bundle.putSerializable("photos", new ArrayList<>(photos));
            bundle.putInt("position", position);
            bundle.putBoolean("editMode", false);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.photoViewerFragment, bundle);
        } catch (Exception e) {
            // If navigation fails, show a toast
            Toast.makeText(requireContext(), "Unable to open photo viewer", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
