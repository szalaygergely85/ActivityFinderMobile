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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.ActivityPhoto;
import com.gege.activityfindermobile.data.repository.ActivityPhotoRepository;
import com.gege.activityfindermobile.ui.adapters.SelectedPhotoAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UploadPhotosFragment extends Fragment {

    @Inject ActivityPhotoRepository activityPhotoRepository;

    private static final int MIN_PHOTOS = 3;
    private static final int MAX_PHOTOS = 40;

    private RecyclerView rvSelectedPhotos;
    private SelectedPhotoAdapter adapter;
    private MaterialButton btnSelectPhotos;
    private MaterialButton btnUpload;
    private TextView tvSelectedCount;
    private TextView tvUploadInfo;
    private CircularProgressIndicator progressLoading;

    private Long activityId;
    private String activityTitle;
    private List<Uri> selectedPhotoUris = new ArrayList<>();
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId", 0L);
            activityTitle = getArguments().getString("activityTitle", "Upload Photos");
        }

        // Register image picker for multiple selection
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
        return inflater.inflate(R.layout.fragment_upload_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        setupRecyclerView();
    }

    private void initViews(View view) {
        rvSelectedPhotos = view.findViewById(R.id.rv_selected_photos);
        btnSelectPhotos = view.findViewById(R.id.btn_select_photos);
        btnUpload = view.findViewById(R.id.btn_upload);
        tvSelectedCount = view.findViewById(R.id.tv_selected_count);
        tvUploadInfo = view.findViewById(R.id.tv_upload_info);
        progressLoading = view.findViewById(R.id.progress_loading);

        btnSelectPhotos.setOnClickListener(v -> openImagePicker());
        btnUpload.setOnClickListener(v -> uploadPhotos());

        updateUploadInfo(0);
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new SelectedPhotoAdapter(position -> {
            selectedPhotoUris.remove(position);
            adapter.removePhoto(position);
            updateUI();
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        rvSelectedPhotos.setLayoutManager(gridLayoutManager);
        rvSelectedPhotos.setAdapter(adapter);
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void handleSelectedPhotos(List<Uri> uris) {
        if (uris.size() > MAX_PHOTOS) {
            Toast.makeText(
                    requireContext(),
                    "Maximum " + MAX_PHOTOS + " photos allowed",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        selectedPhotoUris = new ArrayList<>(uris);
        adapter.setPhotos(selectedPhotoUris);
        updateUI();
    }

    private void updateUI() {
        int count = selectedPhotoUris.size();
        boolean hasPhotos = count > 0;
        boolean canUpload = count >= MIN_PHOTOS && count <= MAX_PHOTOS;

        rvSelectedPhotos.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
        tvSelectedCount.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
        btnUpload.setVisibility(canUpload ? View.VISIBLE : View.GONE);

        if (hasPhotos) {
            String countText = count == 1 ? "1 photo selected" : count + " photos selected";
            tvSelectedCount.setText(countText);
        }

        updateUploadInfo(count);
    }

    private void updateUploadInfo(int count) {
        if (count < MIN_PHOTOS) {
            tvUploadInfo.setText("Select " + MIN_PHOTOS + "-" + MAX_PHOTOS + " photos from this event");
        } else if (count > MAX_PHOTOS) {
            tvUploadInfo.setText("Too many photos! Maximum is " + MAX_PHOTOS);
        } else {
            tvUploadInfo.setText("Ready to upload " + count + " photos");
        }
    }

    private void uploadPhotos() {
        if (selectedPhotoUris.isEmpty() || selectedPhotoUris.size() < MIN_PHOTOS) {
            Toast.makeText(
                    requireContext(),
                    "Please select at least " + MIN_PHOTOS + " photos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Convert URIs to File objects
        List<File> imageFiles = new ArrayList<>();
        for (Uri uri : selectedPhotoUris) {
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

        activityPhotoRepository.uploadPhotos(activityId, imageFiles, new ApiCallback<List<ActivityPhoto>>() {
            @Override
            public void onSuccess(List<ActivityPhoto> photos) {
                setLoading(false);
                Toast.makeText(
                        requireContext(),
                        photos.size() + " photos uploaded successfully!",
                        Toast.LENGTH_SHORT).show();

                // Navigate back to gallery
                requireActivity().onBackPressed();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(requireContext(), "Upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
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

            File file = new File(requireContext().getCacheDir(), "upload_" + System.currentTimeMillis() + "." + extension);
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

    private void setLoading(boolean loading) {
        if (progressLoading != null) {
            progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSelectPhotos.setEnabled(!loading);
            btnUpload.setEnabled(!loading);
        }
    }
}
