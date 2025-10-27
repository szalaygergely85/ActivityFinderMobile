package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.utils.ImageLoader;

import java.util.List;

public class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoGalleryAdapter.ViewHolder> {

    private List<UserPhoto> photos;
    private OnPhotoActionListener listener;
    private boolean isEditMode = false;

    public interface OnPhotoActionListener {
        void onSetAsProfile(UserPhoto photo);

        void onDeletePhoto(UserPhoto photo);

        void onPhotoClick(UserPhoto photo);
    }

    public PhotoGalleryAdapter(List<UserPhoto> photos, OnPhotoActionListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    public void setPhotos(List<UserPhoto> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_photo_gallery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserPhoto photo = photos.get(position);
        holder.bind(photo, isEditMode);
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private TextView tvProfileBadge;
        private ImageButton btnSetAsProfile;
        private ImageButton btnDeletePhoto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            tvProfileBadge = itemView.findViewById(R.id.tv_profile_badge);
            btnSetAsProfile = itemView.findViewById(R.id.btn_set_as_profile);
            btnDeletePhoto = itemView.findViewById(R.id.btn_delete_photo);
        }

        void bind(UserPhoto photo, boolean editMode) {
            Context context = itemView.getContext();

            // Load photo with authorization header
            ImageLoader.loadGalleryPhoto(context, photo.getPhotoUrl(), ivPhoto);

            // Show/hide profile badge - only in edit mode
            if (editMode && photo.getIsProfilePicture() != null && photo.getIsProfilePicture()) {
                tvProfileBadge.setVisibility(View.VISIBLE);
                tvProfileBadge.setText("⭐ Profile");
            } else {
                tvProfileBadge.setVisibility(View.GONE);
            }

            // Show/hide buttons based on edit mode
            if (editMode) {
                btnSetAsProfile.setVisibility(View.VISIBLE);
                btnDeletePhoto.setVisibility(View.VISIBLE);

                // Set as profile button - only show if not already profile picture
                if (photo.getIsProfilePicture() != null && photo.getIsProfilePicture()) {
                    btnSetAsProfile.setEnabled(false);
                    btnSetAsProfile.setAlpha(0.5f);
                } else {
                    btnSetAsProfile.setEnabled(true);
                    btnSetAsProfile.setAlpha(1f);
                    btnSetAsProfile.setOnClickListener(
                            v -> {
                                if (listener != null) {
                                    listener.onSetAsProfile(photo);
                                }
                            });
                }

                // Delete button
                btnDeletePhoto.setOnClickListener(
                        v -> {
                            if (listener != null) {
                                listener.onDeletePhoto(photo);
                            }
                        });
            } else {
                btnSetAsProfile.setVisibility(View.GONE);
                btnDeletePhoto.setVisibility(View.GONE);
            }

            // Photo click listener for viewing
            ivPhoto.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onPhotoClick(photo);
                        }
                    });
        }
    }
}
