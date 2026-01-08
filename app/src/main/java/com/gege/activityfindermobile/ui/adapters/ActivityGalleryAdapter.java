package com.gege.activityfindermobile.ui.adapters;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityGalleryAdapter
        extends RecyclerView.Adapter<ActivityGalleryAdapter.PhotoViewHolder> {

    private List<ActivityPhoto> photos;
    private final OnPhotoClickListener listener;
    private final Long currentUserId;

    public interface OnPhotoClickListener {
        void onPhotoClick(ActivityPhoto photo, int position);

        void onPhotoLongClick(ActivityPhoto photo);
    }

    public ActivityGalleryAdapter(Long currentUserId, OnPhotoClickListener listener) {
        this.photos = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setPhotos(List<ActivityPhoto> photos) {
        this.photos = photos != null ? photos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity_gallery_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        ActivityPhoto photo = photos.get(position);
        holder.bind(photo);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPhoto;
        private final CircleImageView ivUserAvatar;
        private final TextView tvUserName;
        private final View uploadedByContainer;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            uploadedByContainer = itemView.findViewById(R.id.uploaded_by_container);
        }

        public void bind(ActivityPhoto photo) {
            // Load photo
            ImageLoader.loadGalleryPhoto(itemView.getContext(), photo.getPhotoUrl(), ivPhoto);

            // Load user avatar and name
            if (photo.getUserAvatar() != null && !photo.getUserAvatar().isEmpty()) {
                ImageLoader.loadCircularProfileImage(
                        itemView.getContext(), photo.getUserAvatar(), ivUserAvatar);
            } else {
                ivUserAvatar.setImageResource(R.drawable.ic_person);
            }

            tvUserName.setText(photo.getUserName() != null ? photo.getUserName() : "Unknown");

            // Click listener - open fullscreen
            itemView.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onPhotoClick(photo, getAdapterPosition());
                        }
                    });

            // Long click - show delete option if user owns the photo
            itemView.setOnLongClickListener(
                    v -> {
                        if (listener != null
                                && currentUserId != null
                                && currentUserId.equals(photo.getUserId())) {
                            listener.onPhotoLongClick(photo);
                            return true;
                        }
                        return false;
                    });
        }
    }

    public List<ActivityPhoto> getPhotos() {
        return photos;
    }
}
