package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.CoverImage;
import com.gege.activityfindermobile.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CoverImageAdapter extends RecyclerView.Adapter<CoverImageAdapter.ViewHolder> {

    private final List<CoverImage> coverImages = new ArrayList<>();
    private final OnCoverImageClickListener listener;
    private String selectedImageUrl = null;

    public interface OnCoverImageClickListener {
        void onCoverImageClick(CoverImage coverImage);
    }

    public CoverImageAdapter(OnCoverImageClickListener listener) {
        this.listener = listener;
    }

    public void setCoverImages(List<CoverImage> images) {
        coverImages.clear();
        if (images != null) {
            coverImages.addAll(images);
        }
        notifyDataSetChanged();
    }

    public void setSelectedImageUrl(String imageUrl) {
        this.selectedImageUrl = imageUrl;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_cover_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(coverImages.get(position));
    }

    @Override
    public int getItemCount() {
        return coverImages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCoverThumb;
        private final ImageView ivSelectedCheck;
        private final View viewSelectedOverlay;
        private final TextView tvDisplayName;
        private final Context context;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            ivCoverThumb = itemView.findViewById(R.id.iv_cover_thumb);
            ivSelectedCheck = itemView.findViewById(R.id.iv_selected_check);
            viewSelectedOverlay = itemView.findViewById(R.id.view_selected_overlay);
            tvDisplayName = itemView.findViewById(R.id.tv_display_name);
        }

        void bind(CoverImage coverImage) {
            // Load image
            String imageUrl = coverImage.getImageUrl();
            String fullUrl = buildFullUrl(imageUrl);

            Glide.with(context)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.activity_default)
                    .error(R.drawable.activity_default)
                    .into(ivCoverThumb);

            // Display name
            if (coverImage.getDisplayName() != null && !coverImage.getDisplayName().isEmpty()) {
                tvDisplayName.setText(coverImage.getDisplayName());
                tvDisplayName.setVisibility(View.VISIBLE);
            } else {
                tvDisplayName.setVisibility(View.GONE);
            }

            // Selected state
            boolean isSelected =
                    selectedImageUrl != null && selectedImageUrl.equals(coverImage.getImageUrl());
            ivSelectedCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            viewSelectedOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Click listener
            itemView.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onCoverImageClick(coverImage);
                        }
                    });
        }

        private String buildFullUrl(String imageUrl) {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return "";
            }
            if (imageUrl.startsWith("http")) {
                return imageUrl;
            }
            String baseUrl = Constants.BASE_URL;
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            return baseUrl + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
        }
    }
}
