package com.gege.activityfindermobile.ui.gallery;

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
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.model.ActivityPhoto;
import com.gege.activityfindermobile.data.repository.ActivityPhotoRepository;
import com.gege.activityfindermobile.ui.adapters.ActivityGalleryAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityGalleryFragment extends Fragment {

    @Inject ActivityPhotoRepository activityPhotoRepository;
    @Inject SharedPreferencesManager prefsManager;

    private static final int MIN_PHOTOS = 1;
    private static final int MAX_PHOTOS = 30;

    private RecyclerView rvPhotos;
    private ActivityGalleryAdapter adapter;
    private TextView tvActivityTitle;
    private TextView tvPhotoCount;
    private View layoutEmpty;
    private CircularProgressIndicator progressLoading;
    private ExtendedFloatingActionButton fabUpload;

    private Long activityId;
    private String activityTitle;
    private Long currentUserId;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId", 0L);
            activityTitle = getArguments().getString("activityTitle", "Event Gallery");
        }

        currentUserId = prefsManager.getUserId();

        // Register image picker for multiple photo selection
        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetMultipleContents(),
                        uris -> {
                            if (uris != null && !uris.isEmpty()) {
                                handleSelectedPhotos(uris);
                            }
                        });
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle window insets for top bar and bottom FAB
        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    com.google.android.material.appbar.AppBarLayout appBar =
                            v.findViewById(R.id.app_bar);
                    if (appBar != null) {
                        appBar.setPadding(0, systemBars.top, 0, 0);
                    }

                    // Adjust FAB for bottom insets
                    ExtendedFloatingActionButton fabUpload = v.findViewById(R.id.fab_upload);
                    if (fabUpload != null) {
                        ViewGroup.MarginLayoutParams params =
                                (ViewGroup.MarginLayoutParams) fabUpload.getLayoutParams();
                        params.bottomMargin =
                                systemBars.bottom
                                        + (int) getResources().getDimension(R.dimen.margin_medium);
                        fabUpload.setLayoutParams(params);
                    }

                    return insets;
                });

        initViews(view);
        setupToolbar(view);
        setupRecyclerView();
        loadPhotos();
    }

    private void initViews(View view) {
        rvPhotos = view.findViewById(R.id.rv_photos);
        tvActivityTitle = view.findViewById(R.id.tv_activity_title);
        tvPhotoCount = view.findViewById(R.id.tv_photo_count);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressLoading = view.findViewById(R.id.progress_loading);
        fabUpload = view.findViewById(R.id.fab_upload);

        tvActivityTitle.setText(activityTitle);

        // Open photo picker directly when FAB is clicked
        fabUpload.setOnClickListener(v -> openImagePicker());
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        adapter =
                new ActivityGalleryAdapter(
                        currentUserId,
                        new ActivityGalleryAdapter.OnPhotoClickListener() {
                            @Override
                            public void onPhotoClick(ActivityPhoto photo, int position) {
                                // Navigate to fullscreen photo viewer
                                navigateToPhotoViewer(position);
                            }

                            @Override
                            public void onPhotoLongClick(ActivityPhoto photo) {
                                // Show delete confirmation
                                showDeleteConfirmation(photo);
                            }
                        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvPhotos.setLayoutManager(gridLayoutManager);
        rvPhotos.setAdapter(adapter);
    }

    private void loadPhotos() {
        setLoading(true);

        activityPhotoRepository.getActivityPhotos(
                activityId,
                new ApiCallback<List<ActivityPhoto>>() {
                    @Override
                    public void onSuccess(List<ActivityPhoto> photos) {
                        setLoading(false);

                        if (photos != null && !photos.isEmpty()) {
                            adapter.setPhotos(photos);
                            updatePhotoCount(photos.size());
                            showContent();
                        } else {
                            // No photos - clear adapter and show empty view
                            adapter.setPhotos(new ArrayList<>());
                            updatePhotoCount(0);
                            showEmptyView();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        adapter.setPhotos(new ArrayList<>());
                        updatePhotoCount(0);
                        showEmptyView();
                    }
                });
    }

    private void updatePhotoCount(int count) {
        String photoText = count == 1 ? "1 photo" : count + " photos";
        tvPhotoCount.setText(photoText);
    }

    private void showDeleteConfirmation(ActivityPhoto photo) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> deletePhoto(photo))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePhoto(ActivityPhoto photo) {
        setLoading(true);

        activityPhotoRepository.deletePhoto(
                activityId,
                photo.getId(),
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        Toast.makeText(requireContext(), "Photo deleted", Toast.LENGTH_SHORT)
                                .show();
                        loadPhotos(); // Reload photos
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void handleSelectedPhotos(List<Uri> uris) {
        if (uris.isEmpty()) {
            Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uris.size() > MAX_PHOTOS) {
            Toast.makeText(
                            requireContext(),
                            "Maximum "
                                    + MAX_PHOTOS
                                    + " photos allowed. Please select fewer photos.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Show confirmation dialog
        String message =
                uris.size() == 1
                        ? "Upload 1 photo to this activity gallery?"
                        : "Upload " + uris.size() + " photos to this activity gallery?";

        new AlertDialog.Builder(requireContext())
                .setTitle("Upload Photos")
                .setMessage(message)
                .setPositiveButton("Upload", (dialog, which) -> uploadPhotos(uris))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadPhotos(List<Uri> uris) {
        setLoading(true);

        // Convert URIs to File objects
        List<File> imageFiles = new ArrayList<>();
        for (Uri uri : uris) {
            File file = getFileFromUri(uri);
            if (file != null) {
                imageFiles.add(file);
            }
        }

        if (imageFiles.isEmpty()) {
            setLoading(false);
            Toast.makeText(requireContext(), "Failed to process images", Toast.LENGTH_SHORT).show();
            return;
        }

        activityPhotoRepository.uploadPhotos(
                activityId,
                imageFiles,
                new ApiCallback<List<ActivityPhoto>>() {
                    @Override
                    public void onSuccess(List<ActivityPhoto> photos) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        photos.size() + " photos uploaded successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Reload photos to show newly uploaded ones
                        loadPhotos();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Upload failed: " + errorMessage,
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

            if (extension == null) {
                extension = "jpg";
            }

            File file =
                    new File(
                            requireContext().getCacheDir(),
                            "upload_" + System.currentTimeMillis() + "." + extension);
            InputStream inputStream = contentResolver.openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void navigateToPhotoViewer(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putLong("activityId", activityId);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_activityGalleryFragment_to_photoViewerFragment, bundle);
    }

    private void setLoading(boolean loading) {
        if (progressLoading != null) {
            progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showContent() {
        if (rvPhotos != null && layoutEmpty != null) {
            rvPhotos.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmptyView() {
        if (rvPhotos != null && layoutEmpty != null) {
            rvPhotos.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }
}
