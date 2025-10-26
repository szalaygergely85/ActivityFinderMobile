package com.gege.activityfindermobile.ui.adapters;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.utils.ImageLoader;

import java.util.List;

public class FullScreenPhotoAdapter
        extends RecyclerView.Adapter<FullScreenPhotoAdapter.PhotoViewHolder> {

    private List<UserPhoto> photos;

    public FullScreenPhotoAdapter(List<UserPhoto> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(
                new ViewPager2.LayoutParams(
                        ViewPager2.LayoutParams.MATCH_PARENT,
                        ViewPager2.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new PhotoViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        UserPhoto photo = photos.get(position);
        ImageLoader.loadGalleryPhoto(
                holder.imageView.getContext(), photo.getPhotoUrl(), holder.imageView);
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        PhotoViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }
}
