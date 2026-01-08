package com.gege.activityfindermobile.ui.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.UserPhotoRepository;
import com.gege.activityfindermobile.ui.adapters.PhotoGalleryAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserPhotosFragment extends Fragment {

    @Inject UserPhotoRepository userPhotoRepository;

    private RecyclerView rvPhotos;
    private PhotoGalleryAdapter photoAdapter;
    private View layoutEmpty;
    private CircularProgressIndicator progressLoading;

    private List<UserPhoto> currentPhotos = new ArrayList<>();
    private Long userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get userId from arguments
        if (getArguments() != null) {
            userId = getArguments().getLong("userId", -1L);
            if (userId == -1L) {
                userId = null;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        loadUserPhotos();
    }

    private void initViews(View view) {
        rvPhotos = view.findViewById(R.id.rv_photos);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressLoading = view.findViewById(R.id.progress_loading);

        // Setup adapter (view-only mode, no edit buttons)
        photoAdapter =
                new PhotoGalleryAdapter(
                        currentPhotos,
                        new PhotoGalleryAdapter.OnPhotoActionListener() {
                            @Override
                            public void onSetAsProfile(UserPhoto photo) {}

                            @Override
                            public void onDeletePhoto(UserPhoto photo) {}

                            @Override
                            public void onPhotoClick(UserPhoto photo) {
                                // Optional: open full-screen view
                            }

                            @Override
                            public void onAddPhotoClick() {
                                // Not available in this view
                            }
                        });

        // Setup grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvPhotos.setLayoutManager(gridLayoutManager);
        rvPhotos.setAdapter(photoAdapter);
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void loadUserPhotos() {
        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        userPhotoRepository.getUserPhotos(
                userId,
                new com.gege.activityfindermobile.data.callback.ApiCallback<List<UserPhoto>>() {
                    @Override
                    public void onSuccess(List<UserPhoto> photos) {
                        setLoading(false);
                        currentPhotos.clear();
                        currentPhotos.addAll(photos);
                        photoAdapter.setPhotos(currentPhotos);

                        if (currentPhotos.isEmpty()) {
                            showEmptyState();
                        } else {
                            showContent();
                        }
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
}
