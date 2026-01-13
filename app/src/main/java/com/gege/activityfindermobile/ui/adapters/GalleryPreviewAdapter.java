package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.ActivityPhoto;
import com.gege.activityfindermobile.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for showing gallery preview in activity detail
 * Shows first 3 photos with "See All" overlay on the last one
 */
public class GalleryPreviewAdapter extends RecyclerView.Adapter<GalleryPreviewAdapter.ViewHolder> {

    private static final int MAX_PREVIEW_PHOTOS = 3;

    private final Context context;
    private List<ActivityPhoto> photos = new ArrayList<>();
    private int totalPhotoCount = 0;
    private OnGalleryClickListener listener;

    public interface OnGalleryClickListener {
        void onPhotoClick(int position);
        void onSeeAllClick();
    }

    public GalleryPreviewAdapter(Context context) {
        this.context = context;
    }

    public void setPhotos(List<ActivityPhoto> photos) {
        this.photos = photos != null ? photos : new ArrayList<>();
        this.totalPhotoCount = this.photos.size();
        notifyDataSetChanged();
    }

    public void setOnGalleryClickListener(OnGalleryClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return Math.min(photos.size(), MAX_PREVIEW_PHOTOS);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_gallery_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityPhoto photo = photos.get(position);

        // Load photo
        ImageLoader.loadGalleryPhoto(context, photo.getPhotoUrl(), holder.ivPhoto);

        // Check if this is the last preview item and there are more photos
        boolean isLastPreview = position == MAX_PREVIEW_PHOTOS - 1;
        boolean hasMorePhotos = totalPhotoCount > MAX_PREVIEW_PHOTOS;

        if (isLastPreview && hasMorePhotos) {
            // Show "See All" overlay
            holder.overlaySeeAll.setVisibility(View.VISIBLE);
            int remainingCount = totalPhotoCount;
            holder.tvSeeAllCount.setText("See All " + remainingCount);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSeeAllClick();
                }
            });
        } else {
            // Hide overlay and set photo click
            holder.overlaySeeAll.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoClick(position);
                }
            });
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        View overlaySeeAll;
        TextView tvSeeAllCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_preview_photo);
            overlaySeeAll = itemView.findViewById(R.id.overlay_see_all);
            tvSeeAllCount = itemView.findViewById(R.id.tv_see_all_count);
        }
    }
}
