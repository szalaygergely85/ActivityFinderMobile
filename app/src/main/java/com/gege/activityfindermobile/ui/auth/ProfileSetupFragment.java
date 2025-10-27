package com.gege.activityfindermobile.ui.auth;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
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
public class ProfileSetupFragment extends Fragment {

    @Inject UserRepository userRepository;

    @Inject UserPhotoRepository userPhotoRepository;

    @Inject SharedPreferencesManager prefsManager;

    private CircleImageView ivProfilePicture;
    private MaterialButton btnChoosePhoto, btnRemovePhoto, btnContinue, btnSkip;
    private MaterialButton btnUploadPhoto;
    private TextInputEditText etBio;
    private ChipGroup chipGroupInterests;
    private CircularProgressIndicator progressLoading;
    private RecyclerView rvSetupPhotos;
    private LinearLayout layoutSetupPhotosEmpty;
    private PhotoGalleryAdapter photoGalleryAdapter;
    private List<UserPhoto> setupPhotos = new ArrayList<>();
    private de.hdodenhof.circleimageview.CircleImageView ivSetupProfilePicture;

    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker for profile picture
        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                selectedImageUri = uri;
                                ivProfilePicture.setImageURI(uri);
                                btnRemovePhoto.setVisibility(View.VISIBLE);
                            }
                        });

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
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        btnChoosePhoto = view.findViewById(R.id.btn_choose_photo);
        btnRemovePhoto = view.findViewById(R.id.btn_remove_photo);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnSkip = view.findViewById(R.id.btn_skip);
        btnUploadPhoto = view.findViewById(R.id.btn_upload_photo_setup);
        etBio = view.findViewById(R.id.et_bio);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        progressLoading = view.findViewById(R.id.progress_loading);
        rvSetupPhotos = view.findViewById(R.id.rv_setup_photos);
        layoutSetupPhotosEmpty = view.findViewById(R.id.layout_setup_photos_empty);
        ivSetupProfilePicture = view.findViewById(R.id.iv_setup_profile_picture);
    }

    private void setupListeners() {
        btnChoosePhoto.setOnClickListener(v -> openImagePicker());

        btnRemovePhoto.setOnClickListener(
                v -> {
                    selectedImageUri = null;
                    ivProfilePicture.setImageResource(R.drawable.ic_person);
                    btnRemovePhoto.setVisibility(View.GONE);
                });

        btnUploadPhoto.setOnClickListener(v -> openPhotoGalleryPicker());
        btnContinue.setOnClickListener(v -> saveProfileAndContinue());

        btnSkip.setOnClickListener(v -> navigateToFeed());
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void saveProfileAndContinue() {
        String bio = etBio.getText().toString().trim();
        List<String> interests = getSelectedInterests();

        // If nothing is filled, just skip
        if (bio.isEmpty() && interests.isEmpty() && selectedImageUri == null) {
            navigateToFeed();
            return;
        }

        // Show loading
        setLoading(true);

        // Get user ID
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            navigateToFeed();
            return;
        }

        // If user selected an image, upload it first
        if (selectedImageUri != null) {
            uploadImageAndUpdateProfile(userId, bio, interests);
        } else {
            // No image, just update profile with bio and interests
            updateProfile(userId, bio, interests, null);
        }
    }

    private void uploadImageAndUpdateProfile(Long userId, String bio, List<String> interests) {
        // Convert URI to File
        File imageFile = getFileFromUri(selectedImageUri);
        if (imageFile == null) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            // Continue anyway with just bio and interests
            updateProfile(userId, bio, interests, null);
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
                        updateProfile(userId, bio, interests, imageUrl);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Image upload failed, but still update profile without it
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to upload image, but profile will be updated",
                                        Toast.LENGTH_SHORT)
                                .show();
                        updateProfile(userId, bio, interests, null);
                    }
                });
    }

    private void updateProfile(Long userId, String bio, List<String> interests, String imageUrl) {
        // Create update request
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        if (!bio.isEmpty()) {
            request.setBio(bio);
        }
        if (!interests.isEmpty()) {
            request.setInterests(interests);
        }
        if (imageUrl != null) {
            request.setProfileImageUrl(imageUrl);
        }

        // Update profile
        userRepository.updateUserProfile(
                userId,
                userId,
                request,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Profile updated successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();
                        navigateToFeed();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        // Even if update fails, still let them continue
                        Toast.makeText(
                                        requireContext(),
                                        "Could not update profile, but you can edit it later",
                                        Toast.LENGTH_SHORT)
                                .show();
                        navigateToFeed();
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

    private List<String> getSelectedInterests() {
        List<String> interests = new ArrayList<>();

        for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
            View child = chipGroupInterests.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    interests.add(chip.getText().toString());
                }
            }
        }

        return interests;
    }

    private void navigateToFeed() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_profileSetupFragment_to_nav_feed);
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnContinue.setEnabled(false);
            btnSkip.setEnabled(false);
            btnChoosePhoto.setEnabled(false);
            btnUploadPhoto.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
        } else {
            btnContinue.setEnabled(true);
            btnSkip.setEnabled(true);
            btnChoosePhoto.setEnabled(true);
            btnUploadPhoto.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
        }
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
                        setLoading(false);
                        Toast.makeText(requireContext(), "Photo uploaded!", Toast.LENGTH_SHORT)
                                .show();
                        // Reload photos from backend
                        loadSetupPhotos();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to upload: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void loadSetupPhotos() {
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
                        setupPhotos = photos;
                        displaySetupPhotos(photos);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        layoutSetupPhotosEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void displaySetupPhotos(List<UserPhoto> photos) {
        if (photos != null && !photos.isEmpty()) {
            rvSetupPhotos.setVisibility(View.VISIBLE);
            layoutSetupPhotosEmpty.setVisibility(View.GONE);
            setupPhotoAdapter(photos);
        } else {
            rvSetupPhotos.setVisibility(View.GONE);
            layoutSetupPhotosEmpty.setVisibility(View.VISIBLE);
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
        rvSetupPhotos.setAdapter(photoGalleryAdapter);
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
                        for (UserPhoto p : setupPhotos) {
                            p.setIsProfilePicture(false);
                        }
                        // Find and update the specific photo in the list
                        for (UserPhoto p : setupPhotos) {
                            if (p.getId().equals(updatedPhoto.getId())) {
                                p.setIsProfilePicture(true);
                                break;
                            }
                        }
                        photoGalleryAdapter.notifyDataSetChanged();

                        // Update profile picture display
                        ImageLoader.loadCircularProfileImage(
                                requireContext(),
                                updatedPhoto.getPhotoUrl(),
                                ivSetupProfilePicture);

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
                                        "Failed to update: " + errorMessage,
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
                        setupPhotos.remove(photo);
                        displaySetupPhotos(setupPhotos);
                        Toast.makeText(requireContext(), "Photo deleted!", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to delete: " + errorMessage,
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
                .navigate(R.id.action_profileSetupFragment_to_photoViewerFragment, bundle);
    }
}
