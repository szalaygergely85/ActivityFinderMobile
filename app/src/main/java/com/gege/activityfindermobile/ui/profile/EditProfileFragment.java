package com.gege.activityfindermobile.ui.profile;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.model.ImageUploadResponse;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.UserPhotoRepository;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.ui.adapters.PhotoGalleryAdapter;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    @Inject UserRepository userRepository;

    @Inject UserPhotoRepository userPhotoRepository;

    @Inject SharedPreferencesManager prefsManager;

    private CircleImageView ivProfilePicture;
    private TextInputEditText etFullName, etBio;
    private ChipGroup chipGroupInterests;
    private MaterialButton btnSave, btnUploadPhoto;
    private CircularProgressIndicator progressLoading;
    private RecyclerView rvMyPhotos;
    private TextView tvPhotoCount;
    private View layoutPhotosEmpty;

    private User currentUser;
    private List<String> selectedInterests = new ArrayList<>();
    private ActivityResultLauncher<String> photoPickerLauncher;
    private PhotoGalleryAdapter photoGalleryAdapter;
    private List<UserPhoto> userPhotos = new ArrayList<>();

    // Available interests (in a real app, this would come from API)
    private static final String[] AVAILABLE_INTERESTS = {
        "Sports", "Music", "Art", "Technology", "Gaming",
        "Cooking", "Travel", "Reading", "Photography", "Fitness",
        "Movies", "Dancing", "Hiking", "Yoga", "Food"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register photo picker for gallery
        photoPickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                uploadPhotoToGallery(uri);
                            }
                        });
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        loadCurrentProfile();
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        etFullName = view.findViewById(R.id.et_full_name);
        etBio = view.findViewById(R.id.et_bio);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        btnSave = view.findViewById(R.id.btn_save);
        progressLoading = view.findViewById(R.id.progress_loading);
        btnUploadPhoto = view.findViewById(R.id.btn_upload_photo);
        rvMyPhotos = view.findViewById(R.id.rv_my_photos);
        tvPhotoCount = view.findViewById(R.id.tv_photo_count);
        layoutPhotosEmpty = view.findViewById(R.id.layout_photos_empty);

        btnSave.setOnClickListener(v -> saveProfile());
        btnUploadPhoto.setOnClickListener(v -> openPhotoGalleryPicker());
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void loadCurrentProfile() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        setLoading(true);

        userRepository.getUserById(
                userId,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        setLoading(false);
                        currentUser = user;
                        displayUserData(user);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load profile: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                        requireActivity().onBackPressed();
                    }
                });
    }

    private void displayUserData(User user) {
        // Set current values
        if (user.getFullName() != null) {
            etFullName.setText(user.getFullName());
        }

        if (user.getBio() != null) {
            etBio.setText(user.getBio());
        }

        // Load profile image from photos if exists
        if (user.getPhotos() != null) {
            for (UserPhoto photo : user.getPhotos()) {
                if (photo.getIsProfilePicture() != null && photo.getIsProfilePicture()) {
                    ImageLoader.loadCircularProfileImage(
                            requireContext(), photo.getPhotoUrl(), ivProfilePicture);
                    break;
                }
            }
        }

        // Store current interests
        if (user.getInterests() != null) {
            selectedInterests.addAll(user.getInterests());
        }

        // Setup interest chips
        setupInterestChips();

        // Load user photos
        loadUserPhotos();
    }

    private void setupInterestChips() {
        chipGroupInterests.removeAllViews();

        for (String interest : AVAILABLE_INTERESTS) {
            Chip chip = new Chip(requireContext());
            chip.setText(interest);
            chip.setCheckable(true);
            chip.setChecked(selectedInterests.contains(interest));

            chip.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (isChecked) {
                            if (!selectedInterests.contains(interest)) {
                                selectedInterests.add(interest);
                            }
                        } else {
                            selectedInterests.remove(interest);
                        }
                    });

            chipGroupInterests.addView(chip);
        }
    }

    private void saveProfile() {
        // Validate input
        String fullName =
                etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String bio = etBio.getText() != null ? etBio.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            etFullName.setError("Name cannot be empty");
            etFullName.requestFocus();
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Profile image is now managed through the photos upload
        updateProfileData(userId, fullName, bio, null);
    }

    private void updateProfileData(Long userId, String fullName, String bio, String imageUrl) {
        // Create update request
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName(fullName);
        request.setBio(bio.isEmpty() ? null : bio);
        request.setInterests(selectedInterests.isEmpty() ? null : selectedInterests);
        request.setProfileImageUrl(imageUrl);

        userRepository.updateUserProfile(
                userId,
                userId,
                request,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User updatedUser) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Profile updated successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Navigate back to profile
                        requireActivity().onBackPressed();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to update profile: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private File getFileFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = requireContext().getContentResolver();
            String mimeType = contentResolver.getType(uri);
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension == null) extension = "jpg";

            // Create a temporary file
            File tempFile =
                    File.createTempFile(
                            "profile_image_", "." + extension, requireContext().getCacheDir());

            // Copy URI content to file
            InputStream inputStream = contentResolver.openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressLoading.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            btnUploadPhoto.setEnabled(false);
            etFullName.setEnabled(false);
            etBio.setEnabled(false);
            chipGroupInterests.setEnabled(false);
        } else {
            progressLoading.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            btnUploadPhoto.setEnabled(true);
            etFullName.setEnabled(true);
            etBio.setEnabled(true);
            chipGroupInterests.setEnabled(true);
        }
    }

    private void loadUserPhotos() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            setLoading(false);
            return;
        }

        userPhotoRepository.getMyPhotos(
                new ApiCallback<List<UserPhoto>>() {
                    @Override
                    public void onSuccess(List<UserPhoto> photos) {
                        setLoading(false);
                        userPhotos = photos;
                        displayPhotos(photos);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        layoutPhotosEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void displayPhotos(List<UserPhoto> photos) {
        if (photos != null && !photos.isEmpty()) {
            tvPhotoCount.setText(photos.size() + "/6");
            btnUploadPhoto.setEnabled(photos.size() < 6);
            setupPhotoAdapter(photos);
            layoutPhotosEmpty.setVisibility(View.GONE);
        } else {
            layoutPhotosEmpty.setVisibility(View.VISIBLE);
            tvPhotoCount.setText("0/6");
            btnUploadPhoto.setEnabled(true);
        }
    }

    private void setupPhotoAdapter(List<UserPhoto> photos) {
        photoGalleryAdapter =
                new PhotoGalleryAdapter(
                        photos,
                        new PhotoGalleryAdapter.OnPhotoActionListener() {
                            @Override
                            public void onSetAsProfile(UserPhoto photo) {
                                setPhotoAsProfile(photo);
                            }

                            @Override
                            public void onDeletePhoto(UserPhoto photo) {
                                deletePhoto(photo);
                            }

                            @Override
                            public void onPhotoClick(UserPhoto photo) {
                                openPhotoViewer(photos, photos.indexOf(photo));
                            }
                        });
        photoGalleryAdapter.setEditMode(true);
        rvMyPhotos.setAdapter(photoGalleryAdapter);
    }

    private void openPhotoGalleryPicker() {
        photoPickerLauncher.launch("image/*");
    }

    private void uploadPhotoToGallery(Uri photoUri) {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = getFileFromUri(photoUri);
        if (photoFile == null) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        userPhotoRepository.uploadPhoto(
                photoFile,
                new ApiCallback<ImageUploadResponse>() {
                    @Override
                    public void onSuccess(ImageUploadResponse uploadResponse) {
                        Toast.makeText(
                                        requireContext(),
                                        "Photo uploaded successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();
                        // Reload photos from backend to get complete data
                        // loadUserPhotos() will call setLoading(false) when done
                        loadUserPhotos();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to upload photo: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void setPhotoAsProfile(UserPhoto photo) {
        setLoading(true);
        userPhotoRepository.setPhotoAsProfile(
                photo.getId(),
                new ApiCallback<UserPhoto>() {
                    @Override
                    public void onSuccess(UserPhoto updatedPhoto) {
                        setLoading(false);
                        // Update local list - set all to false first
                        for (UserPhoto p : userPhotos) {
                            p.setIsProfilePicture(false);
                        }
                        // Find and update the specific photo in the list
                        for (UserPhoto p : userPhotos) {
                            if (p.getId().equals(updatedPhoto.getId())) {
                                p.setIsProfilePicture(true);
                                break;
                            }
                        }
                        photoGalleryAdapter.notifyDataSetChanged();

                        // Update profile picture display at top
                        ImageLoader.loadCircularProfileImage(
                                requireContext(), updatedPhoto.getPhotoUrl(), ivProfilePicture);

                        Toast.makeText(
                                        requireContext(),
                                        "Profile picture updated!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to update profile picture: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void deletePhoto(UserPhoto photo) {
        setLoading(true);
        userPhotoRepository.deletePhoto(
                photo.getId(),
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        userPhotos.remove(photo);
                        displayPhotos(userPhotos);
                        Toast.makeText(
                                        requireContext(),
                                        "Photo deleted successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to delete photo: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void openPhotoViewer(List<UserPhoto> photos, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", new ArrayList<>(photos));
        bundle.putInt("position", position);
        bundle.putBoolean("editMode", true);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_editProfileFragment_to_photoViewerFragment, bundle);
    }
}
