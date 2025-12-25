package com.gege.activityfindermobile.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SelectedPhotoAdapter extends RecyclerView.Adapter<SelectedPhotoAdapter.PhotoViewHolder> {

    private List<Uri> photoUris;
    private final OnPhotoRemovedListener listener;

    public interface OnPhotoRemovedListener {
        void onPhotoRemoved(int position);
    }

    public SelectedPhotoAdapter(OnPhotoRemovedListener listener) {
        this.photoUris = new ArrayList<>();
        this.listener = listener;
    }

    public void setPhotos(List<Uri> uris) {
        this.photoUris = uris != null ? uris : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removePhoto(int position) {
        if (position >= 0 && position < photoUris.size()) {
            photoUris.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, photoUris.size());
        }
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photoUris.get(position);
        holder.bind(photoUri);
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPhoto;
        private final FloatingActionButton btnRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(Uri photoUri) {
            // Load photo from URI
            ivPhoto.setImageURI(photoUri);

            // Remove button click
            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPhotoRemoved(position);
                }
            });
        }
    }
}
