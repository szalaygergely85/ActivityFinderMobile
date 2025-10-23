package com.gege.activityfindermobile.ui.profile;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    @Inject UserRepository userRepository;

    @Inject SharedPreferencesManager prefsManager;

    private CircleImageView ivProfilePicture;
    private MaterialButton btnChoosePhoto, btnRemovePhoto;
    private TextInputEditText etFullName, etBio;
    private ChipGroup chipGroupInterests;
    private MaterialButton btnSave;
    private CircularProgressIndicator progressLoading;

    private User currentUser;
    private List<String> selectedInterests = new ArrayList<>();
    private Uri selectedImageUri;
    private boolean imageChanged = false;
    private ActivityResultLauncher<String> imagePickerLauncher;

    // Available interests (in a real app, this would come from API)
    private static final String[] AVAILABLE_INTERESTS = {
        "Sports", "Music", "Art", "Technology", "Gaming",
        "Cooking", "Travel", "Reading", "Photography", "Fitness",
        "Movies", "Dancing", "Hiking", "Yoga", "Food"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker
        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                selectedImageUri = uri;
                                ivProfilePicture.setImageURI(uri);
                                imageChanged = true;
                                btnRemovePhoto.setVisibility(View.VISIBLE);
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
        btnChoosePhoto = view.findViewById(R.id.btn_choose_photo);
        btnRemovePhoto = view.findViewById(R.id.btn_remove_photo);
        etFullName = view.findViewById(R.id.et_full_name);
        etBio = view.findViewById(R.id.et_bio);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        btnSave = view.findViewById(R.id.btn_save);
        progressLoading = view.findViewById(R.id.progress_loading);

        btnChoosePhoto.setOnClickListener(v -> openImagePicker());
        btnRemovePhoto.setOnClickListener(
                v -> {
                    selectedImageUri = null;
                    imageChanged = true;
                    ivProfilePicture.setImageResource(R.drawable.ic_person);
                    btnRemovePhoto.setVisibility(View.GONE);
                });
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
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

        // Load profile image if exists
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            // TODO: Use image loading library like Glide or Picasso
            // For now just show the remove button if there's an image URL
            btnRemovePhoto.setVisibility(View.VISIBLE);
        }

        // Store current interests
        if (user.getInterests() != null) {
            selectedInterests.addAll(user.getInterests());
        }

        // Setup interest chips
        setupInterestChips();
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

        // If image was changed, handle upload
        if (imageChanged) {
            if (selectedImageUri != null) {
                // New image selected, upload it
                uploadImageAndUpdateProfile(userId, fullName, bio);
            } else {
                // Image removed, update profile with null image URL
                updateProfileData(userId, fullName, bio, null);
            }
        } else {
            // Image not changed, keep existing URL
            String existingImageUrl =
                    (currentUser != null && currentUser.getProfileImageUrl() != null)
                            ? currentUser.getProfileImageUrl()
                            : null;
            updateProfileData(userId, fullName, bio, existingImageUrl);
        }
    }

    private void uploadImageAndUpdateProfile(Long userId, String fullName, String bio) {
        // Convert URI to File
        File imageFile = getFileFromUri(selectedImageUri);
        if (imageFile == null) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT)
                    .show();
            // Continue with existing image
            String existingImageUrl =
                    (currentUser != null) ? currentUser.getProfileImageUrl() : null;
            updateProfileData(userId, fullName, bio, existingImageUrl);
            return;
        }

        // Upload image
        userRepository.uploadProfileImage(
                userId,
                imageFile,
                new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        // Image uploaded successfully, now update profile with the URL
                        updateProfileData(userId, fullName, bio, imageUrl);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to upload image: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
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
            btnChoosePhoto.setEnabled(false);
            etFullName.setEnabled(false);
            etBio.setEnabled(false);
            chipGroupInterests.setEnabled(false);
        } else {
            progressLoading.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            btnChoosePhoto.setEnabled(true);
            etFullName.setEnabled(true);
            etBio.setEnabled(true);
            chipGroupInterests.setEnabled(true);
        }
    }
}
