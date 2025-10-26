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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.model.ImageUploadResponse;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.UserPhotoRepository;
import com.gege.activityfindermobile.ui.adapters.PhotoGalleryAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoManagementFragment extends Fragment {

    @Inject UserPhotoRepository userPhotoRepository;

    private RecyclerView rvPhotos;
    private PhotoGalleryAdapter photoAdapter;
    private MaterialButton btnUploadPhoto;
    private SwitchMaterial switchEditMode;
    private TextView tvPhotoCount;
    private View layoutEmpty;
    private CircularProgressIndicator progressLoading;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private List<UserPhoto> currentPhotos = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker
        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                uploadPhoto(uri);
                            }
                        });
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        loadPhotos();
    }

    private void initViews(View view) {
        rvPhotos = view.findViewById(R.id.rv_photos);
        btnUploadPhoto = view.findViewById(R.id.btn_upload_photo);
        switchEditMode = view.findViewById(R.id.switch_edit_mode);
        tvPhotoCount = view.findViewById(R.id.tv_photo_count);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressLoading = view.findViewById(R.id.progress_loading);

        // Setup adapter
        photoAdapter =
                new PhotoGalleryAdapter(
                        currentPhotos,
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
                                // Open fullscreen view (optional)
                            }
                        });

        // Setup grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvPhotos.setLayoutManager(gridLayoutManager);
        rvPhotos.setAdapter(photoAdapter);

        // Button listeners
        btnUploadPhoto.setOnClickListener(v -> openImagePicker());
        switchEditMode.setOnCheckedChangeListener(
                (buttonView, isChecked) -> photoAdapter.setEditMode(isChecked));
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void uploadPhoto(Uri imageUri) {
        setLoading(true);

        try {
            File imageFile = getFileFromUri(imageUri);
            if (imageFile == null) {
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT)
                        .show();
                setLoading(false);
                return;
            }

            userPhotoRepository.uploadPhoto(
                    imageFile,
                    new ApiCallback<ImageUploadResponse>() {
                        @Override
                        public void onSuccess(ImageUploadResponse uploadResponse) {
                            Toast.makeText(
                                            requireContext(),
                                            "Photo uploaded successfully!",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            loadPhotos(); // Refresh the photo list
                        }

                        @Override
                        public void onError(String errorMessage) {
                            setLoading(false);
                            Toast.makeText(
                                            requireContext(),
                                            "Upload failed: " + errorMessage,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPhotos() {
        setLoading(true);

        userPhotoRepository.getMyPhotos(
                new com.gege.activityfindermobile.data.callback.ApiCallback<List<UserPhoto>>() {
                    @Override
                    public void onSuccess(List<UserPhoto> photos) {
                        setLoading(false);
                        currentPhotos.clear();
                        currentPhotos.addAll(photos);
                        photoAdapter.setPhotos(currentPhotos);

                        // Update UI based on photo count
                        updatePhotoCountUI();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load photos: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                        showEmptyState();
                    }
                });
    }

    private void setPhotoAsProfile(UserPhoto photo) {
        setLoading(true);

        userPhotoRepository.setPhotoAsProfile(
                photo.getId(),
                new com.gege.activityfindermobile.data.callback.ApiCallback<UserPhoto>() {
                    @Override
                    public void onSuccess(UserPhoto updatedPhoto) {
                        Toast.makeText(
                                        requireContext(),
                                        "Profile picture updated!",
                                        Toast.LENGTH_SHORT)
                                .show();
                        loadPhotos(); // Refresh to show updated badges
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
                        Toast.makeText(requireContext(), "Photo deleted!", Toast.LENGTH_SHORT)
                                .show();
                        loadPhotos(); // Refresh the photo list
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

    private void updatePhotoCountUI() {
        int photoCount = currentPhotos.size();
        tvPhotoCount.setText(String.format("Photos: %d/6", photoCount));

        // Enable/disable upload button based on count
        btnUploadPhoto.setEnabled(photoCount < 6);
        if (photoCount >= 6) {
            btnUploadPhoto.setText("Maximum photos reached");
        } else {
            btnUploadPhoto.setText("+ Upload Photo");
        }

        // Show empty state or content
        if (photoCount == 0) {
            showEmptyState();
        } else {
            showContent();
        }
    }

    private void showContent() {
        rvPhotos.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        rvPhotos.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private File getFileFromUri(Uri uri) throws Exception {
        ContentResolver contentResolver = requireContext().getContentResolver();
        InputStream inputStream = contentResolver.openInputStream(uri);

        if (inputStream == null) {
            return null;
        }

        String fileName = "photo_" + System.currentTimeMillis();
        String extension = getFileExtension(uri);
        if (extension != null) {
            fileName += "." + extension;
        }

        File tempFile = new File(requireContext().getCacheDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
