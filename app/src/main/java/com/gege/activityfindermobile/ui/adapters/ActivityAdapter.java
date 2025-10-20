package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Activity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private List<Activity> activities = new ArrayList<>();
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public ActivityAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDate, tvTime, tvLocation;
        TextView tvCreatorName, tvCreatorRating, tvSpotsAvailable;
        Chip chipCategory, badgeTrending;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCreatorName = itemView.findViewById(R.id.tv_creator_name);
            tvCreatorRating = itemView.findViewById(R.id.tv_creator_rating);
            tvSpotsAvailable = itemView.findViewById(R.id.tv_spots_available);
            chipCategory = itemView.findViewById(R.id.chip_category);
            badgeTrending = itemView.findViewById(R.id.badge_trending);

            itemView.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onActivityClick(activities.get(getAdapterPosition()));
                        }
                    });
        }

        void bind(Activity activity) {
            Context context = itemView.getContext();
            tvTitle.setText(activity.getTitle());
            tvDescription.setText(activity.getDescription());

            // Handle date with null/empty check
            String date = activity.getDate();
            if (date != null && !date.isEmpty()) {
                tvDate.setText(date);
                tvDate.setVisibility(View.VISIBLE);
            } else {
                tvDate.setText("Date TBD");
                tvDate.setVisibility(View.VISIBLE);
            }

            // Handle time with null/empty check
            String time = activity.getTime();
            if (time != null && !time.isEmpty()) {
                tvTime.setText(time);
                tvTime.setVisibility(View.VISIBLE);
            } else {
                tvTime.setText("Time TBD");
                tvTime.setVisibility(View.VISIBLE);
            }

            tvLocation.setText(activity.getLocation());
            tvCreatorName.setText(activity.getCreatorName());

            // Handle rating with null check
            if (activity.getCreatorRating() != null) {
                tvCreatorRating.setText(String.format("%.1f", activity.getCreatorRating()));
            } else {
                tvCreatorRating.setText("N/A");
            }

            // Get current participants count from API or calculate
            int currentParticipants = activity.getParticipantsCount();
            int totalSpots = activity.getTotalSpots() != null ? activity.getTotalSpots() : 0;
            tvSpotsAvailable.setText(currentParticipants + "/" + totalSpots + " spots");

            // Set category with dynamic color and icon
            String category = activity.getCategory();
            chipCategory.setText(category);
            setCategoryStyleAndIcon(chipCategory, category, context);

            badgeTrending.setVisibility(
                    activity.getTrending() != null && activity.getTrending()
                            ? View.VISIBLE
                            : View.GONE);
        }

        private void setCategoryStyleAndIcon(Chip chip, String category, Context context) {
            int colorRes;
            int iconRes;

            switch (category.toLowerCase()) {
                case "sports":
                    colorRes = R.color.category_sports;
                    iconRes = R.drawable.ic_sports;
                    break;
                case "social":
                    colorRes = R.color.category_social;
                    iconRes = R.drawable.ic_social;
                    break;
                case "outdoor":
                    colorRes = R.color.category_outdoor;
                    iconRes = R.drawable.ic_outdoor;
                    break;
                case "food":
                    colorRes = R.color.category_food;
                    iconRes = R.drawable.ic_food;
                    break;
                case "travel":
                    colorRes = R.color.category_travel;
                    iconRes = R.drawable.ic_travel;
                    break;
                case "photography":
                    colorRes = R.color.category_photography;
                    iconRes = R.drawable.ic_photography;
                    break;
                case "music":
                    colorRes = R.color.category_music;
                    iconRes = R.drawable.ic_music;
                    break;
                case "art":
                    colorRes = R.color.category_art;
                    iconRes = R.drawable.ic_art;
                    break;
                case "gaming":
                    colorRes = R.color.category_gaming;
                    iconRes = R.drawable.ic_gaming;
                    break;
                case "fitness":
                    colorRes = R.color.category_fitness;
                    iconRes = R.drawable.ic_fitness;
                    break;
                default:
                    colorRes = R.color.primary;
                    iconRes = R.drawable.ic_category;
                    break;
            }

            int color = ContextCompat.getColor(context, colorRes);
            chip.setChipBackgroundColor(ColorStateList.valueOf(color));
            chip.setChipIcon(ContextCompat.getDrawable(context, iconRes));
            chip.setChipIconTint(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        }
    }
}
