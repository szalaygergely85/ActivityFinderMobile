package com.gege.activityfindermobile.ui.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.CoverImage;
import com.gege.activityfindermobile.data.repository.CoverImageRepository;
import com.gege.activityfindermobile.ui.adapters.CoverImageAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CoverImagePickerDialog extends BottomSheetDialogFragment
        implements CoverImageAdapter.OnCoverImageClickListener {

    @Inject CoverImageRepository coverImageRepository;

    private RecyclerView rvCoverImages;
    private ProgressBar progressLoading;
    private TextView tvEmpty;
    private MaterialButton btnUseDefault;

    private CoverImageAdapter adapter;
    private OnCoverImageSelectedListener listener;
    private String currentSelectedUrl;

    public interface OnCoverImageSelectedListener {
        void onCoverImageSelected(CoverImage coverImage);

        void onClearCoverImage();
    }

    public static CoverImagePickerDialog newInstance(String currentSelectedUrl) {
        CoverImagePickerDialog dialog = new CoverImagePickerDialog();
        Bundle args = new Bundle();
        args.putString("currentSelectedUrl", currentSelectedUrl);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSelectedUrl = getArguments().getString("currentSelectedUrl");
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_cover_image_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCoverImages = view.findViewById(R.id.rv_cover_images);
        progressLoading = view.findViewById(R.id.progress_loading);
        tvEmpty = view.findViewById(R.id.tv_empty);
        btnUseDefault = view.findViewById(R.id.btn_use_default);

        setupRecyclerView();
        setupButtons();
        loadCoverImages();
    }

    private void setupRecyclerView() {
        adapter = new CoverImageAdapter(this);
        adapter.setSelectedImageUrl(currentSelectedUrl);

        rvCoverImages.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvCoverImages.setAdapter(adapter);
    }

    private void setupButtons() {
        btnUseDefault.setOnClickListener(
                v -> {
                    if (listener != null) {
                        listener.onClearCoverImage();
                    }
                    dismiss();
                });
    }

    private void loadCoverImages() {
        progressLoading.setVisibility(View.VISIBLE);
        rvCoverImages.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        coverImageRepository.getCoverImages(
                new ApiCallback<List<CoverImage>>() {
                    @Override
                    public void onSuccess(List<CoverImage> coverImages) {
                        progressLoading.setVisibility(View.GONE);

                        if (coverImages == null || coverImages.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvCoverImages.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            rvCoverImages.setVisibility(View.VISIBLE);
                            adapter.setCoverImages(coverImages);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressLoading.setVisibility(View.GONE);
                        tvEmpty.setText("Failed to load images");
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvCoverImages.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onCoverImageClick(CoverImage coverImage) {
        if (listener != null) {
            listener.onCoverImageSelected(coverImage);
        }
        dismiss();
    }

    public void setOnCoverImageSelectedListener(OnCoverImageSelectedListener listener) {
        this.listener = listener;
    }
}
