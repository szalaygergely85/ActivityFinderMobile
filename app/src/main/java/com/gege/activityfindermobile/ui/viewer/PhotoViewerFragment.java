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
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.UserPhotoRepository;
import com.gege.activityfindermobile.ui.adapters.FullScreenPhotoAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoViewerFragment extends Fragment {

    @Inject UserPhotoRepository userPhotoRepository;

    private ViewPager2 viewPagerPhotos;
    private ImageButton btnClose;
    private TextView tvPhotoCounter;
    private MaterialButton btnSetAsProfile;
    private MaterialButton btnDeletePhoto;
    private LinearLayout layoutEditButtons;
    private FullScreenPhotoAdapter photoAdapter;

    private List<UserPhoto> photos = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isEditMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            photos = (ArrayList<UserPhoto>) getArguments().getSerializable("photos");
            currentPosition = getArguments().getInt("position", 0);
            isEditMode = getArguments().getBoolean("editMode", false);
        }

        if (photos == null) {
            photos = new ArrayList<>();
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
        setupViewPager();
    }

    private void initViews(View view) {
        viewPagerPhotos = view.findViewById(R.id.view_pager_photos);
        btnClose = view.findViewById(R.id.btn_close);
        tvPhotoCounter = view.findViewById(R.id.tv_photo_counter);
        btnSetAsProfile = view.findViewById(R.id.btn_set_as_profile_fullscreen);
        btnDeletePhoto = view.findViewById(R.id.btn_delete_photo_fullscreen);
        layoutEditButtons = view.findViewById(R.id.layout_edit_buttons);

        btnClose.setOnClickListener(v -> requireActivity().onBackPressed());

        // Only show edit buttons in edit mode
        if (isEditMode) {
            layoutEditButtons.setVisibility(View.VISIBLE);
            btnSetAsProfile.setOnClickListener(v -> setCurrentPhotoAsProfile());
            btnDeletePhoto.setOnClickListener(v -> deleteCurrentPhoto());
        }
    }

    private void setupViewPager() {
        photoAdapter = new FullScreenPhotoAdapter(photos);
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
        tvPhotoCounter.setText(String.format("%d/%d", currentPosition + 1, photos.size()));
    }

    private void setCurrentPhotoAsProfile() {
        if (currentPosition < 0 || currentPosition >= photos.size()) {
            return;
        }

        UserPhoto currentPhoto = photos.get(currentPosition);
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
                        for (int i = 0; i < photos.size(); i++) {
                            if (i != currentPosition) {
                                photos.get(i).setIsProfilePicture(false);
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
        if (currentPosition < 0 || currentPosition >= photos.size()) {
            return;
        }

        UserPhoto currentPhoto = photos.get(currentPosition);
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
                        photos.remove(currentPosition);

                        if (photos.isEmpty()) {
                            // No more photos, go back
                            requireActivity().onBackPressed();
                        } else {
                            // Adjust current position if necessary
                            if (currentPosition >= photos.size()) {
                                currentPosition = photos.size() - 1;
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
}
