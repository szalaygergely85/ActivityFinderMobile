package com.gege.activityfindermobile.ui.viewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.model.ActivityPhoto;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.ActivityPhotoRepository;
import com.gege.activityfindermobile.data.repository.UserPhotoRepository;
import com.gege.activityfindermobile.ui.adapters.FullScreenPhotoAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoViewerFragment extends Fragment {

    @Inject UserPhotoRepository userPhotoRepository;
    @Inject ActivityPhotoRepository activityPhotoRepository;
    @Inject SharedPreferencesManager prefsManager;

    private ViewPager2 viewPagerPhotos;
    private ImageButton btnClose;
    private TextView tvPhotoCounter;
    private TextView btnEdit;
    private MaterialButton btnSetAsProfile;
    private MaterialButton btnDeletePhoto;
    private LinearLayout layoutEditButtons;
    private FullScreenPhotoAdapter photoAdapter;

    private List<UserPhoto> userPhotos = new ArrayList<>();
    private List<ActivityPhoto> activityPhotos = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isEditMode = false;
    private boolean isActivityGallery = false;
    private Long activityId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Check if this is for activity gallery
            activityId = getArguments().getLong("activityId", 0L);
            if (activityId > 0) {
                isActivityGallery = true;
                currentPosition = getArguments().getInt("position", 0);
            } else {
                // User profile photos
                userPhotos = (ArrayList<UserPhoto>) getArguments().getSerializable("photos");
                currentPosition = getArguments().getInt("position", 0);
                isEditMode = getArguments().getBoolean("editMode", false);
            }
        }

        if (userPhotos == null) {
            userPhotos = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        if (isActivityGallery) {
            loadActivityPhotos();
        } else {
            setupViewPager();
        }
    }

    private void initViews(View view) {
        viewPagerPhotos = view.findViewById(R.id.view_pager_photos);
        btnClose = view.findViewById(R.id.btn_close);
        tvPhotoCounter = view.findViewById(R.id.tv_photo_counter);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnSetAsProfile = view.findViewById(R.id.btn_set_as_profile_fullscreen);
        btnDeletePhoto = view.findViewById(R.id.btn_delete_photo_fullscreen);
        layoutEditButtons = view.findViewById(R.id.layout_edit_buttons);

        btnClose.setOnClickListener(v -> requireActivity().onBackPressed());

        // Set up edit button click listener
        btnEdit.setOnClickListener(
                v -> {
                    isEditMode = !isEditMode;
                    updateEditModeVisibility();
                });

        // Set up action button listeners
        btnSetAsProfile.setOnClickListener(v -> setCurrentPhotoAsProfile());
        btnDeletePhoto.setOnClickListener(v -> deleteCurrentPhoto());

        // Show/hide edit button and action buttons based on mode
        if (isActivityGallery) {
            // For activity gallery, hide edit button
            btnEdit.setVisibility(View.GONE);
            // Show delete button if user owns the photo
            layoutEditButtons.setVisibility(View.VISIBLE);
            btnSetAsProfile.setVisibility(View.GONE);
            btnDeletePhoto.setOnClickListener(v -> deleteCurrentActivityPhoto());
        } else {
            // For user photos, show edit button
            btnEdit.setVisibility(View.VISIBLE);
            updateEditModeVisibility();
        }
    }

    private void updateEditModeVisibility() {
        if (isEditMode) {
            layoutEditButtons.setVisibility(View.VISIBLE);
        } else {
            layoutEditButtons.setVisibility(View.GONE);
        }
    }

    private void loadActivityPhotos() {
        activityPhotoRepository.getActivityPhotos(activityId, new ApiCallback<List<ActivityPhoto>>() {
            @Override
            public void onSuccess(List<ActivityPhoto> photos) {
                activityPhotos = photos;
                if (activityPhotos != null && !activityPhotos.isEmpty()) {
                    setupActivityPhotoViewPager();
                } else {
                    Toast.makeText(requireContext(), "No photos found", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Failed to load photos: " + errorMessage, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });
    }

    private void setupActivityPhotoViewPager() {
        // Convert ActivityPhoto list to UserPhoto list for the adapter
        List<UserPhoto> convertedPhotos = new ArrayList<>();
        for (ActivityPhoto activityPhoto : activityPhotos) {
            UserPhoto userPhoto = new UserPhoto();
            userPhoto.setId(activityPhoto.getId());
            userPhoto.setPhotoUrl(activityPhoto.getPhotoUrl());
            convertedPhotos.add(userPhoto);
        }

        photoAdapter = new FullScreenPhotoAdapter(convertedPhotos);
        viewPagerPhotos.setAdapter(photoAdapter);
        viewPagerPhotos.setCurrentItem(currentPosition, false);

        // Update counter when page changes
        viewPagerPhotos.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        currentPosition = position;
                        updatePhotoCounter();
                        updateDeleteButtonVisibility();
                    }
                });

        updatePhotoCounter();
        updateDeleteButtonVisibility();
    }

    private void setupViewPager() {
        photoAdapter = new FullScreenPhotoAdapter(userPhotos);
        viewPagerPhotos.setAdapter(photoAdapter);
        viewPagerPhotos.setCurrentItem(currentPosition, false);

        // Update counter when page changes
        viewPagerPhotos.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        currentPosition = position;
                        updatePhotoCounter();
                    }
                });

        updatePhotoCounter();
    }

    private void updatePhotoCounter() {
        int totalPhotos = isActivityGallery ? activityPhotos.size() : userPhotos.size();
        tvPhotoCounter.setText(String.format("%d/%d", currentPosition + 1, totalPhotos));
    }

    private void updateDeleteButtonVisibility() {
        if (!isActivityGallery || activityPhotos.isEmpty() || currentPosition >= activityPhotos.size()) {
            btnDeletePhoto.setVisibility(View.GONE);
            return;
        }

        ActivityPhoto currentPhoto = activityPhotos.get(currentPosition);
        Long currentUserId = prefsManager.getUserId();

        // Only show delete button if user owns this photo
        if (currentUserId != null && currentUserId.equals(currentPhoto.getUserId())) {
            btnDeletePhoto.setVisibility(View.VISIBLE);
        } else {
            btnDeletePhoto.setVisibility(View.GONE);
        }
    }

    private void setCurrentPhotoAsProfile() {
        if (currentPosition < 0 || currentPosition >= userPhotos.size()) {
            return;
        }

        UserPhoto currentPhoto = userPhotos.get(currentPosition);
        userPhotoRepository.setPhotoAsProfile(
                currentPhoto.getId(),
                new ApiCallback<UserPhoto>() {
                    @Override
                    public void onSuccess(UserPhoto updatedPhoto) {
                        Toast.makeText(
                                        requireContext(),
                                        "Profile picture updated!",
                                        Toast.LENGTH_SHORT)
                                .show();
                        currentPhoto.setIsProfilePicture(true);
                        // Update other photos to not be profile picture
                        for (int i = 0; i < userPhotos.size(); i++) {
                            if (i != currentPosition) {
                                userPhotos.get(i).setIsProfilePicture(false);
                            }
                        }
                        photoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to update: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void deleteCurrentPhoto() {
        if (currentPosition < 0 || currentPosition >= userPhotos.size()) {
            return;
        }

        UserPhoto currentPhoto = userPhotos.get(currentPosition);
        userPhotoRepository.deletePhoto(
                currentPhoto.getId(),
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                        requireContext(),
                                        "Photo deleted successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();
                        userPhotos.remove(currentPosition);

                        if (userPhotos.isEmpty()) {
                            // No more photos, go back
                            requireActivity().onBackPressed();
                        } else {
                            // Adjust current position if necessary
                            if (currentPosition >= userPhotos.size()) {
                                currentPosition = userPhotos.size() - 1;
                            }
                            photoAdapter.notifyDataSetChanged();
                            updatePhotoCounter();
                            viewPagerPhotos.setCurrentItem(currentPosition, false);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to delete: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void deleteCurrentActivityPhoto() {
        if (currentPosition < 0 || currentPosition >= activityPhotos.size()) {
            return;
        }

        ActivityPhoto currentPhoto = activityPhotos.get(currentPosition);
        Long currentUserId = prefsManager.getUserId();

        // Verify user owns this photo
        if (currentUserId == null || !currentUserId.equals(currentPhoto.getUserId())) {
            Toast.makeText(requireContext(), "You can only delete your own photos", Toast.LENGTH_SHORT).show();
            return;
        }

        activityPhotoRepository.deletePhoto(
                activityId,
                currentPhoto.getId(),
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                        requireContext(),
                                        "Photo deleted successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();
                        activityPhotos.remove(currentPosition);

                        if (activityPhotos.isEmpty()) {
                            // No more photos, go back
                            requireActivity().onBackPressed();
                        } else {
                            // Adjust current position if necessary
                            if (currentPosition >= activityPhotos.size()) {
                                currentPosition = activityPhotos.size() - 1;
                            }
                            // Reload to update adapter
                            setupActivityPhotoViewPager();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to delete: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
