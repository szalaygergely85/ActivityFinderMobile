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

public class PhotoGalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PHOTO = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    private List<UserPhoto> photos;
    private OnPhotoActionListener listener;
    private boolean isEditMode = false;
    private boolean showAddButton = false;

    public interface OnPhotoActionListener {
        void onSetAsProfile(UserPhoto photo);

        void onDeletePhoto(UserPhoto photo);

        void onPhotoClick(UserPhoto photo);

        void onAddPhotoClick();
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

    public void setShowAddButton(boolean showAddButton) {
        this.showAddButton = showAddButton;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int photoCount = photos != null ? photos.size() : 0;
        // If showing add button and this is the last position, it's the add button
        if (showAddButton && position == photoCount) {
            return TYPE_ADD_BUTTON;
        }
        return TYPE_PHOTO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD_BUTTON) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_photo_add_button, parent, false);
            return new AddButtonViewHolder(view);
        } else {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_photo_gallery, parent, false);
            return new PhotoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PhotoViewHolder) {
            UserPhoto photo = photos.get(position);
            ((PhotoViewHolder) holder).bind(photo, isEditMode);
        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        int photoCount = photos != null ? photos.size() : 0;
        return showAddButton ? photoCount + 1 : photoCount;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View tvProfileBadge;
        private ImageButton btnSetAsProfile;
        private ImageButton btnDeletePhoto;
        private View bottomOverlay;
        private View layoutActions;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            tvProfileBadge = itemView.findViewById(R.id.tv_profile_badge);
            btnSetAsProfile = itemView.findViewById(R.id.btn_set_as_profile);
            btnDeletePhoto = itemView.findViewById(R.id.btn_delete_photo);
            bottomOverlay = itemView.findViewById(R.id.bottom_overlay);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }

        void bind(UserPhoto photo, boolean editMode) {
            Context context = itemView.getContext();

            // Load photo with authorization header
            ImageLoader.loadGalleryPhoto(context, photo.getPhotoUrl(), ivPhoto);

            // Show/hide profile badge - only in edit mode
            if (editMode && photo.getIsProfilePicture() != null && photo.getIsProfilePicture()) {
                tvProfileBadge.setVisibility(View.VISIBLE);
            } else {
                tvProfileBadge.setVisibility(View.GONE);
            }

            // Show/hide bottom overlay and buttons based on edit mode
            if (editMode) {
                bottomOverlay.setVisibility(View.VISIBLE);
                layoutActions.setVisibility(View.VISIBLE);

                // Set as profile button - only show if not already profile picture
                if (photo.getIsProfilePicture() != null && photo.getIsProfilePicture()) {
                    btnSetAsProfile.setVisibility(View.GONE);
                } else {
                    btnSetAsProfile.setVisibility(View.VISIBLE);
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
                bottomOverlay.setVisibility(View.GONE);
                layoutActions.setVisibility(View.GONE);
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

    class AddButtonViewHolder extends RecyclerView.ViewHolder {
        AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind() {
            itemView.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onAddPhotoClick();
                        }
                    });
        }
    }
}
