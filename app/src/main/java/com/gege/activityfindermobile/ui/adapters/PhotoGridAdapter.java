package com.gege.activityfindermobile.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.utils.ImageLoader;

import java.util.List;

public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder> {

    private List<UserPhoto> photos;
    private boolean isEditMode;
    private OnPhotoClickListener onPhotoClickListener;

    public interface OnPhotoClickListener {
        void onPhotoClick(UserPhoto photo, int position, List<UserPhoto> photoList);
    }

    public PhotoGridAdapter(List<UserPhoto> photos, boolean isEditMode) {
        this.photos = photos;
        this.isEditMode = isEditMode;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    public void setPhotos(List<UserPhoto> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_photo_grid, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        UserPhoto photo = photos.get(position);
        holder.bind(photo, position);
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
        }

        void bind(UserPhoto photo, int position) {
            // Load photo using ImageLoader with authorization headers
            ImageLoader.loadGalleryPhoto(itemView.getContext(), photo.getPhotoUrl(), ivPhoto);

            // Set click listener to navigate to full-screen viewer
            itemView.setOnClickListener(
                    v -> {
                        if (onPhotoClickListener != null) {
                            onPhotoClickListener.onPhotoClick(photo, position, photos);
                        }
                    });
        }
    }
}
